package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.PaydayReadinessMission
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.domain.model.PlanPreview
import java.math.BigDecimal
import java.time.LocalDate

class CreatePaydayReadinessUseCaseTest {

    private val useCase = CreatePaydayReadinessUseCase()

    @Test
    fun `execute marks plan ready when bills weekly guide and priority move are set`() {
        val readiness = useCase.execute(
            preview(
                fixedCosts = BigDecimal("400"),
                weeklyFlexibleSpend = BigDecimal("80"),
                priorityContribution = BigDecimal("150")
            )
        )

        assertEquals(PaydayReadinessStatus.READY, readiness.status)
        assertEquals(3, readiness.completedMissions)
        assertEquals(1f, readiness.progress)
        assertEquals(null, readiness.nextAction)
    }

    @Test
    fun `execute points to priority move when plan has no future contribution`() {
        val readiness = useCase.execute(
            preview(
                fixedCosts = BigDecimal("400"),
                weeklyFlexibleSpend = BigDecimal("120"),
                priorityContribution = BigDecimal.ZERO
            )
        )

        assertEquals(PaydayReadinessStatus.ALMOST_READY, readiness.status)
        assertEquals(2, readiness.completedMissions)
        assertEquals(PaydayReadinessMission.SET_PRIORITY_MOVE, readiness.nextAction)
    }

    @Test
    fun `execute flags attention when fixed costs consume the payday`() {
        val readiness = useCase.execute(
            preview(
                income = BigDecimal("500"),
                fixedCosts = BigDecimal("700"),
                weeklyFlexibleSpend = BigDecimal.ZERO,
                priorityContribution = BigDecimal.ZERO
            )
        )

        assertEquals(PaydayReadinessStatus.NEEDS_ATTENTION, readiness.status)
        assertEquals(PaydayReadinessMission.PROTECT_BILLS, readiness.nextAction)
    }

    private fun preview(
        income: BigDecimal = BigDecimal("1000"),
        fixedCosts: BigDecimal,
        weeklyFlexibleSpend: BigDecimal,
        priorityContribution: BigDecimal
    ): PlanPreview {
        return PlanPreview(
            incomePerPayday = income,
            fixedCostsPerPayday = fixedCosts,
            flexibleSpendPerPayday = weeklyFlexibleSpend.multiply(BigDecimal("4")),
            savingsPerPayday = priorityContribution,
            investingPerPayday = BigDecimal.ZERO,
            cryptoPerPayday = BigDecimal.ZERO,
            debtPerPayday = BigDecimal.ZERO,
            priorityContributionPerPayday = priorityContribution,
            weeklyFlexibleSpend = weeklyFlexibleSpend,
            monthlyGoalContribution = priorityContribution,
            nextPayday = LocalDate.of(2026, 6, 1),
            goalTargetDate = null,
            summary = "plan_summary_save_without_stress"
        )
    }
}
