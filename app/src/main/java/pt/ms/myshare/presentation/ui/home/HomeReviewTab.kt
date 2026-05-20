package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PremiumCheckInStatus
import pt.ms.myshare.domain.model.PremiumProofVariant
import pt.ms.myshare.domain.model.PremiumReviewMomentumStatus
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Responsibility: Renders the Manual Review form and historical performance records.
 * This file is part of the Home screen refactoring to follow SRP.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun LazyListScope.homeReviewTab(
    state: ReviewCardState,
    history: List<ReviewHistoryItemState>,
    performanceStats: PerformanceStatsState,
    isPremium: Boolean,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit,
    onShowPaywall: () -> Unit,
    onShowFirstReviewPaywall: () -> Unit,
    onOpenFullHistory: () -> Unit,
    onEditReview: (ReviewHistoryItemState) -> Unit,
    onDeleteReview: (ReviewHistoryItemState) -> Unit,
    onConfigureReminder: () -> Unit,
    onApplyPaydayRecommendation: () -> Unit
) {
    val coachingInsights = state.coachingInsights
    val coachingSummary = state.coachingSummary
    val paydayRecommendation = state.paydayRecommendation
    val premiumCheckIn = state.premiumCheckIn
    val premiumMomentum = state.premiumMomentum

    if (isPremium && premiumCheckIn != null) {
        item {
            PremiumCheckInReviewCard(
                checkIn = premiumCheckIn,
                onConfigureReminder = onConfigureReminder
            )
            Spacer(Modifier.height(16.dp))
        }
    }
    
    if (history.isNotEmpty()) {
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_review_performance_title))
            if (isPremium) {
                if (performanceStats.performanceTrend.isNotEmpty()) {
                    PremiumSparkline(
                        points = performanceStats.performanceTrend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(vertical = 12.dp)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_review_score_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.home_review_score_percent, performanceStats.healthScore),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.home_review_score_count, performanceStats.totalReviews),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .align(Alignment.CenterVertically)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_review_streak_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.home_review_streak_count, performanceStats.currentStreak),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (premiumMomentum != null) {
                    Spacer(Modifier.height(12.dp))
                    PremiumReviewMomentumCard(momentum = premiumMomentum)
                }
            } else {
                LockedPerformanceTrendCard(
                    item = history.first(),
                    onClick = onShowPaywall
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (isPremium && performanceStats.totalFlexSavingsLabel.isNotEmpty() && performanceStats.totalSavings > BigDecimal.ZERO) {
            item {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_review_savings_title, performanceStats.totalFlexSavingsLabel),
                    description = stringResource(R.string.home_review_savings_desc),
                    icon = Icons.Default.Savings,
                    onClick = {}
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (paydayRecommendation != null) {
        item {
            if (isPremium) {
                PaydayAdjustmentRecommendationCard(
                    recommendation = paydayRecommendation,
                    messageKey = state.recommendationMessageKey,
                    onApply = onApplyPaydayRecommendation
                )
            } else {
                LockedPaydayRecommendationCard(
                    recommendation = paydayRecommendation,
                    onClick = onShowPaywall
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    } else if (!isPremium && history.isNotEmpty()) {
        item {
            LockedReviewRecommendationCard(
                item = history.first(),
                onClick = onShowPaywall
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (isPremium && (coachingSummary != null || coachingInsights.isNotEmpty())) {
        item {
            val context = LocalContext.current
            PremiumSectionHeader(title = stringResource(R.string.home_review_coach_title))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                coachingSummary?.let {
                    PremiumReviewCoachingSummaryCard(summary = it)
                }

                coachingInsights.forEach { insight ->
                    val headline = remember(insight.headline) {
                        val resId = context.resources.getIdentifier(insight.headline, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else insight.headline
                    }
                    val body = remember(insight.supportingText) {
                        val resId = context.resources.getIdentifier(insight.supportingText, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else insight.supportingText
                    }

                    val icon = when (insight.type) {
                        pt.ms.myshare.domain.model.InsightType.SUCCESS -> Icons.Default.CheckCircle
                        pt.ms.myshare.domain.model.InsightType.WARNING -> Icons.Default.Warning
                        pt.ms.myshare.domain.model.InsightType.TIP -> Icons.Default.AutoAwesome
                    }

                    PremiumBenefitCard(
                        title = headline,
                        description = body,
                        icon = icon,
                        onClick = {}
                    )
                }
            }
        }
    }

    if (history.isEmpty() && paydayRecommendation == null && (!isPremium || premiumCheckIn == null)) {
        item {
            FirstReviewPremiumProofCard(
                isPremium = isPremium,
                premiumProofVariant = state.premiumProofVariant,
                onShowPaywall = onShowFirstReviewPaywall
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    item {
        val context = LocalContext.current
        val errorMessage = remember(state.error) {
            state.error?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else it
            }
        }
        CompactReviewEntryCard(
            flexibleSpend = state.actualFlexibleSpend,
            goalContribution = state.actualGoalContribution,
            currencySymbol = state.currencySymbol,
            errorMessage = errorMessage,
            onFlexibleSpendChanged = onFlexibleSpendChanged,
            onGoalContributionChanged = onGoalContributionChanged,
            onSaveReview = onSaveReview
        )
    }

    item {
        PremiumSectionHeader(title = stringResource(R.string.home_review_history_title))
    }

    if (history.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = stringResource(R.string.home_review_empty_history_title),
                description = stringResource(R.string.home_review_empty_history_desc),
                icon = Icons.Default.BarChart,
                onClick = {}
            )
        }
    } else {
        val visibleHistory = if (isPremium) {
            history.take(PREMIUM_INLINE_HISTORY_LIMIT)
        } else {
            history.take(1)
        }
        visibleHistory.forEach { historyItem ->
            item(key = historyItem.id) {
                CompactReviewHistoryCard(
                    item = historyItem,
                    onEdit = { onEditReview(historyItem) },
                    onDelete = { onDeleteReview(historyItem) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        if (isPremium && history.size > visibleHistory.size) {
            item {
                ReviewHistoryTimelinePreviewCard(
                    hiddenCount = history.size - visibleHistory.size,
                    totalCount = history.size,
                    onClick = onOpenFullHistory
                )
                Spacer(Modifier.height(8.dp))
            }
        } else if (!isPremium && history.size > 1) {
            item {
                LockedReviewHistoryPreviewCard(
                    hiddenCount = history.size - visibleHistory.size,
                    onClick = onShowPaywall
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PremiumReviewMomentumCard(
    momentum: PremiumReviewMomentumState,
    modifier: Modifier = Modifier
) {
    val headline = when (momentum.status) {
        PremiumReviewMomentumStatus.STARTING -> stringResource(R.string.home_review_momentum_headline_starting)
        PremiumReviewMomentumStatus.BUILDING -> stringResource(R.string.home_review_momentum_headline_building)
        PremiumReviewMomentumStatus.STREAKING -> stringResource(R.string.home_review_momentum_headline_streaking)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_momentum_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(
                            R.string.home_review_momentum_body,
                            momentum.reviewsUntilNextMilestone,
                            momentum.nextMilestone
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            LinearProgressIndicator(
                progress = { momentum.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MySharePrimary,
                trackColor = MySharePrimary.copy(alpha = 0.12f)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
                val chips = listOf(
                    Triple(
                        stringResource(R.string.home_review_momentum_reviews, momentum.totalReviews),
                        Icons.Default.CheckCircle,
                        MySharePrimary
                    ),
                    Triple(
                        stringResource(R.string.home_review_momentum_streak, momentum.currentStreak),
                        Icons.Default.LocalFireDepartment,
                        MySharePositive
                    ),
                    Triple(
                        stringResource(R.string.home_review_momentum_next, momentum.nextMilestone),
                        Icons.Default.Flag,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chips.forEach { chip ->
                            PremiumMomentumChip(
                                label = chip.first,
                                icon = chip.second,
                                iconColor = chip.third,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        chips.forEach { chip ->
                            PremiumMomentumChip(
                                label = chip.first,
                                icon = chip.second,
                                iconColor = chip.third,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumMomentumChip(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FirstReviewPremiumProofCard(
    isPremium: Boolean,
    premiumProofVariant: PremiumProofVariant,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (isPremium) {
        stringResource(R.string.home_review_first_premium_title_unlocked)
    } else if (premiumProofVariant == PremiumProofVariant.PROGRESS_LOOP) {
        stringResource(R.string.home_review_first_premium_title_progress_loop)
    } else {
        stringResource(R.string.home_review_first_premium_title_locked)
    }
    val description = if (isPremium) {
        stringResource(R.string.home_review_first_premium_desc_unlocked)
    } else if (premiumProofVariant == PremiumProofVariant.PROGRESS_LOOP) {
        stringResource(R.string.home_review_first_premium_desc_progress_loop)
    } else {
        stringResource(R.string.home_review_first_premium_desc_locked)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = if (isPremium) Icons.Default.AutoAwesome else Icons.Default.Lock,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_first_premium_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
                val steps = listOf(
                    Triple(
                        stringResource(R.string.home_review_first_premium_step_review),
                        stringResource(R.string.home_review_first_premium_step_review_desc),
                        Icons.Default.EditNote
                    ),
                    Triple(
                        stringResource(R.string.home_review_first_premium_step_learn),
                        stringResource(R.string.home_review_first_premium_step_learn_desc),
                        Icons.Default.QueryStats
                    ),
                    Triple(
                        stringResource(R.string.home_review_first_premium_step_adjust),
                        stringResource(R.string.home_review_first_premium_step_adjust_desc),
                        Icons.Default.Tune
                    )
                )

                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        steps.forEach { step ->
                            ReviewPreviewPill(
                                label = step.first,
                                body = step.second,
                                icon = step.third,
                                iconColor = MySharePrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        steps.forEach { step ->
                            ReviewPreviewPill(
                                label = step.first,
                                body = step.second,
                                icon = step.third,
                                iconColor = MySharePrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (!isPremium) {
                TextButton(
                    onClick = onShowPaywall,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_first_premium_action),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumCheckInReviewCard(
    checkIn: PremiumCheckInState,
    onConfigureReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relativeLabel = checkIn.relativeLabel()
    val title = when (checkIn.status) {
        PremiumCheckInStatus.READY_NOW -> stringResource(R.string.home_review_checkin_title_ready)
        PremiumCheckInStatus.OVERDUE -> stringResource(R.string.home_review_checkin_title_overdue)
        PremiumCheckInStatus.SCHEDULED -> stringResource(R.string.home_review_checkin_title_scheduled)
        PremiumCheckInStatus.REVIEWED -> stringResource(R.string.home_review_checkin_title_reviewed)
    }
    val body = when (checkIn.status) {
        PremiumCheckInStatus.READY_NOW -> stringResource(R.string.home_review_checkin_body_ready)
        PremiumCheckInStatus.OVERDUE -> stringResource(R.string.home_review_checkin_body_overdue, checkIn.checkInDateLabel)
        PremiumCheckInStatus.SCHEDULED -> stringResource(R.string.home_review_checkin_body_scheduled, checkIn.checkInDateLabel)
        PremiumCheckInStatus.REVIEWED -> stringResource(R.string.home_review_checkin_body_reviewed, checkIn.checkInDateLabel)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.13f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_checkin_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumCheckInStatusPill(
                            label = relativeLabel,
                            isDue = checkIn.isDue,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PremiumCheckInReminderAction(
                            reminderEnabled = checkIn.reminderEnabled,
                            onConfigureReminder = onConfigureReminder,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumCheckInStatusPill(
                            label = relativeLabel,
                            isDue = checkIn.isDue,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumCheckInReminderAction(
                            reminderEnabled = checkIn.reminderEnabled,
                            onConfigureReminder = onConfigureReminder,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumCheckInReminderAction(
    reminderEnabled: Boolean,
    onConfigureReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reminderEnabled) {
        Surface(
            modifier = modifier.heightIn(min = 42.dp),
            shape = RoundedCornerShape(999.dp),
            color = MySharePositive.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, MySharePositive.copy(alpha = 0.24f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MySharePositive,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.home_review_checkin_reminder_on),
                    style = MaterialTheme.typography.labelMedium,
                    color = MySharePositive,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onConfigureReminder,
            modifier = modifier.heightIn(min = 42.dp),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.home_review_checkin_set_reminder),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumCheckInStatusPill(
    label: String,
    isDue: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 42.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (isDue) MySharePrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (isDue) MySharePrimary.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDue) Icons.Default.PlayCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isDue) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isDue) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumCheckInState.relativeLabel(): String {
    val context = LocalContext.current
    return remember(relativeLabelKey, relativeLabelArgs) {
        val resId = context.resources.getIdentifier(relativeLabelKey, "string", context.packageName)
        if (resId != 0) {
            context.getString(resId, *relativeLabelArgs.toTypedArray())
        } else {
            relativeLabelKey
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewHistoryTimelineBottomSheet(
    history: List<ReviewHistoryItemState>,
    onDismissRequest: () -> Unit,
    onEditReview: (ReviewHistoryItemState) -> Unit,
    onDeleteReview: (ReviewHistoryItemState) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val groupedHistory = remember(history) {
        history.groupBy { it.monthLabel.ifBlank { it.dateLabel } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_review_history_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_history_sheet_desc, history.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedHistory.forEach { (month, itemsForMonth) ->
                    item(key = "month-$month") {
                        Text(
                            text = month.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(
                        items = itemsForMonth,
                        key = { item -> "timeline-${item.id}" }
                    ) { item ->
                        ReviewTimelineRow(
                            item = item,
                            onEdit = {
                                onDismissRequest()
                                onEditReview(item)
                            },
                            onDelete = { onDeleteReview(item) }
                        )
                    }
                }
            }

            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_review_history_sheet_close),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCorrectionBottomSheet(
    item: ReviewHistoryItemState,
    currencySymbol: String,
    onDismissRequest: () -> Unit,
    onSave: (String, String) -> Boolean,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var flexibleSpend by remember(item.id) { mutableStateOf(item.editableFlexibleSpend) }
    var goalContribution by remember(item.id) { mutableStateOf(item.editableGoalContribution) }
    var showValidationError by remember(item.id) { mutableStateOf(false) }
    fun saveCorrection() {
        if (onSave(flexibleSpend, goalContribution)) {
            onDismissRequest()
        } else {
            showValidationError = true
        }
    }
    val inputKeyboardActions = rememberInputKeyboardActions(onDone = ::saveCorrection)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_review_edit_title, item.dateLabel),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_edit_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_flex_exact),
                            value = flexibleSpend,
                            currencySymbol = currencySymbol,
                            onValueChange = {
                                flexibleSpend = it
                                showValidationError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            imeAction = ImeAction.Next,
                            keyboardActions = inputKeyboardActions
                        )
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_goal_exact),
                            value = goalContribution,
                            currencySymbol = currencySymbol,
                            onValueChange = {
                                goalContribution = it
                                showValidationError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            imeAction = ImeAction.Done,
                            keyboardActions = inputKeyboardActions
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_flex_exact),
                            value = flexibleSpend,
                            currencySymbol = currencySymbol,
                            onValueChange = {
                                flexibleSpend = it
                                showValidationError = false
                            },
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next,
                            keyboardActions = inputKeyboardActions
                        )
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_goal_exact),
                            value = goalContribution,
                            currencySymbol = currencySymbol,
                            onValueChange = {
                                goalContribution = it
                                showValidationError = false
                            },
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Done,
                            keyboardActions = inputKeyboardActions
                        )
                    }
                }
            }

            if (showValidationError) {
                Text(
                    text = stringResource(R.string.home_review_error_invalid_amounts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = ::saveCorrection,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_review_edit_save),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                        Button(
                            onClick = ::saveCorrection,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.home_review_edit_save),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaydayAdjustmentRecommendationCard(
    recommendation: PaydayAdjustmentRecommendationState,
    messageKey: String?,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val message = remember(messageKey) {
        messageKey?.let {
            val resId = context.resources.getIdentifier(it, "string", context.packageName)
            if (resId != 0) context.getString(resId) else it
        }
    }
    val title = recommendationTitle(recommendation.direction)
    val body = recommendationBody(recommendation)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_recommendation_premium_unlocked_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            RecommendationMetricsGrid(recommendation)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                    border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.16f))
                ) {
                    Text(
                        text = stringResource(
                            R.string.home_review_recommendation_confidence,
                            recommendation.confidencePercent
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = stringResource(
                        R.string.home_review_recommendation_review_count,
                        recommendation.analyzedReviewCount
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            if (message != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MySharePositive,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            if (recommendation.isApplyable) {
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_review_recommendation_apply),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedPerformanceTrendCard(
    item: ReviewHistoryItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_performance_locked_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_performance_locked_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.home_review_performance_locked_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.home_review_performance_locked_latest,
                            item.flexibleSpendLabel,
                            item.goalContributionLabel
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_performance_locked_free_label),
                            body = stringResource(R.string.home_review_performance_locked_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_performance_locked_premium_label),
                            body = stringResource(R.string.home_review_performance_locked_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_performance_locked_free_label),
                            body = stringResource(R.string.home_review_performance_locked_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_performance_locked_premium_label),
                            body = stringResource(R.string.home_review_performance_locked_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.home_review_performance_locked_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LockedPaydayRecommendationCard(
    recommendation: PaydayAdjustmentRecommendationState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val lockedBody = if (recommendation.direction == PaydayAdjustmentRecommendationDirection.KEEP_PLAN) {
                stringResource(
                    R.string.home_review_recommendation_locked_keep_body,
                    recommendation.currentFlexibleSpendLabel,
                    recommendation.currentPriorityContributionLabel
                )
            } else {
                stringResource(
                    R.string.home_review_recommendation_locked_specific_body,
                    recommendation.recommendedFlexibleSpendLabel,
                    recommendation.recommendedPriorityContributionLabel
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_recommendation_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = recommendationTitle(recommendation.direction),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lockedBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            RecommendationMetricsGrid(recommendation)
            Text(
                text = stringResource(R.string.home_review_recommendation_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecommendationMetricsGrid(recommendation: PaydayAdjustmentRecommendationState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
        val metrics = listOf(
            Triple(
                stringResource(R.string.home_review_recommendation_current_flex),
                recommendation.currentFlexibleSpendLabel,
                Icons.Default.RadioButtonUnchecked
            ),
            Triple(
                stringResource(R.string.home_review_recommendation_next_flex),
                recommendation.recommendedFlexibleSpendLabel,
                Icons.Default.CheckCircle
            ),
            Triple(
                stringResource(R.string.home_review_recommendation_current_priority),
                recommendation.currentPriorityContributionLabel,
                Icons.Default.Flag
            ),
            Triple(
                stringResource(R.string.home_review_recommendation_next_priority),
                recommendation.recommendedPriorityContributionLabel,
                Icons.Default.Savings
            )
        )

        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { metric ->
                    RecommendationMetricPill(
                        label = metric.first,
                        value = metric.second,
                        icon = metric.third,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.chunked(2).forEach { rowMetrics ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowMetrics.forEach { metric ->
                            RecommendationMetricPill(
                                label = metric.first,
                                value = metric.second,
                                icon = metric.third,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationMetricPill(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun recommendationTitle(direction: PaydayAdjustmentRecommendationDirection): String {
    return when (direction) {
        PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY ->
            stringResource(R.string.home_review_recommendation_move_title)
        PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER ->
            stringResource(R.string.home_review_recommendation_restore_title)
        PaydayAdjustmentRecommendationDirection.KEEP_PLAN ->
            stringResource(R.string.home_review_recommendation_keep_title)
    }
}

@Composable
private fun recommendationBody(recommendation: PaydayAdjustmentRecommendationState): String {
    return when (recommendation.direction) {
        PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY -> stringResource(
            R.string.home_review_recommendation_move_body,
            recommendation.recommendedFlexibleSpendLabel,
            recommendation.adjustmentAmountLabel,
            recommendation.recommendedPriorityContributionLabel
        )
        PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER -> stringResource(
            R.string.home_review_recommendation_restore_body,
            recommendation.currentFlexibleSpendLabel,
            recommendation.adjustmentAmountLabel,
            recommendation.recommendedPriorityContributionLabel
        )
        PaydayAdjustmentRecommendationDirection.KEEP_PLAN -> stringResource(
            R.string.home_review_recommendation_keep_body,
            recommendation.currentFlexibleSpendLabel,
            recommendation.currentPriorityContributionLabel
        )
    }
}

@Composable
private fun LockedReviewRecommendationCard(
    item: ReviewHistoryItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val body = if (item.isPositive) {
        stringResource(
            R.string.home_review_recommendation_success_body,
            item.plannedFlexibleLabel
        )
    } else {
        stringResource(
            R.string.home_review_recommendation_recovery_body,
            item.plannedFlexibleLabel,
            item.plannedGoalLabel
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_review_recommendation_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_recommendation_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_recommendation_free_label),
                            body = stringResource(R.string.home_review_recommendation_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_recommendation_premium_label),
                            body = stringResource(R.string.home_review_recommendation_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_recommendation_free_label),
                            body = stringResource(R.string.home_review_recommendation_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        ReviewPreviewPill(
                            label = stringResource(R.string.home_review_recommendation_premium_label),
                            body = stringResource(R.string.home_review_recommendation_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.home_review_recommendation_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReviewPreviewPill(
    label: String,
    body: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
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
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LockedReviewHistoryPreviewCard(
    hiddenCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (hiddenCount == 1) {
        stringResource(R.string.home_review_history_locked_title_single)
    } else {
        stringResource(R.string.home_review_history_locked_title, hiddenCount)
    }

    PremiumBenefitCard(
        title = title,
        description = stringResource(R.string.home_review_history_locked_desc),
        icon = Icons.Default.Lock,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun ReviewHistoryTimelinePreviewCard(
    hiddenCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (hiddenCount == 1) {
        stringResource(R.string.home_review_history_archive_title_single)
    } else {
        stringResource(R.string.home_review_history_archive_title, hiddenCount)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.20f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.home_review_history_archive_desc, totalCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CompactReviewHistoryCard(
    item: ReviewHistoryItemState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (item.isPositive) MySharePositive else MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (item.isPositive) {
                            stringResource(R.string.home_review_history_on_plan)
                        } else {
                            stringResource(R.string.home_review_history_needs_attention)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = if (item.isPositive) {
                            Icons.AutoMirrored.Filled.TrendingDown
                        } else {
                            Icons.AutoMirrored.Filled.TrendingUp
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
                ReviewHistoryActionMenu(
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactHistoryMetric(
                            label = stringResource(R.string.home_review_history_flex_label),
                            value = item.flexibleSpendLabel,
                            support = stringResource(R.string.home_review_history_target_delta, item.plannedFlexibleLabel, item.flexibleDeltaLabel)
                        )
                        CompactHistoryMetric(
                            label = stringResource(R.string.home_review_history_goal),
                            value = item.goalContributionLabel,
                            support = stringResource(R.string.home_review_history_target_delta, item.plannedGoalLabel, item.goalDeltaLabel)
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactHistoryMetric(
                            label = stringResource(R.string.home_review_history_flex_label),
                            value = item.flexibleSpendLabel,
                            support = stringResource(R.string.home_review_history_target_delta, item.plannedFlexibleLabel, item.flexibleDeltaLabel),
                            modifier = Modifier.weight(1f)
                        )
                        CompactHistoryMetric(
                            label = stringResource(R.string.home_review_history_goal),
                            value = item.goalContributionLabel,
                            support = stringResource(R.string.home_review_history_target_delta, item.plannedGoalLabel, item.goalDeltaLabel),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewTimelineRow(
    item: ReviewHistoryItemState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (item.isPositive) MySharePositive else MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = if (item.isPositive) {
                            Icons.AutoMirrored.Filled.TrendingDown
                        } else {
                            Icons.AutoMirrored.Filled.TrendingUp
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.isPositive) {
                            stringResource(R.string.home_review_history_on_plan)
                        } else {
                            stringResource(R.string.home_review_history_needs_attention)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ReviewHistoryActionMenu(
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimelineHistoryMetric(
                            label = stringResource(R.string.home_review_history_flex_label),
                            value = item.flexibleSpendLabel,
                            support = stringResource(
                                R.string.home_review_history_target_delta,
                                item.plannedFlexibleLabel,
                                item.flexibleDeltaLabel
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TimelineHistoryMetric(
                            label = stringResource(R.string.home_review_history_goal),
                            value = item.goalContributionLabel,
                            support = stringResource(
                                R.string.home_review_history_target_delta,
                                item.plannedGoalLabel,
                                item.goalDeltaLabel
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimelineHistoryMetric(
                            label = stringResource(R.string.home_review_history_flex_label),
                            value = item.flexibleSpendLabel,
                            support = stringResource(
                                R.string.home_review_history_target_delta,
                                item.plannedFlexibleLabel,
                                item.flexibleDeltaLabel
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TimelineHistoryMetric(
                            label = stringResource(R.string.home_review_history_goal),
                            value = item.goalContributionLabel,
                            support = stringResource(
                                R.string.home_review_history_target_delta,
                                item.plannedGoalLabel,
                                item.goalDeltaLabel
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineHistoryMetric(
    label: String,
    value: String,
    support: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = support,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewHistoryActionMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.home_review_history_actions),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_review_history_edit)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.home_review_history_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun CompactHistoryMetric(
    label: String,
    value: String,
    support: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 86.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = support,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

private const val PREMIUM_INLINE_HISTORY_LIMIT = 3

@Composable
private fun ReviewAmountField(
    label: String,
    value: String,
    currencySymbol: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = { Text(currencySymbol) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        modifier = modifier.bringFocusedInputIntoView(debugLabel = label),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CompactReviewEntryCard(
    flexibleSpend: String,
    goalContribution: String,
    currencySymbol: String,
    errorMessage: String?,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit
) {
    val inputKeyboardActions = rememberInputKeyboardActions(onDone = onSaveReview)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_review_manual_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_manual_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_flex_exact),
                            value = flexibleSpend,
                            currencySymbol = currencySymbol,
                            onValueChange = onFlexibleSpendChanged,
                            modifier = Modifier.fillMaxWidth(),
                            imeAction = ImeAction.Next,
                            keyboardActions = inputKeyboardActions
                        )
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_goal_exact),
                            value = goalContribution,
                            currencySymbol = currencySymbol,
                            onValueChange = onGoalContributionChanged,
                            modifier = Modifier.fillMaxWidth(),
                            imeAction = ImeAction.Done,
                            keyboardActions = inputKeyboardActions
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_flex_exact),
                            value = flexibleSpend,
                            currencySymbol = currencySymbol,
                            onValueChange = onFlexibleSpendChanged,
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next,
                            keyboardActions = inputKeyboardActions
                        )
                        ReviewAmountField(
                            label = stringResource(R.string.home_review_input_goal_exact),
                            value = goalContribution,
                            currencySymbol = currencySymbol,
                            onValueChange = onGoalContributionChanged,
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Done,
                            keyboardActions = inputKeyboardActions
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            PremiumButton(
                text = stringResource(R.string.home_review_submit),
                onClick = onSaveReview
            )
        }
    }
}
