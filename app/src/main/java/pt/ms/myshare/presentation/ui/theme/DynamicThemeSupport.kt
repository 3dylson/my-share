package pt.ms.myshare.presentation.ui.theme

import android.os.Build

internal object DynamicThemeSupport {
    fun shouldUseDynamicColor(enabled: Boolean, sdkInt: Int): Boolean {
        return enabled && sdkInt >= Build.VERSION_CODES.S
    }
}
