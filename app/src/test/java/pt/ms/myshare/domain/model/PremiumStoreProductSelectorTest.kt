package pt.ms.myshare.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PremiumStoreProductSelectorTest {

    @Test
    fun `standard product ignores founder and save offers`() {
        val products = listOf(
            product(
                productId = PremiumSubscriptionProducts.MONTHLY_ID,
                offerId = PremiumSubscriptionProducts.MONTHLY_SAVE_OFFER_ID,
                offerTags = listOf(PremiumSubscriptionProducts.SAVE_OFFER_TAG)
            ),
            product(
                productId = PremiumSubscriptionProducts.MONTHLY_ID,
                offerId = null,
                offerTags = emptyList()
            )
        )

        val selected = PremiumStoreProductSelector.standardProduct(products, BillingPlan.MONTHLY)

        assertEquals(null, selected?.offerId)
    }

    @Test
    fun `standard product prefers free trial among normal offers`() {
        val products = listOf(
            product(
                productId = PremiumSubscriptionProducts.ANNUAL_ID,
                offerId = null,
                freeTrialDays = null
            ),
            product(
                productId = PremiumSubscriptionProducts.ANNUAL_ID,
                offerId = null,
                freeTrialDays = 7
            )
        )

        val selected = PremiumStoreProductSelector.standardProduct(products, BillingPlan.ANNUAL)

        assertEquals(7, selected?.freeTrialDays)
    }

    @Test
    fun `standard product returns null when only special offers exist`() {
        val products = listOf(
            product(
                productId = PremiumSubscriptionProducts.ANNUAL_ID,
                offerId = PremiumSubscriptionProducts.ANNUAL_FOUNDER_OFFER_ID,
                offerTags = listOf(PremiumSubscriptionProducts.FOUNDER_OFFER_TAG)
            )
        )

        val selected = PremiumStoreProductSelector.standardProduct(products, BillingPlan.ANNUAL)

        assertNull(selected)
    }

    private fun product(
        productId: String,
        offerId: String?,
        offerTags: List<String> = emptyList(),
        freeTrialDays: Int? = null
    ): StoreProduct {
        return StoreProduct(
            productId = productId,
            name = productId,
            description = productId,
            price = "1.99",
            basePlanId = null,
            offerToken = "token",
            offerId = offerId,
            offerTags = offerTags,
            freeTrialDays = freeTrialDays
        )
    }
}
