package pt.ms.myshare.presentation.ui.paywall

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent

class BillingStatusMessageMapperTest {
    @Test
    fun `launch result maps to truthful paywall message`() {
        assertEquals(
            BillingStatusMessageKeys.HANDOFF,
            BillingStatusMessageMapper.fromLaunchResult(BillingFlowLaunchResult.Launched)
        )
        assertEquals(
            BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE,
            BillingStatusMessageMapper.fromLaunchResult(BillingFlowLaunchResult.ProductUnavailable)
        )
        assertEquals(
            BillingStatusMessageKeys.CHECKOUT_FAILED,
            BillingStatusMessageMapper.fromLaunchResult(BillingFlowLaunchResult.Failed(3, "Billing unavailable"))
        )
    }

    @Test
    fun `purchase event maps to post-checkout paywall message`() {
        assertEquals(
            BillingStatusMessageKeys.COMPLETED,
            BillingStatusMessageMapper.fromPurchaseEvent(BillingPurchaseEvent.Completed)
        )
        assertEquals(
            BillingStatusMessageKeys.PENDING,
            BillingStatusMessageMapper.fromPurchaseEvent(BillingPurchaseEvent.Pending)
        )
        assertEquals(
            BillingStatusMessageKeys.CANCELED,
            BillingStatusMessageMapper.fromPurchaseEvent(BillingPurchaseEvent.Canceled)
        )
        assertEquals(
            BillingStatusMessageKeys.CHECKOUT_FAILED,
            BillingStatusMessageMapper.fromPurchaseEvent(BillingPurchaseEvent.Failed(5, "Developer error"))
        )
    }
}
