package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AppReviewPromptState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ResolveAppReviewPromptEligibilityUseCase @Inject constructor() {

    fun execute(state: AppReviewPromptState, nowMillis: Long): Boolean {
        if (state.positiveActionCount < REQUIRED_POSITIVE_ACTIONS) return false
        if (state.inAppReviewRequestCount >= MAX_IN_APP_REVIEW_REQUESTS) return false
        if (state.lastInAppReviewRequestedAtMillis == 0L) return true

        val elapsedMillis = nowMillis - state.lastInAppReviewRequestedAtMillis
        return elapsedMillis >= MIN_RETRY_INTERVAL_MILLIS
    }

    private companion object {
        const val REQUIRED_POSITIVE_ACTIONS = 2
        const val MAX_IN_APP_REVIEW_REQUESTS = 1
        val MIN_RETRY_INTERVAL_MILLIS: Long = TimeUnit.DAYS.toMillis(120)
    }
}
