package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.SalaryPlan
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class BuildPaydayNotificationMessageUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    fun execute(
        plan: SalaryPlan,
        locale: Locale = Locale.getDefault(),
        currencyCode: String? = null
    ): NotificationContent {
        val preview = calculatePlanPreviewUseCase.execute(plan, java.math.BigDecimal.ZERO)
        val currency = NumberFormat.getCurrencyInstance(locale).apply {
            currencyCode?.let { code -> runCatching { currency = Currency.getInstance(code) } }
        }
        return NotificationContent(
            titleKey = "notification_payday_title",
            messageKey = "notification_payday_body",
            messageArgs = listOf(
                currency.format(preview.fixedCostsPerPayday),
                currency.format(preview.flexibleSpendPerPayday),
                currency.format(preview.savingsPerPayday),
                currency.format(preview.investingPerPayday)
            )
        )
    }
}
