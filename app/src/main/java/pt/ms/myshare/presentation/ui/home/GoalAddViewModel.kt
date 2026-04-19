package pt.ms.myshare.presentation.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class GoalAddState(
    val goalId: String? = null,
    val name: String = "",
    val amount: String = "",
    val createdAt: LocalDate? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalAddViewModel @Inject constructor(
    private val repository: PlannerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GoalAddState())
    val state: StateFlow<GoalAddState> = _state.asStateFlow()

    private val navGoalId: String? = savedStateHandle.get<String>("goalId")

    init {
        if (navGoalId != null) {
            loadExistingGoal(navGoalId)
        }
    }

    private fun loadExistingGoal(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val goal = repository.observeGoals().first().find { it.id == id }
            if (goal != null) {
                _state.update { 
                    it.copy(
                        goalId = goal.id,
                        name = goal.name,
                        amount = goal.targetAmount.toPlainString(),
                        createdAt = goal.createdAt,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Goal not found.") }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onAmountChanged(newAmount: String) {
        _state.update { it.copy(amount = newAmount) }
    }

    fun saveGoal() {
        val amount = _state.value.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (_state.value.name.isBlank() || amount <= BigDecimal.ZERO) {
            _state.update { it.copy(error = "Please enter a valid name and amount.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val finalGoal = Goal(
                id = _state.value.goalId ?: UUID.randomUUID().toString(),
                name = _state.value.name,
                targetAmount = amount,
                createdAt = _state.value.createdAt ?: LocalDate.now(),
                isCompleted = false
            )
            repository.saveGoal(finalGoal)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteGoal() {
        val id = _state.value.goalId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.deleteGoal(id)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
