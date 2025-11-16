package pt.ms.myshare.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import pt.ms.myshare.domain.use_case.GetDashboardDataUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnChipSelected -> onChipSelected(event.id)
            DashboardEvent.OnRefresh -> getDashboardData()
        }
    }

    private fun onChipSelected(id: String) {
        _uiState.update { it.copy(selectedChipId = id) }
    }

    private fun getDashboardData() {
        getDashboardDataUseCase().onEach { dashboardState ->
            _uiState.value = dashboardState
        }.launchIn(viewModelScope)
    }
}

sealed class DashboardEvent {
    data class OnChipSelected(val id: String) : DashboardEvent()
    object OnRefresh : DashboardEvent()
}
