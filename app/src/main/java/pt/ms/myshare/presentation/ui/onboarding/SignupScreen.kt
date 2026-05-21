package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReadResult
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReader
import pt.ms.myshare.presentation.ui.components.GoogleSignInButton
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.rememberKeyboardDismissOnScrollConnection
import pt.ms.myshare.presentation.ui.theme.*
import timber.log.Timber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignupScreen(
    isSignupActionInProgress: Boolean,
    onSignup: (String) -> Unit,
    onContinueLocally: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleIdTokenReader = remember(context) {
        GoogleIdTokenReader(
            credentialManager = CredentialManager.create(context),
            serverClientId = context.getString(R.string.default_web_client_id)
        )
    }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var googleSignInLoading by remember { mutableStateOf(false) }
    var localContinueLoading by remember { mutableStateOf(false) }
    val keyboardDismissOnScrollConnection = rememberKeyboardDismissOnScrollConnection()

    KeyboardDismissEffect()

    fun startGoogleSignIn() {
        if (isSignupActionInProgress || googleSignInLoading) return
        localContinueLoading = false
        coroutineScope.launch {
            googleSignInLoading = true
            googleSignInError = null
            when (val result = googleIdTokenReader.readIdToken(context, "onboarding")) {
                is GoogleIdTokenReadResult.Success -> onSignup(result.idToken)
                GoogleIdTokenReadResult.NoCredential -> {
                    Timber.e("Google sign-in failed because no Google credential is available")
                    googleSignInError = context.getString(R.string.onboarding_signup_google_error_no_credentials)
                }
                GoogleIdTokenReadResult.UnsupportedCredential -> {
                    googleSignInError = context.getString(R.string.onboarding_signup_google_error_generic)
                }
                is GoogleIdTokenReadResult.Failure -> {
                    googleSignInError = context.getString(R.string.onboarding_signup_google_error_generic)
                }
            }
            googleSignInLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 28.dp),
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
                        isLoading = googleSignInLoading || (isSignupActionInProgress && !localContinueLoading),
                        onClick = ::startGoogleSignIn
                    )

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            if (!isSignupActionInProgress && !googleSignInLoading) {
                                localContinueLoading = true
                                onContinueLocally()
                            }
                        },
                        enabled = !isSignupActionInProgress && !googleSignInLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        if (localContinueLoading || (isSignupActionInProgress && !googleSignInLoading)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            stringResource(R.string.onboarding_signup_continue_without_sync),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                .padding(horizontal = 24.dp)
                .nestedScroll(keyboardDismissOnScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(28.dp))
            
            Text(
                stringResource(R.string.onboarding_signup_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                stringResource(R.string.onboarding_signup_subtitle), 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MySharePrimary, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
