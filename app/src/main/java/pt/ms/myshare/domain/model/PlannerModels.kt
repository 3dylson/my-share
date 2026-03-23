package pt.ms.myshare.domain.model

import java.math.BigDecimal
import java.time.LocalDate

enum class PlanningFocus {
    SAVE_WITHOUT_STRESS,
    INVEST_WITH_DISCIPLINE,
    STOP_OVERSPENDING,
    PLAN_TOGETHER
}

enum class PayFrequency {
    MONTHLY,
    BIWEEKLY
}

data class SalaryPlan(
    val focus: PlanningFocus,
    val netIncomePerPayday: BigDecimal,
    val monthlyFixedCosts: BigDecimal,
    val payFrequency: PayFrequency,
    val monthlyPayday: Int? = null,
    val nextBiweeklyPayday: LocalDate? = null,
    val preset: AllocationPreset,
    val goalName: String,
    val goalAmount: BigDecimal,
    val flexibleSpend: BigDecimal? = null,
    val savings: BigDecimal? = null,
    val investing: BigDecimal? = null,
    val crypto: BigDecimal? = null,
    val createdAt: LocalDate = LocalDate.now()
)

data class ReminderConfiguration(
    val enabled: Boolean = false,
    val hourOfDay: Int = 9,
    val minute: Int = 0,
    val cadence: ReminderCadence = ReminderCadence.PAYDAY
)

enum class ReminderCadence {
    PAYDAY,
    WEEKLY_REVIEW
}

data class ManualReview(
    val actualFlexibleSpend: BigDecimal,
    val actualGoalContribution: BigDecimal,
    val createdAt: LocalDate = LocalDate.now()
)

data class ReviewInsight(
    val plannedFlexibleSpend: BigDecimal,
    val actualFlexibleSpend: BigDecimal,
    val flexibleSpendDelta: BigDecimal,
    val plannedGoalContribution: BigDecimal,
    val actualGoalContribution: BigDecimal,
    val goalContributionDelta: BigDecimal,
    val headline: String,
    val supportingText: String
)

enum class BillingPlan {
    MONTHLY,
    ANNUAL
}

data class PricingStrategy(
    val marketCluster: String,
    val monthlyLabel: String,
    val annualLabel: String,
    val heroPlan: BillingPlan,
    val trialDays: Int,
    val paywallHeadline: String,
    val paywallSubhead: String
)
