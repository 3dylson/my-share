package pt.ms.myshare.domain.use_case

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.time.LocalDate

class GetPerformanceStatsUseCaseTest {

    private val repository = mockk<PlannerRepository>()
    private val useCase = GetPerformanceStatsUseCase(repository)

    @Test
    fun `execute counts consecutive reviewed pay cycles independently from positive streak`() = runTest {
        every { repository.observeReviews() } returns flowOf(
            listOf(
                review(
                    id = "older",
                    actualFlexibleSpend = BigDecimal("350"),
                    actualGoalContribution = BigDecimal("80"),
                    createdAt = LocalDate.of(2026, 5, 1),
                    paydayDate = LocalDate.of(2026, 4, 1)
                ),
                review(
                    id = "latest",
                    actualFlexibleSpend = BigDecimal("250"),
                    actualGoalContribution = BigDecimal("150"),
                    createdAt = LocalDate.of(2026, 5, 2),
                    paydayDate = LocalDate.of(2026, 5, 1)
                )
            )
        )

        val stats = useCase.execute().first()

        assertEquals(1, stats.currentStreak)
        assertEquals(2, stats.payCycleReviewStreak)
    }

    @Test
    fun `execute stops reviewed pay cycle streak after a long gap`() = runTest {
        every { repository.observeReviews() } returns flowOf(
            listOf(
                review(
                    id = "older",
                    createdAt = LocalDate.of(2026, 2, 1),
                    paydayDate = LocalDate.of(2026, 2, 1)
                ),
                review(
                    id = "latest",
                    createdAt = LocalDate.of(2026, 5, 1),
                    paydayDate = LocalDate.of(2026, 5, 1)
                )
            )
        )

        val stats = useCase.execute().first()

        assertEquals(1, stats.payCycleReviewStreak)
    }

    private fun review(
        id: String,
        actualFlexibleSpend: BigDecimal = BigDecimal("250"),
        actualGoalContribution: BigDecimal = BigDecimal("150"),
        createdAt: LocalDate,
        paydayDate: LocalDate
    ): ManualReview {
        return ManualReview(
            id = id,
            actualFlexibleSpend = actualFlexibleSpend,
            actualGoalContribution = actualGoalContribution,
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("100"),
            createdAt = createdAt,
            paydayDate = paydayDate
        )
    }
}
