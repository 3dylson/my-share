package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.repository.EntitlementRepository
import javax.inject.Inject

class RefreshEntitlementUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository
) {
    suspend operator fun invoke() {
        entitlementRepository.checkActiveEntitlement()
    }
}
