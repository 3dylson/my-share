package pt.ms.myshare.presentation.ui.edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.use_case.edit_profile.GetEditProfileDataUseCase
import pt.ms.myshare.domain.use_case.edit_profile.SaveEditProfileDataUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getEditProfileDataUseCase: GetEditProfileDataUseCase,
    private val saveEditProfileDataUseCase: SaveEditProfileDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    private var getDataJob: Job? = null

    init {
        getEditProfileData()
    }

    private fun getEditProfileData() {
        Timber.d("getEditProfileData() called")
        getDataJob?.cancel()
        getDataJob = getEditProfileDataUseCase().onEach {
            _uiState.value = it
            Timber.d("getEditProfileData() success: $it")
        }.launchIn(viewModelScope)
    }

    fun onNetSalaryChange(value: String) {
        Timber.d("onNetSalaryChange() called with: $value")
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(netSalary = digitsOnly) }
    }

    fun onNetSalaryPercentageChange(value: String) {
        Timber.d("onNetSalaryPercentageChange() called with: $value")
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            _uiState.update { it.copy(netSalaryPercentage = "", netSalaryPercentageError = null) }
            return
        }
        val intValue = digitsOnly.toIntOrNull() ?: 0
        if (intValue > 100) {
            _uiState.update { it.copy(netSalaryPercentageError = "Cannot exceed 100%") }
        } else {
            _uiState.update { it.copy(netSalaryPercentage = intValue.toString(), netSalaryPercentageError = null) }
        }
    }

    fun onStockPercentageChange(value: String) {
        Timber.d("onStockPercentageChange() called with: $value")
        handlePercentageChange(value, "stock")
    }

    fun onCryptoPercentageChange(value: String) {
        Timber.d("onCryptoPercentageChange() called with: $value")
        handlePercentageChange(value, "crypto")
    }

    fun onSavingsPercentageChange(value: String) {
        Timber.d("onSavingsPercentageChange() called with: $value")
        handlePercentageChange(value, "savings")
    }

    private fun handlePercentageChange(value: String, field: String) {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            _uiState.update {
                when (field) {
                    "stock" -> it.copy(stockPercentage = "", stockPercentageError = null)
                    "crypto" -> it.copy(cryptoPercentage = "", cryptoPercentageError = null)
                    "savings" -> it.copy(savingsPercentage = "", savingsPercentageError = null)
                    else -> it
                }
            }
            return
        }

        val intValue = digitsOnly.toIntOrNull() ?: 0
        val stockValue = _uiState.value.stockPercentage.toIntOrNull() ?: 0
        val cryptoValue = _uiState.value.cryptoPercentage.toIntOrNull() ?: 0
        val savingsValue = _uiState.value.savingsPercentage.toIntOrNull() ?: 0

        val maxAllowed = 100 - when (field) {
            "stock" -> cryptoValue + savingsValue
            "crypto" -> stockValue + savingsValue
            "savings" -> stockValue + cryptoValue
            else -> 0
        }

        if (intValue > maxAllowed) {
            val error = "Cannot exceed $maxAllowed%"
            _uiState.update {
                when (field) {
                    "stock" -> it.copy(stockPercentageError = error)
                    "crypto" -> it.copy(cryptoPercentageError = error)
                    "savings" -> it.copy(savingsPercentageError = error)
                    else -> it
                }
            }
        } else {
            _uiState.update {
                when (field) {
                    "stock" -> it.copy(stockPercentage = intValue.toString(), stockPercentageError = null)
                    "crypto" -> it.copy(cryptoPercentage = intValue.toString(), cryptoPercentageError = null)
                    "savings" -> it.copy(savingsPercentage = intValue.toString(), savingsPercentageError = null)
                    else -> it
                }
            }
        }
    }

    fun onSave() {
        Timber.d("onSave() called")
        val stock = _uiState.value.stockPercentage.toIntOrNull() ?: 0
        val crypto = _uiState.value.cryptoPercentage.toIntOrNull() ?: 0
        val savings = _uiState.value.savingsPercentage.toIntOrNull() ?: 0

        if (stock + crypto + savings > 100) {
            _uiState.update { it.copy(error = "Investment percentages cannot add up to more than 100%") }
            Timber.w("onSave() failed: Investment percentages add up to more than 100%")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            saveEditProfileDataUseCase(
                _uiState.value
            ).onEach { result ->
                _uiState.value = result
                Timber.d("onSave() success: $result")
            }.launchIn(viewModelScope)
        }
    }
}
