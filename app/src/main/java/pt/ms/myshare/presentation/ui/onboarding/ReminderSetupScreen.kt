package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import pt.ms.myshare.domain.model.ReminderCadence
import java.time.LocalTime

@Composable
fun ReminderSetupScreen(
    onConfirm: (LocalTime, ReminderCadence) -> Unit,
    onSkip: () -> Unit
) {
    var time by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var cadence by remember { mutableStateOf(ReminderCadence.PAYDAY) }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                onConfirm(time, cadence)
            } else {
                message = "Notifications stayed off. You can still use the plan and turn reminders on later."
            }
        }
    )

    fun requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onConfirm(time, cadence)
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onConfirm(time, cadence)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Set up the repeat loop", style = MaterialTheme.typography.headlineMedium)
            Text("Ask for notifications only after the plan exists and only in the context of reminders.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { cadence = ReminderCadence.PAYDAY }) { Text("Payday") }
                Button(onClick = { cadence = ReminderCadence.WEEKLY_REVIEW }) { Text("Weekly review") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { time = time.minusHours(1) }) { Text("-1h") }
                Text("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
                Button(onClick = { time = time.plusHours(1) }) { Text("+1h") }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.weight(1f))
            Button(onClick = { requestPermissionIfNeeded() }, modifier = Modifier.fillMaxWidth()) {
                Text("Enable reminders")
            }
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("I’ll do this later")
            }
        }
    }
}
