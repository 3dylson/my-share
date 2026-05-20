package pt.ms.myshare.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import pt.ms.myshare.domain.model.ProductExperienceConfig
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
        assertEquals(ProductExperienceConfig.DEFAULT_ONBOARDING_PAYWALL_VARIANT, config.onboardingPaywallVariant)
        assertEquals(ProductExperienceConfig.DEFAULT_PREMIUM_PROOF_VARIANT, config.premiumProofVariant)
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
