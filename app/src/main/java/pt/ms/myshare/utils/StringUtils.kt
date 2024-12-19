package pt.ms.myshare.utils

import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

object StringUtils {
    private const val TAG = "StringUtils"
    const val EMPTY_STRING = ""
    const val SPACE = " "
    const val COMMA = ","
    const val DOT = "."
    const val PARAGRAPH = "\n"
    const val PERCENTAGE = "%"
    const val ZERO = "0"
    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(
        PreferenceUtils.getLocale()
    )
    val CURRENCY_SYMBOL: String = numberFormat.currency!!.symbol

    fun parsePercentageValue(value: String): String {
        if (value == EMPTY_STRING) return ZERO

        val formatted = value.replace(PERCENTAGE, EMPTY_STRING)
            .replace(SPACE, EMPTY_STRING)

        if (formatted == DOT || formatted == COMMA) {
            return ZERO
        }

        return formatted
    }

    fun parseCurrencyValue(value: String, numberFormat: NumberFormat): BigDecimal {
        try {
            val replaceRegex = java.lang.String.format(
                "[%s,\\s]",
                numberFormat.currency!!.displayName
            )
            val currencyValue =
                value.replace(replaceRegex.toRegex(), EMPTY_STRING).replace(
                    numberFormat.currency!!.symbol, EMPTY_STRING
                )
            return BigDecimal(currencyValue)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e)
        }
        return BigDecimal.ZERO
    }

    private fun isPercentageValue(value: String): Boolean {
        return value.contains(PERCENTAGE)
    }

    private fun isCurrencyValue(value: String): Boolean {
        return value.contains(CURRENCY_SYMBOL)
    }

    fun getRawInputText(inputText: String) =
        if (isCurrencyValue(inputText)) {
            inputText.replace(CURRENCY_SYMBOL, EMPTY_STRING).filter { !it.isWhitespace() }
        } else if (isPercentageValue(inputText)) {
            inputText.replace(PERCENTAGE, EMPTY_STRING).filter { !it.isWhitespace() }
        } else inputText.replace(SPACE, EMPTY_STRING).filter { !it.isWhitespace() }

    fun formatCurrency(value: String): String {
        val formatter = numberFormat
        formatter.apply {
            maximumFractionDigits = 0
            roundingMode = RoundingMode.FLOOR
        }
        val parsed: BigDecimal = parseCurrencyValue(value, formatter)
        return formatter.format(parsed)
    }
}