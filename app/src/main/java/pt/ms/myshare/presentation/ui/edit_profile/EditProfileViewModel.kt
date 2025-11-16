package pt.ms.myshare.presentation.ui.edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        getEditProfileData()
    }

    private fun getEditProfileData() {
        getEditProfileDataUseCase().onEach {
            _uiState.value = it
        }.launchIn(viewModelScope)
    }

    fun onNetSalaryChange(value: String) {
        // Only allow digits
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(netSalary = digitsOnly) }
    }

    fun onNetSalaryPercentageChange(value: String) {
        _uiState.update { it.copy(netSalaryPercentage = value) }
    }

    fun onStockPercentageChange(value: String) {
        _uiState.update { it.copy(stockPercentage = value) }
    }

    fun onCryptoPercentageChange(value: String) {
        _uiState.update { it.copy(cryptoPercentage = value) }
    }

    fun onSavingsPercentageChange(value: String) {
        _uiState.update { it.copy(savingsPercentage = value) }
    }

    fun onSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            saveEditProfileDataUseCase(
                _uiState.value
            ).onEach { result ->
                _uiState.value = result
            }.launchIn(viewModelScope)
        }
    }
}
