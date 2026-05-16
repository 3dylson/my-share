package pt.ms.myshare.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun String.formatToCurrency(locale: Locale = Locale.getDefault(), currencyCode: String? = null): String {
    val value = this.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
    return NumberFormat.getCurrencyInstance(locale).apply {
        currencyCode?.let { code -> runCatching { currency = Currency.getInstance(code) } }
    }.format((value / 100))
}
