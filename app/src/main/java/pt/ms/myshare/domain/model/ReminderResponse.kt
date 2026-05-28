package pt.ms.myshare.domain.model

import java.time.LocalDateTime

data class ReminderResponse(
    val notificationType: String,
    val response: String,
    val respondedAt: LocalDateTime = LocalDateTime.now()
)
