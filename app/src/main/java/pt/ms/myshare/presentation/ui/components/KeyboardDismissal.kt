package pt.ms.myshare.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardDismissEffect(key: Any? = Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberKeyboardDismissOnScrollConnection(): NestedScrollConnection {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    return remember(focusManager, keyboardController) {
        object : NestedScrollConnection {
            private var accumulatedUserScrollPx = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                accumulatedUserScrollPx += abs(available.y)
                if (accumulatedUserScrollPx >= KEYBOARD_DISMISS_SCROLL_THRESHOLD_PX) {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    accumulatedUserScrollPx = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                accumulatedUserScrollPx = 0f
                return Velocity.Zero
            }
        }
    }
}

private const val KEYBOARD_DISMISS_SCROLL_THRESHOLD_PX = 24f
