package pt.ms.myshare.utils

import java.text.NumberFormat
import java.util.Locale

fun String.formatToCurrency(): String {
    val value = this.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format((value / 100))
}
