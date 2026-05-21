package pt.ms.myshare.data.auth

interface CredentialStateClearer {
    suspend fun clearCredentialState()
}
