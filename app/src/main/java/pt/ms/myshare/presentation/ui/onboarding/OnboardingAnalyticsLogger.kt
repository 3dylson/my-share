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

    fun logStarted(
        pricingStrategy: PricingStrategy,
        preferences: UserPreferences,
        onboardingPaywallVariant: String,
        onboardingExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String
    ) {
        FirebaseUtils.logEvent("onboarding_started", Bundle().apply {
            putString("price_cluster", pricingStrategy.marketCluster)
            putString("language", preferences.locale.language)
            putString("onboarding_paywall_variant", onboardingPaywallVariant)
            putString("onboarding_experiment", onboardingExperiment)
            putString("paywall_trial_framing", paywallTrialFraming)
            putString("onboarding_intro_variant", onboardingIntroVariant)
            putString("premium_value_frame", "payday_cycle_proof")
        })
    }

    fun logStepViewed(
        route: String,
        stepIndex: Int,
        setupStepTotal: Int,
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?,
        onboardingPaywallVariant: String,
        onboardingExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String
    ) {
        FirebaseUtils.logEvent(
            "onboarding_step_viewed",
            stepBundle(
                route = route,
                stepIndex = stepIndex,
                setupStepTotal = setupStepTotal,
                focus = focus,
                pricingStrategy = pricingStrategy,
                onboardingPaywallVariant = onboardingPaywallVariant,
                onboardingExperiment = onboardingExperiment,
                paywallTrialFraming = paywallTrialFraming,
                onboardingIntroVariant = onboardingIntroVariant
            )
        )
        Timber.tag(TAG).d("Onboarding step viewed route=%s step=%d/%d", route, stepIndex, setupStepTotal)
    }

    fun logStepCompleted(
        route: String,
        stepIndex: Int,
        setupStepTotal: Int,
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?,
        onboardingPaywallVariant: String,
        onboardingExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String
    ) {
        FirebaseUtils.logEvent(
            "onboarding_step_completed",
            stepBundle(
                route = route,
                stepIndex = stepIndex,
                setupStepTotal = setupStepTotal,
                focus = focus,
                pricingStrategy = pricingStrategy,
                onboardingPaywallVariant = onboardingPaywallVariant,
                onboardingExperiment = onboardingExperiment,
                paywallTrialFraming = paywallTrialFraming,
                onboardingIntroVariant = onboardingIntroVariant
            )
        )
        Timber.tag(TAG).d("Onboarding step completed route=%s step=%d/%d", route, stepIndex, setupStepTotal)
    }

    fun logActivationReached(
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?,
        onboardingExperiment: String? = null,
        paywallTrialFraming: String? = null,
        onboardingIntroVariant: String? = null,
        timeToFirstValueMillis: Long? = null
    ) {
        FirebaseUtils.logEvent("onboarding_activation_reached", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
            onboardingExperiment?.let { putString("onboarding_experiment", it) }
            paywallTrialFraming?.let { putString("paywall_trial_framing", it) }
            onboardingIntroVariant?.let { putString("onboarding_intro_variant", it) }
            timeToFirstValueMillis?.let { putLong("time_to_first_value_ms", it) }
            putString("premium_value_frame", "payday_cycle_proof")
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

    fun logTrajectoryViewed(
        focus: PlanningFocus,
        pricingStrategy: PricingStrategy?,
        onboardingPaywallVariant: String,
        onboardingExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String
    ) {
        FirebaseUtils.logEvent("trajectory_viewed", Bundle().apply {
            putString("selected_focus", focus.analyticsName())
            putString("price_cluster", pricingStrategy?.marketCluster)
            putString("onboarding_paywall_variant", onboardingPaywallVariant)
            putString("onboarding_experiment", onboardingExperiment)
            putString("paywall_trial_framing", paywallTrialFraming)
            putString("onboarding_intro_variant", onboardingIntroVariant)
            putString("premium_value_frame", "payday_cycle_proof")
            putString("decision_frame", "fixed_vs_adaptive")
        })
        Timber.tag(TAG).d(
            "Onboarding trajectory viewed focus=%s experiment=%s trialFraming=%s",
            focus.analyticsName(),
            onboardingExperiment,
            paywallTrialFraming
        )
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
        pricingStrategy: PricingStrategy?,
        onboardingPaywallVariant: String,
        onboardingExperiment: String,
        paywallTrialFraming: String,
        onboardingIntroVariant: String
    ): Bundle = Bundle().apply {
        putString("route", route)
        putInt("step_index", stepIndex)
        putInt("setup_step_total", setupStepTotal)
        putString("selected_focus", focus.analyticsName())
        putString("price_cluster", pricingStrategy?.marketCluster)
        putString("onboarding_paywall_variant", onboardingPaywallVariant)
        putString("onboarding_experiment", onboardingExperiment)
        putString("paywall_trial_framing", paywallTrialFraming)
        putString("onboarding_intro_variant", onboardingIntroVariant)
        putString("premium_value_frame", "payday_cycle_proof")
    }

    private fun PlanningFocus.analyticsName(): String = name.lowercase(Locale.US)

    private companion object {
        const val TAG = "OnboardingAnalytics"
    }
}
