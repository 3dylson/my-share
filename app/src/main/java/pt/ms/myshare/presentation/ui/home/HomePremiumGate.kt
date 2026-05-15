package pt.ms.myshare.presentation.ui.home

import androidx.annotation.StringRes
import pt.ms.myshare.R

enum class HomePremiumGate(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
) {
    General(
        titleRes = R.string.premium_gate_general_title,
        bodyRes = R.string.premium_gate_general_body
    ),
    MultipleGoals(
        titleRes = R.string.premium_gate_goals_title,
        bodyRes = R.string.premium_gate_goals_body
    ),
    MultipleRules(
        titleRes = R.string.premium_gate_rules_title,
        bodyRes = R.string.premium_gate_rules_body
    ),
    ReviewHistory(
        titleRes = R.string.premium_gate_review_title,
        bodyRes = R.string.premium_gate_review_body
    ),
    SmartAutomation(
        titleRes = R.string.premium_gate_automation_title,
        bodyRes = R.string.premium_gate_automation_body
    )
}
