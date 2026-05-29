package pt.ms.myshare.domain.repository

import pt.ms.myshare.domain.model.ReminderResponse

interface ReminderResponseRepository {
    fun saveResponse(response: ReminderResponse)
    fun loadLatestResponse(): ReminderResponse?
}
