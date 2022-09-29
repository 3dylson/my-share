package pt.ms.myshare.data

import pt.ms.myshare.utils.StringUtils

data class InvestAmount(
    val category: String,
    val value: String? = StringUtils.ZERO
)
