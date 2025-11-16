package pt.ms.myshare.ui.dashboard

import androidx.annotation.StringRes
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.InvestAmount

data class ChipOption(@StringRes val labelRes: Int, val id: String)

data class DashboardState(
    val investments: List<InvestAmount> = emptyList(),
    val chipOptions: List<ChipOption> = listOf(
        ChipOption(R.string.dashboard_label, "Dashboard"),
        ChipOption(R.string.stocks_label, "Stocks"),
        ChipOption(R.string.crypto_label, "Crypto"),
        ChipOption(R.string.savings_label, "Savings")
    ),
    val selectedChipId: String = "Dashboard",
    val date: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
