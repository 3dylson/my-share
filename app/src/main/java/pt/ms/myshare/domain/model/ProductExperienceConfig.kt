package pt.ms.myshare.domain.model

data class ProductExperienceConfig(
    val paywallDefaultPlan: RemoteBillingPlanDefault = RemoteBillingPlanDefault.MARKET,
    val onboardingPaywallVariant: OnboardingPaywallVariant = OnboardingPaywallVariant.PAYDAY_PROOF,
    val founderOfferEnabled: Boolean = true,
    val premiumRemindersEnabled: Boolean = true,
    val premiumProofVariant: PremiumProofVariant = PremiumProofVariant.NEXT_MOVE,
    val onboardingConversionExperiment: String = DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT,
    val paywallTrialFraming: PaywallTrialFraming = PaywallTrialFraming.SEVEN_DAY
) {
    companion object {
        const val DEFAULT_ONBOARDING_PAYWALL_VARIANT = "payday_proof"
        const val DEFAULT_PREMIUM_PROOF_VARIANT = "next_move"
        const val DEFAULT_ONBOARDING_CONVERSION_EXPERIMENT = "baseline"
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
