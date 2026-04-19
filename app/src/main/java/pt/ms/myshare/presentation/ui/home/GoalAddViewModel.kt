package pt.ms.myshare.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class GoalAddState(
    val name: String = "",
    val amount: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalAddViewModel @Inject constructor(
    private val repository: PlannerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GoalAddState())
    val state: StateFlow<GoalAddState> = _state.asStateFlow()

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
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                name = _state.value.name,
                targetAmount = amount,
                createdAt = LocalDate.now(),
                isCompleted = false
            )
            repository.saveGoal(newGoal)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
