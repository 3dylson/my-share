package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.PremiumGoalPaydaySplit
import pt.ms.myshare.domain.model.PremiumGoalPaydaySplitItem
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreatePremiumGoalPaydaySplitUseCase @Inject constructor() {

    fun execute(
        goals: List<Goal>,
        priorityMove: BigDecimal
    ): PremiumGoalPaydaySplit? {
        val activeGoals = goals.filter { goal ->
            !goal.isCompleted && goal.targetAmount > BigDecimal.ZERO && goal.currentProgress < goal.targetAmount
        }
        val totalMove = priorityMove.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        if (activeGoals.size < MINIMUM_GOAL_COUNT || totalMove <= BigDecimal.ZERO) {
            return null
        }

        val sharePercents = sharePercentsFor(activeGoals.size)
        var allocatedAmount = BigDecimal.ZERO
        val items = activeGoals.mapIndexed { index, goal ->
            val amount = if (index == activeGoals.lastIndex) {
                totalMove.subtract(allocatedAmount).setScale(2, RoundingMode.HALF_UP)
            } else {
                totalMove
                    .multiply(sharePercents[index])
                    .divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP)
                    .also { allocatedAmount = allocatedAmount.add(it) }
            }

            PremiumGoalPaydaySplitItem(
                goalId = goal.id,
                amount = amount,
                sharePercent = sharePercents[index]
            )
        }

        return PremiumGoalPaydaySplit(
            totalMove = totalMove,
            items = items
        )
    }

    private fun sharePercentsFor(goalCount: Int): List<BigDecimal> {
        return when (goalCount) {
            2 -> listOf(BigDecimal("65"), BigDecimal("35"))
            3 -> listOf(BigDecimal("50"), BigDecimal("30"), BigDecimal("20"))
            else -> {
                val trailingShare = BigDecimal("25").divide(
                    BigDecimal(goalCount - 2),
                    SCALE,
                    RoundingMode.HALF_UP
                )
                listOf(BigDecimal("50"), BigDecimal("25")) + List(goalCount - 2) { trailingShare }
            }
        }
    }

    private companion object {
        const val MINIMUM_GOAL_COUNT = 2
        const val SCALE = 4
        val ONE_HUNDRED: BigDecimal = BigDecimal("100")
    }
}
