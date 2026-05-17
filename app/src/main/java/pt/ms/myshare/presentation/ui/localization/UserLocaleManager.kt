package pt.ms.myshare.presentation.ui.localization

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ms.myshare.domain.model.UserPreferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun apply(preferences: UserPreferences) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applyPlatformLocale(preferences)
        } else {
            applyAppCompatLocale(preferences)
        }
    }

    private fun applyPlatformLocale(preferences: UserPreferences) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val current = localeManager.applicationLocales.toLanguageTags()
        if (current == preferences.languageTag) return
        Timber.tag(TAG).d("Applying platform app locale language=%s", preferences.languageTag)
        localeManager.applicationLocales = LocaleList.forLanguageTags(preferences.languageTag)
    }

    private fun applyAppCompatLocale(preferences: UserPreferences) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current == preferences.languageTag) return
        Timber.tag(TAG).d("Applying AppCompat app locale language=%s", preferences.languageTag)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(preferences.languageTag))
    }

    private companion object {
        const val TAG = "UserLocaleManager"
    }
}
