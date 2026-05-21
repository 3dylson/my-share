package pt.ms.myshare.data.billing

import kotlinx.coroutines.flow.Flow

interface BillingAuthSession {
    val userId: Flow<String?>
    fun currentUserId(): String?
    suspend fun requireAuthenticatedUserId(): Result<String>
    suspend fun requireAuthenticatedSession(): Result<BillingAuthenticatedSession>
}
