package pt.ms.myshare.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendation
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PremiumCheckInPlan
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.PremiumGoalPaydaySplit
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.AdjustGoalProgressForReviewCorrectionUseCase
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreatePaydayAdjustmentRecommendationUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumCheckInPlanUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumGoalPaydaySplitUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.EnforcePremiumDowngradeUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.domain.use_case.GetReviewHistoryUseCase
import pt.ms.myshare.domain.use_case.UpdateGoalProgressUseCase
import pt.ms.myshare.domain.use_case.GetPerformanceStatsUseCase
import pt.ms.myshare.domain.use_case.GetCoachingInsightsUseCase
import pt.ms.myshare.domain.use_case.PerformanceStats
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.formatting.SubscriptionSavingsFormatter
import pt.ms.myshare.presentation.ui.localization.UserLocaleManager
import pt.ms.myshare.presentation.ui.onboarding.ReminderWorkScheduler
import pt.ms.myshare.presentation.ui.paywall.BillingStatusMessageKeys
import pt.ms.myshare.presentation.ui.paywall.BillingStatusMessageMapper
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val createPaydayAdjustmentRecommendationUseCase: CreatePaydayAdjustmentRecommendationUseCase,
    private val createPremiumCheckInPlanUseCase: CreatePremiumCheckInPlanUseCase,
    private val createPremiumGoalPaydaySplitUseCase: CreatePremiumGoalPaydaySplitUseCase,
    private val createReviewInsightUseCase: CreateReviewInsightUseCase,
    private val enforcePremiumDowngradeUseCase: EnforcePremiumDowngradeUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase,
    private val getReviewHistoryUseCase: GetReviewHistoryUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val adjustGoalProgressForReviewCorrectionUseCase: AdjustGoalProgressForReviewCorrectionUseCase,
    private val getPerformanceStatsUseCase: GetPerformanceStatsUseCase,
    private val getCoachingInsightsUseCase: GetCoachingInsightsUseCase,
    private val reminderWorkScheduler: ReminderWorkScheduler,
    private val userLocaleManager: UserLocaleManager
) : ViewModel() {

    private val uiState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = uiState.asStateFlow()

    private var availableStoreProducts: List<pt.ms.myshare.domain.model.StoreProduct> = emptyList()
    private var currentPlan: SalaryPlan? = null
    private var currentReviews: List<ManualReview> = emptyList()
    private var currentPaydayRecommendation: PaydayAdjustmentRecommendation? = null
    private var paydayRecommendationRollback: PaydayRecommendationRollback? = null
    private var nextReviewSavedEventId = 0L

    private var currentPreferences = userPreferencesRepository.loadPreferences()

    fun onToggleAutomation(enabled: Boolean) {
        viewModelScope.launch {
            plannerRepository.saveAutomationEnabled(enabled)
            FirebaseUtils.logEvent(if (enabled) "automation_enabled" else "automation_disabled")
            Timber.tag(TAG).d("Automation toggled: %s", enabled)
        }
    }

    init {
        viewModelScope.launch {
            plannerRepository.syncFromFirestore()
            userPreferencesRepository.syncFromFirestore()
        }
        userLocaleManager.apply(currentPreferences)
        observeProducts()
        observeBillingPurchaseEvents()
        observeEntitlementLifecycle()
        observePlannerData()
        observeReviewHistory()
        FirebaseUtils.logScreen("home")
    }

    private fun observeReviewHistory() {
        viewModelScope.launch {
            getReviewHistoryUseCase.execute().collect { history ->
                uiState.update { it.copy(reviewHistory = history) }
                Timber.tag(TAG).d("Review history updated: %d items", history.size)
            }
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            entitlementRepository.availableProducts.collect { products ->
                availableStoreProducts = products
            }
        }
    }

    private fun observeBillingPurchaseEvents() {
        viewModelScope.launch {
            entitlementRepository.purchaseEvents.collect { event ->
                val messageKey = BillingStatusMessageMapper.fromPurchaseEvent(event)
                var shouldLogAccountPrompt = false
                uiState.update {
                    val shouldShowAccountPrompt = event == BillingPurchaseEvent.Completed &&
                        it.moreCard.userEmail.isNullOrBlank() &&
                        !it.moreCard.showPremiumAccountPrompt
                    shouldLogAccountPrompt = shouldShowAccountPrompt
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isBillingActionInProgress = false,
                            billingMessage = messageKey,
                            showPremiumAccountPrompt = it.moreCard.showPremiumAccountPrompt || shouldShowAccountPrompt,
                            error = null
                        )
                    )
                }
                if (shouldLogAccountPrompt) {
                    FirebaseUtils.logEvent("premium_account_prompt_shown")
                    Timber.tag(TAG).d("Premium account prompt shown after purchase completion")
                }
                logBillingPurchaseEvent(event)
            }
        }
    }

    private fun observeEntitlementLifecycle() {
        viewModelScope.launch {
            entitlementRepository.entitlementState
                .distinctUntilChanged()
                .collect { entitlementState ->
                    val automationDisabled = enforcePremiumDowngradeUseCase.execute(entitlementState)
                    if (automationDisabled) {
                        FirebaseUtils.logEvent("premium_downgrade_enforced")
                        Timber.tag(TAG).d("Premium downgrade enforced. entitlementState=%s automationDisabled=true", entitlementState)
                    }
                }
        }
    }

    private fun observePlannerData() {
        viewModelScope.launch {
            val plannerCoreFlow = combine(
                plannerRepository.observePlan(),
                plannerRepository.observeRules(),
                plannerRepository.observeGoals(),
                plannerRepository.observeLatestReview()
            ) { plan, rules, goals, latestReview ->
                PlannerCore(
                    plan = plan,
                    rules = rules,
                    goals = goals,
                    latestReview = latestReview
                )
            }

            val plannerSignalsFlow = combine(
                plannerRepository.observeReminderConfiguration(),
                plannerRepository.observeAutomationEnabled(),
                plannerRepository.observeReviews(),
                getPerformanceStatsUseCase.execute()
            ) { reminder, automation, history, stats ->
                PlannerSignals(
                    reminder = reminder,
                    automation = automation,
                    history = history,
                    stats = stats
                )
            }

            val plannerFlow = combine(plannerCoreFlow, plannerSignalsFlow) { core, signals ->
                val coaching = if (core.plan != null) {
                    getCoachingInsightsUseCase.execute(core.plan, signals.history)
                } else {
                    emptyList()
                }
                
                val trend = if (core.plan != null) {
                    signals.history.reversed().takeLast(5).map { review ->
                        val preview = calculatePlanPreviewUseCase.execute(core.plan, BigDecimal.ZERO)
                        val flexTarget = review.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
                        val goalTarget = review.plannedGoalContribution ?: preview.priorityContributionPerPayday
                        
                        val flexPerformance = if (review.actualFlexibleSpend <= flexTarget) 1.0f else 
                            (flexTarget.toDouble() / review.actualFlexibleSpend.toDouble()).coerceIn(0.0, 1.0).toFloat()
                        
                        val goalPerformance = if (goalTarget <= BigDecimal.ZERO || review.actualGoalContribution >= goalTarget) 1.0f else
                            (review.actualGoalContribution.toDouble() / goalTarget.toDouble()).coerceIn(0.0, 1.0).toFloat()
                            
                        (flexPerformance + goalPerformance) / 2f
                    }
                } else {
                    emptyList()
                }
                
                PlannerGroup(
                    plan = core.plan,
                    rules = core.rules,
                    goals = core.goals,
                    latestReview = core.latestReview,
                    reminder = signals.reminder,
                    automation = signals.automation,
                    reviewHistory = signals.history,
                    performanceStats = signals.stats,
                    coachingInsights = coaching,
                    performanceTrend = trend
                )
            }

            combine(
                plannerFlow,
                entitlementRepository.isPro,
                entitlementRepository.availableProducts,
                authRepository.currentUser,
                userPreferencesRepository.observePreferences()
            ) { planner, isPremium, products, user, preferences ->
                val currentState = uiState.value
                currentPreferences = preferences
                userLocaleManager.apply(preferences)
                val locale = preferences.locale
                val pricingStrategy = resolvePricingStrategyUseCase.execute(locale)
                val plan = planner.plan
                val currentRules = planner.rules
                val goals = planner.goals
                val latestReview = planner.latestReview
                val reminder = planner.reminder
                val automation = planner.automation
                currentReviews = planner.reviewHistory
                
                val updatedPlan = plan?.copy(rules = currentRules)
                currentPlan = updatedPlan
                val paydayRecommendation = updatedPlan?.let {
                    createPaydayAdjustmentRecommendationUseCase.execute(it, planner.reviewHistory)
                }
                currentPaydayRecommendation = paydayRecommendation
                val premiumCheckIn = updatedPlan?.let {
                    createPremiumCheckInPlanUseCase.execute(
                        plan = it,
                        latestReview = latestReview,
                        reminderConfiguration = reminder,
                        automationEnabled = automation && isPremium
                    ).toState(preferences)
                }

                val emptyMessage = if (updatedPlan == null) "home_empty_build_plan_first" else null
                val primaryGoal = goals.firstOrNull()
                val planCard = updatedPlan?.let { buildPlanCard(it, primaryGoal?.targetAmount ?: BigDecimal.ZERO, preferences) }
                val goalPaydaySplit = if (isPremium && updatedPlan != null) {
                    val priorityPreview = calculatePlanPreviewUseCase.execute(updatedPlan, BigDecimal.ZERO)
                    createPremiumGoalPaydaySplitUseCase
                        .execute(goals, priorityPreview.priorityContributionPerPayday)
                        ?.toState(goals, preferences)
                } else {
                    null
                }
                goalPaydaySplit?.let {
                    Timber.tag(TAG).d(
                        "Premium goal payday split ready. goalCount=%d visible=%d hidden=%d",
                        it.goalCount,
                        it.visibleItems.size,
                        it.hiddenGoalCount
                    )
                }
                val goalCards = goals.mapIndexed { index, goal ->
                    buildGoalCard(goal, updatedPlan, preferences).copy(
                        isLockedByEntitlement = !isPremium && index > 0
                    )
                }
                val ruleCards = currentRules.mapIndexed { index, rule ->
                    buildRuleCard(rule, preferences).copy(
                        isLockedByEntitlement = !isPremium && index > 0
                    )
                }
                val reviewCard = buildReviewCard(updatedPlan, latestReview, preferences)
                val performanceStats = if (isPremium) {
                    planner.performanceStats.toState(planner.performanceTrend)
                } else {
                    PerformanceStatsState(totalReviews = planner.reviewHistory.size)
                }
                val coachingInsights = if (isPremium) {
                    planner.coachingInsights.map { it.toState() }
                } else {
                    emptyList()
                }
                
                val monthlyProduct = products.find { it.productId == PremiumSubscriptionProducts.MONTHLY_ID }
                val annualProduct = products.find { it.productId == PremiumSubscriptionProducts.ANNUAL_ID }
                val annualComparison = SubscriptionSavingsFormatter.formatAnnualComparison(
                    monthlyProduct = monthlyProduct,
                    annualProduct = annualProduct,
                    locale = locale
                )
                val currentMore = currentState.moreCard
                val selectedBillingPlan = if (currentMore.pricingStrategy == null) {
                    pricingStrategy.heroPlan
                } else {
                    currentMore.selectedBillingPlan
                }
                val selectedProductAvailable = when (selectedBillingPlan) {
                    BillingPlan.MONTHLY -> monthlyProduct != null
                    BillingPlan.ANNUAL -> annualProduct != null
                }
                val shouldClearUnavailableMessage =
                    currentMore.billingMessage == BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE && selectedProductAvailable

                val paydayRecommendationState = paydayRecommendation?.toState(preferences)
                val recommendationMessageKey = currentState.reviewCard.recommendationMessageKey
                val moreCard = MoreCardState(
                    reminderEnabled = reminder.enabled,
                    reminderLabelKey = if (reminder.enabled) {
                        reminder.cadence.labelAtTimeKey
                    } else {
                        "home_more_reminders_off"
                    },
                    reminderLabelArgs = if (reminder.enabled) {
                        listOf(
                            "${reminder.hourOfDay.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}"
                        )
                    } else {
                        emptyList()
                    },
                    reminderHourOfDay = reminder.hourOfDay,
                    reminderMinute = reminder.minute,
                    reminderCadence = reminder.cadence,
                    pricingStrategy = pricingStrategy,
                    actualMonthlyPrice = monthlyProduct?.price,
                    actualAnnualPrice = annualProduct?.price,
                    actualMonthlyPriceCurrencyCode = monthlyProduct?.priceCurrencyCode,
                    actualAnnualPriceCurrencyCode = annualProduct?.priceCurrencyCode,
                    annualMonthlyEquivalentPrice = annualComparison?.monthlyEquivalentPrice,
                    annualSavingsPrice = annualComparison?.savingsPrice,
                    actualMonthlyTrialDays = monthlyProduct?.freeTrialDays?.takeIf { it > 0 },
                    actualAnnualTrialDays = annualProduct?.freeTrialDays?.takeIf { it > 0 },
                    showAdsConsentOption = currentMore.showAdsConsentOption,
                    selectedBillingPlan = selectedBillingPlan,
                    isBillingActionInProgress = currentMore.isBillingActionInProgress,
                    billingMessage = currentMore.billingMessage.takeUnless { shouldClearUnavailableMessage },
                    isPremium = isPremium,
                    automationEnabled = automation && isPremium,
                    userEmail = user?.email,
                    canConnectGoogle = user?.email.isNullOrBlank(),
                    showPremiumAccountPrompt = currentMore.showPremiumAccountPrompt && user?.email.isNullOrBlank(),
                    isGoogleConnectionInProgress = currentMore.isGoogleConnectionInProgress,
                    googleConnectionMessage = currentMore.googleConnectionMessage,
                    googleConnectionError = currentMore.googleConnectionError,
                    isLogoutInProgress = currentMore.isLogoutInProgress,
                    logoutError = currentMore.logoutError,
                    userPreferences = preferences,
                    weeklyGuideLabel = planCard?.weeklySpendLabel.orEmpty(),
                    priorityMoveLabel = planCard?.savingsLabel.orEmpty(),
                    ruleCount = currentRules.size,
                    reviewCount = planner.reviewHistory.size,
                    smartAdjustment = paydayRecommendationState.toSmartAdjustmentControlState(
                        hasPlan = updatedPlan != null,
                        messageKey = recommendationMessageKey
                    ),
                    premiumCheckIn = premiumCheckIn.takeIf { isPremium },
                    error = currentMore.error.takeUnless { shouldClearUnavailableMessage }
                )
                HomeState(
                    selectedDestination = currentState.selectedDestination,
                    plan = updatedPlan,
                    planCard = planCard,
                    goalPaydaySplit = goalPaydaySplit,
                    goals = goalCards,
                    rules = ruleCards,
                    performanceStats = performanceStats,
                    reviewCard = reviewCard.copy(
                        coachingInsights = coachingInsights,
                        paydayRecommendation = paydayRecommendationState,
                        premiumCheckIn = premiumCheckIn.takeIf { isPremium },
                        recommendationMessageKey = recommendationMessageKey
                    ),
                    reviewHistory = planner.reviewHistory.asReversed().map { it.toState() },
                    moreCard = moreCard,
                    reviewSavedEventId = currentState.reviewSavedEventId,
                    isLoading = false,
                    emptyMessage = emptyMessage,
                    error = null
                )
            }.collect { newState ->
                uiState.value = newState
            }
        }
    }

    private data class PlannerGroup(
        val plan: SalaryPlan?,
        val rules: List<PaydayRule>,
        val goals: List<Goal>,
        val latestReview: ManualReview?,
        val reminder: ReminderConfiguration,
        val automation: Boolean,
        val reviewHistory: List<ManualReview>,
        val performanceStats: PerformanceStats,
        val coachingInsights: List<ReviewInsight>,
        val performanceTrend: List<Float>
    )

    private data class PlannerCore(
        val plan: SalaryPlan?,
        val rules: List<PaydayRule>,
        val goals: List<Goal>,
        val latestReview: ManualReview?
    )

    private data class PlannerSignals(
        val reminder: ReminderConfiguration,
        val automation: Boolean,
        val history: List<ManualReview>,
        val stats: PerformanceStats
    )

    private data class PaydayRecommendationRollback(
        val previousRules: List<PaydayRule>,
        val affectedRuleIds: Set<String>
    )

    private fun buildPlanCard(
        plan: pt.ms.myshare.domain.model.SalaryPlan,
        goalAmount: BigDecimal,
        preferences: UserPreferences
    ): HomePlanCardState {
        val preview = calculatePlanPreviewUseCase.execute(plan, goalAmount)
        val currencyFormat = currencyFormat(preferences)
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", preferences.locale)
        return HomePlanCardState(
            incomeLabel = currencyFormat.format(plan.netIncomePerPayday),
            fixedCostsLabel = currencyFormat.format(preview.fixedCostsPerPayday),
            flexibleSpendLabel = currencyFormat.format(preview.flexibleSpendPerPayday),
            savingsLabel = currencyFormat.format(preview.priorityContributionPerPayday),
            investingLabel = currencyFormat.format(preview.investingPerPayday.add(preview.cryptoPerPayday)),
            weeklySpendLabel = currencyFormat.format(preview.weeklyFlexibleSpend),
            summary = preview.summary,
            nextPaydayKey = "home_plan_next_payday",
            nextPaydayArgs = listOf(preview.nextPayday.format(dateFormatter))
        )
    }

    private fun buildGoalCard(goal: Goal, plan: SalaryPlan?, preferences: UserPreferences): GoalCardState {
        val preview = if (plan != null) {
            calculatePlanPreviewUseCase.execute(plan, goal.targetAmount)
        } else null
        val currencyFormat = currencyFormat(preferences)

        val (targetDateKey, targetDateArgs) = if (goal.isCompleted) {
            "home_goal_mission_accomplished" to emptyList()
        } else {
            preview?.goalTargetDate?.let {
                "home_goal_on_pace" to listOf(it.format(DateTimeFormatter.ofPattern("MMMM yyyy", preferences.locale)))
            } ?: ("home_goal_no_trajectory" to emptyList())
        }
        
        val progressPercent = if (goal.targetAmount > BigDecimal.ZERO) {
            goal.currentProgress.divide(goal.targetAmount, 4, java.math.RoundingMode.HALF_UP).toFloat()
        } else 0f

        return GoalCardState(
            id = goal.id,
            goalName = goal.name,
            goalNameKey = goal.defaultNameKey,
            goalAmountLabel = currencyFormat.format(goal.targetAmount),
            progress = progressPercent,
            progressLabelKey = "home_goal_progress_label",
            progressLabelArgs = listOf(currencyFormat.format(goal.currentProgress), currencyFormat.format(goal.targetAmount)),
            targetDateKey = targetDateKey,
            targetDateArgs = targetDateArgs,
            progressNoteKey = if (goal.isCompleted) "home_goal_note_completed" else "home_goal_note_tracking"
        )
    }

    private fun buildRuleCard(rule: pt.ms.myshare.domain.model.PaydayRule, preferences: UserPreferences): RuleCardState {
        val amountLabel = if (rule.isPercentage) {
            LocalizedAmountFormatter.formatPercentage(rule.amount, preferences.locale)
        } else {
            currencyFormat(preferences).format(rule.amount)
        }
        return RuleCardState(
            id = rule.id,
            name = rule.name,
            amountLabel = amountLabel,
            typeLabel = rule.type.name.lowercase().replaceFirstChar { it.titlecase() },
            typeLabelKey = rule.type.labelKey,
            isPercentage = rule.isPercentage,
            nameKey = rule.defaultNameKey
        )
    }

    private fun buildReviewCard(
        plan: pt.ms.myshare.domain.model.SalaryPlan?,
        review: ManualReview?,
        preferences: UserPreferences
    ): ReviewCardState {
        val insight = if (plan != null && review != null) {
            createReviewInsightUseCase.execute(plan, review)
        } else {
            null
        }
        val preview = plan?.let { calculatePlanPreviewUseCase.execute(it, BigDecimal.ZERO) }
        val defaultFlexibleSpend = review?.actualFlexibleSpend ?: preview?.flexibleSpendPerPayday
        val defaultGoalContribution = review?.actualGoalContribution ?: preview?.priorityContributionPerPayday
        val flexibleMax = reviewRangeMax(defaultFlexibleSpend ?: BigDecimal.ZERO)
        val goalMax = reviewRangeMax(defaultGoalContribution ?: BigDecimal.ZERO)

        return ReviewCardState(
            actualFlexibleSpend = defaultFlexibleSpend?.let { LocalizedAmountFormatter.formatEditableAmount(it, preferences.locale) }.orEmpty(),
            actualGoalContribution = defaultGoalContribution?.let { LocalizedAmountFormatter.formatEditableAmount(it, preferences.locale) }.orEmpty(),
            currencySymbol = LocalizedAmountFormatter.currencySymbol(preferences.locale, preferences.currencyCode),
            flexibleSpendMax = flexibleMax,
            goalContributionMax = goalMax,
            insight = insight,
            savedReviewDate = review?.createdAt?.format(DateTimeFormatter.ofPattern("d MMM", preferences.locale))
        )
    }

    fun selectDestination(destination: HomeDestination) {
        uiState.update { it.copy(selectedDestination = destination) }
        FirebaseUtils.logEvent("home_tab_opened", android.os.Bundle().apply {
            putString("tab", destination.name.lowercase(Locale.US))
        })
    }

    fun onFlexibleSpendChanged(value: String) {
        uiState.update { it.copy(reviewCard = it.reviewCard.copy(actualFlexibleSpend = sanitizeNumber(value), error = null)) }
    }

    fun onGoalContributionChanged(value: String) {
        uiState.update { it.copy(reviewCard = it.reviewCard.copy(actualGoalContribution = sanitizeNumber(value), error = null)) }
    }

    fun saveReview() {
        val plan = currentPlan
        if (plan == null) {
            uiState.update { it.copy(reviewCard = it.reviewCard.copy(error = "home_review_error_no_plan")) }
            return
        }

        val actualFlexible = LocalizedAmountFormatter.parseAmount(uiState.value.reviewCard.actualFlexibleSpend, currentPreferences.locale)
        val actualGoal = LocalizedAmountFormatter.parseAmount(uiState.value.reviewCard.actualGoalContribution, currentPreferences.locale)
        if (actualFlexible == null || actualGoal == null) {
            uiState.update { it.copy(reviewCard = it.reviewCard.copy(error = "home_review_error_invalid_amounts")) }
            return
        }

        viewModelScope.launch {
            val goals = plannerRepository.loadGoals()
            val targetAmount = goals.firstOrNull()?.targetAmount ?: BigDecimal.ZERO
            val preview = calculatePlanPreviewUseCase.execute(plan, targetAmount)

            val review = ManualReview(
                actualFlexibleSpend = actualFlexible,
                actualGoalContribution = actualGoal,
                plannedFlexibleSpend = preview.flexibleSpendPerPayday,
                plannedGoalContribution = preview.priorityContributionPerPayday
            )
            plannerRepository.saveReview(review)
            updateGoalProgressUseCase.execute(actualGoal)
            emitReviewSavedFeedback()
            
            FirebaseUtils.logEvent("weekly_checkin_completed")
            Timber.tag(TAG).d("Review saved with snapshots. planFlex=%s planPriority=%s", preview.flexibleSpendPerPayday, preview.priorityContributionPerPayday)
        }
    }

    fun updateReview(
        reviewId: String,
        flexibleSpend: String,
        goalContribution: String
    ): Boolean {
        val originalReview = currentReviews.firstOrNull { it.id == reviewId }
        if (originalReview == null) {
            Timber.tag(TAG).d("Review update ignored; review not found. reviewId=%s", reviewId)
            return false
        }

        val actualFlexible = LocalizedAmountFormatter.parseAmount(flexibleSpend, currentPreferences.locale)
        val actualGoal = LocalizedAmountFormatter.parseAmount(goalContribution, currentPreferences.locale)
        if (actualFlexible == null || actualGoal == null) {
            Timber.tag(TAG).d("Review update rejected; invalid amounts. reviewId=%s", reviewId)
            return false
        }

        viewModelScope.launch {
            val updatedReview = originalReview.copy(
                actualFlexibleSpend = actualFlexible,
                actualGoalContribution = actualGoal
            )
            plannerRepository.updateReview(updatedReview)
            adjustGoalProgressForReviewCorrectionUseCase.execute(
                actualGoal.subtract(originalReview.actualGoalContribution)
            )
            FirebaseUtils.logEvent("review_updated")
            Timber.tag(TAG).d("Review updated. reviewId=%s", reviewId)
        }
        return true
    }

    fun deleteReview(reviewId: String) {
        val review = currentReviews.firstOrNull { it.id == reviewId }
        if (review == null) {
            Timber.tag(TAG).d("Review delete ignored; review not found. reviewId=%s", reviewId)
            return
        }

        viewModelScope.launch {
            plannerRepository.deleteReview(reviewId)
            adjustGoalProgressForReviewCorrectionUseCase.execute(review.actualGoalContribution.negate())
            FirebaseUtils.logEvent("review_deleted")
            Timber.tag(TAG).d("Review deleted. reviewId=%s", reviewId)
        }
    }

    fun applyPaydayRecommendation(): Boolean {
        val recommendation = currentPaydayRecommendation
        if (recommendation == null || !recommendation.isApplyable) {
            Timber.tag(TAG).d("Payday recommendation apply ignored. recommendationAvailable=%s", recommendation != null)
            return false
        }
        if (!uiState.value.moreCard.isPremium) {
            Timber.tag(TAG).d("Payday recommendation apply blocked for free user")
            return false
        }

        val rulesBeforeApply = plannerRepository.loadRules()
        val suggestedRules = recommendation.suggestedRules
        paydayRecommendationRollback = PaydayRecommendationRollback(
            previousRules = rulesBeforeApply,
            affectedRuleIds = suggestedRules.map { it.id }.toSet()
        )

        viewModelScope.launch {
            try {
                suggestedRules.forEach { rule ->
                    plannerRepository.saveRule(rule)
                }
                uiState.update {
                    it.copy(
                        reviewCard = it.reviewCard.copy(
                            recommendationMessageKey = "home_review_recommendation_applied_feedback"
                        )
                    )
                }
                FirebaseUtils.logEvent("premium_payday_adjustment_applied", android.os.Bundle().apply {
                    putString("direction", recommendation.direction.name.lowercase(Locale.US))
                    putInt("review_count", recommendation.analyzedReviewCount)
                    putString("adjustment_amount", recommendation.adjustmentAmount.toPlainString())
                })
                Timber.tag(TAG).d(
                    "Premium payday recommendation applied. direction=%s rules=%d adjustment=%s",
                    recommendation.direction,
                    suggestedRules.size,
                    recommendation.adjustmentAmount
                )
            } catch (exception: Exception) {
                paydayRecommendationRollback = null
                Timber.tag(TAG).e(exception, "Failed to apply Premium payday recommendation")
            }
        }
        return true
    }

    fun undoPaydayRecommendation(): Boolean {
        val rollback = paydayRecommendationRollback
        if (rollback == null) {
            Timber.tag(TAG).d("Payday recommendation undo ignored; no rollback available")
            return false
        }

        paydayRecommendationRollback = null
        viewModelScope.launch {
            try {
                val previousRulesById = rollback.previousRules.associateBy { it.id }
                rollback.affectedRuleIds.forEach { ruleId ->
                    val previousRule = previousRulesById[ruleId]
                    if (previousRule != null) {
                        plannerRepository.saveRule(previousRule)
                    } else {
                        plannerRepository.deleteRule(ruleId)
                    }
                }
                uiState.update {
                    it.copy(
                        reviewCard = it.reviewCard.copy(
                            recommendationMessageKey = "home_review_recommendation_undone_feedback"
                        )
                    )
                }
                FirebaseUtils.logEvent("premium_payday_adjustment_undone")
                Timber.tag(TAG).d(
                    "Premium payday recommendation undone. affectedRules=%d",
                    rollback.affectedRuleIds.size
                )
            } catch (exception: Exception) {
                Timber.tag(TAG).e(exception, "Failed to undo Premium payday recommendation")
            }
        }
        return true
    }

    private fun emitReviewSavedFeedback() {
        nextReviewSavedEventId += 1
        val eventId = nextReviewSavedEventId
        uiState.update { it.copy(reviewSavedEventId = eventId) }
        Timber.tag(TAG).d("Review saved feedback emitted eventId=%d", eventId)
    }

    fun clearReviewSavedFeedback(eventId: Long) {
        uiState.update { current ->
            if (current.reviewSavedEventId == eventId) {
                current.copy(reviewSavedEventId = 0L)
            } else {
                current
            }
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val current = plannerRepository.loadReminderConfiguration()
            val configuration = current.copy(enabled = enabled)
            plannerRepository.saveReminderConfiguration(configuration)
            reminderWorkScheduler.sync(configuration)
            FirebaseUtils.logEvent(if (enabled) "reminder_enabled" else "reminder_disabled")
            Timber.tag(TAG).d("Reminder toggled enabled=%s", enabled)
        }
    }

    fun saveReminderConfiguration(hourOfDay: Int, minute: Int, cadence: ReminderCadence) {
        viewModelScope.launch {
            val configuration = ReminderConfiguration(
                enabled = true,
                hourOfDay = hourOfDay.coerceIn(0, 23),
                minute = minute.coerceIn(0, 59),
                cadence = cadence
            )
            plannerRepository.saveReminderConfiguration(configuration)
            reminderWorkScheduler.sync(configuration)
            FirebaseUtils.logEvent("reminder_settings_saved")
            Timber.tag(TAG).d(
                "Reminder settings saved hour=%s minute=%s cadence=%s",
                configuration.hourOfDay,
                configuration.minute,
                configuration.cadence
            )
        }
    }

    fun chooseBillingPlan(plan: BillingPlan) {
        uiState.update { it.copy(moreCard = it.moreCard.copy(selectedBillingPlan = plan, billingMessage = null, error = null)) }
        FirebaseUtils.logEvent("paywall_plan_selected", android.os.Bundle().apply {
            putString("billing_plan", plan.name.lowercase(Locale.US))
            putString("price_cluster", currentPricingStrategy().marketCluster)
            putString("source", "home_more")
        })
    }

    fun updateLanguage(languageTag: String) {
        val updated = currentPreferences.copy(languageTag = languageTag)
        updatePreferenceState(updated)
        viewModelScope.launch {
            userPreferencesRepository.savePreferences(updated)
            userLocaleManager.apply(userPreferencesRepository.loadPreferences())
        }
    }

    fun updateCurrency(currencyCode: String) {
        val updated = currentPreferences.copy(currencyCode = currencyCode)
        updatePreferenceState(updated)
        viewModelScope.launch {
            userPreferencesRepository.savePreferences(updated)
        }
    }

    private fun updatePreferenceState(preferences: UserPreferences) {
        currentPreferences = preferences
        val updatedPricing = resolvePricingStrategyUseCase.execute(preferences.locale)
        uiState.update { current ->
            val previousPricing = current.moreCard.pricingStrategy
            val updatedSelectedPlan = previousPricing?.let { oldPricing ->
                if (current.moreCard.selectedBillingPlan == oldPricing.heroPlan) {
                    updatedPricing.heroPlan
                } else {
                    current.moreCard.selectedBillingPlan
                }
            } ?: updatedPricing.heroPlan

            current.copy(
                moreCard = current.moreCard.copy(
                    userPreferences = preferences,
                    pricingStrategy = updatedPricing,
                    selectedBillingPlan = updatedSelectedPlan
                )
            )
        }
    }

    fun connectGoogleAccount(idToken: String) {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    moreCard = it.moreCard.copy(
                        isGoogleConnectionInProgress = true,
                        googleConnectionMessage = null,
                        googleConnectionError = null
                    )
                )
            }

            val result = authRepository.connectGoogleAccount(idToken)
            result.fold(
                onSuccess = { user ->
                    plannerRepository.syncLocalStateIfAuthenticated()
                    userPreferencesRepository.syncFromFirestore()
                    uiState.update {
                        it.copy(
                            moreCard = it.moreCard.copy(
                                isGoogleConnectionInProgress = false,
                                googleConnectionMessage = "home_more_account_connect_google_success",
                                googleConnectionError = null,
                                userEmail = user.email,
                                canConnectGoogle = user.email.isNullOrBlank(),
                                showPremiumAccountPrompt = false
                            )
                        )
                    }
                    FirebaseUtils.logEvent("google_account_connected")
                    Timber.tag(TAG).d("Google account connected from Home")
                },
                onFailure = { throwable ->
                    uiState.update {
                        it.copy(
                            moreCard = it.moreCard.copy(
                                isGoogleConnectionInProgress = false,
                                googleConnectionMessage = null,
                                googleConnectionError = "home_more_account_connect_google_error_generic"
                            )
                        )
                    }
                    FirebaseUtils.logEvent("google_account_connect_failed")
                    Timber.tag(TAG).e(throwable, "Google account connection failed from Home")
                }
            )
        }
    }

    fun setGoogleConnectionCredentialError(errorKey: String) {
        uiState.update {
            it.copy(
                moreCard = it.moreCard.copy(
                    isGoogleConnectionInProgress = false,
                    googleConnectionMessage = null,
                    googleConnectionError = errorKey
                )
            )
        }
    }

    fun dismissPremiumAccountPrompt() {
        uiState.update {
            it.copy(
                moreCard = it.moreCard.copy(showPremiumAccountPrompt = false)
            )
        }
        Timber.tag(TAG).d("Premium account prompt dismissed")
    }

    fun logPremiumGateViewed(gate: HomePremiumGate) {
        clearIdleBillingFeedback()
        logPremiumGateEvent("premium_gate_viewed", gate)
    }

    fun logPremiumGateUpgradeClicked(gate: HomePremiumGate) {
        logPremiumGateEvent("premium_gate_upgrade_clicked", gate)
    }

    fun unlockPremium(activity: android.app.Activity, source: String = "more_inline") {
        val storeProductId = selectedStoreProductId()
        val realProduct = selectedStoreProduct()

        viewModelScope.launch {
            uiState.update {
                it.copy(
                    moreCard = it.moreCard.copy(
                        isBillingActionInProgress = true,
                        billingMessage = BillingStatusMessageKeys.STARTING,
                        error = null
                    )
                )
            }
            if (realProduct != null) {
                FirebaseUtils.logEvent("purchase_started", android.os.Bundle().apply {
                    putString("billing_plan", uiState.value.moreCard.selectedBillingPlan.name.lowercase(Locale.US))
                    putString("price_cluster", currentPricingStrategy().marketCluster)
                    putString("product_id", realProduct.productId)
                    putBoolean("has_trial", realProduct.hasFreeTrial)
                    putString("source", source)
                })
                val launchResult = entitlementRepository.purchasePlan(activity, realProduct)
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isBillingActionInProgress = false,
                            billingMessage = BillingStatusMessageMapper.fromLaunchResult(launchResult),
                            error = null
                        )
                    )
                }
                logBillingLaunchResult(launchResult, realProduct.productId, source)
            } else {
                FirebaseUtils.logEvent("purchase_unavailable", android.os.Bundle().apply {
                    putString("billing_plan", uiState.value.moreCard.selectedBillingPlan.name.lowercase(Locale.US))
                    putString("price_cluster", currentPricingStrategy().marketCluster)
                    putString("product_id", storeProductId)
                    putString("source", source)
                })
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isBillingActionInProgress = false,
                            billingMessage = BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE,
                            error = "more_error_products_not_loaded"
                        )
                    )
                }
                Timber.tag(TAG).e("Cannot purchase: Product %s not found in store", storeProductId)
            }
        }
    }

    private fun clearIdleBillingFeedback() {
        uiState.update { current ->
            if (current.moreCard.isBillingActionInProgress) {
                current
            } else {
                current.copy(
                    moreCard = current.moreCard.copy(
                        billingMessage = null,
                        error = null
                    )
                )
            }
        }
    }

    private fun logBillingLaunchResult(
        result: BillingFlowLaunchResult,
        productId: String,
        source: String
    ) {
        when (result) {
            BillingFlowLaunchResult.Launched -> Timber.tag(TAG).d(
                "Billing launch accepted product=%s source=%s",
                productId,
                source
            )
            BillingFlowLaunchResult.ProductUnavailable -> Timber.tag(TAG).e(
                "Billing launch unavailable product=%s source=%s",
                productId,
                source
            )
            is BillingFlowLaunchResult.Failed -> Timber.tag(TAG).e(
                "Billing launch failed product=%s source=%s code=%d message=%s",
                productId,
                source,
                result.responseCode,
                result.debugMessage
            )
        }
    }

    private fun logBillingPurchaseEvent(event: BillingPurchaseEvent) {
        when (event) {
            BillingPurchaseEvent.Completed -> Timber.tag(TAG).d("Billing purchase completed")
            BillingPurchaseEvent.Pending -> Timber.tag(TAG).d("Billing purchase pending")
            BillingPurchaseEvent.Canceled -> Timber.tag(TAG).d("Billing purchase canceled")
            is BillingPurchaseEvent.Failed -> Timber.tag(TAG).e(
                "Billing purchase failed code=%d message=%s",
                event.responseCode,
                event.debugMessage
            )
        }
    }

    private fun logPremiumGateEvent(eventName: String, gate: HomePremiumGate) {
        val selectedProduct = selectedStoreProduct()
        FirebaseUtils.logEvent(eventName, android.os.Bundle().apply {
            putString("premium_gate", gate.analyticsName)
            putString("billing_plan", uiState.value.moreCard.selectedBillingPlan.name.lowercase(Locale.US))
            putString("price_cluster", currentPricingStrategy().marketCluster)
            putString("product_id", selectedStoreProductId())
            putBoolean("selected_product_available", selectedProduct != null)
            putBoolean("has_trial", selectedProduct?.hasFreeTrial == true)
        })
        Timber.tag(TAG).d("Premium gate event logged: %s gate=%s", eventName, gate.analyticsName)
    }

    private fun selectedStoreProductId(): String {
        return PremiumSubscriptionProducts.productIdFor(uiState.value.moreCard.selectedBillingPlan)
    }

    private fun selectedStoreProduct(): StoreProduct? {
        val storeProductId = selectedStoreProductId()
        return availableStoreProducts.find { it.productId == storeProductId }
    }

    fun updateAdsConsentRequirement(isRequired: Boolean) {
        uiState.update { it.copy(moreCard = it.moreCard.copy(showAdsConsentOption = isRequired)) }
        Timber.tag(TAG).d("Ads consent option visibility updated: %s", isRequired)
    }

    fun onLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            val moreCard = uiState.value.moreCard
            if (moreCard.requiresPremiumAccountProtectionBeforeLogout) {
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            showPremiumAccountPrompt = true,
                            logoutError = null
                        )
                    )
                }
                FirebaseUtils.logEvent("premium_logout_blocked_until_account_secure")
                Timber.tag(TAG).d("Blocked Premium sign-out until Google account connection is offered")
                return@launch
            }

            uiState.update {
                it.copy(
                    moreCard = it.moreCard.copy(
                        isLogoutInProgress = true,
                        logoutError = null
                    )
                )
            }

            try {
                authRepository.signOut()
                plannerRepository.clearPlan()
                plannerRepository.setOnboardingCompleted(false)
                FirebaseUtils.logEvent("user_logged_out")
                Timber.tag(TAG).d("User signed out and local planner state cleared")
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(isLogoutInProgress = false)
                    )
                }
                onComplete()
            } catch (error: Exception) {
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isLogoutInProgress = false,
                            logoutError = "home_more_account_signout_error"
                        )
                    )
                }
                Timber.tag(TAG).e(error, "Sign out failed")
            }
        }
    }

    private fun PerformanceStats.toState(trend: List<Float> = emptyList()): PerformanceStatsState {
        return PerformanceStatsState(
            healthScore = healthScore,
            currentStreak = currentStreak,
            totalFlexSavingsLabel = currencyFormat(currentPreferences).format(totalSavingsBeyondGoal),
            totalSavings = totalSavingsBeyondGoal,
            totalReviews = totalReviews,
            performanceTrend = trend
        )
    }

    private fun ReviewInsight.toState(): ReviewInsightState {
        return ReviewInsightState(
            headline = headline,
            supportingText = supportingText,
            type = type,
            actionLabel = actionLabel
        )
    }

    private fun PremiumCheckInPlan.toState(preferences: UserPreferences): PremiumCheckInState {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", preferences.locale)
        val daysUntil = ChronoUnit.DAYS.between(today, checkInDate)
        val relative = when {
            status == PremiumCheckInStatus.READY_NOW -> "home_premium_checkin_relative_today" to emptyList()
            status == PremiumCheckInStatus.OVERDUE -> {
                val daysLate = daysUntil.unaryMinus().coerceAtLeast(1)
                "home_premium_checkin_relative_overdue" to listOf(daysLate.toString())
            }
            daysUntil == 1L -> "home_premium_checkin_relative_tomorrow" to emptyList()
            daysUntil > 1L -> "home_premium_checkin_relative_in_days" to listOf(daysUntil.toString())
            else -> "home_premium_checkin_relative_today" to emptyList()
        }

        return PremiumCheckInState(
            status = status,
            checkInDateLabel = checkInDate.format(dateFormatter),
            relativeLabelKey = relative.first,
            relativeLabelArgs = relative.second,
            reminderEnabled = reminderEnabled,
            automationEnabled = automationEnabled,
            isDue = isDue
        )
    }

    private fun PremiumGoalPaydaySplit.toState(
        goals: List<Goal>,
        preferences: UserPreferences
    ): GoalPaydaySplitCardState {
        val currencyFormat = currencyFormat(preferences)
        val goalsById = goals.associateBy { it.id }
        val visibleItems = items.take(SPLIT_VISIBLE_GOAL_COUNT).mapNotNull { item ->
            val goal = goalsById[item.goalId] ?: return@mapNotNull null
            GoalPaydaySplitItemState(
                goalId = goal.id,
                goalName = goal.name,
                goalNameKey = goal.defaultNameKey,
                amountLabel = currencyFormat.format(item.amount),
                shareLabel = LocalizedAmountFormatter.formatPercentage(item.sharePercent, preferences.locale)
            )
        }

        return GoalPaydaySplitCardState(
            totalMoveLabel = currencyFormat.format(totalMove),
            goalCount = items.size,
            visibleItems = visibleItems,
            hiddenGoalCount = (items.size - visibleItems.size).coerceAtLeast(0)
        )
    }

    private fun PaydayAdjustmentRecommendation.toState(preferences: UserPreferences): PaydayAdjustmentRecommendationState {
        val currencyFormat = currencyFormat(preferences)
        return PaydayAdjustmentRecommendationState(
            direction = direction,
            analyzedReviewCount = analyzedReviewCount,
            currentFlexibleSpendLabel = currencyFormat.format(currentFlexibleSpend),
            recommendedFlexibleSpendLabel = currencyFormat.format(recommendedFlexibleSpend),
            currentPriorityContributionLabel = currencyFormat.format(currentPriorityContribution),
            recommendedPriorityContributionLabel = currencyFormat.format(recommendedPriorityContribution),
            adjustmentAmountLabel = currencyFormat.format(adjustmentAmount),
            confidencePercent = confidencePercent,
            isApplyable = isApplyable
        )
    }

    private fun PaydayAdjustmentRecommendationState?.toSmartAdjustmentControlState(
        hasPlan: Boolean,
        messageKey: String?
    ): SmartAdjustmentControlState {
        if (this == null) {
            return SmartAdjustmentControlState(
                hasPlan = hasPlan,
                lastActionMessageKey = messageKey
            )
        }

        return SmartAdjustmentControlState(
            hasPlan = hasPlan,
            hasRecommendation = true,
            isApplyable = isApplyable,
            direction = direction,
            currentFlexibleSpendLabel = currentFlexibleSpendLabel,
            recommendedFlexibleSpendLabel = recommendedFlexibleSpendLabel,
            currentPriorityContributionLabel = currentPriorityContributionLabel,
            recommendedPriorityContributionLabel = recommendedPriorityContributionLabel,
            adjustmentAmountLabel = adjustmentAmountLabel,
            confidencePercent = confidencePercent,
            analyzedReviewCount = analyzedReviewCount,
            lastActionMessageKey = messageKey
        )
    }

    private fun ManualReview.toState(): ReviewHistoryItemState {
        val currencyFormat = currencyFormat(currentPreferences)
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM", currentPreferences.locale)
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", currentPreferences.locale)
        val flexTarget = plannedFlexibleSpend ?: BigDecimal.ZERO
        val flexDelta = actualFlexibleSpend.subtract(flexTarget)
        val goalTarget = plannedGoalContribution ?: BigDecimal.ZERO
        val goalDelta = actualGoalContribution.subtract(goalTarget)

        return ReviewHistoryItemState(
            id = id,
            dateLabel = createdAt.format(dateFormatter),
            monthLabel = createdAt.format(monthFormatter),
            flexibleSpendLabel = currencyFormat.format(actualFlexibleSpend),
            plannedFlexibleLabel = currencyFormat.format(flexTarget),
            editableFlexibleSpend = LocalizedAmountFormatter.formatEditableAmount(actualFlexibleSpend, currentPreferences.locale),
            goalContributionLabel = currencyFormat.format(actualGoalContribution),
            plannedGoalLabel = currencyFormat.format(goalTarget),
            editableGoalContribution = LocalizedAmountFormatter.formatEditableAmount(actualGoalContribution, currentPreferences.locale),
            flexibleDeltaLabel = (if (flexDelta > BigDecimal.ZERO) "+" else "") + currencyFormat.format(flexDelta),
            goalDeltaLabel = (if (goalDelta > BigDecimal.ZERO) "+" else "") + currencyFormat.format(goalDelta),
            isPositive = flexDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO
        )
    }

    private fun sanitizeNumber(value: String): String =
        LocalizedAmountFormatter.sanitizeAmountInput(value, currentPreferences.locale)

    private fun currencyFormat(preferences: UserPreferences): NumberFormat {
        return NumberFormat.getCurrencyInstance(preferences.locale).apply {
            currency = preferences.currency
        }
    }

    private fun currentPricingStrategy() = resolvePricingStrategyUseCase.execute(currentPreferences.locale)

    private fun reviewRangeMax(value: BigDecimal): Float {
        val doubled = value.multiply(BigDecimal("2")).toFloat()
        return doubled.coerceAtLeast(100f)
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val SPLIT_VISIBLE_GOAL_COUNT = 3
    }
    }

