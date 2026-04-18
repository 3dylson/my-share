package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.onboarding_banksync_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.onboarding_banksync_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onSync,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_banksync_link), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.onboarding_banksync_skip), fontSize = 16.sp)
            }
        }
    }
}
