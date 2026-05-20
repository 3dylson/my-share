package pt.ms.myshare.data.remote

import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.OnboardingPaywallVariant
import pt.ms.myshare.domain.model.PremiumProofVariant
import pt.ms.myshare.domain.model.RemoteBillingPlanDefault
import java.util.Locale

object RemoteProductConfigMapper {

    fun fromValues(
        paywallDefaultPlan: String,
        onboardingPaywallVariant: String,
        founderOfferEnabled: Boolean,
        premiumRemindersEnabled: Boolean,
        premiumProofVariant: String
    ): ProductExperienceConfig {
        return ProductExperienceConfig(
            paywallDefaultPlan = paywallDefaultPlan.toRemoteBillingPlanDefault(),
            onboardingPaywallVariant = onboardingPaywallVariant.toOnboardingPaywallVariant(),
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant.toPremiumProofVariant()
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
}
