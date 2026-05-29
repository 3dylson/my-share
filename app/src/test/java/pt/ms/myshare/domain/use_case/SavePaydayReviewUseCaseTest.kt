package pt.ms.myshare.domain.use_case

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.ManualReview
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PlannerSyncResult
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PremiumAdjustmentRecord
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.time.LocalDate

class SavePaydayReviewUseCaseTest {

    private val plannerRepository = SaveReviewFakePlannerRepository()
    private val updateGoalProgressUseCase = mockk<UpdateGoalProgressUseCase>(relaxed = true)
    private val adjustGoalProgressForReviewCorrectionUseCase = mockk<AdjustGoalProgressForReviewCorrectionUseCase>(relaxed = true)
    private val useCase = SavePaydayReviewUseCase(
        plannerRepository = plannerRepository,
        calculatePlanPreviewUseCase = CalculatePlanPreviewUseCase(ResolveAllocationStrategyRulesUseCase()),
        updateGoalProgressUseCase = updateGoalProgressUseCase,
        adjustGoalProgressForReviewCorrectionUseCase = adjustGoalProgressForReviewCorrectionUseCase
    )

    @Test
    fun `saves actual flexible spend from spending pot leftover`() = runTest {
        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("200"),
            movedToGoal = BigDecimal("100")
        )

        assertEquals(BigDecimal("100.00"), result.review.actualFlexibleSpend)
        assertEquals(BigDecimal("100"), result.review.actualGoalContribution)
        assertEquals(BigDecimal("300.00"), result.review.plannedFlexibleSpend)
        assertEquals(result.review, plannerRepository.savedReviews.single())
        coVerify(exactly = 1) { updateGoalProgressUseCase.execute(BigDecimal("100")) }
    }

    @Test
    fun `clamps actual flexible spend to zero when leftover is above planned pot`() = runTest {
        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("500"),
            movedToGoal = BigDecimal("100")
        )

        assertEquals(BigDecimal("0"), result.review.actualFlexibleSpend)
    }

    @Test
    fun `treats negative leftover as zero leftover`() = runTest {
        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("-25"),
            movedToGoal = BigDecimal("0")
        )

        assertEquals(BigDecimal("300.00"), result.review.actualFlexibleSpend)
        coVerify(exactly = 1) { updateGoalProgressUseCase.execute(BigDecimal("0")) }
    }

    @Test
    fun `uses first goal target when snapshotting planned amounts`() = runTest {
        plannerRepository.goals = listOf(
            Goal(
                name = "Emergency fund",
                targetAmount = BigDecimal("1000"),
                currentProgress = BigDecimal.ZERO
            )
        )

        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("200"),
            movedToGoal = BigDecimal("100")
        )

        assertEquals(BigDecimal("300.00"), result.plannedFlexibleSpend)
        assertEquals(BigDecimal("300.00"), result.plannedGoalContribution)
    }

    @Test
    fun `updates today's review and adjusts goal progress by contribution delta`() = runTest {
        val existingReview = ManualReview(
            actualFlexibleSpend = BigDecimal("120"),
            actualGoalContribution = BigDecimal("80"),
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("300")
        )
        plannerRepository.seedReview(existingReview)

        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("150"),
            movedToGoal = BigDecimal("125")
        )

        assertEquals(existingReview.id, result.review.id)
        assertEquals(0, BigDecimal("150").compareTo(result.review.actualFlexibleSpend))
        assertEquals(BigDecimal("300"), result.review.plannedFlexibleSpend)
        assertEquals(BigDecimal("300"), result.review.plannedGoalContribution)
        assertEquals(result.review, plannerRepository.savedReviews.single())
        coVerify(exactly = 0) { updateGoalProgressUseCase.execute(BigDecimal("125")) }
        coVerify(exactly = 1) {
            adjustGoalProgressForReviewCorrectionUseCase.execute(BigDecimal("45"))
        }
    }

    @Test
    fun `updates today's review even when stored history is not ordered`() = runTest {
        val todaysReview = ManualReview(
            id = "today",
            actualFlexibleSpend = BigDecimal("120"),
            actualGoalContribution = BigDecimal("80"),
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("300")
        )
        val olderReviewAfterTodayInStorage = ManualReview(
            id = "older",
            actualFlexibleSpend = BigDecimal("200"),
            actualGoalContribution = BigDecimal("200"),
            plannedFlexibleSpend = BigDecimal("300"),
            plannedGoalContribution = BigDecimal("300"),
            createdAt = LocalDate.now().minusMonths(1),
            paydayDate = LocalDate.now().minusMonths(1)
        )
        plannerRepository.seedReviews(listOf(todaysReview, olderReviewAfterTodayInStorage))

        val result = useCase.execute(
            plan = plan(),
            leftInSpendingPot = BigDecimal("150"),
            movedToGoal = BigDecimal("125")
        )

        assertEquals("today", result.review.id)
        assertEquals(2, plannerRepository.savedReviews.size)
        coVerify(exactly = 1) {
            adjustGoalProgressForReviewCorrectionUseCase.execute(BigDecimal("45"))
        }
    }

    private fun plan(): SalaryPlan = SalaryPlan(
        focus = PlanningFocus.SAVE_WITHOUT_STRESS,
        netIncomePerPayday = BigDecimal("1000"),
        monthlyFixedCosts = BigDecimal("400"),
        payFrequency = PayFrequency.MONTHLY,
        monthlyPayday = 1,
        preset = AllocationPreset.BALANCED
    )
}

