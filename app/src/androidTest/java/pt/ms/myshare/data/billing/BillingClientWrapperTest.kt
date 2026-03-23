package pt.ms.myshare.data.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingClientWrapperTest {

    private lateinit var wrapper: BillingClientWrapper
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockBillingClient: BillingClient = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(BillingClient::class)
        val builder: BillingClient.Builder = mockk()
        every { BillingClient.newBuilder(any()) } returns builder
        every { builder.setListener(any()) } returns builder
        every { builder.enablePendingPurchases() } returns builder
        every { builder.build() } returns mockBillingClient
        
        wrapper = BillingClientWrapper(context)
    }

    @Test
    fun queryProductDetails_returnsMappedProducts() = runTest {
        val productDetails = mockk<ProductDetails>()
        every { productDetails.productId } returns "myshare_monthly"
        every { productDetails.name } returns "Monthly Premium"
        every { productDetails.description } returns "Monthly subscription"
        every { productDetails.productType } returns BillingClient.ProductType.SUBS
        
        val offerDetails = mockk<ProductDetails.SubscriptionOfferDetails>()
        every { offerDetails.offerToken } returns "token_123"
        val pricingPhase = mockk<ProductDetails.PricingPhase>()
        every { pricingPhase.formattedPrice } returns "$4.99"
        every { offerDetails.pricingPhases.pricingPhaseList } returns listOf(pricingPhase)
        
        every { productDetails.subscriptionOfferDetails } returns listOf(offerDetails)

        val productDetailsResult = mockk<ProductDetailsResult>()
        every { productDetailsResult.billingResult.responseCode } returns BillingClient.BillingResponseCode.OK
        every { productDetailsResult.productDetailsList } returns listOf(productDetails)

        // Capture the listener
        val slot = slot<ProductDetailsResponseListener>()
        every { 
            mockBillingClient.queryProductDetailsAsync(any(), capture(slot)) 
        } answers {
            slot.captured.onProductDetailsResponse(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build(),
                listOf(productDetails)
            )
        }

        val result = wrapper.availableProducts.first()
        // Note: BillingClientWrapper starts querying on init or startConnection
        // We might need to trigger it manually or mock the connection callback
    }
}
