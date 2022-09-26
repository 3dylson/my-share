package pt.ms.myshare.utils

import android.util.Log
import java.math.BigDecimal
import java.text.NumberFormat

object StringUtils {
    private const val TAG = "StringUtils"
    const val EMPTY_STRING = ""
    const val SPACE = " "
    const val COMMA = ","
    const val DOT = "."
    const val PERCENTAGE = "%"
    const val ZERO = "0"
    val CURRENCY: String =
        NumberFormat.getCurrencyInstance(PreferenceUtils.getCurrency()).currency!!.symbol

    fun parsePercentageValue(value: String): String {
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
            Log.w(TAG, e.message, e)
        }
        return BigDecimal.ZERO
    }

    fun isPercentageValue(value: String): Boolean {
        return value.contains(PERCENTAGE)
    }

    fun isCurrencyValue(value: String): Boolean {
        return value.contains(CURRENCY)
    }

    fun getRawInputText(inputText: String) =
        if (isCurrencyValue(inputText)) inputText.replace(
            CURRENCY,
            EMPTY_STRING
        ).replace(SPACE, EMPTY_STRING)
            .trim() else if (isPercentageValue(inputText)) inputText.replace(
            PERCENTAGE,
            EMPTY_STRING
        ).replace(SPACE, EMPTY_STRING).trim() else inputText.replace(SPACE, EMPTY_STRING).trim()
}