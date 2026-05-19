package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PremiumReviewCoachingMetricType
import pt.ms.myshare.domain.model.PremiumReviewCoachingStatus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.time.LocalDate

class CreatePremiumReviewCoachingSummaryUseCaseTest {

    private val useCase = CreatePremiumReviewCoachingSummaryUseCase(
        CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase())
    )

    @Test
    fun `returns null until there are enough reviews`() {
        val summary = useCase.execute(
            plan = plan(),
            history = listOf(
                review(
                    actualFlexibleSpend = "260",
                    actualGoalContribution = "120",
                    createdAt = LocalDate.of(2026, 5, 19)
                )
            )
        )

        assertNull(summary)
    }

    @Test
    fun `summarizes strong review pattern from recent snapshots`() {
        val summary = useCase.execute(
            plan = plan(),
            history = listOf(
                review("290", "115", createdAt = LocalDate.of(2026, 5, 3)),
                review("280", "120", createdAt = LocalDate.of(2026, 5, 17)),
                review("260", "110", createdAt = LocalDate.of(2026, 5, 19))
            )
        )

        assertEquals(PremiumReviewCoachingStatus.STRONG, summary?.status)
        assertEquals(3, summary?.reviewedPaydays)
        assertEquals("home_review_coaching_summary_headline_strong", summary?.headlineKey)
        assertEquals(BigDecimal("23.33"), summary?.metrics?.first()?.value)
        assertEquals(PremiumReviewCoachingMetricType.PERCENT, summary?.metrics?.last()?.valueType)
        assertEquals(BigDecimal("100"), summary?.metrics?.last()?.value)
    }

    @Test
    fun `marks pattern steady when some paydays miss the plan`() {
        val summary = useCase.execute(
            plan = plan(),
            history = listOf(
                review("330", "80", createdAt = LocalDate.of(2026, 5, 3)),
                review("280", "120", createdAt = LocalDate.of(2026, 5, 17)),
                review("260", "110", createdAt = LocalDate.of(2026, 5, 19))
            )
        )

        assertEquals(PremiumReviewCoachingStatus.STEADY, summary?.status)
        assertEquals(BigDecimal("67"), summary?.metrics?.last()?.value)
        assertTrue(summary?.metrics?.last()?.isPositive == true)
    }

    @Test
    fun `marks pattern as needing attention when recent paydays keep missing`() {
        val summary = useCase.execute(
            plan = plan(),
            history = listOf(
                review("340", "80", createdAt = LocalDate.of(2026, 5, 3)),
                review("330", "90", createdAt = LocalDate.of(2026, 5, 17)),
                review("320", "95", createdAt = LocalDate.of(2026, 5, 19))
            )
        )

        assertEquals(PremiumReviewCoachingStatus.NEEDS_ATTENTION, summary?.status)
        assertEquals("home_review_coaching_summary_headline_attention", summary?.headlineKey)
        assertEquals(BigDecimal("0"), summary?.metrics?.last()?.value)
    }

    private fun plan(): SalaryPlan {
        return SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 1,
            preset = AllocationPreset.BALANCED
        )
    }

    private fun review(
        actualFlexibleSpend: String,
        actualGoalContribution: String,
        createdAt: LocalDate
    ): ManualReview {
        return ManualReview(
            actualFlexibleSpend = BigDecimal(actualFlexibleSpend),
            actualGoalContribution = BigDecimal(actualGoalContribution),
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("100"),
            createdAt = createdAt
        )
    }
}
