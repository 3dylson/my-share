package pt.ms.myshare.presentation.ui.paywall

import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent

object BillingStatusMessageMapper {
    fun fromLaunchResult(result: BillingFlowLaunchResult): String = when (result) {
        BillingFlowLaunchResult.Launched -> BillingStatusMessageKeys.HANDOFF
        BillingFlowLaunchResult.ProductUnavailable -> BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE
        is BillingFlowLaunchResult.Failed -> BillingStatusMessageKeys.CHECKOUT_FAILED
    }

    fun fromPurchaseEvent(event: BillingPurchaseEvent): String = when (event) {
        BillingPurchaseEvent.Completed -> BillingStatusMessageKeys.COMPLETED
        BillingPurchaseEvent.Pending -> BillingStatusMessageKeys.PENDING
        BillingPurchaseEvent.Canceled -> BillingStatusMessageKeys.CANCELED
        is BillingPurchaseEvent.Failed -> BillingStatusMessageKeys.CHECKOUT_FAILED
    }
}
