package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun BankSyncOptionalScreen(
    onSync: () -> Unit,
    onSkip: () -> Unit
) {
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
                        color = MySharePrimaryContainer.copy(alpha = 0.5f),
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
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_banksync_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Spacer(Modifier.weight(1f))
            
            PremiumButton(
                text = stringResource(R.string.onboarding_banksync_link),
                onClick = onSync
            )
            
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    stringResource(R.string.onboarding_banksync_skip), 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MyShareSecondary
                )
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
