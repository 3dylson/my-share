package pt.ms.myshare.data.grant

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LegacyPremiumGrantEligibilityStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun captureInstallEligibilitySnapshot() {
        if (!prefs.getBoolean(KEY_EVALUATED, false)) {
            val eligible = prefs.getBoolean(KEY_PLANNER_ONBOARDING_COMPLETED, false) ||
                prefs.contains(KEY_PLANNER_NET_INCOME)
            prefs.edit {
                putBoolean(KEY_EVALUATED, true)
                putBoolean(KEY_ELIGIBLE, eligible)
            }
        }
    }

    fun evaluateEligibility(): Boolean {
        if (!prefs.getBoolean(KEY_EVALUATED, false)) {
            return false
        }
        return prefs.getBoolean(KEY_ELIGIBLE, false) &&
            !prefs.getBoolean(KEY_CLAIMED, false) &&
            !prefs.getBoolean(KEY_DISMISSED, false)
    }

    fun markClaimed() {
        prefs.edit {
            putBoolean(KEY_CLAIMED, true)
            putBoolean(KEY_DISMISSED, false)
        }
    }

    fun markDismissed() {
        prefs.edit { putBoolean(KEY_DISMISSED, true) }
    }

    private companion object {
        const val KEY_EVALUATED = "legacy_premium_grant_evaluated_2026_05"
        const val KEY_ELIGIBLE = "legacy_premium_grant_eligible_2026_05"
        const val KEY_CLAIMED = "legacy_premium_grant_claimed_2026_05"
        const val KEY_DISMISSED = "legacy_premium_grant_dismissed_2026_05"
        const val KEY_PLANNER_ONBOARDING_COMPLETED = "planner_onboarding_completed"
        const val KEY_PLANNER_NET_INCOME = "planner_net_income"
    }
}
