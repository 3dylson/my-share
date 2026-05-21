package pt.ms.myshare.domain.use_case

/**
 * Responsibility: Value object for localized notification content.
 */
data class NotificationContent(
    val titleKey: String,
    val messageKey: String,
    val messageArgs: List<String>,
    val destination: String,
    val analyticsType: String
)
