package pt.ms.myshare.domain.model

object PremiumSubscriptionProducts {
    const val MONTHLY_ID = "myshare_monthly"
    const val ANNUAL_ID = "myshare_annual"
    const val MONTHLY_SAVE_OFFER_ID = "save50-1m"
    const val SAVE_OFFER_TAG = "save-offer"
    const val MONTHLY_BILLING_PERIOD = "P1M"
    const val ANNUAL_BILLING_PERIOD = "P1Y"

    fun productIdFor(plan: BillingPlan): String {
        return when (plan) {
            BillingPlan.MONTHLY -> MONTHLY_ID
            BillingPlan.ANNUAL -> ANNUAL_ID
        }
    }

    fun expectedBillingPeriod(productId: String): String? {
        return when (productId) {
            MONTHLY_ID -> MONTHLY_BILLING_PERIOD
            ANNUAL_ID -> ANNUAL_BILLING_PERIOD
            else -> null
        }
    }
}
