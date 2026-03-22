package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class CalculatePlanPreviewUseCase @Inject constructor() {

    fun execute(plan: SalaryPlan): PlanPreview {
        val fixedCostsPerPayday = fixedCostsPerPayday(plan.monthlyFixedCosts, plan.payFrequency)
        val remainingAfterFixed = plan.netIncomePerPayday.subtract(fixedCostsPerPayday).max(BigDecimal.ZERO)

        val weights = adjustedWeights(plan.focus, plan.preset)
        val flexibleSpend = percentageOf(remainingAfterFixed, weights.flexibleSpend)
        val savings = percentageOf(remainingAfterFixed, weights.savings)
        val investing = percentageOf(remainingAfterFixed, weights.investing)
        val crypto = percentageOf(remainingAfterFixed, weights.crypto)
        val debt = remainingAfterFixed
            .subtract(flexibleSpend)
            .subtract(savings)
            .subtract(investing)
            .subtract(crypto)
            .setScale(2, RoundingMode.HALF_UP)

        val monthlyGoalContribution = normalizeToMonthly(savings.add(investing).add(crypto).add(debt), plan.payFrequency)
        val weeklySpend = normalizeToMonthly(flexibleSpend, plan.payFrequency)
            .divide(BigDecimal("4.3333"), SCALE, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP)

        val nextPayday = nextPayday(plan)
        val goalTargetDate = calculateGoalTargetDate(plan.goalAmount, monthlyGoalContribution)
        val summary = when (plan.focus) {
            PlanningFocus.SAVE_WITHOUT_STRESS -> "A calm split that protects essentials and builds savings."
            PlanningFocus.INVEST_WITH_DISCIPLINE -> "A disciplined split that keeps investing consistent each payday."
            PlanningFocus.STOP_OVERSPENDING -> "A tighter plan that protects future money before flexible spending."
            PlanningFocus.PLAN_TOGETHER -> "A shared-friendly split that keeps bills clear and progress visible."
        }

        return PlanPreview(
            incomePerPayday = plan.netIncomePerPayday.setScale(2, RoundingMode.HALF_UP),
            fixedCostsPerPayday = fixedCostsPerPayday.setScale(2, RoundingMode.HALF_UP),
            flexibleSpendPerPayday = flexibleSpend,
            savingsPerPayday = savings,
            investingPerPayday = investing,
            cryptoPerPayday = crypto,
            debtPerPayday = debt,
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
