package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PremiumReviewCoachingMetric
import pt.ms.myshare.domain.model.PremiumReviewCoachingMetricType
import pt.ms.myshare.domain.model.PremiumReviewCoachingStatus
import pt.ms.myshare.domain.model.PremiumReviewCoachingSummary
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreatePremiumReviewCoachingSummaryUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {

    fun execute(plan: SalaryPlan, history: List<ManualReview>): PremiumReviewCoachingSummary? {
        if (history.size < MINIMUM_REVIEWS) return null

        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
        val samples = history
            .withIndex()
            .sortedWith(
                compareByDescending<IndexedValue<ManualReview>> { it.value.createdAt }
                    .thenByDescending { it.index }
            )
            .take(MAXIMUM_REVIEWS)
            .map { (_, review) ->
                ReviewCoachingSample(
                    plannedFlexibleSpend = review.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday,
                    actualFlexibleSpend = review.actualFlexibleSpend,
                    plannedGoalContribution = review.plannedGoalContribution ?: preview.priorityContributionPerPayday,
                    actualGoalContribution = review.actualGoalContribution
                )
            }

        if (samples.size < MINIMUM_REVIEWS) return null

        val averageFlexibleBuffer = average(
            samples.fold(BigDecimal.ZERO) { total, sample ->
                total.add(sample.flexibleBuffer)
            },
            samples.size
        )
        val averageGoalExtra = average(
            samples.fold(BigDecimal.ZERO) { total, sample ->
                total.add(sample.goalExtra)
            },
            samples.size
        )
        val onTrackCount = samples.count { it.isOnTrack }
        val onTrackRate = BigDecimal(onTrackCount)
            .multiply(ONE_HUNDRED)
            .divide(BigDecimal(samples.size), 0, RoundingMode.HALF_UP)

        val status = when {
            onTrackRate >= STRONG_RATE -> PremiumReviewCoachingStatus.STRONG
            onTrackRate >= STEADY_RATE -> PremiumReviewCoachingStatus.STEADY
            else -> PremiumReviewCoachingStatus.NEEDS_ATTENTION
        }

        return PremiumReviewCoachingSummary(
            headlineKey = status.headlineKey,
            bodyKey = status.bodyKey,
            status = status,
            reviewedPaydays = samples.size,
            metrics = listOf(
                PremiumReviewCoachingMetric(
                    labelKey = "home_review_coaching_metric_buffer",
                    value = averageFlexibleBuffer,
                    valueType = PremiumReviewCoachingMetricType.MONEY,
                    isPositive = averageFlexibleBuffer > BigDecimal.ZERO
                ),
                PremiumReviewCoachingMetric(
                    labelKey = "home_review_coaching_metric_extra",
                    value = averageGoalExtra,
                    valueType = PremiumReviewCoachingMetricType.MONEY,
                    isPositive = averageGoalExtra > BigDecimal.ZERO
                ),
                PremiumReviewCoachingMetric(
                    labelKey = "home_review_coaching_metric_hit_rate",
                    value = onTrackRate,
                    valueType = PremiumReviewCoachingMetricType.PERCENT,
                    isPositive = onTrackRate >= STEADY_RATE
                )
            )
        )
    }

    private fun average(total: BigDecimal, count: Int): BigDecimal {
        return total.divide(BigDecimal(count), MONEY_SCALE, RoundingMode.HALF_UP)
    }

    private data class ReviewCoachingSample(
        val plannedFlexibleSpend: BigDecimal,
        val actualFlexibleSpend: BigDecimal,
        val plannedGoalContribution: BigDecimal,
        val actualGoalContribution: BigDecimal
    ) {
        val flexibleBuffer: BigDecimal
            get() = plannedFlexibleSpend.subtract(actualFlexibleSpend)
                .max(BigDecimal.ZERO)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP)

        val goalExtra: BigDecimal
            get() = actualGoalContribution.subtract(plannedGoalContribution)
                .max(BigDecimal.ZERO)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP)

        val isOnTrack: Boolean
            get() = actualFlexibleSpend <= plannedFlexibleSpend &&
                actualGoalContribution >= plannedGoalContribution
    }

    private val PremiumReviewCoachingStatus.headlineKey: String
        get() = when (this) {
            PremiumReviewCoachingStatus.STRONG -> "home_review_coaching_summary_headline_strong"
            PremiumReviewCoachingStatus.STEADY -> "home_review_coaching_summary_headline_steady"
            PremiumReviewCoachingStatus.NEEDS_ATTENTION -> "home_review_coaching_summary_headline_attention"
        }

    private val PremiumReviewCoachingStatus.bodyKey: String
        get() = when (this) {
            PremiumReviewCoachingStatus.STRONG -> "home_review_coaching_summary_body_strong"
            PremiumReviewCoachingStatus.STEADY -> "home_review_coaching_summary_body_steady"
            PremiumReviewCoachingStatus.NEEDS_ATTENTION -> "home_review_coaching_summary_body_attention"
        }

    private companion object {
        const val MONEY_SCALE = 2
        const val MINIMUM_REVIEWS = 2
        const val MAXIMUM_REVIEWS = 6
        val ONE_HUNDRED: BigDecimal = BigDecimal("100")
        val STRONG_RATE: BigDecimal = BigDecimal("70")
        val STEADY_RATE: BigDecimal = BigDecimal("40")
    }
}
