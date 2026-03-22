package pt.ms.myshare.presentation.ui.onboarding

sealed class OnboardingRoute(val route: String) {
    object Welcome : OnboardingRoute("welcome")
    object GoalPicker : OnboardingRoute("goal_picker")
    object SalaryAndSchedule : OnboardingRoute("salary_and_schedule")
    object FixedCosts : OnboardingRoute("fixed_costs")
    object PlanPreview : OnboardingRoute("plan_preview")
    object Paywall : OnboardingRoute("paywall")
    object ReminderSetup : OnboardingRoute("reminder_setup")
}

