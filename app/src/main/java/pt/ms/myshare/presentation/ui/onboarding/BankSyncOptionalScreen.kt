package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumInfoCard
import pt.ms.myshare.presentation.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BankSyncOptionalScreen(
    onSync: () -> Unit,
    onSkip: () -> Unit
) {
    var isInterestRegistered by remember { mutableStateOf(false) }

    LaunchedEffect(isInterestRegistered) {
        if (isInterestRegistered) {
            delay(3000)
            onSync()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))
            
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MySharePrimary
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                stringResource(R.string.onboarding_banksync_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                stringResource(R.string.onboarding_banksync_subtitle), 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Spacer(Modifier.weight(1f))
            
            AnimatedContent(targetState = isInterestRegistered, label = "interestState") { registered ->
                if (registered) {
                    PremiumInfoCard(
                        title = stringResource(R.string.onboarding_banksync_interest_title),
                        body = stringResource(R.string.onboarding_banksync_interest_body),
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PremiumButton(
                            text = stringResource(R.string.onboarding_banksync_skip),
                            onClick = onSkip
                        )
                        
                        TextButton(
                            onClick = { isInterestRegistered = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(
                                stringResource(R.string.onboarding_banksync_link),
                                style = MaterialTheme.typography.labelLarge, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
