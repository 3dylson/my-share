package pt.ms.myshare.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.domain.use_case.GetReviewHistoryUseCase
import pt.ms.myshare.domain.use_case.UpdateGoalProgressUseCase
import pt.ms.myshare.domain.use_case.GetPerformanceStatsUseCase
import pt.ms.myshare.domain.use_case.GetCoachingInsightsUseCase
import pt.ms.myshare.domain.use_case.PerformanceStats
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val authRepository: AuthRepository,
    private val entitlementRepository: EntitlementRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val createReviewInsightUseCase: CreateReviewInsightUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase,
    private val getReviewHistoryUseCase: GetReviewHistoryUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val getPerformanceStatsUseCase: GetPerformanceStatsUseCase,
    private val getCoachingInsightsUseCase: GetCoachingInsightsUseCase
) : ViewModel() {

    private val uiState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = uiState.asStateFlow()

    private var availableStoreProducts: List<pt.ms.myshare.domain.model.StoreProduct> = emptyList()
    private var currentPlan: SalaryPlan? = null

    private val locale: Locale = Locale.getDefault()
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    private val pricingStrategy = resolvePricingStrategyUseCase.execute(locale)

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
        }
        observeProducts()
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

    private fun observePlannerData() {
        viewModelScope.launch {
            val plannerFlow = combine(
                plannerRepository.observePlan(),
                plannerRepository.observeRules(),
                plannerRepository.observeGoals(),
                plannerRepository.observeLatestReview(),
                plannerRepository.observeReminderConfiguration(),
                plannerRepository.observeAutomationEnabled(),
                plannerRepository.observeReviews(),
                getPerformanceStatsUseCase.execute()
            ) { args: Array<Any?> ->
                val plan = args[0] as pt.ms.myshare.domain.model.SalaryPlan?
                val rules = args[1] as List<pt.ms.myshare.domain.model.PaydayRule>
                val goals = args[2] as List<Goal>
                val lr = args[3] as ManualReview?
                val rc = args[4] as pt.ms.myshare.domain.model.ReminderConfiguration
                val ae = args[5] as Boolean
                val history = args[6] as List<ManualReview>
                val stats = args[7] as PerformanceStats

                val coaching = if (plan != null) {
                    getCoachingInsightsUseCase.execute(plan, history)
                } else {
                    emptyList()
                }
                
                val trend = if (plan != null) {
                    history.reversed().takeLast(5).map { review ->
                        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
                        val flexTarget = review.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
                        val goalTarget = review.plannedGoalContribution ?: preview.savingsPerPayday
                        
                        val flexPerformance = if (review.actualFlexibleSpend <= flexTarget) 1.0f else 
                            (flexTarget.toDouble() / review.actualFlexibleSpend.toDouble()).coerceIn(0.0, 1.0).toFloat()
                        
                        val goalPerformance = if (review.actualGoalContribution >= goalTarget) 1.0f else
                            (review.actualGoalContribution.toDouble() / goalTarget.toDouble()).coerceIn(0.0, 1.0).toFloat()
                            
                        (flexPerformance + goalPerformance) / 2f
                    }
                } else {
                    emptyList()
                }
                
                PlannerGroup(
                    plan = plan,
                    rules = rules,
                    goals = goals,
                    latestReview = lr,
                    reminder = rc,
                    automation = ae,
                    stats = stats,
                    combined = Triple(history, stats, coaching to trend)
                )
            }

            combine(
                plannerFlow,
                entitlementRepository.isPro,
                entitlementRepository.availableProducts,
                authRepository.currentUser
            ) { planner, isPremium, products, user ->
                val plan = planner.plan
                val currentRules = planner.rules
                val goals = planner.goals
                val latestReview = planner.latestReview
                val reminder = planner.reminder
                val automation = planner.automation
                val combined = planner.combined
                
                val updatedPlan = plan?.copy(rules = currentRules)
                currentPlan = updatedPlan

                val emptyMessage = if (updatedPlan == null) "home_empty_build_plan_first" else null
                val primaryGoal = goals.firstOrNull()
                val planCard = updatedPlan?.let { buildPlanCard(it, primaryGoal?.targetAmount ?: BigDecimal.ZERO) }
                val goalCards = goals.map { buildGoalCard(it, updatedPlan) }
                val ruleCards = currentRules.map { buildRuleCard(it) }
                val reviewCard = buildReviewCard(updatedPlan, latestReview)
                
                val monthlyProduct = products.find { it.productId == PremiumSubscriptionProducts.MONTHLY_ID }
                val annualProduct = products.find { it.productId == PremiumSubscriptionProducts.ANNUAL_ID }

                val moreCard = MoreCardState(
                    reminderEnabled = reminder.enabled,
                    reminderLabelKey = if (reminder.enabled) "home_more_reminder_at_time" else "home_more_reminders_off",
                    reminderLabelArgs = if (reminder.enabled) {
                        listOf(
                            reminder.cadence.name.replace('_', ' ')
                                .lowercase(Locale.getDefault())
                                .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                            "${reminder.hourOfDay.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}"
                        )
                    } else {
                        emptyList()
                    },
                    pricingStrategy = pricingStrategy,
                    actualMonthlyPrice = monthlyProduct?.price ?: pricingStrategy.monthlyLabel,
                    actualAnnualPrice = annualProduct?.price ?: pricingStrategy.annualLabel,
                    actualMonthlyTrialDays = monthlyProduct?.freeTrialDays?.takeIf { it > 0 },
                    actualAnnualTrialDays = annualProduct?.freeTrialDays?.takeIf { it > 0 },
                    showAdsConsentOption = uiState.value.moreCard.showAdsConsentOption,
                    selectedBillingPlan = pricingStrategy.heroPlan,
                    isPremium = isPremium,
                    automationEnabled = automation,
                    userEmail = user?.email
                )
                HomeState(
                    selectedDestination = uiState.value.selectedDestination,
                    plan = updatedPlan,
                    planCard = planCard,
                    goals = goalCards,
                    rules = ruleCards,
                    performanceStats = combined.second.toState(combined.third.second),
                    reviewCard = reviewCard.copy(coachingInsights = combined.third.first.map { it.toState() }),
                    reviewHistory = combined.first.map { it.toState() },
                    moreCard = moreCard,
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
        val plan: pt.ms.myshare.domain.model.SalaryPlan?,
        val rules: List<pt.ms.myshare.domain.model.PaydayRule>,
        val goals: List<Goal>,
        val latestReview: ManualReview?,
        val reminder: pt.ms.myshare.domain.model.ReminderConfiguration,
        val automation: Boolean,
        val stats: PerformanceStats,
        val combined: Triple<List<ManualReview>, PerformanceStats, Pair<List<ReviewInsight>, List<Float>>>
    )

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    private fun buildPlanCard(plan: pt.ms.myshare.domain.model.SalaryPlan, goalAmount: BigDecimal): HomePlanCardState {
        val preview = calculatePlanPreviewUseCase.execute(plan, goalAmount)
        return HomePlanCardState(
            incomeLabel = currencyFormat.format(plan.netIncomePerPayday),
            fixedCostsLabel = currencyFormat.format(preview.fixedCostsPerPayday),
            flexibleSpendLabel = currencyFormat.format(preview.flexibleSpendPerPayday),
            savingsLabel = currencyFormat.format(preview.savingsPerPayday),
            investingLabel = currencyFormat.format(preview.investingPerPayday.add(preview.cryptoPerPayday)),
            weeklySpendLabel = currencyFormat.format(preview.weeklyFlexibleSpend),
            summary = preview.summary,
            nextPaydayKey = "home_plan_next_payday",
            nextPaydayArgs = listOf(preview.nextPayday.format(dateFormatter))
        )
    }

    private fun buildGoalCard(goal: Goal, plan: SalaryPlan?): GoalCardState {
        val preview = if (plan != null) {
            calculatePlanPreviewUseCase.execute(plan, goal.targetAmount)
        } else null

        val (targetDateKey, targetDateArgs) = if (goal.isCompleted) {
            "home_goal_mission_accomplished" to emptyList()
        } else {
            preview?.goalTargetDate?.let { 
                val monthStr = it.month.name.lowercase().replaceFirstChar(Char::titlecase)
                "home_goal_on_pace" to listOf("$monthStr ${it.year}")
            } ?: ("home_goal_no_trajectory" to emptyList())
        }
        
        val progressPercent = if (goal.targetAmount > BigDecimal.ZERO) {
            goal.currentProgress.divide(goal.targetAmount, 4, java.math.RoundingMode.HALF_UP).toFloat()
        } else 0f

        return GoalCardState(
            id = goal.id,
            goalName = goal.name,
            goalAmountLabel = currencyFormat.format(goal.targetAmount),
            progress = progressPercent,
            progressLabelKey = "home_goal_progress_label",
            progressLabelArgs = listOf(currencyFormat.format(goal.currentProgress), currencyFormat.format(goal.targetAmount)),
            targetDateKey = targetDateKey,
            targetDateArgs = targetDateArgs,
            progressNoteKey = if (goal.isCompleted) "home_goal_note_completed" else "home_goal_note_tracking"
        )
    }

    private fun buildRuleCard(rule: pt.ms.myshare.domain.model.PaydayRule): RuleCardState {
        val amountLabel = if (rule.isPercentage) {
            LocalizedAmountFormatter.formatPercentage(rule.amount, locale)
        } else {
            currencyFormat.format(rule.amount)
        }
        return RuleCardState(
            id = rule.id,
            name = rule.name,
            amountLabel = amountLabel,
            typeLabel = rule.type.name.lowercase().replaceFirstChar { it.titlecase() },
            typeLabelKey = rule.type.labelKey,
            isPercentage = rule.isPercentage
        )
    }

    private fun buildReviewCard(plan: pt.ms.myshare.domain.model.SalaryPlan?, review: ManualReview?): ReviewCardState {
        val insight = if (plan != null && review != null) {
            createReviewInsightUseCase.execute(plan, review)
        } else {
            null
        }
        val preview = plan?.let { calculatePlanPreviewUseCase.execute(it, BigDecimal.ZERO) }
        val defaultFlexibleSpend = review?.actualFlexibleSpend ?: preview?.flexibleSpendPerPayday
        val defaultGoalContribution = review?.actualGoalContribution ?: preview?.savingsPerPayday
        val flexibleMax = reviewRangeMax(defaultFlexibleSpend ?: BigDecimal.ZERO)
        val goalMax = reviewRangeMax(defaultGoalContribution ?: BigDecimal.ZERO)

        return ReviewCardState(
            actualFlexibleSpend = defaultFlexibleSpend?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) }.orEmpty(),
            actualGoalContribution = defaultGoalContribution?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) }.orEmpty(),
            flexibleSpendMax = flexibleMax,
            goalContributionMax = goalMax,
            insight = insight,
            savedReviewDate = review?.createdAt?.format(dateFormatter)
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

        val actualFlexible = LocalizedAmountFormatter.parseAmount(uiState.value.reviewCard.actualFlexibleSpend, locale)
        val actualGoal = LocalizedAmountFormatter.parseAmount(uiState.value.reviewCard.actualGoalContribution, locale)
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
                plannedGoalContribution = preview.savingsPerPayday
            )
            plannerRepository.saveReview(review)
            updateGoalProgressUseCase.execute(actualGoal)
            
            FirebaseUtils.logEvent("weekly_checkin_completed")
            Timber.tag(TAG).d("Review saved with snapshots. planFlex=%s planGoal=%s", preview.flexibleSpendPerPayday, preview.savingsPerPayday)
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val current = plannerRepository.loadReminderConfiguration()
            plannerRepository.saveReminderConfiguration(current.copy(enabled = enabled))
            FirebaseUtils.logEvent(if (enabled) "reminder_enabled" else "reminder_disabled")
        }
    }

    private fun updateReminderState(enabled: Boolean) {
        val labelKey = if (enabled) "home_more_reminders_on" else "home_more_reminders_off"
        uiState.update { it.copy(moreCard = it.moreCard.copy(reminderEnabled = enabled, reminderLabelKey = labelKey)) }
    }

    fun chooseBillingPlan(plan: BillingPlan) {
        uiState.update { it.copy(moreCard = it.moreCard.copy(selectedBillingPlan = plan, billingMessage = null)) }
    }

    fun unlockPremium(activity: android.app.Activity) {
        val storeProductId = PremiumSubscriptionProducts.productIdFor(uiState.value.moreCard.selectedBillingPlan)
        
        val realProduct = availableStoreProducts.find { it.productId == storeProductId }
        
        viewModelScope.launch {
            uiState.update {
                it.copy(moreCard = it.moreCard.copy(isBillingActionInProgress = true, billingMessage = "paywall_billing_starting", error = null))
            }
            if (realProduct != null) {
                entitlementRepository.purchasePlan(activity, realProduct)
                FirebaseUtils.logEvent("purchase_started")
                uiState.update {
                    it.copy(moreCard = it.moreCard.copy(isBillingActionInProgress = false, billingMessage = "paywall_billing_handoff"))
                }
            } else {
                uiState.update {
                    it.copy(
                        moreCard = it.moreCard.copy(
                            isBillingActionInProgress = false,
                            billingMessage = "paywall_billing_products_unavailable",
                            error = "more_error_products_not_loaded"
                        )
                    )
                }
                Timber.tag(TAG).e("Cannot purchase: Product %s not found in store", storeProductId)
            }
        }
    }

    fun updateAdsConsentRequirement(isRequired: Boolean) {
        uiState.update { it.copy(moreCard = it.moreCard.copy(showAdsConsentOption = isRequired)) }
        Timber.tag(TAG).d("Ads consent option visibility updated: %s", isRequired)
    }

    fun onLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            plannerRepository.clearPlan() // Reset local state on logout
            plannerRepository.setOnboardingCompleted(false) // Force back to onboarding
            FirebaseUtils.logEvent("user_logged_out")
            onComplete()
        }
    }

    private fun PerformanceStats.toState(trend: List<Float> = emptyList()): PerformanceStatsState {
        return PerformanceStatsState(
            healthScore = healthScore,
            currentStreak = currentStreak,
            totalFlexSavingsLabel = currencyFormat.format(totalSavingsBeyondGoal),
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

    private fun ManualReview.toState(): ReviewHistoryItemState {
        val flexTarget = plannedFlexibleSpend ?: BigDecimal.ZERO
        val flexDelta = actualFlexibleSpend.subtract(flexTarget)
        val goalTarget = plannedGoalContribution ?: BigDecimal.ZERO
        val goalDelta = actualGoalContribution.subtract(goalTarget)

        return ReviewHistoryItemState(
            id = id,
            dateLabel = createdAt.format(dateFormatter),
            flexibleSpendLabel = currencyFormat.format(actualFlexibleSpend),
            plannedFlexibleLabel = currencyFormat.format(flexTarget),
            goalContributionLabel = currencyFormat.format(actualGoalContribution),
            plannedGoalLabel = currencyFormat.format(goalTarget),
            flexibleDeltaLabel = (if (flexDelta > BigDecimal.ZERO) "+" else "") + currencyFormat.format(flexDelta),
            goalDeltaLabel = (if (goalDelta > BigDecimal.ZERO) "+" else "") + currencyFormat.format(goalDelta),
            isPositive = flexDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO
        )
    }

    private fun sanitizeNumber(value: String): String = LocalizedAmountFormatter.sanitizeAmountInput(value, locale)

    private fun reviewRangeMax(value: BigDecimal): Float {
        val doubled = value.multiply(BigDecimal("2")).toFloat()
        return doubled.coerceAtLeast(100f)
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}

private val pt.ms.myshare.domain.model.PaydayRuleType.labelKey: String
    get() = when (this) {
        pt.ms.myshare.domain.model.PaydayRuleType.SAVINGS -> "rule_type_savings"
        pt.ms.myshare.domain.model.PaydayRuleType.INVESTING -> "rule_type_investing"
        pt.ms.myshare.domain.model.PaydayRuleType.CRYPTO -> "rule_type_crypto"
        pt.ms.myshare.domain.model.PaydayRuleType.DEBT -> "rule_type_debt"
        pt.ms.myshare.domain.model.PaydayRuleType.OTHER -> "rule_type_other"
    }
