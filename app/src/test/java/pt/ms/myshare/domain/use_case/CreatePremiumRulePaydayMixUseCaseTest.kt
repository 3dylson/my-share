package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal

class CreatePremiumRulePaydayMixUseCaseTest {

    private val useCase = CreatePremiumRulePaydayMixUseCase()

    @Test
    fun `builds payday mix from percentage and fixed priority rules`() {
        val mix = useCase.execute(
            plan = plan(
                rules = listOf(
                    rule(id = "savings", amount = BigDecimal("10"), isPercentage = true, type = PaydayRuleType.SAVINGS),
                    rule(id = "investing", amount = BigDecimal("5"), isPercentage = true, type = PaydayRuleType.INVESTING),
                    rule(id = "debt", amount = BigDecimal("100"), isPercentage = false, type = PaydayRuleType.DEBT)
                )
            )
        )

        assertEquals(BigDecimal("280.00"), mix?.totalMove)
        assertEquals(BigDecimal("120.00"), mix?.items?.get(0)?.amount)
        assertEquals(BigDecimal("42.86"), mix?.items?.get(0)?.sharePercent)
        assertEquals(BigDecimal("60.00"), mix?.items?.get(1)?.amount)
        assertEquals(BigDecimal("21.43"), mix?.items?.get(1)?.sharePercent)
        assertEquals(BigDecimal("100.00"), mix?.items?.get(2)?.amount)
        assertEquals(BigDecimal("35.71"), mix?.items?.get(2)?.sharePercent)
    }

    @Test
    fun `ignores other and zero amount rules`() {
        val mix = useCase.execute(
            plan = plan(
                rules = listOf(
                    rule(id = "savings", amount = BigDecimal("10"), isPercentage = true, type = PaydayRuleType.SAVINGS),
                    rule(id = "ignored", amount = BigDecimal("20"), isPercentage = true, type = PaydayRuleType.OTHER),
                    rule(id = "zero", amount = BigDecimal.ZERO, isPercentage = false, type = PaydayRuleType.DEBT),
                    rule(id = "investing", amount = BigDecimal("5"), isPercentage = true, type = PaydayRuleType.INVESTING)
                )
            )
        )

        assertEquals(2, mix?.items?.size)
        assertEquals(BigDecimal("180.00"), mix?.totalMove)
        assertEquals("savings", mix?.items?.get(0)?.ruleId)
        assertEquals("investing", mix?.items?.get(1)?.ruleId)
    }

    @Test
    fun `uses fixed costs per payday for biweekly plans`() {
        val mix = useCase.execute(
            plan = plan(
                monthlyFixedCosts = BigDecimal("1300"),
                payFrequency = PayFrequency.BIWEEKLY,
                rules = listOf(
                    rule(id = "savings", amount = BigDecimal("10"), isPercentage = true, type = PaydayRuleType.SAVINGS),
                    rule(id = "investing", amount = BigDecimal("5"), isPercentage = true, type = PaydayRuleType.INVESTING)
                )
            )
        )

        assertEquals(BigDecimal("210.00"), mix?.totalMove)
        assertEquals(BigDecimal("140.00"), mix?.items?.get(0)?.amount)
        assertEquals(BigDecimal("70.00"), mix?.items?.get(1)?.amount)
    }

    @Test
    fun `does not create mix without multiple priority rules`() {
        assertNull(
            useCase.execute(
                plan = plan(
                    rules = listOf(
                        rule(id = "savings", amount = BigDecimal("10"), isPercentage = true, type = PaydayRuleType.SAVINGS)
                    )
                )
            )
        )
        assertNull(
            useCase.execute(
                plan = plan(
                    rules = listOf(
                        rule(id = "ignored", amount = BigDecimal("10"), isPercentage = true, type = PaydayRuleType.OTHER),
                        rule(id = "zero", amount = BigDecimal.ZERO, isPercentage = false, type = PaydayRuleType.DEBT)
                    )
                )
            )
        )
    }

    private fun plan(
        monthlyFixedCosts: BigDecimal = BigDecimal("800"),
        payFrequency: PayFrequency = PayFrequency.MONTHLY,
        rules: List<PaydayRule>
    ): SalaryPlan {
        return SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("2000"),
            monthlyFixedCosts = monthlyFixedCosts,
            payFrequency = payFrequency,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED,
            rules = rules
        )
    }

    private fun rule(
        id: String,
        amount: BigDecimal,
        isPercentage: Boolean,
        type: PaydayRuleType
    ): PaydayRule {
        return PaydayRule(
            id = id,
            name = id,
            amount = amount,
            isPercentage = isPercentage,
            type = type
        )
    }
}
