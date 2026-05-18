package pt.ms.myshare.presentation.ui.onboarding

import android.Manifest
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.ms.myshare.R
import pt.ms.myshare.data.repository.PlannerRepositoryImpl
import pt.ms.myshare.data.repository.SharedUserPreferencesRepository
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.use_case.BuildPaydayNotificationMessageUseCase
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import pt.ms.myshare.presentation.MyShareApp
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
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
    private val buildPaydayNotificationMessageUseCase = BuildPaydayNotificationMessageUseCase(calculatePlanPreviewUseCase)

    override fun doWork(): Result {
        val plan = plannerRepository.loadPlan() ?: return Result.success()
        val reminderConfiguration = plannerRepository.loadReminderConfiguration()
        if (!reminderConfiguration.enabled) return Result.success()

        val shouldNotify = when (reminderConfiguration.cadence) {
            ReminderCadence.PAYDAY -> isPayday(plan, LocalDate.now())
            ReminderCadence.WEEKLY_REVIEW -> LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY
        }
        if (!shouldNotify) return Result.success()

        val preferences = userPreferencesRepository.loadPreferences()
        val content = buildPaydayNotificationMessageUseCase.execute(
            plan = plan,
            locale = preferences.locale,
            currencyCode = preferences.currencyCode
        )
        showNotification(content)
        Timber.tag(TAG).d("Reminder delivered cadence=%s", reminderConfiguration.cadence)
        return Result.success()
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

    private fun showNotification(content: pt.ms.myshare.domain.use_case.NotificationContent) {
        val permissionState = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Timber.tag(TAG).w("Skipping notification because POST_NOTIFICATIONS is not granted")
            return
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val UNIQUE_NAME = "payday_reminder"
        private const val TAG = "ReminderWorker"
        private const val NOTIFICATION_ID = 1001
    }
}
