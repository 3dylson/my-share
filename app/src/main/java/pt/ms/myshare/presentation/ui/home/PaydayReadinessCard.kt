package pt.ms.myshare.presentation.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaydayReadinessMission
import pt.ms.myshare.domain.model.PaydayReadinessStatus
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import kotlinx.coroutines.delay

@Composable
fun PaydayReadinessCard(
    readiness: PaydayReadinessState,
    fixedCostsLabel: String,
    weeklyGuideLabel: String,
    priorityMoveLabel: String,
    modifier: Modifier = Modifier
) {
    val isReady = readiness.status == PaydayReadinessStatus.READY
    val completionScale = remember { Animatable(1f) }

    LaunchedEffect(isReady) {
        if (isReady) {
            completionScale.animateTo(1.025f, animationSpec = tween(180))
            completionScale.animateTo(1f, animationSpec = tween(260))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = completionScale.value
                scaleY = completionScale.value
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PaydayReadinessHeader(readiness)
            PaydayReadinessProgress(readiness)
            PaydayReadinessMissionList(
                readiness = readiness,
                fixedCostsLabel = fixedCostsLabel,
                weeklyGuideLabel = weeklyGuideLabel,
                priorityMoveLabel = priorityMoveLabel
            )
        }
    }
}

@Composable
private fun PaydayReadinessHeader(readiness: PaydayReadinessState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MySharePrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.padding(10.dp).size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.home_plan_readiness_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MySharePrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(readiness.status.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = readiness.nextAction?.let { stringResource(it.nextActionRes) }
                    ?: stringResource(R.string.home_plan_readiness_next_action_ready),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PaydayReadinessProgress(readiness: PaydayReadinessState) {
    val animatedProgress by animateFloatAsState(
        targetValue = readiness.progress,
        animationSpec = tween(durationMillis = 720),
        label = "payday_readiness_progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.home_plan_readiness_progress_label,
                    readiness.completedMissions,
                    readiness.totalMissions
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_review_score_percent, (readiness.progress * 100).toInt()),
                style = MaterialTheme.typography.labelMedium,
                color = MySharePrimary,
                fontWeight = FontWeight.Black
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
            color = MySharePrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PaydayReadinessMissionList(
    readiness: PaydayReadinessState,
    fixedCostsLabel: String,
    weeklyGuideLabel: String,
    priorityMoveLabel: String
) {
    val visibleMissions = remember(readiness.missions) {
        mutableStateListOf<Boolean>().apply {
            repeat(readiness.missions.size) { add(false) }
        }
    }

    LaunchedEffect(readiness.missions) {
        readiness.missions.indices.forEach { index ->
            delay(index * MISSION_REVEAL_DELAY_MS)
            visibleMissions[index] = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.3f
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                readiness.missions.forEachIndexed { index, mission ->
                    AnimatedMissionPill(
                        visible = visibleMissions.getOrNull(index) == true,
                        mission = mission,
                        fixedCostsLabel = fixedCostsLabel,
                        weeklyGuideLabel = weeklyGuideLabel,
                        priorityMoveLabel = priorityMoveLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                readiness.missions.forEachIndexed { index, mission ->
                    AnimatedMissionPill(
                        visible = visibleMissions.getOrNull(index) == true,
                        mission = mission,
                        fixedCostsLabel = fixedCostsLabel,
                        weeklyGuideLabel = weeklyGuideLabel,
                        priorityMoveLabel = priorityMoveLabel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMissionPill(
    visible: Boolean,
    mission: PaydayReadinessMissionItemState,
    fixedCostsLabel: String,
    weeklyGuideLabel: String,
    priorityMoveLabel: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(220)) { fullHeight -> fullHeight / 5 },
        modifier = modifier
    ) {
        PaydayReadinessMissionPill(
            mission = mission,
            fixedCostsLabel = fixedCostsLabel,
            weeklyGuideLabel = weeklyGuideLabel,
            priorityMoveLabel = priorityMoveLabel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PaydayReadinessMissionPill(
    mission: PaydayReadinessMissionItemState,
    fixedCostsLabel: String,
    weeklyGuideLabel: String,
    priorityMoveLabel: String,
    modifier: Modifier = Modifier
) {
    val icon = if (mission.isComplete) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val iconColor = if (mission.isComplete) MySharePositive else MaterialTheme.colorScheme.onSurfaceVariant
    val value = when (mission.mission) {
        PaydayReadinessMission.PROTECT_BILLS -> fixedCostsLabel
        PaydayReadinessMission.SET_WEEKLY_GUIDE -> weeklyGuideLabel
        PaydayReadinessMission.SET_PRIORITY_MOVE -> priorityMoveLabel
    }
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(17.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(mission.mission.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private val PaydayReadinessStatus.titleRes: Int
    get() = when (this) {
        PaydayReadinessStatus.READY -> R.string.home_plan_readiness_title_ready
        PaydayReadinessStatus.ALMOST_READY -> R.string.home_plan_readiness_title_almost_ready
        PaydayReadinessStatus.NEEDS_ATTENTION -> R.string.home_plan_readiness_title_needs_attention
    }

private val PaydayReadinessMission.labelRes: Int
    get() = when (this) {
        PaydayReadinessMission.PROTECT_BILLS -> R.string.home_plan_readiness_mission_bills
        PaydayReadinessMission.SET_WEEKLY_GUIDE -> R.string.home_plan_readiness_mission_weekly
        PaydayReadinessMission.SET_PRIORITY_MOVE -> R.string.home_plan_readiness_mission_priority
    }

private val PaydayReadinessMission.nextActionRes: Int
    get() = when (this) {
        PaydayReadinessMission.PROTECT_BILLS -> R.string.home_plan_readiness_next_action_bills
        PaydayReadinessMission.SET_WEEKLY_GUIDE -> R.string.home_plan_readiness_next_action_weekly
        PaydayReadinessMission.SET_PRIORITY_MOVE -> R.string.home_plan_readiness_next_action_priority
    }

private const val MISSION_REVEAL_DELAY_MS = 85L
