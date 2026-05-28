package pt.ms.myshare.presentation.ui.paywall

import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.presentation.ui.localization.UiText

object BillingStatusMessageMapper {
    fun fromLaunchResult(result: BillingFlowLaunchResult): UiText = when (result) {
        BillingFlowLaunchResult.Launched -> BillingStatusMessageKeys.HANDOFF
        BillingFlowLaunchResult.ProductUnavailable -> BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE
        is BillingFlowLaunchResult.Failed -> BillingStatusMessageKeys.CHECKOUT_FAILED
    }

    fun fromPurchaseEvent(event: BillingPurchaseEvent): UiText = when (event) {
        BillingPurchaseEvent.Completed -> BillingStatusMessageKeys.COMPLETED
        BillingPurchaseEvent.Pending -> BillingStatusMessageKeys.PENDING
        BillingPurchaseEvent.Canceled -> BillingStatusMessageKeys.CANCELED
        is BillingPurchaseEvent.Failed -> BillingStatusMessageKeys.CHECKOUT_FAILED
    }
}
