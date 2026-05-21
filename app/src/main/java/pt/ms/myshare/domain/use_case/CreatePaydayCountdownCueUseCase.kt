package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PaydayCountdownAction
import pt.ms.myshare.domain.model.PaydayCountdownCue
import pt.ms.myshare.domain.model.PaydayReadiness
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.domain.model.PlanPreview
import timber.log.Timber
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CreatePaydayCountdownCueUseCase @Inject constructor() {

    fun execute(
        preview: PlanPreview,
        readiness: PaydayReadiness,
        latestReview: ManualReview?,
        today: LocalDate = LocalDate.now()
    ): PaydayCountdownCue {
        val daysUntilPayday = ChronoUnit.DAYS.between(today, preview.nextPayday).coerceAtLeast(0)
        val reviewedPaydayDate = latestReview?.paydayDate ?: latestReview?.createdAt
        val currentPaydayNeedsReview = daysUntilPayday == 0L && reviewedPaydayDate != preview.nextPayday
        val action = when {
            currentPaydayNeedsReview -> PaydayCountdownAction.REVIEW_PAYDAY
            readiness.status != PaydayReadinessStatus.READY -> PaydayCountdownAction.FINISH_SETUP
            else -> PaydayCountdownAction.KEEP_GUIDE
        }

        Timber.tag(TAG).d(
            "Payday countdown cue computed. nextPayday=%s daysUntil=%d action=%s reviewedPaydayDate=%s",
            preview.nextPayday,
            daysUntilPayday,
            action,
            reviewedPaydayDate
        )

        return PaydayCountdownCue(
            nextPayday = preview.nextPayday,
            daysUntilPayday = daysUntilPayday,
            action = action
        )
    }

    private companion object {
        const val TAG = "PaydayCountdownCue"
    }
}
