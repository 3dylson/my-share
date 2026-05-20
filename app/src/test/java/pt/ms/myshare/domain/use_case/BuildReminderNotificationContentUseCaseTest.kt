package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.util.Locale

class BuildReminderNotificationContentUseCaseTest {

    private val useCase = BuildReminderNotificationContentUseCase(
        CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase())
    )

    @Test
    fun `payday notification opens plan with concrete money moves`() {
        val content = useCase.execute(
            plan = plan(),
            type = ReminderNotificationType.PAYDAY_ACTION,
            locale = Locale.US,
            currencyCode = "USD"
        )

        assertEquals("notification_payday_title", content.titleKey)
        assertEquals("notification_payday_body", content.messageKey)
        assertEquals("plan", content.destination)
        assertEquals("payday_action", content.analyticsType)
        assertEquals(listOf("$600.00", "$450.00", "$450.00"), content.messageArgs)
    }

    @Test
    fun `weekly review notification opens review without payday execution copy`() {
        val content = useCase.execute(
            plan = plan(),
            type = ReminderNotificationType.WEEKLY_REVIEW,
            locale = Locale.US,
            currencyCode = "USD"
        )

        assertEquals("notification_weekly_review_title", content.titleKey)
        assertEquals("notification_weekly_review_body", content.messageKey)
        assertEquals("review", content.destination)
        assertEquals("weekly_review", content.analyticsType)
        assertEquals(listOf("$103.85"), content.messageArgs)
    }

    @Test
    fun `premium check-in notification opens review with premium proof points`() {
        val content = useCase.execute(
            plan = plan(),
            type = ReminderNotificationType.PREMIUM_CHECK_IN_DUE,
            locale = Locale.US,
            currencyCode = "USD"
        )

        assertEquals("notification_premium_checkin_due_title", content.titleKey)
        assertEquals("notification_premium_checkin_due_body", content.messageKey)
        assertEquals("review", content.destination)
        assertEquals("premium_checkin_due", content.analyticsType)
        assertEquals(listOf("$103.85", "$450.00"), content.messageArgs)
    }

    private fun plan(): SalaryPlan = SalaryPlan(
        focus = PlanningFocus.SAVE_WITHOUT_STRESS,
        netIncomePerPayday = BigDecimal("1500"),
        monthlyFixedCosts = BigDecimal("600"),
        payFrequency = PayFrequency.MONTHLY,
        monthlyPayday = 1,
        preset = AllocationPreset.BALANCED
    )
}
