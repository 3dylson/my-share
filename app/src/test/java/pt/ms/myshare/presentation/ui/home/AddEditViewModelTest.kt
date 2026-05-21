package pt.ms.myshare.presentation.ui.home

import androidx.lifecycle.SavedStateHandle
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.use_case.CheckEntitlementLimitUseCase
import pt.ms.myshare.TestUserPreferencesRepository
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `missing goal edit route shows missing state and blocks save`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.observeGoals() } returns flowOf(emptyList())

        val viewModel = GoalAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle(mapOf("goalId" to "missing-goal"))
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isMissingExistingGoal)
        assertTrue(viewModel.state.value.error == "goal_add_error_missing")

        viewModel.saveGoal()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveGoal(any<Goal>()) }
    }

    @Test
    fun `missing rule edit route shows missing state and blocks save`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadRules() } returns emptyList()

        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle(mapOf("ruleId" to "missing-rule"))
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isMissingExistingRule)
        assertTrue(viewModel.state.value.error == "rule_add_error_missing")

        viewModel.saveRule()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveRule(any<PaydayRule>()) }
    }

    @Test
    fun `goal validation error clears when user edits an input`() = runTest {
        val viewModel = GoalAddViewModel(
            repository = mockk(relaxed = true),
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.saveGoal()

        assertTrue(viewModel.state.value.error != null)

        viewModel.onNameChanged("Emergency Fund")

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isMissingExistingGoal)
    }

    @Test
    fun `rule validation error clears when user edits an input`() = runTest {
        val viewModel = RuleAddViewModel(
            repository = mockk(relaxed = true),
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.saveRule()

        assertTrue(viewModel.state.value.error != null)

        viewModel.onAmountChanged("10")

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isMissingExistingRule)
    }

    @Test
    fun `rule allocation toggle converts percentage to fixed amount`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns salaryPlan()
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onAmountChanged("25")
        viewModel.onPercentageToggle(false)

        assertFalse(viewModel.state.value.isPercentage)
        assertEquals("150", viewModel.state.value.amount)
    }

    @Test
    fun `rule allocation toggle converts fixed amount to percentage`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns salaryPlan()
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onPercentageToggle(false)
        viewModel.onAmountChanged("150")
        viewModel.onPercentageToggle(true)

        assertTrue(viewModel.state.value.isPercentage)
        assertEquals("25", viewModel.state.value.amount)
    }

    @Test
    fun `rule save blocks fixed amount greater than available income`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns salaryPlan()
        every { repository.loadRules() } returns emptyList()
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Too large")
        viewModel.onPercentageToggle(false)
        viewModel.onAmountChanged("700")
        viewModel.saveRule()
        advanceUntilIdle()

        assertEquals("rule_add_error_exceeds_available", viewModel.state.value.error)
        coVerify(exactly = 0) { repository.saveRule(any<PaydayRule>()) }
    }

    @Test
    fun `rule save blocks percentage greater than available income`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns salaryPlan()
        every { repository.loadRules() } returns emptyList()
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Too large")
        viewModel.onAmountChanged("101")
        viewModel.saveRule()
        advanceUntilIdle()

        assertEquals("rule_add_error_exceeds_available", viewModel.state.value.error)
        coVerify(exactly = 0) { repository.saveRule(any<PaydayRule>()) }
    }

    @Test
    fun `rule edit validation excludes current rule from existing allocations`() = runTest {
        val currentRule = PaydayRule(
            id = "rule-1",
            name = "Savings",
            amount = BigDecimal("500"),
            isPercentage = false
        )
        val otherRule = PaydayRule(
            id = "rule-2",
            name = "Investing",
            amount = BigDecimal("100"),
            isPercentage = false
        )
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns salaryPlan()
        every { repository.loadRules() } returns listOf(currentRule, otherRule)
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(),
            savedStateHandle = SavedStateHandle(mapOf("ruleId" to currentRule.id))
        )
        advanceUntilIdle()

        viewModel.saveRule()
        advanceUntilIdle()

        coVerify { repository.saveRule(any<PaydayRule>()) }

        viewModel.onAmountChanged("550")
        viewModel.saveRule()
        advanceUntilIdle()

        assertEquals("rule_add_error_exceeds_available", viewModel.state.value.error)
        coVerify(exactly = 1) { repository.saveRule(any<PaydayRule>()) }
    }

    @Test
    fun `free user cannot save a second goal from direct add route`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadGoals() } returns listOf(
            Goal(id = "goal-1", name = "Emergency", targetAmount = BigDecimal("500"))
        )
        val viewModel = GoalAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(isPro = false),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Investing")
        viewModel.onAmountChanged("1000")
        viewModel.saveGoal()
        advanceUntilIdle()

        assertEquals("goal_add_error_premium_required", viewModel.state.value.error)
        coVerify(exactly = 0) { repository.saveGoal(any<Goal>()) }
    }

    @Test
    fun `premium user can save a second goal from direct add route`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadGoals() } returns listOf(
            Goal(id = "goal-1", name = "Emergency", targetAmount = BigDecimal("500"))
        )
        val viewModel = GoalAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(isPro = true),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Investing")
        viewModel.onAmountChanged("1000")
        viewModel.saveGoal()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSaved)
        coVerify(exactly = 1) { repository.saveGoal(any<Goal>()) }
    }

    @Test
    fun `free user cannot save a second payday rule from direct add route`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns null
        every { repository.loadRules() } returns listOf(
            PaydayRule(id = "rule-1", name = "Savings", amount = BigDecimal("10"))
        )
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(isPro = false),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Investing")
        viewModel.onAmountChanged("5")
        viewModel.saveRule()
        advanceUntilIdle()

        assertEquals("rule_add_error_premium_required", viewModel.state.value.error)
        coVerify(exactly = 0) { repository.saveRule(any<PaydayRule>()) }
    }

    @Test
    fun `premium user can save a second payday rule from direct add route`() = runTest {
        val repository = mockk<PlannerRepository>(relaxed = true)
        every { repository.loadPlan() } returns null
        every { repository.loadRules() } returns listOf(
            PaydayRule(id = "rule-1", name = "Savings", amount = BigDecimal("10"))
        )
        val viewModel = RuleAddViewModel(
            repository = repository,
            userPreferencesRepository = TestUserPreferencesRepository(),
            checkEntitlementLimitUseCase = checkEntitlementLimitUseCase(isPro = true),
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Investing")
        viewModel.onAmountChanged("5")
        viewModel.saveRule()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSaved)
        coVerify(exactly = 1) { repository.saveRule(any<PaydayRule>()) }
    }

    private fun salaryPlan(): SalaryPlan = SalaryPlan(
        focus = PlanningFocus.SAVE_WITHOUT_STRESS,
        netIncomePerPayday = BigDecimal("1000"),
        monthlyFixedCosts = BigDecimal("400"),
        payFrequency = PayFrequency.MONTHLY,
        preset = AllocationPreset.BALANCED
    )

    private fun checkEntitlementLimitUseCase(isPro: Boolean = false): CheckEntitlementLimitUseCase {
        val entitlementRepository = mockk<EntitlementRepository>(relaxed = true)
        every { entitlementRepository.isPro } returns flowOf(isPro)
        return CheckEntitlementLimitUseCase(entitlementRepository)
    }
}
