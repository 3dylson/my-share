package pt.ms.myshare.presentation.ui.dashboard

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
import pt.ms.myshare.domain.model.InvestAmount
import pt.ms.myshare.domain.use_case.GetDashboardDataUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnChipSelected -> onChipSelected(event.id)
            DashboardEvent.OnRefresh -> getDashboardData()
        }
    }

    private fun onChipSelected(id: String) {
        _uiState.update { currentState ->
            val filteredList = filterInvestments(id, currentState.allInvestments)
            currentState.copy(
                selectedChipId = id,
                investments = filteredList
            )
        }
    }

    private fun getDashboardData() {
        dashboardJob?.cancel()
        dashboardJob = getDashboardDataUseCase().onEach { dashboardState ->
            _uiState.update { currentState ->
                val newAllInvestments = dashboardState.investments
                val filteredList = filterInvestments(currentState.selectedChipId, newAllInvestments)
                currentState.copy(
                    allInvestments = newAllInvestments,
                    investments = filteredList,
                    date = dashboardState.date,
                    isLoading = false,
                    error = null
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun filterInvestments(chipId: String, investments: List<InvestAmount>): List<InvestAmount> {
        return when (chipId.lowercase()) {
            "all" -> investments
            "stocks" -> investments.filter { it.category.equals("Stocks", ignoreCase = true) }
            "crypto" -> investments.filter { it.category.equals("Crypto", ignoreCase = true) }
            "savings" -> investments.filter { it.category.equals("Savings", ignoreCase = true) }
            else -> investments
        }
    }
}

sealed class DashboardEvent {
    data class OnChipSelected(val id: String) : DashboardEvent()
    object OnRefresh : DashboardEvent()
}
