package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class CalculatePlanPreviewUseCase @Inject constructor() {

    fun execute(plan: SalaryPlan, goalAmount: BigDecimal): PlanPreview {
        val fixedCostsPerPayday = fixedCostsPerPayday(plan.monthlyFixedCosts, plan.payFrequency)
        val remainingAfterFixed = plan.netIncomePerPayday.subtract(fixedCostsPerPayday).max(BigDecimal.ZERO)

        val rules = if (plan.rules.isEmpty()) {
            generateRulesFromPreset(plan)
        } else {
            plan.rules
        }

        var savings = BigDecimal.ZERO
        var investing = BigDecimal.ZERO
        var crypto = BigDecimal.ZERO
        var debt = BigDecimal.ZERO

        rules.forEach { rule ->
            val amount = if (rule.isPercentage) {
                percentageOf(remainingAfterFixed, rule.amount.divide(BigDecimal("100"), SCALE, RoundingMode.HALF_UP))
            } else {
                rule.amount
            }

            when (rule.type) {
                PaydayRuleType.SAVINGS -> savings = savings.add(amount)
                PaydayRuleType.INVESTING -> investing = investing.add(amount)
                PaydayRuleType.CRYPTO -> crypto = crypto.add(amount)
                PaydayRuleType.DEBT -> debt = debt.add(amount)
                PaydayRuleType.OTHER -> { /* Counted towards total contribution, but not specific category */ }
            }
        }

        val totalRuleContribution = savings.add(investing).add(crypto).add(debt)
        val flexibleSpend = remainingAfterFixed.subtract(totalRuleContribution).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO)

        val monthlyGoalContribution = normalizeToMonthly(totalRuleContribution, plan.payFrequency)
        val weeklySpend = normalizeToMonthly(flexibleSpend, plan.payFrequency)
            .divide(BigDecimal("4.3333"), SCALE, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP)

        val nextPayday = nextPayday(plan)
        val goalTargetDate = calculateGoalTargetDate(goalAmount, monthlyGoalContribution)
        val summary = when (plan.focus) {
            PlanningFocus.SAVE_WITHOUT_STRESS -> "plan_summary_save_without_stress"
            PlanningFocus.INVEST_WITH_DISCIPLINE -> "plan_summary_invest_with_discipline"
            PlanningFocus.STOP_OVERSPENDING -> "plan_summary_stop_overspending"
            PlanningFocus.PLAN_TOGETHER -> "plan_summary_plan_together"
        }

        return PlanPreview(
            incomePerPayday = plan.netIncomePerPayday.setScale(2, RoundingMode.HALF_UP),
            fixedCostsPerPayday = fixedCostsPerPayday.setScale(2, RoundingMode.HALF_UP),
            flexibleSpendPerPayday = flexibleSpend,
            savingsPerPayday = savings.setScale(2, RoundingMode.HALF_UP),
            investingPerPayday = investing.setScale(2, RoundingMode.HALF_UP),
            cryptoPerPayday = crypto.setScale(2, RoundingMode.HALF_UP),
            debtPerPayday = debt.setScale(2, RoundingMode.HALF_UP),
            weeklyFlexibleSpend = weeklySpend,
            monthlyGoalContribution = monthlyGoalContribution.setScale(2, RoundingMode.HALF_UP),
            nextPayday = nextPayday,
            goalTargetDate = goalTargetDate,
            summary = summary
        )
    }

    private fun normalizeToMonthly(amountPerPayday: BigDecimal, payFrequency: PayFrequency): BigDecimal = when (payFrequency) {
        PayFrequency.MONTHLY -> amountPerPayday
        PayFrequency.BIWEEKLY -> amountPerPayday.multiply(BigDecimal("26")).divide(BigDecimal("12"), SCALE, RoundingMode.HALF_UP)
    }

    private fun fixedCostsPerPayday(monthlyFixedCosts: BigDecimal, payFrequency: PayFrequency): BigDecimal = when (payFrequency) {
        PayFrequency.MONTHLY -> monthlyFixedCosts
        PayFrequency.BIWEEKLY -> monthlyFixedCosts.multiply(BigDecimal("12")).divide(BigDecimal("26"), SCALE, RoundingMode.HALF_UP)
    }

    private fun nextPayday(plan: SalaryPlan): LocalDate {
        val today = LocalDate.now()
        return when (plan.payFrequency) {
            PayFrequency.MONTHLY -> {
                val payday = (plan.monthlyPayday ?: today.dayOfMonth).coerceIn(1, 28)
                val thisMonth = today.withDayOfMonth(payday)
                if (thisMonth.isBefore(today)) thisMonth.plusMonths(1) else thisMonth
            }
            PayFrequency.BIWEEKLY -> {
                var next = plan.nextBiweeklyPayday ?: today.plusDays(14)
                while (next.isBefore(today)) {
                    next = next.plusDays(14)
                }
                next
            }
        }
    }

    private fun calculateGoalTargetDate(goalAmount: BigDecimal, monthlyGoalContribution: BigDecimal): YearMonth? {
        if (goalAmount <= BigDecimal.ZERO || monthlyGoalContribution <= BigDecimal.ZERO) return null
        val monthsNeeded = goalAmount.divide(monthlyGoalContribution, SCALE, RoundingMode.UP)
            .setScale(0, RoundingMode.UP)
            .toLong()
        return YearMonth.now().plusMonths(monthsNeeded)
    }

    private fun percentageOf(base: BigDecimal, percentage: BigDecimal): BigDecimal =
        base.multiply(percentage).setScale(2, RoundingMode.HALF_UP)

    private fun generateRulesFromPreset(plan: SalaryPlan): List<PaydayRule> {
        val weights = adjustedWeights(plan.focus, plan.preset)
        val rules = mutableListOf<PaydayRule>()
        
        if (weights.savings > BigDecimal.ZERO) {
            rules.add(PaydayRule(name = "Savings", amount = weights.savings.multiply(BigDecimal("100")), type = PaydayRuleType.SAVINGS, isPercentage = true))
        }
        if (weights.investing > BigDecimal.ZERO) {
            rules.add(PaydayRule(name = "Investing", amount = weights.investing.multiply(BigDecimal("100")), type = PaydayRuleType.INVESTING, isPercentage = true))
        }
        if (weights.crypto > BigDecimal.ZERO) {
            rules.add(PaydayRule(name = "Crypto", amount = weights.crypto.multiply(BigDecimal("100")), type = PaydayRuleType.CRYPTO, isPercentage = true))
        }
        
        return rules
    }

    private fun adjustedWeights(focus: PlanningFocus, preset: AllocationPreset): AllocationWeights {
        val base = when (focus) {
            PlanningFocus.SAVE_WITHOUT_STRESS -> AllocationWeights(
                flexibleSpend = bd("0.45"), savings = bd("0.35"), investing = bd("0.15"), crypto = bd("0.00")
            )
            PlanningFocus.INVEST_WITH_DISCIPLINE -> AllocationWeights(
                flexibleSpend = bd("0.35"), savings = bd("0.20"), investing = bd("0.30"), crypto = bd("0.10")
            )
            PlanningFocus.STOP_OVERSPENDING -> AllocationWeights(
                flexibleSpend = bd("0.40"), savings = bd("0.35"), investing = bd("0.10"), crypto = bd("0.00")
            )
            PlanningFocus.PLAN_TOGETHER -> AllocationWeights(
                flexibleSpend = bd("0.50"), savings = bd("0.25"), investing = bd("0.10"), crypto = bd("0.00")
            )
        }
        return when (preset) {
            AllocationPreset.CONSERVATIVE -> base.copy(
                savings = base.savings.add(bd("0.10")),
                investing = base.investing.subtract(bd("0.05")).max(BigDecimal.ZERO),
                crypto = base.crypto.subtract(bd("0.05")).max(BigDecimal.ZERO)
            )
            AllocationPreset.BALANCED -> base
            AllocationPreset.GROWTH -> base.copy(
                savings = base.savings.subtract(bd("0.10")).max(BigDecimal.ZERO),
                investing = base.investing.add(bd("0.07")),
                crypto = base.crypto.add(bd("0.03"))
            )
        }
    }

    private data class AllocationWeights(
        val flexibleSpend: BigDecimal,
        val savings: BigDecimal,
        val investing: BigDecimal,
        val crypto: BigDecimal
    )

    private companion object {
        const val SCALE = 4
        fun bd(value: String): BigDecimal = BigDecimal(value)
    }
}
