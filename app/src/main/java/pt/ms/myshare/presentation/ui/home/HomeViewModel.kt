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
import pt.ms.myshare.domain.model.LegacyPremiumGrantStatus
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendation
import pt.ms.myshare.domain.model.PaydayCountdownCue
import pt.ms.myshare.domain.model.PaydayReadiness
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PremiumCheckInPlan
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.PremiumAdjustmentRecord
import pt.ms.myshare.domain.model.PremiumAdjustmentStatus
import pt.ms.myshare.domain.model.PremiumGoalPaydaySplit
import pt.ms.myshare.domain.model.PremiumReviewCoachingMetric
import pt.ms.myshare.domain.model.PremiumReviewCoachingMetricType
import pt.ms.myshare.domain.model.PremiumReviewCoachingSummary
import pt.ms.myshare.domain.model.PremiumReviewMomentum
import pt.ms.myshare.domain.model.PremiumRulePaydayMix
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoogleAccountConnectionMode
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.model.User
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.AppReviewPromptRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.FirstRunExperienceRepository
import pt.ms.myshare.domain.repository.LegacyPremiumGrantRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.ProductConfigRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.AdjustGoalProgressForReviewCorrectionUseCase
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreatePaydayAdjustmentRecommendationUseCase
import pt.ms.myshare.domain.use_case.CreatePaydayCountdownCueUseCase
import pt.ms.myshare.domain.use_case.CreatePaydayReadinessUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumCheckInPlanUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumGoalPaydaySplitUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumRulePaydayMixUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumReviewCoachingSummaryUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumReviewMomentumUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.EnforcePremiumDowngradeUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.domain.use_case.ResolveAppReviewPromptEligibilityUseCase
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
import pt.ms.myshare.utils.logs.FirebasePerformanceUtils
import pt.ms.myshare.utils.logs.FirebasePerformanceUtils.putMetricSafely
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
    private val legacyPremiumGrantRepository: LegacyPremiumGrantRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appReviewPromptRepository: AppReviewPromptRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val createPaydayCountdownCueUseCase: CreatePaydayCountdownCueUseCase,
    private val createPaydayReadinessUseCase: CreatePaydayReadinessUseCase,
    private val createPaydayAdjustmentRecommendationUseCase: CreatePaydayAdjustmentRecommendationUseCase,
    private val createPremiumCheckInPlanUseCase: CreatePremiumCheckInPlanUseCase,
    private val createPremiumGoalPaydaySplitUseCase: CreatePremiumGoalPaydaySplitUseCase,
    private val createPremiumRulePaydayMixUseCase: CreatePremiumRulePaydayMixUseCase,
    private val createPremiumReviewCoachingSummaryUseCase: CreatePremiumReviewCoachingSummaryUseCase,
    private val createPremiumReviewMomentumUseCase: CreatePremiumReviewMomentumUseCase,
    private val createReviewInsightUseCase: CreateReviewInsightUseCase,
    private val enforcePremiumDowngradeUseCase: EnforcePremiumDowngradeUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase,
    private val resolveAppReviewPromptEligibilityUseCase: ResolveAppReviewPromptEligibilityUseCase,
    private val getReviewHistoryUseCase: GetReviewHistoryUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val adjustGoalProgressForReviewCorrectionUseCase: AdjustGoalProgressForReviewCorrectionUseCase,
    private val getPerformanceStatsUseCase: GetPerformanceStatsUseCase,
    private val getCoachingInsightsUseCase: GetCoachingInsightsUseCase,
    private val reminderWorkScheduler: ReminderWorkScheduler,
    private val userLocaleManager: UserLocaleManager,
    private val productConfigRepository: ProductConfigRepository,
    private val firstRunExperienceRepository: FirstRunExperienceRepository
) : ViewModel() {

    private val uiState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = uiState.asStateFlow()

    private var availableStoreProducts: List<pt.ms.myshare.domain.model.StoreProduct> = emptyList()
    private var currentPlan: SalaryPlan? = null
    private var currentReviews: List<ManualReview> = emptyList()
    private var currentPaydayRecommendation: PaydayAdjustmentRecommendation? = null
    private var paydayRecommendationRollback: PaydayRecommendationRollback? = null
    private var nextReviewSavedEventId = 0L
    private var nextAppReviewRequestEventId = 0L
    private var legacyPremiumGrantViewedLogged = false
    private var isFounderOfferBillingFlowPending = false
    private var currentProductConfig = ProductExperienceConfig()
    private var lastLoggedPaydayReadinessStatus: PaydayReadinessStatus? = null
    private var lastLoggedPaydayCountdownCueSignature: String? = null
    private var lastLoggedReviewSavedMilestoneTotalReviews: Int? = null

    private var currentPreferences = userPreferencesRepository.loadPreferences()

    fun onToggleAutomation(enabled: Boolean) {
        viewModelScope.launch {
            plannerRepository.saveAutomationEnabled(enabled)
            FirebaseUtils.logEvent(if (enabled) "automation_enabled" else "automation_disabled")
            Timber.tag(TAG).d("Automation toggled: %s", enabled)
        }
    }

    fun logReviewSavedMilestoneViewed(milestone: ReviewSavedMilestoneState) {
        if (lastLoggedReviewSavedMilestoneTotalReviews == milestone.totalReviews) return

        lastLoggedReviewSavedMilestoneTotalReviews = milestone.totalReviews
        FirebaseUtils.logEvent("review_saved_milestone_viewed", android.os.Bundle().apply {
            putInt("total_reviews", milestone.totalReviews)
            putString("first_payday_cycle", milestone.isFirstPaydayCycle.toString())
            putString("has_next_move_preview", milestone.hasPremiumNextMovePreview.toString())
        })
        Timber.tag(TAG).d(
            "Review saved milestone viewed. totalReviews=%d firstCycle=%s hasNextMovePreview=%s",
            milestone.totalReviews,
            milestone.isFirstPaydayCycle,
            milestone.hasPremiumNextMovePreview
        )
    }

    fun logReviewSavedMilestonePremiumClicked(milestone: ReviewSavedMilestoneState) {
        FirebaseUtils.logEvent("review_saved_milestone_premium_cta_tapped", android.os.Bundle().apply {
            putInt("total_reviews", milestone.totalReviews)
            putString("first_payday_cycle", milestone.isFirstPaydayCycle.toString())
            putString("has_next_move_preview", milestone.hasPremiumNextMovePreview.toString())
        })
        Timber.tag(TAG).d("Review saved milestone Premium CTA tapped totalReviews=%d", milestone.totalReviews)
    }

    fun logPaydayCountdownCueReviewTapped(cue: PaydayCountdownCueState) {
        FirebaseUtils.logEvent("payday_countdown_review_cue_tapped", android.os.Bundle().apply {
            putLong("days_until_payday", cue.daysUntilPayday)
            putString("action", cue.action.name.lowercase(Locale.US))
        })
        Timber.tag(TAG).d(
            "Payday countdown review cue tapped. daysUntilPayday=%d action=%s",
            cue.daysUntilPayday,
            cue.action
        )
    }

    init {
        viewModelScope.launch {
            plannerRepository.syncFromFirestore()
            userPreferencesRepository.syncFromFirestore()
            legacyPremiumGrantRepository.refreshAvailability()
            productConfigRepository.refresh()
        }
        userLocaleManager.apply(currentPreferences)
        observeProducts()
        observeBillingPurchaseEvents()
        observeEntitlementLifecycle()
        observeLegacyPremiumGrant()
        observePlannerData()
        observeReviewHistory()
        restorePendingCoachMarks()
        FirebaseUtils.logScreen("home")
    }

    private fun restorePendingCoachMarks() {
        if (!firstRunExperienceRepository.isHomeCoachMarksPending()) return
        uiState.update {
            it.copy(
                selectedDestination = HomeCoachMarkStep.PLAN.destination,
                coachMarks = HomeCoachMarksState(isVisible = true, currentStep = HomeCoachMarkStep.PLAN)
            )
        }
        FirebaseUtils.logEvent("home_coach_marks_shown")
        Timber.tag(TAG).d("Home coach marks shown")
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

    private fun observeLegacyPremiumGrant() {
        viewModelScope.launch {
            legacyPremiumGrantRepository.grantState.collect { grantState ->
                val founderOfferEnabled = currentProductConfig.founderOfferEnabled
                val visibleGrantState = grantState.takeIf { founderOfferEnabled }
                    ?: pt.ms.myshare.domain.model.LegacyPremiumGrantState()
                if (
                    founderOfferEnabled &&
                    !legacyPremiumGrantViewedLogged &&
                    grantState.status == LegacyPremiumGrantStatus.Eligible
                ) {
                    legacyPremiumGrantViewedLogged = true
                    FirebaseUtils.logEvent("legacy_premium_grant_viewed")
                    Timber.tag(TAG).d("Legacy Premium grant viewed")
                }
                uiState.update { current ->
                    current.copy(
                        moreCard = current.moreCard.copy(
                            legacyPremiumGrant = visibleGrantState
                        )
                    )
                }
            }
        }
    }

    private fun observeBillingPurchaseEvents() {
        viewModelScope.launch {
            entitlementRepository.purchaseEvents.collect { event ->
                val messageKey = BillingStatusMessageMapper.fromPurchaseEvent(event)
                var shouldLogAccountPrompt = false
                if (event == BillingPurchaseEvent.Completed) {
                    plannerRepository.saveAutomationEnabled(true)
                    Timber.tag(TAG).d("Premium watch enabled after completed purchase")
                    FirebaseUtils.logEvent("premium_watch_enabled_after_purchase")
                    if (isFounderOfferBillingFlowPending) {
                        isFounderOfferBillingFlowPending = false
                        legacyPremiumGrantRepository.markFounderOfferClaimed()
                        FirebaseUtils.logEvent("legacy_premium_founder_offer_completed")
                        Timber.tag(TAG).d("Legacy Premium founder offer completed through Play")
                    }
                } else if (isFounderOfferBillingFlowPending &&
                    (event == BillingPurchaseEvent.Canceled || event is BillingPurchaseEvent.Failed)
                ) {
                    isFounderOfferBillingFlowPending = false
                    legacyPremiumGrantRepository.releaseFounderOffer()
                    FirebaseUtils.logEvent("legacy_premium_founder_offer_released")
                    Timber.tag(TAG).d("Legacy Premium founder offer reservation released after Play event")
                }
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
                getPerformanceStatsUseCase.execute(),
                plannerRepository.observePremiumAdjustmentRecords()
            ) { reminder, automation, history, stats, adjustments ->
                PlannerSignals(
                    reminder = reminder,
                    automation = automation,
                    history = history,
                    stats = stats,
                    adjustments = adjustments
                )
            }

            val plannerFlow = combine(plannerCoreFlow, plannerSignalsFlow) { core, signals ->
                val coaching = if (core.plan != null) {
                    getCoachingInsightsUseCase.execute(core.plan, signals.history)
                } else {
                    emptyList()
                }
                val coachingSummary = core.plan?.let {
                    createPremiumReviewCoachingSummaryUseCase.execute(it, signals.history)
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
                    coachingSummary = coachingSummary,
                    coachingInsights = coaching,
                    performanceTrend = trend,
                    adjustments = signals.adjustments
                )
            }

            val productFlow = combine(
                entitlementRepository.isPro,
                entitlementRepository.availableProducts,
                authRepository.currentUser,
                userPreferencesRepository.observePreferences(),
                productConfigRepository.config
            ) { isPremium, products, user, preferences, productConfig ->
                ProductInputs(
                    isPremium = isPremium,
                    products = products,
                    user = user,
                    preferences = preferences,
                    productConfig = productConfig
                )
            }

            combine(plannerFlow, productFlow) { planner, productInputs ->
                val currentState = uiState.value
                val isPremium = productInputs.isPremium
                val products = productInputs.products
                val user = productInputs.user
                val preferences = productInputs.preferences
                val productConfig = productInputs.productConfig
                currentProductConfig = productConfig
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
                        reminderConfiguration = if (productConfig.premiumRemindersEnabled) {
                            reminder
                        } else {
                            reminder.copy(enabled = false)
                        },
                        automationEnabled = automation && isPremium
                    ).toState(preferences)
                }

                val emptyMessage = if (updatedPlan == null) "home_empty_build_plan_first" else null
                val primaryGoal = goals.firstOrNull()
                val planCard = updatedPlan?.let {
                    buildPlanCard(
                        plan = it,
                        goalAmount = primaryGoal?.targetAmount ?: BigDecimal.ZERO,
                        latestReview = latestReview,
                        preferences = preferences
                    )
                }
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
                val rulePaydayMix = if (isPremium && updatedPlan != null) {
                    createPremiumRulePaydayMixUseCase
                        .execute(updatedPlan)
                        ?.toState(currentRules, preferences)
                } else {
                    null
                }
                rulePaydayMix?.let {
                    Timber.tag(TAG).d(
                        "Premium rule payday mix ready. ruleCount=%d visible=%d hidden=%d",
                        it.ruleCount,
                        it.visibleItems.size,
                        it.hiddenRuleCount
                    )
                }
                val latestAdjustment = planner.adjustments.maxByOrNull { it.createdAt }
                val adjustmentHistory = planner.adjustments
                    .sortedByDescending { it.createdAt }
                    .take(PREMIUM_ADJUSTMENT_HISTORY_LIMIT)
                val latestAppliedAdjustment = planner.adjustments
                    .filter { it.status == PremiumAdjustmentStatus.APPLIED }
                    .maxByOrNull { it.createdAt }
                val latestAdjustedRuleIds = latestAppliedAdjustment?.affectedRuleIds.orEmpty().toSet()
                val goalCards = goals.mapIndexed { index, goal ->
                    buildGoalCard(goal, updatedPlan, preferences).copy(
                        isLockedByEntitlement = !isPremium && index > 0
                    )
                }
                val ruleCards = currentRules.mapIndexed { index, rule ->
                    buildRuleCard(rule, preferences).copy(
                        isAdjustedByPremium = isPremium && latestAdjustedRuleIds.contains(rule.id),
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
                val coachingSummary = if (isPremium) {
                    planner.coachingSummary?.toState(preferences)
                } else {
                    null
                }
                val premiumMomentum = if (isPremium) {
                    createPremiumReviewMomentumUseCase
                        .execute(
                            totalReviews = performanceStats.totalReviews,
                            currentStreak = performanceStats.currentStreak
                        )
                        ?.toState()
                } else {
                    null
                }
                coachingSummary?.let {
                    Timber.tag(TAG).d(
                        "Premium review coaching summary ready. status=%s metrics=%d",
                        it.status,
                        it.metrics.size
                    )
                }
                
                val monthlyProduct = products.find { it.productId == PremiumSubscriptionProducts.MONTHLY_ID }
                val annualProduct = products.find { it.productId == PremiumSubscriptionProducts.ANNUAL_ID }
                val annualComparison = SubscriptionSavingsFormatter.formatAnnualComparison(
                    monthlyProduct = monthlyProduct,
                    annualProduct = annualProduct,
                    locale = locale
                )
                val currentMore = currentState.moreCard
                val remoteDefaultPlan = productConfig.paywallDefaultPlan.resolve(pricingStrategy.heroPlan)
                val selectedBillingPlan = if (currentMore.hasUserSelectedBillingPlan) {
                    currentMore.selectedBillingPlan
                } else {
                    remoteDefaultPlan
                }
                val selectedProductAvailable = when (selectedBillingPlan) {
                    BillingPlan.MONTHLY -> monthlyProduct != null
                    BillingPlan.ANNUAL -> annualProduct != null
                }
                val shouldClearUnavailableMessage =
                    currentMore.billingMessage == BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE && selectedProductAvailable

                val paydayRecommendationState = paydayRecommendation?.toState(preferences)
                val premiumReviewResult = if (isPremium && latestReview != null) {
                    PremiumReviewResultState(
                        savedReviewDateLabel = latestReview.createdAt.format(
                            DateTimeFormatter.ofPattern("d MMM", preferences.locale)
                        ),
                        totalReviews = planner.reviewHistory.size,
                        coachingSummary = coachingSummary,
                        recommendation = paydayRecommendationState
                    )
                } else {
                    null
                }
                val reviewSavedMilestone = latestReview?.let {
                    ReviewSavedMilestoneState(
                        savedReviewDateLabel = it.createdAt.format(
                            DateTimeFormatter.ofPattern("d MMM", preferences.locale)
                        ),
                        totalReviews = planner.reviewHistory.size,
                        isFirstPaydayCycle = planner.reviewHistory.size == 1,
                        hasPremiumNextMovePreview = paydayRecommendationState != null
                    )
                }
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
                    hasUserSelectedBillingPlan = currentMore.hasUserSelectedBillingPlan,
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
                    adjustmentMemory = latestAdjustment?.takeIf { isPremium }?.toMemoryState(preferences),
                    adjustmentHistory = adjustmentHistory
                        .takeIf { isPremium }
                        ?.map { it.toMemoryState(preferences) }
                        .orEmpty(),
                    premiumCheckIn = premiumCheckIn.takeIf { isPremium },
                    legacyPremiumGrant = currentMore.legacyPremiumGrant.takeIf {
                        productConfig.founderOfferEnabled
                    } ?: pt.ms.myshare.domain.model.LegacyPremiumGrantState(),
                    error = currentMore.error.takeUnless { shouldClearUnavailableMessage }
                )
                HomeState(
                    selectedDestination = currentState.selectedDestination,
                    coachMarks = currentState.coachMarks,
                    plan = updatedPlan,
                    planCard = planCard,
                    goalPaydaySplit = goalPaydaySplit,
                    rulePaydayMix = rulePaydayMix,
                    goals = goalCards,
                    rules = ruleCards,
                    performanceStats = performanceStats,
                    reviewCard = reviewCard.copy(
                        reviewSavedMilestone = reviewSavedMilestone,
                        premiumReviewResult = premiumReviewResult,
                        coachingSummary = coachingSummary,
                        premiumMomentum = premiumMomentum,
                        coachingInsights = coachingInsights,
                        paydayRecommendation = paydayRecommendationState,
                        premiumCheckIn = premiumCheckIn.takeIf { isPremium },
                        premiumProofVariant = productConfig.premiumProofVariant,
                        recommendationMessageKey = recommendationMessageKey
                    ),
                    reviewHistory = planner.reviewHistory.asReversed().map { it.toState() },
                    moreCard = moreCard,
                    reviewSavedEventId = currentState.reviewSavedEventId,
                    appReviewRequestEventId = currentState.appReviewRequestEventId,
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
        val coachingSummary: PremiumReviewCoachingSummary?,
        val coachingInsights: List<ReviewInsight>,
        val performanceTrend: List<Float>,
        val adjustments: List<PremiumAdjustmentRecord>
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
        val stats: PerformanceStats,
        val adjustments: List<PremiumAdjustmentRecord>
    )

    private data class ProductInputs(
        val isPremium: Boolean,
        val products: List<StoreProduct>,
        val user: User?,
        val preferences: UserPreferences,
        val productConfig: ProductExperienceConfig
    )

    private data class PaydayRecommendationRollback(
        val previousRules: List<PaydayRule>,
        val affectedRuleIds: Set<String>,
        val adjustmentRecordId: String
    )

    private fun buildPlanCard(
        plan: pt.ms.myshare.domain.model.SalaryPlan,
        goalAmount: BigDecimal,
        latestReview: ManualReview?,
        preferences: UserPreferences
    ): HomePlanCardState {
        val preview = calculatePlanPreviewUseCase.execute(plan, goalAmount)
        val readinessModel = createPaydayReadinessUseCase.execute(preview)
        logPaydayReadinessIfChanged(readinessModel)
        val paydayCueModel = createPaydayCountdownCueUseCase.execute(preview, readinessModel, latestReview)
        logPaydayCountdownCueIfChanged(paydayCueModel)
        val paydayCue = paydayCueModel.toState()
        val readiness = readinessModel.toState()
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
            readiness = readiness,
            paydayCue = paydayCue,
            nextPaydayKey = "home_plan_next_payday",
            nextPaydayArgs = listOf(preview.nextPayday.format(dateFormatter))
        )
    }

    private fun logPaydayReadinessIfChanged(readiness: PaydayReadiness) {
        if (lastLoggedPaydayReadinessStatus == readiness.status) return

        lastLoggedPaydayReadinessStatus = readiness.status
        FirebaseUtils.logEvent("payday_readiness_viewed", android.os.Bundle().apply {
            putString("status", readiness.status.name.lowercase(Locale.US))
            putInt("completed_missions", readiness.completedMissions)
            putInt("total_missions", readiness.totalMissions)
            putString("next_action", readiness.nextAction?.name?.lowercase(Locale.US) ?: "none")
        })
        Timber.tag(TAG).d(
            "Payday readiness viewed. status=%s completed=%d total=%d nextAction=%s",
            readiness.status,
            readiness.completedMissions,
            readiness.totalMissions,
            readiness.nextAction
        )
    }

    private fun logPaydayCountdownCueIfChanged(cue: PaydayCountdownCue) {
        val signature = "${cue.nextPayday}:${cue.daysUntilPayday}:${cue.action}"
        if (lastLoggedPaydayCountdownCueSignature == signature) return

        lastLoggedPaydayCountdownCueSignature = signature
        FirebaseUtils.logEvent("payday_countdown_cue_viewed", android.os.Bundle().apply {
            putLong("days_until_payday", cue.daysUntilPayday)
            putString("action", cue.action.name.lowercase(Locale.US))
            putString("next_payday", cue.nextPayday.toString())
        })
        Timber.tag(TAG).d(
            "Payday countdown cue viewed. nextPayday=%s daysUntilPayday=%d action=%s",
            cue.nextPayday,
            cue.daysUntilPayday,
            cue.action
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

    fun advanceHomeCoachMark() {
        val currentStep = uiState.value.coachMarks.currentStep
        if (currentStep.isLast) {
            completeHomeCoachMarks()
            return
        }
        val nextStep = HomeCoachMarkStep.entries[currentStep.ordinal + 1]
        uiState.update {
            it.copy(
                selectedDestination = nextStep.destination,
                coachMarks = it.coachMarks.copy(currentStep = nextStep)
            )
        }
        FirebaseUtils.logEvent("home_coach_mark_advanced", android.os.Bundle().apply {
            putString("step", nextStep.name.lowercase(Locale.US))
            putInt("step_index", nextStep.stepNumber)
        })
        Timber.tag(TAG).d("Home coach mark advanced step=%s", nextStep.name)
    }

    fun completeHomeCoachMarks() {
        clearHomeCoachMarks(completed = true)
    }

    fun dismissHomeCoachMarks() {
        clearHomeCoachMarks(completed = false)
    }

    private fun clearHomeCoachMarks(completed: Boolean) {
        viewModelScope.launch {
            firstRunExperienceRepository.setHomeCoachMarksPending(false)
            uiState.update { it.copy(coachMarks = HomeCoachMarksState()) }
            FirebaseUtils.logEvent(
                if (completed) "home_coach_marks_completed" else "home_coach_marks_dismissed"
            )
            Timber.tag(TAG).d("Home coach marks cleared completed=%s", completed)
        }
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
            FirebasePerformanceUtils.traceSuspend(
                name = "weekly_checkin_save",
                attributes = mapOf(
                    "is_premium" to uiState.value.moreCard.isPremium.toString()
                )
            ) { trace ->
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

                val historyAfterSave = currentReviews
                    .filterNot { it.id == review.id } + review
                val recommendationAfterSave = createPaydayAdjustmentRecommendationUseCase.execute(
                    plan.copy(rules = plannerRepository.loadRules()),
                    historyAfterSave
                )
                val isPremium = uiState.value.moreCard.isPremium
                trace?.putMetricSafely("review_count_after", historyAfterSave.size.toLong())
                trace?.putMetricSafely("has_recommendation", if (recommendationAfterSave != null) 1L else 0L)
                FirebaseUtils.logEvent("weekly_checkin_completed", android.os.Bundle().apply {
                    putString("is_premium", isPremium.toString())
                    putInt("review_count", historyAfterSave.size)
                    putString("has_recommendation", (recommendationAfterSave != null).toString())
                    putString(
                        "recommendation_direction",
                        recommendationAfterSave?.direction?.name?.lowercase(Locale.US) ?: "none"
                    )
                    putString("recommendation_applyable", (recommendationAfterSave?.isApplyable == true).toString())
                })
                recommendationAfterSave?.let { recommendation ->
                    FirebaseUtils.logEvent("premium_review_recommendation_generated", android.os.Bundle().apply {
                        putString("is_premium", isPremium.toString())
                        putString("direction", recommendation.direction.name.lowercase(Locale.US))
                        putInt("review_count", recommendation.analyzedReviewCount)
                        putString("adjustment_amount", recommendation.adjustmentAmount.toPlainString())
                        putString("is_applyable", recommendation.isApplyable.toString())
                    })
                    if (isPremium) {
                        FirebaseUtils.logEvent("premium_review_result_ready", android.os.Bundle().apply {
                            putString("direction", recommendation.direction.name.lowercase(Locale.US))
                            putInt("review_count", recommendation.analyzedReviewCount)
                            putString("is_applyable", recommendation.isApplyable.toString())
                        })
                    } else {
                        FirebaseUtils.logEvent("post_review_premium_proof_ready", android.os.Bundle().apply {
                            putString("premium_proof_variant", currentProductConfig.premiumProofVariant.remoteValue)
                            putString("direction", recommendation.direction.name.lowercase(Locale.US))
                            putInt("review_count", recommendation.analyzedReviewCount)
                            putString("is_applyable", recommendation.isApplyable.toString())
                        })
                    }
                }
                recordAppReviewPositiveAction(historyAfterSave.size)
                Timber.tag(TAG).d("Review saved with snapshots. planFlex=%s planPriority=%s", preview.flexibleSpendPerPayday, preview.priorityContributionPerPayday)
            }
        }
    }

    private suspend fun recordAppReviewPositiveAction(reviewCount: Int) {
        val promptState = appReviewPromptRepository.recordPositiveAction()
        val nowMillis = System.currentTimeMillis()
        val eligible = resolveAppReviewPromptEligibilityUseCase.execute(promptState, nowMillis)
        FirebaseUtils.logEvent("app_review_prompt_eligibility_checked", android.os.Bundle().apply {
            putInt("positive_action_count", promptState.positiveActionCount)
            putInt("review_count", reviewCount)
            putString("eligible", eligible.toString())
        })
        Timber.tag(TAG).d(
            "App review prompt eligibility checked. positiveActions=%d reviewCount=%d eligible=%s",
            promptState.positiveActionCount,
            reviewCount,
            eligible
        )
        if (eligible) {
            nextAppReviewRequestEventId += 1
            uiState.update { it.copy(appReviewRequestEventId = nextAppReviewRequestEventId) }
            FirebaseUtils.logEvent("app_review_prompt_eligibility_reached", android.os.Bundle().apply {
                putInt("positive_action_count", promptState.positiveActionCount)
                putInt("review_count", reviewCount)
            })
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
            FirebaseUtils.logEvent("premium_payday_adjustment_apply_blocked", android.os.Bundle().apply {
                putString("reason", "not_premium")
                putString("direction", recommendation.direction.name.lowercase(Locale.US))
                putInt("review_count", recommendation.analyzedReviewCount)
            })
            return false
        }

        val rulesBeforeApply = plannerRepository.loadRules()
        val suggestedRules = recommendation.suggestedRules
        val adjustmentRecord = recommendation.toPremiumAdjustmentRecord(
            affectedRuleIds = suggestedRules.map { it.id },
            reviewId = currentReviews.lastOrNull()?.id
        )
        paydayRecommendationRollback = PaydayRecommendationRollback(
            previousRules = rulesBeforeApply,
            affectedRuleIds = suggestedRules.map { it.id }.toSet(),
            adjustmentRecordId = adjustmentRecord.id
        )

        viewModelScope.launch {
            try {
                suggestedRules.forEach { rule ->
                    plannerRepository.saveRule(rule)
                }
                plannerRepository.savePremiumAdjustmentRecord(adjustmentRecord)
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
                plannerRepository.loadPremiumAdjustmentRecords()
                    .firstOrNull { it.id == rollback.adjustmentRecordId }
                    ?.let { record ->
                        plannerRepository.savePremiumAdjustmentRecord(
                            record.copy(
                                status = PremiumAdjustmentStatus.UNDONE,
                                undoneAt = LocalDate.now()
                            )
                        )
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

    fun markInAppReviewRequested(eventId: Long, launched: Boolean) {
        if (eventId <= 0L || uiState.value.appReviewRequestEventId != eventId) return
        viewModelScope.launch {
            appReviewPromptRepository.markInAppReviewRequested(System.currentTimeMillis())
            FirebaseUtils.logEvent("app_review_prompt_requested", android.os.Bundle().apply {
                putString("launched", launched.toString())
            })
            Timber.tag(TAG).d("App review prompt requested. eventId=%d launched=%s", eventId, launched)
            uiState.update { current ->
                if (current.appReviewRequestEventId == eventId) {
                    current.copy(appReviewRequestEventId = 0L)
                } else {
                    current
                }
            }
        }
    }

    fun openPlayStoreRateEntry() {
        viewModelScope.launch {
            appReviewPromptRepository.markPlayStoreRateOpened()
            FirebaseUtils.logEvent("play_store_rate_link_opened")
            Timber.tag(TAG).d("Play Store rate link opened from More tab")
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
        uiState.update {
            it.copy(
                moreCard = it.moreCard.copy(
                    selectedBillingPlan = plan,
                    hasUserSelectedBillingPlan = true,
                    billingMessage = null,
                    error = null
                )
            )
        }
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
            val updatedSelectedPlan = if (current.moreCard.hasUserSelectedBillingPlan) {
                current.moreCard.selectedBillingPlan
            } else {
                currentProductConfig.paywallDefaultPlan.resolve(updatedPricing.heroPlan)
            }

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
                onSuccess = { connection ->
                    val user = connection.user
                    val syncSucceeded = if (connection.mode == GoogleAccountConnectionMode.SignedInToExistingAccount) {
                        val syncResult = plannerRepository.preserveLocalStateForAuthenticatedUser()
                        if (syncResult.isSuccess) {
                            userPreferencesRepository.syncToFirestoreIfAuthenticated()
                            entitlementRepository.restorePurchases()
                            Timber.tag(TAG).d(
                                "Google account collision merged local state previousUid=%s currentUid=%s",
                                connection.previousUserId,
                                connection.currentUserId
                            )
                        }
                        syncResult.isSuccess
                    } else {
                        plannerRepository.syncLocalStateIfAuthenticated()
                        userPreferencesRepository.syncFromFirestore()
                        true
                    }
                    if (!syncSucceeded) {
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
                        Timber.tag(TAG).e("Google account connected but local state merge failed")
                        return@fold
                    }
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
        logPremiumGateEvent("paywall_viewed", gate)
    }

    fun logPremiumGateUpgradeClicked(gate: HomePremiumGate) {
        logPremiumGateEvent("premium_gate_upgrade_clicked", gate)
    }

    fun logSubscriptionRetentionViewed() {
        FirebaseUtils.logEvent("subscription_retention_viewed", android.os.Bundle().apply {
            putString("source", "more_manage_subscription")
            putString("offer_id", PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID)
        })
        Timber.tag(TAG).d("Subscription retention viewed")
    }

    fun logSubscriptionRetentionContinue() {
        FirebaseUtils.logEvent("subscription_retention_continue_play", android.os.Bundle().apply {
            putString("source", "more_manage_subscription")
            putString("offer_id", PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID)
        })
        Timber.tag(TAG).d("Subscription retention continued to Google Play")
    }

    fun logReminderSettingsOpened(source: String) {
        FirebaseUtils.logEvent("reminder_settings_opened", android.os.Bundle().apply {
            putString("source", source)
        })
        Timber.tag(TAG).d("Reminder settings opened source=%s", source)
    }

    fun logReminderPermissionResult(granted: Boolean, source: String) {
        FirebaseUtils.logEvent(if (granted) "reminder_permission_granted" else "reminder_permission_denied", android.os.Bundle().apply {
            putString("source", source)
            putString("surface", "home")
        })
        Timber.tag(TAG).d("Reminder permission result granted=%s source=%s", granted, source)
    }

    fun unlockPremium(activity: android.app.Activity, source: String = "more_inline") {
        val storeProductId = selectedStoreProductId()
        val realProduct = selectedStoreProduct()

        viewModelScope.launch {
            FirebasePerformanceUtils.traceSuspend(
                name = "premium_purchase_launch",
                attributes = mapOf(
                    "source" to source,
                    "billing_plan" to uiState.value.moreCard.selectedBillingPlan.name.lowercase(Locale.US)
                )
            ) { trace ->
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
                    trace?.putMetricSafely("product_available", 1L)
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
                    trace?.putMetricSafely("product_available", 0L)
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
    }

    fun claimSubscriptionSaveOffer(activity: android.app.Activity) {
        val saveOfferProduct = monthlySaveOfferProduct()

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
            if (saveOfferProduct != null) {
                FirebaseUtils.logEvent("subscription_save_offer_started", android.os.Bundle().apply {
                    putString("product_id", saveOfferProduct.productId)
                    putString("offer_id", saveOfferProduct.offerId)
                    putString("offer_tags", saveOfferProduct.offerTags.joinToString(","))
                    putString("source", "subscription_retention")
                })
                val launchResult = entitlementRepository.purchasePlan(activity, saveOfferProduct)
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isBillingActionInProgress = false,
                            billingMessage = BillingStatusMessageMapper.fromLaunchResult(launchResult),
                            error = null
                        )
                    )
                }
                logBillingLaunchResult(launchResult, saveOfferProduct.productId, "subscription_retention")
            } else {
                FirebaseUtils.logEvent("subscription_save_offer_unavailable", android.os.Bundle().apply {
                    putString("product_id", PremiumSubscriptionProducts.MONTHLY_ID)
                    putString("offer_id", PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID)
                    putString("source", "subscription_retention")
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
                Timber.tag(TAG).e(
                    "Cannot launch subscription save offer: offer %s not found for product=%s",
                    PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID,
                    PremiumSubscriptionProducts.MONTHLY_ID
                )
            }
        }
    }

    fun claimLegacyPremiumGrant(activity: android.app.Activity) {
        viewModelScope.launch {
            if (!currentProductConfig.founderOfferEnabled) {
                FirebaseUtils.logEvent("legacy_premium_grant_blocked_by_config")
                Timber.tag(TAG).d("Legacy Premium grant claim blocked by Remote Config")
                return@launch
            }
            FirebaseUtils.logEvent("legacy_premium_grant_claim_started")
            val grantState = legacyPremiumGrantRepository.reserveFounderOffer()
            if (grantState.status == LegacyPremiumGrantStatus.Reserved) {
                val founderProduct = annualFounderOfferProduct()
                    ?: entitlementRepository.refreshProducts().firstOrNull {
                        it.productId == PremiumSubscriptionProducts.ANNUAL_ID && it.isFounderOffer()
                }
                if (founderProduct == null) {
                    legacyPremiumGrantRepository.releaseFounderOffer()
                    val annualProducts = availableStoreProducts.filter {
                        it.productId == PremiumSubscriptionProducts.ANNUAL_ID
                    }
                    val annualOfferIds = annualProducts.mapNotNull { it.offerId }.distinct()
                    FirebaseUtils.logEvent("legacy_premium_founder_offer_unavailable", android.os.Bundle().apply {
                        putString("product_id", PremiumSubscriptionProducts.ANNUAL_ID)
                        putString("offer_id", PremiumSubscriptionProducts.ANNUAL_FOUNDER_OFFER_ID)
                        putInt("available_product_count", availableStoreProducts.size)
                        putInt("annual_offer_count", annualProducts.size)
                        putString("annual_trial_returned", annualProducts.any { it.hasFreeTrial }.toString())
                        putString("annual_offer_ids", annualOfferIds.toTelemetryValue())
                    })
                    FirebaseUtils.logEvent("legacy_founder_offer_missing", android.os.Bundle().apply {
                        putInt("available_product_count", availableStoreProducts.size)
                        putInt("annual_offer_count", annualProducts.size)
                        putString("annual_trial_returned", annualProducts.any { it.hasFreeTrial }.toString())
                        putString("annual_offer_ids", annualOfferIds.toTelemetryValue())
                    })
                    FirebaseUtils.setCrashlyticsKey("legacy_founder_offer_missing", true)
                    FirebaseUtils.setCrashlyticsKey("legacy_founder_available_count", availableStoreProducts.size)
                    FirebaseUtils.setCrashlyticsKey("legacy_founder_annual_offer_ids", annualOfferIds.toTelemetryValue())
                    FirebaseUtils.logCrashlyticsBreadcrumb(
                        "Founder offer unavailable available=${availableStoreProducts.size} annualOffers=${annualOfferIds.toTelemetryValue()}"
                    )
                    uiState.update {
                        it.copy(
                            moreCard = it.moreCard.copy(
                                billingMessage = BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE,
                                error = "more_error_products_not_loaded"
                            )
                        )
                    }
                    Timber.tag(TAG).e("Legacy Premium founder offer product unavailable")
                    return@launch
                }

                FirebaseUtils.logEvent("legacy_premium_founder_offer_started", android.os.Bundle().apply {
                    putString("product_id", founderProduct.productId)
                    putString("offer_id", founderProduct.offerId)
                    putString("offer_tags", founderProduct.offerTags.joinToString(","))
                    putInt("free_trial_days", founderProduct.freeTrialDays ?: 0)
                })
                val launchResult = entitlementRepository.purchasePlan(activity, founderProduct)
                if (launchResult == BillingFlowLaunchResult.Launched) {
                    isFounderOfferBillingFlowPending = true
                    legacyPremiumGrantRepository.markFounderOfferStarted()
                } else {
                    legacyPremiumGrantRepository.releaseFounderOffer()
                }
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            billingMessage = BillingStatusMessageMapper.fromLaunchResult(launchResult),
                            error = null
                        )
                    )
                }
                logBillingLaunchResult(launchResult, founderProduct.productId, "legacy_premium_founder_offer")
            } else if (grantState.status == LegacyPremiumGrantStatus.NotEligible) {
                FirebaseUtils.logEvent("legacy_premium_grant_not_eligible")
            } else if (grantState.status == LegacyPremiumGrantStatus.Error) {
                FirebaseUtils.logEvent("legacy_premium_grant_error")
            }
        }
    }

    fun dismissLegacyPremiumGrant() {
        viewModelScope.launch {
            legacyPremiumGrantRepository.dismissGrant()
            FirebaseUtils.logEvent("legacy_premium_grant_dismissed")
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
            BillingPurchaseEvent.Completed -> {
                FirebaseUtils.logEvent("purchase_completed", android.os.Bundle().apply {
                    putString("source", "home_paywall")
                    putString("billing_plan", uiState.value.moreCard.selectedBillingPlan.name.lowercase(Locale.US))
                    putString("product_id", selectedStoreProductId())
                })
                Timber.tag(TAG).d("Billing purchase completed")
            }
            BillingPurchaseEvent.Pending -> {
                FirebaseUtils.logEvent("purchase_pending", android.os.Bundle().apply {
                    putString("source", "home_paywall")
                    putString("product_id", selectedStoreProductId())
                })
                Timber.tag(TAG).d("Billing purchase pending")
            }
            BillingPurchaseEvent.Canceled -> {
                FirebaseUtils.logEvent("purchase_canceled", android.os.Bundle().apply {
                    putString("source", "home_paywall")
                    putString("product_id", selectedStoreProductId())
                })
                Timber.tag(TAG).d("Billing purchase canceled")
            }
            is BillingPurchaseEvent.Failed -> {
                FirebaseUtils.logEvent("purchase_failed", android.os.Bundle().apply {
                    putString("source", "home_paywall")
                    putString("product_id", selectedStoreProductId())
                    putInt("response_code", event.responseCode)
                })
                Timber.tag(TAG).e(
                    "Billing purchase failed code=%d message=%s",
                    event.responseCode,
                    event.debugMessage
                )
            }
        }
    }

    private fun logPremiumGateEvent(eventName: String, gate: HomePremiumGate) {
        val selectedProduct = selectedStoreProduct()
        FirebaseUtils.logEvent(eventName, android.os.Bundle().apply {
            putString("premium_gate", gate.analyticsName)
            putString("premium_proof_variant", currentProductConfig.premiumProofVariant.remoteValue)
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
        return availableStoreProducts
            .filter { it.productId == storeProductId }
            .filterNot { it.isSpecialOffer() }
            .sortedWith(
                compareByDescending<StoreProduct> { it.hasFreeTrial }
            )
            .firstOrNull()
    }

    private fun monthlySaveOfferProduct(): StoreProduct? {
        return availableStoreProducts.firstOrNull {
            it.productId == PremiumSubscriptionProducts.MONTHLY_ID &&
                it.isSubscriptionSaveOffer()
        }
    }

    private fun annualFounderOfferProduct(): StoreProduct? {
        return availableStoreProducts.firstOrNull {
            it.productId == PremiumSubscriptionProducts.ANNUAL_ID &&
                it.isFounderOffer()
        }
    }

    private fun StoreProduct.isSpecialOffer(): Boolean {
        return isSubscriptionSaveOffer() || isFounderOffer()
    }

    private fun StoreProduct.isSubscriptionSaveOffer(): Boolean {
        return offerId == PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID ||
            PremiumSubscriptionProducts.SAVE_OFFER_TAG in offerTags
    }

    private fun StoreProduct.isFounderOffer(): Boolean {
        return offerId == PremiumSubscriptionProducts.ANNUAL_FOUNDER_OFFER_ID ||
            PremiumSubscriptionProducts.FOUNDER_OFFER_TAG in offerTags ||
            isLegacyFreeYearOffer()
    }

    private fun StoreProduct.isLegacyFreeYearOffer(): Boolean {
        return productId == PremiumSubscriptionProducts.ANNUAL_ID &&
            (freeTrialDays ?: 0) >= LEGACY_FREE_YEAR_TRIAL_DAYS
    }

    private fun List<String>.toTelemetryValue(): String {
        return if (isEmpty()) "none" else joinToString(",").take(100)
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
            payCycleReviewStreak = payCycleReviewStreak,
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

    private fun PremiumReviewCoachingSummary.toState(preferences: UserPreferences): PremiumReviewCoachingSummaryState {
        return PremiumReviewCoachingSummaryState(
            headlineKey = headlineKey,
            bodyKey = bodyKey,
            status = status,
            metrics = metrics.map { it.toState(preferences) }
        )
    }

    private fun PremiumReviewCoachingMetric.toState(preferences: UserPreferences): PremiumReviewCoachingMetricState {
        val valueLabel = when (valueType) {
            PremiumReviewCoachingMetricType.MONEY -> currencyFormat(preferences).format(value)
            PremiumReviewCoachingMetricType.PERCENT -> LocalizedAmountFormatter.formatPercentage(value, preferences.locale)
        }
        return PremiumReviewCoachingMetricState(
            labelKey = labelKey,
            valueLabel = valueLabel,
            isPositive = isPositive
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

    private fun PremiumRulePaydayMix.toState(
        rules: List<PaydayRule>,
        preferences: UserPreferences
    ): RulePaydayMixCardState {
        val currencyFormat = currencyFormat(preferences)
        val rulesById = rules.associateBy { it.id }
        val visibleItems = items.take(MIX_VISIBLE_RULE_COUNT).mapNotNull { item ->
            val rule = rulesById[item.ruleId] ?: return@mapNotNull null
            RulePaydayMixItemState(
                ruleId = rule.id,
                ruleName = rule.name,
                ruleNameKey = rule.defaultNameKey,
                typeLabel = rule.type.name.lowercase().replaceFirstChar { it.titlecase() },
                typeLabelKey = rule.type.labelKey,
                amountLabel = currencyFormat.format(item.amount),
                shareLabel = LocalizedAmountFormatter.formatPercentage(item.sharePercent, preferences.locale)
            )
        }

        return RulePaydayMixCardState(
            totalMoveLabel = currencyFormat.format(totalMove),
            ruleCount = items.size,
            visibleItems = visibleItems,
            hiddenRuleCount = (items.size - visibleItems.size).coerceAtLeast(0)
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

    private fun PaydayAdjustmentRecommendation.toPremiumAdjustmentRecord(
        affectedRuleIds: List<String>,
        reviewId: String?
    ): PremiumAdjustmentRecord {
        return PremiumAdjustmentRecord(
            direction = direction,
            adjustmentAmount = adjustmentAmount,
            previousFlexibleSpend = currentFlexibleSpend,
            recommendedFlexibleSpend = recommendedFlexibleSpend,
            previousPriorityContribution = currentPriorityContribution,
            recommendedPriorityContribution = recommendedPriorityContribution,
            confidencePercent = confidencePercent,
            analyzedReviewCount = analyzedReviewCount,
            affectedRuleIds = affectedRuleIds,
            reviewId = reviewId
        )
    }

    private fun PremiumAdjustmentRecord.toMemoryState(preferences: UserPreferences): PremiumAdjustmentMemoryState {
        val currencyFormat = currencyFormat(preferences)
        return PremiumAdjustmentMemoryState(
            id = id,
            dateLabel = createdAt.format(DateTimeFormatter.ofPattern("d MMM", preferences.locale)),
            direction = direction,
            status = status,
            adjustmentAmountLabel = currencyFormat.format(adjustmentAmount),
            previousFlexibleSpendLabel = currencyFormat.format(previousFlexibleSpend),
            recommendedFlexibleSpendLabel = currencyFormat.format(recommendedFlexibleSpend),
            previousPriorityContributionLabel = currencyFormat.format(previousPriorityContribution),
            recommendedPriorityContributionLabel = currencyFormat.format(recommendedPriorityContribution),
            affectedRuleCount = affectedRuleIds.size
        )
    }

    private fun PremiumReviewMomentum.toState(): PremiumReviewMomentumState {
        return PremiumReviewMomentumState(
            status = status,
            totalReviews = totalReviews,
            currentStreak = currentStreak,
            nextMilestone = nextMilestone,
            reviewsUntilNextMilestone = reviewsUntilNextMilestone,
            progress = progress
        )
    }

    private fun PaydayReadiness.toState(): PaydayReadinessState {
        return PaydayReadinessState(
            status = status,
            progress = progress,
            completedMissions = completedMissions,
            totalMissions = totalMissions,
            nextAction = nextAction,
            missions = missions.map {
                PaydayReadinessMissionItemState(
                    mission = it.mission,
                    isComplete = it.isComplete
                )
            }
        )
    }

    private fun PaydayCountdownCue.toState(): PaydayCountdownCueState {
        return PaydayCountdownCueState(
            daysUntilPayday = daysUntilPayday,
            action = action
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
        const val MIX_VISIBLE_RULE_COUNT = 3
        const val PREMIUM_ADJUSTMENT_HISTORY_LIMIT = 8
        const val LEGACY_FREE_YEAR_TRIAL_DAYS = 300
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
