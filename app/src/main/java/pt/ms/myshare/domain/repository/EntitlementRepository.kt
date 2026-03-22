package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    val isPro: Flow<Boolean>
    suspend fun setPro(value: Boolean)
    suspend fun restorePurchases()
}
