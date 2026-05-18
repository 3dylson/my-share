package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendation
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreatePaydayAdjustmentRecommendationUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val resolveAllocationStrategyRulesUseCase: ResolveAllocationStrategyRulesUseCase
) {

    fun execute(plan: SalaryPlan, history: List<ManualReview>): PaydayAdjustmentRecommendation? {
        val analyzedReviews = history
            .sortedByDescending { it.createdAt }
            .take(MAX_REVIEWS)
            .map { it.toSignal(plan) }

        if (analyzedReviews.isEmpty()) return null

        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
        val remainingAfterFixed = plan.netIncomePerPayday
            .subtract(fixedCostsPerPayday(plan))
            .max(BigDecimal.ZERO)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        val minimumAction = practicalAmount(
            remainingAfterFixed.multiply(MINIMUM_ACTION_RATIO)
        ).max(MINIMUM_ACTION_AMOUNT)

        val averageFlexDelta = analyzedReviews.averageOf { it.flexibleSpendDelta }
        val averageGoalDelta = analyzedReviews.averageOf { it.goalContributionDelta }
        val averagePlannedPriority = analyzedReviews.averageOf { it.plannedGoalContribution }
        val flexSurplus = averageFlexDelta.negate().max(BigDecimal.ZERO)
        val flexPressure = averageFlexDelta.max(BigDecimal.ZERO)
        val goalShortfall = averageGoalDelta.negate().max(BigDecimal.ZERO)

        return when {
            flexSurplus >= minimumAction && averageGoalDelta >= BigDecimal.ZERO -> {
                createRecommendation(
                    direction = PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY,
                    plan = plan,
                    analyzedReviewCount = analyzedReviews.size,
                    currentFlexibleSpend = preview.flexibleSpendPerPayday,
                    currentPriorityContribution = preview.priorityContributionPerPayday,
                    targetPriorityContribution = averagePlannedPriority
                        .add(practicalAmount(flexSurplus.multiply(OPPORTUNITY_CAPTURE_RATIO)))
                        .coerceAtMost(remainingAfterFixed.multiply(MAX_PRIORITY_RATIO)),
                    remainingAfterFixed = remainingAfterFixed,
                    minimumAction = minimumAction
                )
            }

            flexPressure >= minimumAction || goalShortfall >= minimumAction -> {
                val pressureAdjustment = practicalAmount(flexPressure.max(goalShortfall))
                createRecommendation(
                    direction = PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER,
                    plan = plan,
                    analyzedReviewCount = analyzedReviews.size,
                    currentFlexibleSpend = preview.flexibleSpendPerPayday,
                    currentPriorityContribution = preview.priorityContributionPerPayday,
                    targetPriorityContribution = averagePlannedPriority
                        .subtract(pressureAdjustment)
                        .max(BigDecimal.ZERO),
                    remainingAfterFixed = remainingAfterFixed,
                    minimumAction = minimumAction
                )
            }

            else -> PaydayAdjustmentRecommendation(
                direction = PaydayAdjustmentRecommendationDirection.KEEP_PLAN,
                analyzedReviewCount = analyzedReviews.size,
                currentFlexibleSpend = preview.flexibleSpendPerPayday,
                recommendedFlexibleSpend = preview.flexibleSpendPerPayday,
                currentPriorityContribution = preview.priorityContributionPerPayday,
                recommendedPriorityContribution = preview.priorityContributionPerPayday,
                adjustmentAmount = BigDecimal.ZERO.setScale(MONEY_SCALE),
                confidencePercent = confidenceFor(analyzedReviews.size),
                suggestedRules = emptyList()
            )
        }
    }

    private fun createRecommendation(
        direction: PaydayAdjustmentRecommendationDirection,
        plan: SalaryPlan,
        analyzedReviewCount: Int,
        currentFlexibleSpend: BigDecimal,
        currentPriorityContribution: BigDecimal,
        targetPriorityContribution: BigDecimal,
        remainingAfterFixed: BigDecimal,
        minimumAction: BigDecimal
    ): PaydayAdjustmentRecommendation {
        val normalizedTarget = targetPriorityContribution
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, remainingAfterFixed.multiply(MAX_PRIORITY_RATIO))
        val adjustment = normalizedTarget.subtract(currentPriorityContribution).abs()
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        if (adjustment < minimumAction) {
            return PaydayAdjustmentRecommendation(
                direction = PaydayAdjustmentRecommendationDirection.KEEP_PLAN,
                analyzedReviewCount = analyzedReviewCount,
                currentFlexibleSpend = currentFlexibleSpend,
                recommendedFlexibleSpend = currentFlexibleSpend,
                currentPriorityContribution = currentPriorityContribution,
                recommendedPriorityContribution = currentPriorityContribution,
                adjustmentAmount = BigDecimal.ZERO.setScale(MONEY_SCALE),
                confidencePercent = confidenceFor(analyzedReviewCount),
                suggestedRules = emptyList()
            )
        }
        val recommendedFlexibleSpend = remainingAfterFixed
            .subtract(normalizedTarget)
            .max(BigDecimal.ZERO)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)

        return PaydayAdjustmentRecommendation(
            direction = direction,
            analyzedReviewCount = analyzedReviewCount,
            currentFlexibleSpend = currentFlexibleSpend,
            recommendedFlexibleSpend = recommendedFlexibleSpend,
            currentPriorityContribution = currentPriorityContribution,
            recommendedPriorityContribution = normalizedTarget,
            adjustmentAmount = adjustment,
            confidencePercent = confidenceFor(analyzedReviewCount),
            suggestedRules = buildSuggestedRules(plan, normalizedTarget, remainingAfterFixed)
        )
    }

    private fun buildSuggestedRules(
        plan: SalaryPlan,
        targetPriorityContribution: BigDecimal,
        remainingAfterFixed: BigDecimal
    ): List<PaydayRule> {
        val sourceRules = effectiveRules(plan)
        val priorityRules = sourceRules.filter { it.type != PaydayRuleType.OTHER }
        if (priorityRules.isEmpty()) {
            return listOf(
                PaydayRule(
                    name = "Savings",
                    amount = percentageFor(targetPriorityContribution, remainingAfterFixed),
                    isPercentage = true,
                    type = PaydayRuleType.SAVINGS
                )
            )
        }

        val currentRuleAmounts = priorityRules.associateWith { rule ->
            rule.priorityAmount(remainingAfterFixed)
        }
        val currentTotal = currentRuleAmounts.values.fold(BigDecimal.ZERO, BigDecimal::add)

        return priorityRules.map { rule ->
            val targetAmount = if (currentTotal > BigDecimal.ZERO) {
                currentRuleAmounts.getValue(rule)
                    .multiply(targetPriorityContribution)
                    .divide(currentTotal, CALC_SCALE, RoundingMode.HALF_UP)
            } else {
                targetPriorityContribution.divide(
                    BigDecimal(priorityRules.size),
                    CALC_SCALE,
                    RoundingMode.HALF_UP
                )
            }
            rule.copy(
                amount = if (rule.isPercentage) {
                    percentageFor(targetAmount, remainingAfterFixed)
                } else {
                    targetAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                }
            )
        }
    }

    private fun effectiveRules(plan: SalaryPlan): List<PaydayRule> {
        return plan.rules.ifEmpty {
            resolveAllocationStrategyRulesUseCase.execute(plan.focus, plan.preset, plan.strategy)
        }
    }

    private fun ManualReview.toSignal(plan: SalaryPlan): ReviewSignal {
        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
        val plannedFlexible = plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
        val plannedGoal = plannedGoalContribution ?: preview.priorityContributionPerPayday
        return ReviewSignal(
            plannedGoalContribution = plannedGoal,
            flexibleSpendDelta = actualFlexibleSpend.subtract(plannedFlexible),
            goalContributionDelta = actualGoalContribution.subtract(plannedGoal)
        )
    }

    private fun PaydayRule.priorityAmount(remainingAfterFixed: BigDecimal): BigDecimal {
        return if (isPercentage) {
            remainingAfterFixed.multiply(amount)
                .divide(BigDecimal("100"), CALC_SCALE, RoundingMode.HALF_UP)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        } else {
            amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        }
    }

    private fun fixedCostsPerPayday(plan: SalaryPlan): BigDecimal = when (plan.payFrequency) {
        PayFrequency.MONTHLY -> plan.monthlyFixedCosts
        PayFrequency.BIWEEKLY -> plan.monthlyFixedCosts
            .multiply(BigDecimal("12"))
            .divide(BigDecimal("26"), CALC_SCALE, RoundingMode.HALF_UP)
    }

    private fun percentageFor(amount: BigDecimal, base: BigDecimal): BigDecimal {
        if (base <= BigDecimal.ZERO) return BigDecimal.ZERO.setScale(MONEY_SCALE)
        return amount.multiply(BigDecimal("100"))
            .divide(base, CALC_SCALE, RoundingMode.HALF_UP)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
    }

    private fun practicalAmount(amount: BigDecimal): BigDecimal {
        return amount.divide(PRACTICAL_INCREMENT, 0, RoundingMode.HALF_UP)
            .multiply(PRACTICAL_INCREMENT)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
    }

    private fun confidenceFor(reviewCount: Int): Int = when {
        reviewCount >= 3 -> 86
        reviewCount == 2 -> 74
        else -> 62
    }

    private fun List<ReviewSignal>.averageOf(selector: (ReviewSignal) -> BigDecimal): BigDecimal {
        return fold(BigDecimal.ZERO) { total, signal -> total.add(selector(signal)) }
            .divide(BigDecimal(size), CALC_SCALE, RoundingMode.HALF_UP)
    }

    private fun BigDecimal.coerceIn(minimum: BigDecimal, maximum: BigDecimal): BigDecimal {
        return max(minimum).min(maximum)
    }

    private data class ReviewSignal(
        val plannedGoalContribution: BigDecimal,
        val flexibleSpendDelta: BigDecimal,
        val goalContributionDelta: BigDecimal
    )

    private companion object {
        const val MONEY_SCALE = 2
        const val CALC_SCALE = 4
        const val MAX_REVIEWS = 3
        val MINIMUM_ACTION_AMOUNT: BigDecimal = BigDecimal("5.00")
        val MINIMUM_ACTION_RATIO: BigDecimal = BigDecimal("0.02")
        val OPPORTUNITY_CAPTURE_RATIO: BigDecimal = BigDecimal("0.50")
        val MAX_PRIORITY_RATIO: BigDecimal = BigDecimal("0.85")
        val PRACTICAL_INCREMENT: BigDecimal = BigDecimal("5")
    }
}
