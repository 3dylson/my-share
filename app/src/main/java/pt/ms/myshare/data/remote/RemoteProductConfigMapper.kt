package pt.ms.myshare.data.remote

import pt.ms.myshare.domain.model.ProductExperienceConfig
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
            onboardingPaywallVariant = onboardingPaywallVariant.ifBlank {
                ProductExperienceConfig.DEFAULT_ONBOARDING_PAYWALL_VARIANT
            },
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant.ifBlank {
                ProductExperienceConfig.DEFAULT_PREMIUM_PROOF_VARIANT
            }
        )
    }

    private fun String.toRemoteBillingPlanDefault(): RemoteBillingPlanDefault {
        return when (trim().lowercase(Locale.US)) {
            "monthly" -> RemoteBillingPlanDefault.MONTHLY
            "annual" -> RemoteBillingPlanDefault.ANNUAL
            else -> RemoteBillingPlanDefault.MARKET
        }
    }
}
