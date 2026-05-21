package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.emptyFlow
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository

class CheckEntitlementLimitUseCaseTest {

    private lateinit var useCase: CheckEntitlementLimitUseCase
    private lateinit var fakeRepository: FakeEntitlementRepository

    @Before
    fun setup() {
        fakeRepository = FakeEntitlementRepository()
        useCase = CheckEntitlementLimitUseCase(fakeRepository)
    }

    @Test
    fun `canUsePreset allows BALANCED for free users`() = runTest {
        fakeRepository.setProState(false)
        assertTrue(useCase.canUsePreset(AllocationPreset.BALANCED))
    }

    @Test
    fun `canUsePreset denies CONSERVATIVE and GROWTH for free users`() = runTest {
        fakeRepository.setProState(false)
        assertFalse(useCase.canUsePreset(AllocationPreset.CONSERVATIVE))
        assertFalse(useCase.canUsePreset(AllocationPreset.GROWTH))
    }

    @Test
    fun `canUsePreset allows any preset for PRO users`() = runTest {
        fakeRepository.setProState(true)
        assertTrue(useCase.canUsePreset(AllocationPreset.CONSERVATIVE))
        assertTrue(useCase.canUsePreset(AllocationPreset.GROWTH))
        assertTrue(useCase.canUsePreset(AllocationPreset.BALANCED))
    }

    @Test
    fun `canAddMultipleGoals allows first goal for free users`() = runTest {
        fakeRepository.setProState(false)
        assertTrue(useCase.canAddMultipleGoals(0))
    }

    @Test
    fun `canAddMultipleGoals denies second goal for free users`() = runTest {
        fakeRepository.setProState(false)
        assertFalse(useCase.canAddMultipleGoals(1))
    }

    @Test
    fun `canAddMultipleGoals allows unlimited goals for PRO users`() = runTest {
        fakeRepository.setProState(true)
        assertTrue(useCase.canAddMultipleGoals(0))
        assertTrue(useCase.canAddMultipleGoals(1))
        assertTrue(useCase.canAddMultipleGoals(50))
    }

    @Test
    fun `canAddMultipleRules allows first rule for free users`() = runTest {
        fakeRepository.setProState(false)
        assertTrue(useCase.canAddMultipleRules(0))
    }

    @Test
    fun `canAddMultipleRules denies second rule for free users`() = runTest {
        fakeRepository.setProState(false)
        assertFalse(useCase.canAddMultipleRules(1))
    }

    @Test
    fun `canAddMultipleRules allows unlimited rules for PRO users`() = runTest {
        fakeRepository.setProState(true)
        assertTrue(useCase.canAddMultipleRules(0))
        assertTrue(useCase.canAddMultipleRules(1))
        assertTrue(useCase.canAddMultipleRules(50))
    }

    @Test
    fun `canViewReviewHistoryDepth limits free users to 3`() = runTest {
        fakeRepository.setProState(false)
        assertTrue(useCase.canViewReviewHistoryDepth(0))
        assertTrue(useCase.canViewReviewHistoryDepth(2))
        assertFalse(useCase.canViewReviewHistoryDepth(3))
        assertFalse(useCase.canViewReviewHistoryDepth(10))
    }

    @Test
    fun `canViewReviewHistoryDepth allows infinite depth for PRO users`() = runTest {
        fakeRepository.setProState(true)
        assertTrue(useCase.canViewReviewHistoryDepth(0))
        assertTrue(useCase.canViewReviewHistoryDepth(3))
        assertTrue(useCase.canViewReviewHistoryDepth(12))
        assertTrue(useCase.canViewReviewHistoryDepth(100))
    }
}

class FakeEntitlementRepository : EntitlementRepository {
    private val _isPro = MutableStateFlow(false)
    override val isPro = _isPro.asStateFlow()
    private val _entitlementState = MutableStateFlow(EntitlementState.FREE)
    override val entitlementState = _entitlementState.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    override val availableProducts = _availableProducts.asStateFlow()
    override val purchaseEvents = emptyFlow<BillingPurchaseEvent>()

    suspend fun setProState(value: Boolean) {
        _isPro.emit(value)
        _entitlementState.emit(if (value) EntitlementState.PRO else EntitlementState.FREE)
    }

    override suspend fun checkActiveEntitlement() {}

    override suspend fun refreshProducts(): List<StoreProduct> = _availableProducts.value

    override suspend fun purchasePlan(activity: android.app.Activity, product: StoreProduct): BillingFlowLaunchResult =
        BillingFlowLaunchResult.ProductUnavailable

    override suspend fun restorePurchases() {}
}
