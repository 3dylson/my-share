package pt.ms.myshare.presentation.ui.onboarding

import android.content.Context
import androidx.preference.PreferenceManager
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoalType
import pt.ms.myshare.domain.model.PaySchedule
import pt.ms.myshare.domain.model.PlanInput
import java.math.BigDecimal
import java.time.LocalDate
import androidx.core.content.edit

/**
 * Lightweight SharedPreferences storage for onboarding + autopilot reminder inputs.
 *
 * This is intentionally simple (no extra dependencies) and is safe to replace later
 * with DataStore / Room without changing the rest of the feature.
 */
object OnboardingPrefs {

    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_GOAL_TYPE = "goal_type"
    private const val KEY_GOAL_AMOUNT = "goal_amount"
    private const val KEY_GOAL_LABEL = "goal_label"
    private const val KEY_NET_SALARY = "net_salary"
    private const val KEY_PRESET = "allocation_preset"
    private const val KEY_SCHEDULE_TYPE = "schedule_type" // monthly | biweekly
    private const val KEY_SCHEDULE_DAY_OF_MONTH = "schedule_day_of_month"
    private const val KEY_SCHEDULE_NEXT_PAYDAY_EPOCH = "schedule_next_payday_epoch"

    private const val SCHEDULE_MONTHLY = "monthly"
    private const val SCHEDULE_BIWEEKLY = "biweekly"

    fun isOnboardingCompleted(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        prefs(context).edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }

    fun savePlanInput(context: Context, input: PlanInput) {
        prefs(context).edit {
            putString(KEY_GOAL_TYPE, input.goal.type.name)
            putString(KEY_GOAL_AMOUNT, input.goal.amount.toPlainString())
            putString(KEY_GOAL_LABEL, input.goal.label ?: "")
            putString(KEY_NET_SALARY, input.netSalary.toPlainString())
            putString(KEY_PRESET, input.preset.name)
            when (val schedule = input.schedule) {
                is PaySchedule.Monthly -> {
                    putString(KEY_SCHEDULE_TYPE, SCHEDULE_MONTHLY)
                    putInt(KEY_SCHEDULE_DAY_OF_MONTH, schedule.dayOfMonth)
                    remove(KEY_SCHEDULE_NEXT_PAYDAY_EPOCH)
                }

                is PaySchedule.BiWeekly -> {
                    putString(KEY_SCHEDULE_TYPE, SCHEDULE_BIWEEKLY)
                    putLong(KEY_SCHEDULE_NEXT_PAYDAY_EPOCH, schedule.nextPayday.toEpochDay())
                    remove(KEY_SCHEDULE_DAY_OF_MONTH)
                }
            }
        }
    }

    fun loadPlanInput(context: Context): PlanInput? {
        val p = prefs(context)
        val netSalaryStr = p.getString(KEY_NET_SALARY, null) ?: return null
        val goalTypeStr = p.getString(KEY_GOAL_TYPE, null) ?: return null
        val goalAmountStr = p.getString(KEY_GOAL_AMOUNT, null) ?: return null
        val presetStr = p.getString(KEY_PRESET, null) ?: AllocationPreset.BALANCED.name

        val goalType = runCatching { GoalType.valueOf(goalTypeStr) }.getOrNull() ?: return null
        val goalAmount = runCatching { BigDecimal(goalAmountStr) }.getOrNull() ?: return null
        val goalLabel = p.getString(KEY_GOAL_LABEL, "")?.takeIf { it.isNotBlank() }

        val netSalary = runCatching { BigDecimal(netSalaryStr) }.getOrNull() ?: return null
        val preset = runCatching { AllocationPreset.valueOf(presetStr) }.getOrElse { AllocationPreset.BALANCED }

        val scheduleType = p.getString(KEY_SCHEDULE_TYPE, SCHEDULE_MONTHLY) ?: SCHEDULE_MONTHLY
        val schedule: PaySchedule = when (scheduleType) {
            SCHEDULE_BIWEEKLY -> {
                val epoch = p.getLong(KEY_SCHEDULE_NEXT_PAYDAY_EPOCH, Long.MIN_VALUE)
                if (epoch == Long.MIN_VALUE) return null
                PaySchedule.BiWeekly(nextPayday = LocalDate.ofEpochDay(epoch))
            }
            else -> {
                val day = p.getInt(KEY_SCHEDULE_DAY_OF_MONTH, 1).coerceIn(1, 31)
                PaySchedule.Monthly(dayOfMonth = day)
            }
        }

        return PlanInput(
            netSalary = netSalary,
            schedule = schedule,
            preset = preset,
            goal = Goal(amount = goalAmount, type = goalType, label = goalLabel)
        )
    }

    /**
        * For bi-weekly scheduling, the Worker advances the next payday after firing.
        */
    fun updateNextBiWeeklyPayday(context: Context, nextPayday: LocalDate) {
        prefs(context).edit {
            putString(KEY_SCHEDULE_TYPE, SCHEDULE_BIWEEKLY)
                .putLong(KEY_SCHEDULE_NEXT_PAYDAY_EPOCH, nextPayday.toEpochDay())
        }
    }

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
}
