package pt.ms.myshare.presentation.ui.onboarding

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
    private val reminderWorkScheduler: ReminderWorkScheduler = mockk(relaxed = true)

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
            reminderWorkScheduler
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
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult
        
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
    fun `setFixedCostsAndBuild rejects fixed costs greater than payday income`() = runTest {
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")

        val built = viewModel.setFixedCostsAndBuild(BigDecimal("1200"), AllocationPreset.BALANCED)
        advanceUntilIdle()

        assertFalse(built)
        assertEquals(BigDecimal("1200"), viewModel.uiState.value.monthlyFixedCosts)
        assertEquals(
            OnboardingViewModel.FIXED_COSTS_EXCEED_INCOME_ERROR,
            viewModel.uiState.value.error
        )
        coVerify(exactly = 0) { plannerRepository.savePlan(any()) }
    }

    @Test
    fun `completeOnboarding fails if guards not met`() = runTest {
        // Initially guards are false
        viewModel.completeOnboarding()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.onboardingCompleted)
        assertEquals("onboarding_error_plan_required", state.error)
    }

    @Test
    fun `completeOnboarding succeeds only if all guards met`() = runTest {
        // Set up valid preview to satisfy planSaved
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns mockk(relaxed = true)
        viewModel.setFixedCostsAndBuild(BigDecimal("400"), AllocationPreset.BALANCED)
        advanceUntilIdle()
        
        // Reminder not yet handled
        viewModel.completeOnboarding()
        assertEquals("onboarding_error_reminder_required", viewModel.uiState.value.error)
        
        // Handle reminder
        viewModel.skipReminderConfiguration()
        advanceUntilIdle()
        
        // Bank sync not handled
        viewModel.completeOnboarding()
        assertEquals("onboarding_error_bank_sync_required", viewModel.uiState.value.error)
        
        // Handle bank sync
        viewModel.setBankSyncHandled()
        
        // Now it should succeed if plan exists in repo
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            preset = AllocationPreset.BALANCED
        )
        every { plannerRepository.loadPlan() } returns plan

        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.onboardingCompleted)
    }

    @Test
    fun `skipToHomeWithDefaultPlan sets defaults and completes`() = runTest {
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns mockk(relaxed = true)
        
        viewModel.skipToHomeWithDefaultPlan()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.onboardingCompleted)
        assertEquals(BigDecimal("1500"), state.netIncomePerPayday)
        assertTrue(state.planSaved)
        assertTrue(state.reminderSkipped)
        assertTrue(state.bankSyncHandled)
        coVerify { plannerRepository.setOnboardingCompleted(true) }
    }

    @Test
    fun `purchasePremium uses real StoreProduct from availableProducts`() = runTest {
        val monthlyProduct = pt.ms.myshare.domain.model.StoreProduct(
            productId = "myshare_monthly",
            name = "Monthly",
            description = "Monthly premium",
            price = "€4.99/mo",
            basePlanId = "monthly-base",
            offerToken = "token-monthly-123"
        )
        every { entitlementRepository.availableProducts } returns MutableStateFlow(listOf(monthlyProduct))
        viewModel.setSelectedBillingPlan(BillingPlan.MONTHLY)
        val activity = mockk<android.app.Activity>(relaxed = true)
        viewModel.purchasePremium(activity)
        advanceUntilIdle()
        coVerify { entitlementRepository.purchasePlan(activity, monthlyProduct) }
    }

    @Test
    fun `purchasePremium shows billing message when product not yet loaded`() = runTest {
        every { entitlementRepository.availableProducts } returns MutableStateFlow(emptyList())
        val activity = mockk<android.app.Activity>(relaxed = true)
        viewModel.purchasePremium(activity)
        advanceUntilIdle()
        assertEquals("paywall_billing_products_unavailable", viewModel.uiState.value.billingMessage)
        assertFalse(viewModel.uiState.value.isBillingActionInProgress)
        coVerify(exactly = 0) { entitlementRepository.purchasePlan(any(), any()) }
    }
}
