package pt.ms.myshare.presentation.ui.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreatePaydayAdjustmentRecommendationUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumCheckInPlanUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumGoalPaydaySplitUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumRulePaydayMixUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.EnforcePremiumDowngradeUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import java.math.BigDecimal

import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.use_case.AdjustGoalProgressForReviewCorrectionUseCase
import pt.ms.myshare.domain.use_case.GetReviewHistoryUseCase
import pt.ms.myshare.domain.use_case.UpdateGoalProgressUseCase
import pt.ms.myshare.domain.use_case.GetPerformanceStatsUseCase
import pt.ms.myshare.domain.use_case.GetCoachingInsightsUseCase
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.User
import pt.ms.myshare.TestUserPreferencesRepository
import pt.ms.myshare.presentation.ui.localization.UserLocaleManager
import pt.ms.myshare.presentation.ui.onboarding.ReminderWorkScheduler
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var fakePlannerRepository: FakePlannerRepository
    private lateinit var fakeEntitlementRepository: TestFakeEntitlementRepository
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)
    private val mockGetReviewHistoryUseCase = mockk<GetReviewHistoryUseCase>(relaxed = true)
    private val mockUpdateGoalProgressUseCase = mockk<UpdateGoalProgressUseCase>(relaxed = true)
    private val mockReminderWorkScheduler = mockk<ReminderWorkScheduler>(relaxed = true)
    private val mockUserLocaleManager = mockk<UserLocaleManager>(relaxed = true)
    private lateinit var currentUserFlow: MutableStateFlow<User?>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePlannerRepository = FakePlannerRepository()
        fakeEntitlementRepository = TestFakeEntitlementRepository()
        val calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase())
        currentUserFlow = MutableStateFlow(null)
        
        every { mockAuthRepository.currentUser } returns currentUserFlow
        every { mockGetReviewHistoryUseCase.execute() } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            plannerRepository = fakePlannerRepository,
            authRepository = mockAuthRepository,
            entitlementRepository = fakeEntitlementRepository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            calculatePlanPreviewUseCase = calculatePlanPreviewUseCase,
            createPaydayAdjustmentRecommendationUseCase = CreatePaydayAdjustmentRecommendationUseCase(
                calculatePlanPreviewUseCase,
                ResolveAllocationStrategyRulesUseCase()
            ),
            createPremiumCheckInPlanUseCase = CreatePremiumCheckInPlanUseCase(),
            createPremiumGoalPaydaySplitUseCase = CreatePremiumGoalPaydaySplitUseCase(),
            createPremiumRulePaydayMixUseCase = CreatePremiumRulePaydayMixUseCase(),
            createReviewInsightUseCase = CreateReviewInsightUseCase(calculatePlanPreviewUseCase),
            enforcePremiumDowngradeUseCase = EnforcePremiumDowngradeUseCase(fakePlannerRepository),
            resolvePricingStrategyUseCase = ResolvePricingStrategyUseCase(),
            getReviewHistoryUseCase = mockGetReviewHistoryUseCase,
            updateGoalProgressUseCase = mockUpdateGoalProgressUseCase,
            adjustGoalProgressForReviewCorrectionUseCase = AdjustGoalProgressForReviewCorrectionUseCase(
                fakePlannerRepository,
                fakeEntitlementRepository
            ),
            getPerformanceStatsUseCase = GetPerformanceStatsUseCase(fakePlannerRepository),
            getCoachingInsightsUseCase = GetCoachingInsightsUseCase(calculatePlanPreviewUseCase),
            reminderWorkScheduler = mockReminderWorkScheduler,
            userLocaleManager = mockUserLocaleManager
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

    @Test
    fun `updateReview replaces review and adjusts goal progress delta`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.saveGoal(
            Goal(id = "goal-1", name = "Savings", targetAmount = BigDecimal("500"), currentProgress = BigDecimal("100"))
        )
        fakePlannerRepository.saveReview(
            ManualReview(
                id = "review-1",
                actualFlexibleSpend = BigDecimal("200"),
                actualGoalContribution = BigDecimal("100"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("100")
            )
        )
        advanceUntilIdle()

        val accepted = viewModel.updateReview("review-1", "180", "40")
        advanceUntilIdle()

        assertTrue(accepted)
        assertEquals(BigDecimal("180"), fakePlannerRepository.reviews().first().actualFlexibleSpend)
        assertEquals(BigDecimal("40"), fakePlannerRepository.reviews().first().actualGoalContribution)
        assertEquals(BigDecimal("40"), fakePlannerRepository.loadGoals().first().currentProgress)
    }

    @Test
    fun `deleteReview removes review and reverses goal contribution`() = runTest {
        fakePlannerRepository.saveGoal(
            Goal(id = "goal-1", name = "Savings", targetAmount = BigDecimal("500"), currentProgress = BigDecimal("100"))
        )
        fakePlannerRepository.saveReview(
            ManualReview(
                id = "review-1",
                actualFlexibleSpend = BigDecimal("200"),
                actualGoalContribution = BigDecimal("100"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("100")
            )
        )
        advanceUntilIdle()

        viewModel.deleteReview("review-1")
        advanceUntilIdle()

        assertTrue(fakePlannerRepository.reviews().isEmpty())
        assertEquals(BigDecimal.ZERO, fakePlannerRepository.loadGoals().first().currentProgress)
    }

    @Test
    fun `billing unavailable feedback survives planner refresh`() = runTest {
        advanceUntilIdle()

        viewModel.chooseBillingPlan(BillingPlan.MONTHLY)
        viewModel.unlockPremium(mockk(relaxed = true), "test_gate")
        advanceUntilIdle()

        assertEquals(
            "paywall_billing_products_unavailable",
            viewModel.state.value.moreCard.billingMessage
        )
        assertEquals("more_error_products_not_loaded", viewModel.state.value.moreCard.error)

        fakePlannerRepository.saveReminderConfiguration(
            ReminderConfiguration(
                enabled = true,
                hourOfDay = 10,
                minute = 30,
                cadence = ReminderCadence.WEEKLY_REVIEW
            )
        )
        advanceUntilIdle()

        assertEquals(
            "paywall_billing_products_unavailable",
            viewModel.state.value.moreCard.billingMessage
        )
        assertEquals("more_error_products_not_loaded", viewModel.state.value.moreCard.error)

        fakeEntitlementRepository.setProducts(
            listOf(
                StoreProduct(
                    productId = PremiumSubscriptionProducts.MONTHLY_ID,
                    name = "Monthly",
                    description = "Monthly premium",
                    price = "€4.99",
                    basePlanId = "monthly",
                    offerToken = "monthly-token"
                )
            )
        )
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.moreCard.billingMessage)
        assertEquals(null, viewModel.state.value.moreCard.error)
    }

    @Test
    fun `unlockPremium shows checkout failure when Play rejects billing launch`() = runTest {
        fakeEntitlementRepository.setProducts(
            listOf(
                StoreProduct(
                    productId = PremiumSubscriptionProducts.MONTHLY_ID,
                    name = "Monthly",
                    description = "Monthly premium",
                    price = "€4.99",
                    basePlanId = "monthly",
                    offerToken = "monthly-token"
                )
            )
        )
        fakeEntitlementRepository.purchaseResult = BillingFlowLaunchResult.Failed(
            responseCode = 5,
            debugMessage = "Developer error"
        )
        advanceUntilIdle()

        viewModel.chooseBillingPlan(BillingPlan.MONTHLY)
        viewModel.unlockPremium(mockk(relaxed = true), "test_gate")
        advanceUntilIdle()

        assertEquals(
            "paywall_billing_checkout_failed",
            viewModel.state.value.moreCard.billingMessage
        )
        assertEquals(false, viewModel.state.value.moreCard.isBillingActionInProgress)
    }

    @Test
    fun `purchase canceled event replaces handoff feedback`() = runTest {
        fakeEntitlementRepository.setProducts(
            listOf(
                StoreProduct(
                    productId = PremiumSubscriptionProducts.MONTHLY_ID,
                    name = "Monthly",
                    description = "Monthly premium",
                    price = "€4.99",
                    basePlanId = "monthly",
                    offerToken = "monthly-token"
                )
            )
        )
        advanceUntilIdle()

        viewModel.chooseBillingPlan(BillingPlan.MONTHLY)
        viewModel.unlockPremium(mockk(relaxed = true), "test_gate")
        advanceUntilIdle()

        assertEquals(
            "paywall_billing_handoff",
            viewModel.state.value.moreCard.billingMessage
        )

        fakeEntitlementRepository.emitPurchaseEvent(BillingPurchaseEvent.Canceled)
        advanceUntilIdle()

        assertEquals(
            "paywall_billing_canceled",
            viewModel.state.value.moreCard.billingMessage
        )
    }

    @Test
    fun `purchase completed asks local user to connect Google account`() = runTest {
        advanceUntilIdle()

        fakeEntitlementRepository.emitPurchaseEvent(BillingPurchaseEvent.Completed)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.moreCard.showPremiumAccountPrompt)
        assertEquals(
            "paywall_billing_completed",
            viewModel.state.value.moreCard.billingMessage
        )
    }

    @Test
    fun `dismissPremiumAccountPrompt hides post purchase account prompt`() = runTest {
        advanceUntilIdle()
        fakeEntitlementRepository.emitPurchaseEvent(BillingPurchaseEvent.Completed)
        advanceUntilIdle()

        viewModel.dismissPremiumAccountPrompt()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.moreCard.showPremiumAccountPrompt)
    }

    @Test
    fun `saveReview emits one shot review saved feedback`() = runTest {
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

        viewModel.onFlexibleSpendChanged("200")
        viewModel.onGoalContributionChanged("100")
        viewModel.saveReview()
        advanceUntilIdle()

        val eventId = viewModel.state.value.reviewSavedEventId
        assertTrue(eventId > 0L)

        viewModel.clearReviewSavedFeedback(eventId)
        advanceUntilIdle()

        assertEquals(0L, viewModel.state.value.reviewSavedEventId)
    }

    @Test
    fun `downgrade to free disables premium automation and locks more card`() = runTest {
        fakeEntitlementRepository.setEntitlementState(EntitlementState.PRO)
        fakePlannerRepository.saveAutomationEnabled(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.moreCard.isPremium)
        assertEquals(true, viewModel.state.value.moreCard.automationEnabled)

        fakeEntitlementRepository.setEntitlementState(EntitlementState.FREE)
        advanceUntilIdle()

        assertEquals(false, fakePlannerRepository.automationEnabled())
        assertEquals(false, viewModel.state.value.moreCard.isPremium)
        assertEquals(false, viewModel.state.value.moreCard.automationEnabled)
    }

    @Test
    fun `free user review performance analytics stay locked`() = runTest {
        seedPlanWithPerformanceReviews()
        fakeEntitlementRepository.setEntitlementState(EntitlementState.FREE)
        advanceUntilIdle()

        val stats = viewModel.state.value.performanceStats

        assertEquals(0, stats.healthScore)
        assertEquals(0, stats.currentStreak)
        assertEquals(BigDecimal.ZERO, stats.totalSavings)
        assertTrue(stats.performanceTrend.isEmpty())
        assertEquals(2, stats.totalReviews)
    }

    @Test
    fun `premium user receives review performance analytics`() = runTest {
        seedPlanWithPerformanceReviews()
        fakeEntitlementRepository.setEntitlementState(EntitlementState.PRO)
        advanceUntilIdle()

        val stats = viewModel.state.value.performanceStats

        assertEquals(50, stats.healthScore)
        assertEquals(1, stats.currentStreak)
        assertEquals(BigDecimal("50"), stats.totalSavings)
        assertEquals(2, stats.performanceTrend.size)
        assertEquals(2, stats.totalReviews)
    }

    @Test
    fun `premium user sees next payday split across multiple active goals`() = runTest {
        fakePlannerRepository.savePlan(
            SalaryPlan(
                focus = PlanningFocus.SAVE_WITHOUT_STRESS,
                netIncomePerPayday = BigDecimal("1500"),
                monthlyFixedCosts = BigDecimal("600"),
                payFrequency = PayFrequency.MONTHLY,
                monthlyPayday = 1,
                preset = AllocationPreset.BALANCED
            )
        )
        fakePlannerRepository.saveRule(
            PaydayRule(
                id = "rule-1",
                name = "Savings",
                amount = BigDecimal("300"),
                isPercentage = false,
                type = PaydayRuleType.SAVINGS
            )
        )
        listOf(
            Goal(id = "goal-1", name = "Emergency fund", targetAmount = BigDecimal("3000")),
            Goal(id = "goal-2", name = "Investing base", targetAmount = BigDecimal("2000")),
            Goal(id = "goal-3", name = "Holiday", targetAmount = BigDecimal("1200")),
            Goal(id = "goal-4", name = "New laptop", targetAmount = BigDecimal("1600"))
        ).forEach { goal ->
            fakePlannerRepository.saveGoal(goal)
        }
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.goalPaydaySplit)

        fakeEntitlementRepository.setPro(true)
        advanceUntilIdle()

        val split = viewModel.state.value.goalPaydaySplit
        assertEquals(4, split?.goalCount)
        assertEquals(3, split?.visibleItems?.size)
        assertEquals(1, split?.hiddenGoalCount)
        assertEquals("goal-1", split?.visibleItems?.first()?.goalId)
        assertTrue(split?.totalMoveLabel?.isNotBlank() == true)
    }

    @Test
    fun `premium user sees next payday mix across multiple priority rules`() = runTest {
        fakePlannerRepository.savePlan(
            SalaryPlan(
                focus = PlanningFocus.SAVE_WITHOUT_STRESS,
                netIncomePerPayday = BigDecimal("2000"),
                monthlyFixedCosts = BigDecimal("800"),
                payFrequency = PayFrequency.MONTHLY,
                monthlyPayday = 1,
                preset = AllocationPreset.BALANCED
            )
        )
        listOf(
            PaydayRule(
                id = "rule-1",
                name = "Savings",
                amount = BigDecimal("10"),
                isPercentage = true,
                type = PaydayRuleType.SAVINGS
            ),
            PaydayRule(
                id = "rule-2",
                name = "Investing",
                amount = BigDecimal("5"),
                isPercentage = true,
                type = PaydayRuleType.INVESTING
            ),
            PaydayRule(
                id = "rule-3",
                name = "Debt payoff",
                amount = BigDecimal("100"),
                isPercentage = false,
                type = PaydayRuleType.DEBT
            ),
            PaydayRule(
                id = "rule-4",
                name = "Crypto",
                amount = BigDecimal("2"),
                isPercentage = true,
                type = PaydayRuleType.CRYPTO
            )
        ).forEach { rule ->
            fakePlannerRepository.saveRule(rule)
        }
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.rulePaydayMix)

        fakeEntitlementRepository.setPro(true)
        advanceUntilIdle()

        val mix = viewModel.state.value.rulePaydayMix
        assertEquals(4, mix?.ruleCount)
        assertEquals(3, mix?.visibleItems?.size)
        assertEquals(1, mix?.hiddenRuleCount)
        assertEquals("rule-1", mix?.visibleItems?.first()?.ruleId)
        assertTrue(mix?.totalMoveLabel?.isNotBlank() == true)
    }

    @Test
    fun `premium more card exposes pending smart adjustment`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakeEntitlementRepository.setPro(true)
        fakePlannerRepository.saveAutomationEnabled(true)
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.saveReview(
            ManualReview(
                actualFlexibleSpend = BigDecimal("220"),
                actualGoalContribution = BigDecimal("310"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("300")
            )
        )
        advanceUntilIdle()

        val moreCard = viewModel.state.value.moreCard
        val smartAdjustment = moreCard.smartAdjustment

        assertTrue(moreCard.automationEnabled)
        assertTrue(smartAdjustment.hasPlan)
        assertTrue(smartAdjustment.hasRecommendation)
        assertTrue(smartAdjustment.isApplyable)
        assertEquals(
            PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY,
            smartAdjustment.direction
        )
        assertTrue(smartAdjustment.recommendedFlexibleSpendLabel.isNotBlank())
        assertTrue(smartAdjustment.recommendedPriorityContributionLabel.isNotBlank())
        assertEquals(62, smartAdjustment.confidencePercent)
        assertEquals(1, smartAdjustment.analyzedReviewCount)
    }

    @Test
    fun `premium user sees due check in on payday`() = runTest {
        val today = LocalDate.now()
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.BIWEEKLY,
            nextBiweeklyPayday = today,
            preset = AllocationPreset.BALANCED,
            createdAt = today.minusMonths(1)
        )
        fakeEntitlementRepository.setPro(true)
        fakePlannerRepository.saveAutomationEnabled(true)
        fakePlannerRepository.savePlan(currentPlan)
        advanceUntilIdle()

        val reviewCheckIn = viewModel.state.value.reviewCard.premiumCheckIn
        val moreCheckIn = viewModel.state.value.moreCard.premiumCheckIn

        assertEquals(PremiumCheckInStatus.READY_NOW, reviewCheckIn?.status)
        assertEquals(PremiumCheckInStatus.READY_NOW, moreCheckIn?.status)
        assertTrue(reviewCheckIn?.isDue == true)
        assertTrue(moreCheckIn?.automationEnabled == true)
    }

    @Test
    fun `premium user can apply payday recommendation to real rules`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakeEntitlementRepository.setPro(true)
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.saveReview(
            ManualReview(
                actualFlexibleSpend = BigDecimal("220"),
                actualGoalContribution = BigDecimal("310"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("300")
            )
        )
        advanceUntilIdle()

        assertEquals(
            PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY,
            viewModel.state.value.reviewCard.paydayRecommendation?.direction
        )

        viewModel.applyPaydayRecommendation()
        advanceUntilIdle()

        assertTrue(fakePlannerRepository.loadRules().isNotEmpty())
        assertEquals(
            "home_review_recommendation_applied_feedback",
            viewModel.state.value.reviewCard.recommendationMessageKey
        )
    }

    @Test
    fun `premium user can undo applied payday recommendation`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakeEntitlementRepository.setPro(true)
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.saveReview(
            ManualReview(
                actualFlexibleSpend = BigDecimal("220"),
                actualGoalContribution = BigDecimal("310"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("300")
            )
        )
        advanceUntilIdle()

        assertTrue(viewModel.applyPaydayRecommendation())
        advanceUntilIdle()
        assertTrue(fakePlannerRepository.loadRules().isNotEmpty())

        assertTrue(viewModel.undoPaydayRecommendation())
        advanceUntilIdle()

        assertTrue(fakePlannerRepository.loadRules().isEmpty())
        assertEquals(
            "home_review_recommendation_undone_feedback",
            viewModel.state.value.reviewCard.recommendationMessageKey
        )
    }

    @Test
    fun `grace period keeps premium automation available`() = runTest {
        fakeEntitlementRepository.setEntitlementState(EntitlementState.GRACE_PERIOD)
        fakePlannerRepository.saveAutomationEnabled(true)
        advanceUntilIdle()

        assertEquals(true, fakePlannerRepository.automationEnabled())
        assertEquals(true, viewModel.state.value.moreCard.isPremium)
        assertEquals(true, viewModel.state.value.moreCard.automationEnabled)
    }

    @Test
    fun `connectGoogleAccount shows success feedback`() = runTest {
        coEvery { mockAuthRepository.connectGoogleAccount("google-token") } returns Result.success(
            User(email = "user@example.com")
        )

        viewModel.connectGoogleAccount("google-token")
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.moreCard.isGoogleConnectionInProgress)
        assertEquals(
            "home_more_account_connect_google_success",
            viewModel.state.value.moreCard.googleConnectionMessage
        )
        assertEquals(null, viewModel.state.value.moreCard.googleConnectionError)
        assertEquals(1, fakePlannerRepository.syncLocalStateIfAuthenticatedCalls)
    }

    @Test
    fun `logout clears local state and returns to onboarding for non premium user`() = runTest {
        val currentPlan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
        fakePlannerRepository.savePlan(currentPlan)
        fakePlannerRepository.setOnboardingCompleted(true)
        var completed = false
        advanceUntilIdle()

        viewModel.onLogout { completed = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { mockAuthRepository.signOut() }
        assertTrue(completed)
        assertEquals(1, fakePlannerRepository.clearPlanCalls)
        assertEquals(null, fakePlannerRepository.loadPlan())
        assertFalse(fakePlannerRepository.isOnboardingCompleted())
        assertFalse(viewModel.state.value.moreCard.isLogoutInProgress)
    }

    @Test
    fun `logout blocks anonymous premium user and prompts account protection`() = runTest {
        currentUserFlow.value = User(isAnonymous = true)
        fakeEntitlementRepository.setEntitlementState(EntitlementState.PRO)
        var completed = false
        advanceUntilIdle()

        viewModel.onLogout { completed = true }
        advanceUntilIdle()

        coVerify(exactly = 0) { mockAuthRepository.signOut() }
        assertFalse(completed)
        assertEquals(0, fakePlannerRepository.clearPlanCalls)
        assertTrue(viewModel.state.value.moreCard.showPremiumAccountPrompt)
        assertFalse(viewModel.state.value.moreCard.isLogoutInProgress)
    }

    @Test
    fun `logout allows premium Google user because access is recoverable`() = runTest {
        currentUserFlow.value = User(email = "user@example.com", isAnonymous = false)
        fakeEntitlementRepository.setEntitlementState(EntitlementState.PRO)
        var completed = false
        advanceUntilIdle()

        viewModel.onLogout { completed = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { mockAuthRepository.signOut() }
        assertTrue(completed)
        assertEquals(1, fakePlannerRepository.clearPlanCalls)
    }

    @Test
    fun `logout failure surfaces account error and keeps user in app`() = runTest {
        coEvery { mockAuthRepository.signOut() } throws IllegalStateException("network")
        var completed = false
        advanceUntilIdle()

        viewModel.onLogout { completed = true }
        advanceUntilIdle()

        assertFalse(completed)
        assertEquals(0, fakePlannerRepository.clearPlanCalls)
        assertFalse(viewModel.state.value.moreCard.isLogoutInProgress)
        assertEquals(
            "home_more_account_signout_error",
            viewModel.state.value.moreCard.logoutError
        )
    }

    private suspend fun seedPlanWithPerformanceReviews() {
        fakePlannerRepository.savePlan(
            SalaryPlan(
                focus = PlanningFocus.SAVE_WITHOUT_STRESS,
                netIncomePerPayday = BigDecimal("1000"),
                monthlyFixedCosts = BigDecimal("400"),
                payFrequency = PayFrequency.MONTHLY,
                monthlyPayday = 1,
                preset = AllocationPreset.BALANCED
            )
        )
        fakePlannerRepository.saveReview(
            ManualReview(
                id = "older-review",
                actualFlexibleSpend = BigDecimal("350"),
                actualGoalContribution = BigDecimal("80"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("100"),
                createdAt = LocalDate.of(2026, 5, 1)
            )
        )
        fakePlannerRepository.saveReview(
            ManualReview(
                id = "latest-review",
                actualFlexibleSpend = BigDecimal("250"),
                actualGoalContribution = BigDecimal("150"),
                plannedFlexibleSpend = BigDecimal("300"),
                plannedGoalContribution = BigDecimal("100"),
                createdAt = LocalDate.of(2026, 5, 2)
            )
        )
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
    var syncLocalStateIfAuthenticatedCalls = 0
    var clearPlanCalls = 0

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
        val existingIndex = current.indexOfFirst { it.id == goal.id }
        if (existingIndex >= 0) {
            current[existingIndex] = goal
        } else {
            current.add(goal)
        }
        goalsFlow.emit(current)
    }
    override fun loadGoals(): List<Goal> = goalsFlow.value
    override suspend fun deleteGoal(goalId: String) {
        goalsFlow.emit(goalsFlow.value.filter { it.id != goalId })
    }

    override fun observeLatestReview(): MutableStateFlow<ManualReview?> = reviewFlow
    override suspend fun saveReview(review: ManualReview) {
        val current = reviewsFlow.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == review.id }
        if (existingIndex >= 0) {
            current[existingIndex] = review
        } else {
            current.add(review)
        }
        reviewFlow.emit(current.lastOrNull())
        reviewsFlow.emit(current)
    }
    override suspend fun updateReview(review: ManualReview) {
        saveReview(review)
    }
    override suspend fun deleteReview(reviewId: String) {
        val updated = reviewsFlow.value.filterNot { it.id == reviewId }
        reviewsFlow.emit(updated)
        reviewFlow.emit(updated.lastOrNull())
    }
    override fun loadLatestReview(): ManualReview? = reviewFlow.value
    override fun observeReviews(): Flow<List<ManualReview>> = reviewsFlow.asStateFlow()
    fun reviews(): List<ManualReview> = reviewsFlow.value
    
    override fun observeReminderConfiguration(): MutableStateFlow<ReminderConfiguration> = reminderFlow
    override suspend fun saveReminderConfiguration(config: ReminderConfiguration) { reminderFlow.emit(config) }
    override fun loadReminderConfiguration(): ReminderConfiguration = reminderFlow.value
    
    override fun observeAutomationEnabled(): Flow<Boolean> = automationFlow.asStateFlow()
    override suspend fun saveAutomationEnabled(enabled: Boolean) { automationFlow.emit(enabled) }
    fun automationEnabled(): Boolean = automationFlow.value

    override fun isOnboardingCompleted(): Boolean = isOnboardingCompletedState
    override suspend fun setOnboardingCompleted(completed: Boolean) { isOnboardingCompletedState = completed }
    override fun loadPlan(): pt.ms.myshare.domain.model.SalaryPlan? = planFlow.value
    override suspend fun clearPlan() {
        clearPlanCalls += 1
        planFlow.emit(null)
        rulesFlow.emit(emptyList())
        goalsFlow.emit(emptyList())
        reviewFlow.emit(null)
        reviewsFlow.emit(emptyList())
        reminderFlow.emit(ReminderConfiguration(false, 9, 0, ReminderCadence.PAYDAY))
        automationFlow.emit(false)
        isOnboardingCompletedState = false
    }
    override suspend fun syncFromFirestore() {}
    override suspend fun syncLocalStateIfAuthenticated() {
        syncLocalStateIfAuthenticatedCalls += 1
    }
}

class TestFakeEntitlementRepository : EntitlementRepository {
    private val _isPro = MutableStateFlow(false)
    override val isPro = _isPro.asStateFlow()
    private val _entitlementState = MutableStateFlow(EntitlementState.FREE)
    override val entitlementState = _entitlementState.asStateFlow()
    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    override val availableProducts = _availableProducts.asStateFlow()
    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 1)
    override val purchaseEvents = _purchaseEvents
    var purchaseResult: BillingFlowLaunchResult = BillingFlowLaunchResult.Launched

    override suspend fun checkActiveEntitlement() {}
    override suspend fun purchasePlan(activity: android.app.Activity, product: StoreProduct): BillingFlowLaunchResult = purchaseResult
    suspend fun setProducts(products: List<StoreProduct>) { _availableProducts.emit(products) }
    suspend fun emitPurchaseEvent(event: BillingPurchaseEvent) { _purchaseEvents.emit(event) }
    suspend fun setPro(value: Boolean) {
        _isPro.emit(value)
        _entitlementState.emit(if (value) EntitlementState.PRO else EntitlementState.FREE)
    }
    suspend fun setEntitlementState(state: EntitlementState) {
        _entitlementState.emit(state)
        _isPro.emit(state.hasPremiumAccess)
    }
    override suspend fun restorePurchases() {}
}
