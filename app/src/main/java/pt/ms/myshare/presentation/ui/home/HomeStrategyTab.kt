package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.PremiumBenefitCard
import pt.ms.myshare.presentation.ui.components.PremiumGoalCard
import pt.ms.myshare.presentation.ui.components.PremiumRuleCard
import pt.ms.myshare.presentation.ui.components.PremiumSectionHeader

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.homeStrategyTab(
    goals: List<GoalCardState>,
    rules: List<RuleCardState>,
    isPremium: Boolean,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit,
    onShowPaywall: () -> Unit
) {
    // === GOALS SECTION ===
    item {
        PremiumSectionHeader(title = "Financial Goals")
    }

    if (goals.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = "Set Your Sights",
                description = "Define what you're saving for. We'll track your progress toward your biggest milestones.",
                icon = Icons.Default.Flag,
                onClick = onAddNewGoal,
                modifier = Modifier.animateItemPlacement()
            )
        }
    } else {
        items(
            items = goals,
            key = { it.id }
        ) { goal ->
            PremiumGoalCard(
                goalName = goal.goalName,
                goalAmountLabel = goal.goalAmountLabel,
                progress = goal.progress,
                progressLabel = goal.progressLabel,
                targetDateLabel = goal.targetDateLabel,
                progressNote = goal.progressNote,
                onClick = { onEditGoal(goal.id) },
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            PremiumBenefitCard(
                title = "Add Another Goal",
                description = "Got more aspirations? Create another target.",
                icon = Icons.Default.Add,
                onClick = onAddNewGoal,
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    // === RULES SECTION ===
    item {
        PremiumSectionHeader(title = "Automated Rules")
    }

    if (rules.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = "Create a Rule",
                description = "Automate your savings or investments based on your income.",
                icon = Icons.Default.SettingsSuggest,
                onClick = onAddNewRule,
                modifier = Modifier.animateItemPlacement()
            )
        }
    } else {
        items(
            items = rules,
            key = { it.id }
        ) { rule ->
            PremiumRuleCard(
                ruleName = rule.name,
                amountLabel = rule.amountLabel,
                typeLabel = rule.typeLabel,
                onClick = { onEditRule(rule.id) },
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            PremiumBenefitCard(
                title = "Add Another Rule",
                description = if (isPremium || rules.isEmpty()) "Create an automated transfer rule" else "Upgrade to Premium to add multiple rules",
                icon = Icons.Default.Add,
                onClick = {
                    if (isPremium || rules.isEmpty()) onAddNewRule() else onShowPaywall()
                },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
