package pt.ms.myshare.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.domain.repository.EntitlementRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesEntitlementRepository @Inject constructor(
    @ApplicationContext context: Context
) : EntitlementRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val premiumState = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))

    override val isPro: Flow<Boolean> = premiumState.asStateFlow()

    override suspend fun setPro(value: Boolean) {
        Timber.tag(TAG).d("setPro value=%s", value)
        prefs.edit().putBoolean(KEY_IS_PRO, value).apply()
        premiumState.value = value
    }

    override suspend fun restorePurchases() {
        Timber.tag(TAG).d("restorePurchases placeholder current=%s", premiumState.value)
        premiumState.value = prefs.getBoolean(KEY_IS_PRO, false)
    }

    private companion object {
        const val TAG = "EntitlementRepository"
        const val KEY_IS_PRO = "planner_is_pro"
    }
}
