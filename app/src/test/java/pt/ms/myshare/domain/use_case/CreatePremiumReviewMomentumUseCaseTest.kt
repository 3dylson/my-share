package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.PremiumReviewMomentumStatus

class CreatePremiumReviewMomentumUseCaseTest {

    private val useCase = CreatePremiumReviewMomentumUseCase()

    @Test
    fun `execute skips momentum when there are no reviews`() {
        assertNull(useCase.execute(totalReviews = 0, currentStreak = 0))
    }

    @Test
    fun `execute builds starting milestone for early review habit`() {
        val momentum = useCase.execute(totalReviews = 1, currentStreak = 1)

        assertEquals(PremiumReviewMomentumStatus.STARTING, momentum?.status)
        assertEquals(3, momentum?.nextMilestone)
        assertEquals(2, momentum?.reviewsUntilNextMilestone)
        assertTrue(momentum != null && momentum.progress in 0.33f..0.34f)
    }

    @Test
    fun `execute marks streaking when current streak is strong`() {
        val momentum = useCase.execute(totalReviews = 4, currentStreak = 3)

        assertEquals(PremiumReviewMomentumStatus.STREAKING, momentum?.status)
        assertEquals(6, momentum?.nextMilestone)
        assertEquals(2, momentum?.reviewsUntilNextMilestone)
    }

    @Test
    fun `execute extends milestone ladder after standard milestones`() {
        val momentum = useCase.execute(totalReviews = 25, currentStreak = 1)

        assertEquals(PremiumReviewMomentumStatus.BUILDING, momentum?.status)
        assertEquals(36, momentum?.nextMilestone)
        assertEquals(11, momentum?.reviewsUntilNextMilestone)
    }
}
