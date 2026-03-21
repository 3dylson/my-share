package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface EntitlementRepository {
    val isPro: Flow<Boolean>
    suspend fun setPro(value: Boolean)
    suspend fun restorePurchases()
}

class InMemoryEntitlementRepository : EntitlementRepository {
    private val _isPro = MutableStateFlow(false)
    override val isPro: Flow<Boolean> get() = _isPro

    override suspend fun setPro(value: Boolean) {
        _isPro.value = value
    }

    override suspend fun restorePurchases() {
        // Placeholder for future Play Billing / RevenueCat integration.
    }
}

