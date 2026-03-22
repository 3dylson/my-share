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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var fakePlannerRepository: FakePlannerRepository
    private lateinit var fakeEntitlementRepository: TestFakeEntitlementRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePlannerRepository = FakePlannerRepository()
        fakeEntitlementRepository = TestFakeEntitlementRepository()
        
        viewModel = HomeViewModel(
            plannerRepository = fakePlannerRepository,
            entitlementRepository = fakeEntitlementRepository,
            calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(),
            createReviewInsightUseCase = CreateReviewInsightUseCase(),
            resolvePricingStrategyUseCase = ResolvePricingStrategyUseCase()
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
            focus = PlanningFocus.BALANCED,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            nextBiweeklyPayday = null,
            preset = AllocationPreset.BALANCED,
            goalName = "Savings",
            goalAmount = BigDecimal("500")
        )
        fakePlannerRepository.savePlan(currentPlan)
        
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
            focus = PlanningFocus.BALANCED,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            nextBiweeklyPayday = null,
            preset = AllocationPreset.BALANCED,
            goalName = "Savings",
            goalAmount = BigDecimal("500")
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
        assertEquals("Enter valid amounts for both review fields.", error)
    }
}

class FakePlannerRepository : PlannerRepository {
    private val planFlow = MutableStateFlow<SalaryPlan?>(null)
    private val reviewFlow = MutableStateFlow<ManualReview?>(null)
    private val reminderFlow = MutableStateFlow(ReminderConfiguration(false, 9, 0, ReminderCadence.PAYDAYS))
    private var isOnboardingCompletedState = false

    override fun observePlan(): MutableStateFlow<SalaryPlan?> = planFlow
    override suspend fun savePlan(plan: SalaryPlan) { planFlow.emit(plan) }
    
    override fun observeLatestReview(): MutableStateFlow<ManualReview?> = reviewFlow
    override suspend fun saveReview(review: ManualReview) { reviewFlow.emit(review) }
    override suspend fun getReviewHistory(limit: Int): List<ManualReview> = listOfNotNull(reviewFlow.value)
    
    override fun observeReminderConfiguration(): MutableStateFlow<ReminderConfiguration> = reminderFlow
    override suspend fun saveReminderConfiguration(config: ReminderConfiguration) { reminderFlow.emit(config) }
    override suspend fun loadReminderConfiguration(): ReminderConfiguration = reminderFlow.value
    
    override fun isOnboardingCompleted(): Boolean = isOnboardingCompletedState
    override suspend fun setOnboardingCompleted(completed: Boolean) { isOnboardingCompletedState = completed }
    override suspend fun clearAllData() {
        planFlow.emit(null)
        reviewFlow.emit(null)
        isOnboardingCompletedState = false
    }
}

class TestFakeEntitlementRepository : EntitlementRepository {
    private val _isPro = MutableStateFlow(false)
    override val isPro = _isPro.asStateFlow()
    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    override val availableProducts = _availableProducts.asStateFlow()

    override suspend fun checkActiveEntitlement() {}
    override suspend fun purchasePlan(activity: android.app.Activity, product: StoreProduct) {}
    override suspend fun setPro(value: Boolean) { _isPro.emit(value) }
    override suspend fun restorePurchases() {}
}
