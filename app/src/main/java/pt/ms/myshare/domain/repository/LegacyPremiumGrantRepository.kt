package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.LegacyPremiumGrantState

interface LegacyPremiumGrantRepository {
    val grantState: Flow<LegacyPremiumGrantState>

    suspend fun refreshAvailability()
    suspend fun reserveFounderOffer(): LegacyPremiumGrantState
    suspend fun releaseFounderOffer()
    suspend fun markFounderOfferStarted()
    suspend fun markFounderOfferClaimed()
    suspend fun dismissGrant()
}
