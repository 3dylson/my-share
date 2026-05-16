package pt.ms.myshare.presentation.ui.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import pt.ms.myshare.domain.model.UserPreferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocaleManager @Inject constructor() {

    fun apply(preferences: UserPreferences) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current == preferences.languageTag) return
        Timber.tag(TAG).d("Applying app locale language=%s", preferences.languageTag)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(preferences.languageTag))
    }

    private companion object {
        const val TAG = "UserLocaleManager"
    }
}
