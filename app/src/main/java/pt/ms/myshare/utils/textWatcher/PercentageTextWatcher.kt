package pt.ms.myshare.utils.textWatcher

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import pt.ms.myshare.utils.InputFilterNumberRange
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.StringUtils.parsePercentageValue
import java.lang.ref.WeakReference

private const val TAG = "PercentageTextWatcher"
private const val MAX_INPUT_LENGTH = 5 // ex: 100 %
private const val MAX_PERCENTAGE = "100"
private const val MIN_PERCENTAGE = "1"

/**
 * @author @3dylson
 * */
class PercentageTextWatcher(editText: EditText?) : TextWatcher {

    private var editTextWeakReference: WeakReference<EditText>? = null

    init {
        editTextWeakReference = WeakReference(editText)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        val editText: EditText = editTextWeakReference?.get() ?: return
        editText.filters =
            arrayOf(
                InputFilter.LengthFilter(MAX_INPUT_LENGTH), InputFilterNumberRange(
                    MIN_PERCENTAGE, MAX_PERCENTAGE
                )
            )

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val editText: EditText? = editTextWeakReference?.get()
        if (editText == null || editText.text.toString() == StringUtils.EMPTY_STRING) {
            return
        }
        editText.removeTextChangedListener(this)

        val parsed: String = parsePercentageValue(editText.text.toString())

        if (parsed.toFloatOrNull() != null && parsed.toFloat() > 100) {
            editText.addTextChangedListener(this)
            return
        }

        if (parsed == StringUtils.EMPTY_STRING) {
            editText.setText(parsed)
        } else {
            try {
                val formatted: String = parsed.plus(StringUtils.SPACE).plus(StringUtils.PERCENTAGE)
                editText.setText(formatted)
                editText.setSelection(formatted.length - 2)
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e.message, e)
            }
        }

        editText.addTextChangedListener(this)

    }


}