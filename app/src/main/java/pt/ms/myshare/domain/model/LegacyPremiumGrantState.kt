package pt.ms.myshare.domain.model

enum class LegacyPremiumGrantStatus {
    NotEligible,
    Eligible,
    Claiming,
    Reserved,
    Claimed,
    Dismissed,
    Error
}

data class LegacyPremiumGrantState(
    val status: LegacyPremiumGrantStatus = LegacyPremiumGrantStatus.NotEligible,
    val expiryTimeMillis: Long? = null,
    val errorMessageKey: String? = null
) {
    val shouldShowOffer: Boolean
        get() = status == LegacyPremiumGrantStatus.Eligible ||
            status == LegacyPremiumGrantStatus.Claiming ||
            status == LegacyPremiumGrantStatus.Error
}
