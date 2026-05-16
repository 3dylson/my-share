package pt.ms.myshare.presentation.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.preferences.currencyLabel
import pt.ms.myshare.presentation.ui.preferences.languageLabel
import pt.ms.myshare.presentation.ui.theme.*

/**
 * Responsibility: Renders global settings, paywall, and account management.
 * This file is part of the Home screen refactoring to follow SRP.
 */
fun LazyListScope.homeMoreTab(
    state: MoreCardState,
    activity: android.app.Activity?,
    onToggleReminder: (Boolean) -> Unit,
    onConfigureReminder: () -> Unit,
    onToggleAutomation: (Boolean) -> Unit,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: (android.app.Activity, String) -> Unit,
    onManageAdsConsent: () -> Unit,
    onShowLanguagePicker: () -> Unit,
    onShowCurrencyPicker: () -> Unit,
    onShowAutomationLock: () -> Unit,
    onShowAccountDetails: () -> Unit,
    isGoogleCredentialRequestInProgress: Boolean,
    onConnectGoogle: () -> Unit,
    onLogout: () -> Unit
) {
    item {
        val accountLabel = state.userEmail
            ?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.home_more_guest)
        PremiumProfileHeader(
            email = accountLabel,
            isPremium = state.isPremium,
            modifier = Modifier.padding(bottom = 24.dp),
            onAccountClick = onShowAccountDetails
        )
    }

    if (!state.isPremium) {
        item {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                PremiumSectionHeader(title = stringResource(R.string.home_more_premium_title))
                val planOrder = if (state.pricingStrategy?.heroPlan == BillingPlan.ANNUAL) {
                    listOf(BillingPlan.ANNUAL, BillingPlan.MONTHLY)
                } else {
                    listOf(BillingPlan.MONTHLY, BillingPlan.ANNUAL)
                }
                planOrder.forEachIndexed { index, plan ->
                    CompactBillingPlanRow(
                        title = stringResource(plan.moreTitleRes),
                        price = when (plan) {
                            BillingPlan.MONTHLY -> state.actualMonthlyPrice
                            BillingPlan.ANNUAL -> state.actualAnnualPrice
                        } ?: stringResource(R.string.paywall_price_loading),
                        period = stringResource(plan.morePeriodRes),
                        badge = when {
                            plan == BillingPlan.ANNUAL -> stringResource(R.string.home_more_annual_badge)
                            plan == state.pricingStrategy?.heroPlan -> stringResource(R.string.paywall_badge_easy_start)
                            else -> null
                        },
                        comparisonPrice = if (plan == BillingPlan.ANNUAL) {
                            state.annualMonthlyEquivalentPrice
                        } else {
                            null
                        },
                        savingsLabel = if (plan == BillingPlan.ANNUAL) {
                            state.annualSavingsPrice?.let { stringResource(R.string.paywall_annual_savings_label, it) }
                        } else {
                            null
                        },
                        isSelected = state.selectedBillingPlan == plan,
                        onClick = { onBillingPlanSelected(plan) }
                    )
                    if (index != planOrder.lastIndex) {
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                val selectedPrice = if (state.selectedBillingPlan == BillingPlan.ANNUAL) {
                    state.actualAnnualPrice
                } else {
                    state.actualMonthlyPrice
                }
                val selectedPeriod = if (state.selectedBillingPlan == BillingPlan.ANNUAL) {
                    stringResource(R.string.paywall_period_year)
                } else {
                    stringResource(R.string.paywall_period_month)
                }
                val selectedTrialDays = if (state.selectedBillingPlan == BillingPlan.ANNUAL) {
                    state.actualAnnualTrialDays
                } else {
                    state.actualMonthlyTrialDays
                }
                val checkoutTerms = when {
                    selectedPrice == null -> stringResource(R.string.paywall_footer_store_terms_unavailable)
                    selectedTrialDays != null -> stringResource(
                        R.string.paywall_footer_trial_terms,
                        selectedTrialDays,
                        selectedPrice,
                        selectedPeriod
                    )
                    else -> stringResource(R.string.paywall_footer_no_trial_terms, selectedPrice, selectedPeriod)
                }
                Text(
                    text = checkoutTerms,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(10.dp))
                val context = LocalContext.current
                if (state.error != null && state.billingMessage == null) {
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
                if (state.billingMessage != null) {
                    val billingMessage = remember(state.billingMessage) {
                        val resId = context.resources.getIdentifier(state.billingMessage, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else state.billingMessage
                    }
                    PremiumInfoCard(
                        title = billingMessage,
                        body = stringResource(R.string.paywall_billing_status_body),
                        icon = Icons.Default.Info,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }
                PremiumButton(
                    text = if (state.isBillingActionInProgress) {
                        stringResource(R.string.paywall_upgrade_loading)
                    } else if (selectedPrice == null) {
                        stringResource(R.string.paywall_price_loading)
                    } else if (selectedTrialDays != null) {
                        stringResource(R.string.paywall_start_trial_button)
                    } else {
                        stringResource(R.string.home_more_premium_button)
                    },
                    onClick = {
                        if (!state.isBillingActionInProgress) {
                            activity?.let { onUnlockPremium(it, "more_inline") }
                        }
                    },
                    enabled = selectedPrice != null && !state.isBillingActionInProgress,
                    isLoading = state.isBillingActionInProgress
                )
            }
        }
    }

    item {
        PremiumSettingsGroup(title = stringResource(R.string.home_more_pref_title)) {
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_pref_notifications),
                subtitle = if (state.reminderLabelKey != null) {
                    val context = LocalContext.current
                    val resId = context.resources.getIdentifier(state.reminderLabelKey, "string", context.packageName)
                    if (resId != 0) {
                        context.getString(resId, *state.reminderLabelArgs.toTypedArray())
                    } else {
                        state.reminderLabel
                    }
                } else state.reminderLabel,
                icon = Icons.Default.Notifications,
                iconColor = MySharePrimary,
                trailingContent = {
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onConfigureReminder()
                            } else {
                                onToggleReminder(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MySharePrimary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    )
                },
                onClick = onConfigureReminder
            )
            PremiumSettingsRow(
                title = stringResource(R.string.preferences_language_title),
                subtitle = languageLabel(state.userPreferences.languageTag),
                icon = Icons.Default.Language,
                iconColor = MySharePrimary,
                onClick = onShowLanguagePicker
            )
            PremiumSettingsRow(
                title = stringResource(R.string.preferences_currency_title),
                subtitle = currencyLabel(state.userPreferences.currencyCode, state.userPreferences.locale),
                icon = Icons.Default.Payments,
                iconColor = MySharePrimary,
                onClick = onShowCurrencyPicker
            )
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_pref_automation),
                subtitle = if (state.automationEnabled) 
                    stringResource(R.string.home_more_pref_automation_active) 
                else if (state.isPremium)
                    stringResource(R.string.home_more_pref_automation_idle)
                else
                    stringResource(R.string.home_more_pref_automation_locked),
                icon = Icons.Default.PrecisionManufacturing,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        onShowAutomationLock()
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
                onClick = { uriHandler.openUri("https://my-share-finance.web.app/terms") }
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
            val uriHandler = LocalUriHandler.current
            GoogleConnectionFeedback(state)
            if (state.canConnectGoogle) {
                PremiumSettingsRow(
                    title = stringResource(R.string.home_more_account_connect_google_title),
                    subtitle = stringResource(R.string.home_more_account_connect_google_desc),
                    icon = Icons.Default.Cloud,
                    iconColor = MySharePrimary,
                    trailingContent = {
                        if (state.isGoogleConnectionInProgress || isGoogleCredentialRequestInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MySharePrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = MySharePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    onClick = {
                        if (!state.isGoogleConnectionInProgress && !isGoogleCredentialRequestInProgress) {
                            onConnectGoogle()
                        }
                    }
                )
            }
            PremiumSettingsRow(
                title = stringResource(R.string.home_more_account_manage_subscription),
                subtitle = stringResource(R.string.home_more_account_manage_subscription_desc),
                icon = Icons.Default.Payment,
                iconColor = MySharePrimary,
                onClick = {
                    uriHandler.openUri("https://play.google.com/store/account/subscriptions?package=pt.ms.myshare")
                }
            )
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

        if (!state.isPremium) {
            Spacer(modifier = Modifier.height(24.dp))
            PremiumAdBanner()
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.home_more_version_label, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GoogleConnectionFeedback(state: MoreCardState) {
    val context = LocalContext.current
    val messageKey = state.googleConnectionError ?: state.googleConnectionMessage ?: return
    val message = remember(messageKey) {
        val resId = context.resources.getIdentifier(messageKey, "string", context.packageName)
        if (resId != 0) context.getString(resId) else messageKey
    }
    val isError = state.googleConnectionError != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
            } else {
                MySharePrimary.copy(alpha = 0.35f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MySharePrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CompactBillingPlanRow(
    title: String,
    price: String,
    period: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    comparisonPrice: String? = null,
    savingsLabel: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MySharePrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.price_per_period, price, period),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (comparisonPrice != null) {
                        Text(
                            text = stringResource(R.string.price_per_period, comparisonPrice, period),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }
                if (savingsLabel != null) {
                    Text(
                        text = savingsLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private val BillingPlan.moreTitleRes: Int
    get() = when (this) {
        BillingPlan.MONTHLY -> R.string.home_more_monthly_title
        BillingPlan.ANNUAL -> R.string.home_more_annual_title
    }

private val BillingPlan.morePeriodRes: Int
    get() = when (this) {
        BillingPlan.MONTHLY -> R.string.period_month
        BillingPlan.ANNUAL -> R.string.period_year
    }
