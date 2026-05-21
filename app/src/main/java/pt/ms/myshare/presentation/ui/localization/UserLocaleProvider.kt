package pt.ms.myshare.presentation.ui.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

@Composable
fun UserLocaleProvider(
    languageTag: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val locale = remember(languageTag) { Locale.forLanguageTag(languageTag) }
    val localizedConfiguration = remember(baseConfiguration, locale) {
        Configuration(baseConfiguration).applySelectedLocale(locale)
    }
    val localizedContext = remember(baseContext, localizedConfiguration) {
        baseContext.createConfigurationContext(localizedConfiguration)
    }
    val layoutDirection = remember(locale) {
        if (TextUtils.getLayoutDirectionFromLocale(locale) == android.view.View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides layoutDirection,
        content = content
    )
}

private fun Configuration.applySelectedLocale(locale: Locale): Configuration {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setLocales(LocaleList(locale))
    } else {
        @Suppress("DEPRECATION")
        setLocale(locale)
    }
    setLayoutDirection(locale)
    return this
}
