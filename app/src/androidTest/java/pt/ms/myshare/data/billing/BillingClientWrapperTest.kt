package pt.ms.myshare.data.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        every { builder.enablePendingPurchases(any()) } returns builder
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
        every { offerDetails.basePlanId } returns "monthly"
        every { offerDetails.offerId } returns null
        every { offerDetails.offerTags } returns emptyList()
        val pricingPhase = mockk<ProductDetails.PricingPhase>()
        every { pricingPhase.formattedPrice } returns "$4.99"
        every { pricingPhase.priceAmountMicros } returns 4_990_000L
        every { pricingPhase.recurrenceMode } returns ProductDetails.RecurrenceMode.INFINITE_RECURRING
        every { pricingPhase.billingPeriod } returns "P1M"
        every { pricingPhase.billingCycleCount } returns 0
        every { offerDetails.pricingPhases.pricingPhaseList } returns listOf(pricingPhase)
        
        every { productDetails.subscriptionOfferDetails } returns listOf(offerDetails)

        val productDetailsResult = mockk<QueryProductDetailsResult>()
        every { productDetailsResult.productDetailsList } returns listOf(productDetails)

        val connectionSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(connectionSlot)) } answers {
            connectionSlot.captured.onBillingSetupFinished(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.OK)
                    .build()
            )
        }

        val productDetailsSlot = slot<ProductDetailsResponseListener>()
        every { 
            mockBillingClient.queryProductDetailsAsync(any(), capture(productDetailsSlot)) 
        } answers {
            productDetailsSlot.captured.onProductDetailsResponse(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build(),
                productDetailsResult
            )
        }
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } just Runs

        wrapper.startBillingConnection()
        val result = wrapper.availableProducts.first()
        assertEquals(1, result.size)
        assertEquals("myshare_monthly", result.first().productId)
        assertEquals("$4.99", result.first().price)
        assertEquals("P1M", result.first().recurringBillingPeriod)
    }

    @Test
    fun queryProductDetails_prefersFreeTrialOfferAndDisplaysRecurringPrice() = runTest {
        val productDetails = mockk<ProductDetails>()
        every { productDetails.productId } returns "myshare_annual"
        every { productDetails.name } returns "Annual Premium"
        every { productDetails.description } returns "Annual subscription"
        every { productDetails.productType } returns BillingClient.ProductType.SUBS

        val baseOffer = mockk<ProductDetails.SubscriptionOfferDetails>()
        every { baseOffer.offerToken } returns "base_token"
        every { baseOffer.basePlanId } returns "annual"
        every { baseOffer.offerId } returns null
        every { baseOffer.offerTags } returns emptyList()
        val basePhase = mockk<ProductDetails.PricingPhase>()
        every { basePhase.formattedPrice } returns "$49.99"
        every { basePhase.priceAmountMicros } returns 49_990_000L
        every { basePhase.recurrenceMode } returns ProductDetails.RecurrenceMode.INFINITE_RECURRING
        every { basePhase.billingPeriod } returns "P1Y"
        every { basePhase.billingCycleCount } returns 0
        every { baseOffer.pricingPhases.pricingPhaseList } returns listOf(basePhase)

        val trialOffer = mockk<ProductDetails.SubscriptionOfferDetails>()
        every { trialOffer.offerToken } returns "trial_token"
        every { trialOffer.basePlanId } returns "annual"
        every { trialOffer.offerId } returns "seven-day-trial"
        every { trialOffer.offerTags } returns listOf("trial")
        val trialPhase = mockk<ProductDetails.PricingPhase>()
        every { trialPhase.formattedPrice } returns "$0.00"
        every { trialPhase.priceAmountMicros } returns 0L
        every { trialPhase.recurrenceMode } returns ProductDetails.RecurrenceMode.FINITE_RECURRING
        every { trialPhase.billingPeriod } returns "P1W"
        every { trialPhase.billingCycleCount } returns 1
        val recurringPhase = mockk<ProductDetails.PricingPhase>()
        every { recurringPhase.formattedPrice } returns "$49.99"
        every { recurringPhase.priceAmountMicros } returns 49_990_000L
        every { recurringPhase.recurrenceMode } returns ProductDetails.RecurrenceMode.INFINITE_RECURRING
        every { recurringPhase.billingPeriod } returns "P1Y"
        every { recurringPhase.billingCycleCount } returns 0
        every { trialOffer.pricingPhases.pricingPhaseList } returns listOf(trialPhase, recurringPhase)

        every { productDetails.subscriptionOfferDetails } returns listOf(baseOffer, trialOffer)

        val productDetailsResult = mockk<QueryProductDetailsResult>()
        every { productDetailsResult.productDetailsList } returns listOf(productDetails)

        val connectionSlot = slot<BillingClientStateListener>()
        every { mockBillingClient.startConnection(capture(connectionSlot)) } answers {
            connectionSlot.captured.onBillingSetupFinished(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.OK)
                    .build()
            )
        }

        val productDetailsSlot = slot<ProductDetailsResponseListener>()
        every {
            mockBillingClient.queryProductDetailsAsync(any(), capture(productDetailsSlot))
        } answers {
            productDetailsSlot.captured.onProductDetailsResponse(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build(),
                productDetailsResult
            )
        }
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } just Runs

        wrapper.startBillingConnection()
        val result = wrapper.availableProducts.first()
        assertEquals(1, result.size)
        assertEquals("trial_token", result.first().offerToken)
        assertEquals("$49.99", result.first().price)
        assertEquals(7, result.first().freeTrialDays)
        assertEquals("P1W", result.first().freeTrialPeriod)
    }
}
