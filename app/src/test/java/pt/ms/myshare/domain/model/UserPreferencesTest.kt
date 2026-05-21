package pt.ms.myshare.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class UserPreferencesTest {

    @Test
    fun `defaults resolve language from device and currency from country`() {
        val preferences = UserPreferences.defaults(
            deviceLocale = Locale.forLanguageTag("pt-PT"),
            countryIso = "US"
        )

        assertEquals("pt-PT", preferences.languageTag)
        assertEquals("USD", preferences.currencyCode)
    }

    @Test
    fun `sanitize falls back for unsupported language and invalid currency`() {
        val preferences = UserPreferences.sanitize(
            languageTag = "it-IT",
            currencyCode = "invalid",
            deviceLocale = Locale.US
        )

        assertEquals("en", preferences.languageTag)
        assertEquals("USD", preferences.currencyCode)
    }
}
