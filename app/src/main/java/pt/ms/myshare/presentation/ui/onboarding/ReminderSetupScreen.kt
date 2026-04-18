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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            
            Text(
                "Stay on track",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Consistent check-ins are the key to building wealth. We'll send a gentle nudge to help you follow your plan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))

            // Cadence Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.FilterChip(
                    selected = cadence == ReminderCadence.PAYDAY,
                    onClick = { cadence = ReminderCadence.PAYDAY },
                    label = { Text("Every Payday") },
                    modifier = Modifier.padding(4.dp)
                )
                androidx.compose.material3.FilterChip(
                    selected = cadence == ReminderCadence.WEEKLY_REVIEW,
                    onClick = { cadence = ReminderCadence.WEEKLY_REVIEW },
                    label = { Text("Weekly Review") },
                    modifier = Modifier.padding(4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Time Selector
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(
                    "Reminder Time",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = { time = time.minusHours(1) },
                        modifier = androidx.compose.foundation.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                    ) {
                        Text("-", fontSize = 24.sp)
                    }

                    Text(
                        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    androidx.compose.material3.IconButton(
                        onClick = { time = time.plusHours(1) },
                        modifier = androidx.compose.foundation.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                    ) {
                        Text("+", fontSize = 24.sp)
                    }
                }
            }

            message?.let { 
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center) 
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { requestPermissionIfNeeded() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Enable reminders", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("I’ll do this later", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
