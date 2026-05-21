package pt.ms.myshare.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class PremiumAdjustmentRecord(
    val id: String = UUID.randomUUID().toString(),
    val direction: PaydayAdjustmentRecommendationDirection,
    val status: PremiumAdjustmentStatus = PremiumAdjustmentStatus.APPLIED,
    val adjustmentAmount: BigDecimal,
    val previousFlexibleSpend: BigDecimal,
    val recommendedFlexibleSpend: BigDecimal,
    val previousPriorityContribution: BigDecimal,
    val recommendedPriorityContribution: BigDecimal,
    val confidencePercent: Int,
    val analyzedReviewCount: Int,
    val affectedRuleIds: List<String>,
    val createdAt: LocalDate = LocalDate.now(),
    val reviewId: String? = null,
    val undoneAt: LocalDate? = null
)

enum class PremiumAdjustmentStatus {
    APPLIED,
    UNDONE
}
