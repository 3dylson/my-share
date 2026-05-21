package pt.ms.myshare.domain.model

enum class PaydayReadinessStatus {
    READY,
    ALMOST_READY,
    NEEDS_ATTENTION
}

enum class PaydayReadinessMission {
    PROTECT_BILLS,
    SET_WEEKLY_GUIDE,
    SET_PRIORITY_MOVE
}

data class PaydayReadinessMissionState(
    val mission: PaydayReadinessMission,
    val isComplete: Boolean
)

data class PaydayReadiness(
    val status: PaydayReadinessStatus,
    val progress: Float,
    val completedMissions: Int,
    val totalMissions: Int,
    val nextAction: PaydayReadinessMission?,
    val missions: List<PaydayReadinessMissionState>
)
