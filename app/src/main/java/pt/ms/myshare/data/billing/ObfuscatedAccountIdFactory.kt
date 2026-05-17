package pt.ms.myshare.data.billing

import java.security.MessageDigest

object ObfuscatedAccountIdFactory {
    fun fromFirebaseUid(uid: String?): String? {
        val normalizedUid = uid?.takeIf { it.isNotBlank() } ?: return null
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalizedUid.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
