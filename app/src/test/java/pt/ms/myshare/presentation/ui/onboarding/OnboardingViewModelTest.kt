package pt.ms.myshare.presentation.ui.onboarding

import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val plannerRepository: PlannerRepository = mockk(relaxed = true)
    private val entitlementRepository: EntitlementRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase = mockk()
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase = mockk()
    private val workManager: WorkManager = mockk(relaxed = true)

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { plannerRepository.isOnboardingCompleted() } returns false
        val pricingStrategy = PricingStrategy(
            marketCluster = "US",
            monthlyLabel = "$4.99/mo",
            annualLabel = "$39.99/yr",
            heroPlan = BillingPlan.ANNUAL,
            trialDays = 7,
            paywallHeadline = "Unlock everything",
            paywallSubhead = "Try it free"
        )
        every { resolvePricingStrategyUseCase.execute(any()) } returns pricingStrategy
        every { entitlementRepository.isPro } returns MutableStateFlow(false)

        viewModel = OnboardingViewModel(
            plannerRepository,
            entitlementRepository,
            authRepository,
            calculatePlanPreviewUseCase,
            resolvePricingStrategyUseCase,
            workManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `buildPreview relies on concrete allocations and requires income`() = runTest {
        // Without setting income
        var built = viewModel.buildPreview()
        assertFalse(built)

        // Set income
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        
        val previewResult = PlanPreview(
            incomePerPayday = BigDecimal("1000"),
            fixedCostsPerPayday = BigDecimal("400"),
            flexibleSpendPerPayday = BigDecimal("300"),
            savingsPerPayday = BigDecimal("200"),
            investingPerPayday = BigDecimal("100"),
            cryptoPerPayday = BigDecimal("0"),
            debtPerPayday = BigDecimal("0"),
            weeklyFlexibleSpend = BigDecimal("75"),
            monthlyGoalContribution = BigDecimal("200"),
            nextPayday = LocalDate.now(),
            goalTargetDate = null,
            summary = "Preview snippet"
        )
        every { calculatePlanPreviewUseCase.execute(any()) } returns previewResult
        
        viewModel.setFixedCostsAndBuild(BigDecimal("400"), AllocationPreset.BALANCED)
        viewModel.setAllocationsAndBuild(
            flexibleSpend = BigDecimal("300"),
            savings = BigDecimal("200"),
            investing = BigDecimal("100"),
            crypto = BigDecimal("0")
        )
        advanceUntilIdle()

        // Verify state is populated
        val state = viewModel.uiState.value
        assertEquals(BigDecimal("300"), state.allocatedFlexibleSpend)
        assertNotNull(state.planPreview)
        coVerify { plannerRepository.savePlan(any()) }
    }

    @Test
    fun `completeOnboarding sets error if no plan exists`() = runTest {
        // Given no plan exists in repository
        every { plannerRepository.loadPlan() } returns null

        // When completeOnboarding called
        viewModel.completeOnboarding()
        advanceUntilIdle()

        // Then onboardingCompleted is false and error string is emitted
        val state = viewModel.uiState.value
        assertFalse(state.onboardingCompleted)
        assertEquals("Cannot complete onboarding without a valid plan.", state.error)
    }

    @Test
    fun `completeOnboarding succeeds if plan exists`() = runTest {
        // Given a plan exists in repository
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            preset = AllocationPreset.BALANCED,
            goalName = "Goal",
            goalAmount = BigDecimal("500")
        )
        every { plannerRepository.loadPlan() } returns plan

        // When completeOnboarding called
        viewModel.completeOnboarding()
        advanceUntilIdle()

        // Then onboardingCompleted is true
        val state = viewModel.uiState.value
        assertTrue(state.onboardingCompleted)
        coVerify { plannerRepository.setOnboardingCompleted(true) }
    }
}
