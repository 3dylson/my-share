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
    val goalName: String = "",
    val goalAmountLabel: String = "",
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

data class MoreCardState(
    val reminderEnabled: Boolean = false,
    val reminderLabel: String = "",
    val automationEnabled: Boolean = false,
    val pricingStrategy: PricingStrategy? = null,
    val selectedBillingPlan: BillingPlan = BillingPlan.MONTHLY,
    val isPremium: Boolean = false,
    val userEmail: String? = null
)

data class HomeState(
    val selectedDestination: HomeDestination = HomeDestination.PLAN,
    val plan: SalaryPlan? = null,
    val planCard: HomePlanCardState? = null,
    val goalCard: GoalCardState? = null,
    val reviewCard: ReviewCardState = ReviewCardState(),
    val moreCard: MoreCardState = MoreCardState(),
    val isLoading: Boolean = true,
    val emptyMessage: String? = null,
    val error: String? = null
)
