package pt.ms.myshare.presentation.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Rule
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PaydayAdjustmentRecommendationDirection
import pt.ms.myshare.domain.model.PremiumAdjustmentStatus
import pt.ms.myshare.domain.model.PremiumCheckInStatus
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
    onOpenReview: () -> Unit,
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

    item {
        MoreRoutineSummaryCard(
            state = state,
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }

    if (state.isPremium) {
        item {
            SmartAdjustmentControlCard(
                state = state,
                onToggleAutomation = onToggleAutomation,
                onOpenReview = onOpenReview,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
        state.adjustmentMemory?.let { memory ->
            item {
                PremiumAdjustmentMemoryCard(
                    memory = memory,
                    modifier = Modifier.padding(bottom = 20.dp)
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

    if (!state.isPremium) {
        item {
            MorePremiumUpgradeSection(
                state = state,
                activity = activity,
                onBillingPlanSelected = onBillingPlanSelected,
                onUnlockPremium = onUnlockPremium,
                modifier = Modifier.padding(bottom = 20.dp)
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
            AccountFeedback(state)
            if (state.canConnectGoogle) {
                PremiumSettingsRow(
                    title = stringResource(R.string.home_more_account_connect_google_title),
                    subtitle = stringResource(R.string.home_more_account_connect_google_desc),
                    icon = Icons.Default.AccountCircle,
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
                    leadingIconContent = {
                        GoogleLogo(modifier = Modifier.size(22.dp))
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
                trailingContent = {
                    if (state.isLogoutInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                showDivider = false,
                onClick = {
                    if (!state.isLogoutInProgress) {
                        onLogout()
                    }
                }
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
private fun MoreRoutineSummaryCard(
    state: MoreCardState,
    modifier: Modifier = Modifier
) {
    val weeklyGuide = state.weeklyGuideLabel.ifBlank { stringResource(R.string.home_more_routine_not_set) }
    val priorityMove = state.priorityMoveLabel.ifBlank { stringResource(R.string.home_more_routine_not_set) }
    val body = if (state.isPremium) {
        stringResource(R.string.home_more_routine_body_premium)
    } else {
        stringResource(R.string.home_more_routine_body_free)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePrimary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_more_routine_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 300.dp
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoreRoutineMetric(
                            label = stringResource(R.string.home_more_routine_weekly_label),
                            value = weeklyGuide,
                            icon = Icons.Default.Payments,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MoreRoutineMetric(
                            label = stringResource(R.string.home_more_routine_priority_label),
                            value = priorityMove,
                            icon = Icons.Default.Flag,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MoreRoutineMetric(
                            label = stringResource(R.string.home_more_routine_rules_label),
                            value = state.ruleCount.toString(),
                            icon = Icons.AutoMirrored.Filled.Rule,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MoreRoutineMetric(
                            label = stringResource(R.string.home_more_routine_reviews_label),
                            value = state.reviewCount.toString(),
                            icon = Icons.Default.History,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MoreRoutineMetric(
                                label = stringResource(R.string.home_more_routine_weekly_label),
                                value = weeklyGuide,
                                icon = Icons.Default.Payments,
                                modifier = Modifier.weight(1f)
                            )
                            MoreRoutineMetric(
                                label = stringResource(R.string.home_more_routine_priority_label),
                                value = priorityMove,
                                icon = Icons.Default.Flag,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MoreRoutineMetric(
                                label = stringResource(R.string.home_more_routine_rules_label),
                                value = state.ruleCount.toString(),
                                icon = Icons.AutoMirrored.Filled.Rule,
                                modifier = Modifier.weight(1f)
                            )
                            MoreRoutineMetric(
                                label = stringResource(R.string.home_more_routine_reviews_label),
                                value = state.reviewCount.toString(),
                                icon = Icons.Default.History,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreRoutineMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 70.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SmartAdjustmentControlCard(
    state: MoreCardState,
    onToggleAutomation: (Boolean) -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smartAdjustment = state.smartAdjustment
    val statusText = when {
        state.automationEnabled -> stringResource(R.string.home_more_smart_adjustments_status_watching)
        else -> stringResource(R.string.home_more_smart_adjustments_status_paused)
    }
    val body = when {
        !smartAdjustment.hasPlan -> stringResource(R.string.home_more_smart_adjustments_body_no_plan)
        smartAdjustment.isApplyable -> stringResource(
            R.string.home_more_smart_adjustments_body_pending,
            smartAdjustment.recommendedFlexibleSpendLabel,
            smartAdjustment.recommendedPriorityContributionLabel
        )
        smartAdjustment.hasRecommendation -> stringResource(
            R.string.home_more_smart_adjustments_body_keep,
            smartAdjustment.currentFlexibleSpendLabel,
            smartAdjustment.currentPriorityContributionLabel
        )
        else -> stringResource(R.string.home_more_smart_adjustments_body_waiting)
    }
    val actionLabel = if (state.automationEnabled) {
        stringResource(R.string.home_more_smart_adjustments_pause)
    } else {
        stringResource(R.string.home_more_smart_adjustments_enable)
    }
    val reviewActionEnabled = smartAdjustment.hasRecommendation || state.premiumCheckIn?.isDue == true
    val reviewActionLabel = if (state.premiumCheckIn?.isDue == true) {
        stringResource(R.string.home_more_smart_checkin_start)
    } else {
        stringResource(R.string.home_more_smart_adjustments_review)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_more_smart_adjustments_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        SmartAdjustmentStatusPill(
                            text = statusText,
                            isActive = state.automationEnabled
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_more_smart_adjustments_title),
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

            SmartAdjustmentSignalGrid(smartAdjustment = smartAdjustment)

            state.premiumCheckIn?.let { checkIn ->
                PremiumCheckInCompactSummary(checkIn = checkIn)
            }

            if (smartAdjustment.lastActionMessageKey != null) {
                SmartAdjustmentLastAction(messageKey = smartAdjustment.lastActionMessageKey)
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 340.dp
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmartAdjustmentReviewButton(
                            label = reviewActionLabel,
                            enabled = reviewActionEnabled,
                            onClick = onOpenReview,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { onToggleAutomation(!state.automationEnabled) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(text = actionLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onToggleAutomation(!state.automationEnabled) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(text = actionLabel, fontWeight = FontWeight.Bold)
                        }
                        SmartAdjustmentReviewButton(
                            label = reviewActionLabel,
                            enabled = reviewActionEnabled,
                            onClick = onOpenReview,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumAdjustmentMemoryCard(
    memory: PremiumAdjustmentMemoryState,
    modifier: Modifier = Modifier
) {
    val title = when (memory.status) {
        PremiumAdjustmentStatus.APPLIED -> stringResource(R.string.home_more_adjustment_memory_title)
        PremiumAdjustmentStatus.UNDONE -> stringResource(R.string.home_more_adjustment_memory_title_undone)
    }
    val body = when (memory.status) {
        PremiumAdjustmentStatus.UNDONE -> stringResource(
            R.string.home_more_adjustment_memory_body_undone,
            memory.adjustmentAmountLabel
        )
        PremiumAdjustmentStatus.APPLIED -> when (memory.direction) {
            PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY -> stringResource(
                R.string.home_more_adjustment_memory_body_move,
                memory.dateLabel,
                memory.adjustmentAmountLabel
            )
            PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER -> stringResource(
                R.string.home_more_adjustment_memory_body_restore,
                memory.dateLabel,
                memory.adjustmentAmountLabel
            )
            PaydayAdjustmentRecommendationDirection.KEEP_PLAN -> stringResource(
                R.string.home_more_adjustment_memory_body_keep,
                memory.dateLabel
            )
        }
    }
    val statusLabel = when (memory.status) {
        PremiumAdjustmentStatus.APPLIED -> stringResource(R.string.home_more_adjustment_memory_status_applied)
        PremiumAdjustmentStatus.UNDONE -> stringResource(R.string.home_more_adjustment_memory_status_undone)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MySharePositive.copy(alpha = 0.2f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MySharePositive.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MySharePositive,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_more_adjustment_memory_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePositive,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        SmartAdjustmentStatusPill(
                            text = statusLabel,
                            isActive = memory.status == PremiumAdjustmentStatus.APPLIED
                        )
                    }
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

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 300.dp
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumAdjustmentMemoryMetric(
                            label = stringResource(R.string.home_more_adjustment_memory_flexible),
                            beforeLabel = memory.previousFlexibleSpendLabel,
                            afterLabel = memory.recommendedFlexibleSpendLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PremiumAdjustmentMemoryMetric(
                            label = stringResource(R.string.home_more_adjustment_memory_priority),
                            beforeLabel = memory.previousPriorityContributionLabel,
                            afterLabel = memory.recommendedPriorityContributionLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumAdjustmentMemoryMetric(
                            label = stringResource(R.string.home_more_adjustment_memory_flexible),
                            beforeLabel = memory.previousFlexibleSpendLabel,
                            afterLabel = memory.recommendedFlexibleSpendLabel,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumAdjustmentMemoryMetric(
                            label = stringResource(R.string.home_more_adjustment_memory_priority),
                            beforeLabel = memory.previousPriorityContributionLabel,
                            afterLabel = memory.recommendedPriorityContributionLabel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.home_more_adjustment_memory_rules, memory.affectedRuleCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PremiumAdjustmentMemoryMetric(
    label: String,
    beforeLabel: String,
    afterLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 82.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.home_more_adjustment_memory_change, beforeLabel, afterLabel),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumCheckInCompactSummary(
    checkIn: PremiumCheckInState,
    modifier: Modifier = Modifier
) {
    val relativeLabel = checkIn.relativeLabel()
    val title = when (checkIn.status) {
        PremiumCheckInStatus.READY_NOW -> stringResource(R.string.home_more_smart_checkin_ready)
        PremiumCheckInStatus.OVERDUE -> stringResource(R.string.home_more_smart_checkin_overdue)
        PremiumCheckInStatus.SCHEDULED -> stringResource(R.string.home_more_smart_checkin_scheduled)
        PremiumCheckInStatus.REVIEWED -> stringResource(R.string.home_more_smart_checkin_reviewed)
    }
    val body = when (checkIn.status) {
        PremiumCheckInStatus.READY_NOW -> stringResource(R.string.home_more_smart_checkin_ready_body)
        PremiumCheckInStatus.OVERDUE -> stringResource(R.string.home_more_smart_checkin_overdue_body, checkIn.checkInDateLabel)
        PremiumCheckInStatus.SCHEDULED -> stringResource(R.string.home_more_smart_checkin_scheduled_body, checkIn.checkInDateLabel)
        PremiumCheckInStatus.REVIEWED -> stringResource(R.string.home_more_smart_checkin_reviewed_body, checkIn.checkInDateLabel)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (checkIn.isDue) Icons.Default.PlayCircle else Icons.Default.EventAvailable,
                contentDescription = null,
                tint = if (checkIn.isDue) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SmartAdjustmentStatusPill(
                        text = relativeLabel,
                        isActive = checkIn.isDue
                    )
                }
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SmartAdjustmentStatusPill(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (isActive) MySharePrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (isActive) MySharePrimary.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmartAdjustmentSignalGrid(
    smartAdjustment: SmartAdjustmentControlState,
    modifier: Modifier = Modifier
) {
    val flexibleValue = when {
        smartAdjustment.hasRecommendation -> smartAdjustment.recommendedFlexibleSpendLabel
        smartAdjustment.hasPlan -> smartAdjustment.currentFlexibleSpendLabel.ifBlank { stringResource(R.string.home_more_routine_not_set) }
        else -> stringResource(R.string.home_more_routine_not_set)
    }
    val priorityValue = when {
        smartAdjustment.hasRecommendation -> smartAdjustment.recommendedPriorityContributionLabel
        smartAdjustment.hasPlan -> smartAdjustment.currentPriorityContributionLabel.ifBlank { stringResource(R.string.home_more_routine_not_set) }
        else -> stringResource(R.string.home_more_routine_not_set)
    }
    val evidenceValue = if (smartAdjustment.hasRecommendation) {
        stringResource(
            R.string.home_more_smart_adjustments_evidence_value,
            smartAdjustment.confidencePercent,
            smartAdjustment.analyzedReviewCount
        )
    } else {
        stringResource(R.string.home_more_smart_adjustments_evidence_waiting)
    }
    val directionValue = when (smartAdjustment.direction) {
        PaydayAdjustmentRecommendationDirection.MOVE_MORE_TO_PRIORITY ->
            stringResource(R.string.home_more_smart_adjustments_direction_move)
        PaydayAdjustmentRecommendationDirection.RESTORE_FLEXIBLE_BUFFER ->
            stringResource(R.string.home_more_smart_adjustments_direction_restore)
        PaydayAdjustmentRecommendationDirection.KEEP_PLAN ->
            stringResource(R.string.home_more_smart_adjustments_direction_keep)
        null -> stringResource(R.string.home_more_smart_adjustments_direction_waiting)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 300.dp
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartAdjustmentSignalPill(
                    label = stringResource(R.string.home_more_smart_adjustments_next_flex),
                    value = flexibleValue,
                    icon = Icons.Default.Payments,
                    modifier = Modifier.fillMaxWidth()
                )
                SmartAdjustmentSignalPill(
                    label = stringResource(R.string.home_more_smart_adjustments_next_priority),
                    value = priorityValue,
                    icon = Icons.Default.Flag,
                    modifier = Modifier.fillMaxWidth()
                )
                SmartAdjustmentSignalPill(
                    label = stringResource(R.string.home_more_smart_adjustments_signal),
                    value = directionValue,
                    icon = Icons.Default.AutoGraph,
                    modifier = Modifier.fillMaxWidth()
                )
                SmartAdjustmentSignalPill(
                    label = stringResource(R.string.home_more_smart_adjustments_evidence),
                    value = evidenceValue,
                    icon = Icons.Default.QueryStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmartAdjustmentSignalPill(
                        label = stringResource(R.string.home_more_smart_adjustments_next_flex),
                        value = flexibleValue,
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                    SmartAdjustmentSignalPill(
                        label = stringResource(R.string.home_more_smart_adjustments_next_priority),
                        value = priorityValue,
                        icon = Icons.Default.Flag,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmartAdjustmentSignalPill(
                        label = stringResource(R.string.home_more_smart_adjustments_signal),
                        value = directionValue,
                        icon = Icons.Default.AutoGraph,
                        modifier = Modifier.weight(1f)
                    )
                    SmartAdjustmentSignalPill(
                        label = stringResource(R.string.home_more_smart_adjustments_evidence),
                        value = evidenceValue,
                        icon = Icons.Default.QueryStats,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartAdjustmentSignalPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 74.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(17.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SmartAdjustmentLastAction(
    messageKey: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val message = remember(messageKey) {
        val resId = context.resources.getIdentifier(messageKey, "string", context.packageName)
        if (resId != 0) context.getString(resId) else messageKey
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MySharePositive,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SmartAdjustmentReviewButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoGraph,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )
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
private fun MorePremiumUpgradeSection(
    state: MoreCardState,
    activity: android.app.Activity?,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: (android.app.Activity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PremiumSectionHeader(title = stringResource(R.string.home_more_subscription_title))
        MorePremiumPreviewCard(state = state)
        Spacer(Modifier.height(12.dp))

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
        val selectedPriceCurrencyCode = if (state.selectedBillingPlan == BillingPlan.ANNUAL) {
            state.actualAnnualPriceCurrencyCode
        } else {
            state.actualMonthlyPriceCurrencyCode
        }
        val currencyMismatchNotice = selectedPriceCurrencyCode
            ?.takeUnless { it.equals(state.userPreferences.currencyCode, ignoreCase = true) }
            ?.let {
                stringResource(
                    R.string.paywall_currency_mismatch_notice,
                    state.userPreferences.currencyCode,
                    it
                )
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
        if (currencyMismatchNotice != null) {
            Text(
                text = currencyMismatchNotice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
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

@Composable
private fun MorePremiumPreviewCard(
    state: MoreCardState,
    modifier: Modifier = Modifier
) {
    val hasPlanValues = state.weeklyGuideLabel.isNotBlank() && state.priorityMoveLabel.isNotBlank()
    val body = if (hasPlanValues) {
        stringResource(
            R.string.home_more_premium_preview_body,
            state.weeklyGuideLabel,
            state.priorityMoveLabel,
            state.ruleCount
        )
    } else {
        stringResource(
            R.string.home_more_premium_preview_body_generic,
            state.ruleCount
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.2f))
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(9.dp).size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_more_premium_preview_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.home_more_premium_preview_title),
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

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val shouldStack = maxWidth < 300.dp
                if (shouldStack) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MorePremiumPreviewPill(
                            label = stringResource(R.string.home_more_premium_preview_free_label),
                            body = stringResource(R.string.home_more_premium_preview_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MorePremiumPreviewPill(
                            label = stringResource(R.string.home_more_premium_preview_paid_label),
                            body = stringResource(R.string.home_more_premium_preview_paid_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MorePremiumPreviewPill(
                            label = stringResource(R.string.home_more_premium_preview_free_label),
                            body = stringResource(R.string.home_more_premium_preview_free_body),
                            icon = Icons.Default.RadioButtonUnchecked,
                            modifier = Modifier.weight(1f)
                        )
                        MorePremiumPreviewPill(
                            label = stringResource(R.string.home_more_premium_preview_paid_label),
                            body = stringResource(R.string.home_more_premium_preview_paid_body),
                            icon = Icons.Default.CheckCircle,
                            iconColor = MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MorePremiumPreviewPill(
    label: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color? = null
) {
    val resolvedIconColor = iconColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedIconColor,
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = resolvedIconColor,
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
private fun AccountFeedback(state: MoreCardState) {
    val context = LocalContext.current
    val messageKey = state.logoutError ?: state.googleConnectionError ?: state.googleConnectionMessage ?: return
    val message = remember(messageKey) {
        val resId = context.resources.getIdentifier(messageKey, "string", context.packageName)
        if (resId != 0) context.getString(resId) else messageKey
    }
    val isError = state.logoutError != null || state.googleConnectionError != null

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

@OptIn(ExperimentalLayoutApi::class)
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badge != null) {
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.price_per_period, price, period),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (comparisonPrice != null) {
                            Text(
                                text = stringResource(R.string.price_per_period, comparisonPrice, period),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 1.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
