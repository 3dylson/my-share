package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class BuildReminderNotificationContentUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    fun execute(
        plan: SalaryPlan,
        type: ReminderNotificationType,
        locale: Locale = Locale.getDefault(),
        currencyCode: String? = null
    ): NotificationContent {
        val preview = calculatePlanPreviewUseCase.execute(plan, BigDecimal.ZERO)
        val currencyFormatter = NumberFormat.getCurrencyInstance(locale).apply {
            currencyCode?.let { code -> runCatching { currency = Currency.getInstance(code) } }
        }
        val fixedCosts = currencyFormatter.format(preview.fixedCostsPerPayday)
        val flexibleSpend = currencyFormatter.format(preview.flexibleSpendPerPayday)
        val priorityMove = currencyFormatter.format(preview.priorityContributionPerPayday)
        val weeklyGuide = currencyFormatter.format(preview.weeklyFlexibleSpend)

        return when (type) {
            ReminderNotificationType.PAYDAY_ACTION -> NotificationContent(
                titleKey = "notification_payday_title",
                messageKey = "notification_payday_body",
                messageArgs = listOf(fixedCosts, flexibleSpend, priorityMove),
                destination = DESTINATION_PLAN,
                analyticsType = "payday_action"
            )
            ReminderNotificationType.PAYDAY_REVIEW_DUE -> NotificationContent(
                titleKey = "notification_payday_review_due_title",
                messageKey = "notification_payday_review_due_body",
                messageArgs = listOf(weeklyGuide),
                destination = DESTINATION_REVIEW,
                analyticsType = "payday_review_due"
            )
            ReminderNotificationType.WEEKLY_REVIEW -> NotificationContent(
                titleKey = "notification_weekly_review_title",
                messageKey = "notification_weekly_review_body",
                messageArgs = listOf(weeklyGuide),
                destination = DESTINATION_REVIEW,
                analyticsType = "weekly_review"
            )
            ReminderNotificationType.PREMIUM_CHECK_IN_DUE -> NotificationContent(
                titleKey = "notification_premium_checkin_due_title",
                messageKey = "notification_premium_checkin_due_body",
                messageArgs = listOf(weeklyGuide, priorityMove),
                destination = DESTINATION_REVIEW,
                analyticsType = "premium_checkin_due"
            )
            ReminderNotificationType.PREMIUM_CHECK_IN_OVERDUE -> NotificationContent(
                titleKey = "notification_premium_checkin_overdue_title",
                messageKey = "notification_premium_checkin_overdue_body",
                messageArgs = listOf(weeklyGuide, priorityMove),
                destination = DESTINATION_REVIEW,
                analyticsType = "premium_checkin_overdue"
            )
        }
    }

    companion object {
        const val DESTINATION_PLAN = "plan"
        const val DESTINATION_REVIEW = "review"
    }
}
