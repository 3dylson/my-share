package pt.ms.myshare.presentation.ui.onboarding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import pt.ms.myshare.domain.model.ReminderResponse
import pt.ms.myshare.domain.repository.ReminderResponseRepository
import pt.ms.myshare.presentation.notifications.MyShareNotificationIntentFactory
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ReminderResponseActionReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderResponseRepository: ReminderResponseRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra(MyShareNotificationIntentFactory.EXTRA_NOTIFICATION_TYPE)
            .orEmpty()
        val responseAction = intent.getStringExtra(MyShareNotificationIntentFactory.EXTRA_REMINDER_RESPONSE_ACTION)
            .orEmpty()

        reminderResponseRepository.saveResponse(
            ReminderResponse(
                notificationType = notificationType,
                response = responseAction
            )
        )

        FirebaseUtils.logEvent("notification_response_action", Bundle().apply {
            putString("type", notificationType)
            putString("response", responseAction)
        })
        NotificationManagerCompat.from(context).cancel(ReminderWorker.NOTIFICATION_ID)
        Timber.tag(TAG).d(
            "Reminder quick response received type=%s response=%s",
            notificationType,
            responseAction
        )
    }

    private companion object {
        const val TAG = "ReminderResponseAction"
    }
}
