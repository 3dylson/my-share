package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PremiumRulePaydayMix
import pt.ms.myshare.domain.model.PremiumRulePaydayMixItem
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CreatePremiumRulePaydayMixUseCase @Inject constructor() {

    fun execute(plan: SalaryPlan): PremiumRulePaydayMix? {
        val remainingAfterFixed = plan.netIncomePerPayday
            .subtract(fixedCostsPerPayday(plan.monthlyFixedCosts, plan.payFrequency))
            .max(BigDecimal.ZERO)

        val itemsWithAmount = plan.rules.mapNotNull { rule ->
            if (!rule.type.isPriorityRule()) return@mapNotNull null
            val amount = contributionAmount(rule, remainingAfterFixed)
                .setScale(2, RoundingMode.HALF_UP)
                .takeIf { it > BigDecimal.ZERO }
                ?: return@mapNotNull null
            rule to amount
        }
        val totalMove = itemsWithAmount.fold(BigDecimal.ZERO) { total, (_, amount) ->
            total.add(amount)
        }.setScale(2, RoundingMode.HALF_UP)

        if (itemsWithAmount.size < MINIMUM_RULE_COUNT || totalMove <= BigDecimal.ZERO) {
            return null
        }

        val items = itemsWithAmount.map { (rule, amount) ->
            PremiumRulePaydayMixItem(
                ruleId = rule.id,
                amount = amount,
                sharePercent = amount
                    .multiply(ONE_HUNDRED)
                    .divide(totalMove, SCALE, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP)
            )
        }

        return PremiumRulePaydayMix(
            totalMove = totalMove,
            items = items
        )
    }

    private fun contributionAmount(rule: PaydayRule, remainingAfterFixed: BigDecimal): BigDecimal {
        return if (rule.isPercentage) {
            remainingAfterFixed
                .multiply(rule.amount)
                .divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP)
        } else {
            rule.amount
        }
    }

    private fun fixedCostsPerPayday(monthlyFixedCosts: BigDecimal, payFrequency: PayFrequency): BigDecimal {
        return when (payFrequency) {
            PayFrequency.MONTHLY -> monthlyFixedCosts
            PayFrequency.BIWEEKLY -> monthlyFixedCosts
                .multiply(BigDecimal("12"))
                .divide(BigDecimal("26"), SCALE, RoundingMode.HALF_UP)
        }
    }

    private fun PaydayRuleType.isPriorityRule(): Boolean {
        return when (this) {
            PaydayRuleType.SAVINGS,
            PaydayRuleType.INVESTING,
            PaydayRuleType.CRYPTO,
            PaydayRuleType.DEBT -> true
            PaydayRuleType.OTHER -> false
        }
    }

    private companion object {
        const val MINIMUM_RULE_COUNT = 2
        const val SCALE = 4
        val ONE_HUNDRED: BigDecimal = BigDecimal("100")
    }
}
