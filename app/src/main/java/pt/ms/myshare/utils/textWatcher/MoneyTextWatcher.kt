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
class MoneyTextWatcher(private val editText: EditText) : TextWatcher {

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

    private val lengthFilter = InputFilter { source, start, end, dest, dstart, dend ->
        val newText = dest.toString().substring(0, dstart) +
                source.toString().substring(start, end) +
                dest.toString().substring(dend)

        val rawInput = getRawInput(newText)
        if (rawInput.length <= MAX_DIGITS) {
            null
        } else {
            ""
        }
    }

    init {
        editText.filters = arrayOf(lengthFilter)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No operation needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // No operation needed
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditing) return
        isEditing = true

        try {
            val currentText = s?.toString() ?: ""
            if (currentText.isEmpty()) {
                editText.setText("")
                isEditing = false
                return
            }

            val rawInput = getRawInput(currentText)
            if (rawInput.isEmpty()) {
                editText.setText("")
                isEditing = false
                return
            }

            val hasDecimal = rawInput.contains(getDecimalSeparator(numberFormat))
            val parsedValue = rawInput.replace(getDecimalSeparator(numberFormat), ".").toDoubleOrNull() ?: 0.0

            val formatted = if (hasDecimal) {
                formatWithDecimal(parsedValue, rawInput)
            } else {
                numberFormat.format(parsedValue)
            }

            editText.setText(formatted)
            editText.setSelection(formatted.length)
        } catch (e: Exception) {
            Timber.tag(this::class.java.simpleName).e(e, "Error formatting text")
        } finally {
            isEditing = false
        }
    }

    private fun getRawInput(currentText: String): String {
        val allowedChars = "0123456789" + getDecimalSeparator(numberFormat)
        return currentText.filter { it in allowedChars }
    }

    private fun formatWithDecimal(parsedValue: Double, rawInput: String): String {
        val formatted = numberFormat.format(parsedValue)
        if (rawInput.endsWith(getDecimalSeparator(numberFormat))) {
            return formatted + getDecimalSeparator(numberFormat)
        }
        return formatted
    }
}