package pt.ms.myshare.domain.model

import java.util.Currency
import java.util.Locale

data class UserPreferences(
    val languageTag: String,
    val currencyCode: String
) {
    val locale: Locale
        get() = Locale.forLanguageTag(languageTag)

    val currency: Currency
        get() = Currency.getInstance(currencyCode)

    companion object {
        val supportedLanguages = listOf(
            SupportedLanguage("en", "English"),
            SupportedLanguage("pt-PT", "Português"),
            SupportedLanguage("es", "Español"),
            SupportedLanguage("fr", "Français"),
            SupportedLanguage("de", "Deutsch"),
            SupportedLanguage("ar", "العربية")
        )

        fun defaults(
            deviceLocale: Locale = Locale.getDefault(),
            countryIso: String? = deviceLocale.country
        ): UserPreferences {
            val languageTag = supportedLanguageFor(deviceLocale).languageTag
            val countryLocale = countryIso
                ?.takeIf { it.isNotBlank() }
                ?.let { Locale.Builder().setRegion(it.uppercase(Locale.US)).build() }
            val currencyCode = runCatching {
                Currency.getInstance(countryLocale ?: deviceLocale).currencyCode
            }
                .getOrDefault("USD")
            return UserPreferences(languageTag = languageTag, currencyCode = currencyCode)
        }

        fun supportedLanguageFor(locale: Locale): SupportedLanguage {
            return supportedLanguages.firstOrNull { it.languageTag == locale.toLanguageTag() }
                ?: supportedLanguages.firstOrNull { Locale.forLanguageTag(it.languageTag).language == locale.language }
                ?: supportedLanguages.first()
        }

        fun sanitize(
            languageTag: String?,
            currencyCode: String?,
            deviceLocale: Locale = Locale.getDefault(),
            countryIso: String? = deviceLocale.country
        ): UserPreferences {
            val defaults = defaults(deviceLocale, countryIso)
            val supportedLanguage = languageTag
                ?.let(Locale::forLanguageTag)
                ?.let(::supportedLanguageFor)
                ?: supportedLanguageFor(Locale.forLanguageTag(defaults.languageTag))
            val validCurrencyCode = currencyCode
                ?.uppercase(Locale.US)
                ?.takeIf { code -> runCatching { Currency.getInstance(code) }.isSuccess }
                ?: defaults.currencyCode
            return UserPreferences(
                languageTag = supportedLanguage.languageTag,
                currencyCode = validCurrencyCode
            )
        }
    }
}

data class SupportedLanguage(
    val languageTag: String,
    val displayName: String
)
