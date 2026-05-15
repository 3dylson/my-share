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
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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
import pt.ms.myshare.presentation.ui.theme.*
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val snackbarHostState = remember { SnackbarHostState() }
    val reviewSavedMessage = stringResource(R.string.home_review_saved_feedback)
    var showPaywallSheet by remember { mutableStateOf(false) }
    var premiumGate by remember { mutableStateOf(HomePremiumGate.General) }
    var showAutomationLockDialog by remember { mutableStateOf(false) }
    var showReminderSettingsDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAccountDetailsDialog by remember { mutableStateOf(false) }
    var previousReviewCount by remember { mutableStateOf<Int?>(null) }
    var isGoogleCredentialRequestInProgress by remember { mutableStateOf(false) }
    var pendingReminderSelection by remember { mutableStateOf<ReminderSettingsSelection?>(null) }
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

    LaunchedEffect(state.reviewHistory.size) {
        val previousCount = previousReviewCount
        if (previousCount != null && state.reviewHistory.size > previousCount) {
            snackbarHostState.showSnackbar(
                message = reviewSavedMessage,
                duration = SnackbarDuration.Short
            )
        }
        previousReviewCount = state.reviewHistory.size
    }

    if (showPaywallSheet) {
        PremiumPaywallBottomSheet(
            onDismissRequest = { showPaywallSheet = false },
            title = stringResource(premiumGate.titleRes),
            body = stringResource(premiumGate.bodyRes),
            isBillingActionInProgress = state.moreCard.isBillingActionInProgress,
            billingMessage = state.moreCard.billingMessage,
            onUpgradeClick = { 
                onPremiumGateUpgradeClicked(premiumGate)
                activity?.let { onUnlockPremium(it, premiumGate.analyticsName) }
            }
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
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = stringResource(state.selectedDestination.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (state.moreCard.isPremium) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(text = stringResource(R.string.premium_badge)) },
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
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                HomeDestination.entries.forEach { destination ->
                    val isSelected = state.selectedDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
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
                            onDestinationSelected = onDestinationSelected
                        )
                    }
                    HomeDestination.STRATEGY -> {
                        homeStrategyTab(
                            goals = state.goals,
                            rules = state.rules,
                            isPremium = state.moreCard.isPremium,
                            onAddNewGoal = onAddNewGoal,
                            onEditGoal = onEditGoal,
                            onAddNewRule = onAddNewRule,
                            onEditRule = onEditRule,
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
                            onShowAutomationLock = {
                                Timber.tag("HomeScreen").d("Locked Smart automation row tapped")
                                showAutomationLockDialog = true
                            },
                            onShowAccountDetails = {
                                Timber.tag("HomeScreen").d("Account details opened from More tab")
                                showAccountDetailsDialog = true
                            },
                            isGoogleCredentialRequestInProgress = isGoogleCredentialRequestInProgress,
                            onConnectGoogle = startGoogleAccountConnection,
                            onLogout = {
                                Timber.tag("HomeScreen").d("Sign out confirmation requested")
                                showSignOutDialog = true
                            }
                        )
                    }
                }
            }
        }
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
            onLogout = {},
            onManageAdsConsent = {},
            onAddNewGoal = {},
            onEditGoal = {},
            onAddNewRule = {},
            onEditRule = {}
        )
    }
}
