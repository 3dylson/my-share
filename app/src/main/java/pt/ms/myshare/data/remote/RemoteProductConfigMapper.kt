package pt.ms.myshare.data.remote

import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.AdsExperimentVariant
import pt.ms.myshare.domain.model.OnboardingIntroVariant
import pt.ms.myshare.domain.model.OnboardingPaywallVariant
import pt.ms.myshare.domain.model.PaywallTrialFraming
import pt.ms.myshare.domain.model.PremiumProofVariant
import pt.ms.myshare.domain.model.RemoteBillingPlanDefault
import java.util.Locale

object RemoteProductConfigMapper {

    fun fromValues(
        paywallDefaultPlan: String,
        onboardingPaywallVariant: String,
        founderOfferEnabled: Boolean,
        premiumRemindersEnabled: Boolean,
        premiumProofVariant: String,
        onboardingConversionExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String,
        adsExperimentVariant: String,
        adsRolloutPercent: Long,
        adsAnchoredBannerEnabled: Boolean,
        adsNativeMoreEnabled: Boolean,
        adsAppOpenEnabled: Boolean,
        adsInterstitialEnabled: Boolean,
        adsRewardedEnabled: Boolean,
        adsMinSessionsAppOpen: Long,
        adsAppOpenCooldownHours: Long,
        adsInterstitialDailyCap: Long,
        adsRewardedDailyCap: Long,
        adsNativeDailyCap: Long,
        adsNonBannerDailyCap: Long
    ): ProductExperienceConfig {
        return ProductExperienceConfig(
            paywallDefaultPlan = paywallDefaultPlan.toRemoteBillingPlanDefault(),
            onboardingPaywallVariant = onboardingPaywallVariant.toOnboardingPaywallVariant(),
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant.toPremiumProofVariant(),
            onboardingConversionExperiment = onboardingConversionExperiment.toExperimentName(),
            paywallTrialFraming = paywallTrialFraming.toPaywallTrialFraming(),
            onboardingIntroVariant = onboardingIntroVariant.toOnboardingIntroVariant(),
            adsExperimentVariant = adsExperimentVariant.toAdsExperimentVariant(),
            adsRolloutPercent = adsRolloutPercent.toPercent(),
            adsAnchoredBannerEnabled = adsAnchoredBannerEnabled,
            adsNativeMoreEnabled = adsNativeMoreEnabled,
            adsAppOpenEnabled = adsAppOpenEnabled,
            adsInterstitialEnabled = adsInterstitialEnabled,
            adsRewardedEnabled = adsRewardedEnabled,
            adsMinSessionsAppOpen = adsMinSessionsAppOpen.toPositiveInt(ProductExperienceConfig.DEFAULT_ADS_MIN_SESSIONS_APP_OPEN),
            adsAppOpenCooldownHours = adsAppOpenCooldownHours.toPositiveInt(ProductExperienceConfig.DEFAULT_ADS_APP_OPEN_COOLDOWN_HOURS),
            adsInterstitialDailyCap = adsInterstitialDailyCap.toNonNegativeInt(ProductExperienceConfig.DEFAULT_ADS_INTERSTITIAL_DAILY_CAP),
            adsRewardedDailyCap = adsRewardedDailyCap.toNonNegativeInt(ProductExperienceConfig.DEFAULT_ADS_REWARDED_DAILY_CAP),
            adsNativeDailyCap = adsNativeDailyCap.toNonNegativeInt(ProductExperienceConfig.DEFAULT_ADS_NATIVE_DAILY_CAP),
            adsNonBannerDailyCap = adsNonBannerDailyCap.toNonNegativeInt(ProductExperienceConfig.DEFAULT_ADS_NON_BANNER_DAILY_CAP)
        )
    }

    private fun String.toRemoteBillingPlanDefault(): RemoteBillingPlanDefault {
        return when (trim().lowercase(Locale.US)) {
            "monthly" -> RemoteBillingPlanDefault.MONTHLY
            "annual" -> RemoteBillingPlanDefault.ANNUAL
            else -> RemoteBillingPlanDefault.MARKET
        }
    }

    private fun String.toOnboardingPaywallVariant(): OnboardingPaywallVariant {
        val normalized = trim().lowercase(Locale.US)
        return OnboardingPaywallVariant.entries.firstOrNull { it.remoteValue == normalized }
            ?: OnboardingPaywallVariant.PAYDAY_PROOF
    }

    private fun String.toPremiumProofVariant(): PremiumProofVariant {
        val normalized = trim().lowercase(Locale.US)
        return PremiumProofVariant.entries.firstOrNull { it.remoteValue == normalized }
            ?: PremiumProofVariant.NEXT_MOVE
    }

    private fun String.toPaywallTrialFraming(): PaywallTrialFraming {
        val normalized = trim().lowercase(Locale.US)
        return PaywallTrialFraming.entries.firstOrNull { it.remoteValue == normalized }
            ?: PaywallTrialFraming.FIRST_CHECKIN
    }

    private fun String.toOnboardingIntroVariant(): OnboardingIntroVariant {
        val normalized = trim().lowercase(Locale.US)
        return OnboardingIntroVariant.entries.firstOrNull { it.remoteValue == normalized }
            ?: OnboardingIntroVariant.PLAN_FIRST
    }

    private fun String.toAdsExperimentVariant(): AdsExperimentVariant {
        val normalized = trim().lowercase(Locale.US)
        return AdsExperimentVariant.entries.firstOrNull { it.remoteValue == normalized }
            ?: AdsExperimentVariant.CONTROL
    }

    private fun Long.toPercent(): Int {
        return coerceIn(MIN_PERCENT.toLong(), MAX_PERCENT.toLong()).toInt()
    }

    private fun Long.toPositiveInt(defaultValue: Int): Int {
        return if (this > 0) coerceAtMost(MAX_REMOTE_CAP_VALUE).toInt() else defaultValue
    }

    private fun Long.toNonNegativeInt(defaultValue: Int): Int {
        return if (this >= 0) coerceAtMost(MAX_REMOTE_CAP_VALUE).toInt() else defaultValue
    }

    private fun String.toExperimentName(): String {
        return trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(EXPERIMENT_NAME_MAX_LENGTH)
            .ifBlank { ProductExperienceConfig.DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT }
    }

    private const val EXPERIMENT_NAME_MAX_LENGTH = 36
    private const val MIN_PERCENT = 0
    private const val MAX_PERCENT = 100
    private const val MAX_REMOTE_CAP_VALUE = 1_000L
}
