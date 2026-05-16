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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.TestUserPreferencesRepository

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
            savedStateHandle = SavedStateHandle()
        )

        viewModel.saveRule()

        assertTrue(viewModel.state.value.error != null)

        viewModel.onAmountChanged("10")

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isMissingExistingRule)
    }
}
