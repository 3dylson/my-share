package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MySharePrimaryContainer

/**
 * Responsibility: Renders the Payday Rules list and allocation logic explanation.
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homeRulesTab(
    rules: List<RuleCardState>,
    isPremium: Boolean,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit,
    onDestinationSelected: (HomeDestination) -> Unit
) {
    if (rules.isEmpty()) {
        item {
            PremiumSectionHeader(title = "No Custom Logic")
            PremiumInfoCard(
                title = "Your Rules, Your Money",
                body = "Add payday rules to automate how your surplus is distributed between savings, debt, and other categories.",
                icon = Icons.Default.Rule
            )
        }
    } else {
        item {
            PremiumSettingsGroup(title = "Primary Rules") {
                rules.forEachIndexed { index, ruleCard ->
                    PremiumSettingsRow(
                        title = ruleCard.name,
                        subtitle = "${ruleCard.amountLabel} • ${ruleCard.typeLabel}",
                        icon = if (ruleCard.isPercentage) Icons.Default.Percent else Icons.Default.Savings,
                        iconColor = if (ruleCard.isPercentage) pt.ms.myshare.presentation.ui.theme.MySharePrimary else pt.ms.myshare.presentation.ui.theme.MyShareSecondary,
                        onClick = { onEditRule(ruleCard.id) },
                        showDivider = index < rules.size - 1
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (!isPremium && rules.size >= 3) {
        item {
            PremiumBenefitCard(
                title = "Deep Automation",
                description = "Free tier supports up to 3 active rules. Upgrade for unlimited complex logic and nested allocations.",
                icon = Icons.Default.Lock,
                onClick = { onDestinationSelected(HomeDestination.MORE) }
            )
        }
    } else {
        item {
            PremiumButton(
                text = "Add New Rule",
                onClick = onAddNewRule,
                icon = Icons.Default.Add,
                containerColor = MySharePrimaryContainer,
                contentColor = MySharePrimary
            )
        }
    }

    item {
        PremiumInfoCard(
            title = "Logic Flow",
            body = "Rules are applied in order. Percentage rules take from the *remaining* balance after fixed costs and previous fixed rules.",
            icon = Icons.Default.Lightbulb,
            backgroundColor = MySharePrimaryContainer.copy(alpha = 0.6f)
        )
    }
}
