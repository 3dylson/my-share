package pt.ms.myshare.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoalType
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.repository.PlannerRepository

import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PlannerRepository {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val planState = MutableStateFlow(readPlan())
    private val ruleState = MutableStateFlow<List<PaydayRule>>(emptyList())

    private val goalState = MutableStateFlow<List<Goal>>(emptyList())

    private val reviewState = MutableStateFlow<List<ManualReview>>(emptyList())

    private val reminderState = MutableStateFlow(readReminderConfiguration())
    private val automationState = MutableStateFlow(readAutomationEnabled())

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
            .remove(KEY_GOAL_NAME)
            .remove(KEY_GOAL_AMOUNT)
            .putLong(KEY_PLAN_CREATED_AT_EPOCH, plan.createdAt.toEpochDay())
            .putString(KEY_PRESET, plan.preset.name)
            .apply()
        planState.value = plan
        
        syncPlanToFirestore(plan)
    }

    private fun syncPlanToFirestore(plan: SalaryPlan) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf(
                "focus" to plan.focus.name,
                "netIncomePerPayday" to plan.netIncomePerPayday.toPlainString(),
                "monthlyFixedCosts" to plan.monthlyFixedCosts.toPlainString(),
                "payFrequency" to plan.payFrequency.name,
                "monthlyPayday" to (plan.monthlyPayday ?: 1),
                "nextBiweeklyPaydayText" to (plan.nextBiweeklyPayday?.toString() ?: ""),
                "preset" to plan.preset.name,
                "createdAtDate" to plan.createdAt.toString()
            )
            try {
                firestore.collection("users").document(user.uid).collection("plans").document("current")
                    .set(data)
                Timber.tag(TAG).d("Plan synced to Firestore successfully")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to sync plan to Firestore")
            }
        }
    }

    override suspend fun clearPlan() {
        Timber.tag(TAG).d("clearPlan")
        prefs.edit().clear().apply()
        planState.value = null
        ruleState.value = emptyList()
        goalState.value = emptyList()
        reviewState.value = emptyList()
        automationState.value = false
    }

    override suspend fun syncFromFirestore() {
        val user = firebaseAuth.currentUser ?: return
        Timber.tag(TAG).d("syncFromFirestore starting for user %s", user.uid)
        
        try {
            // 1. Sync Plan
            val planDoc = firestore.collection("users").document(user.uid).collection("plans").document("current").get().await()
            if (planDoc.exists()) {
                val focus = planDoc.getString("focus")?.let { PlanningFocus.valueOf(it) } ?: PlanningFocus.SAVE_WITHOUT_STRESS
                val income = planDoc.getString("netIncomePerPayday")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val fixed = planDoc.getString("monthlyFixedCosts")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val freq = planDoc.getString("payFrequency")?.let { PayFrequency.valueOf(it) } ?: PayFrequency.MONTHLY
                val preset = planDoc.getString("preset")?.let { AllocationPreset.valueOf(it) } ?: AllocationPreset.BALANCED
                
                val plan = SalaryPlan(
                    focus = focus,
                    netIncomePerPayday = income,
                    monthlyFixedCosts = fixed,
                    payFrequency = freq,
                    monthlyPayday = planDoc.getLong("monthlyPayday")?.toInt(),
                    nextBiweeklyPayday = planDoc.getString("nextBiweeklyPaydayText")?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) },
                    preset = preset,
                    createdAt = planDoc.getString("createdAtDate")?.let { LocalDate.parse(it) } ?: LocalDate.now()
                )
                savePlan(plan)
            }

            // 1.5 Sync Rules, Goals & Reviews (Collections)
            syncRulesFromFirestore(user.uid)
            syncGoalsFromFirestore(user.uid)
            syncReviewsFromFirestore(user.uid)

            // 2. Sync Onboarding State
            val userDoc = firestore.collection("users").document(user.uid).get().await()
            if (userDoc.exists()) {
                val completed = userDoc.getBoolean("onboardingCompleted") ?: false
                prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
            }
            
            // 3. Sync Settings (Automation)
            val settingsDoc = firestore.collection("users").document(user.uid).collection("settings").document("automation").get().await()
            if (settingsDoc.exists()) {
                val automationEnabled = settingsDoc.getBoolean("automationEnabled") ?: false
                saveAutomationEnabled(automationEnabled)
            }
            
            Timber.tag(TAG).d("syncFromFirestore completed successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "syncFromFirestore failed")
        }
    }

    private suspend fun syncRulesFromFirestore(uid: String) {
        try {
            val rulesSnapshot = firestore.collection("users").document(uid).collection("rules").get().await()
            val rules = rulesSnapshot.documents.mapNotNull { doc ->
                runCatching {
                    PaydayRule(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        amount = doc.getString("amount")?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        isPercentage = doc.getBoolean("isPercentage") ?: true,
                        type = doc.getString("type")?.let { PaydayRuleType.valueOf(it) } ?: PaydayRuleType.OTHER,
                        createdAt = doc.getString("createdAtDate")?.let { LocalDate.parse(it) } ?: LocalDate.now()
                    )
                }.getOrNull()
            }
            ruleState.value = rules
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "syncRulesFromFirestore failed")
        }
    }

    private suspend fun syncGoalsFromFirestore(uid: String) {
        try {
            val goalsSnapshot = firestore.collection("users").document(uid).collection("goals").get().await()
            val goals = goalsSnapshot.documents.mapNotNull { doc ->
                runCatching {
                    Goal(
                        id = doc.id,
                        targetAmount = doc.getString("targetAmount")?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        currentProgress = doc.getString("currentProgress")?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        type = doc.getString("type")?.let { GoalType.valueOf(it) } ?: GoalType.CUSTOM,
                        name = doc.getString("name") ?: "Goal",
                        createdAt = doc.getString("createdAtDate")?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                        isCompleted = doc.getBoolean("isCompleted") ?: false
                    )
                }.getOrNull()
            }
            goalState.value = goals
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "syncGoalsFromFirestore failed")
        }
    }

    private suspend fun syncReviewsFromFirestore(uid: String) {
        try {
            val reviewsSnapshot = firestore.collection("users").document(uid).collection("reviews").get().await()
            val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
                runCatching {
                    ManualReview(
                        id = doc.id,
                        actualFlexibleSpend = doc.getString("actualFlexibleSpend")?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        actualGoalContribution = doc.getString("actualGoalContribution")?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        plannedFlexibleSpend = doc.getString("plannedFlexibleSpend")?.toBigDecimalOrNull(),
                        plannedGoalContribution = doc.getString("plannedGoalContribution")?.toBigDecimalOrNull(),
                        createdAt = doc.getString("createdAtDate")?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                        paydayDate = doc.getString("paydayDate")?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
                    )
                }.getOrNull()
            }
            reviewState.value = reviews
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "syncReviewsFromFirestore failed")
        }
    }

    override fun observeRules(): Flow<List<PaydayRule>> = ruleState.asStateFlow()

    override fun loadRules(): List<PaydayRule> = ruleState.value

    override suspend fun saveRule(rule: PaydayRule) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf(
                "id" to rule.id,
                "name" to rule.name,
                "amount" to rule.amount.toPlainString(),
                "isPercentage" to rule.isPercentage,
                "type" to rule.type.name,
                "createdAtDate" to rule.createdAt.toString()
            )
            try {
                firestore.collection("users").document(user.uid)
                    .collection("rules").document(rule.id).set(data)
                
                // Update local state
                val currentRules = ruleState.value.toMutableList()
                val index = currentRules.indexOfFirst { it.id == rule.id }
                if (index >= 0) {
                    currentRules[index] = rule
                } else {
                    currentRules.add(rule)
                }
                ruleState.value = currentRules
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to save rule to Firestore")
            }
        }
    }

    override suspend fun deleteRule(ruleId: String) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("rules").document(ruleId).delete()
                
                // Update local state
                ruleState.value = ruleState.value.filter { it.id != ruleId }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to delete rule from Firestore")
            }
        }
    }

    override fun observeGoals(): Flow<List<Goal>> = goalState.asStateFlow()

    override fun loadGoals(): List<Goal> = goalState.value

    override suspend fun saveGoal(goal: Goal) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf(
                "id" to goal.id,
                "targetAmount" to goal.targetAmount.toPlainString(),
                "currentProgress" to goal.currentProgress.toPlainString(),
                "type" to goal.type.name,
                "name" to goal.name,
                "createdAtDate" to goal.createdAt.toString(),
                "isCompleted" to goal.isCompleted
            )
            try {
                firestore.collection("users").document(user.uid)
                    .collection("goals").document(goal.id).set(data)
                
                // Update local state
                val currentGoals = goalState.value.toMutableList()
                val index = currentGoals.indexOfFirst { it.id == goal.id }
                if (index >= 0) {
                    currentGoals[index] = goal
                } else {
                    currentGoals.add(goal)
                }
                goalState.value = currentGoals
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to save goal to Firestore")
            }
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("goals").document(goalId).delete()
                
                // Update local state
                goalState.value = goalState.value.filter { it.id != goalId }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to delete goal from Firestore")
            }
        }
    }

    override fun observeReviews(): Flow<List<ManualReview>> = reviewState.asStateFlow()

    override fun observeLatestReview(): Flow<ManualReview?> = reviewState.asStateFlow().map { it.maxByOrNull { r -> r.createdAt } }

    override fun loadLatestReview(): ManualReview? = reviewState.value.maxByOrNull { it.createdAt }

    override suspend fun saveReview(review: ManualReview) {
        Timber.tag(TAG).d("saveReview actualFlexible=%s actualGoal=%s", review.actualFlexibleSpend, review.actualGoalContribution)
        syncReviewToFirestore(review)
        
        // Update local state
        val currentReviews = reviewState.value.toMutableList()
        currentReviews.add(review)
        reviewState.value = currentReviews
    }

    private fun syncReviewToFirestore(review: ManualReview) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf(
                "id" to review.id,
                "actualFlexibleSpend" to review.actualFlexibleSpend.toPlainString(),
                "actualGoalContribution" to review.actualGoalContribution.toPlainString(),
                "plannedFlexibleSpend" to review.plannedFlexibleSpend?.toPlainString(),
                "plannedGoalContribution" to review.plannedGoalContribution?.toPlainString(),
                "createdAtDate" to review.createdAt.toString(),
                "paydayDate" to (review.paydayDate?.toString() ?: "")
            )
            try {
                firestore.collection("users").document(user.uid)
                    .collection("reviews").document(review.id).set(data)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to sync review to Firestore")
            }
        }
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
        syncReminderToFirestore(configuration)
    }

    private fun syncReminderToFirestore(config: ReminderConfiguration) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf(
                "enabled" to config.enabled,
                "hourOfDay" to config.hourOfDay,
                "minute" to config.minute,
                "cadence" to config.cadence.name
            )
            try {
                firestore.collection("users").document(user.uid).collection("settings").document("reminders").set(data)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to sync reminder to Firestore")
            }
        }
    }

    override fun observeAutomationEnabled(): Flow<Boolean> = automationState.asStateFlow()

    override suspend fun saveAutomationEnabled(enabled: Boolean) {
        Timber.tag(TAG).d("saveAutomationEnabled enabled=%s", enabled)
        prefs.edit().putBoolean(KEY_AUTOMATION_ENABLED, enabled).apply()
        automationState.value = enabled
        syncAutomationToFirestore(enabled)
    }

    private fun syncAutomationToFirestore(enabled: Boolean) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf("automationEnabled" to enabled)
            try {
                firestore.collection("users").document(user.uid).collection("settings").document("automation").set(data)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to sync automation to Firestore")
            }
        }
    }

    override fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        Timber.tag(TAG).d("setOnboardingCompleted completed=%s", completed)
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        syncOnboardingStateToFirestore(completed)
    }

    private fun syncOnboardingStateToFirestore(completed: Boolean) {
        val user = firebaseAuth.currentUser ?: return
        coroutineScope.launch {
            val data = hashMapOf("onboardingCompleted" to completed)
            try {
                firestore.collection("users").document(user.uid).set(data, com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to sync onboarding state to Firestore")
            }
        }
    }

    private fun readPlan(): SalaryPlan? {
        val preset = prefs.getString(KEY_PRESET, null)?.let { runCatching { AllocationPreset.valueOf(it) }.getOrNull() } 
            ?: AllocationPreset.BALANCED // SEED: Fallback to Balanced if null during audit
        val income = prefs.getString(KEY_NET_INCOME, null)?.toBigDecimalOrNull()
            ?: BigDecimal.ZERO
        val focusText = prefs.getString(KEY_FOCUS, null)
        val focus = if (focusText != null) {
            runCatching { PlanningFocus.valueOf(focusText) }.getOrNull() ?: PlanningFocus.SAVE_WITHOUT_STRESS
        } else PlanningFocus.SAVE_WITHOUT_STRESS

        val fixedCosts = prefs.getString(KEY_MONTHLY_FIXED_COSTS, null)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val payFrequency = prefs.getString(KEY_PAY_FREQUENCY, null)?.let { runCatching { PayFrequency.valueOf(it) }.getOrNull() } ?: PayFrequency.MONTHLY
        val goalName = prefs.getString(KEY_GOAL_NAME, null).orEmpty().ifBlank { "Emergency fund" }
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

    private fun readAutomationEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTOMATION_ENABLED, false)
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
        const val KEY_FLEXIBLE_SPEND = "planner_flexible_spend"
        const val KEY_SAVINGS = "planner_savings"
        const val KEY_INVESTING = "planner_investing"
        const val KEY_CRYPTO = "planner_crypto"
        const val KEY_PLAN_CREATED_AT_EPOCH = "planner_plan_created_at_epoch"
        const val KEY_REVIEW_FLEXIBLE_SPEND = "planner_review_flexible_spend"
        const val KEY_REVIEW_GOAL_CONTRIBUTION = "planner_review_goal_contribution"
        const val KEY_REVIEW_CREATED_AT_EPOCH = "planner_review_created_at_epoch"
        const val KEY_REMINDER_ENABLED = "planner_reminder_enabled"
        const val KEY_REMINDER_HOUR = "planner_reminder_hour"
        const val KEY_REMINDER_MINUTE = "planner_reminder_minute"
        const val KEY_REMINDER_CADENCE = "planner_reminder_cadence"
        const val KEY_AUTOMATION_ENABLED = "planner_automation_enabled"
    }
}
