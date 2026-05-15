package pt.ms.myshare.presentation.ui.home

import androidx.annotation.StringRes
import pt.ms.myshare.R

enum class HomePremiumGate(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val analyticsName: String
) {
    General(
        titleRes = R.string.premium_gate_general_title,
        bodyRes = R.string.premium_gate_general_body,
        analyticsName = "general"
    ),
    MultipleGoals(
        titleRes = R.string.premium_gate_goals_title,
        bodyRes = R.string.premium_gate_goals_body,
        analyticsName = "multiple_goals"
    ),
    MultipleRules(
        titleRes = R.string.premium_gate_rules_title,
        bodyRes = R.string.premium_gate_rules_body,
        analyticsName = "multiple_rules"
    ),
    ReviewHistory(
        titleRes = R.string.premium_gate_review_title,
        bodyRes = R.string.premium_gate_review_body,
        analyticsName = "review_history"
    ),
    SmartAutomation(
        titleRes = R.string.premium_gate_automation_title,
        bodyRes = R.string.premium_gate_automation_body,
        analyticsName = "smart_automation"
    )
}
