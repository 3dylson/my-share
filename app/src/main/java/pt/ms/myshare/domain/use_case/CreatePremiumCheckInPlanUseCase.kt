package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PremiumCheckInPlan
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

class CreatePremiumCheckInPlanUseCase @Inject constructor() {

    fun execute(
        plan: SalaryPlan,
        latestReview: ManualReview?,
        reminderConfiguration: ReminderConfiguration,
        automationEnabled: Boolean,
        today: LocalDate = LocalDate.now()
    ): PremiumCheckInPlan {
        val currentPayday = currentPaydayFor(plan, today)
        val nextPayday = nextPaydayAfter(plan, currentPayday)
        val latestReviewDate = latestReview?.paydayDate ?: latestReview?.createdAt
        val reviewCoversCurrentPayday = latestReviewDate != null && !latestReviewDate.isBefore(currentPayday)
        val firstPaydayHasNotArrived = currentPayday.isBefore(plan.createdAt)

        val checkInDate = when {
            firstPaydayHasNotArrived -> nextPaydayAfter(plan, currentPayday)
            reviewCoversCurrentPayday -> nextPayday
            else -> currentPayday
        }
        val status = when {
            reviewCoversCurrentPayday -> PremiumCheckInStatus.REVIEWED
            checkInDate.isBefore(today) -> PremiumCheckInStatus.OVERDUE
            checkInDate == today -> PremiumCheckInStatus.READY_NOW
            else -> PremiumCheckInStatus.SCHEDULED
        }

        Timber.tag(TAG).d(
            "Premium check-in computed status=%s checkInDate=%s latestReviewDate=%s reminder=%s automation=%s",
            status,
            checkInDate,
            latestReviewDate,
            reminderConfiguration.enabled,
            automationEnabled
        )

        return PremiumCheckInPlan(
            status = status,
            checkInDate = checkInDate,
            latestReviewDate = latestReviewDate,
            reminderEnabled = reminderConfiguration.enabled,
            automationEnabled = automationEnabled
        )
    }

    private fun currentPaydayFor(plan: SalaryPlan, today: LocalDate): LocalDate {
        return when (plan.payFrequency) {
            PayFrequency.MONTHLY -> {
                val payday = (plan.monthlyPayday ?: today.dayOfMonth).coerceIn(1, 28)
                val thisMonth = today.withDayOfMonth(payday)
                if (thisMonth.isAfter(today)) thisMonth.minusMonths(1) else thisMonth
            }
            PayFrequency.BIWEEKLY -> {
                val anchor = plan.nextBiweeklyPayday ?: today
                var payday = anchor
                while (payday.isAfter(today)) {
                    payday = payday.minusDays(BIWEEKLY_DAYS)
                }
                while (payday.plusDays(BIWEEKLY_DAYS).isBefore(today) || payday.plusDays(BIWEEKLY_DAYS) == today) {
                    payday = payday.plusDays(BIWEEKLY_DAYS)
                }
                payday
            }
        }
    }

    private fun nextPaydayAfter(plan: SalaryPlan, payday: LocalDate): LocalDate {
        return when (plan.payFrequency) {
            PayFrequency.MONTHLY -> payday.plusMonths(1)
            PayFrequency.BIWEEKLY -> payday.plusDays(BIWEEKLY_DAYS)
        }
    }

    private companion object {
        const val TAG = "CreatePremiumCheckInPlan"
        const val BIWEEKLY_DAYS = 14L
    }
}
