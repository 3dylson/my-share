package pt.ms.myshare.domain.model

import java.math.BigDecimal
import java.time.YearMonth

data class PlanPreview(
    val perPaydayAmounts: PerPaydayAmounts,
    val totalPerMonth: BigDecimal,
    val goalTargetDate: YearMonth?
)

data class PerPaydayAmounts(
    val stocks: BigDecimal,
    val crypto: BigDecimal,
    val savings: BigDecimal
)
