package pt.ms.myshare.presentation.ui.onboarding

import pt.ms.myshare.domain.model.*
import java.math.BigDecimal

// Immutable onboarding state
 data class OnboardingState(
    val selectedGoalType: GoalType? = null,
    val goalAmount: BigDecimal? = null,
    val goalLabel: String? = null,
    val netSalary: BigDecimal? = null,
    val paySchedule: PaySchedule? = null,
    val preset: AllocationPreset = AllocationPreset.BALANCED,
    val planPreview: PlanPreview? = null,
    val sliderValue: Int = 0,
    val monthsSooner: Int? = null,
    val selectedPaywallPlan: PaywallPlan = PaywallPlan.Annual,
    val onboardingCompleted: Boolean = false,
    val event: OnboardingEvent? = null
)

sealed class OnboardingEvent {
    object NavigateNext : OnboardingEvent()
    object ShowPaywall : OnboardingEvent()
    object ClosePaywall : OnboardingEvent()
    object SkipOnboarding : OnboardingEvent()
}

