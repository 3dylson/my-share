package pt.ms.myshare.domain.model

import androidx.annotation.DrawableRes
import pt.ms.myshare.R

data class InvestAmount(
    val category: String,
    val value: String? = "0",
    @DrawableRes
    val chipIcon: Int? = R.drawable.ic_baseline_show_chart
)
