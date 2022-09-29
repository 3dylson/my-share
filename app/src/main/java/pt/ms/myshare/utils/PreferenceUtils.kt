package pt.ms.myshare.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import pt.ms.myshare.R
import java.util.*

/** Utility class to retrieve shared preferences. */
object PreferenceUtils {

    fun getCurrency(): Locale = Locale.FRANCE

    fun getAmountToInvest(context: Context, @StringRes category: Int): String {
        val amount = getIntPref(context, category, 0)
        return amount.toString().plus(StringUtils.SPACE).plus(StringUtils.CURRENCY)
    }

    fun setAmountToInvest(amountToInvest: Int, context: Context) {
        saveIntPreference(context, R.string.id_amount_to_invest, amountToInvest)
    }

    fun setAmountForStocks(amountForStocks: Int, context: Context) {
        saveIntPreference(context, R.string.id_amount_for_stocks, amountForStocks)
    }

    fun setAmountForCrypto(amountForCrypto: Int, context: Context) {
        saveIntPreference(context, R.string.id_amount_for_crypto, amountForCrypto)
    }

    fun setAmountForSavings(amountForSavings: Int, context: Context) {
        saveIntPreference(context, R.string.id_amount_for_savings, amountForSavings)
    }

    fun setInputValue(context: Context, prefKeyId: Int, value: String) {
        saveStringWithIdPreference(context, prefKeyId, value)
    }

    fun getInputValue(context: Context, prefKeyId: Int, defaultValue: String?): String? {
        return getStringPref(context, prefKeyId, defaultValue)
    }


    private fun saveStringResPreference(
        context: Context,
        @StringRes prefKeyId: Int,
        value: String?
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(prefKeyId), value)
            .apply()
    }


    private fun saveStringWithIdPreference(context: Context, prefKeyId: Int, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(prefKeyId.toString(), value)
            .apply()
    }

    private fun saveIntPreference(context: Context, @StringRes prefKeyId: Int, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(context.getString(prefKeyId), value)
            .apply()
    }

    private fun saveBooleanPref(context: Context, @StringRes prefKeyId: Int, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(context.getString(prefKeyId), value)
            .apply()
    }

    private fun getIntPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyId)
        return sharedPreferences.getInt(prefKey, defaultValue)
    }

    private fun getStringPref(
        context: Context,
        prefKeyId: Int,
        defaultValue: String?
    ): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = prefKeyId.toString()
        return sharedPreferences.getString(prefKey, defaultValue)
    }

    private fun getBooleanPref(
        context: Context,
        @StringRes prefKeyId: Int,
        defaultValue: Boolean
    ): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(prefKeyId), defaultValue)
}