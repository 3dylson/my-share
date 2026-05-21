package pt.ms.myshare.presentation.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlayInAppReviewRequester(context: Context) {

    private val reviewManager = ReviewManagerFactory.create(context.applicationContext)

    suspend fun requestReview(activity: Activity): Boolean = suspendCoroutine { continuation ->
        Timber.tag(TAG).d("Requesting Play in-app review flow")
        reviewManager.requestReviewFlow()
            .addOnCompleteListener { requestTask ->
                if (!requestTask.isSuccessful) {
                    Timber.tag(TAG).e(requestTask.exception, "Play in-app review info request failed")
                    continuation.resume(false)
                    return@addOnCompleteListener
                }

                reviewManager.launchReviewFlow(activity, requestTask.result)
                    .addOnCompleteListener { flowTask ->
                        if (flowTask.isSuccessful) {
                            Timber.tag(TAG).d("Play in-app review flow completed")
                        } else {
                            Timber.tag(TAG).e(flowTask.exception, "Play in-app review flow failed")
                        }
                        continuation.resume(flowTask.isSuccessful)
                    }
            }
    }

    private companion object {
        const val TAG = "PlayInAppReview"
    }
}
