package pt.ms.myshare.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoalType
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import java.math.BigDecimal
import java.time.LocalDate

class PlannerCollectionPreferenceCodecTest {

    @Test
    fun `goals round trip with special characters`() {
        val goals = listOf(
            Goal(
                id = "goal|1",
                name = "Emergency\nFund & buffer",
                targetAmount = BigDecimal("3000.50"),
                currentProgress = BigDecimal("125.25"),
                type = GoalType.EMERGENCY_FUND,
                createdAt = LocalDate.of(2026, 5, 21),
                isCompleted = false
            )
        )

        val decoded = PlannerCollectionPreferenceCodec.decodeGoals(
            PlannerCollectionPreferenceCodec.encodeGoals(goals)
        )

        assertEquals(goals, decoded)
    }

    @Test
    fun `rules round trip preserves allocation settings`() {
        val rules = listOf(
            PaydayRule(
                id = "rule-1",
                name = "Savings transfer",
                amount = BigDecimal("35.5"),
                isPercentage = true,
                type = PaydayRuleType.SAVINGS,
                createdAt = LocalDate.of(2026, 6, 1)
            )
        )

        val decoded = PlannerCollectionPreferenceCodec.decodeRules(
            PlannerCollectionPreferenceCodec.encodeRules(rules)
        )

        assertEquals(rules, decoded)
    }

    @Test
    fun `reviews round trip preserves plan snapshots`() {
        val reviews = listOf(
            ManualReview(
                id = "review-1",
                actualFlexibleSpend = BigDecimal("850.00"),
                actualGoalContribution = BigDecimal("950.00"),
                plannedFlexibleSpend = BigDecimal("900.00"),
                plannedGoalContribution = BigDecimal("900.00"),
                createdAt = LocalDate.of(2026, 5, 21),
                paydayDate = LocalDate.of(2026, 5, 1)
            )
        )

        val decoded = PlannerCollectionPreferenceCodec.decodeReviews(
            PlannerCollectionPreferenceCodec.encodeReviews(reviews)
        )

        assertEquals(reviews, decoded)
    }

    @Test
    fun `invalid records are skipped during restore`() {
        val decoded = PlannerCollectionPreferenceCodec.decodeGoals("bad|not-a-number")

        assertEquals(emptyList<Goal>(), decoded)
    }
}
