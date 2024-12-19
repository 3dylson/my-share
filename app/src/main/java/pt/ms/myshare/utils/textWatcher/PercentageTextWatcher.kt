package pt.ms.myshare.utils.textWatcher

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import pt.ms.myshare.utils.InputFilterNumberRange
import pt.ms.myshare.utils.StringUtils
import pt.ms.myshare.utils.StringUtils.parsePercentageValue
import timber.log.Timber
import java.lang.ref.WeakReference

private const val TAG = "PercentageTextWatcher"
private const val MAX_INPUT_LENGTH = 5 // ex: 100 %
private const val MAX_PERCENTAGE = 100
private const val MIN_PERCENTAGE = 0

/**
 * @author @3dylson
 * */
class PercentageTextWatcher(editTexts: Array<EditText?>) : TextWatcher {

    private var numberOfIteration = editTexts.size
    private var editTextWeakReferences: Array<WeakReference<EditText>?> =
        arrayOfNulls(numberOfIteration)

    //Flag to know when to start caring about empty string input
    var isBuildingView = true

    init {
        editTexts.forEachIndexed { index, editText ->
            editTextWeakReferences[index] = WeakReference(editText)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        for (i in 0 until numberOfIteration) {
            val editText: EditText = editTextWeakReferences[i]?.get() ?: return
            editText.filters =
                arrayOf(
                    InputFilter.LengthFilter(MAX_INPUT_LENGTH), InputFilterNumberRange(
                        MIN_PERCENTAGE.toString(), MAX_PERCENTAGE.toString()
                    )
                )
        }

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Not needed
    }

    override fun afterTextChanged(s: Editable?) {
        val editText: EditText? =
            editTextWeakReferences.find { weakReference -> weakReference!!.get()!!.editableText!! == s }
                ?.get()

        val inputText = editText?.text.toString()

        if (editText == null || isBuildingView) {
            return
        }
        editText.removeTextChangedListener(this)

        var parsed: String = parsePercentageValue(inputText)


        if (parsed != StringUtils.EMPTY_STRING && (parsed.toFloatOrNull() != null && parsed.toFloat() > MAX_PERCENTAGE)
        ) {
            editText.addTextChangedListener(this)
            return
        }


        if (parsed == StringUtils.EMPTY_STRING) {
            editText.setText(parsed)
        } else {
            if (parsed.first().toString() == StringUtils.ZERO) {
                parsed = StringUtils.ZERO
            }

            //TODO fill the rest with the amount available also consider adding error label
            if (sumOfPercentage() > MAX_PERCENTAGE) {
                parsed = parsed.dropLast(1)
                if (parsed == StringUtils.EMPTY_STRING) {
                    parsed = StringUtils.ZERO
                }
            }

            try {
                val formatted: String =
                    parsed.plus(StringUtils.SPACE).plus(StringUtils.PERCENTAGE)
                editText.setText(formatted)
                editText.setSelection(formatted.length - 2)
            } catch (e: IndexOutOfBoundsException) {
                Timber.tag(TAG).w(e)
            }
        }

        editText.addTextChangedListener(this)


    }


    private fun sumOfPercentage(): Float {
        var sum = 0f
        editTextWeakReferences.forEach {
            sum += parsePercentageValue(it!!.get()!!.text!!.toString()).toFloat()
        }
        return sum
    }


}