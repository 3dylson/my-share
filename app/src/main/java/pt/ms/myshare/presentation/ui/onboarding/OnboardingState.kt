package pt.ms.myshare.presentation.ui.onboarding

import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.model.UserPreferences
import java.math.BigDecimal

data class OnboardingState(
    val onboardingGoalId: String = java.util.UUID.randomUUID().toString(),
    val selectedFocus: PlanningFocus = PlanningFocus.SAVE_WITHOUT_STRESS,
    val goalName: String = "",
    val goalAmount: BigDecimal = BigDecimal("3000"),
    val netIncomePerPayday: BigDecimal? = null,
    val monthlyFixedCosts: BigDecimal? = null,
    val payFrequency: PayFrequency = PayFrequency.MONTHLY,
    val monthlyPayday: Int = 1,
    val nextBiweeklyPaydayText: String = java.time.LocalDate.now().plusDays(14).toString(),
    val preset: AllocationPreset = AllocationPreset.BALANCED,
    val strategy: AllocationStrategy = AllocationStrategy.BALANCED_SAVINGS,
    val customStrategyName: String = "",
    val allocatedFlexibleSpend: BigDecimal? = null,
    val allocatedSavings: BigDecimal? = null,
    val allocatedInvesting: BigDecimal? = null,
    val allocatedCrypto: BigDecimal? = null,
    val allocatedDebt: BigDecimal? = null,
    val allocationIsPercentage: Boolean = true,
    val planPreview: PlanPreview? = null,
    val userPreferences: UserPreferences = UserPreferences.defaults(),
    val pricingStrategy: PricingStrategy? = null,
    // Live prices fetched from Google Play Billing; empty until billing client connects
    val availableProducts: List<StoreProduct> = emptyList(),
    val selectedBillingPlan: BillingPlan = BillingPlan.MONTHLY,
    val isBillingActionInProgress: Boolean = false,
    val billingMessage: String? = null,
    val planSaved: Boolean = false,
    val reminderSaved: Boolean = false,
    val reminderSkipped: Boolean = false,
    val bankSyncHandled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val isPremium: Boolean = false,
    val isAnonymousUser: Boolean = false,
    val shouldSecurePremiumAccess: Boolean = false,
    val isGoogleConnectionInProgress: Boolean = false,
    val googleConnectionMessage: String? = null,
    val googleConnectionError: String? = null,
    val error: String? = null
)
