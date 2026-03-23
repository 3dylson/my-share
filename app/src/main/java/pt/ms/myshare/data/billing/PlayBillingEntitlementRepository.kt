package pt.ms.myshare.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository

class PlayBillingEntitlementRepository(
    private val billingClientWrapper: BillingClientWrapper
) : EntitlementRepository {

    init {
        billingClientWrapper.startBillingConnection()
    }

    override val isPro: Flow<Boolean> = billingClientWrapper.purchases.map { purchases ->
        purchases.any { purchase -> 
            (purchase.products.contains("myshare_annual") || purchase.products.contains("myshare_monthly")) && 
            purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
        }
    }

    override val availableProducts: Flow<List<StoreProduct>> = billingClientWrapper.availableProducts

    override suspend fun checkActiveEntitlement() {
        billingClientWrapper.queryActivePurchases()
    }

    override suspend fun purchasePlan(activity: Activity, product: StoreProduct) {
        billingClientWrapper.launchBillingFlow(activity, product)
    }

    override suspend fun setPro(value: Boolean) {
        // Ignored in Play Billing mode unless for overriding/testing locally
    }

    override suspend fun restorePurchases() {
        billingClientWrapper.queryActivePurchases()
    }
}
