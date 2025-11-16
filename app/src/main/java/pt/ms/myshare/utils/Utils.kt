package pt.ms.myshare.utils

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.text.DecimalFormat
import java.text.NumberFormat

/** Utility class to provide helper methods.  */
object Utils {

    fun getPercentOfNumber(value: Int, percentage : Float) : Int {
        return (value * (percentage / 100.0f)).toInt()
    }

    /**
     * Returns the decimal separator for the current locale.
     */
    fun getDecimalSeparator(numberFormat: NumberFormat): String {
        return (numberFormat as DecimalFormat).decimalFormatSymbols.decimalSeparator.toString()
    }

}