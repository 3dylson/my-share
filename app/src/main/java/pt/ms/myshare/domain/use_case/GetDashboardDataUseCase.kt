package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.InvestAmount
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.ui.dashboard.DashboardState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<DashboardState> {
        return userDataRepository.getUserData().map { userProfile ->
            val netSalary = userProfile.netSalary.toFloatOrNull() ?: 0f
            val stockPercentage = userProfile.stockPercentage.toFloatOrNull() ?: 0f
            val cryptoPercentage = userProfile.cryptoPercentage.toFloatOrNull() ?: 0f
            val savingsPercentage = userProfile.savingsPercentage.toFloatOrNull() ?: 0f

            val stockAmount = netSalary * (stockPercentage / 100)
            val cryptoAmount = netSalary * (cryptoPercentage / 100)
            val savingsAmount = netSalary * (savingsPercentage / 100)

            val investments = listOf(
                InvestAmount("Stocks", stockAmount.toString(), chipIcon =  R.drawable.ic_baseline_show_chart),
                InvestAmount("Crypto", cryptoAmount.toString(), R.drawable.ic_baseline_currency_bitcoin),
                InvestAmount("Savings", savingsAmount.toString(), R.drawable.savings_48px)
            )

            val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            DashboardState(
                investments = investments,
                date = currentDate
            )
        }
    }
}
