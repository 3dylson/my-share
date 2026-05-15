package pt.ms.myshare.presentation.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.presentation.ui.components.MyShareAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeReminderSettingsDialog(
    initialHourOfDay: Int,
    initialMinute: Int,
    initialCadence: ReminderCadence,
    onDismissRequest: () -> Unit,
    onSave: (hourOfDay: Int, minute: Int, cadence: ReminderCadence) -> Unit
) {
    val context = LocalContext.current
    val timeState = rememberTimePickerState(
        initialHour = initialHourOfDay.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = DateFormat.is24HourFormat(context)
    )
    var cadence by remember(initialCadence) { mutableStateOf(initialCadence) }

    MyShareAlertDialog(
        onDismissRequest = onDismissRequest,
        icon = Icons.Default.Notifications,
        title = stringResource(R.string.home_more_reminder_dialog_title),
        confirmText = stringResource(R.string.dialog_save),
        onConfirm = {
            onSave(timeState.hour, timeState.minute, cadence)
        },
        dismissText = stringResource(R.string.dialog_cancel),
        onDismiss = onDismissRequest
    ) {
        Text(
            text = stringResource(R.string.home_more_reminder_dialog_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.onboarding_reminder_time_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            TimeInput(state = timeState)
        }

        Text(
            text = stringResource(R.string.home_more_reminder_cadence_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = cadence == ReminderCadence.PAYDAY,
                onClick = { cadence = ReminderCadence.PAYDAY },
                label = { Text(text = stringResource(R.string.onboarding_reminder_cadence_payday)) },
                modifier = Modifier.fillMaxWidth()
            )
            FilterChip(
                selected = cadence == ReminderCadence.WEEKLY_REVIEW,
                onClick = { cadence = ReminderCadence.WEEKLY_REVIEW },
                label = { Text(text = stringResource(R.string.onboarding_reminder_cadence_weekly)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
