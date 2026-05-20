package pt.ms.myshare.presentation.ui.onboarding

import android.os.Bundle
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class OnboardingAnalyticsLogger @Inject constructor() {

    fun logStarted(pricingStrategy: PricingStrategy, preferences: UserPreferences) {
        FirebaseUtils.logEvent("onboarding_started", Bundle().apply {
            putString("price_cluster", pricingStrategy.marketCluster)
            putString("language", preferences.locale.language)
        })
    }

    fun logStepViewed(
        route: String,
        stepIndex: Int,
        setupStepTotal: Int,
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?
    ) {
        FirebaseUtils.logEvent("onboarding_step_viewed", stepBundle(route, stepIndex, setupStepTotal, focus, pricingStrategy))
        Timber.tag(TAG).d("Onboarding step viewed route=%s step=%d/%d", route, stepIndex, setupStepTotal)
    }

    fun logStepCompleted(
        route: String,
        stepIndex: Int,
        setupStepTotal: Int,
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?
    ) {
        FirebaseUtils.logEvent("onboarding_step_completed", stepBundle(route, stepIndex, setupStepTotal, focus, pricingStrategy))
        Timber.tag(TAG).d("Onboarding step completed route=%s step=%d/%d", route, stepIndex, setupStepTotal)
    }

    fun logActivationReached(
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?,
        onboardingExperiment: String? = null,
        paywallTrialFraming: String? = null
    ) {
        FirebaseUtils.logEvent("onboarding_activation_reached", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
            onboardingExperiment?.let { putString("onboarding_experiment", it) }
            paywallTrialFraming?.let { putString("paywall_trial_framing", it) }
        })
        Timber.tag(TAG).d("Onboarding activation reached focus=%s", focus.analyticsName())
    }

    fun logAllocationTuneStarted(focus: PlanningFocus, pricingStrategy: PricingStrategy?) {
        FirebaseUtils.logEvent("allocation_tune_started", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
        })
    }

    fun logAllocationTuneCompleted(focus: PlanningFocus, pricingStrategy: PricingStrategy?) {
        FirebaseUtils.logEvent("allocation_tune_completed", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
        })
    }

    fun logReminderPermissionResult(granted: Boolean, focus: PlanningFocus, pricingStrategy: PricingStrategy?) {
        FirebaseUtils.logEvent(if (granted) "reminder_permission_granted" else "reminder_permission_denied", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
        })
    }

    fun logSignupMode(mode: String, focus: PlanningFocus, pricingStrategy: PricingStrategy?) {
        FirebaseUtils.logEvent("onboarding_signup_mode_selected", Bundle().apply {
            putString("signup_mode", mode)
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
        })
    }

    private fun stepBundle(
        route: String,
        stepIndex: Int,
        setupStepTotal: Int,
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?
    ): Bundle = Bundle().apply {
        putString("route", route)
        putInt("step_index", stepIndex)
        putInt("setup_step_total", setupStepTotal)
        putString("selected_focus", focus.analyticsName())
        putString("price_cluster", pricingStrategy?.marketCluster)
    }

    private fun PlanningFocus.analyticsName(): String = name.lowercase(Locale.US)

    private companion object {
        const val TAG = "OnboardingAnalytics"
    }
}
