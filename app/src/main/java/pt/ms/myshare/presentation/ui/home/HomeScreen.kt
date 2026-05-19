package pt.ms.myshare.presentation.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReadResult
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReader
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.paywall.BillingStatusMessageKeys
import pt.ms.myshare.presentation.ui.preferences.CurrencyPickerDialog
import pt.ms.myshare.presentation.ui.preferences.LanguagePickerDialog
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import timber.log.Timber

/**
 * Responsibility: Orchestrates the Home screen by coordinating sub-composables for each tab.
 * Adheres to SRP by delegating rendering logic for tabs to dedicated files.
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavController,
    onManageAdsConsent: () -> Unit = {},
    adsConsentManager: pt.ms.myshare.presentation.ui.ads.AdsConsentManager? = null,
    onFreeHomeReady: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(adsConsentManager) {
        adsConsentManager?.let {
            viewModel.updateAdsConsentRequirement(true)
        }
    }

    LaunchedEffect(uiState.isLoading, uiState.moreCard.isPremium) {
        if (!uiState.isLoading && !uiState.moreCard.isPremium) {
            onFreeHomeReady()
        }
    }

    HomeScreen(
        modifier = modifier,
        state = uiState,
        onDestinationSelected = viewModel::selectDestination,
        onFlexibleSpendChanged = viewModel::onFlexibleSpendChanged,
        onGoalContributionChanged = viewModel::onGoalContributionChanged,
        onSaveReview = viewModel::saveReview,
        onToggleReminder = viewModel::toggleReminder,
        onSaveReminderConfiguration = viewModel::saveReminderConfiguration,
        onToggleAutomation = viewModel::onToggleAutomation,
        onBillingPlanSelected = viewModel::chooseBillingPlan,
        onUnlockPremium = viewModel::unlockPremium,
        onPremiumGateViewed = viewModel::logPremiumGateViewed,
        onPremiumGateUpgradeClicked = viewModel::logPremiumGateUpgradeClicked,
        onConnectGoogleAccount = viewModel::connectGoogleAccount,
        onGoogleConnectionCredentialError = viewModel::setGoogleConnectionCredentialError,
        onDismissPremiumAccountPrompt = viewModel::dismissPremiumAccountPrompt,
        onReviewSavedFeedbackShown = viewModel::clearReviewSavedFeedback,
        onApplyPaydayRecommendation = viewModel::applyPaydayRecommendation,
        onUndoPaydayRecommendation = viewModel::undoPaydayRecommendation,
        onUpdateReview = viewModel::updateReview,
        onDeleteReview = viewModel::deleteReview,
        onLanguageSelected = viewModel::updateLanguage,
        onCurrencySelected = viewModel::updateCurrency,
        onLogout = {
            viewModel.onLogout {
                navController.navigate("onboarding") {
                    popUpTo("home") { inclusive = true }
                }
            }
        },
        onManageAdsConsent = onManageAdsConsent,
        onAddNewGoal = { navController.navigate("add_goal") },
        onEditGoal = { id -> navController.navigate("add_goal?goalId=$id") },
        onAddNewRule = { navController.navigate("add_rule") },
        onEditRule = { id -> navController.navigate("add_rule?ruleId=$id") }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeState,
    onDestinationSelected: (HomeDestination) -> Unit,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSaveReminderConfiguration: (Int, Int, ReminderCadence) -> Unit,
    onToggleAutomation: (Boolean) -> Unit,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: (android.app.Activity, String) -> Unit,
    onPremiumGateViewed: (HomePremiumGate) -> Unit,
    onPremiumGateUpgradeClicked: (HomePremiumGate) -> Unit,
    onConnectGoogleAccount: (String) -> Unit,
    onGoogleConnectionCredentialError: (String) -> Unit,
    onDismissPremiumAccountPrompt: () -> Unit,
    onReviewSavedFeedbackShown: (Long) -> Unit,
    onApplyPaydayRecommendation: () -> Boolean,
    onUndoPaydayRecommendation: () -> Boolean,
    onUpdateReview: (String, String, String) -> Boolean,
    onDeleteReview: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onLogout: () -> Unit,
    onManageAdsConsent: () -> Unit,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit
) {
    val activity = androidx.activity.compose.LocalActivity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleIdTokenReader = remember(context) {
        GoogleIdTokenReader(
            credentialManager = CredentialManager.create(context),
            serverClientId = BuildConfig.GOOGLE_CLIENT_ID
        )
    }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val reviewSavedMessage = stringResource(R.string.home_review_saved_feedback)
    var showPaywallSheet by remember { mutableStateOf(false) }
    var premiumGate by remember { mutableStateOf(HomePremiumGate.General) }
    var showAutomationLockDialog by remember { mutableStateOf(false) }
    var showReminderSettingsDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAccountDetailsDialog by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showReviewHistoryTimeline by remember { mutableStateOf(false) }
    var showStrategyGoalArchive by remember { mutableStateOf(false) }
    var showStrategyRuleArchive by remember { mutableStateOf(false) }
    var showPremiumAdjustmentHistory by remember { mutableStateOf(false) }
    var showPremiumReviewResultSheet by remember { mutableStateOf(false) }
    var recommendationPendingApply by remember { mutableStateOf<PaydayAdjustmentRecommendationState?>(null) }
    var showRecommendationAppliedSheet by remember { mutableStateOf(false) }
    var reviewBeingEdited by remember { mutableStateOf<ReviewHistoryItemState?>(null) }
    var reviewPendingDelete by remember { mutableStateOf<ReviewHistoryItemState?>(null) }
    var isGoogleCredentialRequestInProgress by remember { mutableStateOf(false) }
    var pendingReminderSelection by remember { mutableStateOf<ReminderSettingsSelection?>(null) }
    val clearFocusOnScrollConnection = rememberKeyboardDismissOnScrollConnection()
    val notificationPermissionDeniedMessage = stringResource(R.string.onboarding_reminder_error_permission)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val selection = pendingReminderSelection
            pendingReminderSelection = null
            if (granted && selection != null) {
                onSaveReminderConfiguration(selection.hourOfDay, selection.minute, selection.cadence)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(notificationPermissionDeniedMessage)
                }
            }
        }
    )
    val saveReminderWithPermission: (ReminderSettingsSelection) -> Unit = { selection ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            onSaveReminderConfiguration(selection.hourOfDay, selection.minute, selection.cadence)
        } else {
            pendingReminderSelection = selection
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(showPaywallSheet, state.moreCard.isPremium) {
        if (showPaywallSheet && state.moreCard.isPremium) {
            showPaywallSheet = false
            Timber.tag("HomeScreen").d("Premium gate dismissed after entitlement activation")
        }
    }

    LaunchedEffect(state.moreCard.isPremium, state.reviewCard.paydayRecommendation?.isApplyable) {
        if (recommendationPendingApply != null &&
            (!state.moreCard.isPremium || state.reviewCard.paydayRecommendation?.isApplyable != true)
        ) {
            recommendationPendingApply = null
            Timber.tag("HomeScreen").d("Recommendation confirmation dismissed after recommendation changed")
        }
    }

    val openPremiumGate: (HomePremiumGate) -> Unit = { gate ->
        premiumGate = gate
        showPaywallSheet = true
        onPremiumGateViewed(gate)
    }
    val startGoogleAccountConnection: () -> Unit = {
        if (!isGoogleCredentialRequestInProgress && !state.moreCard.isGoogleConnectionInProgress) {
            coroutineScope.launch {
                isGoogleCredentialRequestInProgress = true
                when (val result = googleIdTokenReader.readIdToken(context, "home_more")) {
                    is GoogleIdTokenReadResult.Success -> onConnectGoogleAccount(result.idToken)
                    GoogleIdTokenReadResult.NoCredential -> onGoogleConnectionCredentialError("home_more_account_connect_google_error_no_credentials")
                    GoogleIdTokenReadResult.UnsupportedCredential -> onGoogleConnectionCredentialError("home_more_account_connect_google_error_generic")
                    is GoogleIdTokenReadResult.Failure -> onGoogleConnectionCredentialError("home_more_account_connect_google_error_generic")
                }
                isGoogleCredentialRequestInProgress = false
            }
        }
    }

    LaunchedEffect(state.reviewSavedEventId) {
        val eventId = state.reviewSavedEventId
        if (eventId > 0L) {
            onReviewSavedFeedbackShown(eventId)
            if (state.moreCard.isPremium) {
                showPremiumReviewResultSheet = true
                Timber.tag("HomeScreen").d("Premium review result sheet opened after saved review")
            } else {
                snackbarHostState.showSnackbar(
                    message = reviewSavedMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (showPaywallSheet) {
        val isPurchaseActivationPending = state.moreCard.billingMessage == BillingStatusMessageKeys.COMPLETED
        PremiumPaywallBottomSheet(
            onDismissRequest = { showPaywallSheet = false },
            title = stringResource(premiumGate.titleRes),
            body = stringResource(premiumGate.bodyRes),
            isBillingActionInProgress = state.moreCard.isBillingActionInProgress || isPurchaseActivationPending,
            billingMessage = state.moreCard.billingMessage,
            onUpgradeClick = { 
                onPremiumGateUpgradeClicked(premiumGate)
                activity?.let { onUnlockPremium(it, premiumGate.analyticsName) }
            }
        )
    }

    if (showReviewHistoryTimeline) {
        ReviewHistoryTimelineBottomSheet(
            history = state.reviewHistory,
            onDismissRequest = { showReviewHistoryTimeline = false },
            onEditReview = { review ->
                showReviewHistoryTimeline = false
                reviewBeingEdited = review
            },
            onDeleteReview = { review ->
                showReviewHistoryTimeline = false
                reviewPendingDelete = review
            }
        )
    }

    if (showPremiumReviewResultSheet) {
        state.reviewCard.premiumReviewResult?.let { result ->
            PremiumReviewResultBottomSheet(
                result = result,
                onDismissRequest = { showPremiumReviewResultSheet = false },
                onReviewAdjustment = { recommendation ->
                    showPremiumReviewResultSheet = false
                    recommendationPendingApply = recommendation
                    Timber.tag("HomeScreen").d("Recommendation confirmation opened from Premium review result")
                }
            )
        }
    }

    if (showStrategyGoalArchive) {
        StrategyGoalArchiveBottomSheet(
            goals = state.goals,
            onDismissRequest = { showStrategyGoalArchive = false },
            onEditGoal = { goalId ->
                showStrategyGoalArchive = false
                onEditGoal(goalId)
            }
        )
    }

    if (showStrategyRuleArchive) {
        StrategyRuleArchiveBottomSheet(
            rules = state.rules,
            onDismissRequest = { showStrategyRuleArchive = false },
            onEditRule = { ruleId ->
                showStrategyRuleArchive = false
                onEditRule(ruleId)
            }
        )
    }

    if (showPremiumAdjustmentHistory) {
        PremiumAdjustmentHistoryBottomSheet(
            history = state.moreCard.adjustmentHistory,
            onDismissRequest = { showPremiumAdjustmentHistory = false }
        )
    }

    recommendationPendingApply?.let { recommendation ->
        PaydayRecommendationConfirmationBottomSheet(
            recommendation = recommendation,
            onDismissRequest = { recommendationPendingApply = null },
            onConfirm = {
                val accepted = onApplyPaydayRecommendation()
                if (accepted) {
                    recommendationPendingApply = null
                    showRecommendationAppliedSheet = true
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    Timber.tag("HomeScreen").d("Payday recommendation apply confirmed")
                }
            }
        )
    }

    if (showRecommendationAppliedSheet) {
        PaydayRecommendationAppliedBottomSheet(
            onDismissRequest = { showRecommendationAppliedSheet = false },
            onReviewRules = {
                showRecommendationAppliedSheet = false
                onDestinationSelected(HomeDestination.STRATEGY)
                Timber.tag("HomeScreen").d("Navigated to Strategy after applying recommendation")
            },
            onUndo = {
                val undone = onUndoPaydayRecommendation()
                showRecommendationAppliedSheet = false
                if (undone) {
                    Timber.tag("HomeScreen").d("Payday recommendation undo requested")
                }
            }
        )
    }

    reviewBeingEdited?.let { review ->
        ReviewCorrectionBottomSheet(
            item = review,
            currencySymbol = state.reviewCard.currencySymbol,
            onDismissRequest = { reviewBeingEdited = null },
            onSave = { flexibleSpend, goalContribution ->
                onUpdateReview(review.id, flexibleSpend, goalContribution)
            }
        )
    }

    reviewPendingDelete?.let { review ->
        MyShareAlertDialog(
            onDismissRequest = { reviewPendingDelete = null },
            icon = Icons.Default.Delete,
            iconTint = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.home_review_delete_title),
            message = stringResource(R.string.home_review_delete_desc, review.dateLabel),
            confirmText = stringResource(R.string.home_review_delete_confirm),
            onConfirm = {
                Timber.tag("HomeScreen").d("Review delete confirmed: %s", review.id)
                reviewPendingDelete = null
                onDeleteReview(review.id)
            },
            dismissText = stringResource(R.string.dialog_cancel),
            onDismiss = { reviewPendingDelete = null },
            actionStyle = MyShareDialogActionStyle.Destructive
        )
    }

    if (showAutomationLockDialog) {
        MyShareAlertDialog(
            onDismissRequest = { showAutomationLockDialog = false },
            icon = Icons.Default.PrecisionManufacturing,
            title = stringResource(R.string.home_more_automation_locked_title),
            message = stringResource(R.string.home_more_automation_locked_body),
            confirmText = stringResource(R.string.home_more_automation_locked_action),
            onConfirm = {
                Timber.tag("HomeScreen").d("Premium gate opened from Smart automation lock")
                showAutomationLockDialog = false
                openPremiumGate(HomePremiumGate.SmartAutomation)
            },
            dismissText = stringResource(R.string.dialog_not_now),
            onDismiss = {
                showAutomationLockDialog = false
            }
        )
    }

    if (showReminderSettingsDialog) {
        HomeReminderSettingsDialog(
            initialHourOfDay = state.moreCard.reminderHourOfDay,
            initialMinute = state.moreCard.reminderMinute,
            initialCadence = state.moreCard.reminderCadence,
            onDismissRequest = { showReminderSettingsDialog = false },
            onSave = { hourOfDay, minute, cadence ->
                showReminderSettingsDialog = false
                saveReminderWithPermission(ReminderSettingsSelection(hourOfDay, minute, cadence))
            }
        )
    }

    if (state.moreCard.showPremiumAccountPrompt) {
        MyShareAlertDialog(
            onDismissRequest = onDismissPremiumAccountPrompt,
            icon = Icons.Default.WorkspacePremium,
            title = stringResource(R.string.paywall_secure_account_title),
            message = stringResource(R.string.paywall_secure_account_body),
            confirmText = stringResource(R.string.paywall_secure_account_button),
            onConfirm = {
                onDismissPremiumAccountPrompt()
                startGoogleAccountConnection()
            },
            dismissText = stringResource(R.string.paywall_secure_account_continue),
            onDismiss = onDismissPremiumAccountPrompt
        )
    }

    if (showAccountDetailsDialog) {
        val accountLabel = state.moreCard.userEmail
            ?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.home_more_guest)
        MyShareAlertDialog(
            onDismissRequest = { showAccountDetailsDialog = false },
            icon = Icons.Default.AccountCircle,
            title = stringResource(R.string.home_more_account_details_title),
            confirmText = stringResource(R.string.dialog_close),
            onConfirm = { showAccountDetailsDialog = false },
            actionStyle = MyShareDialogActionStyle.Text
        ) {
            Text(
                text = stringResource(
                    R.string.home_more_account_details_email,
                    accountLabel
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.home_more_account_details_membership,
                    if (state.moreCard.isPremium) {
                        stringResource(R.string.home_more_account_premium_member_display)
                    } else {
                        stringResource(R.string.home_more_account_basic_member)
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!state.moreCard.isPremium) {
                Text(
                    text = stringResource(R.string.home_more_account_details_sync_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            selectedLanguageTag = state.moreCard.userPreferences.languageTag,
            onDismissRequest = { showLanguagePicker = false },
            onLanguageSelected = {
                showLanguagePicker = false
                onLanguageSelected(it)
            }
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCurrencyCode = state.moreCard.userPreferences.currencyCode,
            locale = state.moreCard.userPreferences.locale,
            onDismissRequest = { showCurrencyPicker = false },
            onCurrencySelected = {
                showCurrencyPicker = false
                onCurrencySelected(it)
            }
        )
    }

    if (showSignOutDialog) {
        MyShareAlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = Icons.AutoMirrored.Filled.Logout,
            iconTint = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.home_more_signout_confirm_title),
            message = stringResource(R.string.home_more_signout_confirm_body),
            confirmText = stringResource(R.string.home_more_account_signout),
            onConfirm = {
                Timber.tag("HomeScreen").d("Sign out confirmed from More tab")
                showSignOutDialog = false
                onLogout()
            },
            dismissText = stringResource(R.string.dialog_cancel),
            onDismiss = {
                showSignOutDialog = false
            },
            actionStyle = MyShareDialogActionStyle.Destructive
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .zIndex(1f),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    val useCompactPremiumBadge = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeTopBarTitle(
                            selectedDestination = state.selectedDestination,
                            modifier = Modifier.weight(1f)
                        )
                        HomePremiumStatusBadge(
                            isPremium = state.moreCard.isPremium,
                            compact = useCompactPremiumBadge
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                HomeDestination.entries.forEach { destination ->
                    val isSelected = state.selectedDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            focusManager.clearFocus(force = true)
                            if (!isSelected) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            Timber.tag("HomeScreen").d("Home tab selected: %s", destination.name)
                            onDestinationSelected(destination)
                        },
                        icon = {
                            val icon = when (destination) {
                                HomeDestination.PLAN -> if (isSelected) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday
                                HomeDestination.STRATEGY -> if (isSelected) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb
                                HomeDestination.REVIEW -> if (isSelected) Icons.Filled.AutoGraph else Icons.Outlined.AutoGraph
                                HomeDestination.MORE -> if (isSelected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            Text(
                                stringResource(destination.labelRes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MySharePrimary,
                            selectedTextColor = MySharePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MySharePrimary)
            }
            return@Scaffold
        }

        AnimatedContent(
            targetState = state.selectedDestination,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300, delayMillis = 100)) + 
                 slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(300, delayMillis = 100)))
                .togetherWith(fadeOut(animationSpec = tween(150)))
            },
            label = "TabTransition",
            modifier = Modifier.fillMaxSize()
        ) { targetDestination ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(clearFocusOnScrollConnection)
                    .imePadding()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = innerPadding.calculateTopPadding() + 24.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (targetDestination) {
                    HomeDestination.PLAN -> {
                        homePlanTab(
                            planCard = state.planCard,
                            isPremium = state.moreCard.isPremium,
                            onShowPaywall = { gate ->
                                openPremiumGate(gate)
                            }
                        )
                    }
                    HomeDestination.STRATEGY -> {
                        homeStrategyTab(
                            goals = state.goals,
                            rules = state.rules,
                            planCard = state.planCard,
                            goalPaydaySplit = state.goalPaydaySplit,
                            rulePaydayMix = state.rulePaydayMix,
                            isPremium = state.moreCard.isPremium,
                            onAddNewGoal = onAddNewGoal,
                            onEditGoal = onEditGoal,
                            onAddNewRule = onAddNewRule,
                            onEditRule = onEditRule,
                            onOpenGoalArchive = {
                                Timber.tag("HomeScreen").d("Strategy goal archive opened")
                                showStrategyGoalArchive = true
                            },
                            onOpenRuleArchive = {
                                Timber.tag("HomeScreen").d("Strategy rule archive opened")
                                showStrategyRuleArchive = true
                            },
                            onShowPaywall = { gate ->
                                openPremiumGate(gate)
                            }
                        )
                    }
                    HomeDestination.REVIEW -> {
                        homeReviewTab(
                            state = state.reviewCard,
                            history = state.reviewHistory,
                            performanceStats = state.performanceStats,
                            isPremium = state.moreCard.isPremium,
                            onFlexibleSpendChanged = onFlexibleSpendChanged,
                            onGoalContributionChanged = onGoalContributionChanged,
                            onSaveReview = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                Timber.tag("HomeScreen").d("Manual review save requested")
                                onSaveReview()
                            },
                            onShowPaywall = {
                                openPremiumGate(HomePremiumGate.ReviewHistory)
                            },
                            onShowFirstReviewPaywall = {
                                openPremiumGate(HomePremiumGate.FirstReview)
                            },
                            onOpenFullHistory = {
                                showReviewHistoryTimeline = true
                            },
                            onEditReview = { review ->
                                reviewBeingEdited = review
                            },
                            onDeleteReview = { review ->
                                reviewPendingDelete = review
                            },
                            onConfigureReminder = {
                                Timber.tag("HomeScreen").d("Reminder settings opened from Premium check-in")
                                showReminderSettingsDialog = true
                            },
                            onApplyPaydayRecommendation = {
                                state.reviewCard.paydayRecommendation?.let { recommendation ->
                                    recommendationPendingApply = recommendation
                                }
                            }
                        )
                    }
                    HomeDestination.MORE -> {
                        homeMoreTab(
                            state = state.moreCard,
                            activity = activity,
                            onToggleReminder = { enabled ->
                                if (enabled) {
                                    showReminderSettingsDialog = true
                                } else {
                                    onToggleReminder(false)
                                }
                            },
                            onConfigureReminder = {
                                Timber.tag("HomeScreen").d("Reminder settings opened from More tab")
                                showReminderSettingsDialog = true
                            },
                            onToggleAutomation = onToggleAutomation,
                            onBillingPlanSelected = onBillingPlanSelected,
                            onUnlockPremium = onUnlockPremium,
                            onManageAdsConsent = onManageAdsConsent,
                            onShowLanguagePicker = { showLanguagePicker = true },
                            onShowCurrencyPicker = { showCurrencyPicker = true },
                            onShowAutomationLock = {
                                Timber.tag("HomeScreen").d("Locked Smart automation row tapped")
                                showAutomationLockDialog = true
                            },
                            onShowAccountDetails = {
                                Timber.tag("HomeScreen").d("Account details opened from More tab")
                                showAccountDetailsDialog = true
                            },
                            onOpenAdjustmentHistory = {
                                Timber.tag("HomeScreen").d("Premium adjustment history opened")
                                showPremiumAdjustmentHistory = true
                            },
                            onOpenReview = {
                                Timber.tag("HomeScreen").d("Smart adjustment review opened from More tab")
                                onDestinationSelected(HomeDestination.REVIEW)
                            },
                            isGoogleCredentialRequestInProgress = isGoogleCredentialRequestInProgress,
                            onConnectGoogle = startGoogleAccountConnection,
                            onLogout = {
                                if (state.moreCard.requiresPremiumAccountProtectionBeforeLogout) {
                                    Timber.tag("HomeScreen").d("Premium sign out requested before account is secured")
                                    onLogout()
                                } else {
                                    Timber.tag("HomeScreen").d("Sign out confirmation requested")
                                    showSignOutDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBarTitle(
    selectedDestination: HomeDestination,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black,
            maxLines = 2
        )
        Text(
            text = stringResource(selectedDestination.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
private fun HomePremiumStatusBadge(
    isPremium: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isPremium) return

    if (compact) {
        Surface(
            modifier = modifier.size(42.dp),
            shape = MaterialTheme.shapes.medium,
            color = MySharePrimary.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = stringResource(R.string.premium_badge),
                    tint = MySharePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    } else {
        AssistChip(
            modifier = modifier,
            onClick = {},
            enabled = false,
            label = { Text(text = stringResource(R.string.premium_badge), maxLines = 1) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

private val HomeDestination.labelRes: Int
    get() = when (this) {
        HomeDestination.PLAN -> R.string.home_tab_plan
        HomeDestination.STRATEGY -> R.string.home_tab_strategy
        HomeDestination.REVIEW -> R.string.home_tab_review
        HomeDestination.MORE -> R.string.home_tab_more
    }

private data class ReminderSettingsSelection(
    val hourOfDay: Int,
    val minute: Int,
    val cadence: ReminderCadence
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
    MyShareTheme {
        HomeScreen(
            state = HomeState(
                isLoading = false,
                selectedDestination = HomeDestination.PLAN,
                planCard = HomePlanCardState(
                    nextPaydayLabel = "Payday: 2 April",
                    incomeLabel = currencyFormat.format(1500),
                    fixedCostsLabel = currencyFormat.format(620),
                    flexibleSpendLabel = currencyFormat.format(380),
                    savingsLabel = currencyFormat.format(300),
                    investingLabel = currencyFormat.format(120),
                    weeklySpendLabel = currencyFormat.format(87),
                    summary = "A calm split that protects essentials and builds savings."
                ),
                goals = listOf(
                    GoalCardState(
                        id = "1",
                        goalNameKey = "goal_default_emergency_fund",
                        goalAmountLabel = currencyFormat.format(3000),
                        targetDateLabel = "On pace for November 2026",
                        progressNote = "Consistency is your greatest asset."
                    )
                )
            ),
            onDestinationSelected = { _ -> },
            onFlexibleSpendChanged = { _ -> },
            onGoalContributionChanged = { _ -> },
            onSaveReview = {},
            onToggleReminder = { _ -> },
            onSaveReminderConfiguration = { _, _, _ -> },
            onToggleAutomation = { _ -> },
            onBillingPlanSelected = { _ -> },
            onUnlockPremium = { _, _ -> },
            onPremiumGateViewed = { _ -> },
            onPremiumGateUpgradeClicked = { _ -> },
            onConnectGoogleAccount = { _ -> },
            onGoogleConnectionCredentialError = { _ -> },
            onDismissPremiumAccountPrompt = {},
            onReviewSavedFeedbackShown = { _ -> },
            onApplyPaydayRecommendation = { true },
            onUndoPaydayRecommendation = { true },
            onUpdateReview = { _, _, _ -> true },
            onDeleteReview = { _ -> },
            onLanguageSelected = { _ -> },
            onCurrencySelected = { _ -> },
            onLogout = {},
            onManageAdsConsent = {},
            onAddNewGoal = {},
            onEditGoal = {},
            onAddNewRule = {},
            onEditRule = {}
        )
    }
}
