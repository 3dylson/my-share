package pt.ms.myshare.utils.textWatcher

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import pt.ms.myshare.utils.PreferenceUtils
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.StringUtils.parseCurrencyValue
import pt.ms.myshare.utils.TimeUtils
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

private const val TAG = "MoneyTextWatcher"
private const val DECIMAL_DIGITS = 2
private const val MAX_DIGITS = 10
private const val PT_NUMBER_FORMAT = "pt_PT"

/**
 * @author @3dylson
 * */
class MoneyTextWatcher(editText: EditText?) : TextWatcher {
    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(
        PreferenceUtils.getCurrency()
    )
    private var editTextWeakReference: WeakReference<EditText>? = null

    init {
        editTextWeakReference = WeakReference(editText)
        numberFormat.maximumFractionDigits = 0
        numberFormat.roundingMode = RoundingMode.FLOOR
        Log.d(
            TAG,
            "NumberFormat By Device -> ${Locale.getDefault()} | NumberFormat By Default -> ${PreferenceUtils.getCurrency()}"
        )
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

        val parsed: BigDecimal = parseCurrencyValue(editText.text.toString(), numberFormat)
        val formatted: String = numberFormat.format(parsed)

        editText.setText(formatted)
        editText.filters =
            arrayOf(InputFilter.LengthFilter(MAX_DIGITS))
        editText.setSelection(formatted.length - 2)
        editText.addTextChangedListener(this)
    }

}