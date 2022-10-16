package pt.ms.myshare.data

import androidx.annotation.DrawableRes
import pt.ms.myshare.R
import pt.ms.myshare.utils.StringUtils

data class InvestAmount(
    val category: String,
    val value: String? = StringUtils.ZERO,
    @DrawableRes
    val chipIcon: Int? = R.drawable.ic_baseline_show_chart
)
