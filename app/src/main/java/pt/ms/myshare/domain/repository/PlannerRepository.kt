package pt.ms.myshare.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan

interface PlannerRepository {
    fun observePlan(): Flow<SalaryPlan?>
    fun loadPlan(): SalaryPlan?
    suspend fun savePlan(plan: SalaryPlan)
    suspend fun clearPlan()
    
    // Multi-Goal Support
    fun observeGoals(): Flow<List<Goal>>
    fun loadGoals(): List<Goal>
    suspend fun saveGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)

    // Historical Reviews
    fun observeReviews(): Flow<List<ManualReview>>
    fun observeLatestReview(): Flow<ManualReview?>
    fun loadLatestReview(): ManualReview?
    suspend fun saveReview(review: ManualReview)

    fun observeReminderConfiguration(): Flow<ReminderConfiguration>
    fun loadReminderConfiguration(): ReminderConfiguration
    suspend fun saveReminderConfiguration(configuration: ReminderConfiguration)

    fun observeAutomationEnabled(): Flow<Boolean>
    suspend fun saveAutomationEnabled(enabled: Boolean)

    fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    
    suspend fun syncFromFirestore()
}
