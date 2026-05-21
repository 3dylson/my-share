package pt.ms.myshare.presentation.ui.paywall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import java.math.BigDecimal
import java.time.LocalDate

class PaywallAutopilotPreviewMapperTest {

    @Test
    fun `map formats personalized plan amounts`() {
        val state = PaywallAutopilotPreviewMapper.map(
            preview = preview(
                weeklyFlexibleSpend = BigDecimal("125.50"),
                priorityContributionPerPayday = BigDecimal("80.00")
            ),
            userPreferences = UserPreferences(languageTag = "en-US", currencyCode = "USD")
        )

        assertTrue(state.hasPersonalPlan)
        assertEquals("$125.50", state.weeklyFlexibleSpend)
        assertEquals("$80.00", state.priorityContribution)
        assertEquals("$30.00", state.suggestedAdjustmentAmount)
    }

    @Test
    fun `map hides priority amount when no priority move exists`() {
        val state = PaywallAutopilotPreviewMapper.map(
            preview = preview(
                weeklyFlexibleSpend = BigDecimal("200.00"),
                priorityContributionPerPayday = BigDecimal.ZERO
            ),
            userPreferences = UserPreferences(languageTag = "en-US", currencyCode = "USD")
        )

        assertTrue(state.hasPersonalPlan)
        assertEquals("$200.00", state.weeklyFlexibleSpend)
        assertNull(state.priorityContribution)
        assertNull(state.suggestedAdjustmentAmount)
    }

    @Test
    fun `map returns generic state without plan preview`() {
        val state = PaywallAutopilotPreviewMapper.map(
            preview = null,
            userPreferences = UserPreferences(languageTag = "en-US", currencyCode = "USD")
        )

        assertFalse(state.hasPersonalPlan)
        assertNull(state.weeklyFlexibleSpend)
        assertNull(state.priorityContribution)
        assertNull(state.suggestedAdjustmentAmount)
    }

    @Test
    fun `map rounds recommendation preview to a practical buffer amount`() {
        val state = PaywallAutopilotPreviewMapper.map(
            preview = preview(
                weeklyFlexibleSpend = BigDecimal("207.69"),
                priorityContributionPerPayday = BigDecimal("900.00")
            ),
            userPreferences = UserPreferences(languageTag = "en-US", currencyCode = "USD")
        )

        assertEquals("$50.00", state.suggestedAdjustmentAmount)
    }

    private fun preview(
        weeklyFlexibleSpend: BigDecimal,
        priorityContributionPerPayday: BigDecimal
    ): PlanPreview {
        return PlanPreview(
            incomePerPayday = BigDecimal("3000.00"),
            fixedCostsPerPayday = BigDecimal("1200.00"),
            flexibleSpendPerPayday = BigDecimal("1000.00"),
            savingsPerPayday = priorityContributionPerPayday,
            investingPerPayday = BigDecimal.ZERO,
            cryptoPerPayday = BigDecimal.ZERO,
            debtPerPayday = BigDecimal.ZERO,
            priorityContributionPerPayday = priorityContributionPerPayday,
            weeklyFlexibleSpend = weeklyFlexibleSpend,
            monthlyGoalContribution = priorityContributionPerPayday,
            nextPayday = LocalDate.of(2026, 5, 29),
            goalTargetDate = null,
            summary = "Test preview"
        )
    }
}
