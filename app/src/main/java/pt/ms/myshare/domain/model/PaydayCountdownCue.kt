package pt.ms.myshare.domain.model

import java.time.LocalDate

enum class PaydayCountdownAction {
    FINISH_SETUP,
    KEEP_GUIDE,
    REVIEW_PAYDAY
}

data class PaydayCountdownCue(
    val nextPayday: LocalDate,
    val daysUntilPayday: Long,
    val action: PaydayCountdownAction
)
