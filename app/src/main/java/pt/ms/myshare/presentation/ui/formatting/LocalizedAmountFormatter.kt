package pt.ms.myshare.presentation.ui.formatting

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Responsibility: Formats and parses user-facing monetary and percentage values using the active locale.
 */
object LocalizedAmountFormatter {

    fun currencySymbol(locale: Locale = Locale.getDefault()): String {
        return DecimalFormatSymbols.getInstance(locale).currencySymbol
    }

    fun formatEditableAmount(amount: BigDecimal, locale: Locale = Locale.getDefault()): String {
        val formatter = NumberFormat.getNumberInstance(locale) as DecimalFormat
        formatter.isGroupingUsed = false
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter.format(amount)
    }

    fun formatPercentage(percentValue: BigDecimal, locale: Locale = Locale.getDefault()): String {
        val formatter = NumberFormat.getPercentInstance(locale)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter.format(percentValue.divide(BigDecimal("100")))
    }

    fun parseAmount(input: String, locale: Locale = Locale.getDefault()): BigDecimal? {
        val compact = input
            .trim()
            .replace("\\s".toRegex(), "")
            .replace("\u00A0", "")
        if (compact.isBlank()) return null

        val symbols = DecimalFormatSymbols.getInstance(locale)
        val decimalSeparator = symbols.decimalSeparator
        val groupingSeparator = symbols.groupingSeparator
        val normalized = normalizeSeparators(compact, decimalSeparator, groupingSeparator)
            .filter { it.isDigit() || it == '.' || it == '-' }

        return normalized.toBigDecimalOrNull()
    }

    fun sanitizeAmountInput(input: String, locale: Locale = Locale.getDefault()): String {
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val decimalSeparator = symbols.decimalSeparator
        return input.filter { it.isDigit() || it == decimalSeparator || it == '.' || it == ',' }
    }

    private fun normalizeSeparators(
        input: String,
        decimalSeparator: Char,
        groupingSeparator: Char
    ): String {
        val withoutGrouping = input.replace(groupingSeparator.toString(), "")
        if (decimalSeparator != '.') {
            return withoutGrouping.replace(decimalSeparator, '.')
        }

        val commaIndex = withoutGrouping.lastIndexOf(',')
        val dotIndex = withoutGrouping.lastIndexOf('.')
        return when {
            commaIndex >= 0 && dotIndex >= 0 && commaIndex > dotIndex ->
                withoutGrouping.replace(".", "").replace(',', '.')
            commaIndex >= 0 && dotIndex < 0 ->
                withoutGrouping.replace(',', '.')
            else -> withoutGrouping
        }
    }
}
