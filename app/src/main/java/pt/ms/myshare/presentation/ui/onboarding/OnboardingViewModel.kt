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

    fun buildPreview(): Boolean {
        val current = state.value
        val income = current.netIncomePerPayday ?: return false
        val fixedCosts = current.monthlyFixedCosts ?: return false
        val plan = buildPlan(current, income, fixedCosts) ?: return false
        val preview = calculatePlanPreviewUseCase.execute(plan)
        state.update { it.copy(planPreview = preview, error = null) }
        viewModelScope.launch {
            plannerRepository.savePlan(plan)
            FirebaseUtils.logEvent("create_plan_completed", Bundle().apply {
                putString("country_cluster", current.pricingStrategy?.marketCluster)
                putString("language", Locale.getDefault().language)
            })
        }
        return true
    }

    fun setSelectedBillingPlan(plan: BillingPlan) {
        state.update { it.copy(selectedBillingPlan = plan) }
    }

    fun unlockPremium(onUnlocked: () -> Unit) {
        viewModelScope.launch {
            entitlementRepository.setPro(true)
            state.update { it.copy(isPremium = true) }
            FirebaseUtils.logEvent("trial_started", Bundle().apply {
                putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
                putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            })
            onUnlocked()
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

    fun completeOnboardingWithoutPremium() {
        viewModelScope.launch {
            plannerRepository.setOnboardingCompleted(true)
            state.update { it.copy(onboardingCompleted = true) }
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
            plannerRepository.setOnboardingCompleted(true)
            state.update { it.copy(onboardingCompleted = true) }
            FirebaseUtils.logEvent("reminder_enabled")
        }
    }

    fun skipReminderConfiguration() {
        viewModelScope.launch {
            plannerRepository.saveReminderConfiguration(ReminderConfiguration(enabled = false))
            plannerRepository.setOnboardingCompleted(true)
            state.update { it.copy(onboardingCompleted = true) }
        }
    }

    fun logPaywallViewed() {
        FirebaseUtils.logEvent("paywall_viewed", Bundle().apply {
            putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
        })
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
            goalAmount = current.goalAmount
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
