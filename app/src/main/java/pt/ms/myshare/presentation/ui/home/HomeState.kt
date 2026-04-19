package pt.ms.myshare.presentation.ui.home

import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.time.YearMonth

enum class HomeDestination {
    PLAN,
    RULES,
    GOALS,
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
    val summary: String = ""
)

data class GoalCardState(
    val id: String,
    val goalName: String = "",
    val goalAmountLabel: String = "",
    val progress: Float = 0f,
    val progressLabel: String = "",
    val targetDateLabel: String = "",
    val progressNote: String = ""
)

data class ReviewCardState(
    val actualFlexibleSpend: String = "",
    val actualGoalContribution: String = "",
    val insight: ReviewInsight? = null,
    val savedReviewDate: String? = null,
    val error: String? = null
)

data class RuleCardState(
    val id: String,
    val name: String,
    val amountLabel: String,
    val typeLabel: String,
    val isPercentage: Boolean
)

data class MoreCardState(
    val reminderEnabled: Boolean = false,
    val reminderLabel: String = "",
    val automationEnabled: Boolean = false,
    val pricingStrategy: PricingStrategy? = null,
    val actualMonthlyPrice: String? = null,
    val actualAnnualPrice: String? = null,
    val showAdsConsentOption: Boolean = false,
    val selectedBillingPlan: BillingPlan = BillingPlan.MONTHLY,
    val isPremium: Boolean = false,
    val userEmail: String? = null,
    val error: String? = null
)

data class ReviewHistoryItemState(
    val id: String,
    val dateLabel: String,
    val flexibleSpendLabel: String,
    val plannedFlexibleLabel: String = "", // Added for snapshots
    val goalContributionLabel: String,
    val plannedGoalLabel: String = "", // Added for snapshots
    val flexibleDeltaLabel: String,
    val goalDeltaLabel: String,
    val isPositive: Boolean
)

data class PerformanceStatsState(
    val healthScore: Int = 0,
    val currentStreak: Int = 0,
    val totalFlexSavingsLabel: String = "",
    val totalReviews: Int = 0
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
    val isLoading: Boolean = true,
    val emptyMessage: String? = null,
    val error: String? = null
)
