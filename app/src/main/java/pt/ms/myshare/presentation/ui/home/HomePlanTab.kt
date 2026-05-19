package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MySharePositive

/**
 * Responsibility: Renders the core financial plan dashboard (Income, Metrics, Allocation Preview).
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homePlanTab(
    planCard: HomePlanCardState?,
    isPremium: Boolean,
    smartAdjustment: SmartAdjustmentControlState,
    premiumCheckIn: PremiumCheckInState?,
    onOpenPremiumControls: () -> Unit,
    onShowPaywall: (HomePremiumGate) -> Unit
) {
    planCard?.let { card ->
        item {
            val context = LocalContext.current
            val headline = if (card.nextPaydayKey != null) {
                stringResource(
                    context.resources.getIdentifier(card.nextPaydayKey, "string", context.packageName),
                    *card.nextPaydayArgs.toTypedArray()
                )
            } else card.nextPaydayLabel
            val summary = remember(card.summary) {
                val resId = context.resources.getIdentifier(card.summary, "string", context.packageName)
                if (resId != 0) context.getString(resId) else card.summary
            }

            CompactPaydaySummary(
                headline = headline,
                body = summary
            )
        }
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_plan_metrics_title))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    CompactPlanMetricsPanel(
                        incomeLabel = stringResource(R.string.home_plan_label_income),
                        incomeValue = card.incomeLabel,
                        weeklyLabel = stringResource(R.string.home_plan_label_weekly),
                        weeklyValue = card.weeklySpendLabel
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactPlanMetric(
                            label = stringResource(R.string.home_plan_label_income),
                            value = card.incomeLabel,
                            icon = Icons.Default.Payments,
                            modifier = Modifier.weight(1f)
                        )
                        CompactPlanMetric(
                            label = stringResource(R.string.home_plan_label_weekly),
                            value = card.weeklySpendLabel,
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = MySharePositive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_plan_allocation_title))
            CompactAllocationGrid(
                items = listOf(
                    AllocationGridItem(
                        label = stringResource(R.string.home_plan_label_fixed),
                        value = card.fixedCostsLabel,
                        icon = Icons.Default.AccountBalance
                    ),
                    AllocationGridItem(
                        label = stringResource(R.string.home_plan_label_flexible),
                        value = card.flexibleSpendLabel,
                        icon = Icons.Default.ShoppingBag,
                        accentColor = MySharePositive
                    ),
                    AllocationGridItem(
                        label = stringResource(R.string.home_plan_label_priority),
                        value = card.savingsLabel,
                        icon = Icons.Default.Savings
                    ),
                    AllocationGridItem(
                        label = stringResource(R.string.home_plan_label_investing),
                        value = card.investingLabel,
                        icon = Icons.Default.AutoGraph
                    )
                )
            )
        }
        if (!isPremium) {
            item {
                LockedSmartAdjustmentPreviewCard(
                    priorityMove = card.savingsLabel,
                    weeklyGuide = card.weeklySpendLabel,
                    onClick = { onShowPaywall(HomePremiumGate.SmartAutomation) }
                )
            }
        } else {
            item {
                PremiumPlanBriefCard(
                    smartAdjustment = smartAdjustment,
                    premiumCheckIn = premiumCheckIn,
                    onOpenPremiumControls = onOpenPremiumControls
                )
            }
        }
    }
}

@Composable
private fun PremiumPlanBriefCard(
    smartAdjustment: SmartAdjustmentControlState,
    premiumCheckIn: PremiumCheckInState?,
    onOpenPremiumControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWatching = premiumCheckIn?.automationEnabled != false
    val body = when {
        smartAdjustment.isApplyable -> stringResource(
            R.string.home_plan_premium_brief_body_pending,
            smartAdjustment.recommendedFlexibleSpendLabel,
            smartAdjustment.recommendedPriorityContributionLabel
        )
        smartAdjustment.hasRecommendation -> stringResource(
            R.string.home_plan_premium_brief_body_steady,
            smartAdjustment.currentFlexibleSpendLabel,
            smartAdjustment.currentPriorityContributionLabel
        )
        !isWatching -> stringResource(R.string.home_plan_premium_brief_body_paused)
        else -> stringResource(R.string.home_plan_premium_brief_body_waiting)
    }
    val title = if (isWatching) {
        stringResource(R.string.home_plan_premium_brief_title_active)
    } else {
        stringResource(R.string.home_plan_premium_brief_title_paused)
    }
    val checkInLabel = premiumCheckIn?.relativeLabel()
        ?: stringResource(R.string.home_plan_premium_brief_checkin_waiting)
    val watchLabel = if (!isWatching) {
        stringResource(R.string.home_plan_premium_brief_watch_paused)
    } else {
        stringResource(R.string.home_plan_premium_brief_watch_active)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isWatching) { onOpenPremiumControls() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
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
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.13f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
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
                        text = stringResource(R.string.home_plan_premium_brief_label).uppercase(),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            if (!isWatching) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_plan_premium_brief_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumPlanBriefSignal(
                            label = stringResource(R.string.home_plan_premium_brief_checkin_label),
                            value = checkInLabel,
                            icon = if (premiumCheckIn?.isDue == true) Icons.Default.PlayCircle else Icons.Default.EventAvailable,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PremiumPlanBriefSignal(
                            label = stringResource(R.string.home_plan_premium_brief_watch_label),
                            value = watchLabel,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumPlanBriefSignal(
                            label = stringResource(R.string.home_plan_premium_brief_checkin_label),
                            value = checkInLabel,
                            icon = if (premiumCheckIn?.isDue == true) Icons.Default.PlayCircle else Icons.Default.EventAvailable,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumPlanBriefSignal(
                            label = stringResource(R.string.home_plan_premium_brief_watch_label),
                            value = watchLabel,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPlanBriefSignal(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PremiumCheckInState.relativeLabel(): String {
    val context = LocalContext.current
    return remember(relativeLabelKey, relativeLabelArgs) {
        val resId = context.resources.getIdentifier(relativeLabelKey, "string", context.packageName)
        if (resId != 0) {
            context.getString(resId, *relativeLabelArgs.toTypedArray())
        } else {
            relativeLabelKey
        }
    }
}

@Composable
private fun LockedSmartAdjustmentPreviewCard(
    priorityMove: String,
    weeklyGuide: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        text = stringResource(R.string.home_plan_smart_preview_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_plan_smart_preview_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(
                            R.string.home_plan_smart_preview_body,
                            priorityMove,
                            weeklyGuide
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartAdjustmentPill(
                            label = stringResource(R.string.home_plan_smart_preview_free_label),
                            body = stringResource(R.string.home_plan_smart_preview_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SmartAdjustmentPill(
                            label = stringResource(R.string.home_plan_smart_preview_premium_label),
                            body = stringResource(R.string.home_plan_smart_preview_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartAdjustmentPill(
                            label = stringResource(R.string.home_plan_smart_preview_free_label),
                            body = stringResource(R.string.home_plan_smart_preview_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        SmartAdjustmentPill(
                            label = stringResource(R.string.home_plan_smart_preview_premium_label),
                            body = stringResource(R.string.home_plan_smart_preview_premium_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.home_plan_smart_preview_action),
                style = MaterialTheme.typography.labelLarge,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SmartAdjustmentPill(
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CompactPaydaySummary(
    headline: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MySharePrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactPlanMetricsPanel(
    incomeLabel: String,
    incomeValue: String,
    weeklyLabel: String,
    weeklyValue: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactPlanMetricRow(
                label = incomeLabel,
                value = incomeValue,
                icon = Icons.Default.Payments
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Spacer(modifier = Modifier.height(1.dp))
            }
            CompactPlanMetricRow(
                label = weeklyLabel,
                value = weeklyValue,
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = MySharePositive
            )
        }
    }
}

@Composable
private fun CompactPlanMetricRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MySharePrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.padding(8.dp).size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CompactPlanMetric(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MySharePrimary
) {
    Surface(
        modifier = modifier.heightIn(min = 124.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private data class AllocationGridItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val accentColor: Color = MySharePrimary
)

@Composable
private fun CompactAllocationGrid(
    items: List<AllocationGridItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.3f
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.forEach { item ->
                            AllocationGridCell(
                                item = item,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { item ->
                                    AllocationGridCell(
                                        item = item,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationGridCell(
    item: AllocationGridItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.accentColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
