package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.PaydayReadiness
import pt.ms.myshare.domain.model.PaydayReadinessMission
import pt.ms.myshare.domain.model.PaydayReadinessMissionState
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.domain.model.PlanPreview
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject

class CreatePaydayReadinessUseCase @Inject constructor() {

    fun execute(preview: PlanPreview): PaydayReadiness {
        val billsProtected = preview.incomePerPayday >= preview.fixedCostsPerPayday
        val weeklyGuideReady = preview.weeklyFlexibleSpend > BigDecimal.ZERO
        val priorityMoveReady = preview.priorityContributionPerPayday > BigDecimal.ZERO
        val missions = listOf(
            PaydayReadinessMissionState(PaydayReadinessMission.PROTECT_BILLS, billsProtected),
            PaydayReadinessMissionState(PaydayReadinessMission.SET_WEEKLY_GUIDE, weeklyGuideReady),
            PaydayReadinessMissionState(PaydayReadinessMission.SET_PRIORITY_MOVE, priorityMoveReady)
        )
        val completedMissions = missions.count { it.isComplete }
        val totalMissions = missions.size
        val progress = completedMissions.toFloat() / totalMissions.toFloat()
        val status = when (completedMissions) {
            totalMissions -> PaydayReadinessStatus.READY
            totalMissions - 1 -> PaydayReadinessStatus.ALMOST_READY
            else -> PaydayReadinessStatus.NEEDS_ATTENTION
        }
        val nextAction = missions.firstOrNull { !it.isComplete }?.mission

        Timber.tag(TAG).d(
            "Payday readiness computed. status=%s completed=%d total=%d nextAction=%s",
            status,
            completedMissions,
            totalMissions,
            nextAction
        )

        return PaydayReadiness(
            status = status,
            progress = progress.coerceIn(0f, 1f),
            completedMissions = completedMissions,
            totalMissions = totalMissions,
            nextAction = nextAction,
            missions = missions
        )
    }

    private companion object {
        const val TAG = "PaydayReadiness"
    }
}
