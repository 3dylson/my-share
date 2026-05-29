package pt.ms.myshare.presentation.ui.localization

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class StringResource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class DynamicString(val value: String) : UiText
}

fun UiText.resolve(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> context.getString(id, *args.toTypedArray())
    }
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
