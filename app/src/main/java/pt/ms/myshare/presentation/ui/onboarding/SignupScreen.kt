package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
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
            
            Spacer(Modifier.height(48.dp))

            // Trust / Feature Section
            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                SecurityFeature(
                    title = "Your Private Vault",
                    description = "We don't store your bank credentials. Ever.",
                    icon = Icons.Default.Lock
                )
                SecurityFeature(
                    title = "Account Sync",
                    description = "Your data is securely tied to your Google account.",
                    icon = Icons.Default.Cloud
                )
                SecurityFeature(
                    title = "Privacy First",
                    description = "We prioritize your financial data privacy.",
                    icon = Icons.Default.Fingerprint
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Error messaging state (using local snackbar for premium feel)
            val snackbarHostState = remember { SnackbarHostState() }
            
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GoogleSignInButton(
                        text = stringResource(R.string.onboarding_signup_google),
                        onClick = {
                            coroutineScope.launch {
                                try {
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
                                        onSignup(googleIdTokenCredential.idToken)
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Google Sign-In failed")
                                    snackbarHostState.showSnackbar(
                                        message = "Sign-in failed. Please check your internet or Google account.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = onSignupAnonymously,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MyShareOnSurfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Text(
                            "Skip Cloud Sync (Run Locally)", 
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
                
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Composable
private fun SecurityFeature(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MySharePrimaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MySharePrimary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(20.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MyShareOnSurface)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MyShareSecondary)
        }
    }
}
