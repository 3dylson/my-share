package pt.ms.myshare.data.repository

import android.os.SystemClock
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.R
import pt.ms.myshare.data.remote.FirebaseRemoteConfigKeys
import pt.ms.myshare.data.remote.RemoteProductConfigMapper
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.repository.ProductConfigRepository
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseProductConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : ProductConfigRepository {

    private val configState = MutableStateFlow(ProductExperienceConfig())
    private val refreshMutex = Mutex()
    private var lastRefreshAttemptAtMillis = 0L

    override val config: StateFlow<ProductExperienceConfig> = configState.asStateFlow()

    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchIntervalSeconds())
                .build()
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    override suspend fun refresh() {
        refreshMutex.withLock {
            val now = SystemClock.elapsedRealtime()
            if (now - lastRefreshAttemptAtMillis < LOCAL_REFRESH_GUARD_MILLIS) {
                Timber.tag(TAG).d("Remote Config refresh skipped; recent attempt already completed")
                return
            }
            lastRefreshAttemptAtMillis = now
            refreshInternal()
        }
    }

    private suspend fun refreshInternal() {
        runCatching {
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults).await()
            val activated = remoteConfig.fetchAndActivate().await()
            val updated = remoteConfig.toProductExperienceConfig()
            configState.value = updated
            FirebaseUtils.setCrashlyticsKey("remote_config_paywall_default", updated.paywallDefaultPlan.name)
            FirebaseUtils.setCrashlyticsKey("remote_config_founder_offer", updated.founderOfferEnabled)
            FirebaseUtils.setCrashlyticsKey("remote_config_onboarding_experiment", updated.onboardingConversionExperiment)
            FirebaseUtils.setCrashlyticsKey("remote_config_onboarding_paywall", updated.onboardingPaywallVariant.remoteValue)
            FirebaseUtils.setCrashlyticsKey("remote_config_trial_framing", updated.paywallTrialFraming.remoteValue)
            FirebaseUtils.setCrashlyticsKey("remote_config_onboarding_intro", updated.onboardingIntroVariant.remoteValue)
            FirebaseUtils.setUserProperty("paywall_default_plan", updated.paywallDefaultPlan.name.lowercase())
            FirebaseUtils.setUserProperty("onboarding_exp", updated.onboardingConversionExperiment)
            FirebaseUtils.setUserProperty("trial_framing", updated.paywallTrialFraming.remoteValue)
            FirebaseUtils.setUserProperty("onboarding_intro_variant", updated.onboardingIntroVariant.remoteValue)
            FirebaseUtils.logEvent(
                "remote_config_refreshed",
                android.os.Bundle().apply {
                    putString("paywall_default_plan", updated.paywallDefaultPlan.name.lowercase())
                    putString("onboarding_paywall_variant", updated.onboardingPaywallVariant.remoteValue)
                    putString("premium_proof_variant", updated.premiumProofVariant.remoteValue)
                    putString("onboarding_experiment", updated.onboardingConversionExperiment)
                    putString("paywall_trial_framing", updated.paywallTrialFraming.remoteValue)
                    putString("onboarding_intro_variant", updated.onboardingIntroVariant.remoteValue)
                    putString("source", if (activated) "activated" else "cached")
                }
            )
            Timber.tag(TAG).d(
                "Remote Config refreshed activated=%s paywallDefault=%s founderOffer=%s reminders=%s",
                activated,
                updated.paywallDefaultPlan,
                updated.founderOfferEnabled,
                updated.premiumRemindersEnabled
            )
        }.onFailure { error ->
            FirebaseUtils.setCrashlyticsKey("remote_config_last_error", error::class.java.simpleName)
            Timber.tag(TAG).e(error, "Remote Config refresh failed; using in-app defaults")
        }
    }

    private fun FirebaseRemoteConfig.toProductExperienceConfig(): ProductExperienceConfig {
        return RemoteProductConfigMapper.fromValues(
            paywallDefaultPlan = getString(FirebaseRemoteConfigKeys.PAYWALL_DEFAULT_PLAN),
            onboardingPaywallVariant = getString(FirebaseRemoteConfigKeys.ONBOARDING_PAYWALL_VARIANT),
            founderOfferEnabled = getBoolean(FirebaseRemoteConfigKeys.FOUNDER_OFFER_ENABLED),
            premiumRemindersEnabled = getBoolean(FirebaseRemoteConfigKeys.PREMIUM_REMINDERS_ENABLED),
            premiumProofVariant = getString(FirebaseRemoteConfigKeys.PREMIUM_PROOF_VARIANT),
            onboardingConversionExperiment = getString(FirebaseRemoteConfigKeys.ONBOARDING_CONVERSION_EXPERIMENT),
            paywallTrialFraming = getString(FirebaseRemoteConfigKeys.PAYWALL_TRIAL_FRAMING),
            onboardingIntroVariant = getString(FirebaseRemoteConfigKeys.ONBOARDING_INTRO_VARIANT)
        )
    }

    private fun fetchIntervalSeconds(): Long {
        return if (BuildConfig.DEBUG) DEBUG_FETCH_INTERVAL_SECONDS else RELEASE_FETCH_INTERVAL_SECONDS
    }

    private companion object {
        private const val TAG = "ProductConfig"
        private const val DEBUG_FETCH_INTERVAL_SECONDS = 0L
        private const val RELEASE_FETCH_INTERVAL_SECONDS = 21_600L
        private const val LOCAL_REFRESH_GUARD_MILLIS = 10_000L
    }
}
