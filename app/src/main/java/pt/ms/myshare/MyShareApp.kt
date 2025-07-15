package pt.ms.myshare

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ms.myshare.utils.logs.FirebaseUtils

class MyShareApp : Application(), Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        FirebaseUtils.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MyShareApp) {}
        }
        registerActivityLifecycleCallbacks(this)
        appOpenAdManager = AppOpenAdManager(this)
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