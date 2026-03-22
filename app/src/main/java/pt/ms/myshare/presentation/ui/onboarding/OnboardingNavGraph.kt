package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun OnboardingEntryRoute(parentNavController: NavController) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.onboardingCompleted) {
        if (state.onboardingCompleted) {
            parentNavController.navigate("home") {
                popUpTo("onboarding") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (state.onboardingCompleted) return

    NavHost(navController = navController, startDestination = OnboardingRoute.Welcome.route) {
        composable(OnboardingRoute.Welcome.route) {
            WelcomeScreen(
                onContinue = { navController.navigate(OnboardingRoute.GoalPicker.route) },
                onSkip = { viewModel.completeOnboardingWithoutPremium() }
            )
        }
        composable(OnboardingRoute.GoalPicker.route) {
            GoalPickerScreen(
                initialFocus = state.selectedFocus,
                initialGoalName = state.goalName,
                initialGoalAmount = state.goalAmount,
                onBack = { navController.popBackStack() },
                onNext = { focus, goalName, goalAmount ->
                    viewModel.setFocus(focus, goalName, goalAmount)
                    viewModel.setGoal(goalName, goalAmount)
                    navController.navigate(OnboardingRoute.SalaryAndSchedule.route)
                }
            )
        }
        composable(OnboardingRoute.SalaryAndSchedule.route) {
            SalaryAndScheduleScreen(
                initialIncome = state.netIncomePerPayday,
                initialFrequency = state.payFrequency,
                initialMonthlyPayday = state.monthlyPayday,
                initialNextBiweeklyPaydayText = state.nextBiweeklyPaydayText,
                onBack = { navController.popBackStack() },
                onNext = { income, frequency, monthlyPayday, biweeklyPayday ->
                    viewModel.setSalaryDetails(
                        incomePerPayday = income,
                        payFrequency = frequency,
                        monthlyPayday = monthlyPayday,
                        nextBiweeklyPaydayText = biweeklyPayday
                    )
                    navController.navigate(OnboardingRoute.FixedCosts.route)
                }
            )
        }
        composable(OnboardingRoute.FixedCosts.route) {
            FixedCostsScreen(
                initialFixedCosts = state.monthlyFixedCosts,
                initialPreset = state.preset,
                onBack = { navController.popBackStack() },
                onNext = { fixedCosts, preset ->
                    if (viewModel.setFixedCostsAndBuild(fixedCosts, preset)) {
                        navController.navigate(OnboardingRoute.PlanPreview.route)
                    }
                }
            )
        }
        composable(OnboardingRoute.PlanPreview.route) {
            val preview = state.planPreview
            if (preview != null) {
                PlanPreviewScreen(
                    preview = preview,
                    goalName = state.goalName,
                    goalAmount = state.goalAmount,
                    onAutopilot = {
                        viewModel.logPaywallViewed()
                        navController.navigate(OnboardingRoute.Paywall.route)
                    },
                    onNotNow = { navController.navigate(OnboardingRoute.ReminderSetup.route) }
                )
            }
        }
        composable(OnboardingRoute.Paywall.route) {
            val pricing = state.pricingStrategy
            if (pricing != null) {
                PaywallScreen(
                    pricingStrategy = pricing,
                    selectedPlan = state.selectedBillingPlan,
                    onPlanSelected = viewModel::setSelectedBillingPlan,
                    onClose = { navController.popBackStack() },
                    onRestore = {
                        viewModel.restorePurchases { restored ->
                            if (restored) {
                                navController.navigate(OnboardingRoute.ReminderSetup.route)
                            }
                        }
                    },
                    onPurchaseSelected = { viewModel.unlockPremium { navController.navigate(OnboardingRoute.ReminderSetup.route) } }
                )
            }
        }
        composable(OnboardingRoute.ReminderSetup.route) {
            ReminderSetupScreen(
                onConfirm = { time, cadence -> viewModel.saveReminderConfiguration(time, cadence) },
                onSkip = { viewModel.skipReminderConfiguration() }
            )
        }
    }
}
