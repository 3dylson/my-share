package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.first
import pt.ms.myshare.domain.model.EntitlementState
import pt.ms.myshare.domain.repository.PlannerRepository
import javax.inject.Inject

class EnforcePremiumDowngradeUseCase @Inject constructor(
    private val plannerRepository: PlannerRepository
) {
    suspend fun execute(entitlementState: EntitlementState): Boolean {
        if (entitlementState == EntitlementState.UNKNOWN || entitlementState.hasPremiumAccess) {
            return false
        }

        val automationEnabled = plannerRepository.observeAutomationEnabled().first()
        if (!automationEnabled) return false

        plannerRepository.saveAutomationEnabled(false)
        return true
    }
}
