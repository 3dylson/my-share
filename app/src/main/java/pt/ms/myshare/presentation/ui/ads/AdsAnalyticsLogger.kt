package pt.ms.myshare.presentation.ui.ads

import android.os.Bundle
import pt.ms.myshare.domain.model.AdEligibilityDecision
import pt.ms.myshare.domain.model.AdPlacement
import pt.ms.myshare.domain.model.AdsExperimentVariant
import pt.ms.myshare.utils.logs.FirebaseUtils

class AdsAnalyticsLogger {

    fun eligibility(
        decision: AdEligibilityDecision,
        placement: AdPlacement,
        variant: AdsExperimentVariant,
        sessionCount: Int,
        isPremium: Boolean
    ) {
        val eventName = if (decision.isEligible) "ad_eligible" else "ad_ineligible"
        log(
            eventName = eventName,
            placement = placement,
            variant = variant,
            sessionCount = sessionCount,
            isPremium = isPremium,
            reason = decision.reason
        )
    }

    fun event(
        eventName: String,
        placement: AdPlacement,
        variant: AdsExperimentVariant,
        sessionCount: Int,
        isPremium: Boolean,
        reason: String? = null
    ) {
        log(eventName, placement, variant, sessionCount, isPremium, reason)
    }

    private fun log(
        eventName: String,
        placement: AdPlacement,
        variant: AdsExperimentVariant,
        sessionCount: Int,
        isPremium: Boolean,
        reason: String? = null
    ) {
        FirebaseUtils.logEvent(
            eventName,
            Bundle().apply {
                putString("placement", placement.analyticsName)
                putString("format", placement.format.analyticsName)
                putString("variant", variant.remoteValue)
                putInt("session_count", sessionCount)
                putString("reason", reason.orEmpty())
                putBoolean("is_premium", isPremium)
            }
        )
    }
}
