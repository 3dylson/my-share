package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

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
    onShowPaywall: () -> Unit
) {
    val coachingInsights = state.coachingInsights
    
    if (history.isNotEmpty()) {
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_review_performance_title))
            
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
                color = MySharePrimaryContainer.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_review_score_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MyShareSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.home_review_score_percent, performanceStats.healthScore),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MyShareOnSurface
                        )
                        Text(
                            text = stringResource(R.string.home_review_score_count, performanceStats.totalReviews),
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareOnSurfaceVariant
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .align(Alignment.CenterVertically)
                            .background(MyShareOutline.copy(alpha = 0.2f))
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_review_streak_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MyShareSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.home_review_streak_count, performanceStats.currentStreak),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MyShareOnSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (performanceStats.totalFlexSavingsLabel.isNotEmpty() && performanceStats.totalSavings > BigDecimal.ZERO) {
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

    if (coachingInsights.isNotEmpty() && isPremium) {
        item {
            val context = LocalContext.current
            PremiumSectionHeader(title = stringResource(R.string.home_review_coach_title))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        // Show history. If not premium, only show the first item and then a lock.
        val visibleHistory = if (isPremium) history else history.take(1)
        visibleHistory.forEach { item ->
            item(key = item.id) {
                CompactReviewHistoryCard(item = item)
                Spacer(Modifier.height(12.dp))
            }
        }
        if (!isPremium && history.size > 1) {
            item {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_review_history_trajectory_title),
                    description = stringResource(R.string.home_review_history_trajectory_desc),
                    icon = Icons.Default.Lock,
                    onClick = onShowPaywall
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CompactReviewHistoryCard(
    item: ReviewHistoryItemState,
    modifier: Modifier = Modifier
) {
    val accentColor = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.14f)),
        shadowElevation = 2.dp
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
                        color = MyShareSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (item.isPositive) {
                            stringResource(R.string.home_review_history_on_plan)
                        } else {
                            stringResource(R.string.home_review_history_needs_attention)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MyShareOnSurfaceVariant
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
            }

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
        color = MySharePrimaryContainer.copy(alpha = 0.22f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MyShareSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MyShareOnSurface,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = support,
                style = MaterialTheme.typography.labelSmall,
                color = MyShareOnSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ReviewAmountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = { Text("$") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CompactReviewEntryCard(
    flexibleSpend: String,
    goalContribution: String,
    errorMessage: String?,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
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
                        color = MyShareOnSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_review_manual_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MyShareSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewAmountField(
                    label = stringResource(R.string.home_review_input_flex_exact),
                    value = flexibleSpend,
                    onValueChange = onFlexibleSpendChanged,
                    modifier = Modifier.weight(1f)
                )
                ReviewAmountField(
                    label = stringResource(R.string.home_review_input_goal_exact),
                    value = goalContribution,
                    onValueChange = onGoalContributionChanged,
                    modifier = Modifier.weight(1f)
                )
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
