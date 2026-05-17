package pt.ms.myshare.presentation.ui.preferences

import java.util.Currency
import java.util.Locale

object CurrencyPickerCatalog {

    fun options(locale: Locale): List<CurrencyOption> {
        return supportedCurrencyCodes.mapNotNull { code ->
            runCatching { Currency.getInstance(code) }.getOrNull()?.let { currency ->
                CurrencyOption(
                    code = currency.currencyCode,
                    symbol = currency.getSymbol(locale),
                    displayName = currency.getDisplayName(locale)
                )
            }
        }
    }

    private val supportedCurrencyCodes = listOf(
        "USD", "EUR", "GBP",
        "CAD", "AUD", "NZD", "CHF",
        "SEK", "NOK", "DKK",
        "PLN", "CZK", "HUF", "RON", "BGN",
        "BRL", "MXN", "ARS", "CLP", "COP", "PEN", "UYU",
        "NGN", "GHS", "KES", "MAD", "EGP", "ZAR",
        "INR", "PKR", "BDT",
        "IDR", "PHP", "VND", "THB", "MYR", "SGD", "HKD",
        "JPY", "KRW", "CNY",
        "AED", "SAR", "QAR", "KWD", "ILS"
    )
}
