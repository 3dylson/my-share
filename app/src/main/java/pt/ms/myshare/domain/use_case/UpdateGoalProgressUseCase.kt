package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.first
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Responsibility: Syncs ManualReview contributions into the Progress of Goals.
 */
class UpdateGoalProgressUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val entitlementRepository: EntitlementRepository
) {
    suspend fun execute(actualContribution: BigDecimal) {
        if (actualContribution <= BigDecimal.ZERO) return

        val isPro = entitlementRepository.isPro.first()
        val allGoals = plannerRepository.observeGoals().first()
        val activeGoals = allGoals.filter { !it.isCompleted }

        if (activeGoals.isEmpty()) return

        if (!isPro || activeGoals.size == 1) {
            // All contribution goes to the FIRST active goal
            val primaryGoal = activeGoals.first()
            val newProgress = primaryGoal.currentProgress.add(actualContribution)
            plannerRepository.saveGoal(
                primaryGoal.copy(
                    currentProgress = newProgress,
                    isCompleted = newProgress >= primaryGoal.targetAmount
                )
            )
        } else {
            // Split contribution equally among all active goals
            val splitAmount = actualContribution.divide(
                BigDecimal(activeGoals.size),
                2,
                RoundingMode.HALF_UP
            )
            
            activeGoals.forEach { goal ->
                val newProgress = goal.currentProgress.add(splitAmount)
                plannerRepository.saveGoal(
                    goal.copy(
                        currentProgress = newProgress,
                        isCompleted = newProgress >= goal.targetAmount
                    )
                )
            }
        }
    }
}
