package pt.ms.myshare.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayBillingEntitlementRepository(
    private val billingClientWrapper: BillingClientWrapper,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : EntitlementRepository {

    init {
        billingClientWrapper.startBillingConnection()
        CoroutineScope(Dispatchers.IO).launch {
            isPro.collect { proStatus ->
                val user = firebaseAuth.currentUser ?: return@collect
                val data = hashMapOf(
                    "isPro" to proStatus,
                    "updatedAtDate" to java.time.LocalDate.now().toString()
                )
                try {
                    firestore.collection("users").document(user.uid)
                        .collection("entitlements").document("snapshot").set(data)
                } catch (e: Exception) {
                    // Ignored
                }
            }
        }
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
