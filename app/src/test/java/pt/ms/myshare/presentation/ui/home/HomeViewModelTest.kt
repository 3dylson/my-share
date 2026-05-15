package pt.ms.myshare.presentation.ui.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import java.math.BigDecimal

import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.use_case.GetReviewHistoryUseCase
import pt.ms.myshare.domain.use_case.UpdateGoalProgressUseCase
import pt.ms.myshare.domain.use_case.GetPerformanceStatsUseCase
import pt.ms.myshare.domain.use_case.GetCoachingInsightsUseCase
import pt.ms.myshare.domain.model.Goal
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var fakePlannerRepository: FakePlannerRepository
    private lateinit var fakeEntitlementRepository: TestFakeEntitlementRepository
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)
    private val mockGetReviewHistoryUseCase = mockk<GetReviewHistoryUseCase>(relaxed = true)
    private val mockUpdateGoalProgressUseCase = mockk<UpdateGoalProgressUseCase>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePlannerRepository = FakePlannerRepository()
        fakeEntitlementRepository = TestFakeEntitlementRepository()
        
        every { mockAuthRepository.currentUser } returns flowOf(null)
        every { mockGetReviewHistoryUseCase.execute() } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            plannerRepository = fakePlannerRepository,
            authRepository = mockAuthRepository,
            entitlementRepository = fakeEntitlementRepository,
            calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(),
            createReviewInsightUseCase = CreateReviewInsightUseCase(CalculatePlanPreviewUseCase()),
            resolvePricingStrategyUseCase = ResolvePricingStrategyUseCase(),
            getReviewHistoryUseCase = mockGetReviewHistoryUseCase,
            updateGoalProgressUseCase = mockUpdateGoalProgressUseCase,
            getPerformanceStatsUseCase = GetPerformanceStatsUseCase(fakePlannerRepository),
            getCoachingInsightsUseCase = GetCoachingInsightsUseCase(CalculatePlanPreviewUseCase())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveReview successfully saves review and updates state`() = runTest {
        // Given a plan exists
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            nextBiweeklyPayday = null,
            preset = AllocationPreset.BALANCED
        )
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.saveGoal(Goal(name = "Savings", targetAmount = BigDecimal("500")))
        
        // Wait for View model flow combination
        advanceUntilIdle()

        // When user enters valid review fields
        viewModel.onFlexibleSpendChanged("200")
        viewModel.onGoalContributionChanged("100")
        viewModel.saveReview()
        
        advanceUntilIdle()

        // Then repository should have the newly saved review
        val savedReview = fakePlannerRepository.observeLatestReview().value
        assertTrue(savedReview != null)
        assertEquals(BigDecimal("200"), savedReview?.actualFlexibleSpend)
        assertEquals(BigDecimal("100"), savedReview?.actualGoalContribution)
    }

    @Test
    fun `saveReview with empty fields sets error state`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakePlannerRepository.savePlan(currentPlan)
        advanceUntilIdle()

        // When user tries saving with invalid fields
        viewModel.onFlexibleSpendChanged("")
        viewModel.onGoalContributionChanged("xyz")
        viewModel.saveReview()

        // Then error state is mapped
        val error = viewModel.state.value.reviewCard.error
        assertTrue(error != null)
        assertEquals("home_review_error_invalid_amounts", error)
    }
}

class FakePlannerRepository : PlannerRepository {
    private val planFlow = MutableStateFlow<SalaryPlan?>(null)
    private val rulesFlow = MutableStateFlow<List<PaydayRule>>(emptyList())
    private val goalsFlow = MutableStateFlow<List<Goal>>(emptyList())
    private val reviewFlow = MutableStateFlow<ManualReview?>(null)
    private val reviewsFlow = MutableStateFlow<List<ManualReview>>(emptyList())
    private val reminderFlow = MutableStateFlow(ReminderConfiguration(false, 9, 0, ReminderCadence.PAYDAY))
    private val automationFlow = MutableStateFlow(false)
    private var isOnboardingCompletedState = false

    override fun observePlan(): MutableStateFlow<SalaryPlan?> = planFlow
    override suspend fun savePlan(plan: SalaryPlan) { planFlow.emit(plan) }

    override fun observeRules(): Flow<List<PaydayRule>> = rulesFlow.asStateFlow()
    override fun loadRules(): List<PaydayRule> = rulesFlow.value
    override suspend fun saveRule(rule: PaydayRule) {
        rulesFlow.emit(rulesFlow.value.filterNot { it.id == rule.id } + rule)
    }
    override suspend fun deleteRule(ruleId: String) {
        rulesFlow.emit(rulesFlow.value.filterNot { it.id == ruleId })
    }
    
    override fun observeGoals(): Flow<List<Goal>> = goalsFlow.asStateFlow()
    override suspend fun saveGoal(goal: Goal) { 
        val current = goalsFlow.value.toMutableList()
        current.add(goal)
        goalsFlow.emit(current)
    }
    override fun loadGoals(): List<Goal> = goalsFlow.value
    override suspend fun deleteGoal(goalId: String) {
        goalsFlow.emit(goalsFlow.value.filter { it.id != goalId })
    }

    override fun observeLatestReview(): MutableStateFlow<ManualReview?> = reviewFlow
    override suspend fun saveReview(review: ManualReview) { 
        reviewFlow.emit(review) 
        val current = reviewsFlow.value.toMutableList()
        current.add(review)
        reviewsFlow.emit(current)
    }
    override fun loadLatestReview(): ManualReview? = reviewFlow.value
    override fun observeReviews(): Flow<List<ManualReview>> = reviewsFlow.asStateFlow()
    
    override fun observeReminderConfiguration(): MutableStateFlow<ReminderConfiguration> = reminderFlow
    override suspend fun saveReminderConfiguration(config: ReminderConfiguration) { reminderFlow.emit(config) }
    override fun loadReminderConfiguration(): ReminderConfiguration = reminderFlow.value
    
    override fun observeAutomationEnabled(): Flow<Boolean> = automationFlow.asStateFlow()
    override suspend fun saveAutomationEnabled(enabled: Boolean) { automationFlow.emit(enabled) }

    override fun isOnboardingCompleted(): Boolean = isOnboardingCompletedState
    override suspend fun setOnboardingCompleted(completed: Boolean) { isOnboardingCompletedState = completed }
    override fun loadPlan(): pt.ms.myshare.domain.model.SalaryPlan? = planFlow.value
    override suspend fun clearPlan() { planFlow.emit(null) }
    override suspend fun syncFromFirestore() {}
}

class TestFakeEntitlementRepository : EntitlementRepository {
    private val _isPro = MutableStateFlow(false)
    override val isPro = _isPro.asStateFlow()
    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    override val availableProducts = _availableProducts.asStateFlow()

    override suspend fun checkActiveEntitlement() {}
    override suspend fun purchasePlan(activity: android.app.Activity, product: StoreProduct) {}
    suspend fun setPro(value: Boolean) { _isPro.emit(value) }
    override suspend fun restorePurchases() {}
}
