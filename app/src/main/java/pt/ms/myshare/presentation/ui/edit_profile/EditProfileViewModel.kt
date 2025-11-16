package pt.ms.myshare.presentation.ui.edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.domain.use_case.edit_profile.EditProfileUseCase
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val editProfileUseCase: EditProfileUseCase,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        userDataRepository.getUserData().onEach {
            _uiState.value = it
        }.launchIn(viewModelScope)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onNetSalaryChange(netSalary: String) {
        _uiState.value = _uiState.value.copy(netSalary = netSalary)
    }

    fun onNetSalaryPercentageChange(percentage: String) {
        _uiState.value = _uiState.value.copy(netSalaryPercentage = percentage)
    }

    fun onStockPercentageChange(percentage: String) {
        _uiState.value = _uiState.value.copy(stockPercentage = percentage)
    }

    fun onCryptoPercentageChange(percentage: String) {
        _uiState.value = _uiState.value.copy(cryptoPercentage = percentage)
    }

    fun onSavingsPercentageChange(percentage: String) {
        _uiState.value = _uiState.value.copy(savingsPercentage = percentage)
    }

    fun onSave() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = editProfileUseCase.validate(_uiState.value)
            if (result.isValid) {
                userDataRepository.saveUserData(_uiState.value)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.errorMessage)
            }
        }
    }
}
