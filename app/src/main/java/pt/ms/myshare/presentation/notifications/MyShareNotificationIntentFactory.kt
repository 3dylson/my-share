package pt.ms.myshare.presentation.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import pt.ms.myshare.domain.use_case.ReminderResponseAction
import pt.ms.myshare.presentation.ui.onboarding.ReminderResponseActionReceiver
import pt.ms.myshare.presentation.ui.MainComposeActivity

object MyShareNotificationIntentFactory {
    const val EXTRA_HOME_DESTINATION = "pt.ms.myshare.extra.HOME_DESTINATION"
    const val EXTRA_NOTIFICATION_TYPE = "pt.ms.myshare.extra.NOTIFICATION_TYPE"
    const val EXTRA_REMINDER_RESPONSE_ACTION = "pt.ms.myshare.extra.REMINDER_RESPONSE_ACTION"

    fun activityIntent(
        context: Context,
        destination: String,
        notificationType: String
    ): PendingIntent {
        val intent = Intent(context, MainComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HOME_DESTINATION, destination)
            putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
        }
        return PendingIntent.getActivity(
            context,
            notificationType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun reminderResponseIntent(
        context: Context,
        notificationType: String,
        responseAction: ReminderResponseAction
    ): PendingIntent {
        val intent = Intent(context, ReminderResponseActionReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
            putExtra(EXTRA_REMINDER_RESPONSE_ACTION, responseAction.analyticsValue)
        }
        return PendingIntent.getBroadcast(
            context,
            "${notificationType}_${responseAction.analyticsValue}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
