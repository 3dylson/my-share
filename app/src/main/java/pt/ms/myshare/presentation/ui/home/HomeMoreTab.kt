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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import pt.ms.myshare.R
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
            email = state.userEmail ?: stringResource(R.string.home_more_guest),
            isPremium = state.isPremium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }

    if (!state.isPremium) {
        item {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                PremiumSectionHeader(title = stringResource(R.string.home_more_premium_title))
                PremiumPaywallCard(
                    title = stringResource(R.string.home_more_annual_title),
                    price = state.actualAnnualPrice ?: "$49.99",
                    period = stringResource(R.string.period_year),
                    description = stringResource(R.string.home_more_annual_desc),
                    badge = stringResource(R.string.home_more_annual_badge),
                    isSelected = state.selectedBillingPlan == BillingPlan.ANNUAL,
                    onClick = { onBillingPlanSelected(BillingPlan.ANNUAL) }
                )
                Spacer(Modifier.height(12.dp))
                PremiumPaywallCard(
                    title = stringResource(R.string.home_more_monthly_title),
                    price = state.actualMonthlyPrice ?: "$5.99",
                    period = stringResource(R.string.period_month),
                    description = stringResource(R.string.home_more_monthly_desc),
                    isSelected = state.selectedBillingPlan == BillingPlan.MONTHLY,
                    onClick = { onBillingPlanSelected(BillingPlan.MONTHLY) }
                )
                Spacer(Modifier.height(16.dp))
                val context = LocalContext.current
                if (state.error != null) {
                    val errorMessage = remember(state.error) {
                        val resId = context.resources.getIdentifier(state.error, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else state.error
                    }
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                PremiumButton(
                    text = stringResource(R.string.home_more_premium_button),
                    onClick = { activity?.let(onUnlockPremium) }
                )
            }
        }
    }

    item {
        PremiumSettingsGroup(title = stringResource(R.string.home_more_pref_title)) {
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_pref_notifications),
                subtitle = if (state.reminderLabelKey != null) {
                    stringResource(
                        LocalContext.current.resources.getIdentifier(state.reminderLabelKey, "string", LocalContext.current.packageName)
                    )
                } else state.reminderLabel,
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
                title = stringResource(R.string.home_more_pref_automation),
                subtitle = if (state.automationEnabled) 
                    stringResource(R.string.home_more_pref_automation_active) 
                else 
                    stringResource(R.string.home_more_pref_automation_idle),
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
                                text = stringResource(R.string.premium_badge),
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
        PremiumSettingsGroup(title = stringResource(R.string.home_more_legal_title)) {
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_legal_terms),
                subtitle = stringResource(R.string.home_more_legal_terms_desc),
                icon = Icons.Default.Description,
                iconColor = Color(0xFF5C6BC0),
                onClick = { uriHandler.openUri("https://myshare.pt/terms") }
            )
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_legal_privacy),
                subtitle = stringResource(R.string.home_more_legal_privacy_desc),
                icon = Icons.Default.PrivacyTip,
                iconColor = Color(0xFF66BB6A),
                onClick = { uriHandler.openUri("https://my-share-finance.web.app/") }
            )
            if (state.showAdsConsentOption) {
                PremiumSettingsRow(
                    title = stringResource(R.string.home_more_legal_ads),
                    subtitle = stringResource(R.string.home_more_legal_ads_desc),
                    icon = Icons.Default.AdsClick,
                    iconColor = Color(0xFFFFA726),
                    showDivider = false,
                    onClick = onManageAdsConsent
                )
            }
        }
    }

    item {
        PremiumSettingsGroup(title = stringResource(R.string.home_more_account_title)) {
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_account_signout),
                subtitle = stringResource(R.string.home_more_account_signout_desc),
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
                text = stringResource(R.string.home_more_version_label, "1.2.0"),
                style = MaterialTheme.typography.labelSmall,
                color = MyShareOnSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
