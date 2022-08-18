package pt.ms.myshare.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.lang.String.format
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

private const val TAG = "MoneyTextWatcher"
private const val DECIMAL_DIGITS = 2

class MoneyTextWatcher(editText: EditText?) : TextWatcher {
    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(
        Locale.getDefault()
    )
    private var editTextWeakReference: WeakReference<EditText>? = null

    init {
        editTextWeakReference = WeakReference(editText)
        numberFormat.maximumFractionDigits = 0
        numberFormat.roundingMode = RoundingMode.FLOOR
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        val editText: EditText = editTextWeakReference?.get() ?: return
        if (s.toString().replace(numberFormat.currency!!.symbol, StringUtils.EMPTY_STRING).trimEnd()
                .endsWith(StringUtils.DOT)
        ) {
            editText.filters =
                arrayOf(InputFilter.LengthFilter(s.toString().length + DECIMAL_DIGITS))
        }

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val editText: EditText? = editTextWeakReference?.get()
        if (editText == null || editText.text.toString() == StringUtils.EMPTY_STRING) {
            return
        }
        editText.removeTextChangedListener(this)

        if (s.toString() != StringUtils.DOT && s.toString().contains(StringUtils.DOT)) {
            editText.setText(s.toString())
            editText.setSelection(s.toString().length - 2)
            editText.addTextChangedListener(this)
            return
        }

        val parsed: BigDecimal = parseCurrencyValue(editText.text.toString())
        val formatted: String = numberFormat.format(parsed)

        editText.setText(formatted)
        editText.filters = emptyArray()
        editText.setSelection(formatted.length - 2)
        editText.addTextChangedListener(this)
    }


    fun parseCurrencyValue(value: String): BigDecimal {
        try {
            val replaceRegex = format(
                "[%s,\\s]",
                numberFormat.currency!!.displayName
            )
            val currencyValue =
                value.replace(replaceRegex.toRegex(), StringUtils.EMPTY_STRING).replace(
                    numberFormat.currency!!.symbol, StringUtils.EMPTY_STRING
                )
            return BigDecimal(currencyValue)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
        return BigDecimal.ZERO
    }
}