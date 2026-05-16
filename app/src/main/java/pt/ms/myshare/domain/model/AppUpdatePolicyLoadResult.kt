package pt.ms.myshare.domain.model

sealed class AppUpdatePolicyLoadResult {
    data class Available(
        val policy: AppUpdatePolicy,
        val source: AppUpdatePolicySource
    ) : AppUpdatePolicyLoadResult()

    data class Unavailable(
        val error: Throwable
    ) : AppUpdatePolicyLoadResult()
}

enum class AppUpdatePolicySource {
    Remote,
    Cache
}
