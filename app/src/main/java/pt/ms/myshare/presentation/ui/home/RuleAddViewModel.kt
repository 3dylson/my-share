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
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

data class RuleAddState(
    val requestedRuleId: String? = null,
    val ruleId: String? = null,
    val name: String = "",
    val amount: String = "",
    val isPercentage: Boolean = true,
    val type: PaydayRuleType = PaydayRuleType.OTHER,
    val createdAt: LocalDate? = null,
    val userPreferences: UserPreferences = UserPreferences.defaults(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isMissingExistingRule: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RuleAddViewModel @Inject constructor(
    private val repository: PlannerRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RuleAddState())
    val state: StateFlow<RuleAddState> = _state.asStateFlow()

    private val navRuleId: String? = savedStateHandle.get<String>("ruleId")

    init {
        _state.update { it.copy(userPreferences = userPreferencesRepository.loadPreferences()) }
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { preferences ->
                _state.update { it.copy(userPreferences = preferences) }
            }
        }
        if (navRuleId != null) {
            _state.update { it.copy(requestedRuleId = navRuleId) }
            loadExistingRule(navRuleId)
        }
    }

    private fun loadExistingRule(id: String) {
        viewModelScope.launch {
            Timber.tag(TAG).d("Loading rule for edit: %s", id)
            _state.update { it.copy(isLoading = true) }
            val rules = repository.loadRules()
            val rule = rules.find { it.id == id }
            if (rule != null) {
                Timber.tag(TAG).d("Loaded rule for edit: %s", id)
                _state.update { 
                    it.copy(
                        ruleId = rule.id,
                        name = rule.name,
                        amount = LocalizedAmountFormatter.formatEditableAmount(rule.amount.stripTrailingZeros(), _state.value.userPreferences.locale),
                        isPercentage = rule.isPercentage,
                        type = rule.type,
                        createdAt = rule.createdAt,
                        isLoading = false
                    )
                }
            } else {
                Timber.tag(TAG).w("Rule requested for edit was not found: %s", id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isMissingExistingRule = true,
                        error = "rule_add_error_missing"
                    )
                }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName, error = null) }
    }

    fun onAmountChanged(newAmount: String) {
        _state.update { it.copy(amount = LocalizedAmountFormatter.sanitizeAmountInput(newAmount, it.userPreferences.locale), error = null) }
    }

    fun onPercentageToggle(isPercentage: Boolean) {
        _state.update { it.copy(isPercentage = isPercentage, error = null) }
    }

    fun onTypeChanged(newType: PaydayRuleType) {
        _state.update { it.copy(type = newType, error = null) }
    }

    fun saveRule() {
        if (_state.value.isMissingExistingRule) {
            Timber.tag(TAG).w("Blocked save for missing rule edit route: %s", _state.value.requestedRuleId)
            return
        }

        val amount = LocalizedAmountFormatter.parseAmount(_state.value.amount, _state.value.userPreferences.locale) ?: BigDecimal.ZERO
        if (_state.value.name.isBlank() || amount <= BigDecimal.ZERO) {
            _state.update { it.copy(error = "rule_add_error_invalid_name_amount") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val finalRule = PaydayRule(
                id = _state.value.ruleId ?: UUID.randomUUID().toString(),
                name = _state.value.name,
                amount = amount,
                isPercentage = _state.value.isPercentage,
                type = _state.value.type,
                createdAt = _state.value.createdAt ?: LocalDate.now()
            )

            repository.saveRule(finalRule)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    fun deleteRule() {
        val id = _state.value.ruleId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.deleteRule(id)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    companion object {
        private const val TAG = "RuleAddViewModel"
    }
}
