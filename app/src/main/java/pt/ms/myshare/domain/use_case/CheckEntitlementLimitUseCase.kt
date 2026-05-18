package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.first
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.repository.EntitlementRepository
import javax.inject.Inject

class CheckEntitlementLimitUseCase @Inject constructor(
    private val entitlementRepository: EntitlementRepository
) {
    suspend fun canUsePreset(preset: AllocationPreset): Boolean {
        if (preset == AllocationPreset.BALANCED) return true
        return entitlementRepository.isPro.first()
    }

    suspend fun canAddMultipleGoals(currentCount: Int): Boolean {
        if (currentCount == 0) return true
        return entitlementRepository.isPro.first()
    }

    suspend fun canAddMultipleRules(currentCount: Int): Boolean {
        if (currentCount == 0) return true
        return entitlementRepository.isPro.first()
    }

    suspend fun canViewReviewHistoryDepth(index: Int): Boolean {
        // Free tier can only see the 3 most recent reviews
        if (index < 3) return true
        return entitlementRepository.isPro.first()
    }
}
