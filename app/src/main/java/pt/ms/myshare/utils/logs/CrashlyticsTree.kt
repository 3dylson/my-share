package pt.ms.myshare.utils.logs

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Add log as a breadcrumb that will be attached to any subsequent crash/exception
        crashlytics.log("${priorityAsString(priority)} - ${tag ?: "NO_TAG"}: $message")

        if (t != null) {
            crashlytics.recordException(t)
        } else if (priority == Log.ERROR) {
            // Only record non-fatal exceptions for ERROR level to avoid spamming Crashlytics with WARNs
            crashlytics.recordException(Exception(message))
        }
    }

    private fun priorityAsString(priority: Int): String {
        return when (priority) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            else -> priority.toString()
        }
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}