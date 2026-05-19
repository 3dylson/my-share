package pt.ms.myshare.domain.model

import java.math.BigDecimal

enum class PremiumReviewCoachingStatus {
    STRONG,
    STEADY,
    NEEDS_ATTENTION
}

enum class PremiumReviewCoachingMetricType {
    MONEY,
    PERCENT
}

data class PremiumReviewCoachingMetric(
    val labelKey: String,
    val value: BigDecimal,
    val valueType: PremiumReviewCoachingMetricType,
    val isPositive: Boolean
)

data class PremiumReviewCoachingSummary(
    val headlineKey: String,
    val bodyKey: String,
    val status: PremiumReviewCoachingStatus,
    val reviewedPaydays: Int,
    val metrics: List<PremiumReviewCoachingMetric>
)