private class SaveReviewFakePlannerRepository : PlannerRepository {
    private val planFlow = MutableStateFlow<SalaryPlan?>(null)
    private val reviewsFlow = MutableStateFlow<List<ManualReview>>(emptyList())
    private val latestReviewFlow = MutableStateFlow<ManualReview?>(null)
    private val reminderFlow = MutableStateFlow(ReminderConfiguration(false, 9, 0, ReminderCadence.PAYDAY))
    private val automationFlow = MutableStateFlow(false)

    var goals: List<Goal> = emptyList()
    val savedReviews: List<ManualReview>
        get() = reviewsFlow.value

    override fun observePlan(): Flow<SalaryPlan?> = planFlow.asStateFlow()
    override fun loadPlan(): SalaryPlan? = planFlow.value
    override suspend fun savePlan(plan: SalaryPlan) { planFlow.emit(plan) }
    override suspend fun clearPlan() { planFlow.emit(null) }
    override fun observeRules(): Flow<List<PaydayRule>> = MutableStateFlow(emptyList<PaydayRule>()).asStateFlow()
    override fun loadRules(): List<PaydayRule> = emptyList()
    override suspend fun saveRule(rule: PaydayRule) = Unit
    override suspend fun deleteRule(ruleId: String) = Unit
    override fun observeGoals(): Flow<List<Goal>> = MutableStateFlow(goals).asStateFlow()
    override fun loadGoals(): List<Goal> = goals
    override suspend fun saveGoal(goal: Goal) = Unit
    override suspend fun deleteGoal(goalId: String) = Unit
    override fun observeReviews(): Flow<List<ManualReview>> = reviewsFlow.asStateFlow()
    override fun observeLatestReview(): Flow<ManualReview?> = latestReviewFlow.asStateFlow()
    override fun loadLatestReview(): ManualReview? = latestReviewFlow.value
    override suspend fun saveReview(review: ManualReview) {
        reviewsFlow.emit(reviewsFlow.value + review)
        latestReviewFlow.emit(review)
    }
    override suspend fun updateReview(review: ManualReview) {
        val updated = reviewsFlow.value.map { if (it.id == review.id) review else it }
        reviewsFlow.emit(updated)
        latestReviewFlow.emit(review)
    }
    fun seedReview(review: ManualReview) {
        seedReviews(listOf(review))
    }

    fun seedReviews(reviews: List<ManualReview>) {
        reviewsFlow.value = reviews
        latestReviewFlow.value = reviews.lastOrNull()
    }
    override suspend fun deleteReview(reviewId: String) {
        val updated = reviewsFlow.value.filterNot { it.id == reviewId }
        reviewsFlow.emit(updated)
        latestReviewFlow.emit(updated.lastOrNull())
    }
    override fun observeReminderConfiguration(): Flow<ReminderConfiguration> = reminderFlow.asStateFlow()
    override fun loadReminderConfiguration(): ReminderConfiguration = reminderFlow.value
    override suspend fun saveReminderConfiguration(configuration: ReminderConfiguration) {
        reminderFlow.emit(configuration)
    }
    override fun observeAutomationEnabled(): Flow<Boolean> = automationFlow.asStateFlow()
    override fun loadAutomationEnabled(): Boolean = automationFlow.value
    override suspend fun saveAutomationEnabled(enabled: Boolean) { automationFlow.emit(enabled) }
    override fun observePremiumAdjustmentRecords(): Flow<List<PremiumAdjustmentRecord>> =
        MutableStateFlow(emptyList<PremiumAdjustmentRecord>()).asStateFlow()
    override fun loadPremiumAdjustmentRecords(): List<PremiumAdjustmentRecord> = emptyList()
    override suspend fun savePremiumAdjustmentRecord(record: PremiumAdjustmentRecord) = Unit
    override fun isOnboardingCompleted(): Boolean = false
    override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    override suspend fun syncFromFirestore() = Unit
    override suspend fun syncLocalStateIfAuthenticated() = Unit
    override suspend fun preserveLocalStateForAuthenticatedUser(): Result<PlannerSyncResult> =
        Result.success(
            PlannerSyncResult(
                localPlanUploaded = false,
                remotePlanPreserved = false,
                ruleCount = 0,
                goalCount = 0,
                reviewCount = reviewsFlow.value.size,
                premiumAdjustmentCount = 0
            )
        )
}
