package pt.ms.myshare.utils

import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import timber.log.Timber

private const val TAG = "InputFilterNumberRange"

/**
 * @author @3dylson
 * */
class InputFilterNumberRange : InputFilter {
    private var min: Int
    private var max: Int

    internal constructor(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = min.toInt()
        this.max = max.toInt()
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {

        try {
            val input = (StringUtils.parsePercentageValue(dest.toString()) + StringUtils.parsePercentageValue(source.toString())).toInt()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
            Timber.tag(TAG).w(nfe)
        }
        return StringUtils.EMPTY_STRING
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}