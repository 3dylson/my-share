package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.dismissKeyboardOnUserDrag
import pt.ms.myshare.presentation.ui.theme.*
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSetupScreen(
    onPermissionResult: (Boolean) -> Unit = {},
    onConfirm: (LocalTime, ReminderCadence) -> Unit,
    onSkip: () -> Unit
) {
    var time by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var cadence by remember { mutableStateOf(ReminderCadence.PAYDAY) }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    KeyboardDismissEffect()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onPermissionResult(granted)
            if (granted) {
                confirmReminderSetup(time, cadence, haptic, onConfirm)
            } else {
                message = context.getString(R.string.onboarding_reminder_error_permission)
            }
        }
    )

    fun requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            confirmReminderSetup(time, cadence, haptic, onConfirm)
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onPermissionResult(true)
            confirmReminderSetup(time, cadence, haptic, onConfirm)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PremiumButton(
                        text = stringResource(R.string.onboarding_reminder_button),
                        onClick = { requestPermissionIfNeeded() }
                    )

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text(
                            stringResource(R.string.onboarding_reminder_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(horizontal = 24.dp)
                .dismissKeyboardOnUserDrag(debugLabel = "ReminderSetupScreen")
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            ReminderHeroIcon()

            Spacer(Modifier.height(14.dp))
            
            Text(
                stringResource(R.string.onboarding_reminder_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                stringResource(R.string.onboarding_reminder_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_reminder_cadence_payday),
                    description = stringResource(R.string.onboarding_reminder_cadence_payday_desc),
                    isSelected = cadence == ReminderCadence.PAYDAY,
                    onClick = {
                        if (cadence != ReminderCadence.PAYDAY) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        cadence = ReminderCadence.PAYDAY
                    }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_reminder_cadence_weekly),
                    description = stringResource(R.string.onboarding_reminder_cadence_weekly_desc),
                    isSelected = cadence == ReminderCadence.WEEKLY_REVIEW,
                    onClick = {
                        if (cadence != ReminderCadence.WEEKLY_REVIEW) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        cadence = ReminderCadence.WEEKLY_REVIEW
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            ReminderTimeCard(
                time = time,
                onDecrease = { time = time.minusHours(1) },
                onIncrease = { time = time.plusHours(1) }
            )

            Spacer(Modifier.height(12.dp))

            ReminderRoutineCard(
                cadence = cadence,
                time = time
            )

            message?.let { 
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it, 
                    color = MaterialTheme.colorScheme.error, 
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                ) 
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun confirmReminderSetup(
    time: LocalTime,
    cadence: ReminderCadence,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onConfirm: (LocalTime, ReminderCadence) -> Unit
) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onConfirm(time, cadence)
}

@Composable
private fun ReminderHeroIcon() {
    Surface(
        modifier = Modifier.size(84.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.42f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ReminderTimeCard(
    time: LocalTime,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.onboarding_reminder_time_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReminderTimeButton(
                    icon = Icons.Default.Remove,
                    onClick = onDecrease
                )

                Text(
                    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReminderTimeButton(
                    icon = Icons.Default.Add,
                    onClick = onIncrease
                )
            }
        }
    }
}

@Composable
private fun ReminderTimeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MySharePrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ReminderRoutineCard(
    cadence: ReminderCadence,
    time: LocalTime
) {
    val cadenceLabel = when (cadence) {
        ReminderCadence.PAYDAY -> stringResource(R.string.onboarding_reminder_cadence_payday)
        ReminderCadence.WEEKLY_REVIEW -> stringResource(R.string.onboarding_reminder_cadence_weekly)
    }
    val timeLabel = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_reminder_routine_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.onboarding_reminder_selected_format, cadenceLabel, timeLabel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.onboarding_reminder_routine_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
