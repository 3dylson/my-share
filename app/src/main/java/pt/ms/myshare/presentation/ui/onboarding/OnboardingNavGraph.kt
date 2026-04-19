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
                onContinue = { navController.navigate(OnboardingRoute.GoalPicker.route) }
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
                error = state.error,
                onBack = { navController.popBackStack() },
                onNext = { fixedCosts, preset ->
                    if (viewModel.setFixedCostsAndBuild(fixedCosts, preset)) {
                        navController.navigate(OnboardingRoute.AllocationPriorities.route)
                    }
                }
            )
        }
        composable(OnboardingRoute.AllocationPriorities.route) {
            val preview = state.planPreview
            if (preview != null) {
                val totalAvailable = preview.incomePerPayday - preview.fixedCostsPerPayday - preview.debtPerPayday
                AllocationPrioritiesScreen(
                    initialFlexibleSpend = preview.flexibleSpendPerPayday,
                    initialSavings = preview.savingsPerPayday,
                    initialInvesting = preview.investingPerPayday,
                    initialCrypto = preview.cryptoPerPayday,
                    totalAvailable = totalAvailable,
                    onBack = { navController.popBackStack() },
                    onNext = { flex, sav, inv, cry ->
                        if (viewModel.setAllocationsAndBuild(flex, sav, inv, cry)) {
                            navController.navigate(OnboardingRoute.BuildingPlan.route)
                        }
                    }
                )
            } else {
                // Should not happen organically in the sequence as FixedCosts guarantees preview
                navController.popBackStack()
            }
        }
        composable(OnboardingRoute.BuildingPlan.route) {
            BuildingPlanScreen(
                onBuilt = {
                    navController.navigate(OnboardingRoute.PlanPreview.route) {
                        popUpTo(OnboardingRoute.AllocationPriorities.route) { inclusive = true }
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
                        navController.navigate(OnboardingRoute.Signup.route)
                    },
                    onNotNow = { navController.navigate(OnboardingRoute.Signup.route) }
                )
            }
        }
        composable(OnboardingRoute.Signup.route) {
            LaunchedEffect(Unit) { viewModel.logSignupStarted() }
            SignupScreen(
                onSignup = { idToken ->
                    viewModel.signInWithGoogle(idToken) {
                        navController.navigate(OnboardingRoute.Trajectory.route)
                    }
                }
            )
        }
        composable(OnboardingRoute.Trajectory.route) {
            LaunchedEffect(Unit) { viewModel.logTrajectoryViewed() }
            TrajectoryScreen(
                preview = state.planPreview,
                goalName = state.goalName,
                onNext = {
                    viewModel.logPaywallViewed()
                    navController.navigate(OnboardingRoute.Paywall.route)
                }
            )
        }
        composable(OnboardingRoute.Paywall.route) {
            val pricing = state.pricingStrategy
            val isPremium = state.isPremium
            
            LaunchedEffect(isPremium) {
                if (isPremium) {
                    navController.navigate(OnboardingRoute.ReminderSetup.route)
                }
            }

            if (pricing != null) {
                PaywallScreen(
                    pricingStrategy = pricing,
                    selectedPlan = state.selectedBillingPlan,
                    onPlanSelected = viewModel::setSelectedBillingPlan,
                    onClose = { navController.navigate(OnboardingRoute.ReminderSetup.route) },
                    onRestore = {
                        viewModel.restorePurchases { restored ->
                            if (restored) {
                                navController.navigate(OnboardingRoute.ReminderSetup.route)
                            }
                        }
                    },
                    onPurchaseSelected = { activity -> viewModel.purchasePremium(activity) }
                )
            }
        }
        composable(OnboardingRoute.ReminderSetup.route) {
            ReminderSetupScreen(
                onConfirm = { time, cadence ->
                    viewModel.saveReminderConfiguration(time, cadence)
                    navController.navigate(OnboardingRoute.BankSyncOptional.route)
                },
                onSkip = {
                    viewModel.skipReminderConfiguration()
                    navController.navigate(OnboardingRoute.BankSyncOptional.route)
                }
            )
        }
        composable(OnboardingRoute.BankSyncOptional.route) {
            LaunchedEffect(Unit) { viewModel.logBankSyncPromptShown() }
            BankSyncOptionalScreen(
                onSync = { 
                    viewModel.setBankSyncHandled()
                    viewModel.completeOnboarding() 
                },
                onSkip = { 
                    viewModel.logBankSyncSkipped()
                    viewModel.setBankSyncHandled()
                    viewModel.completeOnboarding() 
                }
            )
        }
    }
}
