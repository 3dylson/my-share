package pt.ms.myshare.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ms.myshare.domain.repository.FirstRunExperienceRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedFirstRunExperienceRepository internal constructor(
    private val prefs: SharedPreferences
) : FirstRunExperienceRepository {

    @Inject constructor(
        @ApplicationContext context: Context
    ) : this(PreferenceManager.getDefaultSharedPreferences(context))

    override fun isHomeCoachMarksPending(): Boolean {
        return prefs.getBoolean(KEY_HOME_COACH_MARKS_PENDING, false)
    }

    override suspend fun setHomeCoachMarksPending(pending: Boolean) = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("Home coach marks pending updated: %s", pending)
        prefs.edit().putBoolean(KEY_HOME_COACH_MARKS_PENDING, pending).apply()
    }

    private companion object {
        const val TAG = "FirstRunExperience"
        const val KEY_HOME_COACH_MARKS_PENDING = "home_coach_marks_pending"
    }
}
