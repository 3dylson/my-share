package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.theme.MySharePrimary

@Composable
fun RulePaydayMixCard(
    state: RulePaydayMixCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.25f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RulePaydayMixHeader(state)
                        RulePaydayMixTotal(
                            totalMoveLabel = state.totalMoveLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RulePaydayMixHeader(
                            state = state,
                            modifier = Modifier.weight(1f)
                        )
                        RulePaydayMixTotal(
                            totalMoveLabel = state.totalMoveLabel,
                            modifier = Modifier.widthIn(min = 128.dp, max = 164.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.visibleItems.forEach { item ->
                    RulePaydayMixRow(item = item)
                }
                if (state.hiddenRuleCount > 0) {
                    Text(
                        text = hiddenRuleMixText(state.hiddenRuleCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = stringResource(R.string.home_strategy_rule_stack_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RulePaydayMixHeader(
    state: RulePaydayMixCardState,
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
                text = stringResource(R.string.home_strategy_rule_mix_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.home_strategy_rule_mix_body,
                    state.totalMoveLabel,
                    state.ruleCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RulePaydayMixTotal(
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
                text = stringResource(R.string.home_strategy_rule_mix_total_label).uppercase(),
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
private fun RulePaydayMixRow(
    item: RulePaydayMixItemState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ruleName = localizedText(context, item.ruleNameKey, item.ruleName)
    val typeLabel = localizedText(context, item.typeLabelKey, item.typeLabel)
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
                    RulePaydayMixRowLabel(
                        ruleName = ruleName,
                        typeLabel = typeLabel,
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
                    RulePaydayMixRowLabel(
                        ruleName = ruleName,
                        typeLabel = typeLabel,
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
private fun RulePaydayMixRowLabel(
    ruleName: String,
    typeLabel: String,
    shareLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SettingsSuggest,
            contentDescription = null,
            tint = MySharePrimary,
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = ruleName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.home_strategy_rule_mix_item_body, typeLabel, shareLabel),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun hiddenRuleMixText(hiddenCount: Int): String {
    return if (hiddenCount == 1) {
        stringResource(R.string.home_strategy_rule_mix_hidden_single)
    } else {
        stringResource(R.string.home_strategy_rule_mix_hidden, hiddenCount)
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
