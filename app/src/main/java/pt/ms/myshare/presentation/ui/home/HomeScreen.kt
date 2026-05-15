package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
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
        onToggleAutomation = viewModel::onToggleAutomation,
        onBillingPlanSelected = viewModel::chooseBillingPlan,
        onUnlockPremium = viewModel::unlockPremium,
        onPremiumGateViewed = viewModel::logPremiumGateViewed,
        onPremiumGateUpgradeClicked = viewModel::logPremiumGateUpgradeClicked,
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
    onToggleAutomation: (Boolean) -> Unit,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: (android.app.Activity, String) -> Unit,
    onPremiumGateViewed: (HomePremiumGate) -> Unit,
    onPremiumGateUpgradeClicked: (HomePremiumGate) -> Unit,
    onLogout: () -> Unit,
    onManageAdsConsent: () -> Unit,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit
) {
    val activity = androidx.activity.compose.LocalActivity.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val reviewSavedMessage = stringResource(R.string.home_review_saved_feedback)
    var showPaywallSheet by remember { mutableStateOf(false) }
    var premiumGate by remember { mutableStateOf(HomePremiumGate.General) }
    var showAutomationLockDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAccountDetailsDialog by remember { mutableStateOf(false) }
    var previousReviewCount by remember { mutableStateOf<Int?>(null) }
    val openPremiumGate: (HomePremiumGate) -> Unit = { gate ->
        premiumGate = gate
        showPaywallSheet = true
        onPremiumGateViewed(gate)
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
        AlertDialog(
            onDismissRequest = { showAutomationLockDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PrecisionManufacturing,
                    contentDescription = null,
                    tint = MySharePrimary
                )
            },
            title = { Text(text = stringResource(R.string.home_more_automation_locked_title)) },
            text = {
                Text(
                    text = stringResource(R.string.home_more_automation_locked_body)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Timber.tag("HomeScreen").d("Premium gate opened from Smart automation lock")
                        showAutomationLockDialog = false
                        openPremiumGate(HomePremiumGate.SmartAutomation)
                    }
                ) {
                    Text(text = stringResource(R.string.home_more_automation_locked_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutomationLockDialog = false }) {
                    Text(text = stringResource(R.string.dialog_not_now))
                }
            }
        )
    }

    if (showAccountDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDetailsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MySharePrimary
                )
            },
            title = { Text(text = stringResource(R.string.home_more_account_details_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            R.string.home_more_account_details_email,
                            state.moreCard.userEmail ?: stringResource(R.string.home_more_guest)
                        )
                    )
                    Text(
                        text = stringResource(
                            R.string.home_more_account_details_membership,
                            if (state.moreCard.isPremium) {
                                stringResource(R.string.home_more_account_premium_member_display)
                            } else {
                                stringResource(R.string.home_more_account_basic_member)
                            }
                        )
                    )
                    if (!state.moreCard.isPremium) {
                        Text(
                            text = stringResource(R.string.home_more_account_details_sync_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDetailsDialog = false }) {
                    Text(text = stringResource(R.string.dialog_close))
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = stringResource(R.string.home_more_signout_confirm_title)) },
            text = {
                Text(
                    text = stringResource(R.string.home_more_signout_confirm_body)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Timber.tag("HomeScreen").d("Sign out confirmed from More tab")
                        showSignOutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.home_more_account_signout),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(text = stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MyShareBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MyShareBackground)
                    .zIndex(1f),
                color = MyShareBackground,
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
                            color = MyShareOnSurface,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = stringResource(state.selectedDestination.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MyShareSecondary,
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
            Column(modifier = Modifier.navigationBarsPadding()) {
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
                                unselectedIconColor = MyShareSecondary,
                                unselectedTextColor = MyShareSecondary,
                                indicatorColor = MySharePrimaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = innerPadding.calculateTopPadding() + 24.dp,
                    bottom = innerPadding.calculateBottomPadding() + 104.dp
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
                            onToggleReminder = onToggleReminder,
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
            onToggleAutomation = { _ -> },
            onBillingPlanSelected = { _ -> },
            onUnlockPremium = { _, _ -> },
            onPremiumGateViewed = { _ -> },
            onPremiumGateUpgradeClicked = { _ -> },
            onLogout = {},
            onManageAdsConsent = {},
            onAddNewGoal = {},
            onEditGoal = {},
            onAddNewRule = {},
            onEditRule = {}
        )
    }
}
