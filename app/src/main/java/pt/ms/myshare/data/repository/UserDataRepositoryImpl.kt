package pt.ms.myshare.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pt.ms.myshare.R
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.edit_profile.EditProfileState
import timber.log.Timber
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserDataRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getUserData(): Flow<EditProfileState> = flow {
        Timber.d("getUserData() called")
        val netSalary = prefs.getString(context.getString(R.string.pref_key_net_salary_value), "") ?: ""
        val netSalaryPercentage = prefs.getString(context.getString(R.string.pref_key_investments_savings_percentage_value), "") ?: ""
        val stockPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_stocks_value), "") ?: ""
        val cryptoPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_crypto_value), "") ?: ""
        val savingsPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_savings_value), "") ?: ""

        val state = EditProfileState(
            netSalary = netSalary,
            netSalaryPercentage = netSalaryPercentage,
            stockPercentage = stockPercentage,
            cryptoPercentage = cryptoPercentage,
            savingsPercentage = savingsPercentage
        )
        emit(state)
        Timber.d("getUserData() success: $state")
    }

    override suspend fun saveUserData(data: EditProfileState) {
        Timber.d("saveUserData() called with: $data")
        val editor = prefs.edit()
        if (data.netSalary.isEmpty()) {
            editor.remove(context.getString(R.string.pref_key_net_salary_value))
        } else {
            editor.putString(context.getString(R.string.pref_key_net_salary_value), data.netSalary)
        }
        if (data.netSalaryPercentage.isEmpty()) {
            editor.remove(context.getString(R.string.pref_key_investments_savings_percentage_value))
        } else {
            editor.putString(context.getString(R.string.pref_key_investments_savings_percentage_value), data.netSalaryPercentage)
        }
        if (data.stockPercentage.isEmpty()) {
            editor.remove(context.getString(R.string.pref_key_percentage_stocks_value))
        } else {
            editor.putString(context.getString(R.string.pref_key_percentage_stocks_value), data.stockPercentage)
        }
        if (data.cryptoPercentage.isEmpty()) {
            editor.remove(context.getString(R.string.pref_key_percentage_crypto_value))
        } else {
            editor.putString(context.getString(R.string.pref_key_percentage_crypto_value), data.cryptoPercentage)
        }
        if (data.savingsPercentage.isEmpty()) {
            editor.remove(context.getString(R.string.pref_key_percentage_savings_value))
        } else {
            editor.putString(context.getString(R.string.pref_key_percentage_savings_value), data.savingsPercentage)
        }
        val result = editor.commit()
        Timber.d("saveUserData() commit result: $result")
    }
}