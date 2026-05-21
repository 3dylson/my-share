package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.ms.myshare.R
import pt.ms.myshare.data.repository.PlannerRepositoryImpl
import pt.ms.myshare.data.repository.SharedUserPreferencesRepository
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PremiumCheckInPlan
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.use_case.BuildReminderNotificationContentUseCase
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreatePremiumCheckInPlanUseCase
import pt.ms.myshare.domain.use_case.NotificationContent
import pt.ms.myshare.domain.use_case.ReminderNotificationType
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import pt.ms.myshare.presentation.MyShareApp
import pt.ms.myshare.presentation.notifications.MyShareNotificationIntentFactory
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Provider

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val plannerRepository = PlannerRepositoryImpl(
        context,
        com.google.firebase.auth.FirebaseAuth.getInstance(),
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
    )
    private val userPreferencesRepository = SharedUserPreferencesRepository(
        context,
        Provider { com.google.firebase.auth.FirebaseAuth.getInstance() },
        Provider { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    )
    private val calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase())
    private val buildReminderNotificationContentUseCase = BuildReminderNotificationContentUseCase(calculatePlanPreviewUseCase)
    private val createPremiumCheckInPlanUseCase = CreatePremiumCheckInPlanUseCase()

    override fun doWork(): Result {
        val plan = plannerRepository.loadPlan() ?: return Result.success()
        val reminderConfiguration = plannerRepository.loadReminderConfiguration()
        if (!reminderConfiguration.enabled) return Result.success()

        val today = LocalDate.now()
        val notificationType = resolveNotificationType(today, reminderConfiguration.cadence, plan)
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

    private fun resolveNotificationType(
        today: LocalDate,
        cadence: ReminderCadence,
        plan: pt.ms.myshare.domain.model.SalaryPlan
    ): ReminderNotificationType? {
        val checkIn = createPremiumCheckInPlanUseCase.execute(
            plan = plan,
            latestReview = plannerRepository.loadLatestReview(),
            reminderConfiguration = plannerRepository.loadReminderConfiguration(),
            automationEnabled = plannerRepository.loadAutomationEnabled(),
            today = today
        )
        val premiumNotificationType = resolvePremiumCheckInNotificationType(checkIn, today)
        if (premiumNotificationType != null) return premiumNotificationType

        return when (cadence) {
            ReminderCadence.PAYDAY -> resolvePaydayReminderType(plan, today)
            ReminderCadence.WEEKLY_REVIEW -> ReminderNotificationType.WEEKLY_REVIEW.takeIf {
                today.dayOfWeek == DayOfWeek.SUNDAY
            }
        }
    }

    private fun resolvePaydayReminderType(
        plan: pt.ms.myshare.domain.model.SalaryPlan,
        today: LocalDate
    ): ReminderNotificationType? {
        if (!isPayday(plan, today)) return null

        val latestReviewDate = plannerRepository.loadLatestReview()?.let { it.paydayDate ?: it.createdAt }
        return if (latestReviewDate == today) {
            ReminderNotificationType.PAYDAY_ACTION
        } else {
            ReminderNotificationType.PAYDAY_REVIEW_DUE
        }
    }

    private fun resolvePremiumCheckInNotificationType(
        checkIn: PremiumCheckInPlan,
        today: LocalDate
    ): ReminderNotificationType? {
        if (!checkIn.automationEnabled) return null
        return when (checkIn.status) {
            PremiumCheckInStatus.READY_NOW -> ReminderNotificationType.PREMIUM_CHECK_IN_DUE
            PremiumCheckInStatus.OVERDUE -> {
                val daysOverdue = ChronoUnit.DAYS.between(checkIn.checkInDate, today)
                ReminderNotificationType.PREMIUM_CHECK_IN_OVERDUE.takeIf {
                    daysOverdue in 1..3 || today.dayOfWeek == DayOfWeek.SUNDAY
                }
            }
            PremiumCheckInStatus.SCHEDULED,
            PremiumCheckInStatus.REVIEWED -> null
        }
    }

    private fun isPayday(plan: pt.ms.myshare.domain.model.SalaryPlan, today: LocalDate): Boolean {
        return when (plan.payFrequency) {
            PayFrequency.MONTHLY -> today.dayOfMonth == (plan.monthlyPayday ?: 1).coerceIn(1, 28)
            PayFrequency.BIWEEKLY -> {
                var next = plan.nextBiweeklyPayday ?: return false
                while (next.isBefore(today)) {
                    next = next.plusDays(14)
                }
                today == next
            }
        }
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

        val notification = NotificationCompat.Builder(applicationContext, MyShareApp.CHANNEL_ID)
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
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return true
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
        private const val TAG = "ReminderWorker"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "myshare_notification_delivery"
    }
}
