package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.AppUpdateDecision
import pt.ms.myshare.domain.model.AppUpdatePolicyLoadResult
import pt.ms.myshare.domain.repository.AppUpdatePolicyRepository

class ResolveAppUpdateDecisionUseCase(
    private val appUpdatePolicyRepository: AppUpdatePolicyRepository
) {
    suspend operator fun invoke(installedVersionCode: Int): AppUpdateDecision {
        return when (val loadResult = appUpdatePolicyRepository.loadPolicy()) {
            is AppUpdatePolicyLoadResult.Available -> {
                val mustBlockAppUse = installedVersionCode < loadResult.policy.minimumSupportedVersionCode
                AppUpdateDecision(
                    policy = loadResult.policy,
                    shouldRequestImmediateUpdate = mustBlockAppUse && loadResult.policy.immediateUpdateRequired,
                    mustBlockAppUse = mustBlockAppUse,
                    policyError = null
                )
            }
            is AppUpdatePolicyLoadResult.Unavailable -> AppUpdateDecision(
                policy = null,
                shouldRequestImmediateUpdate = false,
                mustBlockAppUse = false,
                policyError = loadResult.error
            )
        }
    }
}
