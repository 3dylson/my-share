package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.time.LocalDate

class CreatePaydayAdjustmentRecommendationUseCaseTest {

    private val resolveRulesUseCase = ResolveAllocationStrategyRulesUseCase()
    private val useCase = CreatePaydayAdjustmentRecommendationUseCase(
        calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(resolveRulesUseCase),
        resolveAllocationStrategyRulesUseCase = resolveRulesUseCase
    )

    @Test
    fun `execute recommends moving repeat flexible surplus into priority rules`() {
        val recommendation = useCase.execute(
            plan = plan(),
            history = listOf(
                review(actualFlexible = "220", actualGoal = "310", daysAgo = 0),
                review(actualFlexible = "230", actualGoal = "305", daysAgo = 7),
                review(actualFlexible = "240", actualGoal = "300", daysAgo = 14)
            )
        )

        requireNotNull(recommendation)
        assertEquals(PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY, recommendation.direction)
        assertEquals(BigDecimal("300.00"), recommendation.currentPriorityContribution)
        assertEquals(BigDecimal("335.00"), recommendation.recommendedPriorityContribution)
        assertEquals(BigDecimal("265.00"), recommendation.recommendedFlexibleSpend)
        assertTrue(recommendation.isApplyable)
        assertTrue(recommendation.suggestedRules.isNotEmpty())
    }

    @Test
    fun `execute recommends restoring flexible buffer when reviews miss the guide`() {
        val recommendation = useCase.execute(
            plan = plan(),
            history = listOf(
                review(actualFlexible = "360", actualGoal = "250", daysAgo = 0),
                review(actualFlexible = "340", actualGoal = "260", daysAgo = 7)
            )
        )

        requireNotNull(recommendation)
        assertEquals(PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER, recommendation.direction)
        assertEquals(BigDecimal("250.00"), recommendation.recommendedPriorityContribution)
        assertEquals(BigDecimal("350.00"), recommendation.recommendedFlexibleSpend)
        assertTrue(recommendation.isApplyable)
    }

    @Test
    fun `execute keeps plan when latest reviews are close to target`() {
        val recommendation = useCase.execute(
            plan = plan(),
            history = listOf(
                review(actualFlexible = "298", actualGoal = "302")
            )
        )

        requireNotNull(recommendation)
        assertEquals(PaydayAdjustmentRecommendationDirection.KEEP_PLAN, recommendation.direction)
        assertEquals(BigDecimal("300.00"), recommendation.recommendedPriorityContribution)
        assertEquals(BigDecimal("300.00"), recommendation.recommendedFlexibleSpend)
        assertTrue(recommendation.suggestedRules.isEmpty())
    }

    @Test
    fun `execute keeps plan when current rules already applied the reviewed adjustment`() {
        val recommendation = useCase.execute(
            plan = plan().copy(
                rules = listOf(
                    PaydayRule(
                        name = "Savings",
                        amount = BigDecimal("39.08"),
                        isPercentage = true,
                        type = PaydayRuleType.SAVINGS
                    ),
                    PaydayRule(
                        name = "Investing",
                        amount = BigDecimal("16.75"),
                        isPercentage = true,
                        type = PaydayRuleType.INVESTING
                    )
                )
            ),
            history = listOf(
                review(actualFlexible = "220", actualGoal = "310", daysAgo = 0),
                review(actualFlexible = "230", actualGoal = "305", daysAgo = 7),
                review(actualFlexible = "240", actualGoal = "300", daysAgo = 14)
            )
        )

        requireNotNull(recommendation)
        assertEquals(PaydayAdjustmentRecommendationDirection.KEEP_PLAN, recommendation.direction)
        assertTrue(recommendation.suggestedRules.isEmpty())
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
        actualFlexible: String,
        actualGoal: String,
        daysAgo: Long = 0
    ): ManualReview {
        return ManualReview(
            actualFlexibleSpend = BigDecimal(actualFlexible),
            actualGoalContribution = BigDecimal(actualGoal),
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("300"),
            createdAt = LocalDate.now().minusDays(daysAgo)
        )
    }
}
