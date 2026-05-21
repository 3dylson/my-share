package pt.ms.myshare.domain.model

import androidx.annotation.DrawableRes
import java.math.BigDecimal

data class InvestAmount(
    val category: String,
    val value: BigDecimal,
    @DrawableRes val chipIcon: Int? = null
)
