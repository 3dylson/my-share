package pt.ms.myshare.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreAppUpdatePolicyMapperTest {

    @Test
    fun `maps complete Firestore policy`() {
        val result = FirestoreAppUpdatePolicyMapper.map(
            mapOf(
                "minimumSupportedVersionCode" to 8L,
                "immediateUpdateRequired" to true,
                "playStorePackageName" to "pt.ms.myshare"
            )
        )

        assertTrue(result.isSuccess)
        val policy = result.getOrThrow()
        assertEquals(8, policy.minimumSupportedVersionCode)
        assertEquals(true, policy.immediateUpdateRequired)
        assertEquals("pt.ms.myshare", policy.playStorePackageName)
    }

    @Test
    fun `missing Firestore fields default safely`() {
        val result = FirestoreAppUpdatePolicyMapper.map(emptyMap())

        assertTrue(result.isSuccess)
        val policy = result.getOrThrow()
        assertEquals(0, policy.minimumSupportedVersionCode)
        assertEquals(false, policy.immediateUpdateRequired)
        assertEquals("pt.ms.myshare", policy.playStorePackageName)
    }

    @Test
    fun `malformed numeric version fails without crashing`() {
        val result = FirestoreAppUpdatePolicyMapper.map(
            mapOf("minimumSupportedVersionCode" to "8")
        )

        assertTrue(result.isFailure)
    }
}
