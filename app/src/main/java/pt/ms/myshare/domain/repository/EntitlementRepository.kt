package pt.ms.myshare.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.model.StoreProduct

interface EntitlementRepository {
    val entitlementState: Flow<EntitlementState>
    val isPro: Flow<Boolean>
    val availableProducts: Flow<List<StoreProduct>>
    val purchaseEvents: Flow<BillingPurchaseEvent>

    suspend fun checkActiveEntitlement()
    suspend fun purchasePlan(activity: Activity, product: StoreProduct): BillingFlowLaunchResult
    suspend fun restorePurchases()
}
