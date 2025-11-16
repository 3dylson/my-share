package pt.ms.myshare.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.InvestAmount
import pt.ms.myshare.domain.repository.UserDataRepository
import pt.ms.myshare.presentation.ui.dashboard.DashboardState
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<DashboardState> {
        return userDataRepository.getUserData().map { userProfile ->
            val netSalary = userProfile.netSalary.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val netSalaryPercentage = userProfile.netSalaryPercentage.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val stockPercentage = userProfile.stockPercentage.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val cryptoPercentage = userProfile.cryptoPercentage.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val savingsPercentage = userProfile.savingsPercentage.toBigDecimalOrNull() ?: BigDecimal.ZERO

            val oneHundred = BigDecimal(100)

            val investmentAmount = netSalary.multiply(netSalaryPercentage).divide(oneHundred)

            val stockAmount = investmentAmount.multiply(stockPercentage).divide(oneHundred)
            val cryptoAmount = investmentAmount.multiply(cryptoPercentage).divide(oneHundred)
            val savingsAmount = investmentAmount.multiply(savingsPercentage).divide(oneHundred)

            val investments = listOf(
                InvestAmount("Stocks", stockAmount, chipIcon = R.drawable.ic_baseline_show_chart),
                InvestAmount("Crypto", cryptoAmount, R.drawable.ic_baseline_currency_bitcoin),
                InvestAmount("Savings", savingsAmount, R.drawable.savings_48px)
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
