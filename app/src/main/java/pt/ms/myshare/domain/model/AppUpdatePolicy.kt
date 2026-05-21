package pt.ms.myshare.domain.model

data class AppUpdatePolicy(
    val minimumSupportedVersionCode: Int,
    val immediateUpdateRequired: Boolean,
    val playStorePackageName: String
)
