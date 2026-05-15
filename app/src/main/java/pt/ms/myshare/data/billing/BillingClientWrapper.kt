package pt.ms.myshare.data.billing

import android.content.Context
import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
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
                        .setProductId(PremiumSubscriptionProducts.MONTHLY_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PremiumSubscriptionProducts.ANNUAL_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val storeProducts = productDetailsResult.productDetailsList.mapNotNull { details ->
                    BillingProductMapper.map(details)
                }
                Timber.d("Billing products mapped: %d", storeProducts.size)
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
            val existing = _purchases.value.map { it.purchaseToken }.toSet()
            val newOnes = validPurchases.filter { it.purchaseToken !in existing }
            if (newOnes.isNotEmpty()) {
                _purchases.value = _purchases.value + newOnes
            }
            // Note: verify-and-acknowledge is triggered reactively by the collect loop in PlayBillingEntitlementRepository
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

        billingClient.queryProductDetailsAsync(params) { _, productDetailsResult ->
            val productDetails = productDetailsResult.productDetailsList.find { it.productId == product.productId }
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

    fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Purchase acknowledged successfully")
            } else {
                Timber.e("Error acknowledging purchase: \${billingResult.debugMessage}")
            }
        }
    }
}
