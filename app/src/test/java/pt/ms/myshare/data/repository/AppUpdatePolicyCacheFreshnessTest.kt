package pt.ms.myshare.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatePolicyCacheFreshnessTest {

    @Test
    fun `cache is fresh when age is within max age`() {
        val nowMillis = 10_000L
        val cachedAtMillis = nowMillis - 1_000L

        val isFresh = AppUpdatePolicyCacheFreshness.isFresh(
            cachedAtMillis = cachedAtMillis,
            nowMillis = nowMillis,
            maxAgeMillis = 2_000L
        )

        assertTrue(isFresh)
    }

    @Test
    fun `cache is stale when age exceeds max age`() {
        val nowMillis = 10_000L
        val cachedAtMillis = nowMillis - 3_000L

        val isFresh = AppUpdatePolicyCacheFreshness.isFresh(
            cachedAtMillis = cachedAtMillis,
            nowMillis = nowMillis,
            maxAgeMillis = 2_000L
        )

        assertFalse(isFresh)
    }

    @Test
    fun `cache is stale when timestamp is missing`() {
        val isFresh = AppUpdatePolicyCacheFreshness.isFresh(
            cachedAtMillis = 0L,
            nowMillis = 10_000L
        )

        assertFalse(isFresh)
    }

    @Test
    fun `cache is stale when timestamp is in the future`() {
        val isFresh = AppUpdatePolicyCacheFreshness.isFresh(
            cachedAtMillis = 11_000L,
            nowMillis = 10_000L
        )

        assertFalse(isFresh)
    }
}
