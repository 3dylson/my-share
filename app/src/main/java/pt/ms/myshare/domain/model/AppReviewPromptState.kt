package pt.ms.myshare.domain.model

data class AppReviewPromptState(
    val positiveActionCount: Int = 0,
    val inAppReviewRequestCount: Int = 0,
    val lastInAppReviewRequestedAtMillis: Long = 0L,
    val playStoreRateOpenCount: Int = 0
)
