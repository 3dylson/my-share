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

        val headline = when {
            flexibleDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO -> "You are on track"
            flexibleDelta > BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO -> "You overspent a bit"
            flexibleDelta <= BigDecimal.ZERO && goalDelta < BigDecimal.ZERO -> "Future money needs a top-up"
            else -> "Your plan drifted this cycle"
        }

        val supportingText = when {
            flexibleDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO -> "You stayed within your flexible budget and kept future money moving."
            flexibleDelta > BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO -> "Flexible spending ran above plan, but your goal contribution still held."
            flexibleDelta <= BigDecimal.ZERO && goalDelta < BigDecimal.ZERO -> "Spending stayed under control, but you contributed less than planned to the goal."
            else -> "Both flexible spending and goal contributions moved away from the plan."
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
