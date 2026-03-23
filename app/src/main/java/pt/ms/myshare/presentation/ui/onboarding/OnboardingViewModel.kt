package pt.ms.myshare.presentation.ui.onboarding

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val entitlementRepository: EntitlementRepository,
    private val authRepository: AuthRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val state = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = state.asStateFlow()

    init {
        val completed = plannerRepository.isOnboardingCompleted()
        val pricing = resolvePricingStrategyUseCase.execute(Locale.getDefault())
        state.update {
            it.copy(
                onboardingCompleted = completed,
                pricingStrategy = pricing,
                selectedBillingPlan = pricing.heroPlan
            )
        }
        viewModelScope.launch {
            val isPremium = entitlementRepository.isPro.first()
            state.update { current -> current.copy(isPremium = isPremium) }
        }
    }

    fun setFocus(focus: PlanningFocus, defaultGoalName: String, defaultGoalAmount: BigDecimal) {
        state.update {
            it.copy(
                selectedFocus = focus,
                goalName = defaultGoalName,
                goalAmount = defaultGoalAmount
            )
        }
    }

    fun setGoal(goalName: String, goalAmount: BigDecimal) {
        state.update { it.copy(goalName = goalName, goalAmount = goalAmount) }
    }

    fun setSalaryDetails(
        incomePerPayday: BigDecimal,
        payFrequency: PayFrequency,
        monthlyPayday: Int,
        nextBiweeklyPaydayText: String
    ) {
        state.update {
            it.copy(
                netIncomePerPayday = incomePerPayday,
                payFrequency = payFrequency,
                monthlyPayday = monthlyPayday,
                nextBiweeklyPaydayText = nextBiweeklyPaydayText,
                error = null
            )
        }
    }

    fun setFixedCostsAndBuild(monthlyFixedCosts: BigDecimal, preset: AllocationPreset): Boolean {
        state.update {
            it.copy(
                monthlyFixedCosts = monthlyFixedCosts,
                preset = preset,
                error = null
            )
        }
        return buildPreview()
    }

    fun setAllocationsAndBuild(
        flexibleSpend: BigDecimal,
        savings: BigDecimal,
        investing: BigDecimal,
        crypto: BigDecimal
    ): Boolean {
        state.update {
            it.copy(
                allocatedFlexibleSpend = flexibleSpend,
                allocatedSavings = savings,
                allocatedInvesting = investing,
                allocatedCrypto = crypto
            )
        }
        return buildPreview()
    }

    fun buildPreview(): Boolean {
        val current = state.value
        val income = current.netIncomePerPayday ?: return false
        val fixedCosts = current.monthlyFixedCosts ?: return false
        val plan = buildPlan(current, income, fixedCosts) ?: return false
        val preview = calculatePlanPreviewUseCase.execute(plan)
        state.update { it.copy(planPreview = preview, error = null, planSaved = true) }
        viewModelScope.launch {
            plannerRepository.savePlan(plan)
            FirebaseUtils.logEvent("create_plan_completed", Bundle().apply {
                putString("country_cluster", current.pricingStrategy?.marketCluster)
                putString("language", Locale.getDefault().language)
            })
        }
        return true
    }

    fun setReminderSaved() {
        state.update { it.copy(reminderSaved = true) }
    }

    fun setBankSyncHandled() {
        state.update { it.copy(bankSyncHandled = true) }
    }

    fun setSelectedBillingPlan(plan: BillingPlan) {
        state.update { it.copy(selectedBillingPlan = plan) }
    }

    fun purchasePremium(activity: android.app.Activity) {
        val storeProductId = if (state.value.selectedBillingPlan == BillingPlan.ANNUAL) "myshare_annual" else "myshare_monthly"
        val product = pt.ms.myshare.domain.model.StoreProduct(storeProductId, "Premium", "Unlock premium features", "$0.00", "subs", null)
        
        viewModelScope.launch {
            FirebaseUtils.logEvent("purchase_started", Bundle().apply {
                putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
                putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            })
            entitlementRepository.purchasePlan(activity, product)
        }
    }


    fun signInWithGoogle(idToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    FirebaseUtils.logEvent("signup_completed")
                    plannerRepository.loadPlan()?.let { plan ->
                        plannerRepository.savePlan(plan)
                    }
                    onComplete()
                }
                .onFailure { error ->
                    Timber.tag(TAG).e(error, "Sign in failed")
                    state.update { it.copy(error = "Sign in failed. Please try again or continue locally.") }
                }
        }
    }

    fun skipSignIn(onComplete: () -> Unit) {
        FirebaseUtils.logEvent("signup_skipped")
        onComplete()
    }

    fun skipToHomeWithDefaultPlan() {
        viewModelScope.launch {
            val pricing = state.value.pricingStrategy
            state.update { 
                it.copy(
                    netIncomePerPayday = BigDecimal("1500"),
                    monthlyFixedCosts = BigDecimal("600"),
                    selectedFocus = PlanningFocus.SAVE_WITHOUT_STRESS,
                    preset = AllocationPreset.BALANCED,
                    planSaved = true,
                    reminderSkipped = true,
                    bankSyncHandled = true
                )
            }
            buildPreview()
            plannerRepository.setOnboardingCompleted(true)
            state.update { it.copy(onboardingCompleted = true) }
            FirebaseUtils.logEvent("onboarding_skipped_with_defaults")
        }
    }

    fun restorePurchases(onRestored: (Boolean) -> Unit) {
        viewModelScope.launch {
            entitlementRepository.restorePurchases()
            val restored = entitlementRepository.isPro.first()
            state.update { it.copy(isPremium = restored) }
            onRestored(restored)
        }
    }

    fun completeOnboarding() {
        val current = state.value
        if (!current.planSaved) {
            state.update { it.copy(error = "Please build your plan first.") }
            return
        }
        if (!current.reminderSaved && !current.reminderSkipped) {
            state.update { it.copy(error = "Please handle the reminder step.") }
            return
        }
        if (!current.bankSyncHandled) {
            state.update { it.copy(error = "Please complete or skip the bank sync step.") }
            return
        }

        viewModelScope.launch {
            if (plannerRepository.loadPlan() != null) {
                plannerRepository.setOnboardingCompleted(true)
                state.update { it.copy(onboardingCompleted = true) }
                FirebaseUtils.logEvent("onboarding_completed")
            } else {
                state.update { it.copy(error = "Cannot complete onboarding without a valid plan.") }
            }
        }
    }

    fun saveReminderConfiguration(time: LocalTime, cadence: ReminderCadence) {
        viewModelScope.launch {
            plannerRepository.saveReminderConfiguration(
                ReminderConfiguration(
                    enabled = true,
                    hourOfDay = time.hour,
                    minute = time.minute,
                    cadence = cadence
                )
            )
            scheduleReminderWork()
            state.update { it.copy(reminderSaved = true, reminderSkipped = false) }
            FirebaseUtils.logEvent("reminder_enabled")
        }
    }

    fun skipReminderConfiguration() {
        viewModelScope.launch {
            plannerRepository.saveReminderConfiguration(ReminderConfiguration(enabled = false))
            state.update { it.copy(reminderSaved = false, reminderSkipped = true) }
            FirebaseUtils.logEvent("reminder_skipped")
        }
    }

    fun logPaywallViewed() {
        FirebaseUtils.logEvent("paywall_viewed", Bundle().apply {
            putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
        })
    }

    fun logSignupStarted() {
        FirebaseUtils.logEvent("signup_started")
    }

    fun logTrajectoryViewed() {
        FirebaseUtils.logEvent("trajectory_viewed")
    }

    fun logBankSyncPromptShown() {
        FirebaseUtils.logEvent("bank_sync_prompt_shown")
    }

    fun logBankSyncSkipped() {
        FirebaseUtils.logEvent("bank_sync_skipped")
    }

    private fun buildPlan(current: OnboardingState, income: BigDecimal, fixedCosts: BigDecimal): SalaryPlan? {
        val nextBiweeklyPayday = if (current.payFrequency == PayFrequency.BIWEEKLY) {
            runCatching { LocalDate.parse(current.nextBiweeklyPaydayText) }.getOrElse {
                state.update { it.copy(error = "Use YYYY-MM-DD for the next biweekly payday.") }
                return null
            }
        } else {
            null
        }
        return SalaryPlan(
            focus = current.selectedFocus,
            netIncomePerPayday = income,
            monthlyFixedCosts = fixedCosts,
            payFrequency = current.payFrequency,
            monthlyPayday = current.monthlyPayday.coerceIn(1, 28),
            nextBiweeklyPayday = nextBiweeklyPayday,
            preset = current.preset,
            goalName = current.goalName,
            goalAmount = current.goalAmount,
            flexibleSpend = current.allocatedFlexibleSpend,
            savings = current.allocatedSavings,
            investing = current.allocatedInvesting,
            crypto = current.allocatedCrypto
        )
    }

    private fun scheduleReminderWork() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .addTag(ReminderWorker.UNIQUE_NAME)
            .build()
        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Timber.tag(TAG).d("Reminder work scheduled")
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}
