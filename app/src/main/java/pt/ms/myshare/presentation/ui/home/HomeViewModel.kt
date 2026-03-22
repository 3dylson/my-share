package pt.ms.myshare.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReviewInsight
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.CreateReviewInsightUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val entitlementRepository: EntitlementRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val createReviewInsightUseCase: CreateReviewInsightUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase
) : ViewModel() {

    private val uiState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = uiState.asStateFlow()

    private val locale: Locale = Locale.getDefault()
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    private val pricingStrategy = resolvePricingStrategyUseCase.execute(locale)

    init {
        observePlannerData()
        FirebaseUtils.logScreen("home")
    }

    private fun observePlannerData() {
        viewModelScope.launch {
            combine(
                plannerRepository.observePlan(),
                plannerRepository.observeLatestReview(),
                plannerRepository.observeReminderConfiguration(),
                entitlementRepository.isPro
            ) { plan, review, reminder, isPremium ->
                val emptyMessage = if (plan == null) "Build a salary plan first to unlock the repeat loop." else null
                val planCard = plan?.let { buildPlanCard(it) }
                val goalCard = plan?.let { buildGoalCard(it) }
                val reviewCard = buildReviewCard(plan, review)
                val moreCard = MoreCardState(
                    reminderEnabled = reminder.enabled,
                    reminderLabel = if (reminder.enabled) {
                        "${reminder.cadence.name.replace('_', ' ')} at ${reminder.hourOfDay.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}"
                    } else {
                        "Reminders are off"
                    },
                    pricingStrategy = pricingStrategy,
                    selectedBillingPlan = pricingStrategy.heroPlan,
                    isPremium = isPremium
                )
                HomeState(
                    selectedDestination = uiState.value.selectedDestination,
                    plan = plan,
                    planCard = planCard,
                    goalCard = goalCard,
                    reviewCard = reviewCard,
                    moreCard = moreCard,
                    isLoading = false,
                    emptyMessage = emptyMessage,
                    error = null
                )
            }.collect { newState ->
                uiState.value = newState
            }
        }
    }

    private fun buildPlanCard(plan: pt.ms.myshare.domain.model.SalaryPlan): HomePlanCardState {
        val preview = calculatePlanPreviewUseCase.execute(plan)
        return HomePlanCardState(
            nextPaydayLabel = "Next payday ${preview.nextPayday.format(dateFormatter)}",
            incomeLabel = currencyFormat.format(preview.incomePerPayday),
            fixedCostsLabel = currencyFormat.format(preview.fixedCostsPerPayday),
            flexibleSpendLabel = currencyFormat.format(preview.flexibleSpendPerPayday),
            savingsLabel = currencyFormat.format(preview.savingsPerPayday),
            investingLabel = currencyFormat.format(preview.investingPerPayday.add(preview.cryptoPerPayday)),
            weeklySpendLabel = currencyFormat.format(preview.weeklyFlexibleSpend),
            summary = preview.summary
        )
    }

    private fun buildGoalCard(plan: pt.ms.myshare.domain.model.SalaryPlan): GoalCardState {
        val preview = calculatePlanPreviewUseCase.execute(plan)
        val targetDateLabel = preview.goalTargetDate?.let { "On pace for ${it.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${it.year}" }
            ?: "Add more goal contribution to see a target date"
        return GoalCardState(
            goalName = plan.goalName,
            goalAmountLabel = currencyFormat.format(plan.goalAmount),
            targetDateLabel = targetDateLabel,
            progressNote = "One goal is free. Extra goals, recurring rules, and deeper reviews stay premium."
        )
    }

    private fun buildReviewCard(plan: pt.ms.myshare.domain.model.SalaryPlan?, review: ManualReview?): ReviewCardState {
        val insight = if (plan != null && review != null) {
            createReviewInsightUseCase.execute(plan, review)
        } else {
            null
        }
        return ReviewCardState(
            actualFlexibleSpend = review?.actualFlexibleSpend?.stripTrailingZeros()?.toPlainString().orEmpty(),
            actualGoalContribution = review?.actualGoalContribution?.stripTrailingZeros()?.toPlainString().orEmpty(),
            insight = insight,
            savedReviewDate = review?.createdAt?.format(dateFormatter)
        )
    }

    fun selectDestination(destination: HomeDestination) {
        uiState.update { it.copy(selectedDestination = destination) }
        FirebaseUtils.logEvent("home_tab_opened", android.os.Bundle().apply {
            putString("tab", destination.name.lowercase(Locale.US))
        })
    }

    fun onFlexibleSpendChanged(value: String) {
        uiState.update { it.copy(reviewCard = it.reviewCard.copy(actualFlexibleSpend = sanitizeNumber(value), error = null)) }
    }

    fun onGoalContributionChanged(value: String) {
        uiState.update { it.copy(reviewCard = it.reviewCard.copy(actualGoalContribution = sanitizeNumber(value), error = null)) }
    }

    fun saveReview() {
        val currentPlan = uiState.value.plan
        if (currentPlan == null) {
            uiState.update { it.copy(reviewCard = it.reviewCard.copy(error = "Create a plan before saving a review.")) }
            return
        }

        val actualFlexible = uiState.value.reviewCard.actualFlexibleSpend.toBigDecimalOrNull()
        val actualGoal = uiState.value.reviewCard.actualGoalContribution.toBigDecimalOrNull()
        if (actualFlexible == null || actualGoal == null) {
            uiState.update { it.copy(reviewCard = it.reviewCard.copy(error = "Enter valid amounts for both review fields.")) }
            return
        }

        viewModelScope.launch {
            val review = ManualReview(
                actualFlexibleSpend = actualFlexible,
                actualGoalContribution = actualGoal
            )
            plannerRepository.saveReview(review)
            FirebaseUtils.logEvent("weekly_checkin_completed")
            Timber.tag(TAG).d("Review saved flexible=%s goal=%s", actualFlexible, actualGoal)
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val current = plannerRepository.loadReminderConfiguration()
            plannerRepository.saveReminderConfiguration(current.copy(enabled = enabled))
            FirebaseUtils.logEvent(if (enabled) "reminder_enabled" else "reminder_disabled")
        }
    }

    fun chooseBillingPlan(plan: BillingPlan) {
        uiState.update { it.copy(moreCard = it.moreCard.copy(selectedBillingPlan = plan)) }
    }

    fun unlockPremium() {
        viewModelScope.launch {
            entitlementRepository.setPro(true)
            FirebaseUtils.logEvent("purchase_completed")
        }
    }

    private fun sanitizeNumber(value: String): String = value.filter { it.isDigit() || it == '.' }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
