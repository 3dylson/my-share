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
import pt.ms.myshare.domain.model.InvestAmount
import pt.ms.myshare.domain.use_case.GetDashboardDataUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()
    private var allInvestments: List<InvestAmount> = emptyList()

    init {
        getDashboardData()
    }

    private fun getDashboardData() {
        getDashboardDataUseCase().onEach { result ->
            allInvestments = result.investments
            _uiState.update {
                it.copy(
                    investments = filterInvestments(it.selectedChipId, allInvestments),
                    date = result.date,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onChipSelected(chipId: String) {
        _uiState.update {
            it.copy(
                selectedChipId = chipId,
                investments = filterInvestments(chipId, allInvestments)
            )
        }
    }

    private fun filterInvestments(chipId: String, investments: List<InvestAmount>): List<InvestAmount> {
        return when (chipId) {
            "Dashboard" -> investments
            else -> investments.filter { it.category == chipId }
        }
    }
}
