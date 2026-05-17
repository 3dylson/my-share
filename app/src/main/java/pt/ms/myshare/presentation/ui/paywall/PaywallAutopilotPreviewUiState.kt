package pt.ms.myshare.presentation.ui.paywall

data class PaywallAutopilotPreviewUiState(
    val weeklyFlexibleSpend: String? = null,
    val priorityContribution: String? = null,
    val suggestedAdjustmentAmount: String? = null,
    val hasPersonalPlan: Boolean = false
)
