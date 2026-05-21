package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import java.math.BigDecimal
import java.time.LocalDate

class CreatePremiumCheckInPlanUseCaseTest {

    private val useCase = CreatePremiumCheckInPlanUseCase()

    @Test
    fun `marks payday as ready when current payday has no review`() {
        val today = LocalDate.of(2026, 5, 19)
        val plan = biweeklyPlan(anchor = today, createdAt = today.minusMonths(1))

        val checkIn = useCase.execute(
            plan = plan,
            latestReview = null,
            reminderConfiguration = ReminderConfiguration(enabled = true, cadence = ReminderCadence.PAYDAY),
            automationEnabled = true,
            today = today
        )

        assertEquals(PremiumCheckInStatus.READY_NOW, checkIn.status)
        assertEquals(today, checkIn.checkInDate)
        assertTrue(checkIn.isDue)
        assertTrue(checkIn.reminderEnabled)
        assertTrue(checkIn.automationEnabled)
    }

    @Test
    fun `schedules next payday after current payday is reviewed`() {
        val today = LocalDate.of(2026, 5, 19)
        val plan = biweeklyPlan(anchor = today, createdAt = today.minusMonths(1))
        val review = ManualReview(
            actualFlexibleSpend = BigDecimal("100"),
            actualGoalContribution = BigDecimal("50"),
            createdAt = today
        )

        val checkIn = useCase.execute(
            plan = plan,
            latestReview = review,
            reminderConfiguration = ReminderConfiguration(enabled = false),
            automationEnabled = true,
            today = today
        )

        assertEquals(PremiumCheckInStatus.REVIEWED, checkIn.status)
        assertEquals(today.plusDays(14), checkIn.checkInDate)
        assertFalse(checkIn.isDue)
    }

    @Test
    fun `does not create overdue check in before first user payday`() {
        val today = LocalDate.of(2026, 5, 19)
        val plan = monthlyPlan(
            payday = 10,
            createdAt = today
        )

        val checkIn = useCase.execute(
            plan = plan,
            latestReview = null,
            reminderConfiguration = ReminderConfiguration(enabled = false),
            automationEnabled = false,
            today = today
        )

        assertEquals(PremiumCheckInStatus.SCHEDULED, checkIn.status)
        assertEquals(LocalDate.of(2026, 6, 10), checkIn.checkInDate)
        assertFalse(checkIn.isDue)
    }

    @Test
    fun `marks missed payday as overdue when plan predates payday`() {
        val today = LocalDate.of(2026, 5, 19)
        val plan = monthlyPlan(
            payday = 10,
            createdAt = LocalDate.of(2026, 4, 1)
        )

        val checkIn = useCase.execute(
            plan = plan,
            latestReview = null,
            reminderConfiguration = ReminderConfiguration(enabled = true),
            automationEnabled = true,
            today = today
        )

        assertEquals(PremiumCheckInStatus.OVERDUE, checkIn.status)
        assertEquals(LocalDate.of(2026, 5, 10), checkIn.checkInDate)
        assertTrue(checkIn.isDue)
    }

    private fun biweeklyPlan(anchor: LocalDate, createdAt: LocalDate): SalaryPlan {
        return SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.BIWEEKLY,
            nextBiweeklyPayday = anchor,
            preset = AllocationPreset.BALANCED,
            createdAt = createdAt
        )
    }

    private fun monthlyPlan(payday: Int, createdAt: LocalDate): SalaryPlan {
        return SalaryPlan(
            focus = PlanningFocus.SAVE_WITHOUT_STRESS,
            netIncomePerPayday = BigDecimal("1000"),
            monthlyFixedCosts = BigDecimal("400"),
            payFrequency = PayFrequency.MONTHLY,
            monthlyPayday = payday,
            preset = AllocationPreset.BALANCED,
            createdAt = createdAt
        )
    }
}
