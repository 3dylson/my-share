package pt.ms.myshare.utils.logs

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object FirebaseUtils {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(context: Context) {
        // Ensure Firebase is initialized first
        FirebaseApp.initializeApp(context)
        
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        // Initialize App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        AppCheckProviderInstaller.install(firebaseAppCheck)
    }

    fun setUserProperty(propertyName: String, propertyValue: String?) {
        if (::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.setUserProperty(propertyName, propertyValue)
        } else {
            Timber.e("FirebaseUtils not initialized")
        }
    }

    fun setUserId(userId: String?) {
        if (::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.setUserId(userId)
        } else {
            Timber.e("FirebaseUtils not initialized")
        }
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

    fun logCrashlyticsBreadcrumb(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    fun setCrashlyticsKey(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun setCrashlyticsKey(key: String, value: Boolean) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun setCrashlyticsKey(key: String, value: Int) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun logButtonClickEvent(buttonName: String, screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName)
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }

        logEvent("button_click", params)
    }
}
