package pt.ms.myshare.domain.model

object PremiumStoreProductSelector {

    fun standardProduct(products: List<StoreProduct>, plan: BillingPlan): StoreProduct? {
        val productId = PremiumSubscriptionProducts.productIdFor(plan)
        return products
            .filter { it.productId == productId }
            .filterNot { it.isSpecialOffer() }
            .sortedWith(compareByDescending<StoreProduct> { it.hasFreeTrial })
            .firstOrNull()
    }

    fun StoreProduct.isSpecialOffer(): Boolean {
        return isSubscriptionSaveOffer() || isFounderOffer()
    }

    fun StoreProduct.isSubscriptionSaveOffer(): Boolean {
        return offerId == PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID ||
            PremiumSubscriptionProducts.SAVE_OFFER_TAG in offerTags
    }

    fun StoreProduct.isFounderOffer(): Boolean {
        return offerId == PremiumSubscriptionProducts.ANNUAL_FOUNDER_OFFER_ID ||
            PremiumSubscriptionProducts.FOUNDER_OFFER_TAG in offerTags
    }
}
