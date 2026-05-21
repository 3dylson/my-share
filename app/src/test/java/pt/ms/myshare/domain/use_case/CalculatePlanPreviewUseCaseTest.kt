package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal

class CalculatePlanPreviewUseCaseTest {

    private val useCase = CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase())

    @Test
    fun `monthly plan keeps monthly fixed costs on the payday`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.BALANCED
        )

        val preview = useCase.execute(plan, BigDecimal("3000"))

        assertEquals(BigDecimal("400.00"), preview.fixedCostsPerPayday)
        assertEquals(BigDecimal("600.00"), preview.flexibleSpendPerPayday + preview.savingsPerPayday + preview.investingPerPayday + preview.cryptoPerPayday + preview.debtPerPayday)
        assertTrue(preview.monthlyGoalContribution > BigDecimal.ZERO)
    }

    @Test
    fun `biweekly plan normalizes fixed costs across paydays`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.INVEST_WITH_DISCIPLINE,
            netIncomePerPayday = BigDecimal("900"),
            monthlyFixedCosts = BigDecimal("780"),
            payFrequency = PayFrequency.BIWEEKLY,
            nextBiweeklyPayday = java.time.LocalDate.now().plusDays(14),
            preset = AllocationPreset.GROWTH
        )

        val preview = useCase.execute(plan, BigDecimal("10000"))

        assertEquals(BigDecimal("360.00"), preview.fixedCostsPerPayday)
        assertTrue(preview.weeklyFlexibleSpend > BigDecimal.ZERO)
        assertTrue(preview.goalTargetDate != null)
    }

    @Test
    fun `no savings strategy creates plan without savings contribution`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.BALANCED,
            strategy = AllocationStrategy.NO_SAVINGS_NOW
        )

        val preview = useCase.execute(plan, BigDecimal("3000"))

        assertEquals(BigDecimal("0.00"), preview.savingsPerPayday)
        assertTrue(preview.flexibleSpendPerPayday > BigDecimal.ZERO)
        assertTrue(preview.monthlyGoalContribution > BigDecimal.ZERO)
    }

    @Test
    fun `flexible only strategy keeps all available income as flexible spend`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.STOP_OVERSPENDING,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.BALANCED,
            strategy = AllocationStrategy.FLEXIBLE_BUDGET_ONLY
        )

        val preview = useCase.execute(plan, BigDecimal("3000"))

        assertEquals(BigDecimal("600.00"), preview.flexibleSpendPerPayday)
        assertEquals(BigDecimal("0.00"), preview.savingsPerPayday)
        assertEquals(BigDecimal("0.00"), preview.investingPerPayday)
        assertEquals(BigDecimal("0.00"), preview.debtPerPayday)
        assertEquals(BigDecimal("0.00"), preview.monthlyGoalContribution)
    }

    @Test
    fun `custom strategy starts with flexible allocation and no forced contribution`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.PLAN_TOGETHER,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("250"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.GROWTH,
            strategy = AllocationStrategy.CUSTOM,
            customStrategyName = "Travel reset"
        )

        val preview = useCase.execute(plan, BigDecimal("3000"))

        assertEquals(BigDecimal("750.00"), preview.flexibleSpendPerPayday)
        assertEquals(BigDecimal("0.00"), preview.priorityContributionPerPayday)
        assertEquals("plan_summary_custom_strategy", preview.summary)
    }
}
