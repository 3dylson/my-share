package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class CalculatePlanPreviewUseCase @Inject constructor(
    private val resolveAllocationStrategyRulesUseCase: ResolveAllocationStrategyRulesUseCase
) {

    fun execute(plan: SalaryPlan, goalAmount: BigDecimal): PlanPreview {
        val fixedCostsPerPayday = fixedCostsPerPayday(plan.monthlyFixedCosts, plan.payFrequency)
        val remainingAfterFixed = plan.netIncomePerPayday.subtract(fixedCostsPerPayday).max(BigDecimal.ZERO)

        val rules = if (plan.rules.isEmpty()) {
            resolveAllocationStrategyRulesUseCase.execute(plan.focus, plan.preset, plan.strategy)
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
        val summary = when (plan.strategy) {
            pt.ms.myshare.domain.model.AllocationStrategy.NO_SAVINGS_NOW -> "plan_summary_no_savings_now"
            pt.ms.myshare.domain.model.AllocationStrategy.DEBT_FIRST -> "plan_summary_debt_first"
            pt.ms.myshare.domain.model.AllocationStrategy.INVESTING_FIRST -> "plan_summary_investing_first"
            pt.ms.myshare.domain.model.AllocationStrategy.FLEXIBLE_BUDGET_ONLY -> "plan_summary_flexible_budget_only"
            pt.ms.myshare.domain.model.AllocationStrategy.CUSTOM -> "plan_summary_custom_strategy"
            pt.ms.myshare.domain.model.AllocationStrategy.BALANCED_SAVINGS -> when (plan.focus) {
                PlanningFocus.SAVE_WITHOUT_STRESS -> "plan_summary_save_without_stress"
                PlanningFocus.INVEST_WITH_DISCIPLINE -> "plan_summary_invest_with_discipline"
                PlanningFocus.STOP_OVERSPENDING -> "plan_summary_stop_overspending"
                PlanningFocus.PLAN_TOGETHER -> "plan_summary_plan_together"
            }
        }

        return PlanPreview(
            incomePerPayday = plan.netIncomePerPayday.setScale(2, RoundingMode.HALF_UP),
            fixedCostsPerPayday = fixedCostsPerPayday.setScale(2, RoundingMode.HALF_UP),
            flexibleSpendPerPayday = flexibleSpend,
            savingsPerPayday = savings.setScale(2, RoundingMode.HALF_UP),
            investingPerPayday = investing.setScale(2, RoundingMode.HALF_UP),
            cryptoPerPayday = crypto.setScale(2, RoundingMode.HALF_UP),
            debtPerPayday = debt.setScale(2, RoundingMode.HALF_UP),
            priorityContributionPerPayday = totalRuleContribution.setScale(2, RoundingMode.HALF_UP),
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

    private companion object {
        const val SCALE = 4
    }
}
