package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ResolveReminderNotificationTypeUseCase @Inject constructor(
    private val createPremiumCheckInPlanUseCase: CreatePremiumCheckInPlanUseCase
) {
    fun execute(
        plan: SalaryPlan,
        latestReview: ManualReview?,
        reminderConfiguration: ReminderConfiguration,
        automationEnabled: Boolean,
        today: LocalDate
    ): ReminderNotificationType? {
        val premiumNotificationType = resolvePremiumCheckInNotificationType(
            plan = plan,
            latestReview = latestReview,
            reminderConfiguration = reminderConfiguration,
            automationEnabled = automationEnabled,
            today = today
        )
        if (premiumNotificationType != null) return premiumNotificationType

        return when (reminderConfiguration.cadence) {
            ReminderCadence.PAYDAY -> resolvePaydayReminderType(plan, latestReview, today)
            ReminderCadence.WEEKLY_REVIEW -> ReminderNotificationType.WEEKLY_REVIEW.takeIf {
                today.dayOfWeek == DayOfWeek.SUNDAY
            }
        }.also {
            Timber.tag(TAG).d(
                "Reminder type resolved type=%s cadence=%s today=%s",
                it,
                reminderConfiguration.cadence,
                today
            )
        }
    }

    private fun resolvePaydayReminderType(
        plan: SalaryPlan,
        latestReview: ManualReview?,
        today: LocalDate
    ): ReminderNotificationType? {
        if (!isPayday(plan, today)) return null

        val latestReviewDate = latestReview?.let { it.paydayDate ?: it.createdAt }
        return if (latestReviewDate == today) {
            ReminderNotificationType.PAYDAY_ACTION
        } else {
            ReminderNotificationType.PAYDAY_REVIEW_DUE
        }
    }

    private fun resolvePremiumCheckInNotificationType(
        plan: SalaryPlan,
        latestReview: ManualReview?,
        reminderConfiguration: ReminderConfiguration,
        automationEnabled: Boolean,
        today: LocalDate
    ): ReminderNotificationType? {
        val checkIn = createPremiumCheckInPlanUseCase.execute(
            plan = plan,
            latestReview = latestReview,
            reminderConfiguration = reminderConfiguration,
            automationEnabled = automationEnabled,
            today = today
        )
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
        }?.also {
            Timber.tag(TAG).d("Premium reminder type resolved type=%s today=%s", it, today)
        }
    }

    private fun isPayday(plan: SalaryPlan, today: LocalDate): Boolean {
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

    private companion object {
        const val TAG = "ResolveReminderNotificationType"
    }
}
