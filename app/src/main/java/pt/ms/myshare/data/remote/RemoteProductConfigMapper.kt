package pt.ms.myshare.data.remote

import pt.ms.myshare.domain.model.ProductExperienceConfig
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
        onboardingIntroVariant: String
    ): ProductExperienceConfig {
        return ProductExperienceConfig(
            paywallDefaultPlan = paywallDefaultPlan.toRemoteBillingPlanDefault(),
            onboardingPaywallVariant = onboardingPaywallVariant.toOnboardingPaywallVariant(),
            founderOfferEnabled = founderOfferEnabled,
            premiumRemindersEnabled = premiumRemindersEnabled,
            premiumProofVariant = premiumProofVariant.toPremiumProofVariant(),
            onboardingConversionExperiment = onboardingConversionExperiment.toExperimentName(),
            paywallTrialFraming = paywallTrialFraming.toPaywallTrialFraming(),
            onboardingIntroVariant = onboardingIntroVariant.toOnboardingIntroVariant()
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

    private fun String.toExperimentName(): String {
        return trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(EXPERIMENT_NAME_MAX_LENGTH)
            .ifBlank { ProductExperienceConfig.DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT }
    }

    private const val EXPERIMENT_NAME_MAX_LENGTH = 36
}
