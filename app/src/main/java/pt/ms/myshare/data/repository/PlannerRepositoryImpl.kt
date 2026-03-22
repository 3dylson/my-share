package pt.ms.myshare.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlannerRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    private val planState = MutableStateFlow(readPlan())
    private val reviewState = MutableStateFlow(readLatestReview())
    private val reminderState = MutableStateFlow(readReminderConfiguration())

    override fun observePlan(): Flow<SalaryPlan?> = planState.asStateFlow()

    override fun loadPlan(): SalaryPlan? = planState.value

    override suspend fun savePlan(plan: SalaryPlan) {
        Timber.tag(TAG).d("savePlan focus=%s income=%s cadence=%s", plan.focus, plan.netIncomePerPayday, plan.payFrequency)
        prefs.edit()
            .putString(KEY_FOCUS, plan.focus.name)
            .putString(KEY_NET_INCOME, plan.netIncomePerPayday.toPlainString())
            .putString(KEY_MONTHLY_FIXED_COSTS, plan.monthlyFixedCosts.toPlainString())
            .putString(KEY_PAY_FREQUENCY, plan.payFrequency.name)
            .putInt(KEY_MONTHLY_PAYDAY, plan.monthlyPayday ?: 1)
            .putLong(KEY_BIWEEKLY_PAYDAY_EPOCH, plan.nextBiweeklyPayday?.toEpochDay() ?: NO_EPOCH)
            .putString(KEY_PRESET, plan.preset.name)
            .putString(KEY_GOAL_NAME, plan.goalName)
            .putString(KEY_GOAL_AMOUNT, plan.goalAmount.toPlainString())
            .putLong(KEY_PLAN_CREATED_AT_EPOCH, plan.createdAt.toEpochDay())
            .apply()
        planState.value = plan
    }

    override suspend fun clearPlan() {
        Timber.tag(TAG).d("clearPlan")
        prefs.edit()
            .remove(KEY_FOCUS)
            .remove(KEY_NET_INCOME)
            .remove(KEY_MONTHLY_FIXED_COSTS)
            .remove(KEY_PAY_FREQUENCY)
            .remove(KEY_MONTHLY_PAYDAY)
            .remove(KEY_BIWEEKLY_PAYDAY_EPOCH)
            .remove(KEY_PRESET)
            .remove(KEY_GOAL_NAME)
            .remove(KEY_GOAL_AMOUNT)
            .remove(KEY_PLAN_CREATED_AT_EPOCH)
            .apply()
        planState.value = null
    }

    override fun observeLatestReview(): Flow<ManualReview?> = reviewState.asStateFlow()

    override fun loadLatestReview(): ManualReview? = reviewState.value

    override suspend fun saveReview(review: ManualReview) {
        Timber.tag(TAG).d("saveReview actualFlexible=%s actualGoal=%s", review.actualFlexibleSpend, review.actualGoalContribution)
        prefs.edit()
            .putString(KEY_REVIEW_FLEXIBLE_SPEND, review.actualFlexibleSpend.toPlainString())
            .putString(KEY_REVIEW_GOAL_CONTRIBUTION, review.actualGoalContribution.toPlainString())
            .putLong(KEY_REVIEW_CREATED_AT_EPOCH, review.createdAt.toEpochDay())
            .apply()
        reviewState.value = review
    }

    override fun observeReminderConfiguration(): Flow<ReminderConfiguration> = reminderState.asStateFlow()

    override fun loadReminderConfiguration(): ReminderConfiguration = reminderState.value

    override suspend fun saveReminderConfiguration(configuration: ReminderConfiguration) {
        Timber.tag(TAG).d(
            "saveReminderConfiguration enabled=%s hour=%s minute=%s cadence=%s",
            configuration.enabled,
            configuration.hourOfDay,
            configuration.minute,
            configuration.cadence
        )
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, configuration.enabled)
            .putInt(KEY_REMINDER_HOUR, configuration.hourOfDay)
            .putInt(KEY_REMINDER_MINUTE, configuration.minute)
            .putString(KEY_REMINDER_CADENCE, configuration.cadence.name)
            .apply()
        reminderState.value = configuration
    }

    override fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        Timber.tag(TAG).d("setOnboardingCompleted completed=%s", completed)
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    private fun readPlan(): SalaryPlan? {
        val focus = prefs.getString(KEY_FOCUS, null)?.let { runCatching { PlanningFocus.valueOf(it) }.getOrNull() } ?: return null
        val income = prefs.getString(KEY_NET_INCOME, null)?.toBigDecimalOrNull() ?: return null
        val fixedCosts = prefs.getString(KEY_MONTHLY_FIXED_COSTS, null)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val payFrequency = prefs.getString(KEY_PAY_FREQUENCY, null)?.let { runCatching { PayFrequency.valueOf(it) }.getOrNull() } ?: PayFrequency.MONTHLY
        val preset = prefs.getString(KEY_PRESET, null)?.let { runCatching { AllocationPreset.valueOf(it) }.getOrNull() } ?: AllocationPreset.BALANCED
        val goalName = prefs.getString(KEY_GOAL_NAME, null).orEmpty().ifBlank { "Emergency fund" }
        val goalAmount = prefs.getString(KEY_GOAL_AMOUNT, null)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val createdAtEpoch = prefs.getLong(KEY_PLAN_CREATED_AT_EPOCH, LocalDate.now().toEpochDay())
        val monthlyPayday = prefs.getInt(KEY_MONTHLY_PAYDAY, 1).coerceIn(1, 31)
        val biweeklyEpoch = prefs.getLong(KEY_BIWEEKLY_PAYDAY_EPOCH, NO_EPOCH)
        return SalaryPlan(
            focus = focus,
            netIncomePerPayday = income,
            monthlyFixedCosts = fixedCosts,
            payFrequency = payFrequency,
            monthlyPayday = monthlyPayday,
            nextBiweeklyPayday = biweeklyEpoch.takeIf { it != NO_EPOCH }?.let(LocalDate::ofEpochDay),
            preset = preset,
            goalName = goalName,
            goalAmount = goalAmount,
            createdAt = LocalDate.ofEpochDay(createdAtEpoch)
        )
    }

    private fun readLatestReview(): ManualReview? {
        val actualFlexible = prefs.getString(KEY_REVIEW_FLEXIBLE_SPEND, null)?.toBigDecimalOrNull() ?: return null
        val actualGoal = prefs.getString(KEY_REVIEW_GOAL_CONTRIBUTION, null)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val createdAt = LocalDate.ofEpochDay(prefs.getLong(KEY_REVIEW_CREATED_AT_EPOCH, LocalDate.now().toEpochDay()))
        return ManualReview(
            actualFlexibleSpend = actualFlexible,
            actualGoalContribution = actualGoal,
            createdAt = createdAt
        )
    }

    private fun readReminderConfiguration(): ReminderConfiguration {
        val cadence = prefs.getString(KEY_REMINDER_CADENCE, ReminderCadence.PAYDAY.name)
            ?.let { runCatching { ReminderCadence.valueOf(it) }.getOrNull() }
            ?: ReminderCadence.PAYDAY
        return ReminderConfiguration(
            enabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false),
            hourOfDay = prefs.getInt(KEY_REMINDER_HOUR, 9),
            minute = prefs.getInt(KEY_REMINDER_MINUTE, 0),
            cadence = cadence
        )
    }

    private companion object {
        const val TAG = "PlannerRepository"
        const val NO_EPOCH = Long.MIN_VALUE
        const val KEY_ONBOARDING_COMPLETED = "planner_onboarding_completed"
        const val KEY_FOCUS = "planner_focus"
        const val KEY_NET_INCOME = "planner_net_income"
        const val KEY_MONTHLY_FIXED_COSTS = "planner_monthly_fixed_costs"
        const val KEY_PAY_FREQUENCY = "planner_pay_frequency"
        const val KEY_MONTHLY_PAYDAY = "planner_monthly_payday"
        const val KEY_BIWEEKLY_PAYDAY_EPOCH = "planner_biweekly_payday_epoch"
        const val KEY_PRESET = "planner_preset"
        const val KEY_GOAL_NAME = "planner_goal_name"
        const val KEY_GOAL_AMOUNT = "planner_goal_amount"
        const val KEY_PLAN_CREATED_AT_EPOCH = "planner_plan_created_at_epoch"
        const val KEY_REVIEW_FLEXIBLE_SPEND = "planner_review_flexible_spend"
        const val KEY_REVIEW_GOAL_CONTRIBUTION = "planner_review_goal_contribution"
        const val KEY_REVIEW_CREATED_AT_EPOCH = "planner_review_created_at_epoch"
        const val KEY_REMINDER_ENABLED = "planner_reminder_enabled"
        const val KEY_REMINDER_HOUR = "planner_reminder_hour"
        const val KEY_REMINDER_MINUTE = "planner_reminder_minute"
        const val KEY_REMINDER_CADENCE = "planner_reminder_cadence"
    }
}
