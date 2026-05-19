package pt.ms.myshare.presentation.ui.onboarding

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.model.User
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.TestUserPreferencesRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.presentation.ui.localization.UserLocaleManager
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val plannerRepository: PlannerRepository = mockk(relaxed = true)
    private val entitlementRepository: EntitlementRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase = mockk()
    private val resolveAllocationStrategyRulesUseCase = ResolveAllocationStrategyRulesUseCase()
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase = mockk()
    private val reminderWorkScheduler: ReminderWorkScheduler = mockk(relaxed = true)
    private val userLocaleManager: UserLocaleManager = mockk(relaxed = true)
    private val onboardingAnalyticsLogger: OnboardingAnalyticsLogger = mockk(relaxed = true)
    private lateinit var isProFlow: MutableStateFlow<Boolean>
    private lateinit var availableProductsFlow: MutableStateFlow<List<StoreProduct>>
    private lateinit var purchaseEventsFlow: MutableSharedFlow<BillingPurchaseEvent>
    private lateinit var currentUserFlow: MutableStateFlow<User?>

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
        isProFlow = MutableStateFlow(false)
        availableProductsFlow = MutableStateFlow(emptyList())
        purchaseEventsFlow = MutableSharedFlow(extraBufferCapacity = 1)
        currentUserFlow = MutableStateFlow(null)
        every { entitlementRepository.isPro } returns isProFlow
        every { entitlementRepository.availableProducts } returns availableProductsFlow
        every { entitlementRepository.purchaseEvents } returns purchaseEventsFlow
        coEvery { entitlementRepository.purchasePlan(any(), any()) } returns BillingFlowLaunchResult.Launched
        every { authRepository.currentUser } returns currentUserFlow

        viewModel = OnboardingViewModel(
            plannerRepository,
            entitlementRepository,
            authRepository,
            TestUserPreferencesRepository(),
            calculatePlanPreviewUseCase,
            resolveAllocationStrategyRulesUseCase,
            resolvePricingStrategyUseCase,
            reminderWorkScheduler,
            userLocaleManager,
            onboardingAnalyticsLogger
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
            crypto = BigDecimal("0"),
            isPercentage = false
        )
        advanceUntilIdle()

        // Verify state is populated
        val state = viewModel.uiState.value
        assertEquals(BigDecimal("300"), state.allocatedFlexibleSpend)
        assertFalse(state.allocationIsPercentage)
        assertNotNull(state.planPreview)
        coVerify { plannerRepository.savePlan(any()) }
    }

    @Test
    fun `setAllocationsAndBuild saves percentage onboarding rules when percentage selected`() = runTest {
        val savedPlans = mutableListOf<SalaryPlan>()
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult()
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        viewModel.setFixedCostsAndBuild(BigDecimal("400"), AllocationPreset.BALANCED)

        viewModel.setAllocationsAndBuild(
            flexibleSpend = BigDecimal("50"),
            savings = BigDecimal("30"),
            investing = BigDecimal("20"),
            crypto = BigDecimal.ZERO,
            isPercentage = true
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) { plannerRepository.savePlan(capture(savedPlans)) }
        val rules = savedPlans.last().rules
        assertEquals(BigDecimal("30"), rules.first { it.name == "Savings" }.amount)
        assertTrue(rules.first { it.name == "Savings" }.isPercentage)
        assertEquals(BigDecimal("20"), rules.first { it.name == "Investing" }.amount)
        assertTrue(rules.first { it.name == "Investing" }.isPercentage)
    }

    @Test
    fun `setAllocationsAndBuild saves fixed onboarding rules when fixed amount selected`() = runTest {
        val savedPlans = mutableListOf<SalaryPlan>()
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult()
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        viewModel.setFixedCostsAndBuild(BigDecimal("400"), AllocationPreset.BALANCED)

        viewModel.setAllocationsAndBuild(
            flexibleSpend = BigDecimal("300"),
            savings = BigDecimal("200"),
            investing = BigDecimal("100"),
            crypto = BigDecimal.ZERO,
            isPercentage = false
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) { plannerRepository.savePlan(capture(savedPlans)) }
        val rules = savedPlans.last().rules
        assertEquals(BigDecimal("200"), rules.first { it.name == "Savings" }.amount)
        assertFalse(rules.first { it.name == "Savings" }.isPercentage)
        assertEquals(BigDecimal("100"), rules.first { it.name == "Investing" }.amount)
        assertFalse(rules.first { it.name == "Investing" }.isPercentage)
    }

    @Test
    fun `setAllocationsAndBuild supports no savings and debt allocation`() = runTest {
        val savedPlans = mutableListOf<SalaryPlan>()
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult()
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        viewModel.setFixedCostsAndBuild(
            monthlyFixedCosts = BigDecimal("400"),
            preset = AllocationPreset.BALANCED,
            strategy = AllocationStrategy.DEBT_FIRST
        )

        viewModel.setAllocationsAndBuild(
            flexibleSpend = BigDecimal("50"),
            savings = BigDecimal.ZERO,
            investing = BigDecimal("10"),
            crypto = BigDecimal.ZERO,
            debt = BigDecimal("40"),
            isPercentage = true
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) { plannerRepository.savePlan(capture(savedPlans)) }
        val plan = savedPlans.last()
        assertEquals(AllocationStrategy.DEBT_FIRST, plan.strategy)
        assertTrue(plan.rules.none { it.type == PaydayRuleType.SAVINGS })
        assertEquals(BigDecimal("40"), plan.rules.first { it.type == PaydayRuleType.DEBT }.amount)
    }

    @Test
    fun `setFixedCostsAndBuild persists custom strategy name on saved plan`() = runTest {
        val savedPlans = mutableListOf<SalaryPlan>()
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult()
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")

        viewModel.setFixedCostsAndBuild(
            monthlyFixedCosts = BigDecimal("400"),
            preset = AllocationPreset.BALANCED,
            strategy = AllocationStrategy.CUSTOM,
            customStrategyName = "Travel reset"
        )
        advanceUntilIdle()

        coVerify(atLeast = 1) { plannerRepository.savePlan(capture(savedPlans)) }
        val plan = savedPlans.last()
        assertEquals(AllocationStrategy.CUSTOM, plan.strategy)
        assertEquals("Travel reset", plan.customStrategyName)
    }

    @Test
    fun `setFixedCostsAndBuild persists default strategy rules before allocation tuning`() = runTest {
        val savedPlans = mutableListOf<SalaryPlan>()
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns previewResult()
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")

        val built = viewModel.setFixedCostsAndBuild(
            monthlyFixedCosts = BigDecimal("400"),
            preset = AllocationPreset.BALANCED,
            strategy = AllocationStrategy.BALANCED_SAVINGS
        )
        advanceUntilIdle()

        assertTrue(built)
        coVerify(atLeast = 1) { plannerRepository.savePlan(capture(savedPlans)) }
        val plan = savedPlans.last()
        assertTrue(plan.rules.isNotEmpty())
        assertTrue(plan.rules.any { it.type == PaydayRuleType.SAVINGS })
        assertTrue(plan.rules.all { it.isPercentage })
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
    fun `completeOnboarding enables premium watch for active entitlement`() = runTest {
        viewModel.setSalaryDetails(BigDecimal("1000"), PayFrequency.MONTHLY, 1, "")
        every { calculatePlanPreviewUseCase.execute(any(), any()) } returns mockk(relaxed = true)
        viewModel.setFixedCostsAndBuild(BigDecimal("400"), AllocationPreset.BALANCED)
        viewModel.skipReminderConfiguration()
        isProFlow.value = true
        advanceUntilIdle()

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
        coVerify { plannerRepository.saveAutomationEnabled(true) }
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
        coVerify { plannerRepository.setOnboardingCompleted(true) }
    }

    @Test
    fun `analytics logger receives setup and activation events`() = runTest {
        viewModel.logSetupStepViewed(OnboardingRoute.GoalPicker, 1)
        viewModel.logSetupStepCompleted(OnboardingRoute.GoalPicker, 1)
        viewModel.logActivationReached()
        viewModel.logActivationReached()

        verify { onboardingAnalyticsLogger.logStepViewed("goal_picker", 1, OnboardingViewModel.SETUP_STEP_TOTAL, any(), any()) }
        verify { onboardingAnalyticsLogger.logStepCompleted("goal_picker", 1, OnboardingViewModel.SETUP_STEP_TOTAL, any(), any()) }
        verify(exactly = 1) { onboardingAnalyticsLogger.logActivationReached(any(), any()) }
    }

    @Test
    fun `purchasePremium uses real StoreProduct from availableProducts`() = runTest {
        val monthlyProduct = StoreProduct(
            productId = "myshare_monthly",
            name = "Monthly",
            description = "Monthly premium",
            price = "€4.99/mo",
            basePlanId = "monthly-base",
            offerToken = "token-monthly-123"
        )
        availableProductsFlow.value = listOf(monthlyProduct)
        viewModel.setSelectedBillingPlan(BillingPlan.MONTHLY)
        val activity = mockk<android.app.Activity>(relaxed = true)
        viewModel.purchasePremium(activity)
        advanceUntilIdle()
        coVerify { entitlementRepository.purchasePlan(activity, monthlyProduct) }
        assertEquals("paywall_billing_handoff", viewModel.uiState.value.billingMessage)
    }

    @Test
    fun `purchasePremium shows checkout failure when Play rejects billing launch`() = runTest {
        val monthlyProduct = StoreProduct(
            productId = "myshare_monthly",
            name = "Monthly",
            description = "Monthly premium",
            price = "€4.99/mo",
            basePlanId = "monthly-base",
            offerToken = "token-monthly-123"
        )
        availableProductsFlow.value = listOf(monthlyProduct)
        coEvery { entitlementRepository.purchasePlan(any(), any()) } returns BillingFlowLaunchResult.Failed(
            responseCode = 5,
            debugMessage = "Developer error"
        )

        viewModel.setSelectedBillingPlan(BillingPlan.MONTHLY)
        viewModel.purchasePremium(mockk(relaxed = true))
        advanceUntilIdle()

        assertEquals("paywall_billing_checkout_failed", viewModel.uiState.value.billingMessage)
        assertFalse(viewModel.uiState.value.isBillingActionInProgress)
    }

    @Test
    fun `purchasePremium shows billing message when product not yet loaded`() = runTest {
        availableProductsFlow.value = emptyList()
        val activity = mockk<android.app.Activity>(relaxed = true)
        viewModel.purchasePremium(activity)
        advanceUntilIdle()
        assertEquals("paywall_billing_products_unavailable", viewModel.uiState.value.billingMessage)
        assertFalse(viewModel.uiState.value.isBillingActionInProgress)
        coVerify(exactly = 0) { entitlementRepository.purchasePlan(any(), any()) }
    }

    @Test
    fun `continueLocally proceeds without anonymous Firebase sign-in`() = runTest {
        var completed = false

        viewModel.continueLocally { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify(exactly = 0) { authRepository.signInAnonymously() }
        coVerify(exactly = 0) { authRepository.signInWithGoogle(any()) }
    }

    @Test
    fun `signInWithGoogle syncs local planner state after account attachment`() = runTest {
        coEvery { authRepository.signInWithGoogle("google-token") } returns Result.success(
            User(email = "user@example.com", isAnonymous = false)
        )
        var completed = false

        viewModel.signInWithGoogle("google-token") { completed = true }
        advanceUntilIdle()

        coVerify(timeout = 1_000) { plannerRepository.syncLocalStateIfAuthenticated() }
        assertTrue(completed)
    }

    @Test
    fun `signInWithGoogle clears signup loading when authentication fails`() = runTest {
        coEvery { authRepository.signInWithGoogle("google-token") } returns Result.failure(
            IllegalStateException("auth failed")
        )

        viewModel.signInWithGoogle("google-token") {}
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSignupActionInProgress)
        assertEquals("error_authentication_failed", viewModel.uiState.value.error)
    }

    @Test
    fun `premium anonymous user is prompted to secure access`() = runTest {
        currentUserFlow.value = User(isAnonymous = true)
        isProFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shouldSecurePremiumAccess)
    }

    @Test
    fun `completed purchase prompts anonymous user to secure access before entitlement refresh`() = runTest {
        currentUserFlow.value = User(isAnonymous = true)
        advanceUntilIdle()

        purchaseEventsFlow.emit(BillingPurchaseEvent.Completed)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shouldSecurePremiumAccess)
        assertEquals("paywall_billing_completed", viewModel.uiState.value.billingMessage)
        coVerify { plannerRepository.saveAutomationEnabled(true) }
    }

    @Test
    fun `dismissed secure access prompt stays hidden during entitlement refresh`() = runTest {
        currentUserFlow.value = User(isAnonymous = true)
        isProFlow.value = true
        advanceUntilIdle()

        viewModel.dismissSecurePremiumAccessPrompt()
        isProFlow.value = false
        isProFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.shouldSecurePremiumAccess)
    }

    @Test
    fun `premium linked user is not prompted to secure access`() = runTest {
        currentUserFlow.value = User(email = "user@example.com", isAnonymous = false)
        isProFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.shouldSecurePremiumAccess)
    }

    @Test
    fun `connectGoogleAccount clears secure prompt after successful link`() = runTest {
        currentUserFlow.value = User(isAnonymous = true)
        isProFlow.value = true
        coEvery { authRepository.connectGoogleAccount("google-token") } returns Result.success(
            User(email = "user@example.com", isAnonymous = false)
        )
        advanceUntilIdle()

        viewModel.connectGoogleAccount("google-token")
        advanceUntilIdle()

        coVerify(timeout = 1_000) { plannerRepository.syncLocalStateIfAuthenticated() }
        assertFalse(viewModel.uiState.value.shouldSecurePremiumAccess)
        assertEquals("home_more_account_connect_google_success", viewModel.uiState.value.googleConnectionMessage)
    }

    private fun previewResult(): PlanPreview = PlanPreview(
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
}
