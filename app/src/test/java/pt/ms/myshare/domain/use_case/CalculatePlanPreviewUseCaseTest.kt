package pt.ms.myshare.domain.use_case

import org.junit.Assert.*
import org.junit.Test
import pt.ms.myshare.domain.model.*
import java.math.BigDecimal
import java.time.LocalDate

class CalculatePlanPreviewUseCaseTest {
    private val useCase = pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase()

    @Test
    fun testZeroSalary() {
        val input = PlanInput(
            netSalary = BigDecimal.ZERO,
            schedule = PaySchedule.Monthly(1),
            preset = AllocationPreset.BALANCED,
            goal = Goal(BigDecimal("1000"), GoalType.EMERGENCY_FUND)
        )
        val result = useCase.execute(input)
        println("testZeroSalary result: $result")
        assertEquals(BigDecimal.ZERO.setScale(0), result.totalPerMonth)
        assertNull(result.goalTargetDate)
    }

    @Test
    fun testTinySalaryHugeGoal() {
        val input = PlanInput(
            netSalary = BigDecimal("0.01"),
            schedule = PaySchedule.Monthly(1),
            preset = AllocationPreset.GROWTH,
            goal = Goal(BigDecimal("1000000"), GoalType.INVEST_TARGET)
        )
        val result = useCase.execute(input)
        assertTrue(result.goalTargetDate != null)
    }

    @Test
    fun testBiWeeklyNormalization() {
        val input = PlanInput(
            netSalary = BigDecimal("1000"),
            schedule = PaySchedule.BiWeekly(LocalDate.now()),
            preset = AllocationPreset.CONSERVATIVE,
            goal = Goal(BigDecimal("12000"), GoalType.CUSTOM)
        )
        val result = useCase.execute(input)
        // 1000 * 26 / 12 = 2166.66
        assertEquals(BigDecimal("2166.66"), result.totalPerMonth)
    }

    @Test
    fun testBalancedAllocation() {
        val input = PlanInput(
            netSalary = BigDecimal("1000"),
            schedule = PaySchedule.Monthly(1),
            preset = AllocationPreset.BALANCED,
            goal = Goal(BigDecimal("3000"), GoalType.CUSTOM)
        )
        val result = useCase.execute(input)
        assertEquals(BigDecimal("300.00"), result.perPaydayAmounts.stocks)
        assertEquals(BigDecimal("100.00"), result.perPaydayAmounts.crypto)
        assertEquals(BigDecimal("600.00"), result.perPaydayAmounts.savings)
    }
}
