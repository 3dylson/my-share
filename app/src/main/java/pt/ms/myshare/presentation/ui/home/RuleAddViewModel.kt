package pt.ms.myshare.presentation.ui.home

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
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.repository.PlannerRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class RuleAddState(
    val ruleId: String? = null,
    val name: String = "",
    val amount: String = "",
    val isPercentage: Boolean = true,
    val type: PaydayRuleType = PaydayRuleType.OTHER,
    val createdAt: LocalDate? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RuleAddViewModel @Inject constructor(
    private val repository: PlannerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RuleAddState())
    val state: StateFlow<RuleAddState> = _state.asStateFlow()

    private val navRuleId: String? = savedStateHandle.get<String>("ruleId")

    init {
        if (navRuleId != null) {
            loadExistingRule(navRuleId)
        }
    }

    private fun loadExistingRule(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val plan = repository.observePlan().first()
            val rule = plan?.rules?.find { it.id == id }
            if (rule != null) {
                _state.update { 
                    it.copy(
                        ruleId = rule.id,
                        name = rule.name,
                        amount = rule.amount.stripTrailingZeros().toPlainString(),
                        isPercentage = rule.isPercentage,
                        type = rule.type,
                        createdAt = rule.createdAt,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Rule not found.") }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onAmountChanged(newAmount: String) {
        _state.update { it.copy(amount = newAmount) }
    }

    fun onPercentageToggle(isPercentage: Boolean) {
        _state.update { it.copy(isPercentage = isPercentage) }
    }

    fun onTypeChanged(newType: PaydayRuleType) {
        _state.update { it.copy(type = newType) }
    }

    fun saveRule() {
        val amount = _state.value.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (_state.value.name.isBlank() || amount <= BigDecimal.ZERO) {
            _state.update { it.copy(error = "Please enter a valid name and amount.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentPlan = repository.loadPlan()
            if (currentPlan == null) {
                _state.update { it.copy(isLoading = false, error = "No active plan found.") }
                return@launch
            }

            val finalRule = PaydayRule(
                id = _state.value.ruleId ?: UUID.randomUUID().toString(),
                name = _state.value.name,
                amount = amount,
                isPercentage = _state.value.isPercentage,
                type = _state.value.type,
                createdAt = _state.value.createdAt ?: LocalDate.now()
            )

            val updatedRules = currentPlan.rules.toMutableList()
            val index = updatedRules.indexOfFirst { it.id == finalRule.id }
            if (index != -1) {
                updatedRules[index] = finalRule
            } else {
                updatedRules.add(finalRule)
            }

            repository.savePlan(currentPlan.copy(rules = updatedRules))
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteRule() {
        val id = _state.value.ruleId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentPlan = repository.loadPlan() ?: return@launch
            val updatedRules = currentPlan.rules.filterNot { it.id == id }
            repository.savePlan(currentPlan.copy(rules = updatedRules))
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
