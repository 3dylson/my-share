package pt.ms.myshare.domain.model

sealed interface BillingFlowLaunchResult {
    data object Launched : BillingFlowLaunchResult
    data object ProductUnavailable : BillingFlowLaunchResult
    data class Failed(
        val responseCode: Int,
        val debugMessage: String?
    ) : BillingFlowLaunchResult
}
