package pt.ms.myshare.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import java.util.*

/** Utility class to retrieve shared preferences. */
object PreferenceUtils {

    fun getCurrency(): Locale = Locale.FRANCE

    fun saveStringPreference(context: Context, prefKeyId: Int, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(prefKeyId.toString(), value)
            .apply()
    }

    fun saveIntPreference(context: Context, @StringRes prefKeyId: Int, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(context.getString(prefKeyId), value)
            .apply()
    }

    private fun getIntPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyId)
        return sharedPreferences.getInt(prefKey, defaultValue)
    }

    fun getStringPref(
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