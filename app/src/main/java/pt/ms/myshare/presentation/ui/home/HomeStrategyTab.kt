package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumBenefitCard
import pt.ms.myshare.presentation.ui.components.PremiumProgressBar
import pt.ms.myshare.presentation.ui.components.PremiumSectionHeader
import pt.ms.myshare.presentation.ui.theme.MyShareOnSurface
import pt.ms.myshare.presentation.ui.theme.MyShareOnSurfaceVariant
import pt.ms.myshare.presentation.ui.theme.MyShareOutline
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MySharePrimaryContainer
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.homeStrategyTab(
    goals: List<GoalCardState>,
    rules: List<RuleCardState>,
    isPremium: Boolean,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit,
    onShowPaywall: (HomePremiumGate) -> Unit
) {
    if (goals.isEmpty() && rules.isEmpty()) {
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_strategy_workspace_title))
            EmptyStrategyWorkspace(
                onAddNewGoal = onAddNewGoal,
                onAddNewRule = onAddNewRule
            )
        }
        return
    }

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
                modifier = Modifier
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

            CompactStrategyGoalCard(
                goalName = goal.goalName,
                targetAmountLabel = goal.goalAmountLabel,
                progress = goal.progress,
                progressLabel = progressLabel,
                targetDateLabel = targetDateLabel,
                onClick = { onEditGoal(goal.id) },
                modifier = Modifier
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            PremiumBenefitCard(
                title = stringResource(R.string.home_strategy_goal_add_title),
                description = if (isPremium || goals.isEmpty())
                    stringResource(R.string.home_strategy_goal_add_desc)
                else
                    stringResource(R.string.home_strategy_goal_add_desc_premium),
                icon = Icons.Default.Add,
                onClick = {
                    if (isPremium || goals.isEmpty()) {
                        onAddNewGoal()
                    } else {
                        onShowPaywall(HomePremiumGate.MultipleGoals)
                    }
                },
                modifier = Modifier
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
                modifier = Modifier
            )
        }
    } else {
        items(
            items = rules,
            key = { it.id }
        ) { rule ->
            val context = LocalContext.current
            val localizedTypeLabel = rule.typeLabelKey?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else null
            } ?: rule.typeLabel
            CompactStrategyRuleCard(
                ruleName = rule.name,
                amountLabel = rule.amountLabel,
                typeLabel = if (rule.name.equals(rule.typeLabel, ignoreCase = true)) {
                    stringResource(R.string.home_strategy_rule_card_subtitle)
                } else {
                    localizedTypeLabel
                },
                onClick = { onEditRule(rule.id) },
                modifier = Modifier
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
                    if (isPremium || rules.isEmpty()) {
                        onAddNewRule()
                    } else {
                        onShowPaywall(HomePremiumGate.MultipleRules)
                    }
                },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun CompactStrategyGoalCard(
    goalName: String,
    targetAmountLabel: String,
    progress: Float,
    progressLabel: String,
    targetDateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goalName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MyShareOnSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = targetAmountLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MyShareSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Black
                )
            }

            PremiumProgressBar(progress = progress, color = MySharePrimary)

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StrategyStatusChip(
                    text = progressLabel,
                    icon = Icons.Default.Flag,
                    modifier = Modifier.weight(1f)
                )
                StrategyStatusChip(
                    text = targetDateLabel,
                    icon = Icons.Default.AutoGraph,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactStrategyRuleCard(
    ruleName: String,
    amountLabel: String,
    typeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.16f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsSuggest,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ruleName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MyShareOnSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MyShareSecondary
                )
            }
            Text(
                text = amountLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MyShareOnSurface,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun StrategyStatusChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MySharePrimaryContainer.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MyShareSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MyShareSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyStrategyWorkspace(
    onAddNewGoal: () -> Unit,
    onAddNewRule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.home_strategy_workspace_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MyShareOnSurfaceVariant
            )
            StrategyActionRow(
                title = stringResource(R.string.home_strategy_goal_empty_title),
                description = stringResource(R.string.home_strategy_goal_empty_desc),
                icon = Icons.Default.Flag,
                onClick = onAddNewGoal
            )
            StrategyActionRow(
                title = stringResource(R.string.home_strategy_rule_empty_title),
                description = stringResource(R.string.home_strategy_rule_empty_desc),
                icon = Icons.Default.SettingsSuggest,
                onClick = onAddNewRule
            )
        }
    }
}

@Composable
private fun StrategyActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MySharePrimaryContainer.copy(alpha = 0.24f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MyShareOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MyShareSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
