package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.SalaryPlan
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

class BuildPaydayNotificationMessageUseCase @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase
) {
    fun execute(plan: SalaryPlan, locale: Locale = Locale.getDefault()): String {
        val preview = calculatePlanPreviewUseCase.execute(plan)
        val currency = NumberFormat.getCurrencyInstance(locale)
        return "It’s payday — fixed ${currency.format(preview.fixedCostsPerPayday)}, spend ${currency.format(preview.flexibleSpendPerPayday)}, save ${currency.format(preview.savingsPerPayday)}, invest ${currency.format(preview.investingPerPayday)}."
    }
}
