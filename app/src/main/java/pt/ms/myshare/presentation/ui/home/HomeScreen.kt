package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pt.ms.myshare.domain.model.BillingPlan
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
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
        onAddNewGoal = { navController.navigate("add_goal") }
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
    onAddNewGoal: () -> Unit
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
                    state.planCard?.let { planCard ->
                        item {
                            PremiumPlanSummary(
                                headline = planCard.nextPaydayLabel,
                                body = planCard.summary
                            )
                        }
                        item {
                            PremiumSectionHeader(title = "Core Metrics")
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PremiumMetricCard(
                                    label = "Income per payday", 
                                    value = planCard.incomeLabel,
                                    icon = Icons.Default.Payments
                                )
                                PremiumMetricCard(
                                    label = "Weekly Guide", 
                                    value = planCard.weeklySpendLabel,
                                    subtitle = "Safe to spend every week",
                                    icon = Icons.Default.AccountBalanceWallet
                                )
                            }
                        }
                        item {
                            PremiumSectionHeader(title = "Allocation Preview")
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                PremiumMetricCard(
                                    label = "Fixed", 
                                    value = planCard.fixedCostsLabel, 
                                    modifier = Modifier.weight(1f),
                                    color = MyShareSecondary
                                )
                                PremiumMetricCard(
                                    label = "Flexible", 
                                    value = planCard.flexibleSpendLabel, 
                                    modifier = Modifier.weight(1f),
                                    color = MyShareSecondary
                                )
                            }
                        }
                        if (!state.moreCard.isPremium) {
                            item {
                                PremiumBenefitCard(
                                    title = "Smart Adjustments",
                                    description = "Enable Automation to automatically adjust your weekly guide based on last month's review.",
                                    icon = Icons.Default.PrecisionManufacturing,
                                    onClick = { onDestinationSelected(HomeDestination.MORE) }
                                )
                            }
                        }
                    }
                }
                HomeDestination.GOALS -> {
                    state.goalCard?.let { goalCard ->
                        item {
                            PremiumSectionHeader(title = "Primary Objective")
                            PremiumMetricCard(
                                label = goalCard.goalName, 
                                value = goalCard.goalAmountLabel, 
                                subtitle = goalCard.targetDateLabel,
                                icon = Icons.Default.Flag
                            )
                        }
                        item {
                            PremiumInfoCard(
                                title = "Design Logic", 
                                body = goalCard.progressNote,
                                icon = Icons.Default.TipsAndUpdates,
                                backgroundColor = MySharePrimary.copy(alpha = 0.05f)
                            )
                        }
                        if (!state.moreCard.isPremium) {
                            item {
                                PremiumBenefitCard(
                                    title = "Multiple Goals",
                                    description = "Don't stop at one. Track home deposists, travel funds, and more simultaneously.",
                                    icon = Icons.Default.AddBusiness,
                                    onClick = { onDestinationSelected(HomeDestination.MORE) }
                                )
                            }
                            item {
                                PremiumPaywallCard(
                                    title = "Unlock Advanced Goals",
                                    price = "Premium",
                                    period = "feature",
                                    description = "Track multiple goals simultaneously and get trajectory predictions.",
                                    isSelected = false,
                                    onClick = { onDestinationSelected(HomeDestination.MORE) }
                                )
                            }
                        } else {
                            item {
                                PremiumButton(
                                    text = "Add New Goal",
                                    onClick = onAddNewGoal,
                                    icon = Icons.Default.Add,
                                    containerColor = MySharePrimaryContainer,
                                    contentColor = MySharePrimary
                                )
                            }
                        }
                    }
                }
                HomeDestination.REVIEW -> {
                    item {
                        PremiumSectionHeader(title = "The Habit Loop")
                        PremiumInfoCard(
                            title = "Manual Check-in", 
                            body = "Log actual spend vs. blueprint. This feedback loop is the key to financial awareness.",
                            icon = Icons.Default.HistoryEdu
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PremiumTextField(
                                value = state.reviewCard.actualFlexibleSpend,
                                onValueChange = onFlexibleSpendChanged,
                                label = "Actual Flexible Spend",
                                prefix = { Text("$ ", color = MyShareSecondary) }
                            )
                            PremiumTextField(
                                value = state.reviewCard.actualGoalContribution,
                                onValueChange = onGoalContributionChanged,
                                label = "Actual Goal Contribution",
                                prefix = { Text("$ ", color = MyShareSecondary) }
                            )
                            
                            PremiumButton(
                                text = "Submit Review",
                                onClick = onSaveReview,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // History / Experiment Area (Ads Zone)
                    if (!state.moreCard.isPremium) {
                        item {
                            PremiumSectionHeader(title = "Previous Sessions")
                            PremiumInfoCard(
                                title = "History is Premium", 
                                body = "Detailed plan-vs-actual history is available for pro members. Your last session is shown above.",
                                icon = Icons.Default.Lock
                            )
                            Spacer(Modifier.height(8.dp))
                            PremiumAdBanner()
                        }
                    }
                }
                HomeDestination.MORE -> {
                    item {
                        PremiumSectionHeader(title = "Global Settings")
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PremiumChoiceCard(
                                title = "Smart Notifications",
                                description = state.moreCard.reminderLabel,
                                isSelected = state.moreCard.reminderEnabled,
                                onClick = { onToggleReminder(!state.moreCard.reminderEnabled) },
                                icon = Icons.Default.Notifications
                            )
                            
                            PremiumChoiceCard(
                                title = "Smart Automation",
                                description = if (state.moreCard.automationEnabled) "System automatically adjusting buffers" else "Disabled",
                                isSelected = state.moreCard.automationEnabled,
                                onClick = { 
                                    if (state.moreCard.isPremium) {
                                        onToggleAutomation(!state.moreCard.automationEnabled)
                                    } else {
                                        // Show paywall or stay as is
                                    }
                                },
                                icon = Icons.Default.PrecisionManufacturing,
                                badge = if (!state.moreCard.isPremium) "PREMIUM" else null
                            )
                        }
                    }
                    if (!state.moreCard.isPremium) {
                        item {
                            PremiumPaywallCard(
                                title = "Annual Membership",
                                price = "$49.99",
                                period = "year",
                                description = "Unlock automation, multiple goals, and detailed sync.",
                                badge = "60% OFF",
                                isSelected = state.moreCard.selectedBillingPlan == BillingPlan.ANNUAL,
                                onClick = { onBillingPlanSelected(BillingPlan.ANNUAL) }
                            )
                        }
                        item {
                            PremiumPaywallCard(
                                title = "Monthly Membership",
                                price = "$5.99",
                                period = "month",
                                description = "Flexible access to all premium features.",
                                isSelected = state.moreCard.selectedBillingPlan == BillingPlan.MONTHLY,
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
                            state.moreCard.userEmail?.let { email ->
                                PremiumMetricCard(
                                    label = "Signed in as",
                                    value = email,
                                    icon = Icons.Default.AccountCircle
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
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
                                        onClick = { uriHandler.openUri("https://myshare.pt/privacy") }
                                    )
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
                goalCard = GoalCardState(
                    goalName = "Emergency Fund",
                    goalAmountLabel = "€3,000.00",
                    targetDateLabel = "On pace for November 2026",
                    progressNote = "Consistency is your greatest asset."
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
            onAddNewGoal = {}
        )
    }
}
