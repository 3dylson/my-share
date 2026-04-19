package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*

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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(adsConsentManager) {
        adsConsentManager?.let {
            viewModel.updateAdsConsentRequirement(it.isPrivacyOptionsRequired)
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
    onUnlockPremium: (android.app.Activity) -> Unit,
    onLogout: () -> Unit,
    onManageAdsConsent: () -> Unit,
    onAddNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    onAddNewRule: () -> Unit,
    onEditRule: (String) -> Unit
) {
    val activity = androidx.activity.compose.LocalActivity.current
    Scaffold(
        modifier = modifier,
        containerColor = MyShareBackground,
        topBar = {
            Column(modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                PremiumAppHeader(
                    title = "My Share",
                    subtitle = "Financial clarity, simplified."
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                HomeDestination.entries.forEach { destination ->
                    val isSelected = state.selectedDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onDestinationSelected(destination) },
                        icon = {
                            val icon = when (destination) {
                                HomeDestination.PLAN -> if (isSelected) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday
                                HomeDestination.RULES -> if (isSelected) Icons.Filled.SettingsSuggest else Icons.Outlined.SettingsSuggest
                                HomeDestination.GOALS -> if (isSelected) Icons.Filled.Flag else Icons.Outlined.Flag
                                HomeDestination.REVIEW -> if (isSelected) Icons.Filled.AutoGraph else Icons.Outlined.AutoGraph
                                HomeDestination.MORE -> if (isSelected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz
                            }
                            Icon(icon, contentDescription = null)
                        },
                        label = { 
                            Text(
                                destination.name.lowercase().replaceFirstChar(Char::titlecase),
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
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MySharePrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (state.selectedDestination) {
                HomeDestination.PLAN -> {
                    homePlanTab(
                        planCard = state.planCard,
                        isPremium = state.moreCard.isPremium,
                        onDestinationSelected = onDestinationSelected
                    )
                }
                HomeDestination.RULES -> {
                    homeRulesTab(
                        rules = state.rules,
                        isPremium = state.moreCard.isPremium,
                        onAddNewRule = onAddNewRule,
                        onEditRule = onEditRule,
                        onDestinationSelected = onDestinationSelected
                    )
                }
                HomeDestination.GOALS -> {
                    homeGoalsTab(
                        goals = state.goals,
                        isPremium = state.moreCard.isPremium,
                        onAddNewGoal = onAddNewGoal,
                        onEditGoal = onEditGoal,
                        onDestinationSelected = onDestinationSelected
                    )
                }
                HomeDestination.REVIEW -> {
                    homeReviewTab(
                        state = state.reviewCard,
                        history = state.reviewHistory,
                        isPremium = state.moreCard.isPremium,
                        onFlexibleSpendChanged = onFlexibleSpendChanged,
                        onGoalContributionChanged = onGoalContributionChanged,
                        onSaveReview = onSaveReview,
                        onDestinationSelected = onDestinationSelected
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
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MyShareTheme {
        HomeScreen(
            state = HomeState(
                isLoading = false,
                selectedDestination = HomeDestination.PLAN,
                planCard = HomePlanCardState(
                    nextPaydayLabel = "Payday: 2 April",
                    incomeLabel = "€1,500.00",
                    fixedCostsLabel = "€620.00",
                    flexibleSpendLabel = "€380.00",
                    savingsLabel = "€300.00",
                    investingLabel = "€120.00",
                    weeklySpendLabel = "€87.00",
                    summary = "A calm split that protects essentials and builds savings."
                ),
                goals = listOf(
                    GoalCardState(
                        id = "1",
                        goalName = "Emergency Fund",
                        goalAmountLabel = "€3,000.00",
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
            onUnlockPremium = {},
            onLogout = {},
            onManageAdsConsent = {},
            onAddNewGoal = {},
            onEditGoal = {},
            onAddNewRule = {},
            onEditRule = {}
        )
    }
}
