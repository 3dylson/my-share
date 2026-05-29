package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AdEligibilityContext
import pt.ms.myshare.domain.model.AdEligibilityDecision
import pt.ms.myshare.domain.model.AdExposureSnapshot
import pt.ms.myshare.domain.model.AdFormat
import pt.ms.myshare.domain.model.AdsExperimentVariant
import pt.ms.myshare.domain.model.ProductExperienceConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EvaluateAdEligibilityUseCase @Inject constructor() {

    operator fun invoke(
        context: AdEligibilityContext,
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        if (context.isPremium) return AdEligibilityDecision.ineligible("premium")
        if (!context.hasFirstPlan) return AdEligibilityDecision.ineligible("no_first_plan")
        if (!context.canRequestAds) return AdEligibilityDecision.ineligible("consent_unavailable")
        if (context.isKeyboardVisible) return AdEligibilityDecision.ineligible("keyboard_visible")
        if (context.isBlockedFlowActive) return AdEligibilityDecision.ineligible("blocked_flow")
        if (config.adsExperimentVariant == AdsExperimentVariant.CONTROL) {
            return AdEligibilityDecision.ineligible("variant_control")
        }
        if (context.rolloutBucket !in 0 until config.adsRolloutPercent) {
            return AdEligibilityDecision.ineligible("rollout")
        }

        return when (context.placement.format) {
            AdFormat.BANNER -> bannerDecision(config)
            AdFormat.NATIVE -> nativeDecision(exposure, config)
            AdFormat.APP_OPEN -> appOpenDecision(context, exposure, config)
            AdFormat.INTERSTITIAL -> interstitialDecision(context, exposure, config)
            AdFormat.REWARDED -> rewardedDecision(context, exposure, config)
        }
    }

    private fun bannerDecision(config: ProductExperienceConfig): AdEligibilityDecision {
        return if (config.adsAnchoredBannerEnabled) {
            AdEligibilityDecision.eligible()
        } else {
            AdEligibilityDecision.ineligible("format_disabled")
        }
    }

    private fun nativeDecision(
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        if (!config.adsNativeMoreEnabled) return AdEligibilityDecision.ineligible("format_disabled")
        if (exposure.nativeImpressionsToday >= config.adsNativeDailyCap) {
            return AdEligibilityDecision.ineligible("native_daily_cap")
        }
        return AdEligibilityDecision.eligible()
    }

    private fun appOpenDecision(
        context: AdEligibilityContext,
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        if (!config.adsAppOpenEnabled) return AdEligibilityDecision.ineligible("format_disabled")
        if (context.isNotificationLaunch) return AdEligibilityDecision.ineligible("notification_deeplink")
        if (context.sessionCount < config.adsMinSessionsAppOpen) {
            return AdEligibilityDecision.ineligible("min_sessions")
        }
        val cooldownMillis = TimeUnit.HOURS.toMillis(config.adsAppOpenCooldownHours.toLong())
        val lastShownAt = exposure.lastAppOpenShownAtMillis
        if (lastShownAt != null && exposure.currentTimeMillis - lastShownAt < cooldownMillis) {
            return AdEligibilityDecision.ineligible("cooldown")
        }
        return nonBannerCapDecision(exposure, config)
    }

    private fun interstitialDecision(
        context: AdEligibilityContext,
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        if (!config.adsInterstitialEnabled) return AdEligibilityDecision.ineligible("format_disabled")
        if (!context.isCompletedAction) return AdEligibilityDecision.ineligible("not_completed_action")
        if (exposure.interstitialImpressionsToday >= config.adsInterstitialDailyCap) {
            return AdEligibilityDecision.ineligible("interstitial_daily_cap")
        }
        if (isInsideNonBannerSpacing(exposure)) {
            return AdEligibilityDecision.ineligible("non_banner_spacing")
        }
        return nonBannerCapDecision(exposure, config)
    }

    private fun rewardedDecision(
        context: AdEligibilityContext,
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        if (!config.adsRewardedEnabled) return AdEligibilityDecision.ineligible("format_disabled")
        if (!context.hasExplicitRewardOptIn) return AdEligibilityDecision.ineligible("reward_not_opted_in")
        if (exposure.rewardedImpressionsToday >= config.adsRewardedDailyCap) {
            return AdEligibilityDecision.ineligible("rewarded_daily_cap")
        }
        return nonBannerCapDecision(exposure, config)
    }

    private fun nonBannerCapDecision(
        exposure: AdExposureSnapshot,
        config: ProductExperienceConfig
    ): AdEligibilityDecision {
        return if (exposure.nonBannerImpressionsToday >= config.adsNonBannerDailyCap) {
            AdEligibilityDecision.ineligible("non_banner_daily_cap")
        } else {
            AdEligibilityDecision.eligible()
        }
    }

    private fun isInsideNonBannerSpacing(exposure: AdExposureSnapshot): Boolean {
        val lastShownAt = exposure.lastNonBannerShownAtMillis ?: return false
        return exposure.currentTimeMillis - lastShownAt < MIN_NON_BANNER_SPACING_MILLIS
    }

    private companion object {
        private val MIN_NON_BANNER_SPACING_MILLIS = TimeUnit.MINUTES.toMillis(3)
    }
}
