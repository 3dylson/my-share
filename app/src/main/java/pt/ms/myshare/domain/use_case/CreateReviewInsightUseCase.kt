package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreateReviewInsightUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {

    fun execute(plan: SalaryPlan, review: ManualReview): ReviewInsight {
        val preview = calculatePlanPreviewUseCase.execute(plan)
        val flexibleDelta = review.actualFlexibleSpend.subtract(preview.flexibleSpendPerPayday).setScale(2, RoundingMode.HALF_UP)
        val goalDelta = review.actualGoalContribution.subtract(preview.monthlyGoalContribution).setScale(2, RoundingMode.HALF_UP)

        val isOverspending = flexibleDelta > BigDecimal.ZERO
        val isGoalShort = goalDelta < BigDecimal.ZERO

        val headline = when (plan.focus) {
            pt.ms.myshare.domain.model.PlanningFocus.SAVE_WITHOUT_STRESS -> {
                if (!isGoalShort) "Safety net growing" else "Buffer needs priority"
            }
            pt.ms.myshare.domain.model.PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                if (!isGoalShort) "Discipline paying off" else "Consistency gap detected"
            }
            pt.ms.myshare.domain.model.PlanningFocus.STOP_OVERSPENDING -> {
                if (!isOverspending) "Budget mastered" else "Leakage detected"
            }
            pt.ms.myshare.domain.model.PlanningFocus.PLAN_TOGETHER -> {
                if (!isOverspending && !isGoalShort) "Harmony achieved" else "Plan needs alignment"
            }
        }

        val supportingText = when (plan.focus) {
            pt.ms.myshare.domain.model.PlanningFocus.SAVE_WITHOUT_STRESS -> {
                if (!isGoalShort) "You're successfully building your cushion. Even if flexible spending varied, your core safety is intact."
                else "Your savings contribution dipped. Try to protect those first to maintain your peace of mind."
            }
            pt.ms.myshare.domain.model.PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                if (!isGoalShort) "Market exposure is holding steady. This consistency is exactly what builds long-term wealth."
                else "Discipline slipped this cycle. Remember, small gaps today have a large impact on your future compounding."
            }
            pt.ms.myshare.domain.model.PlanningFocus.STOP_OVERSPENDING -> {
                if (!isOverspending) "You stayed within your flexible budget! This is a major win for your new financial habits."
                else "Flexible spending ran ${flexibleDelta.abs()} over plan. Scan your transactions to see where the leak happened."
            }
            pt.ms.myshare.domain.model.PlanningFocus.PLAN_TOGETHER -> {
                if (!isOverspending && !isGoalShort) "You maintained perfect alignment with the shared blueprint. Progress is visible and stable."
                else "There's a drift between the plan and reality. A quick check-in can help get the logic back on track."
            }
        }

        return ReviewInsight(
            plannedFlexibleSpend = preview.flexibleSpendPerPayday,
            actualFlexibleSpend = review.actualFlexibleSpend,
            flexibleSpendDelta = flexibleDelta,
            plannedGoalContribution = preview.monthlyGoalContribution,
            actualGoalContribution = review.actualGoalContribution,
            goalContributionDelta = goalDelta,
            headline = headline,
            supportingText = supportingText
        )
    }
}
