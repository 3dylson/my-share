package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

class CalculatePlanPreviewUseCase {
    fun execute(input: PlanInput): PlanPreview {
        val allocation = getAllocationPercentages(input.preset)
        val perPaydayAmounts = calculatePerPaydayAmounts(input.netSalary, allocation)
        val totalPerMonth = normalizeToMonthly(input.netSalary, input.schedule)
        val goalTargetDate = calculateGoalTargetDate(input.goal.amount, totalPerMonth)
        return PlanPreview(
            perPaydayAmounts = perPaydayAmounts,
            totalPerMonth = totalPerMonth,
            goalTargetDate = goalTargetDate
        )
    }

    private fun getAllocationPercentages(preset: AllocationPreset): Triple<BigDecimal, BigDecimal, BigDecimal> {
        return when (preset) {
            AllocationPreset.CONSERVATIVE -> Triple(BigDecimal("0.1"), BigDecimal("0.05"), BigDecimal("0.85"))
            AllocationPreset.BALANCED -> Triple(BigDecimal("0.3"), BigDecimal("0.1"), BigDecimal("0.6"))
            AllocationPreset.GROWTH -> Triple(BigDecimal("0.5"), BigDecimal("0.2"), BigDecimal("0.3"))
        }
    }

    private fun calculatePerPaydayAmounts(netSalary: BigDecimal, allocation: Triple<BigDecimal, BigDecimal, BigDecimal>): PerPaydayAmounts {
        val stocks = netSalary.multiply(allocation.first).setScale(2, RoundingMode.DOWN)
        val crypto = netSalary.multiply(allocation.second).setScale(2, RoundingMode.DOWN)
        val savings = netSalary.multiply(allocation.third).setScale(2, RoundingMode.DOWN)
        return PerPaydayAmounts(stocks, crypto, savings)
    }

    private fun normalizeToMonthly(netSalary: BigDecimal, schedule: PaySchedule): BigDecimal {
        return when (schedule) {
            is PaySchedule.Monthly -> netSalary
            is PaySchedule.BiWeekly -> netSalary.multiply(BigDecimal("26")).divide(BigDecimal("12"), 2, RoundingMode.DOWN)
        }
    }

    private fun calculateGoalTargetDate(goalAmount: BigDecimal, totalPerMonth: BigDecimal): YearMonth? {
        if (totalPerMonth.compareTo(BigDecimal.ZERO) == 0) return null
        val monthsNeeded = goalAmount.divide(totalPerMonth, 10, RoundingMode.UP).setScale(0, RoundingMode.UP)
        val now = YearMonth.now()
        return now.plusMonths(monthsNeeded.toLong())
    }
}
