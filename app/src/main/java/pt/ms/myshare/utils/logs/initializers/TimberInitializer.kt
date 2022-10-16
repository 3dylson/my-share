package pt.ms.myshare.utils.logs.initializers

import android.content.Context
import androidx.startup.Initializer
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.utils.logs.CrashlyticsTree
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}