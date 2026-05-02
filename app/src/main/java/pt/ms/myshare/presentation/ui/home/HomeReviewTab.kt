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
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*

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
            PremiumSectionHeader(title = "Performance Insights")
            
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
                            text = "Trust Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MyShareSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${performanceStats.healthScore}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MyShareOnSurface
                        )
                        Text(
                            text = "${performanceStats.totalReviews} reviews",
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
                            text = "Current Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = MyShareSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${performanceStats.currentStreak}",
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
                            text = "Day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareOnSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (performanceStats.totalFlexSavingsLabel.isNotEmpty() && performanceStats.totalFlexSavingsLabel != "$0.00") {
            item {
                PremiumBenefitCard(
                    title = "Excess Savings: ${performanceStats.totalFlexSavingsLabel}",
                    description = "This is how much you've stayed under budget across all your sessions. Great job!",
                    icon = Icons.Default.Savings,
                    onClick = {}
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (coachingInsights.isNotEmpty() && isPremium) {
        item {
            PremiumSectionHeader(title = "Coach's Corner")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                coachingInsights.forEach { insight ->
                    val icon = when (insight.type) {
                        pt.ms.myshare.domain.model.InsightType.SUCCESS -> Icons.Default.CheckCircle
                        pt.ms.myshare.domain.model.InsightType.WARNING -> Icons.Default.Warning
                        pt.ms.myshare.domain.model.InsightType.TIP -> Icons.Default.AutoAwesome
                    }

                    PremiumBenefitCard(
                        title = insight.headline,
                        description = insight.supportingText,
                        icon = icon,
                        onClick = {}
                    )
                }
            }
        }
    }

    item {
        PremiumSectionHeader(title = "The Habit Loop")
        PremiumInfoCard(
            title = "Manual Check-in",
            body = "Log actual spend vs. blueprint. This feedback loop is the key to financial awareness.",
            icon = Icons.Default.HistoryEdu,
            backgroundColor = pt.ms.myshare.presentation.ui.theme.MySharePrimary.copy(alpha = 0.05f)
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PremiumSliderCard(
                title = "Actual Flexible Spend",
                value = state.actualFlexibleSpend.toFloatOrNull() ?: 0f,
                onValueChange = { onFlexibleSpendChanged(it.toInt().toString()) },
                valueRange = 0f..5000f,
                icon = Icons.Default.ShoppingCart
            )
            PremiumSliderCard(
                title = "Actual Goal Contribution",
                value = state.actualGoalContribution.toFloatOrNull() ?: 0f,
                onValueChange = { onGoalContributionChanged(it.toInt().toString()) },
                valueRange = 0f..5000f,
                icon = Icons.Default.Flag
            )

            PremiumButton(
                text = "Submit Review",
                onClick = onSaveReview,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    item {
        PremiumSectionHeader(title = "Historical Performance")
    }

    if (history.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = "No History Yet",
                description = "Complete your first review above to start building a timeline of your financial mastery.",
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
                        value = "Flex: ${item.flexibleSpendLabel}",
                        subtitle = "Target: ${item.plannedFlexibleLabel} (${item.flexibleDeltaLabel})",
                        icon = if (item.isPositive) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        color = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        indicatorColor = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    PremiumMetricCard(
                        label = "Goal contribution",
                        value = item.goalContributionLabel,
                        subtitle = "Plan: ${item.plannedGoalLabel} (${item.goalDeltaLabel})",
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
                    title = "Trajectory Insights",
                    description = "Upgrade to see the full list of past performances and detect spending patterns over time.",
                    icon = Icons.Default.Lock,
                    onClick = onShowPaywall
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
