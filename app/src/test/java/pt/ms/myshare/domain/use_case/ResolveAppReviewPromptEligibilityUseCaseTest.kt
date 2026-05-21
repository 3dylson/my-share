package pt.ms.myshare.domain.use_case

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.ms.myshare.domain.model.AppReviewPromptState

class ResolveAppReviewPromptEligibilityUseCaseTest {

    private val useCase = ResolveAppReviewPromptEligibilityUseCase()

    @Test
    fun `execute returns false before enough positive actions`() {
        val state = AppReviewPromptState(positiveActionCount = 1)

        val result = useCase.execute(state, nowMillis = 1_000L)

        assertFalse(result)
    }

    @Test
    fun `execute returns true after enough positive actions and no previous request`() {
        val state = AppReviewPromptState(positiveActionCount = 2)

        val result = useCase.execute(state, nowMillis = 1_000L)

        assertTrue(result)
    }

    @Test
    fun `execute returns false after in app review has already been requested`() {
        val state = AppReviewPromptState(
            positiveActionCount = 4,
            inAppReviewRequestCount = 1,
            lastInAppReviewRequestedAtMillis = 1_000L
        )

        val result = useCase.execute(state, nowMillis = 11_000L)

        assertFalse(result)
    }
}
