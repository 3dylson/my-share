package pt.ms.myshare.data.billing

import android.content.Context
import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.StoreProduct
import timber.log.Timber
import kotlin.coroutines.resume

class BillingClientWrapper(context: Context) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()
    private var isConnectionInProgress = false

    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    val availableProducts = _availableProducts.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases = _purchases.asStateFlow()

    private val _hasLoadedActivePurchases = MutableStateFlow(false)
    val hasLoadedActivePurchases = _hasLoadedActivePurchases.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 1)
    val purchaseEvents = _purchaseEvents.asSharedFlow()

    fun startBillingConnection() {
        if (billingClient.isReady || isConnectionInProgress) {
            Timber.d("Billing Client connection already active or in progress")
            return
        }
        isConnectionInProgress = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnectionInProgress = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing Client Connected")
                    refreshProducts()
                    queryActivePurchases()
                } else {
                    Timber.e(
                        "Billing Client setup failed code=%d message=%s",
                        billingResult.responseCode,
                        billingResult.debugMessage
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnectionInProgress = false
                Timber.w("Billing Client Disconnected. Attempting to reconnect...")
                startBillingConnection()
            }
        })
    }

    fun refreshProducts() {
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
                Timber.d(
                    "Billing product query returned productDetails=%d unfetched=%d",
                    productDetailsResult.productDetailsList.size,
                    productDetailsResult.unfetchedProductList.size
                )
                val storeProducts = productDetailsResult.productDetailsList.flatMap { details ->
                    BillingProductMapper.map(details)
                }
                BillingDiagnosticsLogger.logUnfetchedProducts(
                    source = PRODUCT_REFRESH_SOURCE_INITIAL,
                    result = productDetailsResult
                )
                BillingDiagnosticsLogger.logProductRefresh(
                    source = PRODUCT_REFRESH_SOURCE_INITIAL,
                    result = productDetailsResult,
                    products = storeProducts
                )
                Timber.d("Billing products mapped: %d", storeProducts.size)
                _availableProducts.value = storeProducts
            } else {
                BillingDiagnosticsLogger.logProductRefreshFailure(
                    source = PRODUCT_REFRESH_SOURCE_INITIAL,
                    responseCode = billingResult.responseCode,
                    debugMessage = billingResult.debugMessage
                )
                Timber.e(
                    "Billing product query failed code=%d message=%s",
                    billingResult.responseCode,
                    billingResult.debugMessage
                )
            }
        }
    }

    suspend fun refreshProductsNow(): List<StoreProduct> = suspendCancellableCoroutine { continuation ->
        if (!billingClient.isReady) {
            Timber.d("Billing Client not ready. Product refresh deferred until connection completes")
            startBillingConnection()
            continuation.resume(_availableProducts.value)
            return@suspendCancellableCoroutine
        }

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
            if (!continuation.isActive) return@queryProductDetailsAsync
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d(
                    "Billing product refresh returned productDetails=%d unfetched=%d",
                    productDetailsResult.productDetailsList.size,
                    productDetailsResult.unfetchedProductList.size
                )
                val storeProducts = productDetailsResult.productDetailsList.flatMap { details ->
                    BillingProductMapper.map(details)
                }
                BillingDiagnosticsLogger.logUnfetchedProducts(
                    source = PRODUCT_REFRESH_SOURCE_MANUAL,
                    result = productDetailsResult
                )
                BillingDiagnosticsLogger.logProductRefresh(
                    source = PRODUCT_REFRESH_SOURCE_MANUAL,
                    result = productDetailsResult,
                    products = storeProducts
                )
                Timber.d("Billing products refreshed: %d", storeProducts.size)
                _availableProducts.value = storeProducts
                continuation.resume(storeProducts)
            } else {
                BillingDiagnosticsLogger.logProductRefreshFailure(
                    source = PRODUCT_REFRESH_SOURCE_MANUAL,
                    responseCode = billingResult.responseCode,
                    debugMessage = billingResult.debugMessage
                )
                Timber.e(
                    "Billing product refresh failed code=%d message=%s",
                    billingResult.responseCode,
                    billingResult.debugMessage
                )
                continuation.resume(_availableProducts.value)
            }
        }
    }

    fun queryActivePurchases() {
        if (!billingClient.isReady) {
            Timber.d("Billing Client not ready. Active purchase refresh deferred until connection completes")
            startBillingConnection()
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchases.value = purchasesList
                _hasLoadedActivePurchases.value = true
                Timber.d("Active subscription purchases loaded count=%d", purchasesList.size)
            } else {
                Timber.e(
                    "Active subscription purchase query failed code=%d message=%s",
                    billingResult.responseCode,
                    billingResult.debugMessage
                )
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                val validPurchases = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                val pendingPurchases = purchases.filter { it.purchaseState == Purchase.PurchaseState.PENDING }
                val existing = _purchases.value.map { it.purchaseToken }.toSet()
                val newOnes = validPurchases.filter { it.purchaseToken !in existing }
                if (newOnes.isNotEmpty()) {
                    _purchases.value = _purchases.value + newOnes
                }
                if (validPurchases.isNotEmpty()) {
                    Timber.d("Purchase update received purchased tokens count=%d", validPurchases.size)
                } else if (pendingPurchases.isNotEmpty()) {
                    Timber.d("Purchase update pending count=%d", pendingPurchases.size)
                    _purchaseEvents.tryEmit(BillingPurchaseEvent.Pending)
                }
                // Completion is emitted only after server verification in PlayBillingEntitlementRepository.
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("User cancelled purchase flow.")
                _purchaseEvents.tryEmit(BillingPurchaseEvent.Canceled)
            }
            else -> {
                Timber.e("Purchase error: %s", billingResult.debugMessage)
                _purchaseEvents.tryEmit(
                    BillingPurchaseEvent.Failed(
                        responseCode = billingResult.responseCode,
                        debugMessage = billingResult.debugMessage
                    )
                )
            }
        }
    }

    suspend fun launchBillingFlow(
        activity: Activity,
        product: StoreProduct,
        obfuscatedAccountId: String? = null
    ): BillingFlowLaunchResult = suspendCancellableCoroutine { continuation ->
        if (!billingClient.isReady) {
            Timber.e("Cannot launch billing flow because Billing Client is not ready")
            startBillingConnection()
            continuation.resume(
                BillingFlowLaunchResult.Failed(
                    responseCode = BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    debugMessage = "Billing service is not ready"
                )
            )
            return@suspendCancellableCoroutine
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product.productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build())
            ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (!continuation.isActive) {
                return@queryProductDetailsAsync
            }
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Timber.e(
                    "Billing product details lookup failed code=%d message=%s product=%s",
                    billingResult.responseCode,
                    billingResult.debugMessage,
                    product.productId
                )
                continuation.resume(
                    BillingFlowLaunchResult.Failed(
                        responseCode = billingResult.responseCode,
                        debugMessage = billingResult.debugMessage
                    )
                )
                return@queryProductDetailsAsync
            }
            val productDetails = productDetailsResult.productDetailsList.find { it.productId == product.productId }
            if (productDetails == null) {
                Timber.e("Billing product details unavailable for product=%s", product.productId)
                continuation.resume(BillingFlowLaunchResult.ProductUnavailable)
                return@queryProductDetailsAsync
            }
            val offerToken = product.offerToken
            if (offerToken.isNullOrBlank()) {
                Timber.e("Billing offer token unavailable for product=%s", product.productId)
                continuation.resume(BillingFlowLaunchResult.ProductUnavailable)
                return@queryProductDetailsAsync
            }
            val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
            if (obfuscatedAccountId != null) {
                billingFlowParamsBuilder.setObfuscatedAccountId(obfuscatedAccountId)
            }
            val launchResult = billingClient.launchBillingFlow(activity, billingFlowParamsBuilder.build())
            if (launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("Billing flow launch accepted for product=%s", product.productId)
                continuation.resume(BillingFlowLaunchResult.Launched)
            } else {
                Timber.e(
                    "Billing flow launch failed code=%d message=%s product=%s",
                    launchResult.responseCode,
                    launchResult.debugMessage,
                    product.productId
                )
                continuation.resume(
                    BillingFlowLaunchResult.Failed(
                        responseCode = launchResult.responseCode,
                        debugMessage = launchResult.debugMessage
                    )
                )
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
                Timber.e("Error acknowledging purchase: %s", billingResult.debugMessage)
            }
        }
    }

    private companion object {
        const val PRODUCT_REFRESH_SOURCE_INITIAL = "initial"
        const val PRODUCT_REFRESH_SOURCE_MANUAL = "manual"
    }
}
