package pt.ms.myshare.presentation.ui.paywall

import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private lateinit viewModel: PaywallViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val isProFlow = MutableStateFlow(false)
    private val productsFlow = MutableStateFlow<List<StoreProduct>>(emptyList())
    private var purchaseInvoked = false
    private var restoreInvoked = false
    private var restoreThrows = false

    private val fakeRepository = object : EntitlementRepository {
        override val isPro = isProFlow
        override val availableProducts = productsFlow
        
        override suspend fun purchasePlan(activity: Activity, product: StoreProduct) {
            purchaseInvoked = true
        }

        override suspend fun restorePurchases() {
            restoreInvoked = true
            if (restoreThrows) throw Exception("Test Error")
        }

        override fun getEntitlements(): Boolean = false
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaywallViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `purchasePlan toggles isLoading and calls repository`() = runTest {
        val dummyProduct = StoreProduct("sub_yearly", "Pro Yearly", "19.99")
        val fakeActivity = org.mockito.Mockito.mock(Activity::class.java)

        assertFalse(viewModel.isLoading.value)
        
        viewModel.purchasePlan(fakeActivity, dummyProduct)
        
        // Advance so scope starts executing
        testDispatcher.scheduler.advanceTimeBy(1)
        assertTrue(viewModel.isLoading.value)

        // Advance to finish coroutine
        advanceUntilIdle()
        
        assertTrue(purchaseInvoked)
        assertFalse(viewModel.isLoading.value) // Finally block should reset it
    }

    @Test
    fun `restorePurchases handles success and resets loading`() = runTest {
        viewModel.restorePurchases()
        testDispatcher.scheduler.advanceTimeBy(1)
        assertTrue(viewModel.isLoading.value)
        
        advanceUntilIdle()
        
        assertTrue(restoreInvoked)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `restorePurchases handles failure, resets loading gracefully`() = runTest {
        restoreThrows = true
        viewModel.restorePurchases()
        
        testDispatcher.scheduler.advanceTimeBy(1)
        assertTrue(viewModel.isLoading.value)
        
        advanceUntilIdle()
        
        assertTrue(restoreInvoked)
        assertFalse(viewModel.isLoading.value) // Finally block catches all
    }
}
