package pt.ms.myshare.presentation.ui.onboarding

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaySchedule
import pt.ms.myshare.domain.model.PlanInput
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import java.time.LocalDate
import java.time.YearMonth

class ReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {
    override fun doWork(): Result {
        val planInput = getLatestPlanInput() ?: return Result.success()

        val today = LocalDate.now()
        val isPayday = isPayday(today, planInput.schedule)
        if (!isPayday) return Result.success()

        val preview = CalculatePlanPreviewUseCase().execute(planInput)

        val notificationText = "It’s payday — " +
            "Stocks ${preview.perPaydayAmounts.stocks} • " +
            "Crypto ${preview.perPaydayAmounts.crypto} • " +
            "Savings ${preview.perPaydayAmounts.savings}"

        showNotification(notificationText)
        advanceIfBiWeekly(today, planInput)
        return Result.success()
    }

    private fun getLatestPlanInput(): PlanInput? {
        return OnboardingPrefs.loadPlanInput(applicationContext)
    }

    private fun isPayday(today: LocalDate, schedule: PaySchedule): Boolean {
        return when (schedule) {
            is PaySchedule.Monthly -> {
                val lastDay = YearMonth.from(today).lengthOfMonth()
                val effectiveDay = schedule.dayOfMonth.coerceIn(1, lastDay)
                today.dayOfMonth == effectiveDay
            }
            is PaySchedule.BiWeekly -> {
                // If user missed a payday while the app was inactive, we align to the next one.
                val next = alignBiWeeklyNextPayday(schedule.nextPayday, today)
                today == next
            }
        }
    }

    private fun alignBiWeeklyNextPayday(stored: LocalDate, today: LocalDate): LocalDate {
        var next = stored
        while (next.isBefore(today)) {
            next = next.plusDays(14)
        }
        return next
    }

    private fun advanceIfBiWeekly(today: LocalDate, input: PlanInput) {
        val schedule = input.schedule
        if (schedule is PaySchedule.BiWeekly) {
            val aligned = alignBiWeeklyNextPayday(schedule.nextPayday, today)
            val next = aligned.plusDays(14)
            OnboardingPrefs.updateNextBiWeeklyPayday(applicationContext, next)
        }
    }

    private fun showNotification(text: String) {
        val builder = NotificationCompat.Builder(applicationContext, "payday_reminder")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("My Share")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        NotificationManagerCompat.from(applicationContext).notify(1001, builder.build())
    }
}

