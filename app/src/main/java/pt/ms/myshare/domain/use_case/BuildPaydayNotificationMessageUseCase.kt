package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.SalaryPlan
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

class BuildPaydayNotificationMessageUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    data class NotificationContent(val titleKey: String, val bodyKey: String, val args: List<String>)

    fun execute(plan: SalaryPlan, locale: Locale = Locale.getDefault()): NotificationContent {
        val preview = calculatePlanPreviewUseCase.execute(plan, java.math.BigDecimal.ZERO)
        val currency = NumberFormat.getCurrencyInstance(locale)
        return NotificationContent(
            titleKey = "notification_payday_title",
            bodyKey = "notification_payday_body",
            args = listOf(
                currency.format(preview.fixedCostsPerPayday),
                currency.format(preview.flexibleSpendPerPayday),
                currency.format(preview.savingsPerPayday),
                currency.format(preview.investingPerPayday)
            )
        )
    }
}
