package pt.ms.myshare.presentation.ui.dashboard

import pt.ms.myshare.domain.model.InvestAmount

data class DashboardState(
    val investments: List<InvestAmount> = emptyList(),
    val date: String = ""
)
