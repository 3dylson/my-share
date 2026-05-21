package pt.ms.myshare.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bringFocusedInputIntoView(debugLabel: String): Modifier = composed {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            delay(KEYBOARD_VISIBILITY_REQUEST_DELAY_MS)
            Timber.d("Requesting focused input visibility for %s", debugLabel)
            bringIntoViewRequester.bringIntoView()
        }
    }

    bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
}

private const val KEYBOARD_VISIBILITY_REQUEST_DELAY_MS = 250L
