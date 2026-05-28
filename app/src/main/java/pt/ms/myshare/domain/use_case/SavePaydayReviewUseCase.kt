package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class SavePaydayReviewUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase
) {
    suspend fun execute(
        plan: SalaryPlan,
        leftInSpendingPot: BigDecimal,
        movedToGoal: BigDecimal
    ): SavePaydayReviewResult {
        val targetAmount = plannerRepository.loadGoals().firstOrNull()?.targetAmount ?: BigDecimal.ZERO
        val preview = calculatePlanPreviewUseCase.execute(plan, targetAmount)
        val actualFlexibleSpend = preview.flexibleSpendPerPayday
            .subtract(leftInSpendingPot.coerceAtLeast(BigDecimal.ZERO))
            .max(BigDecimal.ZERO)
        val review = ManualReview(
            actualFlexibleSpend = actualFlexibleSpend,
            actualGoalContribution = movedToGoal,
            plannedFlexibleSpend = preview.flexibleSpendPerPayday,
            plannedGoalContribution = preview.priorityContributionPerPayday
        )

        plannerRepository.saveReview(review)
        updateGoalProgressUseCase.execute(movedToGoal)

        Timber.tag(TAG).d(
            "Payday review saved. plannedFlex=%s leftInPot=%s actualFlex=%s movedToGoal=%s plannedGoal=%s",
            preview.flexibleSpendPerPayday,
            leftInSpendingPot,
            actualFlexibleSpend,
            movedToGoal,
            preview.priorityContributionPerPayday
        )

        return SavePaydayReviewResult(
            review = review,
            leftInSpendingPot = leftInSpendingPot,
            plannedFlexibleSpend = preview.flexibleSpendPerPayday,
            plannedGoalContribution = preview.priorityContributionPerPayday
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
