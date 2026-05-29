package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.dismissKeyboardOnUserDrag
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

@Composable
fun PlanPreviewScreen(
    preview: PlanPreview,
    goalName: String,
    userPreferences: UserPreferences,
    onTuneAllocation: () -> Unit,
    onContinue: () -> Unit
) {
    val locale = userPreferences.locale
    val currency = NumberFormat.getCurrencyInstance(locale).apply { currency = userPreferences.currency }
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    val formattedNextPayday = preview.nextPayday.format(dateFormatter)
    val hasPriorityContribution = preview.priorityContributionPerPayday > BigDecimal.ZERO
    val goalLabel = goalName.ifBlank { stringResource(R.string.onboarding_plan_preview_default_goal) }
    var revealVisible by remember(preview.nextPayday) { mutableStateOf(false) }
    val motionEnabled = rememberOnboardingMotionEnabled()
    val haptic = LocalHapticFeedback.current

    KeyboardDismissEffect(preview.nextPayday)

    LaunchedEffect(preview.nextPayday, preview.weeklyFlexibleSpend, preview.priorityContributionPerPayday, motionEnabled) {
        revealVisible = true
        Timber.d(
            "Plan preview reveal shown: nextPayday=%s weeklyFlexibleSpend=%s priorityContribution=%s fixedCosts=%s motionEnabled=%s",
            preview.nextPayday,
            preview.weeklyFlexibleSpend,
            preview.priorityContributionPerPayday,
            preview.fixedCostsPerPayday,
            motionEnabled
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    PremiumButton(
                        text = stringResource(R.string.onboarding_plan_preview_button_secure),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onContinue()
                        }
                    )
                    TextButton(
                        onClick = onTuneAllocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_plan_preview_button_tune),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dismissKeyboardOnUserDrag(debugLabel = "PlanPreviewScreen")
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 30.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OnboardingProgressIndicator(
                    stepIndex = 4,
                    stepTotal = OnboardingViewModel.SETUP_STEP_TOTAL
                )
            }
            item {
                PlanPreviewHeader(nextPayday = formattedNextPayday)
            }
            item {
                if (motionEnabled) {
                    AnimatedVisibility(
                        visible = revealVisible,
                        enter = fadeIn(animationSpec = tween(OnboardingMotionSpec.PLAN_REVEAL_DURATION_MILLIS)) +
                            slideInVertically(animationSpec = tween(OnboardingMotionSpec.PLAN_REVEAL_DURATION_MILLIS)) { fullHeight -> fullHeight / 10 }
                    ) {
                        PaydayRevealCard(
                            weeklySpend = currency.format(preview.weeklyFlexibleSpend),
                            fixedCosts = currency.format(preview.fixedCostsPerPayday),
                            priorityContribution = currency.format(preview.priorityContributionPerPayday),
                            goalLabel = goalLabel,
                            hasPriorityContribution = hasPriorityContribution
                        )
                    }
                } else {
                    PaydayRevealCard(
                        weeklySpend = currency.format(preview.weeklyFlexibleSpend),
                        fixedCosts = currency.format(preview.fixedCostsPerPayday),
                        priorityContribution = currency.format(preview.priorityContributionPerPayday),
                        goalLabel = goalLabel,
                        hasPriorityContribution = hasPriorityContribution
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanPreviewHeader(nextPayday: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.onboarding_plan_preview_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 34.sp
        )
        Text(
            text = stringResource(R.string.onboarding_plan_preview_subtitle, nextPayday),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PaydayRevealCard(
    weeklySpend: String,
    fixedCosts: String,
    priorityContribution: String,
    goalLabel: String,
    hasPriorityContribution: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PlanPreviewBadge(text = stringResource(R.string.onboarding_plan_preview_unlock_label))
            Text(
                text = stringResource(R.string.onboarding_plan_preview_weekly_safe_title, weeklySpend),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 29.sp
            )
            Text(
                text = if (hasPriorityContribution) {
                    stringResource(
                        R.string.onboarding_plan_preview_reveal_body,
                        fixedCosts,
                        priorityContribution,
                        goalLabel
                    )
                } else {
                    stringResource(R.string.onboarding_plan_preview_reveal_body_without_priority, fixedCosts)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                lineHeight = 21.sp
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 280.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlanPreviewMetric(
                            label = stringResource(R.string.onboarding_plan_preview_metric_fixed),
                            value = fixedCosts,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PlanPreviewMetric(
                            label = stringResource(R.string.onboarding_plan_preview_metric_priority),
                            value = if (hasPriorityContribution) priorityContribution else stringResource(R.string.onboarding_plan_preview_manual_value),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlanPreviewMetric(
                            label = stringResource(R.string.onboarding_plan_preview_metric_fixed),
                            value = fixedCosts,
                            modifier = Modifier.weight(1f)
                        )
                        PlanPreviewMetric(
                            label = stringResource(R.string.onboarding_plan_preview_metric_priority),
                            value = if (hasPriorityContribution) priorityContribution else stringResource(R.string.onboarding_plan_preview_manual_value),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanPreviewBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePositive,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlanPreviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 23.sp
            )
        }
    }
}
