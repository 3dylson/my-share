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
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.CheckEntitlementLimitUseCase
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

data class GoalAddState(
    val requestedGoalId: String? = null,
    val goalId: String? = null,
    val name: String = "",
    val amount: String = "",
    val createdAt: LocalDate? = null,
    val userPreferences: UserPreferences = UserPreferences.defaults(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isMissingExistingGoal: Boolean = false,
    val canUseRewardedExtraGoal: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalAddViewModel @Inject constructor(
    private val repository: PlannerRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val checkEntitlementLimitUseCase: CheckEntitlementLimitUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GoalAddState())
    val state: StateFlow<GoalAddState> = _state.asStateFlow()

    private val navGoalId: String? = savedStateHandle.get<String>("goalId")

    init {
        _state.update { it.copy(userPreferences = userPreferencesRepository.loadPreferences()) }
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { preferences ->
                _state.update { it.copy(userPreferences = preferences) }
            }
        }
        if (navGoalId != null) {
            _state.update { it.copy(requestedGoalId = navGoalId) }
            loadExistingGoal(navGoalId)
        }
    }

    private fun loadExistingGoal(id: String) {
        viewModelScope.launch {
            Timber.tag(TAG).d("Loading goal for edit: %s", id)
            _state.update { it.copy(isLoading = true) }
            val goal = repository.observeGoals().first().find { it.id == id }
            if (goal != null) {
                Timber.tag(TAG).d("Loaded goal for edit: %s", id)
                _state.update { 
                    it.copy(
                        goalId = goal.id,
                        name = goal.name,
                        amount = LocalizedAmountFormatter.formatEditableAmount(goal.targetAmount, _state.value.userPreferences.locale),
                        createdAt = goal.createdAt,
                        isLoading = false
                    )
                }
            } else {
                Timber.tag(TAG).w("Goal requested for edit was not found: %s", id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isMissingExistingGoal = true,
                        error = "goal_add_error_missing"
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

    fun saveGoal() {
        saveGoal(allowRewardedExtraGoal = false)
    }

    fun saveGoalWithRewardedExtraGoal() {
        saveGoal(allowRewardedExtraGoal = true)
    }

    fun markRewardedExtraGoalUnavailable() {
        _state.update { it.copy(error = "goal_add_error_reward_unavailable") }
    }

    private fun saveGoal(allowRewardedExtraGoal: Boolean) {
        if (_state.value.isMissingExistingGoal) {
            Timber.tag(TAG).w("Blocked save for missing goal edit route: %s", _state.value.requestedGoalId)
            return
        }

        val amount = LocalizedAmountFormatter.parseAmount(_state.value.amount, _state.value.userPreferences.locale) ?: BigDecimal.ZERO
        if (_state.value.name.isBlank() || amount <= BigDecimal.ZERO) {
            _state.update { it.copy(error = "goal_add_error_invalid_name_amount") }
            return
        }

        viewModelScope.launch {
            val isNewGoal = _state.value.goalId == null
            val currentGoalCount = repository.loadGoals().size
            if (isNewGoal &&
                !allowRewardedExtraGoal &&
                !checkEntitlementLimitUseCase.canAddMultipleGoals(currentGoalCount)
            ) {
                _state.update {
                    it.copy(
                        error = "goal_add_error_premium_required",
                        canUseRewardedExtraGoal = true
                    )
                }
                Timber.tag(TAG).w("Blocked extra goal save because Premium is inactive")
                return@launch
            }
            if (allowRewardedExtraGoal) {
                Timber.tag(TAG).d("Saving extra free goal after rewarded ad grant")
            }

            _state.update { it.copy(isLoading = true, error = null, canUseRewardedExtraGoal = false) }
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

    companion object {
        private const val TAG = "GoalAddViewModel"
    }
}
