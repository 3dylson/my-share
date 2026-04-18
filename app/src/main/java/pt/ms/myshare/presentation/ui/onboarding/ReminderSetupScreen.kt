package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.theme.*
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            
            Text(
                "Stay on track",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MyShareOnSurface
            )
            
            Text(
                "Consistent check-ins are the key to building wealth. We'll send a gentle nudge to help you follow your plan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(Modifier.height(48.dp))

            // Cadence Selection
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumChoiceCard(
                    title = "Every Payday",
                    description = "Stay aligned with your plan at every income event.",
                    isSelected = cadence == ReminderCadence.PAYDAY,
                    onClick = { cadence = ReminderCadence.PAYDAY }
                )
                PremiumChoiceCard(
                    title = "Weekly Review",
                    description = "Reflect and adjust your progress every Sunday.",
                    isSelected = cadence == ReminderCadence.WEEKLY_REVIEW,
                    onClick = { cadence = ReminderCadence.WEEKLY_REVIEW }
                )
            }

            Spacer(Modifier.height(48.dp))

            // Time Selector
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Reminder Time",
                    style = MaterialTheme.typography.labelLarge,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(
                        onClick = { time = time.minusHours(1) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MySharePrimaryContainer, CircleShape)
                    ) {
                        Text("-", fontSize = 28.sp, color = MySharePrimary, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MyShareOnSurface
                    )

                    IconButton(
                        onClick = { time = time.plusHours(1) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(MySharePrimaryContainer, CircleShape)
                    ) {
                        Text("+", fontSize = 28.sp, color = MySharePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            message?.let { 
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it, 
                    color = MaterialTheme.colorScheme.error, 
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                ) 
            }

            Spacer(Modifier.weight(1f))

            PremiumButton(
                text = "Enable Reminders",
                onClick = { requestPermissionIfNeeded() }
            )
            
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    "I’ll do this later", 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MyShareSecondary
                )
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
