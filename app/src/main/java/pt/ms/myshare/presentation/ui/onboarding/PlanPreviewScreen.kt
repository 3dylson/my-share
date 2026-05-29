package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import pt.ms.myshare.presentation.ui.theme.MyShareWarning
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanPreviewScreen(
    preview: PlanPreview,
    goalName: String,
    goalAmount: BigDecimal,
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

    KeyboardDismissEffect(preview.nextPayday)

    LaunchedEffect(preview.nextPayday, preview.weeklyFlexibleSpend, preview.priorityContributionPerPayday) {
        revealVisible = true
        Timber.d(
            "Plan preview reveal shown: nextPayday=%s weeklyFlexibleSpend=%s priorityContribution=%s fixedCosts=%s",
            preview.nextPayday,
            preview.weeklyFlexibleSpend,
            preview.priorityContributionPerPayday,
            preview.fixedCostsPerPayday
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
                        onClick = onContinue
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
                AnimatedVisibility(
                    visible = revealVisible,
                    enter = fadeIn(animationSpec = tween(420)) +
                        slideInVertically(animationSpec = tween(420)) { fullHeight -> fullHeight / 5 }
                ) {
                    PaydayRevealCard(
                        weeklySpend = currency.format(preview.weeklyFlexibleSpend),
                        fixedCosts = currency.format(preview.fixedCostsPerPayday),
                        priorityContribution = currency.format(preview.priorityContributionPerPayday),
                        goalLabel = goalLabel,
                        hasPriorityContribution = hasPriorityContribution
                    )
                }
            }
            item {
                PaydayMovesCard(
                    fixedCosts = currency.format(preview.fixedCostsPerPayday),
                    priorityContribution = currency.format(preview.priorityContributionPerPayday),
                    weeklySpend = currency.format(preview.weeklyFlexibleSpend),
                    goalLabel = goalLabel,
                    hasPriorityContribution = hasPriorityContribution
                )
            }
            item {
                val targetDate = preview.goalTargetDate
                if (targetDate != null && goalAmount > BigDecimal.ZERO && hasPriorityContribution) {
                    val monthName = targetDate.month.getDisplayName(TextStyle.FULL, locale)
                    PlanImpactCard(
                        goalLabel = goalLabel,
                        targetMonth = monthName,
                        targetYear = targetDate.year.toString(),
                        reminderText = stringResource(
                            R.string.onboarding_plan_preview_reminder_callout,
                            formattedNextPayday
                        )
                    )
                }
            }
            item {
                PremiumAdjustmentTeaserCard(
                    weeklySpend = currency.format(preview.weeklyFlexibleSpend),
                    priorityContribution = currency.format(preview.priorityContributionPerPayday),
                    goalLabel = goalLabel,
                    hasPriorityContribution = hasPriorityContribution
                )
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

@Composable
private fun PaydayMovesCard(
    fixedCosts: String,
    priorityContribution: String,
    weeklySpend: String,
    goalLabel: String,
    hasPriorityContribution: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.onboarding_plan_preview_moves_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            PaydayMoveRow(
                step = "1",
                title = stringResource(R.string.onboarding_plan_preview_step_fixed_title),
                body = stringResource(R.string.onboarding_plan_preview_step_fixed_body),
                amount = fixedCosts,
                icon = Icons.Default.Security,
                iconColor = MySharePrimary
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            )
            if (hasPriorityContribution) {
                PaydayMoveRow(
                    step = "2",
                    title = stringResource(R.string.onboarding_plan_preview_step_goal_title, goalLabel),
                    body = stringResource(R.string.onboarding_plan_preview_step_goal_body),
                    amount = priorityContribution,
                    icon = Icons.Default.Flag,
                    iconColor = MySharePositive
                )
            } else {
                PaydayMoveRow(
                    step = "2",
                    title = stringResource(R.string.onboarding_plan_preview_step_custom_title),
                    body = stringResource(R.string.onboarding_plan_preview_step_custom_body),
                    amount = stringResource(R.string.onboarding_plan_preview_manual_value),
                    icon = Icons.Default.Tune,
                    iconColor = MySharePositive
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            )
            PaydayMoveRow(
                step = "3",
                title = stringResource(R.string.onboarding_plan_preview_step_flex_title),
                body = stringResource(R.string.onboarding_plan_preview_step_flex_body),
                amount = weeklySpend,
                icon = Icons.Default.Celebration,
                iconColor = MyShareWarning
            )
        }
    }
}

@Composable
private fun PaydayMoveRow(
    step: String,
    title: String,
    body: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStackAmount = maxWidth < 310.dp || LocalDensity.current.fontScale >= 1.2f
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_plan_preview_step_label, step),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                if (shouldStackAmount) {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
            if (!shouldStackAmount) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = 128.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun PlanImpactCard(
    goalLabel: String,
    targetMonth: String,
    targetYear: String,
    reminderText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_plan_preview_impact_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.onboarding_plan_preview_impact_body,
                    goalLabel,
                    targetMonth,
                    targetYear
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = reminderText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumAdjustmentTeaserCard(
    weeklySpend: String,
    priorityContribution: String,
    goalLabel: String,
    hasPriorityContribution: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MySharePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.onboarding_plan_preview_premium_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MySharePrimary
                    )
                    Text(
                        text = stringResource(R.string.onboarding_plan_preview_premium_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                text = if (hasPriorityContribution) {
                    stringResource(
                        R.string.onboarding_plan_preview_premium_body,
                        weeklySpend,
                        priorityContribution,
                        goalLabel
                    )
                } else {
                    stringResource(
                        R.string.onboarding_plan_preview_premium_body_without_priority,
                        weeklySpend
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp
            )
            PremiumTimelinePreview()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumWatchItem(
                    icon = Icons.Default.Security,
                    label = stringResource(R.string.onboarding_plan_preview_premium_watch_weekly_label),
                    body = stringResource(R.string.onboarding_plan_preview_premium_watch_weekly_body, weeklySpend),
                    iconColor = MySharePrimary
                )
                PremiumWatchItem(
                    icon = Icons.Default.Flag,
                    label = if (hasPriorityContribution) {
                        stringResource(R.string.onboarding_plan_preview_premium_watch_priority_label)
                    } else {
                        stringResource(R.string.onboarding_plan_preview_premium_watch_bills_label)
                    },
                    body = if (hasPriorityContribution) {
                        stringResource(
                            R.string.onboarding_plan_preview_premium_watch_priority_body,
                            priorityContribution,
                            goalLabel
                        )
                    } else {
                        stringResource(R.string.onboarding_plan_preview_premium_watch_bills_body)
                    },
                    iconColor = MySharePositive
                )
                PremiumWatchItem(
                    icon = Icons.Default.Savings,
                    label = stringResource(R.string.onboarding_plan_preview_premium_watch_review_label),
                    body = stringResource(R.string.onboarding_plan_preview_premium_watch_review_body),
                    iconColor = MyShareWarning
                )
            }
        }
    }
}

@Composable
private fun PremiumTimelinePreview() {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.25f
        val items = listOf(
            PremiumTimelineItemState(
                icon = Icons.Default.CheckCircle,
                label = stringResource(R.string.onboarding_plan_preview_premium_timeline_today_label),
                body = stringResource(R.string.onboarding_plan_preview_premium_timeline_today_body),
                color = MySharePrimary
            ),
            PremiumTimelineItemState(
                icon = Icons.Default.Savings,
                label = stringResource(R.string.onboarding_plan_preview_premium_timeline_review_label),
                body = stringResource(R.string.onboarding_plan_preview_premium_timeline_review_body),
                color = MySharePositive
            ),
            PremiumTimelineItemState(
                icon = Icons.Default.EventAvailable,
                label = stringResource(R.string.onboarding_plan_preview_premium_timeline_next_label),
                body = stringResource(R.string.onboarding_plan_preview_premium_timeline_next_body),
                color = MyShareWarning
            )
        )

        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    PremiumTimelineStep(item = item, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    PremiumTimelineStep(item = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PremiumTimelineStep(
    item: PremiumTimelineItemState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PremiumWatchItem(
    icon: ImageVector,
    label: String,
    body: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

private data class PremiumTimelineItemState(
    val icon: ImageVector,
    val label: String,
    val body: String,
    val color: Color
)
