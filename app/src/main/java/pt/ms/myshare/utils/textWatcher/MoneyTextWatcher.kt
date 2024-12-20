package pt.ms.myshare.utils.textWatcher

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.EditText
import pt.ms.myshare.utils.PreferenceUtils
import pt.ms.myshare.utils.Utils.getDecimalSeparator
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * @author @3dylson
 * */
class MoneyTextWatcher(private val editText: EditText?) : TextWatcher {

    companion object {
        private const val TAG = "MoneyTextWatcher"
        private const val MAX_DIGITS = 10 // Maximum digits allowed
        private const val DECIMAL_DIGITS = 2 // Number of decimal digits allowed
    }

    private val locale: Locale = PreferenceUtils.getLocale()
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = DECIMAL_DIGITS
        isGroupingUsed = true
    }

    private var isEditing: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No operation needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No operation needed
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditing) return // Prevent recursive calls
        isEditing = true

        try {
            val currentText = s?.toString() ?: ""
            if (editText == null || currentText.isEmpty()) {
                isEditing = false
                return
            }

            // Remove all formatting characters except the decimal separator
            val rawInput = getRawInput(currentText)
            if (rawInput.isEmpty()) {
                editText.setText("")
                isEditing = false
                return
            }

            // Check if the user is entering or removing the decimal separator
            val hasDecimal = rawInput.contains(getDecimalSeparator(numberFormat))
            val parsedValue = rawInput.replace(getDecimalSeparator(numberFormat), ".").toDoubleOrNull() ?: 0.0

            // Format based on whether the decimal is present
            val formatted = if (hasDecimal) {
                formatWithDecimal(parsedValue, rawInput)
            } else {
                numberFormat.format(parsedValue)
            }

            // Update the EditText
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error formatting text")
        } finally {
            isEditing = false
        }
    }

    private fun getRawInput(currentText: String) =
        currentText.replace(
            "[^\\d${escapeRegexSpecialChars(getDecimalSeparator(numberFormat))}]".toRegex(),
            ""
        )

    /**
     * Escapes regex special characters for use in dynamic regex patterns.
     */
    private fun escapeRegexSpecialChars(input: String): String {
        return Regex.escape(input)
    }

    /**
     * Formats the number while keeping the decimal part as the user types.
     */
    private fun formatWithDecimal(parsedValue: Double, rawInput: String): String {
        val parts = rawInput.split(getDecimalSeparator(numberFormat))
        val integerPart = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val decimalPart = parts.getOrNull(1) ?: ""

        // Format the integer part
        val formattedInteger = numberFormat.format(integerPart.toDouble())

        // Return formatted value with the user-typed decimal part
        return if (decimalPart.isEmpty()) {
            "$formattedInteger${getDecimalSeparator(numberFormat)}"
        } else {
            "$formattedInteger${getDecimalSeparator(numberFormat)}$decimalPart"
        }
    }
}


