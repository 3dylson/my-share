package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@Composable
fun ReminderSetupScreen(
    initialTime: LocalTime = LocalTime.of(9, 0),
    initialSchedule: String = "MONTHLY",
    onConfirm: (LocalTime, String) -> Unit
) {
    var time by remember { mutableStateOf(initialTime) }
    var schedule by remember { mutableStateOf(initialSchedule) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                error = null
                onConfirm(time, schedule)
            } else {
                error = "Notifications are disabled. You can enable them in system settings."
            }
        }
    )

    fun ensureNotificationPermissionThenConfirm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onConfirm(time, schedule)
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onConfirm(time, schedule)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(Modifier.padding(24.dp)) {
        Text("Set up payday reminders", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Choose reminder time:")
        Row {
            Button(onClick = { time = time.withHour((time.hour + 1) % 24) }) { Text("+1h") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { time = time.withHour((time.hour + 23) % 24) }) { Text("-1h") }
            Spacer(Modifier.width(8.dp))
            Text("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
        }
        Spacer(Modifier.height(16.dp))
        Text("Pay schedule:")
        Row {
            Button(onClick = { schedule = "MONTHLY" }) { Text("Monthly") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { schedule = "BIWEEKLY" }) { Text("Biweekly") }
        }
        Spacer(Modifier.height(32.dp))
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }
        Button(onClick = { ensureNotificationPermissionThenConfirm() }, modifier = Modifier.fillMaxWidth()) {
            Text("Confirm Reminder")
        }
    }
}
