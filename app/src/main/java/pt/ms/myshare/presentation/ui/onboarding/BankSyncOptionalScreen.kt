package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import pt.ms.myshare.R
@Composable
fun BankSyncOptionalScreen(
    onSync: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            
            // Icon or Visual element placeholder (Shield icon for trust)
            androidx.compose.foundation.background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape
            ).let { 
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(80.dp)
                        .then(it),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.onboarding_banksync_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                stringResource(R.string.onboarding_banksync_subtitle), 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = onSync,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_banksync_link), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_banksync_skip), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
