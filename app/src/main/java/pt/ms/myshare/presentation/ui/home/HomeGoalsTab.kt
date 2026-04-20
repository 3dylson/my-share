package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MySharePrimaryContainer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Flag

/**
 * Responsibility: Renders the Financial Milestones (Goals) list and strategy notes.
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homeGoalsTab(
    goals: List<GoalCardState>,
    isPremium: Boolean,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit
) {
    if (goals.isEmpty()) {
        item {
            PremiumSectionHeader(title = "No Active Goals")
            PremiumInfoCard(
                title = "Define your vision",
                body = "Add your first financial milestone to start tracking your trajectory.",
                icon = Icons.Default.Flag
            )
        }
    } else {
        item {
            PremiumSectionHeader(title = "Financial Milestones")
        }
        goals.forEach { goalCard ->
            val goalIcon = when {
                goalCard.goalName.contains("Emergency", ignoreCase = true) -> Icons.Default.Shield
                goalCard.goalName.contains("Vacation", ignoreCase = true) || goalCard.goalName.contains("Travel", ignoreCase = true) -> Icons.Default.FlightTakeoff
                goalCard.goalName.contains("Home", ignoreCase = true) || goalCard.goalName.contains("House", ignoreCase = true) -> Icons.Default.Home
                goalCard.goalName.contains("Car", ignoreCase = true) -> Icons.Default.DirectionsCar
                goalCard.goalName.contains("Invest", ignoreCase = true) -> Icons.Default.TrendingUp
                else -> Icons.Default.Flag
            }
            
            item {
                PremiumGoalCard(
                    goalName = goalCard.goalName,
                    targetAmountLabel = goalCard.goalAmountLabel,
                    progress = goalCard.progress,
                    progressLabel = goalCard.progressLabel,
                    targetDateLabel = goalCard.targetDateLabel,
                    icon = goalIcon,
                    onClick = { onEditGoal(goalCard.id) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (!isPremium && goals.size >= 1) {
        item {
            PremiumBenefitCard(
                title = "Unlock Multi-Goal Support",
                description = "Free tier is limited to one active goal. Upgrade to track travel, home, and emergency funds simultaneously.",
                icon = Icons.Default.Lock,
                onClick = { onDestinationSelected(HomeDestination.MORE) }
            )
        }
    } else {
        item {
            PremiumButton(
                text = "Add New Goal",
                onClick = onAddNewGoal,
                icon = Icons.Default.Add,
                containerColor = MySharePrimaryContainer,
                contentColor = MySharePrimary
            )
        }
    }

    item {
        PremiumInfoCard(
            title = "Strategy Note",
            body = "Focus is your superpower. While we support multiple goals, keeping your 'Primary Objective' in mind helps maintain momentum.",
            icon = Icons.Default.TipsAndUpdates,
            backgroundColor = MySharePrimaryContainer.copy(alpha = 0.6f)
        )
    }
}
