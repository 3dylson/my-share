package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.presentation.ui.home.ReviewHistoryItemState
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

/**
 * Responsibility: Maps ManualReview records to UI-ready history items.
 * Calculates deltas between blueprint and actual performance.
 */
class GetReviewHistoryUseCase @Inject constructor(
    private val repository: PlannerRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    fun execute(): Flow<List<ReviewHistoryItemState>> {
        return combine(
            repository.observeReviews(),
            repository.observePlan(),
            repository.observeGoals(),
            userPreferencesRepository.observePreferences()
        ) { reviews, plan, goals, preferences ->
            reviews.sortedByDescending { it.createdAt }.map { review ->
                mapToState(review, plan, goals, preferences.locale, preferences.currency)
            }
        }
    }

    private fun mapToState(
        review: ManualReview,
        plan: SalaryPlan?,
        goals: List<Goal>,
        locale: Locale,
        currency: Currency
    ): ReviewHistoryItemState {
        val currencyFormatter = NumberFormat.getCurrencyInstance(locale).apply { this.currency = currency }
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)

        val dateLabel = review.createdAt.format(dateFormatter)

        // Use snapshots if available, otherwise calculate against current plan
        val (blueprintFlexible, blueprintGoal) = if (review.plannedFlexibleSpend != null && review.plannedGoalContribution != null) {
            review.plannedFlexibleSpend to review.plannedGoalContribution
        } else {
            val targetAmount = goals.firstOrNull()?.targetAmount ?: BigDecimal.ZERO
            val preview = plan?.let { calculatePlanPreviewUseCase.execute(it, targetAmount) }
            (preview?.flexibleSpendPerPayday ?: BigDecimal.ZERO) to (preview?.priorityContributionPerPayday ?: BigDecimal.ZERO)
        }

        val flexibleDelta = review.actualFlexibleSpend.subtract(blueprintFlexible)
        val goalDelta = review.actualGoalContribution.subtract(blueprintGoal)

        // Positive performance: Spent less than or equal to flexible budget, saved more than or equal to goal target
        val isPositiveValue = flexibleDelta <= BigDecimal.ZERO && goalDelta >= BigDecimal.ZERO

        return ReviewHistoryItemState(
            id = review.id,
            dateLabel = dateLabel,
            flexibleSpendLabel = currencyFormatter.format(review.actualFlexibleSpend),
            plannedFlexibleLabel = currencyFormatter.format(blueprintFlexible),
            goalContributionLabel = currencyFormatter.format(review.actualGoalContribution),
            plannedGoalLabel = currencyFormatter.format(blueprintGoal),
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
