package pt.ms.myshare.data.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import io.mockk.*
import javax.inject.Provider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.StoreProduct

class PlayBillingEntitlementRepositoryTest {

    private lateinit var repository: PlayBillingEntitlementRepository
    private val billingWrapper: BillingClientWrapper = mockk(relaxed = true)
    private val billingAuthSession = FakeBillingAuthSession()
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val firebaseFunctions: FirebaseFunctions = mockk()
    private val firestoreProvider: Provider<FirebaseFirestore> = Provider { firestore }
    private val firebaseFunctionsProvider: Provider<FirebaseFunctions> = Provider { firebaseFunctions }
    private val purchasesFlow = MutableStateFlow<List<Purchase>>(emptyList())
    private val hasLoadedActivePurchasesFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        every { billingWrapper.purchases } returns purchasesFlow
        every { billingWrapper.hasLoadedActivePurchases } returns hasLoadedActivePurchasesFlow
        every { billingWrapper.availableProducts } returns MutableStateFlow<List<StoreProduct>>(emptyList())
        every { billingWrapper.purchaseEvents } returns MutableSharedFlow<BillingPurchaseEvent>()
        repository = PlayBillingEntitlementRepository(
            billingWrapper,
            billingAuthSession,
            firestoreProvider,
            firebaseFunctionsProvider,
        )
    }

    @Test
    fun isPro_initiallyFalse() = runTest {
        assertFalse(repository.isPro.first())
    }

    @Test
    fun isPro_trueWhenActiveSubscriptionPurchaseExists() = runTest {
        val purchase = mockk<Purchase>()
        every { purchase.purchaseToken } returns "token_123"
        every { purchase.products } returns listOf("myshare_monthly")
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.isAcknowledged } returns false

        hasLoadedActivePurchasesFlow.value = true
        purchasesFlow.value = listOf(purchase)

        assertTrue(repository.isPro.first())
    }

    @Test
    fun purchasePlan_authenticatesBeforeLaunchingBillingFlow() = runTest {
        val activity = mockk<Activity>(relaxed = true)
        val product = StoreProduct(
            productId = "myshare_annual",
            name = "Annual",
            description = "Annual plan",
            price = "USD 22.99",
            basePlanId = "yearly",
            offerToken = "offer-token"
        )
        billingAuthSession.requiredUserIdResult = Result.success("firebase-user-123")
        coEvery { billingWrapper.launchBillingFlow(activity, product, any()) } returns BillingFlowLaunchResult.Launched

        val result = repository.purchasePlan(activity, product)

        assertEquals(BillingFlowLaunchResult.Launched, result)
        assertTrue(billingAuthSession.requiredUserIdCalled)
        coVerify {
            billingWrapper.launchBillingFlow(
                activity,
                product,
                ObfuscatedAccountIdFactory.fromFirebaseUid("firebase-user-123")
            )
        }
    }

    private class FakeBillingAuthSession : BillingAuthSession {
        override val userId = MutableStateFlow<String?>(null)
        var requiredUserIdCalled = false
        var requiredUserIdResult: Result<String> = Result.success("firebase-user")

        override fun currentUserId(): String? = userId.value

        override suspend fun requireAuthenticatedUserId(): Result<String> {
            requiredUserIdCalled = true
            return requiredUserIdResult
        }

        override suspend fun requireAuthenticatedSession(): Result<BillingAuthenticatedSession> =
            requiredUserIdResult.map { userId ->
                requiredUserIdCalled = true
                BillingAuthenticatedSession(userId = userId, idToken = "firebase-id-token")
            }
    }
}
