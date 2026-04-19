package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import javax.inject.Inject

data class PerformanceStats(
    val healthScore: Int, // 0-100
    val currentStreak: Int,
    val totalSavingsBeyondGoal: BigDecimal,
    val totalReviews: Int
)

/**
 * Responsibility: Analyzes historical reviews to provide high-level motivation metrics.
 */
class GetPerformanceStatsUseCase @Inject constructor(
    private val repository: PlannerRepository
) {
    fun execute(): Flow<PerformanceStats> {
        return repository.observeReviews().map { reviews ->
            if (reviews.isEmpty()) {
                return@map PerformanceStats(0, 0, BigDecimal.ZERO, 0)
            }

            val sortedReviews = reviews.sortedByDescending { it.createdAt }
            
            // Calculate Positive Reviews
            val positiveReviews = reviews.filter { isPositive(it) }
            val healthScore = if (reviews.isNotEmpty()) {
                (positiveReviews.size * 100) / reviews.size
            } else 0

            // Calculate Streak
            var streak = 0
            for (review in sortedReviews) {
                if (isPositive(review)) {
                    streak++
                } else {
                    break
                }
            }

            // Calculate Flex Savings (How much we stayed UNDER budget in total)
            var totalFlexSavings = BigDecimal.ZERO
            reviews.forEach { review ->
                val planned = review.plannedFlexibleSpend ?: BigDecimal.ZERO
                if (planned > BigDecimal.ZERO && review.actualFlexibleSpend < planned) {
                    totalFlexSavings = totalFlexSavings.add(planned.subtract(review.actualFlexibleSpend))
                }
            }

            PerformanceStats(
                healthScore = healthScore,
                currentStreak = streak,
                totalSavingsBeyondGoal = totalFlexSavings,
                totalReviews = reviews.size
            )
        }
    }

    private fun isPositive(review: ManualReview): Boolean {
        val plannedFlex = review.plannedFlexibleSpend ?: return false
        val plannedGoal = review.plannedGoalContribution ?: return false
        
        val flexOk = review.actualFlexibleSpend <= plannedFlex
        val goalOk = review.actualGoalContribution >= plannedGoal
        
        return flexOk && goalOk
    }
}
