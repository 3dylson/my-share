package pt.ms.myshare.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.android.billingclient.api.Purchase
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import timber.log.Timber

class PlayBillingEntitlementRepository(
    private val billingClientWrapper: BillingClientWrapper,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseFunctions: FirebaseFunctions
) : EntitlementRepository {

    override val isPro: Flow<Boolean> = billingClientWrapper.purchases.map { purchases ->
        purchases.any { purchase ->
            (
                purchase.products.contains(PremiumSubscriptionProducts.ANNUAL_ID) ||
                    purchase.products.contains(PremiumSubscriptionProducts.MONTHLY_ID)
                ) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
    }

    override val availableProducts: Flow<List<StoreProduct>> = billingClientWrapper.availableProducts

    init {
        billingClientWrapper.startBillingConnection()
        CoroutineScope(Dispatchers.IO).launch {
            billingClientWrapper.purchases.collect { purchases ->
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifyAndAcknowledge(purchase)
                    }
                }
            }
        }

    }

    private fun verifyAndAcknowledge(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        val data = hashMapOf(
            "purchaseToken" to purchase.purchaseToken,
            "subscriptionId" to productId
        )

        firebaseFunctions
            .getHttpsCallable("verifySubscription")
            .call(data)
            .addOnSuccessListener { result ->
                val resultMap = result.data as? Map<*, *>
                val isValid = resultMap?.get("isValid") as? Boolean ?: false
                if (isValid) {
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
            }
            .addOnFailureListener { e ->
                Timber.tag("BillingRepo").e(e, "Verification failed")
            }
    }


    override suspend fun checkActiveEntitlement() {
        billingClientWrapper.queryActivePurchases()
    }

    override suspend fun purchasePlan(activity: Activity, product: StoreProduct) {
        val obfuscatedAccountId = ObfuscatedAccountIdFactory.fromFirebaseUid(firebaseAuth.currentUser?.uid)
        Timber.tag("BillingRepo").d(
            "Launching billing flow for product=%s authenticated=%s",
            product.productId,
            obfuscatedAccountId != null
        )
        billingClientWrapper.launchBillingFlow(activity, product, obfuscatedAccountId)
    }

    override suspend fun restorePurchases() {
        billingClientWrapper.queryActivePurchases()
    }
}
