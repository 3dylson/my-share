package pt.ms.myshare.presentation.ui.theme

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicThemeSupportTest {

    @Test
    fun `shouldUseDynamicColor returns false when user disables dynamic color`() {
        val result = DynamicThemeSupport.shouldUseDynamicColor(
            enabled = false,
            sdkInt = Build.VERSION_CODES.S
        )

        assertFalse(result)
    }

    @Test
    fun `shouldUseDynamicColor returns false below Android 12`() {
        val result = DynamicThemeSupport.shouldUseDynamicColor(
            enabled = true,
            sdkInt = Build.VERSION_CODES.R
        )

        assertFalse(result)
    }

    @Test
    fun `shouldUseDynamicColor returns true on Android 12 and above`() {
        val result = DynamicThemeSupport.shouldUseDynamicColor(
            enabled = true,
            sdkInt = Build.VERSION_CODES.S
        )

        assertTrue(result)
    }
}
