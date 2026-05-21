package pt.ms.myshare.presentation.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import pt.ms.myshare.presentation.ui.MainComposeActivity

object MyShareNotificationIntentFactory {
    const val EXTRA_HOME_DESTINATION = "pt.ms.myshare.extra.HOME_DESTINATION"
    const val EXTRA_NOTIFICATION_TYPE = "pt.ms.myshare.extra.NOTIFICATION_TYPE"

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
}
