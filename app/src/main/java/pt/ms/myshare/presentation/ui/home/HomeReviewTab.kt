package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
                            text = stringResource(R.string.home_review_streak_count, performanceStats.healthScore) + "%",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.home_review_streak_count, performanceStats.currentStreak),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MyShareOnSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = pt.ms.myshare.presentation.ui.theme.MyShareSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.home_review_streak_unit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareOnSurfaceVariant
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

    val context = LocalContext.current
    if (coachingInsights.isNotEmpty() && isPremium) {
        item {
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
        PremiumSectionHeader(title = stringResource(R.string.home_review_habit_title))
        PremiumInfoCard(
            title = stringResource(R.string.home_review_manual_title),
            body = stringResource(R.string.home_review_manual_desc),
            icon = Icons.Default.HistoryEdu,
            backgroundColor = pt.ms.myshare.presentation.ui.theme.MySharePrimary.copy(alpha = 0.05f)
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
            
            PremiumSliderCard(
                title = stringResource(R.string.home_review_input_flex),
                value = state.actualFlexibleSpend.toFloatOrNull() ?: 0f,
                onValueChange = { onFlexibleSpendChanged(it.toInt().toString()) },
                valueRange = 0f..5000f,
                icon = Icons.Default.ShoppingCart,
                formatValue = { currencyFormat.format(it) }
            )
            PremiumSliderCard(
                title = stringResource(R.string.home_review_input_goal),
                value = state.actualGoalContribution.toFloatOrNull() ?: 0f,
                onValueChange = { onGoalContributionChanged(it.toInt().toString()) },
                valueRange = 0f..5000f,
                icon = Icons.Default.Flag,
                formatValue = { currencyFormat.format(it) }
            )

            if (state.error != null) {
                val errorMessage = remember(state.error) {
                    val resId = context.resources.getIdentifier(state.error, "string", context.packageName)
                    if (resId != 0) context.getString(resId) else state.error
                }
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            PremiumButton(
                text = stringResource(R.string.home_review_submit),
                onClick = onSaveReview,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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
                Column(modifier = Modifier.animateItemPlacement()) {
                    PremiumMetricCard(
                        label = item.dateLabel,
                        value = stringResource(R.string.home_review_history_flex, item.flexibleSpendLabel),
                        subtitle = stringResource(R.string.home_review_history_flex_target, item.plannedFlexibleLabel, item.flexibleDeltaLabel),
                        icon = if (item.isPositive) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        color = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        indicatorColor = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    PremiumMetricCard(
                        label = stringResource(R.string.home_review_history_goal),
                        value = item.goalContributionLabel,
                        subtitle = stringResource(R.string.home_review_history_goal_plan, item.plannedGoalLabel, item.goalDeltaLabel),
                        icon = if (item.isPositive) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                        color = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        indicatorColor = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        onClick = {}
                    )
                    Spacer(Modifier.height(20.dp))
                }
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
