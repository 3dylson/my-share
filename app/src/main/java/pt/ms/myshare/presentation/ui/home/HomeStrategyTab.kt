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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumBenefitCard
import pt.ms.myshare.presentation.ui.components.PremiumProgressBar
import pt.ms.myshare.presentation.ui.components.PremiumSectionHeader
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.homeStrategyTab(
    goals: List<GoalCardState>,
    rules: List<RuleCardState>,
    planCard: HomePlanCardState?,
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
            val localizedGoalName = goal.goalNameKey?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else null
            } ?: goal.goalName
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
                goalName = localizedGoalName,
                targetAmountLabel = goal.goalAmountLabel,
                progress = goal.progress,
                progressLabel = progressLabel,
                targetDateLabel = targetDateLabel,
                isLocked = goal.isLockedByEntitlement,
                onClick = {
                    if (goal.isLockedByEntitlement) {
                        onShowPaywall(HomePremiumGate.MultipleGoals)
                    } else {
                        onEditGoal(goal.id)
                    }
                },
                modifier = Modifier
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            if (isPremium) {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_strategy_goal_add_title),
                    description = stringResource(R.string.home_strategy_goal_add_desc),
                    icon = Icons.Default.Add,
                    onClick = onAddNewGoal,
                    modifier = Modifier
                )
            } else {
                val context = LocalContext.current
                LockedStrategyPreviewCard(
                    title = stringResource(R.string.home_strategy_goal_preview_title),
                    body = stringResource(
                        R.string.home_strategy_goal_preview_body,
                        planCard?.savingsLabel ?: stringResource(R.string.home_strategy_goal_preview_priority_fallback),
                        localizedText(
                            context = context,
                            key = goals.first().goalNameKey,
                            fallback = goals.first().goalName
                        )
                    ),
                    freeLabel = stringResource(R.string.home_strategy_goal_preview_free_label),
                    freeBody = stringResource(R.string.home_strategy_goal_preview_free_body),
                    premiumLabel = stringResource(R.string.home_strategy_goal_preview_premium_label),
                    premiumBody = stringResource(R.string.home_strategy_goal_preview_premium_body),
                    action = stringResource(R.string.home_strategy_goal_preview_action),
                    onClick = { onShowPaywall(HomePremiumGate.MultipleGoals) }
                )
            }
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
            val localizedRuleName = rule.nameKey?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else null
            } ?: rule.name
            CompactStrategyRuleCard(
                ruleName = localizedRuleName,
                amountLabel = rule.amountLabel,
                typeLabel = if (rule.name.equals(rule.typeLabel, ignoreCase = true)) {
                    stringResource(R.string.home_strategy_rule_card_subtitle)
                } else {
                    localizedTypeLabel
                },
                isPremium = isPremium,
                isLocked = rule.isLockedByEntitlement,
                onClick = {
                    if (rule.isLockedByEntitlement) {
                        onShowPaywall(HomePremiumGate.MultipleRules)
                    } else {
                        onEditRule(rule.id)
                    }
                },
                modifier = Modifier
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            if (isPremium) {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_strategy_rule_add_title),
                    description = stringResource(R.string.home_strategy_rule_add_desc_free),
                    icon = Icons.Default.Add,
                    onClick = onAddNewRule,
                    modifier = Modifier
                )
            } else {
                val context = LocalContext.current
                LockedStrategyPreviewCard(
                    title = stringResource(R.string.home_strategy_rule_preview_title),
                    body = stringResource(
                        R.string.home_strategy_rule_preview_body,
                        localizedText(
                            context = context,
                            key = rules.first().nameKey,
                            fallback = rules.first().name
                        )
                    ),
                    freeLabel = stringResource(R.string.home_strategy_rule_preview_free_label),
                    freeBody = stringResource(R.string.home_strategy_rule_preview_free_body),
                    premiumLabel = stringResource(R.string.home_strategy_rule_preview_premium_label),
                    premiumBody = stringResource(R.string.home_strategy_rule_preview_premium_body),
                    action = stringResource(R.string.home_strategy_rule_preview_action),
                    onClick = { onShowPaywall(HomePremiumGate.MultipleRules) }
                )
            }
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
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = targetAmountLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = NumberFormat.getPercentInstance(Locale.getDefault()).format(progress),
                        style = MaterialTheme.typography.titleMedium,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    if (isLocked) {
                        Text(
                            text = stringResource(R.string.premium_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            PremiumProgressBar(progress = progress, color = MySharePrimary)

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StrategyStatusChip(
                            text = progressLabel,
                            icon = Icons.Default.Flag
                        )
                        StrategyStatusChip(
                            text = targetDateLabel,
                            icon = Icons.Default.AutoGraph
                        )
                    }
                } else {
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
    }
}

@Composable
private fun CompactStrategyRuleCard(
    ruleName: String,
    amountLabel: String,
    typeLabel: String,
    isPremium: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.3f
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isLocked) {
                        Text(
                            text = stringResource(R.string.premium_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (!isPremium) {
                        Text(
                            text = stringResource(R.string.home_strategy_rule_static_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (shouldStack) {
                        Text(
                            text = amountLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                if (!shouldStack) {
                    Text(
                        text = amountLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedStrategyPreviewCard(
    title: String,
    body: String,
    freeLabel: String,
    freeBody: String,
    premiumLabel: String,
    premiumBody: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.24f)),
        shadowElevation = 1.dp
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
                        text = stringResource(R.string.home_strategy_locked_preview_label).uppercase(),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StrategyPreviewPill(
                            label = freeLabel,
                            body = freeBody,
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        StrategyPreviewPill(
                            label = premiumLabel,
                            body = premiumBody,
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StrategyPreviewPill(
                            label = freeLabel,
                            body = freeBody,
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        StrategyPreviewPill(
                            label = premiumLabel,
                            body = premiumBody,
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StrategyPreviewPill(
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun localizedText(
    context: android.content.Context,
    key: String?,
    fallback: String
): String {
    if (key == null) return fallback
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else fallback
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.home_strategy_workspace_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
