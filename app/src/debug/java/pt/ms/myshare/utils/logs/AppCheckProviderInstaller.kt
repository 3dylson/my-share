package pt.ms.myshare.utils.logs

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import timber.log.Timber

object AppCheckProviderInstaller {
    fun install(firebaseAppCheck: FirebaseAppCheck) {
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        Timber.d("Firebase App Check initialized with Debug provider")
    }
}
