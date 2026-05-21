package pt.ms.myshare.domain.model

data class AppUpdateDecision(
    val policy: AppUpdatePolicy?,
    val shouldRequestImmediateUpdate: Boolean,
    val mustBlockAppUse: Boolean,
    val policyError: Throwable?
) {
    val canContinue: Boolean = !mustBlockAppUse
}
