package pt.ms.myshare.presentation.ui.paywall

import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.localization.UiText

object BillingStatusMessageKeys {
    val STARTING = UiText.StringResource(R.string.paywall_billing_starting)
    val HANDOFF = UiText.StringResource(R.string.paywall_billing_handoff)
    val PRODUCTS_UNAVAILABLE = UiText.StringResource(R.string.paywall_billing_products_unavailable)
    val CHECKOUT_FAILED = UiText.StringResource(R.string.paywall_billing_checkout_failed)
    val CANCELED = UiText.StringResource(R.string.paywall_billing_canceled)
    val PENDING = UiText.StringResource(R.string.paywall_billing_pending)
    val COMPLETED = UiText.StringResource(R.string.paywall_billing_completed)
    val RESTORE_CHECKING = UiText.StringResource(R.string.paywall_restore_checking)
    val RESTORE_SUCCESS = UiText.StringResource(R.string.paywall_restore_success)
    val RESTORE_NONE = UiText.StringResource(R.string.paywall_restore_none)
}
