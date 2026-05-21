package pt.ms.myshare.presentation.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber

class GoogleIdTokenReader(
    private val credentialManager: CredentialManager,
    private val serverClientId: String
) {
    suspend fun readIdToken(context: Context, source: String): GoogleIdTokenReadResult {
        return try {
            Timber.d("Starting Google credential request from %s", source)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Timber.d("Google credential token received from %s", source)
                GoogleIdTokenReadResult.Success(googleIdTokenCredential.idToken)
            } else {
                Timber.e("Google credential request from %s returned unsupported type: %s", source, credential.type)
                GoogleIdTokenReadResult.UnsupportedCredential
            }
        } catch (e: NoCredentialException) {
            Timber.e(e, "Google credential request from %s failed because no credential is available", source)
            GoogleIdTokenReadResult.NoCredential
        } catch (e: Exception) {
            Timber.e(e, "Google credential request from %s failed", source)
            GoogleIdTokenReadResult.Failure(e)
        }
    }
}

sealed class GoogleIdTokenReadResult {
    data class Success(val idToken: String) : GoogleIdTokenReadResult()
    object NoCredential : GoogleIdTokenReadResult()
    object UnsupportedCredential : GoogleIdTokenReadResult()
    data class Failure(val throwable: Throwable) : GoogleIdTokenReadResult()
}
