package pt.ms.myshare.presentation.ui.onboarding

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import pt.ms.myshare.domain.model.ReminderConfiguration
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderWorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun sync(configuration: ReminderConfiguration) {
        if (configuration.enabled) {
            schedule(configuration)
        } else {
            cancel()
        }
    }

    private fun schedule(configuration: ReminderConfiguration) {
        val initialDelayMillis = initialDelayMillis(configuration, LocalDateTime.now())
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag(ReminderWorker.UNIQUE_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Timber.tag(TAG).d(
            "Reminder work scheduled hour=%s minute=%s cadence=%s delayMs=%s",
            configuration.hourOfDay,
            configuration.minute,
            configuration.cadence,
            initialDelayMillis
        )
    }

    private fun cancel() {
        workManager.cancelUniqueWork(ReminderWorker.UNIQUE_NAME)
        Timber.tag(TAG).d("Reminder work cancelled")
    }

    private fun initialDelayMillis(configuration: ReminderConfiguration, now: LocalDateTime): Long {
        val selectedTime = now.toLocalDate().atTime(configuration.hourOfDay, configuration.minute)
        val nextRunAt = if (selectedTime.isAfter(now)) selectedTime else selectedTime.plusDays(1)
        return Duration.between(now, nextRunAt).toMillis().coerceAtLeast(0)
    }

    private companion object {
        const val TAG = "ReminderWorkScheduler"
    }
}
