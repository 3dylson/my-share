package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.GoogleSignInButton
import pt.ms.myshare.presentation.ui.theme.*
import timber.log.Timber

@Composable
fun SignupScreen(
    onSignup: (String) -> Unit,
    onSignupAnonymously: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var googleSignInLoading by remember { mutableStateOf(false) }

    fun startGoogleSignIn() {
        coroutineScope.launch {
            googleSignInLoading = true
            googleSignInError = null
            try {
                Timber.d("Starting Google sign-in from onboarding")
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(pt.ms.myshare.BuildConfig.GOOGLE_CLIENT_ID)
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
                    Timber.d("Google sign-in credential received")
                    onSignup(googleIdTokenCredential.idToken)
                } else {
                    Timber.e("Google sign-in returned unsupported credential type: ${credential.type}")
                    googleSignInError = context.getString(R.string.onboarding_signup_google_error_generic)
                }
            } catch (e: NoCredentialException) {
                Timber.e(e, "Google sign-in failed because no Google credential is available")
                googleSignInError = context.getString(R.string.onboarding_signup_google_error_no_credentials)
            } catch (e: Exception) {
                Timber.e(e, "Google Sign-In failed")
                googleSignInError = context.getString(R.string.onboarding_signup_google_error_generic)
            } finally {
                googleSignInLoading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    googleSignInError?.let { message ->
                        GoogleSignInErrorMessage(
                            message = message
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    GoogleSignInButton(
                        text = stringResource(R.string.onboarding_signup_google),
                        isLoading = googleSignInLoading,
                        onClick = ::startGoogleSignIn
                    )

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = onSignupAnonymously,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MyShareOnSurfaceVariant)
                    ) {
                        Text(
                            stringResource(R.string.onboarding_signup_continue_without_sync),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(40.dp))
            
            Text(
                stringResource(R.string.onboarding_signup_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_signup_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MyShareSurface,
                border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecurityFeature(
                        title = stringResource(R.string.onboarding_signup_feature_vault_title),
                        description = stringResource(R.string.onboarding_signup_feature_vault_body),
                        icon = Icons.Default.Lock
                    )
                    SecurityFeature(
                        title = stringResource(R.string.onboarding_signup_feature_sync_title),
                        description = stringResource(R.string.onboarding_signup_feature_sync_body),
                        icon = Icons.Default.Cloud
                    )
                    SecurityFeature(
                        title = stringResource(R.string.onboarding_signup_feature_privacy_title),
                        description = stringResource(R.string.onboarding_signup_feature_privacy_body),
                        icon = Icons.Default.Fingerprint
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.onboarding_signup_local_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MyShareSecondary,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoogleSignInErrorMessage(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SecurityFeature(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MySharePrimaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MySharePrimary, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MyShareOnSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MyShareSecondary)
        }
    }
}
