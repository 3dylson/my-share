package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ms.myshare.BuildConfig
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
                userPreferences = state.userPreferences,
                onLanguageSelected = viewModel::updateLanguage,
                onCurrencySelected = viewModel::updateCurrency,
                onContinue = { navController.navigate(OnboardingRoute.GoalPicker.route) },
                // Only wire the dev skip in debug builds; null means the button is hidden
                onSkipDev = if (BuildConfig.DEBUG) viewModel::skipToHomeWithDefaultPlan else null
            )
        }
        composable(OnboardingRoute.GoalPicker.route) {
            LaunchedEffect(Unit) { viewModel.logSetupStepViewed(OnboardingRoute.GoalPicker, 1) }
            GoalPickerScreen(
                initialFocus = state.selectedFocus,
                initialGoalName = state.goalName,
                initialGoalAmount = state.goalAmount,
                userPreferences = state.userPreferences,
                onBack = { navController.popBackStack() },
                onNext = { focus, goalName, goalAmount ->
                    viewModel.setFocus(focus, goalName, goalAmount)
                    viewModel.setGoal(goalName, goalAmount)
                    viewModel.logSetupStepCompleted(OnboardingRoute.GoalPicker, 1)
                    navController.navigate(OnboardingRoute.SalaryAndSchedule.route)
                }
            )
        }
        composable(OnboardingRoute.SalaryAndSchedule.route) {
            LaunchedEffect(Unit) { viewModel.logSetupStepViewed(OnboardingRoute.SalaryAndSchedule, 2) }
            SalaryAndScheduleScreen(
                initialIncome = state.netIncomePerPayday,
                initialFrequency = state.payFrequency,
                initialMonthlyPayday = state.monthlyPayday,
                initialNextBiweeklyPaydayText = state.nextBiweeklyPaydayText,
                userPreferences = state.userPreferences,
                onBack = { navController.popBackStack() },
                onNext = { income, frequency, monthlyPayday, biweeklyPayday ->
                    viewModel.setSalaryDetails(
                        incomePerPayday = income,
                        payFrequency = frequency,
                        monthlyPayday = monthlyPayday,
                        nextBiweeklyPaydayText = biweeklyPayday
                    )
                    viewModel.logSetupStepCompleted(OnboardingRoute.SalaryAndSchedule, 2)
                    navController.navigate(OnboardingRoute.FixedCosts.route)
                }
            )
        }
        composable(OnboardingRoute.FixedCosts.route) {
            LaunchedEffect(Unit) { viewModel.logSetupStepViewed(OnboardingRoute.FixedCosts, 3) }
            FixedCostsScreen(
                initialFixedCosts = state.monthlyFixedCosts,
                incomePerPayday = state.netIncomePerPayday,
                initialPreset = state.preset,
                initialStrategy = state.strategy,
                initialCustomStrategyName = state.customStrategyName,
                userPreferences = state.userPreferences,
                error = state.error,
                onBack = { navController.popBackStack() },
                onNext = { fixedCosts, preset, strategy, customStrategyName ->
                    if (viewModel.setFixedCostsAndBuild(fixedCosts, preset, strategy, customStrategyName)) {
                        viewModel.logSetupStepCompleted(OnboardingRoute.FixedCosts, 3)
                        navController.navigate(OnboardingRoute.PlanPreview.route)
                    }
                }
            )
        }
        composable(OnboardingRoute.AllocationPriorities.route) {
            val preview = state.planPreview
            if (preview != null) {
                val totalAvailable = preview.incomePerPayday - preview.fixedCostsPerPayday
                AllocationPrioritiesScreen(
                    initialFlexibleSpend = preview.flexibleSpendPerPayday,
                    initialSavings = preview.savingsPerPayday,
                    initialInvesting = preview.investingPerPayday,
                    initialCrypto = preview.cryptoPerPayday,
                    initialDebt = preview.debtPerPayday,
                    totalAvailable = totalAvailable,
                    initialAllocationIsPercentage = state.allocationIsPercentage,
                    userPreferences = state.userPreferences,
                    onBack = { navController.popBackStack() },
                    onNext = { flex, sav, inv, cry, debt, isPercentage ->
                        if (viewModel.setAllocationsAndBuild(flex, sav, inv, cry, debt, isPercentage)) {
                            viewModel.logAllocationTuneCompleted()
                            if (!navController.popBackStack(OnboardingRoute.PlanPreview.route, inclusive = false)) {
                                navController.navigate(OnboardingRoute.PlanPreview.route)
                            }
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
                LaunchedEffect(Unit) {
                    viewModel.logSetupStepViewed(OnboardingRoute.PlanPreview, 4)
                    viewModel.logActivationReached()
                }
                PlanPreviewScreen(
                    preview = preview,
                    goalName = state.goalName,
                    goalAmount = state.goalAmount,
                    userPreferences = state.userPreferences,
                    onTuneAllocation = {
                        viewModel.logAllocationTuneStarted()
                        navController.navigate(OnboardingRoute.AllocationPriorities.route)
                    },
                    onContinue = {
                        viewModel.logSetupStepCompleted(OnboardingRoute.PlanPreview, 4)
                        navController.navigate(OnboardingRoute.Signup.route)
                    }
                )
            }
        }
        composable(OnboardingRoute.Signup.route) {
            LaunchedEffect(Unit) { viewModel.logSignupStarted() }
            SignupScreen(
                isSignupActionInProgress = state.isSignupActionInProgress,
                onSignup = { idToken ->
                    viewModel.signInWithGoogle(idToken) {
                        navController.navigate(OnboardingRoute.Trajectory.route)
                    }
                },
                onContinueLocally = {
                    viewModel.continueLocally {
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
                userPreferences = state.userPreferences,
                onNext = {
                    viewModel.logPaywallViewed()
                    navController.navigate(OnboardingRoute.Paywall.route)
                }
            )
        }
        composable(OnboardingRoute.Paywall.route) {
            val pricing = state.pricingStrategy
            val isPremium = state.isPremium
            
            LaunchedEffect(isPremium, state.shouldSecurePremiumAccess) {
                if (isPremium && !state.shouldSecurePremiumAccess) {
                    navController.navigate(OnboardingRoute.ReminderSetup.route)
                }
            }

            if (pricing != null) {
                PaywallScreen(
                    pricingStrategy = pricing,
                    userPreferences = state.userPreferences,
                    planPreview = state.planPreview,
                    goalName = state.goalName,
                    availableProducts = state.availableProducts,
                    selectedPlan = state.selectedBillingPlan,
                    isBillingActionInProgress = state.isBillingActionInProgress,
                    billingMessage = state.billingMessage,
                    showSecurePremiumAccessPrompt = state.shouldSecurePremiumAccess,
                    isGoogleConnectionInProgress = state.isGoogleConnectionInProgress,
                    googleConnectionMessage = state.googleConnectionMessage,
                    googleConnectionError = state.googleConnectionError,
                    onPlanSelected = viewModel::setSelectedBillingPlan,
                    onClose = { navController.navigate(OnboardingRoute.ReminderSetup.route) },
                    onRestore = {
                        viewModel.restorePurchases { restored ->
                            if (restored) {
                                navController.navigate(OnboardingRoute.ReminderSetup.route)
                            }
                        }
                    },
                    onPurchaseSelected = { activity -> viewModel.purchasePremium(activity) },
                    onConnectGoogleAccount = viewModel::connectGoogleAccount,
                    onGoogleConnectionCredentialError = viewModel::setGoogleConnectionCredentialError,
                    onContinueWithoutSecuring = viewModel::dismissSecurePremiumAccessPrompt
                )
            }

        }
        composable(OnboardingRoute.ReminderSetup.route) {
            ReminderSetupScreen(
                onPermissionResult = viewModel::logReminderPermissionResult,
                onConfirm = { time, cadence ->
                    viewModel.saveReminderConfiguration(time, cadence) {
                        viewModel.completeOnboarding()
                    }
                },
                onSkip = {
                    viewModel.skipReminderConfiguration {
                        viewModel.completeOnboarding()
                    }
                }
            )
        }
    }
}
