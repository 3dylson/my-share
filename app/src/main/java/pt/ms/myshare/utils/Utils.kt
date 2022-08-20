package pt.ms.myshare.utils

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout

/** Utility class to provide helper methods.  */
object Utils {

    /**
     * Adds an onFocusChangeListener so a scroll view can scroll to the position of the clicked EditText´s label
     * @param textInputs The list of inputs
     * @param textInputsLabels The list of the label in the same order as the [textInputs]
     * @param scrollView The Scroll View responsible to make the scroll
     * @param appBarLayout The activity app bar
     * */
    fun setScrollOnFocus(
        textInputs: Array<EditText>,
        textInputsLabels: Array<TextView>,
        scrollView: View,
        appBarLayout: AppBarLayout
    ) {
        for (editText in textInputs.indices) {
            textInputs[editText].setOnEditorActionListener { textView, actionID, keyEvent ->
                if (actionID == EditorInfo.IME_ACTION_NEXT) {
                    appBarLayout.setExpanded(false)
                    textInputs[editText + 1].requestFocus()
                }
                true
            }
            textInputs[editText].onFocusChangeListener =
                View.OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        val labelView = textInputsLabels[editText]
                        if (scrollView is NestedScrollView) {
                            scrollView.smoothScrollTo(
                                labelView.left,
                                labelView.top
                            )
                        } else if (scrollView is ScrollView) {
                            scrollView.smoothScrollTo(
                                labelView.left,
                                labelView.top
                            )
                        }
                    }
                }
        }
    }
}