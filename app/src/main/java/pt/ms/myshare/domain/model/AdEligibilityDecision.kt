package pt.ms.myshare.domain.model

data class AdEligibilityDecision(
    val isEligible: Boolean,
    val reason: String
) {
    companion object {
        fun eligible(): AdEligibilityDecision = AdEligibilityDecision(true, "eligible")
        fun ineligible(reason: String): AdEligibilityDecision = AdEligibilityDecision(false, reason)
    }
}
