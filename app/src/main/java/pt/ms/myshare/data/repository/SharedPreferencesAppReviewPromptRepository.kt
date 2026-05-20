package pt.ms.myshare.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ms.myshare.domain.model.AppReviewPromptState
import pt.ms.myshare.domain.repository.AppReviewPromptRepository
import timber.log.Timber
import javax.inject.Inject

class SharedPreferencesAppReviewPromptRepository @Inject constructor(
    @ApplicationContext context: Context
) : AppReviewPromptRepository {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun loadState(): AppReviewPromptState = preferences.currentState()

    override suspend fun recordPositiveAction(): AppReviewPromptState {
        val updated = preferences.currentState().let {
            it.copy(positiveActionCount = it.positiveActionCount + 1)
        }
        preferences.saveState(updated)
        Timber.tag(TAG).d("App review positive action recorded. count=%d", updated.positiveActionCount)
        return updated
    }

    override suspend fun markInAppReviewRequested(requestedAtMillis: Long): AppReviewPromptState {
        val updated = preferences.currentState().let {
            it.copy(
                inAppReviewRequestCount = it.inAppReviewRequestCount + 1,
                lastInAppReviewRequestedAtMillis = requestedAtMillis
            )
        }
        preferences.saveState(updated)
        Timber.tag(TAG).d("In-app review request marked. count=%d", updated.inAppReviewRequestCount)
        return updated
    }

    override suspend fun markPlayStoreRateOpened(): AppReviewPromptState {
        val updated = preferences.currentState().let {
            it.copy(playStoreRateOpenCount = it.playStoreRateOpenCount + 1)
        }
        preferences.saveState(updated)
        Timber.tag(TAG).d("Play Store rate entry opened. count=%d", updated.playStoreRateOpenCount)
        return updated
    }

    private fun android.content.SharedPreferences.currentState(): AppReviewPromptState =
        AppReviewPromptState(
            positiveActionCount = getInt(KEY_POSITIVE_ACTION_COUNT, 0),
            inAppReviewRequestCount = getInt(KEY_IN_APP_REVIEW_REQUEST_COUNT, 0),
            lastInAppReviewRequestedAtMillis = getLong(KEY_LAST_IN_APP_REVIEW_REQUESTED_AT, 0L),
            playStoreRateOpenCount = getInt(KEY_PLAY_STORE_RATE_OPEN_COUNT, 0)
        )

    private fun android.content.SharedPreferences.saveState(state: AppReviewPromptState) {
        edit()
            .putInt(KEY_POSITIVE_ACTION_COUNT, state.positiveActionCount)
            .putInt(KEY_IN_APP_REVIEW_REQUEST_COUNT, state.inAppReviewRequestCount)
            .putLong(KEY_LAST_IN_APP_REVIEW_REQUESTED_AT, state.lastInAppReviewRequestedAtMillis)
            .putInt(KEY_PLAY_STORE_RATE_OPEN_COUNT, state.playStoreRateOpenCount)
            .apply()
    }

    private companion object {
        const val TAG = "AppReviewPromptStore"
        const val PREFERENCES_NAME = "app_review_prompt"
        const val KEY_POSITIVE_ACTION_COUNT = "positive_action_count"
        const val KEY_IN_APP_REVIEW_REQUEST_COUNT = "in_app_review_request_count"
        const val KEY_LAST_IN_APP_REVIEW_REQUESTED_AT = "last_in_app_review_requested_at"
        const val KEY_PLAY_STORE_RATE_OPEN_COUNT = "play_store_rate_open_count"
    }
}
