package pt.ms.myshare.presentation.ui.home

import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.UserPreferences
import java.math.BigDecimal
import java.time.YearMonth

enum class HomeDestination {
    PLAN,
    STRATEGY,
    REVIEW,
    MORE
}

data class HomePlanCardState(
    val nextPaydayLabel: String = "",
    val incomeLabel: String = "",
    val fixedCostsLabel: String = "",
    val flexibleSpendLabel: String = "",
    val savingsLabel: String = "",
    val investingLabel: String = "",
    val weeklySpendLabel: String = "",
    val summary: String = "",
    val nextPaydayKey: String? = null,
    val nextPaydayArgs: List<String> = emptyList()
)

data class GoalCardState(
    val id: String,
    val goalName: String = "",
    val goalNameKey: String? = null,
    val goalAmountLabel: String = "",
    val progress: Float = 0f,
    val progressLabel: String = "",
    val targetDateLabel: String = "",
    val targetDateKey: String? = null,
    val targetDateArgs: List<String> = emptyList(),
    val progressLabelKey: String? = null,
    val progressLabelArgs: List<String> = emptyList(),
    val progressNote: String = "",
    val progressNoteKey: String? = null,
    val isLockedByEntitlement: Boolean = false
)

data class ReviewCardState(
    val actualFlexibleSpend: String = "",
    val actualGoalContribution: String = "",
    val currencySymbol: String = "",
    val flexibleSpendMax: Float = 5000f,
    val goalContributionMax: Float = 5000f,
    val insight: ReviewInsight? = null,
    val coachingInsights: List<ReviewInsightState> = emptyList(),
    val paydayRecommendation: PaydayAdjustmentRecommendationState? = null,
    val recommendationMessageKey: String? = null,
    val savedReviewDate: String? = null,
    val error: String? = null
)

data class PaydayAdjustmentRecommendationState(
    val direction: PaydayAdjustmentRecommendationDirection,
    val analyzedReviewCount: Int,
    val currentFlexibleSpendLabel: String,
    val recommendedFlexibleSpendLabel: String,
    val currentPriorityContributionLabel: String,
    val recommendedPriorityContributionLabel: String,
    val adjustmentAmountLabel: String,
    val confidencePercent: Int,
    val isApplyable: Boolean
)

data class ReviewInsightState(
    val headline: String,
    val supportingText: String,
    val type: pt.ms.myshare.domain.model.InsightType,
    val actionLabel: String? = null
)

data class RuleCardState(
    val id: String,
    val name: String,
    val amountLabel: String,
    val typeLabel: String,
    val isPercentage: Boolean,
    val typeLabelKey: String? = null,
    val nameKey: String? = null,
    val isLockedByEntitlement: Boolean = false
)

data class MoreCardState(
    val reminderEnabled: Boolean = false,
    val reminderLabel: String = "",
    val reminderLabelKey: String? = null,
    val reminderLabelArgs: List<String> = emptyList(),
    val reminderHourOfDay: Int = 9,
    val reminderMinute: Int = 0,
    val reminderCadence: ReminderCadence = ReminderCadence.PAYDAY,
    val automationEnabled: Boolean = false,
    val pricingStrategy: PricingStrategy? = null,
    val actualMonthlyPrice: String? = null,
    val actualAnnualPrice: String? = null,
    val actualMonthlyPriceCurrencyCode: String? = null,
    val actualAnnualPriceCurrencyCode: String? = null,
    val annualMonthlyEquivalentPrice: String? = null,
    val annualSavingsPrice: String? = null,
    val actualMonthlyTrialDays: Int? = null,
    val actualAnnualTrialDays: Int? = null,
    val showAdsConsentOption: Boolean = true,
    val selectedBillingPlan: BillingPlan = BillingPlan.MONTHLY,
    val isBillingActionInProgress: Boolean = false,
    val billingMessage: String? = null,
    val isPremium: Boolean = false,
    val userEmail: String? = null,
    val canConnectGoogle: Boolean = false,
    val showPremiumAccountPrompt: Boolean = false,
    val isGoogleConnectionInProgress: Boolean = false,
    val googleConnectionMessage: String? = null,
    val googleConnectionError: String? = null,
    val isLogoutInProgress: Boolean = false,
    val logoutError: String? = null,
    val userPreferences: UserPreferences = UserPreferences.defaults(),
    val weeklyGuideLabel: String = "",
    val priorityMoveLabel: String = "",
    val ruleCount: Int = 0,
    val reviewCount: Int = 0,
    val error: String? = null
) {
    val requiresPremiumAccountProtectionBeforeLogout: Boolean
        get() = isPremium && userEmail.isNullOrBlank()
}

data class ReviewHistoryItemState(
    val id: String,
    val dateLabel: String,
    val monthLabel: String = "",
    val flexibleSpendLabel: String,
    val plannedFlexibleLabel: String = "",
    val editableFlexibleSpend: String = "",
    val goalContributionLabel: String,
    val plannedGoalLabel: String = "",
    val editableGoalContribution: String = "",
    val flexibleDeltaLabel: String,
    val goalDeltaLabel: String,
    val isPositive: Boolean
)

data class PerformanceStatsState(
    val healthScore: Int = 0,
    val currentStreak: Int = 0,
    val totalFlexSavingsLabel: String = "",
    val totalSavings: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val totalReviews: Int = 0,
    val performanceTrend: List<Float> = emptyList() // Values 0..1 for sparkline
)

data class HomeState(
    val selectedDestination: HomeDestination = HomeDestination.PLAN,
    val plan: SalaryPlan? = null,
    val planCard: HomePlanCardState? = null,
    val rules: List<RuleCardState> = emptyList(),
    val goals: List<GoalCardState> = emptyList(),
    val reviewCard: ReviewCardState = ReviewCardState(),
    val reviewHistory: List<ReviewHistoryItemState> = emptyList(),
    val performanceStats: PerformanceStatsState = PerformanceStatsState(),
    val moreCard: MoreCardState = MoreCardState(),
    val reviewSavedEventId: Long = 0L,
    val isLoading: Boolean = true,
    val emptyMessage: String? = null,
    val error: String? = null
)
