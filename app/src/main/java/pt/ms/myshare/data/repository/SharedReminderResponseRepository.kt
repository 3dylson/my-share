package pt.ms.myshare.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ms.myshare.domain.model.ReminderResponse
import pt.ms.myshare.domain.repository.ReminderResponseRepository
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedReminderResponseRepository @Inject constructor(
    @ApplicationContext context: Context
) : ReminderResponseRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveResponse(response: ReminderResponse) {
        val count = preferences.getInt(KEY_RESPONSE_COUNT, 0) + 1
        preferences.edit()
            .putString(KEY_NOTIFICATION_TYPE, response.notificationType)
            .putString(KEY_RESPONSE, response.response)
            .putString(KEY_RESPONDED_AT, response.respondedAt.toString())
            .putInt(KEY_RESPONSE_COUNT, count)
            .apply()
        Timber.tag(TAG).d(
            "Reminder response stored type=%s response=%s count=%d",
            response.notificationType,
            response.response,
            count
        )
    }

    override fun loadLatestResponse(): ReminderResponse? {
        val notificationType = preferences.getString(KEY_NOTIFICATION_TYPE, null) ?: return null
        val response = preferences.getString(KEY_RESPONSE, null) ?: return null
        val respondedAt = preferences.getString(KEY_RESPONDED_AT, null)
            ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            ?: return null
        return ReminderResponse(
            notificationType = notificationType,
            response = response,
            respondedAt = respondedAt
        )
    }

    private companion object {
        const val TAG = "ReminderResponseRepository"
        const val PREFS_NAME = "myshare_reminder_responses"
        const val KEY_NOTIFICATION_TYPE = "latest_notification_type"
        const val KEY_RESPONSE = "latest_response"
        const val KEY_RESPONDED_AT = "latest_responded_at"
        const val KEY_RESPONSE_COUNT = "response_count"
    }
}
