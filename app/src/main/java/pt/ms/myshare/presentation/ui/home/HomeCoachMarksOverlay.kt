package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R

@Composable
fun HomeCoachMarksOverlay(
    state: HomeCoachMarksState,
    onNext: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val step = state.currentStep
    val paneTitle = stringResource(R.string.home_coach_marks_pane_title)
    val progress = stringResource(
        R.string.home_coach_marks_progress,
        step.stepNumber,
        state.totalSteps
    )
    val bottomNavClearance = if (LocalDensity.current.fontScale >= 1.2f) 112.dp else 96.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
            .padding(bottom = bottomNavClearance)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .semantics {
                    this.paneTitle = paneTitle
                    liveRegion = LiveRegionMode.Polite
                },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = progress,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(step.titleRes),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = stringResource(step.bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onSkip
                    ) {
                        Text(text = stringResource(R.string.home_coach_marks_skip))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = if (step.isLast) onDone else onNext
                    ) {
                        Text(
                            text = stringResource(
                                if (step.isLast) {
                                    R.string.home_coach_marks_done
                                } else {
                                    R.string.home_coach_marks_next
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

private val HomeCoachMarkStep.titleRes: Int
    get() = when (this) {
        HomeCoachMarkStep.PLAN -> R.string.home_coach_marks_plan_title
        HomeCoachMarkStep.STRATEGY -> R.string.home_coach_marks_strategy_title
        HomeCoachMarkStep.REVIEW -> R.string.home_coach_marks_review_title
        HomeCoachMarkStep.MORE -> R.string.home_coach_marks_more_title
    }

private val HomeCoachMarkStep.bodyRes: Int
    get() = when (this) {
        HomeCoachMarkStep.PLAN -> R.string.home_coach_marks_plan_body
        HomeCoachMarkStep.STRATEGY -> R.string.home_coach_marks_strategy_body
        HomeCoachMarkStep.REVIEW -> R.string.home_coach_marks_review_body
        HomeCoachMarkStep.MORE -> R.string.home_coach_marks_more_body
    }

private val HomeCoachMarkStep.icon: ImageVector
    get() = when (this) {
        HomeCoachMarkStep.PLAN -> Icons.Filled.CalendarToday
        HomeCoachMarkStep.STRATEGY -> Icons.Filled.Lightbulb
        HomeCoachMarkStep.REVIEW -> Icons.Filled.AutoGraph
        HomeCoachMarkStep.MORE -> Icons.Filled.MoreHoriz
    }
