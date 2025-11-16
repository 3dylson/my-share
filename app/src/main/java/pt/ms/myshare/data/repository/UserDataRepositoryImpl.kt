package pt.ms.myshare.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pt.ms.myshare.R
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.edit_profile.EditProfileState
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserDataRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getUserData(): Flow<EditProfileState> = flow {
        val username = prefs.getString(context.getString(R.string.id_username), "") ?: ""
        val netSalary = prefs.getString(context.getString(R.string.pref_key_net_salary_value), "") ?: ""
        val netSalaryPercentage = prefs.getString(context.getString(R.string.pref_key_investments_savings_percentage_value), "") ?: ""
        val stockPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_stocks_value), "") ?: ""
        val cryptoPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_crypto_value), "") ?: ""
        val savingsPercentage = prefs.getString(context.getString(R.string.pref_key_percentage_savings_value), "") ?: ""

        emit(EditProfileState(
            username = username,
            netSalary = netSalary,
            netSalaryPercentage = netSalaryPercentage,
            stockPercentage = stockPercentage,
            cryptoPercentage = cryptoPercentage,
            savingsPercentage = savingsPercentage
        ))
    }

    override suspend fun saveUserData(data: EditProfileState) {
        prefs.edit().apply {
            putString(context.getString(R.string.id_username), data.username)
            putString(context.getString(R.string.pref_key_net_salary_value), data.netSalary)
            putString(context.getString(R.string.pref_key_investments_savings_percentage_value), data.netSalaryPercentage)
            putString(context.getString(R.string.pref_key_percentage_stocks_value), data.stockPercentage)
            putString(context.getString(R.string.pref_key_percentage_crypto_value), data.cryptoPercentage)
            putString(context.getString(R.string.pref_key_percentage_savings_value), data.savingsPercentage)
            apply()
        }
    }
}