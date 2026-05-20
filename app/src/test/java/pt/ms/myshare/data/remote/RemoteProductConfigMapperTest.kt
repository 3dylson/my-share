package pt.ms.myshare.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.OnboardingPaywallVariant
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
    fun `keeps boolean rollout switches`() {
        val config = config(
            founderOfferEnabled = false,
            premiumRemindersEnabled = false
        )

        assertFalse(config.founderOfferEnabled)
        assertFalse(config.premiumRemindersEnabled)
    }

    private fun config(
        paywallDefaultPlan: String = "market",
        onboardingPaywallVariant: String = "payday_proof",
        founderOfferEnabled: Boolean = true,
        premiumRemindersEnabled: Boolean = true,
        premiumProofVariant: String = "next_move"
    ): ProductExperienceConfig {
        return RemoteProductConfigMapper.fromValues(
            paywallDefaultPlan = paywallDefaultPlan,
            onboardingPaywallVariant = onboardingPaywallVariant,
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant
        )
    }
}
