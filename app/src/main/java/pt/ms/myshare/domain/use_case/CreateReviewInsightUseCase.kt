package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.InsightType
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreateReviewInsightUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {

    fun execute(plan: SalaryPlan, review: ManualReview): ReviewInsight {
        // Goal amount doesn't affect delta calculation, using ZERO as placeholder for target date logic which isn't used here
        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
        val plannedFlexibleSpend = review.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
        val plannedGoalContribution = review.plannedGoalContribution ?: preview.priorityContributionPerPayday
        val flexibleDelta = review.actualFlexibleSpend.subtract(plannedFlexibleSpend).setScale(2, RoundingMode.HALF_UP)
        val goalDelta = review.actualGoalContribution.subtract(plannedGoalContribution).setScale(2, RoundingMode.HALF_UP)

        val isOverspending = flexibleDelta > BigDecimal.ZERO
        val isGoalShort = goalDelta < BigDecimal.ZERO

        val headline = when (plan.focus) {
            pt.ms.myshare.domain.model.PlanningFocus.SAVE_WITHOUT_STRESS -> {
                if (!isGoalShort) "insight_review_save_stress_success_headline" else "insight_review_save_stress_failure_headline"
            }
            pt.ms.myshare.domain.model.PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                if (!isGoalShort) "insight_review_invest_discipline_success_headline" else "insight_review_invest_discipline_failure_headline"
            }
            pt.ms.myshare.domain.model.PlanningFocus.STOP_OVERSPENDING -> {
                if (!isOverspending) "insight_review_stop_overspending_success_headline" else "insight_review_stop_overspending_failure_headline"
            }
            pt.ms.myshare.domain.model.PlanningFocus.PLAN_TOGETHER -> {
                if (!isOverspending && !isGoalShort) "insight_review_plan_together_success_headline" else "insight_review_plan_together_failure_headline"
            }
        }

        val supportingText = when (plan.focus) {
            pt.ms.myshare.domain.model.PlanningFocus.SAVE_WITHOUT_STRESS -> {
                if (!isGoalShort) "insight_review_save_stress_success_body"
                else "insight_review_save_stress_failure_body"
            }
            pt.ms.myshare.domain.model.PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                if (!isGoalShort) "insight_review_invest_discipline_success_body"
                else "insight_review_invest_discipline_failure_body"
            }
            pt.ms.myshare.domain.model.PlanningFocus.STOP_OVERSPENDING -> {
                if (!isOverspending) "insight_review_stop_overspending_success_body"
                else "insight_review_stop_overspending_failure_body"
            }
            pt.ms.myshare.domain.model.PlanningFocus.PLAN_TOGETHER -> {
                if (!isOverspending && !isGoalShort) "insight_review_plan_together_success_body"
                else "insight_review_plan_together_failure_body"
            }
        }

        val type = if (isOverspending || isGoalShort) InsightType.WARNING else InsightType.SUCCESS

        return ReviewInsight(
            plannedFlexibleSpend = plannedFlexibleSpend,
            actualFlexibleSpend = review.actualFlexibleSpend,
            flexibleSpendDelta = flexibleDelta,
            plannedGoalContribution = plannedGoalContribution,
            actualGoalContribution = review.actualGoalContribution,
            goalContributionDelta = goalDelta,
            headline = headline,
            supportingText = supportingText,
            type = type
        )
    }
}
