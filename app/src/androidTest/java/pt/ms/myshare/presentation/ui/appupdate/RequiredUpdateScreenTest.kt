package pt.ms.myshare.presentation.ui.appupdate

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pt.ms.myshare.presentation.ui.theme.MyShareTheme

class RequiredUpdateScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun updateButtonOpensPlayStore() {
        var openPlayStoreClicks = 0

        composeRule.setContent {
            MyShareTheme {
                RequiredUpdateScreen(
                    onOpenPlayStore = { openPlayStoreClicks += 1 },
                    onOpenPlayStoreWeb = {}
                )
            }
        }

        composeRule.onNodeWithTag(REQUIRED_UPDATE_SCREEN_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REQUIRED_UPDATE_PLAY_STORE_BUTTON_TAG).performClick()

        assertEquals(1, openPlayStoreClicks)
    }

    @Test
    fun screenDoesNotExposeContinueAction() {
        composeRule.setContent {
            MyShareTheme {
                RequiredUpdateScreen(
                    onOpenPlayStore = {},
                    onOpenPlayStoreWeb = {}
                )
            }
        }

        composeRule.onAllNodesWithText("Continue").assertCountEquals(0)
    }
}
