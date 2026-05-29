package pt.ms.myshare.presentation.ui.onboarding

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber

internal object OnboardingMotionSpec {
    const val PLAN_REVEAL_DURATION_MILLIS = 240
    const val TRAJECTORY_REVEAL_DURATION_MILLIS = 220
    const val TRAJECTORY_REVEAL_DELAY_MILLIS = 140L
    const val BUILDING_STEP_DELAY_MILLIS = 260L
    const val BUILDING_COMPLETE_DELAY_MILLIS = 520L

    fun animationsEnabled(animatorDurationScale: Float): Boolean = animatorDurationScale > 0f
}

@Composable
internal fun rememberOnboardingMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.map(OnboardingMotionSpec::animationsEnabled)
            .getOrElse { error ->
                Timber.e(error, "Failed to read animator duration scale for onboarding motion")
                true
            }
    }
}
