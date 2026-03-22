package pt.ms.myshare.presentation.ui.onboarding

import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PricingStrategy
import java.math.BigDecimal

data class OnboardingState(
    val selectedFocus: PlanningFocus = PlanningFocus.SAVE_WITHOUT_STRESS,
    val goalName: String = "Emergency fund",
    val goalAmount: BigDecimal = BigDecimal("3000"),
    val netIncomePerPayday: BigDecimal? = null,
    val monthlyFixedCosts: BigDecimal? = null,
    val payFrequency: PayFrequency = PayFrequency.MONTHLY,
    val monthlyPayday: Int = 1,
    val nextBiweeklyPaydayText: String = java.time.LocalDate.now().plusDays(14).toString(),
    val preset: AllocationPreset = AllocationPreset.BALANCED,
    val planPreview: PlanPreview? = null,
    val pricingStrategy: PricingStrategy? = null,
    val selectedBillingPlan: BillingPlan = BillingPlan.MONTHLY,
    val onboardingCompleted: Boolean = false,
    val isPremium: Boolean = false,
    val error: String? = null
)
