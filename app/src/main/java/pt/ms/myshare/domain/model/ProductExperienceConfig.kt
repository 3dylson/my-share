package pt.ms.myshare.domain.model

data class ProductExperienceConfig(
    val paywallDefaultPlan: RemoteBillingPlanDefault = RemoteBillingPlanDefault.MARKET,
    val onboardingPaywallVariant: OnboardingPaywallVariant = OnboardingPaywallVariant.PAYDAY_PROOF,
    val founderOfferEnabled: Boolean = true,
    val premiumRemindersEnabled: Boolean = true,
    val premiumProofVariant: PremiumProofVariant = PremiumProofVariant.NEXT_MOVE,
    val onboardingConversionExperiment: String = DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT,
    val paywallTrialFraming: PaywallTrialFraming = PaywallTrialFraming.FIRST_CHECKIN,
    val onboardingIntroVariant: OnboardingIntroVariant = OnboardingIntroVariant.PLAN_FIRST,
    val adsExperimentVariant: AdsExperimentVariant = AdsExperimentVariant.REVENUE_STANDARD,
    val adsRolloutPercent: Int = DEFAULT_ADS_ROLLOUT_PERCENT,
    val adsAnchoredBannerEnabled: Boolean = true,
    val adsNativeMoreEnabled: Boolean = true,
    val adsAppOpenEnabled: Boolean = true,
    val adsInterstitialEnabled: Boolean = true,
    val adsRewardedEnabled: Boolean = true,
    val adsMinSessionsAppOpen: Int = DEFAULT_ADS_MIN_SESSIONS_APP_OPEN,
    val adsAppOpenCooldownHours: Int = DEFAULT_ADS_APP_OPEN_COOLDOWN_HOURS,
    val adsInterstitialDailyCap: Int = DEFAULT_ADS_INTERSTITIAL_DAILY_CAP,
    val adsRewardedDailyCap: Int = DEFAULT_ADS_REWARDED_DAILY_CAP,
    val adsNativeDailyCap: Int = DEFAULT_ADS_NATIVE_DAILY_CAP,
    val adsNonBannerDailyCap: Int = DEFAULT_ADS_NON_BANNER_DAILY_CAP
) {
    companion object {
        const val DEFAULT_ONBOARDING_PAYWALL_VARIANT = "payday_proof"
        const val DEFAULT_PREMIUM_PROOF_VARIANT = "next_move"
        const val DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT = "first_checkin_trial"
        const val DEFAULT_ONBOARDING_INTRO_VARIANT = "plan_first"
        const val DEFAULT_ADS_EXPERIMENT_VARIANT = "revenue_standard"
        const val DEFAULT_ADS_ROLLOUT_PERCENT = 100
        const val DEFAULT_ADS_MIN_SESSIONS_APP_OPEN = 3
        const val DEFAULT_ADS_APP_OPEN_COOLDOWN_HOURS = 12
        const val DEFAULT_ADS_INTERSTITIAL_DAILY_CAP = 1
        const val DEFAULT_ADS_REWARDED_DAILY_CAP = 3
        const val DEFAULT_ADS_NATIVE_DAILY_CAP = 3
        const val DEFAULT_ADS_NON_BANNER_DAILY_CAP = 3
    }
}

enum class OnboardingPaywallVariant(val remoteValue: String) {
    PAYDAY_PROOF(ProductExperienceConfig.DEFAULT_ONBOARDING_PAYWALL_VARIANT),
    REVIEW_MOMENTUM("review_momentum")
}

enum class PremiumProofVariant(val remoteValue: String) {
    NEXT_MOVE(ProductExperienceConfig.DEFAULT_PREMIUM_PROOF_VARIANT),
    PROGRESS_LOOP("progress_loop")
}

enum class PaywallTrialFraming(val remoteValue: String) {
    SEVEN_DAY("seven_day"),
    FIRST_CHECKIN("first_checkin")
}

enum class OnboardingIntroVariant(val remoteValue: String) {
    PLAN_FIRST(ProductExperienceConfig.DEFAULT_ONBOARDING_INTRO_VARIANT),
    SPEND_CLARITY("spend_clarity")
}

enum class AdsExperimentVariant(val remoteValue: String) {
    CONTROL("control"),
    REVENUE_STANDARD(ProductExperienceConfig.DEFAULT_ADS_EXPERIMENT_VARIANT)
}

enum class RemoteBillingPlanDefault {
    MARKET,
    MONTHLY,
    ANNUAL;

    fun resolve(marketDefault: BillingPlan): BillingPlan {
        return when (this) {
            MARKET -> marketDefault
            MONTHLY -> BillingPlan.MONTHLY
            ANNUAL -> BillingPlan.ANNUAL
        }
    }
}
