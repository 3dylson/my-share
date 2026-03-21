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

/**
 * Nested onboarding navigation.
 *
 * Flow:
 * Welcome -> Goal -> Salary/Schedule -> Plan Preview (aha) -> Paywall -> Reminder setup -> Home
 */
@Composable
fun OnboardingEntryRoute(parentNavController: NavController) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

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
                onSkip = {
                    viewModel.completeOnboardingWithoutAutopilot()
                }
            )
        }
        composable(OnboardingRoute.GoalPicker.route) {
            GoalPickerScreen(
                initialType = state.selectedGoalType,
                initialAmount = state.goalAmount,
                initialLabel = state.goalLabel,
                onBack = { navController.popBackStack() },
                onNext = { type, amount, label ->
                    viewModel.selectGoal(type, amount, label)
                    navController.navigate(OnboardingRoute.SalaryAndSchedule.route)
                }
            )
        }
        composable(OnboardingRoute.SalaryAndSchedule.route) {
            SalaryAndScheduleScreen(
                initialSalary = state.netSalary,
                initialSchedule = state.paySchedule,
                initialPreset = state.preset,
                onBack = { navController.popBackStack() },
                onPresetSelected = { viewModel.setPreset(it) },
                onSeePlan = { salary, schedule ->
                    viewModel.enterSalaryAndSchedule(salary, schedule)
                    viewModel.seePlan()
                    navController.navigate(OnboardingRoute.PlanPreview.route)
                }
            )
        }
        composable(OnboardingRoute.PlanPreview.route) {
            val isPro by viewModel.isPro.collectAsState()
            PlanPreviewScreen(
                planPreview = state.planPreview,
                goalAmount = state.goalAmount,
                currencySymbol = "€",
                onSliderChange = { viewModel.onSliderChanged(it) },
                sliderValue = state.sliderValue,
                monthsSooner = state.monthsSooner,
                onAutopilot = {
                    viewModel.onAutopilotClicked(
                        onShowPaywall = { navController.navigate(OnboardingRoute.Paywall.route) },
                        onGoToReminderSetup = { navController.navigate(OnboardingRoute.ReminderSetup.route) }
                    )
                },
                onNotNow = {
                    viewModel.completeOnboardingWithoutAutopilot()
                }
            )
        }
        composable(OnboardingRoute.Paywall.route) {
            PaywallScreen(
                annualPrice = "€49.99 / year",
                monthlyPrice = "€5.99 / month",
                trialAvailable = true,
                selectedPlan = state.selectedPaywallPlan,
                onPlanSelected = { viewModel.setSelectedPaywallPlan(it) },
                onClose = { navController.popBackStack() },
                onRestore = { viewModel.restorePurchases() },
                onPurchaseSelected = {
                    viewModel.purchaseSelectedPlan {
                        navController.navigate(OnboardingRoute.ReminderSetup.route) {
                            popUpTo(OnboardingRoute.PlanPreview.route) { inclusive = false }
                        }
                    }
                }
            )
        }
        composable(OnboardingRoute.ReminderSetup.route) {
            ReminderSetupScreen(
                onConfirm = { time, schedule ->
                    viewModel.setupReminder(time, schedule)
                }
            )
        }
    }
}
