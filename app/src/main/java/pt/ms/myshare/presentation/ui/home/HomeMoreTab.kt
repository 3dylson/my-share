package pt.ms.myshare.presentation.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.components.*

/**
 * Responsibility: Renders global settings, paywall, and account management.
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homeMoreTab(
    state: MoreCardState,
    activity: android.app.Activity?,
    onToggleReminder: (Boolean) -> Unit,
    onToggleAutomation: (Boolean) -> Unit,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: (android.app.Activity) -> Unit,
    onManageAdsConsent: () -> Unit,
    onLogout: () -> Unit
) {
    item {
        PremiumSectionHeader(title = "Global Settings")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PremiumChoiceCard(
                title = "Smart Notifications",
                description = state.reminderLabel,
                isSelected = state.reminderEnabled,
                onClick = { onToggleReminder(!state.reminderEnabled) },
                icon = Icons.Default.Notifications
            )

            PremiumChoiceCard(
                title = "Smart Automation",
                description = if (state.automationEnabled) "System automatically adjusting buffers" else "Disabled",
                isSelected = state.automationEnabled,
                onClick = {
                    if (state.isPremium) {
                        onToggleAutomation(!state.automationEnabled)
                    } else {
                        // User needs to upgrade
                    }
                },
                icon = Icons.Default.PrecisionManufacturing,
                badge = if (!state.isPremium) "PREMIUM" else null
            )
        }
    }

    if (!state.isPremium) {
        item {
            PremiumPaywallCard(
                title = "Annual Membership",
                price = state.actualAnnualPrice ?: "$49.99",
                period = "year",
                description = "Unlock automation, multiple goals, and detailed sync.",
                badge = "60% OFF",
                isSelected = state.selectedBillingPlan == BillingPlan.ANNUAL,
                onClick = { onBillingPlanSelected(BillingPlan.ANNUAL) }
            )
        }
        item {
            PremiumPaywallCard(
                title = "Monthly Membership",
                price = state.actualMonthlyPrice ?: "$5.99",
                period = "month",
                description = "Flexible access to all premium features.",
                isSelected = state.selectedBillingPlan == BillingPlan.MONTHLY,
                onClick = { onBillingPlanSelected(BillingPlan.MONTHLY) }
            )
        }
        item {
            PremiumButton(
                text = "Go Unlimited",
                onClick = { activity?.let(onUnlockPremium) },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        item {
            PremiumMetricCard(
                label = "Account Level",
                value = "Unlimited Access",
                subtitle = "Lifetime system mastery active",
                icon = Icons.Default.VerifiedUser
            )
        }
    }

    item {
        PremiumSectionHeader(title = "Account")
        Column {
            state.userEmail?.let { email ->
                PremiumMetricCard(
                    label = "Signed in as",
                    value = email,
                    icon = Icons.Default.AccountCircle
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val uriHandler = LocalUriHandler.current
            PremiumSectionHeader(title = "Legal & Privacy")
            Column {
                PremiumMetricCard(
                    label = "Terms of Service",
                    value = "View",
                    subtitle = "User Agreement & Logic License",
                    icon = Icons.Default.Info,
                    onClick = { uriHandler.openUri("https://myshare.pt/terms") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                PremiumMetricCard(
                    label = "Privacy Policy",
                    value = "View",
                    subtitle = "Data Handling & Ad Consent",
                    icon = Icons.Default.Info,
                    onClick = { uriHandler.openUri("https://my-share-finance.web.app/") }
                )
                
                if (state.showAdsConsentOption) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PremiumMetricCard(
                        label = "Ad Preferences",
                        value = "Manage",
                        subtitle = "Update choice for personalized ads",
                        icon = Icons.Default.Settings,
                        onClick = onManageAdsConsent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            PremiumButton(
                text = "Logout",
                onClick = onLogout,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.error,
                icon = Icons.AutoMirrored.Filled.Logout
            )
        }
    }
}
