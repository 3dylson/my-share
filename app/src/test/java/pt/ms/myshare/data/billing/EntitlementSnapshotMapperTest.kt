package pt.ms.myshare.data.billing

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.ms.myshare.domain.model.EntitlementState

class EntitlementSnapshotMapperTest {

    @Test
    fun `map returns null when snapshot does not contain entitlement fields`() {
        val result = EntitlementSnapshotMapper.map(emptyMap(), nowMillis = NOW)

        assertNull(result)
    }

    @Test
    fun `map prefers explicit subscription state`() {
        val result = EntitlementSnapshotMapper.map(
            mapOf(
                "subscriptionState" to "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
                "isPro" to false
            ),
            nowMillis = NOW
        )

        assertEquals(EntitlementState.GRACE_PERIOD, result)
    }

    @Test
    fun `map treats active legacy pro snapshot with future expiry as pro`() {
        val result = EntitlementSnapshotMapper.map(
            mapOf(
                "isPro" to true,
                "proExpiry" to Timestamp(NOW_SECONDS + 60, 0)
            ),
            nowMillis = NOW
        )

        assertEquals(EntitlementState.PRO, result)
    }

    @Test
    fun `map treats expired legacy pro snapshot as free`() {
        val result = EntitlementSnapshotMapper.map(
            mapOf(
                "isPro" to true,
                "expiryTimeMillis" to (NOW - 1).toString()
            ),
            nowMillis = NOW
        )

        assertEquals(EntitlementState.FREE, result)
    }

    @Test
    fun `map treats account hold as free access`() {
        val result = EntitlementSnapshotMapper.map(
            mapOf("entitlementState" to "SUBSCRIPTION_STATE_ON_HOLD"),
            nowMillis = NOW
        )

        assertEquals(EntitlementState.FREE, result)
    }

    private companion object {
        const val NOW = 1_800_000_000_000L
        const val NOW_SECONDS = NOW / 1_000
    }
}
