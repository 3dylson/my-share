package pt.ms.myshare.presentation.ui.ads

import android.content.Context
import android.content.SharedPreferences
import pt.ms.myshare.domain.model.AdExposureSnapshot
import pt.ms.myshare.domain.model.AdFormat
import pt.ms.myshare.domain.model.AdPlacement
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class AdExposureStore(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun incrementSessionCount(): Int {
        val sessions = preferences.getInt(KEY_SESSION_COUNT, 0) + 1
        preferences.edit().putInt(KEY_SESSION_COUNT, sessions).apply()
        return sessions
    }

    fun rolloutBucket(): Int {
        val existing = preferences.getInt(KEY_ROLLOUT_BUCKET, UNKNOWN_BUCKET)
        if (existing in BUCKET_MIN..BUCKET_MAX) return existing
        val generated = Random.nextInt(BUCKET_MIN, BUCKET_MAX + 1)
        preferences.edit().putInt(KEY_ROLLOUT_BUCKET, generated).apply()
        return generated
    }

    fun snapshot(currentTimeMillis: Long = System.currentTimeMillis()): AdExposureSnapshot {
        ensureToday()
        return AdExposureSnapshot(
            currentTimeMillis = currentTimeMillis,
            lastAppOpenShownAtMillis = preferences.getLongOrNull(KEY_LAST_APP_OPEN_SHOWN_AT),
            lastNonBannerShownAtMillis = preferences.getLongOrNull(KEY_LAST_NON_BANNER_SHOWN_AT),
            interstitialImpressionsToday = preferences.getInt(KEY_INTERSTITIAL_DAILY_COUNT, 0),
            rewardedImpressionsToday = preferences.getInt(KEY_REWARDED_DAILY_COUNT, 0),
            nativeImpressionsToday = preferences.getInt(KEY_NATIVE_DAILY_COUNT, 0),
            nonBannerImpressionsToday = preferences.getInt(KEY_NON_BANNER_DAILY_COUNT, 0)
        )
    }

    fun recordImpression(placement: AdPlacement, currentTimeMillis: Long = System.currentTimeMillis()) {
        ensureToday()
        preferences.edit().apply {
            when (placement.format) {
                AdFormat.APP_OPEN -> {
                    putLong(KEY_LAST_APP_OPEN_SHOWN_AT, currentTimeMillis)
                    incrementInt(KEY_NON_BANNER_DAILY_COUNT)
                    putLong(KEY_LAST_NON_BANNER_SHOWN_AT, currentTimeMillis)
                }
                AdFormat.INTERSTITIAL -> {
                    incrementInt(KEY_INTERSTITIAL_DAILY_COUNT)
                    incrementInt(KEY_NON_BANNER_DAILY_COUNT)
                    putLong(KEY_LAST_NON_BANNER_SHOWN_AT, currentTimeMillis)
                }
                AdFormat.REWARDED -> {
                    incrementInt(KEY_REWARDED_DAILY_COUNT)
                    incrementInt(KEY_NON_BANNER_DAILY_COUNT)
                    putLong(KEY_LAST_NON_BANNER_SHOWN_AT, currentTimeMillis)
                }
                AdFormat.NATIVE -> incrementInt(KEY_NATIVE_DAILY_COUNT)
                AdFormat.BANNER -> Unit
            }
        }.apply()
    }

    fun recordRewardGrant(currentTimeMillis: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong(KEY_EXTRA_GOAL_REWARD_EXPIRES_AT, currentTimeMillis + EXTRA_GOAL_REWARD_WINDOW_MILLIS)
            .apply()
    }

    fun hasActiveExtraGoalReward(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return preferences.getLong(KEY_EXTRA_GOAL_REWARD_EXPIRES_AT, 0L) > currentTimeMillis
    }

    fun consumeExtraGoalReward() {
        preferences.edit().remove(KEY_EXTRA_GOAL_REWARD_EXPIRES_AT).apply()
    }

    private fun ensureToday() {
        val today = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.BASIC_ISO_DATE)
        if (preferences.getString(KEY_DAILY_BUCKET, null) == today) return
        preferences.edit()
            .putString(KEY_DAILY_BUCKET, today)
            .putInt(KEY_INTERSTITIAL_DAILY_COUNT, 0)
            .putInt(KEY_REWARDED_DAILY_COUNT, 0)
            .putInt(KEY_NATIVE_DAILY_COUNT, 0)
            .putInt(KEY_NON_BANNER_DAILY_COUNT, 0)
            .apply()
    }

    private fun SharedPreferences.getLongOrNull(key: String): Long? {
        val value = getLong(key, NO_TIMESTAMP)
        return value.takeIf { it > 0L }
    }

    private fun SharedPreferences.Editor.incrementInt(key: String) {
        putInt(key, preferences.getInt(key, 0) + 1)
    }

    private companion object {
        private const val PREFERENCES_NAME = "myshare_ads"
        private const val KEY_SESSION_COUNT = "session_count"
        private const val KEY_ROLLOUT_BUCKET = "rollout_bucket"
        private const val KEY_DAILY_BUCKET = "daily_bucket"
        private const val KEY_LAST_APP_OPEN_SHOWN_AT = "last_app_open_shown_at"
        private const val KEY_LAST_NON_BANNER_SHOWN_AT = "last_non_banner_shown_at"
        private const val KEY_INTERSTITIAL_DAILY_COUNT = "interstitial_daily_count"
        private const val KEY_REWARDED_DAILY_COUNT = "rewarded_daily_count"
        private const val KEY_NATIVE_DAILY_COUNT = "native_daily_count"
        private const val KEY_NON_BANNER_DAILY_COUNT = "non_banner_daily_count"
        private const val KEY_EXTRA_GOAL_REWARD_EXPIRES_AT = "extra_goal_reward_expires_at"
        private const val UNKNOWN_BUCKET = -1
        private const val BUCKET_MIN = 0
        private const val BUCKET_MAX = 99
        private const val NO_TIMESTAMP = 0L
        private const val EXTRA_GOAL_REWARD_WINDOW_MILLIS = 24 * 60 * 60 * 1000L
    }
}
