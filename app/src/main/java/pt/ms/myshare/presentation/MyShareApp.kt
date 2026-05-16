package pt.ms.myshare.presentation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.presentation.ui.localization.UserLocaleManager
import pt.ms.myshare.utils.logs.CrashlyticsTree
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyShareApp : Application() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var userLocaleManager: UserLocaleManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Always plant CrashlyticsTree so we catch non-fatal errors in both Debug/Release
        Timber.plant(CrashlyticsTree())

        FirebaseUtils.init(this)

        userLocaleManager.apply(userPreferencesRepository.loadPreferences())


        createNotificationChannel()
        Timber.tag(TAG).d("Application created")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(pt.ms.myshare.R.string.notification_channel_payday_reminders_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(pt.ms.myshare.R.string.notification_channel_payday_reminders_description)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "payday_reminder"
        private const val TAG = "MyShareApp"
    }
}
