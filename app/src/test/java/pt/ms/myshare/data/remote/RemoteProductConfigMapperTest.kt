package pt.ms.myshare.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import pt.ms.myshare.domain.model.AdsExperimentVariant
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.OnboardingIntroVariant
import pt.ms.myshare.domain.model.OnboardingPaywallVariant
import pt.ms.myshare.domain.model.PaywallTrialFraming
import pt.ms.myshare.domain.model.PremiumProofVariant
import pt.ms.myshare.domain.model.RemoteBillingPlanDefault

class RemoteProductConfigMapperTest {

    @Test
    fun `maps supported paywall default plans`() {
        assertEquals(RemoteBillingPlanDefault.MONTHLY, config(paywallDefaultPlan = "monthly").paywallDefaultPlan)
        assertEquals(RemoteBillingPlanDefault.ANNUAL, config(paywallDefaultPlan = "annual").paywallDefaultPlan)
        assertEquals(RemoteBillingPlanDefault.MARKET, config(paywallDefaultPlan = "market").paywallDefaultPlan)
    }

    @Test
    fun `falls back to safe defaults for unknown text variants`() {
        val config = config(
            paywallDefaultPlan = "surprise",
            onboardingPaywallVariant = "",
            premiumProofVariant = ""
        )

        assertEquals(RemoteBillingPlanDefault.MARKET, config.paywallDefaultPlan)
        assertEquals(OnboardingPaywallVariant.PAYDAY_PROOF, config.onboardingPaywallVariant)
        assertEquals(PremiumProofVariant.NEXT_MOVE, config.premiumProofVariant)
        assertEquals(ProductExperienceConfig.DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT, config.onboardingConversionExperiment)
        assertEquals(PaywallTrialFraming.FIRST_CHECKIN, config.paywallTrialFraming)
        assertEquals(OnboardingIntroVariant.PLAN_FIRST, config.onboardingIntroVariant)
        assertEquals(AdsExperimentVariant.REVENUE_STANDARD, config.adsExperimentVariant)
    }

    @Test
    fun `maps supported onboarding paywall variants`() {
        assertEquals(OnboardingPaywallVariant.PAYDAY_PROOF, config(onboardingPaywallVariant = "payday_proof").onboardingPaywallVariant)
        assertEquals(OnboardingPaywallVariant.REVIEW_MOMENTUM, config(onboardingPaywallVariant = "review_momentum").onboardingPaywallVariant)
    }

    @Test
    fun `maps supported premium proof variants`() {
        assertEquals(PremiumProofVariant.NEXT_MOVE, config(premiumProofVariant = "next_move").premiumProofVariant)
        assertEquals(PremiumProofVariant.PROGRESS_LOOP, config(premiumProofVariant = "progress_loop").premiumProofVariant)
    }

    @Test
    fun `maps supported onboarding intro variants`() {
        assertEquals(OnboardingIntroVariant.PLAN_FIRST, config(onboardingIntroVariant = "plan_first").onboardingIntroVariant)
        assertEquals(OnboardingIntroVariant.SPEND_CLARITY, config(onboardingIntroVariant = "spend_clarity").onboardingIntroVariant)
    }

    @Test
    fun `maps ab testing experiment controls`() {
        val config = config(
            onboardingConversionExperiment = "Paywall V1 / First Check-In",
            paywallTrialFraming = "first_checkin"
        )

        assertEquals("paywall_v1_first_check_in", config.onboardingConversionExperiment)
        assertEquals(PaywallTrialFraming.FIRST_CHECKIN, config.paywallTrialFraming)
    }

    @Test
    fun `keeps boolean rollout switches`() {
        val config = config(
            founderOfferEnabled = false,
            premiumRemindersEnabled = false,
            adsAnchoredBannerEnabled = false,
            adsAppOpenEnabled = false
        )

        assertFalse(config.founderOfferEnabled)
        assertFalse(config.premiumRemindersEnabled)
        assertFalse(config.adsAnchoredBannerEnabled)
        assertFalse(config.adsAppOpenEnabled)
    }

    @Test
    fun `maps revenue ads rollout defaults`() {
        val config = config()

        assertEquals(AdsExperimentVariant.REVENUE_STANDARD, config.adsExperimentVariant)
        assertEquals(100, config.adsRolloutPercent)
        assertEquals(3, config.adsMinSessionsAppOpen)
        assertEquals(12, config.adsAppOpenCooldownHours)
        assertEquals(1, config.adsInterstitialDailyCap)
        assertEquals(3, config.adsRewardedDailyCap)
        assertEquals(3, config.adsNativeDailyCap)
        assertEquals(3, config.adsNonBannerDailyCap)
    }

