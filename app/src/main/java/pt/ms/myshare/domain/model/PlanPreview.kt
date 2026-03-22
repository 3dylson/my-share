package pt.ms.myshare.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class PlanPreview(
    val incomePerPayday: BigDecimal,
    val fixedCostsPerPayday: BigDecimal,
    val flexibleSpendPerPayday: BigDecimal,
    val savingsPerPayday: BigDecimal,
    val investingPerPayday: BigDecimal,
    val cryptoPerPayday: BigDecimal,
    val debtPerPayday: BigDecimal,
    val weeklyFlexibleSpend: BigDecimal,
    val monthlyGoalContribution: BigDecimal,
    val nextPayday: LocalDate,
    val goalTargetDate: YearMonth?,
    val summary: String
)
