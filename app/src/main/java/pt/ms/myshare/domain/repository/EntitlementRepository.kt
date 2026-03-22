package pt.ms.myshare.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.StoreProduct

interface EntitlementRepository {
    val isPro: Flow<Boolean>
    val availableProducts: Flow<List<StoreProduct>>

    suspend fun checkActiveEntitlement()
    suspend fun purchasePlan(activity: Activity, product: StoreProduct)
    suspend fun setPro(value: Boolean)
    suspend fun restorePurchases()
}
