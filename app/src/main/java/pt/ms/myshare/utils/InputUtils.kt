package pt.ms.myshare.utils

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import pt.ms.myshare.R

object InputUtils {

    fun saveInputsData(screenInputs: Array<EditText>, context: Context) {
        screenInputs.forEach {
            val inputText = it.text.toString()
            val rawValue = StringUtils.getRawInputText(inputText)

            PreferenceUtils.saveStringPreference(context, it.id, rawValue)
        }
    }

    fun getInputsData(screenInputs: Array<EditText>, context: Context) {
        screenInputs.forEach {
            val value = PreferenceUtils.getStringPref(context, it.id, StringUtils.EMPTY_STRING)
            it.setText(value)
        }
    }

    fun inputDataChanged(screenInputs: Array<EditText>, context: Context): Boolean {
        val hasChanged = false
        screenInputs.forEach {
            val savedData =
                PreferenceUtils.getStringPref(context, it.id, StringUtils.EMPTY_STRING)
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

    fun saveAmountToInvest(amountToInvest: Int, context: Context) {
        PreferenceUtils.saveIntPreference(context, R.string.amount_to_invest, amountToInvest)
    }

    fun saveAmountForStocks(amountForStocks: Int, context: Context) {
        PreferenceUtils.saveIntPreference(context, R.string.amount_for_stocks, amountForStocks)
    }

    fun saveAmountForCrypto(amountForCrypto: Int, context: Context) {
        PreferenceUtils.saveIntPreference(context, R.string.amount_for_crypto, amountForCrypto)
    }

    fun saveAmountForSavings(amountForSavings: Int, context: Context) {
        PreferenceUtils.saveIntPreference(context, R.string.amount_for_savings, amountForSavings)
    }
}