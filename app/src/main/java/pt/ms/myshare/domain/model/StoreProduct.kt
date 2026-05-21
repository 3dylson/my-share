package pt.ms.myshare.domain.model

data class StoreProduct(
    val productId: String,
    val name: String,
    val description: String,
    val price: String,
    val basePlanId: String?,
    val offerToken: String?,
    val priceAmountMicros: Long? = null,
    val priceCurrencyCode: String? = null,
    val recurringBillingPeriod: String? = null,
    val offerId: String? = null,
    val offerTags: List<String> = emptyList(),
    val freeTrialPeriod: String? = null,
    val freeTrialDays: Int? = null
) {
    val hasFreeTrial: Boolean
        get() = freeTrialDays != null && freeTrialDays > 0
}
