package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary

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
            PremiumPlanSummary(
                headline = card.nextPaydayLabel,
                body = card.summary
            )
        }
        item {
            PremiumSectionHeader(title = "Core Metrics")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumMetricCard(
                    label = "Income per payday",
                    value = card.incomeLabel,
                    icon = Icons.Default.Payments
                )
                PremiumMetricCard(
                    label = "Weekly Guide",
                    value = card.weeklySpendLabel,
                    subtitle = "Safe to spend every week",
                    icon = Icons.Default.AccountBalanceWallet
                )
            }
        }
        item {
            PremiumSectionHeader(title = "Allocation Preview")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumMetricCard(
                    label = "Fixed",
                    value = card.fixedCostsLabel,
                    modifier = Modifier.weight(1f),
                    color = MyShareSecondary
                )
                PremiumMetricCard(
                    label = "Flexible",
                    value = card.flexibleSpendLabel,
                    modifier = Modifier.weight(1f),
                    color = MyShareSecondary
                )
            }
        }
        if (!isPremium) {
            item {
                PremiumBenefitCard(
                    title = "Smart Adjustments",
                    description = "Enable Automation to automatically adjust your weekly guide based on last month's review.",
                    icon = Icons.Default.PrecisionManufacturing,
                    onClick = { onDestinationSelected(HomeDestination.MORE) }
                )
            }
        }
    }
}
