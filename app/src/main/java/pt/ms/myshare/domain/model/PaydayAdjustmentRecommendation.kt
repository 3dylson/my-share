package pt.ms.myshare.domain.model

import java.math.BigDecimal

enum class PaydayAdjustmentRecommendationDirection {
    MOVE_MORE_TO_PRIORITY,
    RESTORE_FLEXIBLE_BUFFER,
    KEEP_PLAN
}

data class PaydayAdjustmentRecommendation(
    val direction: PaydayAdjustmentRecommendationDirection,
    val analyzedReviewCount: Int,
    val currentFlexibleSpend: BigDecimal,
    val recommendedFlexibleSpend: BigDecimal,
    val currentPriorityContribution: BigDecimal,
    val recommendedPriorityContribution: BigDecimal,
    val adjustmentAmount: BigDecimal,
    val confidencePercent: Int,
    val suggestedRules: List<PaydayRule>
) {
    val isApplyable: Boolean
        get() = direction != PaydayAdjustmentRecommendationDirection.KEEP_PLAN && suggestedRules.isNotEmpty()
}
