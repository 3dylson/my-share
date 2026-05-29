package pt.ms.myshare.presentation.ui.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.AdEligibilityContext
import pt.ms.myshare.domain.model.AdEligibilityDecision
import pt.ms.myshare.domain.model.AdPlacement
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.use_case.EvaluateAdEligibilityUseCase
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class AdsOrchestrator(
    private val activity: Activity,
    private val consentManager: AdsConsentManager,
    private val exposureStore: AdExposureStore = AdExposureStore(activity),
    private val evaluateAdEligibilityUseCase: EvaluateAdEligibilityUseCase = EvaluateAdEligibilityUseCase(),
    private val analyticsLogger: AdsAnalyticsLogger = AdsAnalyticsLogger()
) {
    private var productConfig = ProductExperienceConfig()
    private var canRequestAds = consentManager.canRequestAds
    private var sessionCount = 1
    private var isNotificationLaunch = false
    private val _adsStateVersion = MutableStateFlow(0)
    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val loggedEligibilityKeys = mutableSetOf<String>()

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var isAppOpenShowing = false
    private var appOpenLoadTimeMillis = 0L
    private var attemptedAppOpenThisSession = false
    private var showAppOpenWhenLoaded = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    val adsStateVersion: StateFlow<Int> = _adsStateVersion.asStateFlow()

    fun updateConfig(config: ProductExperienceConfig) {
        productConfig = config
        bumpAdsStateVersion()
        Timber.tag(TAG).d(
            "Ads config updated variant=%s rollout=%d banner=%s native=%s appOpen=%s interstitial=%s rewarded=%s",
            config.adsExperimentVariant.remoteValue,
            config.adsRolloutPercent,
            config.adsAnchoredBannerEnabled,
            config.adsNativeMoreEnabled,
            config.adsAppOpenEnabled,
            config.adsInterstitialEnabled,
            config.adsRewardedEnabled
        )
    }

    fun updateConsent(canRequest: Boolean) {
        canRequestAds = canRequest
        bumpAdsStateVersion()
        Timber.tag(TAG).d("Ads consent updated canRequestAds=%s", canRequest)
    }

    fun updateSessionCount(count: Int) {
        sessionCount = count.coerceAtLeast(1)
        bumpAdsStateVersion()
    }

    fun incrementSessionCount(): Int {
        val count = exposureStore.incrementSessionCount()
        updateSessionCount(count)
        return count
    }

    fun markNotificationLaunch(isNotification: Boolean) {
        if (isNotification) {
            isNotificationLaunch = true
            bumpAdsStateVersion()
            Timber.tag(TAG).d("Ads app-open disabled for this session because app was opened from notification")
        }
    }

    fun prepareEligibleFreeSession(isPremium: Boolean, hasFirstPlan: Boolean) {
        updateConsent(consentManager.canRequestAds)
        if (!canShowPlacement(
                placement = AdPlacement.HOME_ANCHORED_BANNER,
                isPremium = isPremium,
                hasFirstPlan = hasFirstPlan
            )
        ) {
            return
        }
        initializeMobileAdsIfNeeded()
        preloadInterstitialIfEligible(isPremium = isPremium, hasFirstPlan = hasFirstPlan)
        preloadAppOpenIfEligible(isPremium = isPremium, hasFirstPlan = hasFirstPlan)
        Timber.tag(TAG).d("Prepared ads for eligible free session")
    }

    fun showAppOpenIfEligible(activity: Activity, isPremium: Boolean, hasFirstPlan: Boolean) {
        if (attemptedAppOpenThisSession) return
        val decision = evaluate(
            placement = AdPlacement.APP_OPEN,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan
        )
        if (!decision.isEligible) return
        if (adUnitIdFor(AdPlacement.APP_OPEN).isBlank()) {
            logEligibility(AdEligibilityDecision.ineligible("ad_unit_missing"), AdPlacement.APP_OPEN, isPremium)
            return
        }

        attemptedAppOpenThisSession = true
        initializeMobileAdsIfNeeded()
        val ad = appOpenAd
        if (ad != null && isAppOpenFresh()) {
            showAppOpen(activity, ad, isPremium)
        } else {
            showAppOpenWhenLoaded = true
            loadAppOpenAd(activity, isPremium, hasFirstPlan, showAfterLoad = true)
        }
    }

    fun showInterstitialAfterCompletedAction(
        activity: Activity,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        onFinished: () -> Unit = {}
    ) {
        val decision = evaluate(
            placement = AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan,
            isCompletedAction = true
        )
        if (!decision.isEligible || adUnitIdFor(AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL).isBlank()) {
            onFinished()
            return
        }

        initializeMobileAdsIfNeeded()
        interstitialAd?.let { ad ->
            showInterstitial(activity, ad, isPremium, onFinished)
            return
        }
        loadInterstitial(activity, isPremium, hasFirstPlan, showAfterLoad = true, onFinished = onFinished)
    }

    fun showRewardedExtraGoal(
        activity: Activity,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        onRewardGranted: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val placement = AdPlacement.REWARDED_EXTRA_GOAL
        val decision = evaluate(
            placement = placement,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan,
            hasExplicitRewardOptIn = true
        )
        if (!decision.isEligible || adUnitIdFor(placement).isBlank()) {
            logEligibility(AdEligibilityDecision.ineligible("ad_unit_missing"), placement, isPremium)
            onUnavailable()
            return
        }

        initializeMobileAdsIfNeeded()
        analyticsLogger.event(
            eventName = "rewarded_ad_started",
            placement = placement,
            variant = productConfig.adsExperimentVariant,
            sessionCount = sessionCount,
            isPremium = isPremium
        )
        rewardedAd?.let { ad ->
            showRewarded(activity, ad, isPremium, onRewardGranted, onUnavailable)
            return
        }
        loadRewarded(activity, isPremium, onRewardGranted, onUnavailable)
    }

    fun canShowPlacement(
        placement: AdPlacement,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        isKeyboardVisible: Boolean = false,
        isBlockedFlowActive: Boolean = false,
        isCompletedAction: Boolean = false,
        hasExplicitRewardOptIn: Boolean = false
    ): Boolean {
        if (adUnitIdFor(placement).isBlank()) {
            logEligibility(AdEligibilityDecision.ineligible("ad_unit_missing"), placement, isPremium)
            return false
        }
        return evaluate(
            placement = placement,
            isPremium = isPremium,
            hasFirstPlan = hasFirstPlan,
            isKeyboardVisible = isKeyboardVisible,
            isBlockedFlowActive = isBlockedFlowActive,
            isCompletedAction = isCompletedAction,
            hasExplicitRewardOptIn = hasExplicitRewardOptIn
        ).isEligible
    }

    fun createAdRequest(): AdRequest {
        return AdRequest.Builder()
            .addNetworkExtrasBundle(
                AdMobAdapter::class.java,
                Bundle().apply { putString("npa", "1") }
            )
            .build()
    }

    fun adUnitIdFor(placement: AdPlacement): String {
        val idRes = when (placement) {
            AdPlacement.HOME_ANCHORED_BANNER,
            AdPlacement.MORE_ANCHORED_BANNER -> R.string.admob_banner_ad_unit_id
            AdPlacement.MORE_NATIVE_CARD -> R.string.admob_native_ad_unit_id
            AdPlacement.APP_OPEN -> R.string.admob_app_open_ad_unit_id
            AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL -> R.string.admob_interstitial_ad_unit_id
            AdPlacement.REWARDED_EXTRA_GOAL -> R.string.admob_rewarded_ad_unit_id
        }
        return activity.getString(idRes)
    }

    fun recordAdRequestStarted(placement: AdPlacement, isPremium: Boolean = false) {
        analyticsLogger.event("ad_request_started", placement, productConfig.adsExperimentVariant, sessionCount, isPremium)
    }

    fun recordAdLoaded(placement: AdPlacement, isPremium: Boolean = false) {
        analyticsLogger.event("ad_loaded", placement, productConfig.adsExperimentVariant, sessionCount, isPremium)
    }

    fun recordAdLoadFailed(placement: AdPlacement, reason: String, isPremium: Boolean = false) {
        analyticsLogger.event("ad_load_failed", placement, productConfig.adsExperimentVariant, sessionCount, isPremium, reason)
    }

    fun recordAdImpression(placement: AdPlacement, isPremium: Boolean = false) {
        exposureStore.recordImpression(placement)
        analyticsLogger.event("ad_impression", placement, productConfig.adsExperimentVariant, sessionCount, isPremium)
    }

    fun recordAdClicked(placement: AdPlacement, isPremium: Boolean = false) {
        analyticsLogger.event("ad_clicked", placement, productConfig.adsExperimentVariant, sessionCount, isPremium)
    }

    fun recordAdHidden(placement: AdPlacement, reason: String, isPremium: Boolean = false) {
        analyticsLogger.event("ad_hidden", placement, productConfig.adsExperimentVariant, sessionCount, isPremium, reason)
    }

    fun recordAdFreePremiumUpsellViewed(placement: AdPlacement) {
        analyticsLogger.event(
            eventName = "ad_free_premium_upsell_viewed",
            placement = placement,
            variant = productConfig.adsExperimentVariant,
            sessionCount = sessionCount,
            isPremium = false
        )
    }

    fun hasActiveExtraGoalReward(): Boolean = exposureStore.hasActiveExtraGoalReward()

    fun consumeExtraGoalReward() {
        exposureStore.consumeExtraGoalReward()
    }

    private fun evaluate(
        placement: AdPlacement,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        isKeyboardVisible: Boolean = false,
        isBlockedFlowActive: Boolean = false,
        isCompletedAction: Boolean = false,
        hasExplicitRewardOptIn: Boolean = false
    ): AdEligibilityDecision {
        val decision = evaluateAdEligibilityUseCase(
            context = AdEligibilityContext(
                placement = placement,
                isPremium = isPremium,
                hasFirstPlan = hasFirstPlan,
                canRequestAds = canRequestAds,
                sessionCount = sessionCount,
                rolloutBucket = exposureStore.rolloutBucket(),
                isKeyboardVisible = isKeyboardVisible,
                isNotificationLaunch = isNotificationLaunch,
                isBlockedFlowActive = isBlockedFlowActive,
                isCompletedAction = isCompletedAction,
                hasExplicitRewardOptIn = hasExplicitRewardOptIn
            ),
            exposure = exposureStore.snapshot(),
            config = productConfig
        )
        logEligibility(decision, placement, isPremium)
        return decision
    }

    private fun logEligibility(
        decision: AdEligibilityDecision,
        placement: AdPlacement,
        isPremium: Boolean
    ) {
        val key = listOf(
            placement.analyticsName,
            decision.isEligible,
            decision.reason,
            sessionCount,
            isPremium
        ).joinToString("|")
        if (!loggedEligibilityKeys.add(key)) return
        analyticsLogger.eligibility(decision, placement, productConfig.adsExperimentVariant, sessionCount, isPremium)
        Timber.tag(TAG).d(
            "Ad eligibility placement=%s eligible=%s reason=%s session=%d premium=%s",
            placement.analyticsName,
            decision.isEligible,
            decision.reason,
            sessionCount,
            isPremium
        )
    }

    private fun initializeMobileAdsIfNeeded() {
        if (!isMobileAdsInitialized.compareAndSet(false, true)) return
        MobileAds.initialize(activity) {
            Timber.tag(TAG).d("Mobile Ads SDK initialized")
        }
    }

    private fun bumpAdsStateVersion() {
        _adsStateVersion.value = _adsStateVersion.value + 1
    }

    private fun preloadAppOpenIfEligible(isPremium: Boolean, hasFirstPlan: Boolean) {
        if (!canShowPlacement(AdPlacement.APP_OPEN, isPremium, hasFirstPlan)) return
        loadAppOpenAd(activity, isPremium, hasFirstPlan, showAfterLoad = false)
    }

    private fun loadAppOpenAd(
        context: Context,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        showAfterLoad: Boolean
    ) {
        if (isAppOpenLoading) {
            if (showAfterLoad) showAppOpenWhenLoaded = true
            return
        }
        if (appOpenAd != null && isAppOpenFresh()) return
        val placement = AdPlacement.APP_OPEN
        val adUnitId = adUnitIdFor(placement)
        if (adUnitId.isBlank()) return
        isAppOpenLoading = true
        recordAdRequestStarted(placement, isPremium)
        AppOpenAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenLoadTimeMillis = System.currentTimeMillis()
                    isAppOpenLoading = false
                    recordAdLoaded(placement, isPremium)
                    Timber.tag(TAG).d("App-open ad loaded")
                    val shouldShowLoadedAd = showAfterLoad || showAppOpenWhenLoaded
                    showAppOpenWhenLoaded = false
                    if (shouldShowLoadedAd && evaluate(placement, isPremium, hasFirstPlan).isEligible) {
                        showAppOpen(activity, ad, isPremium)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    showAppOpenWhenLoaded = false
                    recordAdLoadFailed(placement, loadAdError.message, isPremium)
                    Timber.tag(TAG).w("App-open ad failed to load: %s", loadAdError.message)
                }
            }
        )
    }

    private fun showAppOpen(activity: Activity, ad: AppOpenAd, isPremium: Boolean) {
        if (isAppOpenShowing) return
        val placement = AdPlacement.APP_OPEN
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isAppOpenShowing = true
                recordAdImpression(placement, isPremium)
                Timber.tag(TAG).d("App-open ad shown")
            }

            override fun onAdClicked() {
                recordAdClicked(placement, isPremium)
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isAppOpenShowing = false
                recordAdHidden(placement, "dismissed", isPremium)
                Timber.tag(TAG).d("App-open ad dismissed")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isAppOpenShowing = false
                recordAdLoadFailed(placement, adError.message, isPremium)
                Timber.tag(TAG).w("App-open ad failed to show: %s", adError.message)
            }
        }
        ad.show(activity)
    }

    private fun isAppOpenFresh(): Boolean {
        return appOpenAd != null &&
            System.currentTimeMillis() - appOpenLoadTimeMillis < APP_OPEN_EXPIRATION_MILLIS
    }

    private fun preloadInterstitialIfEligible(isPremium: Boolean, hasFirstPlan: Boolean) {
        if (!productConfig.adsInterstitialEnabled) return
        if (!hasFirstPlan || isPremium || !canRequestAds) return
        loadInterstitial(activity, isPremium, hasFirstPlan, showAfterLoad = false)
    }

    private fun loadInterstitial(
        context: Context,
        isPremium: Boolean,
        hasFirstPlan: Boolean,
        showAfterLoad: Boolean,
        onFinished: () -> Unit = {}
    ) {
        if (interstitialAd != null || isInterstitialLoading) {
            onFinished()
            return
        }
        val placement = AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL
        val adUnitId = adUnitIdFor(placement)
        if (adUnitId.isBlank()) {
            onFinished()
            return
        }
        isInterstitialLoading = true
        recordAdRequestStarted(placement, isPremium)
        InterstitialAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    recordAdLoaded(placement, isPremium)
                    Timber.tag(TAG).d("Interstitial ad loaded")
                    if (showAfterLoad && evaluate(placement, isPremium, hasFirstPlan, isCompletedAction = true).isEligible) {
                        showInterstitial(activity, ad, isPremium, onFinished)
                    } else {
                        onFinished()
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    recordAdLoadFailed(placement, loadAdError.message, isPremium)
                    Timber.tag(TAG).w("Interstitial ad failed to load: %s", loadAdError.message)
                    onFinished()
                }
            }
        )
    }

    private fun showInterstitial(
        activity: Activity,
        ad: InterstitialAd,
        isPremium: Boolean,
        onFinished: () -> Unit
    ) {
        val placement = AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                recordAdImpression(placement, isPremium)
                Timber.tag(TAG).d("Interstitial ad shown")
            }

            override fun onAdClicked() {
                recordAdClicked(placement, isPremium)
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                recordAdHidden(placement, "dismissed", isPremium)
                Timber.tag(TAG).d("Interstitial ad dismissed")
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                recordAdLoadFailed(placement, adError.message, isPremium)
                Timber.tag(TAG).w("Interstitial ad failed to show: %s", adError.message)
                onFinished()
            }
        }
        ad.show(activity)
    }

    private fun loadRewarded(
        context: Context,
        isPremium: Boolean,
        onRewardGranted: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        if (isRewardedLoading) {
            onUnavailable()
            return
        }
        val placement = AdPlacement.REWARDED_EXTRA_GOAL
        val adUnitId = adUnitIdFor(placement)
        if (adUnitId.isBlank()) {
            onUnavailable()
            return
        }
        isRewardedLoading = true
        recordAdRequestStarted(placement, isPremium)
        RewardedAd.load(
            context,
            adUnitId,
            createAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    recordAdLoaded(placement, isPremium)
                    Timber.tag(TAG).d("Rewarded ad loaded")
                    showRewarded(activity, ad, isPremium, onRewardGranted, onUnavailable)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    recordAdLoadFailed(placement, loadAdError.message, isPremium)
                    Timber.tag(TAG).w("Rewarded ad failed to load: %s", loadAdError.message)
                    onUnavailable()
                }
            }
        )
    }

    private fun showRewarded(
        activity: Activity,
        ad: RewardedAd,
        isPremium: Boolean,
        onRewardGranted: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val placement = AdPlacement.REWARDED_EXTRA_GOAL
        var rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                recordAdImpression(placement, isPremium)
                Timber.tag(TAG).d("Rewarded ad shown")
            }

            override fun onAdClicked() {
                recordAdClicked(placement, isPremium)
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                recordAdHidden(placement, "dismissed", isPremium)
                if (!rewardEarned) onUnavailable()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                recordAdLoadFailed(placement, adError.message, isPremium)
                Timber.tag(TAG).w("Rewarded ad failed to show: %s", adError.message)
                onUnavailable()
            }
        }
        ad.show(activity) {
            rewardEarned = true
            exposureStore.recordRewardGrant()
            analyticsLogger.event(
                eventName = "rewarded_ad_completed",
                placement = placement,
                variant = productConfig.adsExperimentVariant,
                sessionCount = sessionCount,
                isPremium = isPremium
            )
            analyticsLogger.event(
                eventName = "reward_granted",
                placement = placement,
                variant = productConfig.adsExperimentVariant,
                sessionCount = sessionCount,
                isPremium = isPremium,
                reason = "extra_goal_24h"
            )
            Timber.tag(TAG).d("Rewarded extra goal grant earned")
            onRewardGranted()
        }
    }

    private companion object {
        private const val TAG = "AdsOrchestrator"
        private const val APP_OPEN_EXPIRATION_MILLIS = 4 * 60 * 60 * 1000L
    }
}
