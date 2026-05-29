package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.dismissKeyboardOnUserDrag
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun TrajectoryScreen(
    preview: PlanPreview?,
    goalName: String,
    userPreferences: UserPreferences,
    onNext: () -> Unit,
    onContinueFree: () -> Unit
) {
    KeyboardDismissEffect(preview?.nextPayday)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 20.dp)
                ) {
                    PremiumButton(
                        text = stringResource(R.string.onboarding_trajectory_button),
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = onContinueFree,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_trajectory_free_button),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .dismissKeyboardOnUserDrag(debugLabel = "TrajectoryScreen")
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.onboarding_trajectory_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.onboarding_trajectory_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            if (preview != null) {
                val hasPriorityContribution = preview.priorityContributionPerPayday > java.math.BigDecimal.ZERO
                var premiumBridgeVisible by remember(preview.nextPayday) { mutableStateOf(false) }
                var premiumChoiceVisible by remember(preview.nextPayday) { mutableStateOf(false) }
                val motionEnabled = rememberOnboardingMotionEnabled()
                val formattedWeeklyFlexibleSpend = LocalizedAmountFormatter.formatCurrency(
                    amount = preview.weeklyFlexibleSpend,
                    locale = userPreferences.locale,
                    currencyCode = userPreferences.currencyCode
                )
                val suggestedUnusedAmount = preview.weeklyFlexibleSpend
                    .multiply(BigDecimal("0.20"))
                    .setScale(2, RoundingMode.HALF_UP)
                val formattedSuggestedUnusedAmount = LocalizedAmountFormatter.formatCurrency(
                    amount = suggestedUnusedAmount,
                    locale = userPreferences.locale,
                    currencyCode = userPreferences.currencyCode
                )
                val goalLabel = goalName.ifBlank {
                    stringResource(R.string.onboarding_plan_preview_default_goal)
                }
                LaunchedEffect(preview.nextPayday, motionEnabled) {
                    premiumBridgeVisible = false
                    premiumChoiceVisible = false
                    if (motionEnabled) delay(OnboardingMotionSpec.TRAJECTORY_REVEAL_DELAY_MILLIS)
                    premiumBridgeVisible = true
                    Timber.d(
                        "Onboarding trajectory bridge shown: nextPayday=%s weeklyFlexibleSpend=%s suggestedUnusedAmount=%s hasPriorityContribution=%s motionEnabled=%s",
                        preview.nextPayday,
                        preview.weeklyFlexibleSpend,
                        suggestedUnusedAmount,
                        hasPriorityContribution,
                        motionEnabled
                    )
                    if (motionEnabled) delay(OnboardingMotionSpec.TRAJECTORY_REVEAL_DELAY_MILLIS)
                    premiumChoiceVisible = true
                }

                OnboardingReveal(visible = premiumBridgeVisible, motionEnabled = motionEnabled) {
                    PremiumBridgeCard(
                        unusedAmount = formattedSuggestedUnusedAmount,
                        weeklyFlexibleSpend = formattedWeeklyFlexibleSpend,
                        goalLabel = goalLabel,
                        hasPriorityContribution = hasPriorityContribution
                    )
                }

                Spacer(Modifier.height(14.dp))

                OnboardingReveal(visible = premiumChoiceVisible, motionEnabled = motionEnabled) {
                    PremiumAdjustmentChoiceCard()
                }

                Spacer(Modifier.height(14.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OnboardingReveal(
    visible: Boolean,
    motionEnabled: Boolean,
    content: @Composable () -> Unit
) {
    if (!motionEnabled) {
        if (visible) content()
        return
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(OnboardingMotionSpec.TRAJECTORY_REVEAL_DURATION_MILLIS)) +
            slideInVertically(animationSpec = tween(OnboardingMotionSpec.TRAJECTORY_REVEAL_DURATION_MILLIS)) { height -> height / 10 }
    ) {
        content()
    }
}

@Composable
private fun PremiumBridgeCard(
    unusedAmount: String,
    weeklyFlexibleSpend: String,
    goalLabel: String,
    hasPriorityContribution: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MySharePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.onboarding_trajectory_premium_bridge_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MySharePrimary
                    )
                    Text(
                        text = stringResource(R.string.onboarding_trajectory_premium_bridge_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
            Text(
                text = if (hasPriorityContribution) {
                    stringResource(
                        R.string.onboarding_trajectory_premium_bridge_body,
                        unusedAmount,
                        goalLabel,
                        weeklyFlexibleSpend
                    )
                } else {
                    stringResource(
                        R.string.onboarding_trajectory_premium_bridge_body_without_priority,
                        unusedAmount,
                        weeklyFlexibleSpend
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            PremiumBridgeTimeline()
        }
    }
}

@Composable
private fun PremiumBridgeTimeline() {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.2f
        val steps = listOf(
            PremiumBridgeTimelineStepState(
                title = stringResource(R.string.onboarding_trajectory_timeline_today_title),
                body = stringResource(R.string.onboarding_trajectory_timeline_today_body),
                icon = Icons.Default.Savings,
                tint = MySharePrimary
            ),
            PremiumBridgeTimelineStepState(
                title = stringResource(R.string.onboarding_trajectory_timeline_review_title),
                body = stringResource(R.string.onboarding_trajectory_timeline_review_body),
                icon = Icons.Default.CheckCircle,
                tint = MySharePositive
            ),
            PremiumBridgeTimelineStepState(
                title = stringResource(R.string.onboarding_trajectory_timeline_next_title),
                body = stringResource(R.string.onboarding_trajectory_timeline_next_body),
                icon = Icons.Default.CalendarMonth,
                tint = MySharePrimary
            )
        )

        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEach { step ->
                    PremiumBridgeTimelineStep(step = step, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEach { step ->
                    PremiumBridgeTimelineStep(step = step, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PremiumBridgeTimelineStep(
    step: PremiumBridgeTimelineStepState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, step.tint.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = step.tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = step.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 17.sp
            )
            Text(
                text = step.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

private data class PremiumBridgeTimelineStepState(
    val title: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: androidx.compose.ui.graphics.Color
)

@Composable
private fun PremiumAdjustmentChoiceCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_trajectory_adjustment_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.2f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdjustmentOptionPill(
                            title = stringResource(R.string.onboarding_trajectory_adjustment_free_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_free_body),
                            premium = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AdjustmentOptionPill(
                            title = stringResource(R.string.onboarding_trajectory_adjustment_premium_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_premium_body),
                            premium = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdjustmentOptionPill(
                            title = stringResource(R.string.onboarding_trajectory_adjustment_free_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_free_body),
                            premium = false,
                            modifier = Modifier.weight(1f)
                        )
                        AdjustmentOptionPill(
                            title = stringResource(R.string.onboarding_trajectory_adjustment_premium_label),
                            body = stringResource(R.string.onboarding_trajectory_adjustment_premium_body),
                            premium = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentOptionPill(
    title: String,
    body: String,
    premium: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (premium) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            1.dp,
            if (premium) MySharePrimary.copy(alpha = 0.26f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (premium) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (premium) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 18.sp
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}
