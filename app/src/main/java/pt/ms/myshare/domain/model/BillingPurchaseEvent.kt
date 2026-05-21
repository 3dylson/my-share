package pt.ms.myshare.domain.model

sealed interface BillingPurchaseEvent {
    data object Completed : BillingPurchaseEvent
    data object Pending : BillingPurchaseEvent
    data object Canceled : BillingPurchaseEvent
    data class Failed(
        val responseCode: Int,
        val debugMessage: String?
    ) : BillingPurchaseEvent
}
