package pt.ms.myshare.presentation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.utils.logs.CrashlyticsTree
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber

@HiltAndroidApp
class MyShareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Always plant CrashlyticsTree so we catch non-fatal errors in both Debug/Release
        Timber.plant(CrashlyticsTree())

        FirebaseUtils.init(this)
        


        createNotificationChannel()
        Timber.tag(TAG).d("Application created")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Payday reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you what to do with your next payday plan."
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "payday_reminder"
        private const val TAG = "MyShareApp"
    }
}
