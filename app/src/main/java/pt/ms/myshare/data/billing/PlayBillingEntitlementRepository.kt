package pt.ms.myshare.data.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import timber.log.Timber
import javax.inject.Provider

class PlayBillingEntitlementRepository(
    private val billingClientWrapper: BillingClientWrapper,
    private val billingAuthSession: BillingAuthSession,
    private val firestoreProvider: Provider<FirebaseFirestore>,
    private val firebaseFunctionsProvider: Provider<FirebaseFunctions>
) : EntitlementRepository {

    private val localEntitlementState: Flow<EntitlementState> = combine(
        billingClientWrapper.purchases,
        billingClientWrapper.hasLoadedActivePurchases
    ) { purchases, hasLoadedPurchases ->
        if (!hasLoadedPurchases) {
            EntitlementState.UNKNOWN
        } else if (purchases.any { it.isActivePremiumSubscription() }) {
            Timber.tag(TAG).d("Local premium purchase found and waiting for server entitlement verification")
            EntitlementState.UNKNOWN
        } else {
            EntitlementState.FREE
        }
    }.distinctUntilChanged()

    private val serverEntitlementState: Flow<EntitlementState?> = observeServerEntitlementState()
    private val verifiedPurchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 1)
    private val purchaseVerificationLock = Any()
    private val verifiedPurchaseKeys = mutableSetOf<String>()
    private val purchaseVerificationInFlight = mutableSetOf<String>()

    override val entitlementState: Flow<EntitlementState> = combine(
        localEntitlementState,
        serverEntitlementState
    ) { localState, serverState ->
        serverState ?: localState
    }.distinctUntilChanged()

    override val isPro: Flow<Boolean> = entitlementState
        .map { it.hasPremiumAccess }
        .distinctUntilChanged()

    private fun observeServerEntitlementState(): Flow<EntitlementState?> = callbackFlow {
        var registration: ListenerRegistration? = null

        fun attachUser(uid: String?) {
            registration?.remove()
            registration = null
            if (uid == null) {
                trySend(null)
                return
            }

            registration = firestoreProvider.get().collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.tag(TAG).e(error, "Server entitlement snapshot failed")
                        trySend(null)
                        return@addSnapshotListener
                    }

                    val state = EntitlementSnapshotMapper.map(snapshot?.data)
                    Timber.tag(TAG).d("Server entitlement snapshot state=%s", state)
                    trySend(state)
                }
        }

        val authJob = launch {
            billingAuthSession.userId.collect { uid ->
                attachUser(uid)
            }
        }

        awaitClose {
            authJob.cancel()
            registration?.remove()
        }
    }.distinctUntilChanged()

    private fun Purchase.isActivePremiumSubscription(): Boolean {
        return (
            products.contains(PremiumSubscriptionProducts.ANNUAL_ID) ||
                products.contains(PremiumSubscriptionProducts.MONTHLY_ID)
            ) &&
            purchaseState == Purchase.PurchaseState.PURCHASED
    }

    override val availableProducts: Flow<List<StoreProduct>> = billingClientWrapper.availableProducts
    override val purchaseEvents: Flow<BillingPurchaseEvent> = merge(
        billingClientWrapper.purchaseEvents,
        verifiedPurchaseEvents
    )

    init {
        billingClientWrapper.startBillingConnection()
        CoroutineScope(Dispatchers.IO).launch {
            billingClientWrapper.purchases.collect { purchases ->
                purchases.forEach { purchase ->
                    if (purchase.isActivePremiumSubscription()) {
                        verifyAndAcknowledge(purchase)
                    }
                }
            }
        }

    }

    private suspend fun verifyAndAcknowledge(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return

        val session = billingAuthSession.requireAuthenticatedSession()
            .onFailure { e ->
                Timber.tag(TAG).e(e, "Cannot verify purchase because billing session authentication failed")
            }
            .getOrNull() ?: return
        if (!markPurchaseVerificationStarted(session.userId, purchase.purchaseToken)) return
        val data = hashMapOf(
            "purchaseToken" to purchase.purchaseToken,
            "subscriptionId" to productId,
            "firebaseIdToken" to session.idToken
        )

        try {
            Timber.tag(TAG).d("Verifying purchase product=%s userReady=%s", productId, session.userId.isNotBlank())
            val result = firebaseFunctionsProvider.get()
                .getHttpsCallable("verifySubscription")
                .call(data)
                .await()
            val resultMap = result.data as? Map<*, *>
            val isValid = resultMap?.get("isValid") as? Boolean ?: false
            if (isValid) {
                markPurchaseVerificationFinished(session.userId, purchase.purchaseToken, verified = true)
                verifiedPurchaseEvents.tryEmit(BillingPurchaseEvent.Completed)
                if (resultMap?.isServerAcknowledged() == true) {
                    Timber.tag(TAG).d("Purchase acknowledged by server product=%s", productId)
                } else if (!purchase.isAcknowledged) {
                    Timber.tag(TAG).d("Falling back to BillingClient acknowledgement product=%s", productId)
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
            } else {
                Timber.tag(TAG).e("Purchase verification returned inactive entitlement for product=%s", productId)
                markPurchaseVerificationFinished(session.userId, purchase.purchaseToken, verified = false)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Verification failed")
            markPurchaseVerificationFinished(session.userId, purchase.purchaseToken, verified = false)
        }
    }

    private fun markPurchaseVerificationStarted(userId: String, purchaseToken: String): Boolean {
        val key = purchaseVerificationKey(userId, purchaseToken)
        return synchronized(purchaseVerificationLock) {
            if (key in verifiedPurchaseKeys || key in purchaseVerificationInFlight) {
                false
            } else {
                purchaseVerificationInFlight.add(key)
                true
            }
        }
    }

    private fun markPurchaseVerificationFinished(userId: String, purchaseToken: String, verified: Boolean) {
        val key = purchaseVerificationKey(userId, purchaseToken)
        synchronized(purchaseVerificationLock) {
            purchaseVerificationInFlight.remove(key)
            if (verified) {
                verifiedPurchaseKeys.add(key)
            }
        }
    }

    private fun purchaseVerificationKey(userId: String, purchaseToken: String): String = "$userId:$purchaseToken"


    private fun Map<*, *>.isServerAcknowledged(): Boolean {
        return when (get("serverAcknowledgementStatus") as? String) {
            "acknowledged",
            "already_acknowledged" -> true
            else -> false
        }
    }

    override suspend fun checkActiveEntitlement() {
        billingClientWrapper.queryActivePurchases()
    }

    override suspend fun refreshProducts(): List<StoreProduct> {
        Timber.tag(TAG).d("Refreshing billing products")
        return billingClientWrapper.refreshProductsNow()
    }

    override suspend fun purchasePlan(activity: Activity, product: StoreProduct): BillingFlowLaunchResult {
        val userId = billingAuthSession.requireAuthenticatedUserId()
            .onFailure { e ->
                Timber.tag(TAG).e(e, "Cannot launch billing flow because billing session authentication failed")
            }
            .getOrNull()
            ?: return BillingFlowLaunchResult.Failed(
                responseCode = BillingClient.BillingResponseCode.ERROR,
                debugMessage = "Billing account authentication failed"
            )
        val obfuscatedAccountId = ObfuscatedAccountIdFactory.fromFirebaseUid(userId)
        Timber.tag(TAG).d(
            "Launching billing flow for product=%s authenticated=%s",
            product.productId,
            obfuscatedAccountId != null
        )
        return billingClientWrapper.launchBillingFlow(activity, product, obfuscatedAccountId)
    }

    override suspend fun restorePurchases() {
        billingClientWrapper.queryActivePurchases()
    }

    private companion object {
        const val TAG = "BillingRepo"
    }
}
