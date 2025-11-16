package pt.ms.myshare.utils

import java.text.NumberFormat

fun String.formatToCurrency(): String {
    val value = this.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
    return NumberFormat.getCurrencyInstance().format((value / 100))
}
