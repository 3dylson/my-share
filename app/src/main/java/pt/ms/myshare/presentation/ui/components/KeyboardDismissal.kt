package pt.ms.myshare.presentation.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalViewConfiguration
import timber.log.Timber
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
fun Modifier.dismissKeyboardOnUserDrag(debugLabel: String): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val touchSlop = LocalViewConfiguration.current.touchSlop

    pointerInput(focusManager, keyboardController, touchSlop, debugLabel) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var activePointerId = down.id
            var accumulatedDrag = Offset.Zero
            var hasDismissedForGesture = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == activePointerId }
                    ?: event.changes.firstOrNull { it.pressed }
                    ?: break

                activePointerId = change.id
                if (!change.pressed) break

                accumulatedDrag += change.positionChange()
                val verticalDrag = abs(accumulatedDrag.y)
                val horizontalDrag = abs(accumulatedDrag.x)
                if (!hasDismissedForGesture && verticalDrag > touchSlop && verticalDrag > horizontalDrag) {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    Timber.d("Keyboard dismissed after user drag on %s", debugLabel)
                    hasDismissedForGesture = true
                }
            }
        }
    }
}
