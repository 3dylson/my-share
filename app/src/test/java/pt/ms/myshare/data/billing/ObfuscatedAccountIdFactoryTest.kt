package pt.ms.myshare.data.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObfuscatedAccountIdFactoryTest {

    @Test
    fun `fromFirebaseUid returns null for missing uid`() {
        assertNull(ObfuscatedAccountIdFactory.fromFirebaseUid(null))
        assertNull(ObfuscatedAccountIdFactory.fromFirebaseUid(""))
    }

    @Test
    fun `fromFirebaseUid hashes uid into stable obfuscated account id`() {
        val accountId = ObfuscatedAccountIdFactory.fromFirebaseUid("firebase-user-123")

        assertEquals(64, accountId?.length)
        assertEquals(accountId, ObfuscatedAccountIdFactory.fromFirebaseUid("firebase-user-123"))
        assertNotEquals("firebase-user-123", accountId)
    }
}
