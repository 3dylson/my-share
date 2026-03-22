package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal

class CalculatePlanPreviewUseCaseTest {

    private val useCase = CalculatePlanPreviewUseCase()

    @Test
    fun `monthly plan keeps monthly fixed costs on the payday`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.BALANCED,
            goalName = "Emergency fund",
            goalAmount = BigDecimal("3000")
        )

        val preview = useCase.execute(plan)

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
            preset = AllocationPreset.GROWTH,
            goalName = "Investing base",
            goalAmount = BigDecimal("10000")
        )

        val preview = useCase.execute(plan)

        assertEquals(BigDecimal("360.00"), preview.fixedCostsPerPayday)
        assertTrue(preview.weeklyFlexibleSpend > BigDecimal.ZERO)
        assertTrue(preview.goalTargetDate != null)
    }
}
