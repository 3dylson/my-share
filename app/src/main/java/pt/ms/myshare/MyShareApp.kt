package pt.ms.myshare

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import pt.ms.myshare.utils.logs.FirebaseUtils

class MyShareApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseUtils.init(this)
    }
}