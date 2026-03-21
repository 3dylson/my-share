package pt.ms.myshare.presentation

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.os.Build
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ms.myshare.utils.logs.FirebaseUtils

@HiltAndroidApp
class MyShareApp : Application(), Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        FirebaseUtils.init(this)
        createNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MyShareApp) {}
        }
        registerActivityLifecycleCallbacks(this)
        appOpenAdManager = AppOpenAdManager(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            /* id = */ "payday_reminder",
            /* name = */ "Payday reminders",
            /* importance = */ NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you on payday how much to invest in Stocks, Crypto, and Savings."
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        appOpenAdManager.showAdIfAvailable(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}