private val Goal.defaultNameKey: String?
    get() = when {
        name.isBlank() -> when (type) {
            pt.ms.myshare.domain.model.GoalType.EMERGENCY_FUND -> "goal_default_emergency_fund"
            pt.ms.myshare.domain.model.GoalType.INVEST_TARGET -> "goal_default_investing_base"
            pt.ms.myshare.domain.model.GoalType.CUSTOM -> "goal_default_name"
        }
        name.equals("Emergency fund", ignoreCase = true) -> "goal_default_emergency_fund"
        name.equals("Investing base", ignoreCase = true) -> "goal_default_investing_base"
        name.equals("Cash buffer", ignoreCase = true) -> "goal_default_cash_buffer"
        name.equals("Shared safety net", ignoreCase = true) -> "goal_default_shared_safety_net"
        else -> null
    }

private val pt.ms.myshare.domain.model.PaydayRuleType.labelKey: String
    get() = when (this) {
        pt.ms.myshare.domain.model.PaydayRuleType.SAVINGS -> "rule_type_savings"
        pt.ms.myshare.domain.model.PaydayRuleType.INVESTING -> "rule_type_investing"
        pt.ms.myshare.domain.model.PaydayRuleType.CRYPTO -> "rule_type_crypto"
        pt.ms.myshare.domain.model.PaydayRuleType.DEBT -> "rule_type_debt"
        pt.ms.myshare.domain.model.PaydayRuleType.OTHER -> "rule_type_other"
    }

private val pt.ms.myshare.domain.model.PaydayRule.defaultNameKey: String?
    get() = when {
        name.equals("Savings", ignoreCase = true) && type == pt.ms.myshare.domain.model.PaydayRuleType.SAVINGS -> "rule_name_savings"
        name.equals("Investing", ignoreCase = true) && type == pt.ms.myshare.domain.model.PaydayRuleType.INVESTING -> "rule_name_investing"
        name.equals("Crypto", ignoreCase = true) && type == pt.ms.myshare.domain.model.PaydayRuleType.CRYPTO -> "rule_name_crypto"
        else -> null
    }

private val pt.ms.myshare.domain.model.ReminderCadence.labelAtTimeKey: String
    get() = when (this) {
        pt.ms.myshare.domain.model.ReminderCadence.PAYDAY -> "home_more_reminder_payday_at_time"
        pt.ms.myshare.domain.model.ReminderCadence.WEEKLY_REVIEW -> "home_more_reminder_weekly_at_time"
    }
