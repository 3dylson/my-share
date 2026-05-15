package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal

class CreateReviewInsightUseCaseTest {

    private val reviewInsightUseCase = CreateReviewInsightUseCase(CalculatePlanPreviewUseCase())

    @Test
    fun `returns on track when spend is below plan and contributions are above plan`() {
        val plan = SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = 5,
            preset = AllocationPreset.BALANCED
        )
        val review = ManualReview(
            actualFlexibleSpend = BigDecimal("250"),
            actualGoalContribution = BigDecimal("330")
        )

        val insight = reviewInsightUseCase.execute(plan, review)

        assertEquals("insight_review_save_stress_success_headline", insight.headline)
        assertEquals(BigDecimal("-50.00"), insight.flexibleSpendDelta)
        assertEquals(BigDecimal("30.00"), insight.goalContributionDelta)
    }
}
