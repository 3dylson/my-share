package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.presentation.ui.theme.MyShareTheme

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
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
        onBillingPlanSelected = viewModel::chooseBillingPlan,
        onUnlockPremium = viewModel::unlockPremium
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
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Share")
                        Text(
                            "When money comes in, know what to do next.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                HomeDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = state.selectedDestination == destination,
                        onClick = { onDestinationSelected(destination) },
                        icon = {
                            when (destination) {
                                HomeDestination.PLAN -> Icon(Icons.Outlined.CalendarToday, contentDescription = null)
                                HomeDestination.GOALS -> Icon(Icons.Outlined.Flag, contentDescription = null)
                                HomeDestination.REVIEW -> Icon(Icons.Outlined.AutoGraph, contentDescription = null)
                                HomeDestination.MORE -> Icon(Icons.Outlined.MoreHoriz, contentDescription = null)
                            }
                        },
                        label = { Text(destination.name.lowercase().replaceFirstChar(Char::titlecase)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                state.emptyMessage?.let {
                    HelperCard(title = "Build your first repeat loop", body = it)
                }
            }
            when (state.selectedDestination) {
                HomeDestination.PLAN -> item {
                    state.planCard?.let { planCard ->
                        PlanTab(
                            planCard = planCard,
                            emptyMessage = state.emptyMessage
                        )
                    }
                }
                HomeDestination.GOALS -> item {
                    state.goalCard?.let { goalCard ->
                        GoalsTab(goalCard = goalCard, isPremium = state.moreCard.isPremium)
                    }
                }
                HomeDestination.REVIEW -> item {
                    ReviewTab(
                        reviewCard = state.reviewCard,
                        onFlexibleSpendChanged = onFlexibleSpendChanged,
                        onGoalContributionChanged = onGoalContributionChanged,
                        onSaveReview = onSaveReview
                    )
                }
                HomeDestination.MORE -> item {
                    MoreTab(
                        moreCard = state.moreCard,
                        onToggleReminder = onToggleReminder,
                        onBillingPlanSelected = onBillingPlanSelected,
                        onUnlockPremium = onUnlockPremium
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanTab(planCard: HomePlanCardState, emptyMessage: String?) {
    HelperCard(title = planCard.nextPaydayLabel, body = planCard.summary)
    MetricCard(title = "Income per payday", value = planCard.incomeLabel)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard(title = "Fixed costs", value = planCard.fixedCostsLabel, modifier = Modifier.weight(1f))
        MetricCard(title = "Flexible spend", value = planCard.flexibleSpendLabel, modifier = Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard(title = "Savings", value = planCard.savingsLabel, modifier = Modifier.weight(1f))
        MetricCard(title = "Investing", value = planCard.investingLabel, modifier = Modifier.weight(1f))
    }
    MetricCard(title = "Weekly spend guide", value = planCard.weeklySpendLabel)
    if (emptyMessage != null) {
        HelperCard(title = "Why this matters", body = "The research pointed to payday planning, reminders, and manual review as the clearest repeat loop for My Share.")
    }
}

@Composable
private fun GoalsTab(goalCard: GoalCardState, isPremium: Boolean) {
    MetricCard(title = goalCard.goalName, value = goalCard.goalAmountLabel, supporting = goalCard.targetDateLabel)
    HelperCard(title = "Free tier", body = goalCard.progressNote)
    if (!isPremium) {
        HelperCard(title = "Premium unlocks", body = "Multiple goals, recurring payday rules, and deeper plan-vs-actual history.")
    }
}

@Composable
private fun ReviewTab(
    reviewCard: ReviewCardState,
    onFlexibleSpendChanged: (String) -> Unit,
    onGoalContributionChanged: (String) -> Unit,
    onSaveReview: () -> Unit
) {
    HelperCard(title = "Manual weekly check-in", body = "Log what actually happened. This is the habit loop that the analytics addendum says the current app is missing.")
    OutlinedTextField(
        value = reviewCard.actualFlexibleSpend,
        onValueChange = onFlexibleSpendChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Actual flexible spend") },
        singleLine = true
    )
    OutlinedTextField(
        value = reviewCard.actualGoalContribution,
        onValueChange = onGoalContributionChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Actual goal contribution") },
        singleLine = true
    )
    reviewCard.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Button(onClick = onSaveReview, modifier = Modifier.fillMaxWidth()) {
        Text("Save review")
    }
    reviewCard.savedReviewDate?.let { Text("Last saved $it", style = MaterialTheme.typography.bodyMedium) }
    reviewCard.insight?.let { insight ->
        MetricCard(title = insight.headline, value = insight.supportingText)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(title = "Spend delta", value = insight.flexibleSpendDelta.toPlainString(), modifier = Modifier.weight(1f))
            MetricCard(title = "Goal delta", value = insight.goalContributionDelta.toPlainString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MoreTab(
    moreCard: MoreCardState,
    onToggleReminder: (Boolean) -> Unit,
    onBillingPlanSelected: (BillingPlan) -> Unit,
    onUnlockPremium: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Payday reminders", style = MaterialTheme.typography.titleMedium)
            Text(moreCard.reminderLabel, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = moreCard.reminderEnabled, onCheckedChange = onToggleReminder)
    }
    moreCard.pricingStrategy?.let { pricing ->
        HelperCard(title = pricing.paywallHeadline, body = pricing.paywallSubhead)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = moreCard.selectedBillingPlan == BillingPlan.MONTHLY,
                onClick = { onBillingPlanSelected(BillingPlan.MONTHLY) },
                label = { Text(pricing.monthlyLabel) }
            )
            FilterChip(
                selected = moreCard.selectedBillingPlan == BillingPlan.ANNUAL,
                onClick = { onBillingPlanSelected(BillingPlan.ANNUAL) },
                label = { Text(pricing.annualLabel) }
            )
        }
        if (!moreCard.isPremium) {
            Button(onClick = onUnlockPremium, modifier = Modifier.fillMaxWidth()) {
                Text("Unlock recurring rules")
            }
            Spacer(Modifier.height(16.dp))
            pt.ms.myshare.presentation.ui.ads.SafeAdBanner(isPremium = false)
        } else {
            MetricCard(title = "Premium active", value = "Recurring rules, reminders, and deeper review are unlocked.")
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier, supporting: String? = null) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            supporting?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HelperCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
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
                    nextPaydayLabel = "Next payday 2 Apr",
                    incomeLabel = "€1,500.00",
                    fixedCostsLabel = "€620.00",
                    flexibleSpendLabel = "€380.00",
                    savingsLabel = "€300.00",
                    investingLabel = "€120.00",
                    weeklySpendLabel = "€87.00",
                    summary = "A calm split that protects essentials and builds savings."
                ),
                goalCard = GoalCardState(
                    goalName = "Emergency fund",
                    goalAmountLabel = "€3,000.00",
                    targetDateLabel = "On pace for November 2026",
                    progressNote = "One goal is free."
                )
            ),
            onDestinationSelected = { _ -> },
            onFlexibleSpendChanged = { _ -> },
            onGoalContributionChanged = { _ -> },
            onSaveReview = {},
            onToggleReminder = { _ -> },
            onBillingPlanSelected = { _ -> },
            onUnlockPremium = {}
        )
    }
}
