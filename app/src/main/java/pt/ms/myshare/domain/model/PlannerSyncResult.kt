package pt.ms.myshare.domain.model

data class PlannerSyncResult(
    val localPlanUploaded: Boolean,
    val remotePlanPreserved: Boolean,
    val ruleCount: Int,
    val goalCount: Int,
    val reviewCount: Int,
    val premiumAdjustmentCount: Int
)
