package pt.ms.myshare.presentation.ui.home

import androidx.annotation.StringRes
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PremiumProofVariant

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
    FirstReview(
        titleRes = R.string.premium_gate_first_review_title,
        bodyRes = R.string.premium_gate_first_review_body,
        analyticsName = "first_review"
    ),
    SmartAutomation(
        titleRes = R.string.premium_gate_automation_title,
        bodyRes = R.string.premium_gate_automation_body,
        analyticsName = "smart_automation"
    );

    @StringRes
    fun bodyResFor(proofVariant: PremiumProofVariant): Int {
        return when {
            this == FirstReview && proofVariant == PremiumProofVariant.PROGRESS_LOOP ->
                R.string.premium_gate_first_review_body_progress_loop
            else -> bodyRes
        }
    }
}
