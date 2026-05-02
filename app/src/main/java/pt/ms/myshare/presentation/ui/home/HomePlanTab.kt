package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary
import pt.ms.myshare.presentation.ui.theme.MySharePositive
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Lock

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
            val headline = if (card.nextPaydayKey != null) {
                stringResource(
                    LocalContext.current.resources.getIdentifier(card.nextPaydayKey, "string", LocalContext.current.packageName),
                    *card.nextPaydayArgs.toTypedArray()
                )
            } else card.nextPaydayLabel

            PremiumPlanSummary(
                headline = headline,
                body = card.summary
            )
        }
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_plan_metrics_title))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroMetricCard(
                    label = stringResource(R.string.home_plan_label_income),
                    value = card.incomeLabel,
                    icon = Icons.Default.Payments
                )
                HeroMetricCard(
                    label = stringResource(R.string.home_plan_label_weekly),
                    value = card.weeklySpendLabel,
                    subtitle = stringResource(R.string.home_plan_desc_weekly),
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = MySharePositive
                )
            }
        }
        item {
            PremiumSectionHeader(title = stringResource(R.string.home_plan_allocation_title))
            AllocationPreviewMetric(
                fixedLabel = stringResource(R.string.home_plan_label_fixed),
                fixedValue = card.fixedCostsLabel,
                flexibleLabel = stringResource(R.string.home_plan_label_flexible),
                flexibleValue = card.flexibleSpendLabel
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
