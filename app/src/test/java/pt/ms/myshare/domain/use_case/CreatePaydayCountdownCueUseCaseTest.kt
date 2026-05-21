package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayCountdownAction
import pt.ms.myshare.domain.model.PaydayReadiness
import pt.ms.myshare.domain.model.PaydayReadinessMission
import pt.ms.myshare.domain.model.PaydayReadinessMissionState
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.domain.model.PlanPreview
import java.math.BigDecimal
import java.time.LocalDate

class CreatePaydayCountdownCueUseCaseTest {

    private val useCase = CreatePaydayCountdownCueUseCase()

    @Test
    fun `execute asks for review on payday when current payday has not been reviewed`() {
        val cue = useCase.execute(
            preview = preview(nextPayday = LocalDate.of(2026, 6, 1)),
            readiness = readiness(PaydayReadinessStatus.READY),
            latestReview = null,
            today = LocalDate.of(2026, 6, 1)
        )

        assertEquals(0L, cue.daysUntilPayday)
        assertEquals(PaydayCountdownAction.REVIEW_PAYDAY, cue.action)
    }

    @Test
    fun `execute keeps guide when payday is ready and still ahead`() {
        val cue = useCase.execute(
            preview = preview(nextPayday = LocalDate.of(2026, 6, 6)),
            readiness = readiness(PaydayReadinessStatus.READY),
            latestReview = null,
            today = LocalDate.of(2026, 6, 1)
        )

        assertEquals(5L, cue.daysUntilPayday)
        assertEquals(PaydayCountdownAction.KEEP_GUIDE, cue.action)
    }

    @Test
    fun `execute asks to finish setup before payday when readiness is incomplete`() {
        val cue = useCase.execute(
            preview = preview(nextPayday = LocalDate.of(2026, 6, 6)),
            readiness = readiness(PaydayReadinessStatus.ALMOST_READY),
            latestReview = null,
            today = LocalDate.of(2026, 6, 1)
        )

        assertEquals(PaydayCountdownAction.FINISH_SETUP, cue.action)
    }

    @Test
    fun `execute does not ask for review when current payday already has a review`() {
        val payday = LocalDate.of(2026, 6, 1)
        val cue = useCase.execute(
            preview = preview(nextPayday = payday),
            readiness = readiness(PaydayReadinessStatus.READY),
            latestReview = ManualReview(
                actualFlexibleSpend = BigDecimal("100"),
                actualGoalContribution = BigDecimal("50"),
                createdAt = payday,
                paydayDate = payday
            ),
            today = payday
        )

        assertEquals(PaydayCountdownAction.KEEP_GUIDE, cue.action)
    }

    private fun readiness(status: PaydayReadinessStatus): PaydayReadiness {
        return PaydayReadiness(
            status = status,
            progress = if (status == PaydayReadinessStatus.READY) 1f else 0.67f,
            completedMissions = if (status == PaydayReadinessStatus.READY) 3 else 2,
            totalMissions = 3,
            nextAction = if (status == PaydayReadinessStatus.READY) null else PaydayReadinessMission.SET_PRIORITY_MOVE,
            missions = listOf(
                PaydayReadinessMissionState(PaydayReadinessMission.PROTECT_BILLS, true),
                PaydayReadinessMissionState(PaydayReadinessMission.SET_WEEKLY_GUIDE, true),
                PaydayReadinessMissionState(
                    PaydayReadinessMission.SET_PRIORITY_MOVE,
                    status == PaydayReadinessStatus.READY
                )
            )
        )
    }

    private fun preview(nextPayday: LocalDate): PlanPreview {
        return PlanPreview(
            incomePerPayday = BigDecimal("1000"),
            fixedCostsPerPayday = BigDecimal("400"),
            flexibleSpendPerPayday = BigDecimal("450"),
            savingsPerPayday = BigDecimal("150"),
            investingPerPayday = BigDecimal.ZERO,
            cryptoPerPayday = BigDecimal.ZERO,
            debtPerPayday = BigDecimal.ZERO,
            priorityContributionPerPayday = BigDecimal("150"),
            weeklyFlexibleSpend = BigDecimal("100"),
            monthlyGoalContribution = BigDecimal("150"),
            nextPayday = nextPayday,
            goalTargetDate = null,
            summary = "plan_summary_save_without_stress"
        )
    }
}
