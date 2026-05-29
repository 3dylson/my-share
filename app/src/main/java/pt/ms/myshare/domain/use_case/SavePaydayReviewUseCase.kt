package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.first
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class SavePaydayReviewUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val adjustGoalProgressForReviewCorrectionUseCase: AdjustGoalProgressForReviewCorrectionUseCase
) {
    suspend fun execute(
        plan: SalaryPlan,
        leftInSpendingPot: BigDecimal,
        movedToGoal: BigDecimal
    ): SavePaydayReviewResult {
        val targetAmount = plannerRepository.loadGoals().firstOrNull()?.targetAmount ?: BigDecimal.ZERO
        val preview = calculatePlanPreviewUseCase.execute(plan, targetAmount)
        val today = LocalDate.now()
        val reviewToReplace = plannerRepository.observeReviews().first().firstOrNull { existing ->
            existing.paydayDate == null && existing.createdAt == today
        }
        val plannedFlexibleSpend = reviewToReplace?.plannedFlexibleSpend ?: preview.flexibleSpendPerPayday
        val plannedGoalContribution = reviewToReplace?.plannedGoalContribution ?: preview.priorityContributionPerPayday
        val actualFlexibleSpend = plannedFlexibleSpend
            .subtract(leftInSpendingPot.coerceAtLeast(BigDecimal.ZERO))
            .max(BigDecimal.ZERO)
        val review = ManualReview(
            id = reviewToReplace?.id ?: java.util.UUID.randomUUID().toString(),
            actualFlexibleSpend = actualFlexibleSpend,
            actualGoalContribution = movedToGoal,
            plannedFlexibleSpend = plannedFlexibleSpend,
            plannedGoalContribution = plannedGoalContribution
        )

        if (reviewToReplace != null) {
            plannerRepository.updateReview(review)
            adjustGoalProgressForReviewCorrectionUseCase.execute(
                movedToGoal.subtract(reviewToReplace.actualGoalContribution)
            )
        } else {
            plannerRepository.saveReview(review)
            updateGoalProgressUseCase.execute(movedToGoal)
        }

        Timber.tag(TAG).d(
            "Payday review saved. replaced=%s plannedFlex=%s leftInPot=%s actualFlex=%s movedToGoal=%s plannedGoal=%s",
            reviewToReplace != null,
            plannedFlexibleSpend,
            leftInSpendingPot,
            actualFlexibleSpend,
            movedToGoal,
            plannedGoalContribution
        )

        return SavePaydayReviewResult(
            review = review,
            leftInSpendingPot = leftInSpendingPot,
            plannedFlexibleSpend = plannedFlexibleSpend,
            plannedGoalContribution = plannedGoalContribution
        )
    }

    private companion object {
        const val TAG = "SavePaydayReview"
    }
}

data class SavePaydayReviewResult(
    val review: ManualReview,
    val leftInSpendingPot: BigDecimal,
    val plannedFlexibleSpend: BigDecimal,
    val plannedGoalContribution: BigDecimal
)
