package pt.ms.myshare.presentation.ui.onboarding

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import pt.ms.myshare.R
import timber.log.Timber

@Composable
fun SignupScreen(
    onSignup: (String) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.onboarding_signup_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.onboarding_signup_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("564550726509-g94sj76hhfhjdpufiqp1feqei5f1gbuh.apps.googleusercontent.com")
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
                                onSignup(googleIdTokenCredential.idToken)
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Google Sign-In failed")
                            // Fallback or error handling here
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_signup_google), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_signup_skip), fontSize = 16.sp)
            }
        }
    }
}
