package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.SalaryPlan
import java.util.Locale
import javax.inject.Inject

class BuildPaydayNotificationMessageUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    private val buildReminderNotificationContentUseCase =
        BuildReminderNotificationContentUseCase(calculatePlanPreviewUseCase)

    fun execute(
        plan: SalaryPlan,
        locale: Locale = Locale.getDefault(),
        currencyCode: String? = null
    ): NotificationContent = buildReminderNotificationContentUseCase.execute(
        plan = plan,
        type = ReminderNotificationType.PAYDAY_ACTION,
        locale = locale,
        currencyCode = currencyCode
    )
}
