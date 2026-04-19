package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.presentation.ui.home.ReviewHistoryItemState
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import java.time.LocalDate

/**
 * Responsibility: Maps ManualReview records to UI-ready history items.
 * Calculates deltas between blueprint and actual performance.
 */
class GetReviewHistoryUseCase @Inject constructor(
    private val repository: PlannerRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    fun execute(): Flow<List<ReviewHistoryItemState>> {
        val plan = repository.loadPlan()
        
        return repository.observeReviews().map { reviews ->
            reviews.sortedByDescending { it.createdAt }.map { review ->
                mapToState(review, plan)
            }
        }
    }

    private fun mapToState(review: ManualReview, plan: SalaryPlan?): ReviewHistoryItemState {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        
        val dateLabel = review.createdAt.format(dateFormatter)
        
        // Use plan defaults or review snapshots if we had them (future enhancement)
        // For now, compare against the CURRENT plan blueprint
        val goals = repository.loadGoals() // Need this to get the target amount for preview
        val targetAmount = goals.firstOrNull()?.targetAmount ?: BigDecimal.ZERO
        
        val preview = plan?.let { calculatePlanPreviewUseCase.execute(it, targetAmount) }
        
        val blueprintFlexible = preview?.flexibleSpendPerPayday ?: BigDecimal.ZERO
        val blueprintGoal = preview?.savingsPerPayday ?: BigDecimal.ZERO // In this app, Goal Contribution = Savings
        
        val flexibleDelta = review.actualFlexibleSpend.subtract(blueprintFlexible)
        val goalDelta = review.actualGoalContribution.subtract(blueprintGoal)
        
        val isPositiveValue = flexibleDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO

        return ReviewHistoryItemState(
            id = review.id,
            dateLabel = dateLabel,
            flexibleSpendLabel = currencyFormatter.format(review.actualFlexibleSpend),
            goalContributionLabel = currencyFormatter.format(review.actualGoalContribution),
            flexibleDeltaLabel = formatDelta(flexibleDelta, currencyFormatter),
            goalDeltaLabel = formatDelta(goalDelta, currencyFormatter),
            isPositive = isPositiveValue
        )
    }

    private fun formatDelta(delta: BigDecimal, formatter: NumberFormat): String {
        val prefix = if (delta > BigDecimal.ZERO) "+" else if (delta < BigDecimal.ZERO) "-" else ""
        return "$prefix${formatter.format(delta.abs())}"
    }
}
