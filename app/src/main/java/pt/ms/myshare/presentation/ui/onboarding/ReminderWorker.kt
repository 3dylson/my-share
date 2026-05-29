package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pt.ms.myshare.R
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.BuildReminderNotificationContentUseCase
import pt.ms.myshare.domain.use_case.NotificationContent
import pt.ms.myshare.domain.use_case.ReminderNotificationType
import pt.ms.myshare.domain.use_case.ResolveReminderNotificationTypeUseCase
import pt.ms.myshare.presentation.MyShareApp
import pt.ms.myshare.presentation.notifications.MyShareNotificationIntentFactory
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.time.LocalDate

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val plannerRepository: PlannerRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val resolveReminderNotificationTypeUseCase: ResolveReminderNotificationTypeUseCase,
    private val buildReminderNotificationContentUseCase: BuildReminderNotificationContentUseCase
) : Worker(context, params) {

    override fun doWork(): Result {
        val plan = plannerRepository.loadPlan() ?: return Result.success()
        val reminderConfiguration = plannerRepository.loadReminderConfiguration()
        if (!reminderConfiguration.enabled) return Result.success()

        val today = LocalDate.now()
        val notificationType = resolveReminderNotificationTypeUseCase.execute(
            plan = plan,
            latestReview = plannerRepository.loadLatestReview(),
            reminderConfiguration = reminderConfiguration,
            automationEnabled = plannerRepository.loadAutomationEnabled(),
            today = today
        )
            ?: return Result.success()
        if (!shouldDeliverToday(notificationType, today)) return Result.success()

        val preferences = userPreferencesRepository.loadPreferences()
        val content = buildReminderNotificationContentUseCase.execute(
            plan = plan,
            type = notificationType,
            locale = preferences.locale,
            currencyCode = preferences.currencyCode
        )
        if (showNotification(content)) {
            markDelivered(notificationType, today)
            FirebaseUtils.logEvent("notification_delivered", Bundle().apply {
                putString("type", content.analyticsType)
                putString("destination", content.destination)
            })
            Timber.tag(TAG).d("Reminder delivered type=%s destination=%s", content.analyticsType, content.destination)
        }
        return Result.success()
    }

    private fun showNotification(content: NotificationContent): Boolean {
        val permissionState = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Timber.tag(TAG).w("Skipping notification because POST_NOTIFICATIONS is not granted")
            FirebaseUtils.logEvent("notification_permission_missing", Bundle().apply {
                putString("type", content.analyticsType)
            })
            return false
        }

        val resId = applicationContext.resources.getIdentifier(content.messageKey, "string", applicationContext.packageName)
        val text = if (resId != 0) {
            applicationContext.getString(resId, *content.messageArgs.toTypedArray())
        } else content.messageKey

        val titleResId = applicationContext.resources.getIdentifier(content.titleKey, "string", applicationContext.packageName)
        val title = if (titleResId != 0) {
            applicationContext.getString(titleResId)
        } else applicationContext.getString(R.string.app_name)

        val notificationBuilder = NotificationCompat.Builder(applicationContext, MyShareApp.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(
                MyShareNotificationIntentFactory.activityIntent(
                    context = applicationContext,
                    destination = content.destination,
                    notificationType = content.analyticsType
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        content.responseActions.forEach { responseAction ->
            val actionTitle = resolveText(responseAction.labelKey)
            notificationBuilder.addAction(
                R.drawable.messages,
                actionTitle,
                MyShareNotificationIntentFactory.reminderResponseIntent(
                    context = applicationContext,
                    notificationType = content.analyticsType,
                    responseAction = responseAction
                )
            )
        }
        val notification = notificationBuilder.build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return true
    }

    private fun resolveText(key: String): String {
        val resId = applicationContext.resources.getIdentifier(key, "string", applicationContext.packageName)
        return if (resId != 0) applicationContext.getString(resId) else key
    }

    private fun shouldDeliverToday(type: ReminderNotificationType, today: LocalDate): Boolean {
        val key = deliveryKey(type, today)
        val wasDelivered = applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(key, false)
        if (wasDelivered) {
            Timber.tag(TAG).d("Skipping duplicate reminder notification type=%s date=%s", type, today)
        }
        return !wasDelivered
    }

    private fun markDelivered(type: ReminderNotificationType, today: LocalDate) {
        applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(deliveryKey(type, today), true)
            .apply()
    }

    private fun deliveryKey(type: ReminderNotificationType, today: LocalDate): String =
        "notification_delivered_${type.name}_$today"

    companion object {
        const val UNIQUE_NAME = "payday_reminder"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "ReminderWorker"
        private const val PREFS_NAME = "myshare_notification_delivery"
    }
}
