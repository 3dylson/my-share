package pt.ms.myshare.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidCredentialStateClearer @Inject constructor(
    @ApplicationContext context: Context
) : CredentialStateClearer {

    private val credentialManager = CredentialManager.create(context)

    override suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
