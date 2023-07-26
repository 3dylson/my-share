package pt.ms.myshare.utils.logs

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

object FirebaseUtils {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(context: Context) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logScreen(screenName: String) {
        if (::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                },
            )
        } else {
            Timber.e("FirebaseUtils not initialized")
        }
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        if (!::firebaseAnalytics.isInitialized) {
            Timber.e("FirebaseUtils not initialized")
            return
        }
        firebaseAnalytics.logEvent(eventName, params)
    }

    fun logButtonClickEvent(buttonName: String, screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName)
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }

        logEvent("button_click", params)
    }
}
