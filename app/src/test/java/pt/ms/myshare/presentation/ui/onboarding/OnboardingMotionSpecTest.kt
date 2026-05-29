package pt.ms.myshare.presentation.ui.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingMotionSpecTest {

    @Test
    fun `animations are disabled when animator duration scale is zero`() {
        assertFalse(OnboardingMotionSpec.animationsEnabled(0f))
    }

    @Test
    fun `animations are enabled when animator duration scale is positive`() {
        assertTrue(OnboardingMotionSpec.animationsEnabled(1f))
    }
}
