package pt.ms.myshare.domain.model

import java.time.LocalDate

enum class PremiumCheckInStatus {
    READY_NOW,
    OVERDUE,
    SCHEDULED,
    REVIEWED
}

data class PremiumCheckInPlan(
    val status: PremiumCheckInStatus,
    val checkInDate: LocalDate,
    val latestReviewDate: LocalDate?,
    val reminderEnabled: Boolean,
    val automationEnabled: Boolean
) {
    val isDue: Boolean
        get() = status == PremiumCheckInStatus.READY_NOW || status == PremiumCheckInStatus.OVERDUE
}
