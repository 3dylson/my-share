package pt.ms.myshare.data.billing

import android.content.Context
import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.domain.model.StoreProduct
import timber.log.Timber

class BillingClientWrapper(context: Context) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    val availableProducts = _availableProducts.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases = _purchases.asStateFlow()

    // We only have one premium subscription product id currently
    private val PREMIUM_SUB_ID = "myshare_premium"

    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing Client Connected")
                    queryProducts()
                    queryActivePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing Client Disconnected. Attempting to reconnect...")
                // Retry connection logic could go here
            }
        })
    }

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_SUB_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val storeProducts = productDetailsList.mapNotNull { details ->
                    val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    val basePlanId = details.subscriptionOfferDetails?.firstOrNull()?.basePlanId
                    val price = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    
                    if (offerToken != null && price != null) {
                        StoreProduct(
                            productId = details.productId,
                            name = details.name,
                            description = details.description,
                            price = price,
                            basePlanId = basePlanId,
                            offerToken = offerToken
                        )
                    } else null
                }
                _availableProducts.value = storeProducts
            }
        }
    }

    fun queryActivePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchases.value = purchasesList
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val validPurchases = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            _purchases.value = _purchases.value + validPurchases
            // Note: In production, we'd acknowledge here after backend verification
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.i("User cancelled purchase flow.")
        } else {
            Timber.e("Purchase error: \${billingResult.debugMessage}")
        }
    }

    fun launchBillingFlow(activity: Activity, product: StoreProduct) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product.productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build())
            ).build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            val productDetails = productDetailsList.find { it.productId == product.productId }
            if (productDetails != null) {
                val offerToken = product.offerToken ?: return@queryProductDetailsAsync
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    )
                    .build()
                billingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }
}
