package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary

/**
 * Responsibility: Renders the Manual Review form and historical performance records.
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homeReviewTab(
    state: ReviewCardState,
    history: List<ReviewHistoryItemState>,
    isPremium: Boolean,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit
) {
    item {
        PremiumSectionHeader(title = "The Habit Loop")
        PremiumInfoCard(
            title = "Manual Check-in",
            body = "Log actual spend vs. blueprint. This feedback loop is the key to financial awareness.",
            icon = Icons.Default.HistoryEdu
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PremiumTextField(
                value = state.actualFlexibleSpend,
                onValueChange = onFlexibleSpendChanged,
                label = "Actual Flexible Spend",
                prefix = { Text("$ ", color = MyShareSecondary) }
            )
            PremiumTextField(
                value = state.actualGoalContribution,
                onValueChange = onGoalContributionChanged,
                label = "Actual Goal Contribution",
                prefix = { Text("$ ", color = MyShareSecondary) }
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
            PremiumInfoCard(
                title = "No History Yet",
                body = "Complete your first review to start building a timeline of your financial mastery.",
                icon = Icons.Default.BarChart
            )
        }
    } else {
        // Show history. If not premium, only show the first item and then a lock.
        val visibleHistory = if (isPremium) history else history.take(1)
        visibleHistory.forEach { item ->
            item {
                PremiumMetricCard(
                    label = item.dateLabel,
                    value = item.flexibleDeltaLabel,
                    subtitle = "Goal: ${item.goalDeltaLabel}",
                    icon = if (item.isPositive) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    color = if (item.isPositive) MyShareSecondary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        if (!isPremium && history.size > 1) {
            item {
                PremiumBenefitCard(
                    title = "Trajectory Insights",
                    description = "Upgrade to see the full list of past performances and detect spending patterns over time.",
                    icon = Icons.Default.Lock,
                    onClick = { onDestinationSelected(HomeDestination.MORE) }
                )
                Spacer(Modifier.height(8.dp))
                PremiumAdBanner()
            }
        }
    }
}
