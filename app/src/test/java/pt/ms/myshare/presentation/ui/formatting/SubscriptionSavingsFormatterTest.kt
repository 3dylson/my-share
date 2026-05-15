package pt.ms.myshare.presentation.ui.formatting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.ms.myshare.domain.model.StoreProduct
import java.util.Locale

class SubscriptionSavingsFormatterTest {

    @Test
    fun `formats annual monthly equivalent and savings`() {
        val monthly = storeProduct(
            price = "$4.99",
            priceAmountMicros = 4_990_000,
            priceCurrencyCode = "USD"
        )
        val annual = storeProduct(
            price = "$39.88",
            priceAmountMicros = 39_880_000,
            priceCurrencyCode = "USD"
        )

        val comparison = SubscriptionSavingsFormatter.formatAnnualComparison(monthly, annual, Locale.US)

        assertEquals("$59.88", comparison?.monthlyEquivalentPrice)
        assertEquals("$20.00", comparison?.savingsPrice)
    }

    @Test
    fun `returns no comparison without numeric monthly price`() {
        val comparison = SubscriptionSavingsFormatter.formatAnnualComparison(
            monthlyProduct = storeProduct(priceAmountMicros = null, priceCurrencyCode = "USD"),
            annualProduct = storeProduct(priceAmountMicros = 39_880_000, priceCurrencyCode = "USD"),
            locale = Locale.US
        )

        assertNull(comparison)
    }

    private fun storeProduct(
        price: String = "$4.99",
        priceAmountMicros: Long? = 4_990_000,
        priceCurrencyCode: String? = "USD"
    ) = StoreProduct(
        productId = "product",
        name = "Product",
        description = "Description",
        price = price,
        basePlanId = "base",
        offerToken = "token",
        priceAmountMicros = priceAmountMicros,
        priceCurrencyCode = priceCurrencyCode
    )
}
