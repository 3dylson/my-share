package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AdEligibilityContext
import pt.ms.myshare.domain.model.AdExposureSnapshot
import pt.ms.myshare.domain.model.AdPlacement
import pt.ms.myshare.domain.model.AdsExperimentVariant
import pt.ms.myshare.domain.model.ProductExperienceConfig
import java.util.concurrent.TimeUnit

class EvaluateAdEligibilityUseCaseTest {
    private val useCase = EvaluateAdEligibilityUseCase()

    @Test
    fun `blocks ads for premium users`() {
        val decision = evaluate(context(isPremium = true))

        assertFalse(decision.isEligible)
        assertEquals("premium", decision.reason)
    }

    @Test
    fun `blocks ads before first plan and before consent`() {
        assertEquals("no_first_plan", evaluate(context(hasFirstPlan = false)).reason)
        assertEquals("consent_unavailable", evaluate(context(canRequestAds = false)).reason)
    }

    @Test
    fun `blocks ads in keyboard and blocked flow states`() {
        assertEquals("keyboard_visible", evaluate(context(isKeyboardVisible = true)).reason)
        assertEquals("blocked_flow", evaluate(context(isBlockedFlowActive = true)).reason)
    }

    @Test
    fun `app open requires session minimum and cooldown`() {
        assertEquals(
            "min_sessions",
            evaluate(context(placement = AdPlacement.APP_OPEN, sessionCount = 2)).reason
        )

        val cooldownDecision = evaluate(
            context = context(placement = AdPlacement.APP_OPEN, sessionCount = 3),
            exposure = exposure(lastAppOpenShownAtMillis = NOW - TimeUnit.HOURS.toMillis(1))
        )

        assertFalse(cooldownDecision.isEligible)
        assertEquals("cooldown", cooldownDecision.reason)

        assertTrue(
            evaluate(
                context = context(placement = AdPlacement.APP_OPEN, sessionCount = 3),
                exposure = exposure(lastAppOpenShownAtMillis = NOW - TimeUnit.HOURS.toMillis(13))
            ).isEligible
        )
    }

    @Test
    fun `app open is blocked after notification launch`() {
        val decision = evaluate(
            context(placement = AdPlacement.APP_OPEN, sessionCount = 3, isNotificationLaunch = true)
        )

        assertFalse(decision.isEligible)
        assertEquals("notification_deeplink", decision.reason)
    }

    @Test
    fun `interstitial requires completed action and daily cap`() {
        assertEquals(
            "not_completed_action",
            evaluate(context(placement = AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL)).reason
        )

        val capDecision = evaluate(
            context = context(
                placement = AdPlacement.POST_COMPLETED_ACTION_INTERSTITIAL,
                isCompletedAction = true
            ),
            exposure = exposure(interstitialImpressionsToday = 1)
        )

        assertFalse(capDecision.isEligible)
        assertEquals("interstitial_daily_cap", capDecision.reason)
    }

    @Test
    fun `rewarded requires explicit opt in and respects cap`() {
        assertEquals(
            "reward_not_opted_in",
            evaluate(context(placement = AdPlacement.REWARDED_EXTRA_GOAL)).reason
        )

        val eligible = evaluate(
            context = context(
                placement = AdPlacement.REWARDED_EXTRA_GOAL,
                hasExplicitRewardOptIn = true
            )
        )

        assertTrue(eligible.isEligible)

        val capDecision = evaluate(
            context = context(
                placement = AdPlacement.REWARDED_EXTRA_GOAL,
                hasExplicitRewardOptIn = true
            ),
            exposure = exposure(rewardedImpressionsToday = 3)
        )

        assertFalse(capDecision.isEligible)
        assertEquals("rewarded_daily_cap", capDecision.reason)
    }

    @Test
    fun `unknown remote variant mapped to control disables ads safely`() {
        val decision = evaluate(
            config = ProductExperienceConfig(adsExperimentVariant = AdsExperimentVariant.CONTROL)
        )

        assertFalse(decision.isEligible)
        assertEquals("variant_control", decision.reason)
    }

    private fun evaluate(
        context: AdEligibilityContext = context(),
        exposure: AdExposureSnapshot = exposure(),
        config: ProductExperienceConfig = ProductExperienceConfig()
    ) = useCase(context, exposure, config)

    private fun context(
        placement: AdPlacement = AdPlacement.HOME_ANCHORED_BANNER,
        isPremium: Boolean = false,
        hasFirstPlan: Boolean = true,
        canRequestAds: Boolean = true,
        sessionCount: Int = 3,
        rolloutBucket: Int = 0,
        isKeyboardVisible: Boolean = false,
        isNotificationLaunch: Boolean = false,
        isBlockedFlowActive: Boolean = false,
        isCompletedAction: Boolean = false,
        hasExplicitRewardOptIn: Boolean = false
    ) = AdEligibilityContext(
        placement = placement,
        isPremium = isPremium,
        hasFirstPlan = hasFirstPlan,
        canRequestAds = canRequestAds,
        sessionCount = sessionCount,
        rolloutBucket = rolloutBucket,
        isKeyboardVisible = isKeyboardVisible,
        isNotificationLaunch = isNotificationLaunch,
        isBlockedFlowActive = isBlockedFlowActive,
        isCompletedAction = isCompletedAction,
        hasExplicitRewardOptIn = hasExplicitRewardOptIn
    )

    private fun exposure(
        lastAppOpenShownAtMillis: Long? = null,
        lastNonBannerShownAtMillis: Long? = null,
        interstitialImpressionsToday: Int = 0,
        rewardedImpressionsToday: Int = 0,
        nativeImpressionsToday: Int = 0,
        nonBannerImpressionsToday: Int = 0
    ) = AdExposureSnapshot(
        currentTimeMillis = NOW,
        lastAppOpenShownAtMillis = lastAppOpenShownAtMillis,
        lastNonBannerShownAtMillis = lastNonBannerShownAtMillis,
        interstitialImpressionsToday = interstitialImpressionsToday,
        rewardedImpressionsToday = rewardedImpressionsToday,
        nativeImpressionsToday = nativeImpressionsToday,
        nonBannerImpressionsToday = nonBannerImpressionsToday
    )

    private companion object {
        private const val NOW = 1_700_000_000_000L
    }
}
