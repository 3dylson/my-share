package pt.ms.myshare.presentation.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ms.myshare.domain.model.*
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import java.math.BigDecimal
import java.time.LocalTime
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val entitlementRepository: EntitlementRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    private var hasInteracted = false

    val isPro: StateFlow<Boolean> = entitlementRepository.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var basePreview: PlanPreview? = null

    init {
        // Decide whether to skip onboarding.
        val completed = OnboardingPrefs.isOnboardingCompleted(appContext)
        _state.update { it.copy(onboardingCompleted = completed) }
    }

    fun selectGoal(type: GoalType, amount: BigDecimal, label: String?) {
        _state.update { it.copy(selectedGoalType = type, goalAmount = amount, goalLabel = label) }
    }

    fun setPreset(preset: AllocationPreset) {
        _state.update { it.copy(preset = preset) }
        // If the user already saw a preview, keep it live-updated.
        if (_state.value.planPreview != null) {
            recomputePreview(sliderExtra = _state.value.sliderValue)
        }
    }

    fun enterSalaryAndSchedule(salary: BigDecimal, schedule: PaySchedule) {
        _state.update { it.copy(netSalary = salary, paySchedule = schedule) }
    }

    fun seePlan() {
        recomputePreview(sliderExtra = 0)
    }

    fun interact() {
        hasInteracted = true
    }

    fun onSliderChanged(extra: Int) {
        hasInteracted = true
        _state.update { it.copy(sliderValue = extra) }
        recomputePreview(sliderExtra = extra)
    }

    private fun recomputePreview(sliderExtra: Int) {
        val s = _state.value
        if (s.netSalary == null || s.paySchedule == null || s.goalAmount == null || s.selectedGoalType == null) return

        val effectiveSalary = s.netSalary.add(BigDecimal(sliderExtra))
        val input = PlanInput(
            netSalary = effectiveSalary,
            schedule = s.paySchedule,
            preset = s.preset,
            goal = Goal(amount = s.goalAmount, type = s.selectedGoalType, label = s.goalLabel)
        )
        val preview = calculatePlanPreviewUseCase.execute(input)
        val monthsSooner = calculateMonthsSooner(basePreview, preview)

        if (sliderExtra == 0) {
            basePreview = preview
        }

        _state.update { it.copy(planPreview = preview, monthsSooner = monthsSooner) }
        // Persist the latest plan so the reminder worker can use it.
        OnboardingPrefs.savePlanInput(appContext, input)
    }

    private fun calculateMonthsSooner(base: PlanPreview?, current: PlanPreview?): Int? {
        val b = base?.goalTargetDate ?: return null
        val c = current?.goalTargetDate ?: return null
        val diff = ChronoUnit.MONTHS.between(c.atDay(1), b.atDay(1)).toInt()
        return diff.takeIf { it > 0 }
    }

    fun setSelectedPaywallPlan(plan: PaywallPlan) {
        _state.update { it.copy(selectedPaywallPlan = plan) }
    }

    fun onAutopilotClicked(onShowPaywall: () -> Unit, onGoToReminderSetup: () -> Unit) {
        viewModelScope.launch {
            val pro = isPro.value
            val previewSeen = _state.value.planPreview != null
            if (pro) {
                onGoToReminderSetup()
            } else if (previewSeen) {
                onShowPaywall()
            }
        }
    }

    fun purchaseSelectedPlan(onPurchased: () -> Unit) {
        viewModelScope.launch {
            // Placeholder: unlock immediately.
            entitlementRepository.setPro(true)
            onPurchased()
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            entitlementRepository.restorePurchases()
        }
    }

    var reminderTime: LocalTime = LocalTime.of(9, 0)
    var reminderSchedule: String = "MONTHLY"

    fun setupReminder(time: LocalTime, schedule: String) {
        reminderTime = time
        reminderSchedule = schedule
        schedulePaydayReminder()
        OnboardingPrefs.setOnboardingCompleted(appContext, true)
        _state.update { it.copy(onboardingCompleted = true) }
    }

    fun completeOnboardingWithoutAutopilot() {
        OnboardingPrefs.setOnboardingCompleted(appContext, true)
        _state.update { it.copy(onboardingCompleted = true) }
    }

    private fun schedulePaydayReminder() {
        // Cancel previous jobs
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .addTag("payday_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork(
            /* uniqueWorkName = */ "payday_reminder",
            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.UPDATE,
            /* periodicWork = */ request
        )
    }

    fun updateReminderContent() {
        // Call schedulePaydayReminder() to update content if salary/preset/goal changes
        schedulePaydayReminder()
    }

    fun disableReminders() {
        workManager.cancelUniqueWork("payday_reminder")
    }
}
