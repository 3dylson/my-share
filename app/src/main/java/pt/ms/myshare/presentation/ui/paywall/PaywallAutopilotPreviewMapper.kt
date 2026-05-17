package pt.ms.myshare.presentation.ui.paywall

import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import java.math.BigDecimal
import java.math.RoundingMode

object PaywallAutopilotPreviewMapper {

    fun map(
        preview: PlanPreview?,
        userPreferences: UserPreferences
    ): PaywallAutopilotPreviewUiState {
        if (preview == null) return PaywallAutopilotPreviewUiState()

        val suggestedAdjustmentAmount = preview.priorityContributionPerPayday
            .takeIf { it > BigDecimal.ZERO }
            ?.let { priorityContribution ->
                calculateSuggestedAdjustmentAmount(
                    weeklyFlexibleSpend = preview.weeklyFlexibleSpend,
                    priorityContribution = priorityContribution
                )
            }
            ?.let { amount ->
                LocalizedAmountFormatter.formatCurrency(
                    amount = amount,
                    locale = userPreferences.locale,
                    currencyCode = userPreferences.currencyCode
                )
            }

        return PaywallAutopilotPreviewUiState(
            weeklyFlexibleSpend = LocalizedAmountFormatter.formatCurrency(
                amount = preview.weeklyFlexibleSpend,
                locale = userPreferences.locale,
                currencyCode = userPreferences.currencyCode
            ),
            priorityContribution = preview.priorityContributionPerPayday
                .takeIf { it > BigDecimal.ZERO }
                ?.let { amount ->
                    LocalizedAmountFormatter.formatCurrency(
                        amount = amount,
                        locale = userPreferences.locale,
                        currencyCode = userPreferences.currencyCode
                    )
                },
            suggestedAdjustmentAmount = suggestedAdjustmentAmount,
            hasPersonalPlan = true
        )
    }

    private fun calculateSuggestedAdjustmentAmount(
        weeklyFlexibleSpend: BigDecimal,
        priorityContribution: BigDecimal
    ): BigDecimal? {
        if (weeklyFlexibleSpend <= BigDecimal.ZERO || priorityContribution <= BigDecimal.ZERO) {
            return null
        }

        val rawBuffer = weeklyFlexibleSpend.multiply(BigDecimal("0.25"))
        val roundedToTen = rawBuffer
            .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)
            .multiply(BigDecimal.TEN)
        val meaningfulBuffer = if (roundedToTen > BigDecimal.ZERO) {
            roundedToTen
        } else {
            rawBuffer.setScale(0, RoundingMode.HALF_UP)
        }
        val roundedPriority = priorityContribution.setScale(0, RoundingMode.DOWN)

        return meaningfulBuffer
            .min(roundedPriority)
            .takeIf { it > BigDecimal.ZERO }
    }
}
