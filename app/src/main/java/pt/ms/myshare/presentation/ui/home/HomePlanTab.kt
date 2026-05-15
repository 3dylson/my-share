package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareOnSurface
import pt.ms.myshare.presentation.ui.theme.MyShareOutline
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MySharePrimaryContainer
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary
import pt.ms.myshare.presentation.ui.theme.MySharePositive

/**
 * Responsibility: Renders the core financial plan dashboard (Income, Metrics, Allocation Preview).
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homePlanTab(
    planCard: HomePlanCardState?,
    isPremium: Boolean,
    onDestinationSelected: (HomeDestination) -> Unit
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
                        label = stringResource(R.string.savings_label),
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
                PremiumBenefitCard(
                    title = stringResource(R.string.home_plan_benefit_title),
                    description = stringResource(R.string.home_plan_benefit_desc),
                    icon = Icons.Default.PrecisionManufacturing,
                    onClick = { onDestinationSelected(HomeDestination.MORE) }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.16f)),
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
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MyShareOnSurface,
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.14f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

@Composable
private fun AllocationGridCell(
    item: AllocationGridItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 82.dp),
        shape = RoundedCornerShape(14.dp),
        color = MySharePrimaryContainer.copy(alpha = 0.26f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.accentColor,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = item.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MyShareOnSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
