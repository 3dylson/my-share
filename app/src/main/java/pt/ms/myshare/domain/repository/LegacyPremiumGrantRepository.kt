package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.LegacyPremiumGrantState

interface LegacyPremiumGrantRepository {
    val grantState: Flow<LegacyPremiumGrantState>

    suspend fun refreshAvailability()
    suspend fun claimGrant(): LegacyPremiumGrantState
    suspend fun dismissGrant()
}
