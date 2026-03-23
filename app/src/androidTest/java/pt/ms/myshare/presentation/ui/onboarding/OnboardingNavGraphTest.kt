package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pt.ms.myshare.presentation.ui.MainComposeActivity

@HiltAndroidTest
class OnboardingNavGraphTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainComposeActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun onboardingFlow_smokeTest() {
        // Assert we start on Welcome screen
        composeTestRule.onNodeWithText("Welcome to My Share", ignoreCase = true).assertExists()
        
        // Skip onboarding and assert we land elsewhere (e.g., Home)
        // Since we are mocking nothing yet, it will use real repo defaults
        composeTestRule.onNodeWithText("Skip", ignoreCase = true).performClick()
        
        // Check for a home screen element (like 'Flexible spend' or 'Savings')
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Flexible spend", ignoreCase = true).assertExists()
    }
}
