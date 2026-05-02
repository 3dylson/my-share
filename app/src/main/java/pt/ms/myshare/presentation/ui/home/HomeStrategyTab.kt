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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
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
        PremiumSectionHeader(title = stringResource(R.string.home_strategy_goals_title))
    }

    if (goals.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = stringResource(R.string.home_strategy_goal_empty_title),
                description = stringResource(R.string.home_strategy_goal_empty_desc),
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
            val context = LocalContext.current
            val progressLabel = if (goal.progressLabelKey != null) {
                stringResource(
                    context.resources.getIdentifier(goal.progressLabelKey, "string", context.packageName),
                    *goal.progressLabelArgs.toTypedArray()
                )
            } else goal.progressLabel

            val targetDateLabel = if (goal.targetDateKey != null) {
                stringResource(
                    context.resources.getIdentifier(goal.targetDateKey, "string", context.packageName),
                    *goal.targetDateArgs.toTypedArray()
                )
            } else goal.targetDateLabel

            val progressNote = if (goal.progressNoteKey != null) {
                stringResource(
                    context.resources.getIdentifier(goal.progressNoteKey, "string", context.packageName)
                )
            } else goal.progressNote

            PremiumGoalCard(
                goalName = goal.goalName,
                goalAmountLabel = goal.goalAmountLabel,
                progress = goal.progress,
                progressLabel = progressLabel,
                targetDateLabel = targetDateLabel,
                progressNote = progressNote,
                onClick = { onEditGoal(goal.id) },
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            PremiumBenefitCard(
                title = stringResource(R.string.home_strategy_goal_add_title),
                description = stringResource(R.string.home_strategy_goal_add_desc),
                icon = Icons.Default.Add,
                onClick = onAddNewGoal,
                modifier = Modifier.animateItemPlacement()
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    // === RULES SECTION ===
    item {
        PremiumSectionHeader(title = stringResource(R.string.home_strategy_rules_title))
    }

    if (rules.isEmpty()) {
        item {
            PremiumBenefitCard(
                title = stringResource(R.string.home_strategy_rule_empty_title),
                description = stringResource(R.string.home_strategy_rule_empty_desc),
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
                title = stringResource(R.string.home_strategy_rule_add_title),
                description = if (isPremium || rules.isEmpty()) 
                    stringResource(R.string.home_strategy_rule_add_desc_free) 
                else 
                    stringResource(R.string.home_strategy_rule_add_desc_premium),
                icon = Icons.Default.Add,
                onClick = {
                    if (isPremium || rules.isEmpty()) onAddNewRule() else onShowPaywall()
                },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
