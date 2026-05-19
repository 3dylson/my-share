package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
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
    goalPaydaySplit: GoalPaydaySplitCardState?,
    isPremium: Boolean,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit,
    onOpenGoalArchive: () -> Unit,
    onOpenRuleArchive: () -> Unit,
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
        val visibleGoals = goals.take(
            StrategyCollectionLayoutPolicy.visibleCount(
                totalCount = goals.size,
                isPremium = isPremium
            )
        )
        if (isPremium) {
            if (goalPaydaySplit != null) {
                item {
                    GoalPaydaySplitCard(
                        state = goalPaydaySplit,
                        onClick = onOpenGoalArchive
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else if (goals.size > 1) {
                item {
                    StrategyCollectionSummaryCard(
                        title = stringResource(R.string.home_strategy_goal_stack_title),
                        body = stringResource(
                            R.string.home_strategy_goal_stack_body,
                            goals.size,
                            visibleGoals.size
                        ),
                        metricLabel = stringResource(R.string.home_strategy_goal_stack_metric),
                        metricValue = goals.size.toString(),
                        action = stringResource(R.string.home_strategy_goal_stack_action),
                        icon = Icons.Default.Flag,
                        onClick = onOpenGoalArchive
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_strategy_goal_add_title),
                    description = stringResource(R.string.home_strategy_goal_add_desc),
                    icon = Icons.Default.Add,
                    onClick = onAddNewGoal,
                    modifier = Modifier
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        items(
            items = visibleGoals,
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
        if (!isPremium && goals.size > visibleGoals.size) {
            item {
                LockedStrategyHiddenItemsCard(
                    title = stringResource(
                        R.string.home_strategy_goal_locked_hidden_title,
                        goals.size - visibleGoals.size
                    ),
                    body = stringResource(R.string.home_strategy_goal_locked_hidden_desc),
                    onClick = { onShowPaywall(HomePremiumGate.MultipleGoals) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        if (StrategyCollectionLayoutPolicy.shouldShowPremiumArchive(goals.size, isPremium)) {
            item {
                StrategyArchivePreviewCard(
                    title = hiddenGoalArchiveTitle(
                        hiddenCount = StrategyCollectionLayoutPolicy.hiddenCount(goals.size, isPremium)
                    ),
                    body = stringResource(R.string.home_strategy_goal_archive_body),
                    action = stringResource(R.string.home_strategy_goal_stack_action),
                    icon = Icons.Default.Flag,
                    onClick = onOpenGoalArchive
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        item {
            if (isPremium) {
                Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.height(32.dp))
            }
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
        val visibleRules = rules.take(
            StrategyCollectionLayoutPolicy.visibleCount(
                totalCount = rules.size,
                isPremium = isPremium
            )
        )
        if (isPremium) {
            if (rules.size > 1) {
                item {
                    StrategyCollectionSummaryCard(
                        title = stringResource(R.string.home_strategy_rule_stack_title),
                        body = stringResource(
                            R.string.home_strategy_rule_stack_body,
                            rules.size,
                            visibleRules.size
                        ),
                        metricLabel = stringResource(R.string.home_strategy_rule_stack_metric),
                        metricValue = rules.size.toString(),
                        action = stringResource(R.string.home_strategy_rule_stack_action),
                        icon = Icons.Default.SettingsSuggest,
                        onClick = onOpenRuleArchive
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                PremiumBenefitCard(
                    title = stringResource(R.string.home_strategy_rule_add_title),
                    description = stringResource(R.string.home_strategy_rule_add_desc_free),
                    icon = Icons.Default.Add,
                    onClick = onAddNewRule,
                    modifier = Modifier
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        items(
            items = visibleRules,
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
        if (!isPremium && rules.size > visibleRules.size) {
            item {
                LockedStrategyHiddenItemsCard(
                    title = stringResource(
                        R.string.home_strategy_rule_locked_hidden_title,
                        rules.size - visibleRules.size
                    ),
                    body = stringResource(R.string.home_strategy_rule_locked_hidden_desc),
                    onClick = { onShowPaywall(HomePremiumGate.MultipleRules) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        if (StrategyCollectionLayoutPolicy.shouldShowPremiumArchive(rules.size, isPremium)) {
            item {
                StrategyArchivePreviewCard(
                    title = hiddenRuleArchiveTitle(
                        hiddenCount = StrategyCollectionLayoutPolicy.hiddenCount(rules.size, isPremium)
                    ),
                    body = stringResource(R.string.home_strategy_rule_archive_body),
                    action = stringResource(R.string.home_strategy_rule_stack_action),
                    icon = Icons.Default.SettingsSuggest,
                    onClick = onOpenRuleArchive
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        item {
            if (isPremium) {
                Spacer(Modifier.height(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyGoalArchiveBottomSheet(
    goals: List<GoalCardState>,
    onDismissRequest: () -> Unit,
    onEditGoal: (String) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StrategyArchiveSheetHeader(
                title = stringResource(R.string.home_strategy_goal_sheet_title),
                body = stringResource(R.string.home_strategy_goal_sheet_body, goals.size),
                icon = Icons.Default.Flag
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = goals,
                    key = { goal -> "goal-archive-${goal.id}" }
                ) { goal ->
                    StrategyGoalArchiveRow(
                        goal = goal,
                        goalName = localizedText(context, goal.goalNameKey, goal.goalName),
                        progressLabel = localizedFormattedText(
                            context = context,
                            key = goal.progressLabelKey,
                            args = goal.progressLabelArgs,
                            fallback = goal.progressLabel
                        ),
                        targetDateLabel = localizedFormattedText(
                            context = context,
                            key = goal.targetDateKey,
                            args = goal.targetDateArgs,
                            fallback = goal.targetDateLabel
                        ),
                        onClick = {
                            onDismissRequest()
                            onEditGoal(goal.id)
                        }
                    )
                }
            }

            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_strategy_archive_sheet_close),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyRuleArchiveBottomSheet(
    rules: List<RuleCardState>,
    onDismissRequest: () -> Unit,
    onEditRule: (String) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StrategyArchiveSheetHeader(
                title = stringResource(R.string.home_strategy_rule_sheet_title),
                body = stringResource(R.string.home_strategy_rule_sheet_body, rules.size),
                icon = Icons.Default.SettingsSuggest
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = rules,
                    key = { rule -> "rule-archive-${rule.id}" }
                ) { rule ->
                    val typeLabel = localizedText(context, rule.typeLabelKey, rule.typeLabel)
                    StrategyRuleArchiveRow(
                        rule = rule,
                        ruleName = localizedText(context, rule.nameKey, rule.name),
                        typeLabel = if (rule.name.equals(rule.typeLabel, ignoreCase = true)) {
                            context.getString(R.string.home_strategy_rule_card_subtitle)
                        } else {
                            typeLabel
                        },
                        onClick = {
                            onDismissRequest()
                            onEditRule(rule.id)
                        }
                    )
                }
            }

            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_strategy_archive_sheet_close),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StrategyArchiveSheetHeader(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MySharePrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.padding(10.dp).size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
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

@Composable
private fun StrategyGoalArchiveRow(
    goal: GoalCardState,
    goalName: String,
    progressLabel: String,
    targetDateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useCompactRow = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.25f
            if (useCompactRow) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StrategyArchiveRowIcon(
                            icon = Icons.Default.Flag,
                            compact = true
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = goalName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = goal.goalAmountLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = NumberFormat.getPercentInstance(Locale.getDefault()).format(goal.progress),
                            style = MaterialTheme.typography.labelLarge,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "$progressLabel / $targetDateLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StrategyArchiveRowIcon(icon = Icons.Default.Flag)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = goalName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.goalAmountLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$progressLabel / $targetDateLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = NumberFormat.getPercentInstance(Locale.getDefault()).format(goal.progress),
                        style = MaterialTheme.typography.titleSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyArchiveRowIcon(
    icon: ImageVector,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MySharePrimary.copy(alpha = 0.1f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MySharePrimary,
            modifier = Modifier
                .padding(if (compact) 7.dp else 9.dp)
                .size(if (compact) 18.dp else 20.dp)
        )
    }
}

@Composable
private fun StrategyRuleArchiveRow(
    rule: RuleCardState,
    ruleName: String,
    typeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StrategyArchiveRowIcon(icon = Icons.Default.SettingsSuggest)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = ruleName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = rule.amountLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoalPaydaySplitCard(
    state: GoalPaydaySplitCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GoalPaydaySplitHeader(state)
                        GoalPaydaySplitTotal(
                            totalMoveLabel = state.totalMoveLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoalPaydaySplitHeader(
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                        GoalPaydaySplitTotal(
                            totalMoveLabel = state.totalMoveLabel,
                            modifier = Modifier.widthIn(min = 128.dp, max = 164.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.visibleItems.forEach { item ->
                    GoalPaydaySplitRow(item = item)
                }
                if (state.hiddenGoalCount > 0) {
                    Text(
                        text = hiddenGoalSplitText(state.hiddenGoalCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = stringResource(R.string.home_strategy_goal_stack_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalPaydaySplitHeader(
    state: GoalPaydaySplitCardState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MySharePrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Default.AutoGraph,
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
                text = stringResource(R.string.home_strategy_goal_split_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.home_strategy_goal_split_body,
                    state.totalMoveLabel,
                    state.goalCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoalPaydaySplitTotal(
    totalMoveLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.home_strategy_goal_split_total_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = totalMoveLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoalPaydaySplitRow(
    item: GoalPaydaySplitItemState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val goalName = localizedText(context, item.goalNameKey, item.goalName)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val shouldStack = maxWidth < 300.dp || LocalDensity.current.fontScale >= 1.3f
            if (shouldStack) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoalPaydaySplitRowLabel(
                        goalName = goalName,
                        shareLabel = item.shareLabel
                    )
                    Text(
                        text = item.amountLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoalPaydaySplitRowLabel(
                        goalName = goalName,
                        shareLabel = item.shareLabel,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.amountLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalPaydaySplitRowLabel(
    goalName: String,
    shareLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = MySharePrimary,
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = goalName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = shareLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StrategyCollectionSummaryCard(
    title: String,
    body: String,
    metricLabel: String,
    metricValue: String,
    action: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.18f))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
            if (shouldStack) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StrategyCollectionSummaryHeader(
                        title = title,
                        body = body,
                        icon = icon
                    )
                    StrategyCollectionSummaryMetric(
                        label = metricLabel,
                        value = metricValue,
                        action = action,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StrategyCollectionSummaryHeader(
                        title = title,
                        body = body,
                        icon = icon,
                        modifier = Modifier.weight(1f)
                    )
                    StrategyCollectionSummaryMetric(
                        label = metricLabel,
                        value = metricValue,
                        action = action,
                        modifier = Modifier.widthIn(min = 132.dp, max = 172.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyCollectionSummaryHeader(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StrategyCollectionSummaryMetric(
    label: String,
    value: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StrategyArchivePreviewCard(
    title: String,
    body: String,
    action: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelMedium,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LockedStrategyHiddenItemsCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumBenefitCard(
        title = title,
        description = body,
        icon = Icons.Default.Lock,
        onClick = onClick,
        modifier = modifier
    )
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

private fun localizedFormattedText(
    context: android.content.Context,
    key: String?,
    args: List<String>,
    fallback: String
): String {
    if (key == null) return fallback
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId, *args.toTypedArray()) else fallback
}

@Composable
private fun hiddenGoalSplitText(hiddenCount: Int): String {
    return if (hiddenCount == 1) {
        stringResource(R.string.home_strategy_goal_split_hidden_single)
    } else {
        stringResource(R.string.home_strategy_goal_split_hidden, hiddenCount)
    }
}

@Composable
private fun hiddenGoalArchiveTitle(hiddenCount: Int): String {
    return if (hiddenCount == 1) {
        stringResource(R.string.home_strategy_goal_archive_title_single)
    } else {
        stringResource(R.string.home_strategy_goal_archive_title, hiddenCount)
    }
}

@Composable
private fun hiddenRuleArchiveTitle(hiddenCount: Int): String {
    return if (hiddenCount == 1) {
        stringResource(R.string.home_strategy_rule_archive_title_single)
    } else {
        stringResource(R.string.home_strategy_rule_archive_title, hiddenCount)
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
