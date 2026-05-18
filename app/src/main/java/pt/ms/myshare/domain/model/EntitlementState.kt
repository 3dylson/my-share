package pt.ms.myshare.domain.model

enum class EntitlementState(
    val hasPremiumAccess: Boolean
) {
    UNKNOWN(false),
    FREE(false),
    PRO(true),
    GRACE_PERIOD(true)
}
