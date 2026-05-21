package pt.ms.myshare.data.grant

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import pt.ms.myshare.data.billing.BillingAuthSession
import pt.ms.myshare.domain.model.LegacyPremiumGrantState
import pt.ms.myshare.domain.model.LegacyPremiumGrantStatus
import pt.ms.myshare.domain.repository.LegacyPremiumGrantRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FirebaseLegacyPremiumGrantRepository @Inject constructor(
    private val eligibilityStore: LegacyPremiumGrantEligibilityStore,
    private val billingAuthSession: BillingAuthSession,
    private val firestoreProvider: Provider<FirebaseFirestore>,
    private val firebaseFunctionsProvider: Provider<FirebaseFunctions>
) : LegacyPremiumGrantRepository {

    private val state = MutableStateFlow(LegacyPremiumGrantState())

    override val grantState = state.asStateFlow()

    override suspend fun refreshAvailability() {
        state.value = initialState()
    }

    override suspend fun reserveFounderOffer(): LegacyPremiumGrantState {
        if (state.value.status == LegacyPremiumGrantStatus.Claiming) return state.value
        if (!eligibilityStore.evaluateEligibility()) {
            state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.NotEligible)
            return state.value
        }

        state.value = state.value.copy(status = LegacyPremiumGrantStatus.Claiming, errorMessageKey = null)
        val session = billingAuthSession.requireAuthenticatedSession()
            .onFailure { Timber.tag(TAG).e(it, "Legacy Premium grant authentication failed") }
            .getOrNull()
        if (session == null) {
            state.value = LegacyPremiumGrantState(
                status = LegacyPremiumGrantStatus.Error,
                errorMessageKey = "legacy_premium_grant_error"
            )
            return state.value
        }

        val data = hashMapOf(
            "firebaseIdToken" to session.idToken,
            "campaignId" to CAMPAIGN_ID,
            "clientEligibility" to true
        )

        try {
            val result = firebaseFunctionsProvider.get()
                .getHttpsCallable("reserveLegacyPremiumFounderOffer")
                .call(data)
                .await()
            val resultMap = result.data as? Map<*, *>
            val reserved = resultMap?.get("reserved") as? Boolean ?: false
            val reason = resultMap?.get("reason") as? String
            if (reserved) {
                state.value = LegacyPremiumGrantState(
                    status = LegacyPremiumGrantStatus.Reserved
                )
                Timber.tag(TAG).d("Legacy Premium founder offer reserved")
            } else {
                if (reason == "cap_reached") {
                    eligibilityStore.markDismissed()
                }
                state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.NotEligible)
                Timber.tag(TAG).d("Legacy Premium founder offer rejected by server reason=%s", reason)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Legacy Premium founder offer reservation failed")
            state.value = LegacyPremiumGrantState(
                status = LegacyPremiumGrantStatus.Error,
                errorMessageKey = "legacy_premium_grant_error"
            )
        }
        return state.value
    }

    override suspend fun releaseFounderOffer() {
        val session = billingAuthSession.requireAuthenticatedSession()
            .onFailure { Timber.tag(TAG).e(it, "Legacy Premium founder release authentication failed") }
            .getOrNull()
        if (session == null) return

        val data = hashMapOf(
            "firebaseIdToken" to session.idToken,
            "campaignId" to CAMPAIGN_ID
        )
        try {
            firebaseFunctionsProvider.get()
                .getHttpsCallable("releaseLegacyPremiumFounderOffer")
                .call(data)
                .await()
            if (eligibilityStore.evaluateEligibility()) {
                state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.Eligible)
            }
            Timber.tag(TAG).d("Legacy Premium founder offer reservation released")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Legacy Premium founder offer release failed")
        }
    }

    override suspend fun markFounderOfferStarted() {
        state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.Reserved)
        Timber.tag(TAG).d("Legacy Premium founder Play flow started")
    }

    override suspend fun markFounderOfferClaimed() {
        eligibilityStore.markClaimed()
        state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.Claimed)
        Timber.tag(TAG).d("Legacy Premium founder offer marked claimed locally")
    }

    override suspend fun dismissGrant() {
        eligibilityStore.markDismissed()
        state.value = LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.Dismissed)
        Timber.tag(TAG).d("Legacy Premium grant dismissed")
    }

    private suspend fun initialState(): LegacyPremiumGrantState {
        val isLocallyEligible = eligibilityStore.evaluateEligibility()
        val hasCapacity = hasGrantCapacity()
        return if (isLocallyEligible && hasCapacity) {
            LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.Eligible)
        } else {
            LegacyPremiumGrantState(status = LegacyPremiumGrantStatus.NotEligible)
        }
    }

    private suspend fun hasGrantCapacity(): Boolean {
        return try {
            val snapshot = firestoreProvider.get()
                .collection("app_config")
                .document("legacy_premium_grant")
                .get()
                .await()
            if (!snapshot.exists()) return true
            val active = snapshot.getBoolean("active") ?: true
            val claimedCount = snapshot.getLong("claimedCount") ?: 0L
            val reservedCount = snapshot.getLong("reservedCount") ?: 0L
            val maxClaims = snapshot.getLong("maxClaims") ?: MAX_CLAIMS
            active && claimedCount + reservedCount < maxClaims
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Legacy Premium grant config unavailable")
            false
        }
    }

    private companion object {
        const val TAG = "LegacyPremiumGrant"
        const val CAMPAIGN_ID = "legacy_app_user_2026_05"
        const val MAX_CLAIMS = 100L
    }
}
