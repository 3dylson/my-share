package pt.ms.myshare.presentation.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*

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
        PremiumProfileHeader(
            email = state.userEmail ?: "Guest User",
            isPremium = state.isPremium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (!state.isPremium) {
        item {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                PremiumSectionHeader(title = "Go Unlimited")
                PremiumPaywallCard(
                    title = "Annual Membership",
                    price = state.actualAnnualPrice ?: "$49.99",
                    period = "year",
                    description = "Unlock automation, multiple goals, and detailed sync.",
                    badge = "60% OFF",
                    isSelected = state.selectedBillingPlan == BillingPlan.ANNUAL,
                    onClick = { onBillingPlanSelected(BillingPlan.ANNUAL) }
                )
                Spacer(Modifier.height(12.dp))
                PremiumPaywallCard(
                    title = "Monthly Membership",
                    price = state.actualMonthlyPrice ?: "$5.99",
                    period = "month",
                    description = "Flexible access to all premium features.",
                    isSelected = state.selectedBillingPlan == BillingPlan.MONTHLY,
                    onClick = { onBillingPlanSelected(BillingPlan.MONTHLY) }
                )
                Spacer(Modifier.height(16.dp))
                PremiumButton(
                    text = "Unlock Everything",
                    onClick = { activity?.let(onUnlockPremium) }
                )
            }
        }
    }

    item {
        PremiumSettingsGroup(title = "App Preferences") {
            PremiumSettingsRow(
                title = "Smart Notifications",
                subtitle = state.reminderLabel,
                icon = Icons.Default.Notifications,
                iconColor = MySharePrimary,
                trailingContent = {
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { onToggleReminder(!state.reminderEnabled) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MySharePrimary,
                            uncheckedThumbColor = MyShareOutline,
                            uncheckedTrackColor = MyShareOutline.copy(alpha = 0.1f)
                        )
                    )
                },
                onClick = { onToggleReminder(!state.reminderEnabled) }
            )
            PremiumSettingsRow(
                title = "Smart Automation",
                subtitle = if (state.automationEnabled) "Adaptive buffers active" else "System idle",
                icon = Icons.Default.PrecisionManufacturing,
                iconColor = MyShareSecondary,
                showDivider = false,
                trailingContent = {
                    if (!state.isPremium) {
                        Surface(
                            color = MySharePrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MySharePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Switch(
                            checked = state.automationEnabled,
                            onCheckedChange = { onToggleAutomation(!state.automationEnabled) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MySharePrimary
                            )
                        )
                    }
                },
                onClick = {
                    if (state.isPremium) {
                        onToggleAutomation(!state.automationEnabled)
                    } else {
                        // Analytics or show upgrade
                    }
                }
            )
        }
    }

    item {
        val uriHandler = LocalUriHandler.current
        PremiumSettingsGroup(title = "Legal & Support") {
            PremiumSettingsRow(
                title = "Terms of Service",
                subtitle = "User agreement",
                icon = Icons.Default.Description,
                iconColor = Color(0xFF5C6BC0),
                onClick = { uriHandler.openUri("https://myshare.pt/terms") }
            )
            PremiumSettingsRow(
                title = "Privacy Policy",
                subtitle = "Data handling",
                icon = Icons.Default.PrivacyTip,
                iconColor = Color(0xFF66BB6A),
                onClick = { uriHandler.openUri("https://my-share-finance.web.app/") }
            )
            if (state.showAdsConsentOption) {
                PremiumSettingsRow(
                    title = "Ad Preferences",
                    subtitle = "Manage consent",
                    icon = Icons.Default.AdsClick,
                    iconColor = Color(0xFFFFA726),
                    showDivider = false,
                    onClick = onManageAdsConsent
                )
            }
        }
    }

    item {
        PremiumSettingsGroup(title = "Account Actions") {
            PremiumSettingsRow(
                title = "Sign Out",
                subtitle = "Log out of your account safely",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconColor = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                showDivider = false,
                onClick = onLogout
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Share",
                style = MaterialTheme.typography.titleSmall,
                color = MyShareOnSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Version 1.2.0 (Gold)",
                style = MaterialTheme.typography.labelSmall,
                color = MyShareOnSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
