package pt.ms.myshare.data.billing

import com.android.billingclient.api.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayBillingEntitlementRepositoryTest {

    private lateinit var repository: PlayBillingEntitlementRepository
    private val billingWrapper: BillingClientWrapper = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val firebaseFunctions: FirebaseFunctions = mockk()
    private val purchasesFlow = MutableStateFlow<List<Purchase>>(emptyList())

    @Before
    fun setup() {
        every { billingWrapper.purchases } returns purchasesFlow
        repository = PlayBillingEntitlementRepository(
            billingWrapper,
            firebaseAuth,
            firestore,
            firebaseFunctions,
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

        purchasesFlow.value = listOf(purchase)

        assertTrue(repository.isPro.first())
    }
}
