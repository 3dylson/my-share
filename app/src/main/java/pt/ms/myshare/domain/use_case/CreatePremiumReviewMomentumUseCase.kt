package pt.ms.myshare.domain.use_case

import pt.ms.myshare.domain.model.PremiumReviewMomentum
import pt.ms.myshare.domain.model.PremiumReviewMomentumStatus
import timber.log.Timber
import javax.inject.Inject

class CreatePremiumReviewMomentumUseCase @Inject constructor() {

    fun execute(totalReviews: Int, currentStreak: Int): PremiumReviewMomentum? {
        if (totalReviews <= 0) {
            Timber.tag(TAG).d("Premium review momentum skipped; no reviews yet")
            return null
        }

        val nextMilestone = nextMilestoneAfter(totalReviews)
        val reviewsUntilNextMilestone = (nextMilestone - totalReviews).coerceAtLeast(0)
        val status = when {
            currentStreak >= STRONG_STREAK_THRESHOLD -> PremiumReviewMomentumStatus.STREAKING
            totalReviews >= BUILDING_REVIEW_THRESHOLD -> PremiumReviewMomentumStatus.BUILDING
            else -> PremiumReviewMomentumStatus.STARTING
        }

        Timber.tag(TAG).d(
            "Premium review momentum computed. status=%s reviews=%d streak=%d nextMilestone=%d",
            status,
            totalReviews,
            currentStreak,
            nextMilestone
        )

        return PremiumReviewMomentum(
            status = status,
            totalReviews = totalReviews,
            currentStreak = currentStreak,
            nextMilestone = nextMilestone,
            reviewsUntilNextMilestone = reviewsUntilNextMilestone,
            progress = (totalReviews.toFloat() / nextMilestone.toFloat()).coerceIn(0f, 1f)
        )
    }

    private fun nextMilestoneAfter(totalReviews: Int): Int {
        return REVIEW_MILESTONES.firstOrNull { it > totalReviews }
            ?: (((totalReviews / EXTENDED_MILESTONE_STEP) + 1) * EXTENDED_MILESTONE_STEP)
    }

    private companion object {
        const val TAG = "PremiumReviewMomentum"
        const val STRONG_STREAK_THRESHOLD = 3
        const val BUILDING_REVIEW_THRESHOLD = 6
        const val EXTENDED_MILESTONE_STEP = 12
        val REVIEW_MILESTONES = listOf(3, 6, 12, 24)
    }
}
