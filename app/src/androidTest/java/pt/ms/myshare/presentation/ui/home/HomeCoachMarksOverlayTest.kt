package pt.ms.myshare.presentation.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pt.ms.myshare.presentation.ui.theme.MyShareTheme

class HomeCoachMarksOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun overlayAdvancesAndCompletes() {
        val coachState = mutableStateOf(
            HomeCoachMarksState(isVisible = true, currentStep = HomeCoachMarkStep.PLAN)
        )
        var completed = false

        composeTestRule.setContent {
            MyShareTheme {
                HomeCoachMarksOverlay(
                    state = coachState.value,
                    onNext = {
                        coachState.value = coachState.value.copy(currentStep = HomeCoachMarkStep.STRATEGY)
                    },
                    onDone = { completed = true },
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start here").assertExists()
        composeTestRule.onNodeWithText("1 of 4").assertExists()
        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.onNodeWithText("Tune the rules").assertExists()
        composeTestRule.onNodeWithText("2 of 4").assertExists()

        coachState.value = coachState.value.copy(currentStep = HomeCoachMarkStep.MORE)
        composeTestRule.onNodeWithText("Done").performClick()
        assertTrue(completed)
    }

    @Test
    fun overlaySkipDismisses() {
        var skipped = false

        composeTestRule.setContent {
            MyShareTheme {
                HomeCoachMarksOverlay(
                    state = HomeCoachMarksState(isVisible = true, currentStep = HomeCoachMarkStep.REVIEW),
                    onNext = {},
                    onDone = {},
                    onSkip = { skipped = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Teach Premium").assertExists()
        composeTestRule.onNodeWithText("Skip").performClick()

        assertTrue(skipped)
    }
}
