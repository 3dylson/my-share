package pt.ms.myshare.data.billing

data class BillingAuthenticatedSession(
    val userId: String,
    val idToken: String
)
