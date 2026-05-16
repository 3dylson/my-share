package pt.ms.myshare.data.remote

import pt.ms.myshare.domain.model.AppUpdatePolicy

object FirestoreAppUpdatePolicyMapper {
    private const val DEFAULT_MINIMUM_SUPPORTED_VERSION_CODE = 0
    private const val DEFAULT_IMMEDIATE_UPDATE_REQUIRED = false
    private const val DEFAULT_PLAY_STORE_PACKAGE_NAME = "pt.ms.myshare"

    fun map(data: Map<String, Any?>): Result<AppUpdatePolicy> {
        return runCatching {
            AppUpdatePolicy(
                minimumSupportedVersionCode = parseVersionCode(data["minimumSupportedVersionCode"]),
                immediateUpdateRequired = parseImmediateUpdateRequired(data["immediateUpdateRequired"]),
                playStorePackageName = parsePlayStorePackageName(data["playStorePackageName"])
            )
        }
    }

    private fun parseVersionCode(value: Any?): Int {
        if (value == null) return DEFAULT_MINIMUM_SUPPORTED_VERSION_CODE
        if (value !is Number) {
            throw IllegalArgumentException("minimumSupportedVersionCode must be numeric")
        }
        val versionCode = value.toInt()
        require(versionCode >= 0) { "minimumSupportedVersionCode cannot be negative" }
        return versionCode
    }

    private fun parseImmediateUpdateRequired(value: Any?): Boolean {
        if (value == null) return DEFAULT_IMMEDIATE_UPDATE_REQUIRED
        if (value !is Boolean) {
            throw IllegalArgumentException("immediateUpdateRequired must be boolean")
        }
        return value
    }

    private fun parsePlayStorePackageName(value: Any?): String {
        if (value == null) return DEFAULT_PLAY_STORE_PACKAGE_NAME
        if (value !is String || value.isBlank()) {
            throw IllegalArgumentException("playStorePackageName must be a non-empty string")
        }
        return value
    }
}