    @Test
    fun `unknown ads variant falls back to safe control and clamps unsafe numbers`() {
        val config = config(
            adsExperimentVariant = "unexpected",
            adsRolloutPercent = 150,
            adsMinSessionsAppOpen = -1,
            adsInterstitialDailyCap = -4
        )

        assertEquals(AdsExperimentVariant.CONTROL, config.adsExperimentVariant)
        assertEquals(100, config.adsRolloutPercent)
        assertEquals(ProductExperienceConfig.DEFAULT_ADS_MIN_SESSIONS_APP_OPEN, config.adsMinSessionsAppOpen)
        assertEquals(ProductExperienceConfig.DEFAULT_ADS_INTERSTITIAL_DAILY_CAP, config.adsInterstitialDailyCap)
    }

    private fun config(
        paywallDefaultPlan: String = "market",
        onboardingPaywallVariant: String = "payday_proof",
        founderOfferEnabled: Boolean = true,
        premiumRemindersEnabled: Boolean = true,
        premiumProofVariant: String = "next_move",
        onboardingConversionExperiment: String = ProductExperienceConfig.DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT,
        paywallTrialFraming: String = PaywallTrialFraming.FIRST_CHECKIN.remoteValue,
        onboardingIntroVariant: String = ProductExperienceConfig.DEFAULT_ONBOARDING_INTRO_VARIANT,
        adsExperimentVariant: String = ProductExperienceConfig.DEFAULT_ADS_EXPERIMENT_VARIANT,
        adsRolloutPercent: Long = ProductExperienceConfig.DEFAULT_ADS_ROLLOUT_PERCENT.toLong(),
        adsAnchoredBannerEnabled: Boolean = true,
        adsNativeMoreEnabled: Boolean = true,
        adsAppOpenEnabled: Boolean = true,
        adsInterstitialEnabled: Boolean = true,
        adsRewardedEnabled: Boolean = true,
        adsMinSessionsAppOpen: Long = ProductExperienceConfig.DEFAULT_ADS_MIN_SESSIONS_APP_OPEN.toLong(),
        adsAppOpenCooldownHours: Long = ProductExperienceConfig.DEFAULT_ADS_APP_OPEN_COOLDOWN_HOURS.toLong(),
        adsInterstitialDailyCap: Long = ProductExperienceConfig.DEFAULT_ADS_INTERSTITIAL_DAILY_CAP.toLong(),
        adsRewardedDailyCap: Long = ProductExperienceConfig.DEFAULT_ADS_REWARDED_DAILY_CAP.toLong(),
        adsNativeDailyCap: Long = ProductExperienceConfig.DEFAULT_ADS_NATIVE_DAILY_CAP.toLong(),
        adsNonBannerDailyCap: Long = ProductExperienceConfig.DEFAULT_ADS_NON_BANNER_DAILY_CAP.toLong()
    ): ProductExperienceConfig {
        return RemoteProductConfigMapper.fromValues(
            paywallDefaultPlan = paywallDefaultPlan,
            onboardingPaywallVariant = onboardingPaywallVariant,
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant,
            onboardingConversionExperiment = onboardingConversionExperiment,
            paywallTrialFraming = paywallTrialFraming,
            onboardingIntroVariant = onboardingIntroVariant,
            adsExperimentVariant = adsExperimentVariant,
            adsRolloutPercent = adsRolloutPercent,
            adsAnchoredBannerEnabled = adsAnchoredBannerEnabled,
            adsNativeMoreEnabled = adsNativeMoreEnabled,
            adsAppOpenEnabled = adsAppOpenEnabled,
            adsInterstitialEnabled = adsInterstitialEnabled,
            adsRewardedEnabled = adsRewardedEnabled,
            adsMinSessionsAppOpen = adsMinSessionsAppOpen,
            adsAppOpenCooldownHours = adsAppOpenCooldownHours,
            adsInterstitialDailyCap = adsInterstitialDailyCap,
            adsRewardedDailyCap = adsRewardedDailyCap,
            adsNativeDailyCap = adsNativeDailyCap,
            adsNonBannerDailyCap = adsNonBannerDailyCap
        )
    }
}
