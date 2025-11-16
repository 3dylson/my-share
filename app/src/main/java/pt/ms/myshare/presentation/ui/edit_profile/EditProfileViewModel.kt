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
        getDataJob?.cancel()
        getDataJob = getEditProfileDataUseCase().onEach {
            _uiState.value = it
        }.launchIn(viewModelScope)
    }

    fun onNetSalaryChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(netSalary = digitsOnly) }
    }

    fun onNetSalaryPercentageChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            _uiState.update { it.copy(netSalaryPercentage = "") }
            return
        }
        val intValue = digitsOnly.toIntOrNull() ?: 0
        val validatedValue = if (intValue > 100) 100 else intValue
        _uiState.update { it.copy(netSalaryPercentage = validatedValue.toString()) }
    }

    fun onStockPercentageChange(value: String) {
        handlePercentageChange(value, "stock")
    }

    fun onCryptoPercentageChange(value: String) {
        handlePercentageChange(value, "crypto")
    }

    fun onSavingsPercentageChange(value: String) {
        handlePercentageChange(value, "savings")
    }

    private fun handlePercentageChange(value: String, field: String) {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            updatePercentageField(field, "", null)
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
            updatePercentageField(field, _uiState.value.getPercentageForField(field), error)
        } else {
            updatePercentageField(field, digitsOnly, null)
        }
    }

    private fun updatePercentageField(field: String, value: String, error: String?) {
        _uiState.update {
            when (field) {
                "stock" -> it.copy(stockPercentage = value, stockPercentageError = error, cryptoPercentageError = null, savingsPercentageError = null)
                "crypto" -> it.copy(cryptoPercentage = value, cryptoPercentageError = error, stockPercentageError = null, savingsPercentageError = null)
                "savings" -> it.copy(savingsPercentage = value, savingsPercentageError = error, stockPercentageError = null, cryptoPercentageError = null)
                else -> it
            }
        }
    }

    private fun EditProfileState.getPercentageForField(field: String): String {
        return when (field) {
            "stock" -> this.stockPercentage
            "crypto" -> this.cryptoPercentage
            "savings" -> this.savingsPercentage
            else -> ""
        }
    }

    fun onSave() {
        val stock = _uiState.value.stockPercentage.toIntOrNull() ?: 0
        val crypto = _uiState.value.cryptoPercentage.toIntOrNull() ?: 0
        val savings = _uiState.value.savingsPercentage.toIntOrNull() ?: 0

        if (stock + crypto + savings != 100) {
            _uiState.update { it.copy(error = "Investment percentages must add up to 100%") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            saveEditProfileDataUseCase(
                _uiState.value
            ).onEach { result ->
                _uiState.value = result
            }.launchIn(viewModelScope)
        }
    }
}
