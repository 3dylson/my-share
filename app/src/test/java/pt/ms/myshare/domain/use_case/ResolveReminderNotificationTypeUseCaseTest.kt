package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.time.LocalDate

class ResolveReminderNotificationTypeUseCaseTest {

    private val useCase = ResolveReminderNotificationTypeUseCase(CreatePremiumCheckInPlanUseCase())

    @Test
    fun `payday without same-day review asks for review`() {
        val type = useCase.execute(
            plan = monthlyPlan(),
            latestReview = ManualReview(
                actualFlexibleSpend = BigDecimal.ZERO,
                actualGoalContribution = BigDecimal.ZERO,
                createdAt = LocalDate.of(2026, 5, 1)
            ),
            reminderConfiguration = ReminderConfiguration(enabled = true, cadence = ReminderCadence.PAYDAY),
            automationEnabled = false,
            today = LocalDate.of(2026, 5, 28)
        )

        assertEquals(ReminderNotificationType.PAYDAY_REVIEW_DUE, type)
    }

    @Test
    fun `payday with same-day review gives action reminder`() {
        val today = LocalDate.of(2026, 5, 28)

        val type = useCase.execute(
            plan = monthlyPlan(),
            latestReview = ManualReview(
                actualFlexibleSpend = BigDecimal.ZERO,
                actualGoalContribution = BigDecimal.ZERO,
                createdAt = today
            ),
            reminderConfiguration = ReminderConfiguration(enabled = true, cadence = ReminderCadence.PAYDAY),
            automationEnabled = false,
            today = today
        )

        assertEquals(ReminderNotificationType.PAYDAY_ACTION, type)
    }

    @Test
    fun `weekly review only fires on Sunday`() {
        val type = useCase.execute(
            plan = monthlyPlan(),
            latestReview = null,
            reminderConfiguration = ReminderConfiguration(enabled = true, cadence = ReminderCadence.WEEKLY_REVIEW),
            automationEnabled = false,
            today = LocalDate.of(2026, 5, 31)
        )

        assertEquals(ReminderNotificationType.WEEKLY_REVIEW, type)
    }

    @Test
    fun `non-payday weekday does not fire`() {
        val type = useCase.execute(
            plan = monthlyPlan(),
            latestReview = null,
            reminderConfiguration = ReminderConfiguration(enabled = true, cadence = ReminderCadence.PAYDAY),
            automationEnabled = false,
            today = LocalDate.of(2026, 5, 27)
        )

        assertNull(type)
    }

    private fun monthlyPlan(): SalaryPlan = SalaryPlan(
        focus = PlanningFocus.SAVE_WITHOUT_STRESS,
        netIncomePerPayday = BigDecimal("1500"),
        monthlyFixedCosts = BigDecimal("600"),
        payFrequency = PayFrequency.MONTHLY,
        monthlyPayday = 28,
        preset = AllocationPreset.BALANCED,
        createdAt = LocalDate.of(2026, 1, 1)
    )
}
