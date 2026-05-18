package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.first
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class AdjustGoalProgressForReviewCorrectionUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val entitlementRepository: EntitlementRepository
) {
    suspend fun execute(goalContributionDelta: BigDecimal) {
        if (goalContributionDelta.compareTo(BigDecimal.ZERO) == 0) return

        val goals = plannerRepository.observeGoals().first()
        if (goals.isEmpty()) {
            Timber.tag(TAG).d("Review correction skipped goal progress adjustment; no goals available")
            return
        }

        val targets = targetGoals(goals)
        if (targets.isEmpty()) return

        val isPro = entitlementRepository.isPro.first()
        if (!isPro || targets.size == 1) {
            adjustGoal(targets.first(), goalContributionDelta)
        } else {
            val splitDelta = goalContributionDelta.divide(
                BigDecimal(targets.size),
                2,
                RoundingMode.HALF_UP
            )
            targets.forEach { goal ->
                adjustGoal(goal, splitDelta)
            }
        }
    }

    private fun targetGoals(goals: List<Goal>): List<Goal> {
        val activeGoals = goals.filterNot { it.isCompleted }
        return activeGoals.ifEmpty { goals.take(1) }
    }

    private suspend fun adjustGoal(goal: Goal, delta: BigDecimal) {
        val adjustedProgress = goal.currentProgress.add(delta).coerceAtLeast(BigDecimal.ZERO)
        plannerRepository.saveGoal(
            goal.copy(
                currentProgress = adjustedProgress,
                isCompleted = adjustedProgress >= goal.targetAmount
            )
        )
        Timber.tag(TAG).d(
            "Goal progress adjusted from review correction. goalId=%s delta=%s progress=%s",
            goal.id,
            delta,
            adjustedProgress
        )
    }

    private companion object {
        const val TAG = "ReviewCorrection"
    }
}
