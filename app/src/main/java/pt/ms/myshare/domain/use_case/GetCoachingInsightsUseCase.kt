package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.InsightType
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Responsibility: Analyzes historical reviews to detect behavioral patterns
 * and provide proactive financial coaching.
 */
class GetCoachingInsightsUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {

    fun execute(plan: SalaryPlan, history: List<ManualReview>): List<ReviewInsight> {
        if (history.isEmpty()) return emptyList()

        val insights = mutableListOf<ReviewInsight>()
        val latestReview = history.first()
        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)

        // 1. Consistency King Pattern (Last 3 reviews met goal)
        val goalSuccessCount = history.take(3).count { 
            (it.plannedGoalContribution ?: preview.monthlyGoalContribution) <= it.actualGoalContribution 
        }
        if (goalSuccessCount >= 3) {
            insights.add(createInsight(
                preview, latestReview,
                "coaching_consistency_king_headline",
                "coaching_consistency_king_body",
                InsightType.SUCCESS
            ))
        }

        // 2. Hidden Buffer Pattern (Consistent surplus in flex spend)
        val flexSurplusCount = history.take(3).count {
            val planned = it.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
            it.actualFlexibleSpend < planned.multiply(BigDecimal("0.9")) // 10% surplus
        }
        if (flexSurplusCount >= 3) {
            insights.add(createInsight(
                preview, latestReview,
                "coaching_hidden_opportunity_headline",
                "coaching_hidden_opportunity_body",
                InsightType.TIP,
                "coaching_hidden_opportunity_action"
            ))
        }

        // 3. Lifestyle Inflation (Worsening trend in flex spend)
        if (history.size >= 3) {
            val lastThreeFlexDeltas = history.take(3).map {
                val planned = it.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
                it.actualFlexibleSpend.subtract(planned)
            }
            // If delta is increasing (e.g., -10, 5, 20)
            if (lastThreeFlexDeltas[0] > lastThreeFlexDeltas[1] && lastThreeFlexDeltas[1] > lastThreeFlexDeltas[2]) {
                 // Note: history is likely DESC starting with newest, so index 0 is newest
                 // Let's check trend: index 0 (newest) vs index 1 vs index 2 (oldest)
                 if (lastThreeFlexDeltas[0] > lastThreeFlexDeltas[1] && lastThreeFlexDeltas[1] > lastThreeFlexDeltas[2]) {
                    insights.add(createInsight(
                        preview, latestReview,
                        "coaching_spending_drift_headline",
                        "coaching_spending_drift_body",
                        InsightType.WARNING
                    ))
                 }
            }
        }

        // Default: If no patterns found and only 1 review, show a welcome tip
        if (insights.isEmpty() && history.size == 1) {
            insights.add(createInsight(
                preview, latestReview,
                "coaching_first_step_headline",
                "coaching_first_step_body",
                InsightType.TIP
            ))
        }

        return insights.take(2) // Limit to top 2 to maintain premium UI focus
    }

    private fun createInsight(
        preview: pt.ms.myshare.domain.model.PlanPreview,
        review: ManualReview,
        headline: String,
        body: String,
        type: InsightType,
        actionLabel: String? = null
    ): ReviewInsight {
        val flexPlanned = review.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
        val goalPlanned = review.plannedGoalContribution ?: preview.monthlyGoalContribution
        
        return ReviewInsight(
            plannedFlexibleSpend = flexPlanned,
            actualFlexibleSpend = review.actualFlexibleSpend,
            flexibleSpendDelta = review.actualFlexibleSpend.subtract(flexPlanned).setScale(2, RoundingMode.HALF_UP),
            plannedGoalContribution = goalPlanned,
            actualGoalContribution = review.actualGoalContribution,
            goalContributionDelta = review.actualGoalContribution.subtract(goalPlanned).setScale(2, RoundingMode.HALF_UP),
            headline = headline,
            supportingText = body,
            type = type,
            actionLabel = actionLabel
        )
    }
}
