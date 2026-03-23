package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrajectoryScreen(
    onNext: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Look ahead", style = MaterialTheme.typography.headlineMedium)
            Text("If you stick to this plan, you will reach your goals faster. You can set up automation rules to make it certain.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}
