package pt.ms.myshare.data.billing

import com.google.firebase.Timestamp
import pt.ms.myshare.domain.model.EntitlementState
import java.util.Date
import java.util.Locale

internal object EntitlementSnapshotMapper {

    fun map(fields: Map<String, Any?>?, nowMillis: Long = System.currentTimeMillis()): EntitlementState? {
        if (fields == null) return null

        stateFromString(fields["entitlementState"] as? String)?.let { return it }
        stateFromString(fields["subscriptionState"] as? String)?.let { return it }

        val isPro = fields["isPro"] as? Boolean ?: return null
        if (!isPro) return EntitlementState.FREE

        val expiryMillis = expiryMillis(fields["proExpiry"])
            ?: expiryMillis(fields["expiryTimeMillis"])
            ?: return EntitlementState.PRO

        return if (expiryMillis > nowMillis) {
            EntitlementState.PRO
        } else {
            EntitlementState.FREE
        }
    }

    private fun stateFromString(value: String?): EntitlementState? {
        return when (value?.uppercase(Locale.US)) {
            "PRO",
            "ACTIVE",
            "SUBSCRIPTION_STATE_ACTIVE" -> EntitlementState.PRO

            "GRACE_PERIOD",
            "SUBSCRIPTION_STATE_IN_GRACE_PERIOD" -> EntitlementState.GRACE_PERIOD

            "FREE",
            "EXPIRED",
            "REVOKED",
            "ACCOUNT_HOLD",
            "SUBSCRIPTION_STATE_ON_HOLD",
            "SUBSCRIPTION_STATE_CANCELED",
            "SUBSCRIPTION_STATE_EXPIRED" -> EntitlementState.FREE

            "UNKNOWN" -> EntitlementState.UNKNOWN
            else -> null
        }
    }

    private fun expiryMillis(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Date -> value.time
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}
