package pt.ms.myshare.domain.model

enum class PremiumReviewMomentumStatus {
    STARTING,
    BUILDING,
    STREAKING
}

data class PremiumReviewMomentum(
    val status: PremiumReviewMomentumStatus,
    val totalReviews: Int,
    val currentStreak: Int,
    val nextMilestone: Int,
    val reviewsUntilNextMilestone: Int,
    val progress: Float
)
