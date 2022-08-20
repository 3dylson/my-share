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

    fun parsePercentageValue(value: String): String {
        val formatted = value.replace(PERCENTAGE, EMPTY_STRING)
            .replace(SPACE, EMPTY_STRING)

        if (formatted == DOT) {
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
}