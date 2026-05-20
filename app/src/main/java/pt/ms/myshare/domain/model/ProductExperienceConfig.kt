package pt.ms.myshare.domain.model

data class ProductExperienceConfig(
    val paywallDefaultPlan: RemoteBillingPlanDefault = RemoteBillingPlanDefault.MARKET,
    val onboardingPaywallVariant: String = DEFAULT_ONBOARDING_PAYWALL_VARIANT,
    val founderOfferEnabled: Boolean = true,
    val premiumRemindersEnabled: Boolean = true,
    val premiumProofVariant: String = DEFAULT_PREMIUM_PROOF_VARIANT
) {
    companion object {
        const val DEFAULT_ONBOARDING_PAYWALL_VARIANT = "payday_proof"
        const val DEFAULT_PREMIUM_PROOF_VARIANT = "next_move"
    }
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
