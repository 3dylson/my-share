package pt.ms.myshare.utils

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object InputUtils {

    fun saveInputsData(screenInputs: Array<EditText>, context: Context) {
        screenInputs.forEach {
            val inputText = it.text.toString()
            val rawValue = StringUtils.getRawInputText(inputText)

            PreferenceUtils.setInputValue(context, it.id, rawValue)
        }
    }

    fun getInputsData(screenInputs: Array<EditText>, context: Context) {
        screenInputs.forEach {
            val value = PreferenceUtils.getInputValue(context, it.id, StringUtils.EMPTY_STRING)
            when {
                value != null && value.isNotEmpty() -> it.setText(value)
            }
        }
    }

    fun inputDataChanged(screenInputs: Array<EditText>, context: Context): Boolean {
        val hasChanged = false
        screenInputs.forEach {
            val savedData =
                PreferenceUtils.getInputValue(context, it.id, StringUtils.EMPTY_STRING)
            val currentData = StringUtils.getRawInputText(it.text.toString())
            if (currentData != savedData) return true
        }
        return hasChanged
    }

    fun isAnyInputEmpty(textInputs: Array<EditText>): Boolean {
        textInputs.forEach {
            if (TextUtils.isEmpty(it.text.toString())) return true
        }

        return false
    }

    fun hideKeyboard(view: View) {
        val imm =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}