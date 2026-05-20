package pt.ms.myshare.presentation.ui.onboarding

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.BillingFlowLaunchResult
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.BillingPurchaseEvent
import pt.ms.myshare.domain.model.Goal
import pt.ms.myshare.domain.model.GoalType
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.domain.model.PremiumSubscriptionProducts
import pt.ms.myshare.domain.model.PremiumStoreProductSelector
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.model.ReminderCadence
import pt.ms.myshare.domain.model.ReminderConfiguration
import pt.ms.myshare.domain.model.SalaryPlan
import pt.ms.myshare.domain.model.PaydayRule
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.domain.repository.AuthRepository
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.domain.repository.PlannerRepository
import pt.ms.myshare.domain.repository.ProductConfigRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.domain.use_case.CalculatePlanPreviewUseCase
import pt.ms.myshare.domain.use_case.ResolveAllocationStrategyRulesUseCase
import pt.ms.myshare.domain.use_case.ResolvePricingStrategyUseCase
import pt.ms.myshare.presentation.ui.localization.UserLocaleManager
import pt.ms.myshare.presentation.ui.paywall.BillingStatusMessageKeys
import pt.ms.myshare.presentation.ui.paywall.BillingStatusMessageMapper
import pt.ms.myshare.utils.logs.FirebasePerformanceUtils
import pt.ms.myshare.utils.logs.FirebasePerformanceUtils.putMetricSafely
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val entitlementRepository: EntitlementRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calculatePlanPreviewUseCase: CalculatePlanPreviewUseCase,
    private val resolveAllocationStrategyRulesUseCase: ResolveAllocationStrategyRulesUseCase,
    private val resolvePricingStrategyUseCase: ResolvePricingStrategyUseCase,
    private val reminderWorkScheduler: ReminderWorkScheduler,
    private val userLocaleManager: UserLocaleManager,
    private val onboardingAnalyticsLogger: OnboardingAnalyticsLogger,
    private val productConfigRepository: ProductConfigRepository
) : ViewModel() {

    private val state = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = state.asStateFlow()
    private var activationLogged = false
    private var currentProductConfig = ProductExperienceConfig()

    init {
        val completed = plannerRepository.isOnboardingCompleted()
        val preferences = userPreferencesRepository.loadPreferences()
        userLocaleManager.apply(preferences)
        val pricing = resolvePricingStrategyUseCase.execute(preferences.locale)
        state.update {
            it.copy(
                onboardingCompleted = completed,
                userPreferences = preferences,
                pricingStrategy = pricing,
                selectedBillingPlan = pricing.heroPlan
            )
        }
        if (!completed) {
            onboardingAnalyticsLogger.logStarted(pricing, preferences)
        }
        viewModelScope.launch {
            productConfigRepository.refresh()
        }
        viewModelScope.launch {
            productConfigRepository.config.collect { productConfig ->
                currentProductConfig = productConfig
                state.update { current ->
                    val currentPricing = current.pricingStrategy ?: pricing
                    current.copy(
                        onboardingPaywallVariant = productConfig.onboardingPaywallVariant,
                        onboardingConversionExperiment = productConfig.onboardingConversionExperiment,
                        paywallTrialFraming = productConfig.paywallTrialFraming,
                        selectedBillingPlan = if (current.hasUserSelectedBillingPlan) {
                            current.selectedBillingPlan
                        } else {
                            productConfig.paywallDefaultPlan.resolve(currentPricing.heroPlan)
                        }
                    )
                }
                FirebaseUtils.logEvent("product_config_applied", Bundle().apply {
                    putString("screen", "onboarding")
                    putString("paywall_default_plan", productConfig.paywallDefaultPlan.name.lowercase(Locale.US))
                    putString("onboarding_paywall_variant", productConfig.onboardingPaywallVariant.remoteValue)
                    putString("onboarding_experiment", productConfig.onboardingConversionExperiment)
                    putString("paywall_trial_framing", productConfig.paywallTrialFraming.remoteValue)
                    putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
                })
                Timber.tag(TAG).d(
                    "Onboarding product config applied paywallDefault=%s variant=%s",
                    productConfig.paywallDefaultPlan,
                    productConfig.onboardingPaywallVariant.remoteValue
                )
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observePreferences().collect { updatedPreferences ->
                applyPreferenceState(updatedPreferences, shouldApplyLocale = true)
                Timber.tag(TAG).d(
                    "Onboarding preferences updated language=%s currency=%s",
                    updatedPreferences.languageTag,
                    updatedPreferences.currencyCode
                )
            }
        }
        viewModelScope.launch {
            entitlementRepository.isPro.collect { isPremium ->
                state.update { current ->
                    current.copy(
                        isPremium = isPremium,
                        shouldSecurePremiumAccess = current.shouldSecurePremiumAccess ||
                            (isPremium && current.isAnonymousUser && !current.hasDismissedSecurePremiumAccessPrompt)
                    )
                }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                val isAnonymous = user?.isAnonymous == true
                state.update { current ->
                    current.copy(
                        isAnonymousUser = isAnonymous,
                        shouldSecurePremiumAccess = current.shouldSecurePremiumAccess ||
                            (current.isPremium && isAnonymous && !current.hasDismissedSecurePremiumAccessPrompt)
                    )
                }
            }
        }
        // Collect live Play Billing products so the paywall always shows
        // real, country-correct prices from the Play Store instead of locale estimates.
        viewModelScope.launch {
            entitlementRepository.availableProducts.collect { products ->
                Timber.d("OnboardingVM: received ${products.size} live billing products")
                state.update { it.copy(availableProducts = products) }
            }
        }
        viewModelScope.launch {
            entitlementRepository.purchaseEvents.collect { event ->
                val messageKey = BillingStatusMessageMapper.fromPurchaseEvent(event)
                var shouldLogSecurePrompt = false
                if (event == BillingPurchaseEvent.Completed) {
                    enablePremiumWatchForOnboarding("purchase")
                }
                state.update {
                    val shouldShowSecurePrompt = event == BillingPurchaseEvent.Completed &&
                        it.isAnonymousUser &&
                        !it.hasDismissedSecurePremiumAccessPrompt &&
                        !it.shouldSecurePremiumAccess
                    shouldLogSecurePrompt = shouldShowSecurePrompt
                    it.copy(
                        isBillingActionInProgress = false,
                        billingMessage = messageKey,
                        shouldSecurePremiumAccess = it.shouldSecurePremiumAccess || shouldShowSecurePrompt
                    )
                }
                if (shouldLogSecurePrompt) {
                    FirebaseUtils.logEvent("premium_account_prompt_shown", Bundle().apply {
                        putString("source", "onboarding_paywall")
                    })
                    Timber.tag(TAG).d("Premium account prompt shown from onboarding purchase event")
                }
                logBillingPurchaseEvent(event, "onboarding_paywall")
            }
        }
    }


    fun setFocus(focus: PlanningFocus, defaultGoalName: String, defaultGoalAmount: BigDecimal) {
        state.update {
            it.copy(
                selectedFocus = focus,
                goalName = defaultGoalName,
                goalAmount = defaultGoalAmount
            )
        }
    }

    fun setGoal(goalName: String, goalAmount: BigDecimal) {
        state.update { it.copy(goalName = goalName, goalAmount = goalAmount) }
    }

    fun logSetupStepViewed(route: OnboardingRoute, stepIndex: Int, stepTotal: Int = SETUP_STEP_TOTAL) {
        val current = state.value
        onboardingAnalyticsLogger.logStepViewed(
            route = route.route,
            stepIndex = stepIndex,
            setupStepTotal = stepTotal,
            focus = current.selectedFocus,
            pricingStrategy = current.pricingStrategy
        )
    }

    fun logSetupStepCompleted(route: OnboardingRoute, stepIndex: Int, stepTotal: Int = SETUP_STEP_TOTAL) {
        val current = state.value
        onboardingAnalyticsLogger.logStepCompleted(
            route = route.route,
            stepIndex = stepIndex,
            setupStepTotal = stepTotal,
            focus = current.selectedFocus,
            pricingStrategy = current.pricingStrategy
        )
    }

    fun logActivationReached() {
        if (activationLogged) return
        activationLogged = true
        val current = state.value
        onboardingAnalyticsLogger.logActivationReached(
            focus = current.selectedFocus,
            pricingStrategy = current.pricingStrategy,
            onboardingExperiment = current.onboardingConversionExperiment,
            paywallTrialFraming = current.paywallTrialFraming.remoteValue
        )
    }

    fun logAllocationTuneStarted() {
        val current = state.value
        onboardingAnalyticsLogger.logAllocationTuneStarted(current.selectedFocus, current.pricingStrategy)
    }

    fun logAllocationTuneCompleted() {
        val current = state.value
        onboardingAnalyticsLogger.logAllocationTuneCompleted(current.selectedFocus, current.pricingStrategy)
    }

    fun logReminderPermissionResult(granted: Boolean) {
        val current = state.value
        onboardingAnalyticsLogger.logReminderPermissionResult(granted, current.selectedFocus, current.pricingStrategy)
    }

    fun setSalaryDetails(
        incomePerPayday: BigDecimal,
        payFrequency: PayFrequency,
        monthlyPayday: Int,
        nextBiweeklyPaydayText: String
    ) {
        state.update {
            it.copy(
                netIncomePerPayday = incomePerPayday,
                payFrequency = payFrequency,
                monthlyPayday = monthlyPayday,
                nextBiweeklyPaydayText = nextBiweeklyPaydayText,
                error = null
            )
        }
    }

    fun setFixedCostsAndBuild(
        monthlyFixedCosts: BigDecimal,
        preset: AllocationPreset,
        strategy: AllocationStrategy = AllocationStrategy.BALANCED_SAVINGS,
        customStrategyName: String? = null
    ): Boolean {
        val cleanCustomStrategyName = customStrategyName
            ?.trim()
            ?.takeIf { strategy == AllocationStrategy.CUSTOM && it.isNotBlank() }
        val income = state.value.netIncomePerPayday
        if (income != null && monthlyFixedCosts > income) {
            Timber.tag(TAG).d(
                "Blocked onboarding fixed costs greater than income: fixedCosts=%s income=%s",
                monthlyFixedCosts,
                income
            )
            state.update {
                it.copy(
                    monthlyFixedCosts = monthlyFixedCosts,
                    preset = preset,
                    strategy = strategy,
                    customStrategyName = cleanCustomStrategyName.orEmpty(),
                    error = FIXED_COSTS_EXCEED_INCOME_ERROR,
                    planSaved = false
                )
            }
            return false
        }

        state.update {
            it.copy(
                monthlyFixedCosts = monthlyFixedCosts,
                preset = preset,
                strategy = strategy,
                customStrategyName = cleanCustomStrategyName.orEmpty(),
                error = null
            )
        }
        return buildPreview()
    }

    fun setAllocationsAndBuild(
        flexibleSpend: BigDecimal,
        savings: BigDecimal,
        investing: BigDecimal,
        crypto: BigDecimal,
        debt: BigDecimal = BigDecimal.ZERO,
        isPercentage: Boolean
    ): Boolean {
        Timber.tag(TAG).d(
            "Set onboarding allocations: isPercentage=%s flexible=%s savings=%s investing=%s crypto=%s debt=%s",
            isPercentage,
            flexibleSpend,
            savings,
            investing,
            crypto,
            debt
        )
        state.update {
            it.copy(
                allocatedFlexibleSpend = flexibleSpend,
                allocatedSavings = savings,
                allocatedInvesting = investing,
                allocatedCrypto = crypto,
                allocatedDebt = debt,
                allocationIsPercentage = isPercentage
            )
        }
        return buildPreview()
    }

    fun buildPreview(): Boolean {
        val current = state.value
        val income = current.netIncomePerPayday ?: return false
        val fixedCosts = current.monthlyFixedCosts ?: return false
        val plan = buildPlan(current, income, fixedCosts) ?: return false
        val preview = calculatePlanPreviewUseCase.execute(plan, current.goalAmount)
        state.update { it.copy(planPreview = preview, error = null, planSaved = true) }
        viewModelScope.launch {
            plannerRepository.savePlan(plan)
            // Persist rules: clear stale entries then write each onboarding-generated rule
            plannerRepository.loadRules().forEach { plannerRepository.deleteRule(it.id) }
            plan.rules.forEach { plannerRepository.saveRule(it) }
            plannerRepository.saveGoal(
                Goal(
                    id = current.onboardingGoalId,
                    targetAmount = current.goalAmount,
                    type = when(current.selectedFocus) {
                        PlanningFocus.SAVE_WITHOUT_STRESS -> GoalType.EMERGENCY_FUND
                        PlanningFocus.INVEST_WITH_DISCIPLINE -> GoalType.INVEST_TARGET
                        else -> GoalType.CUSTOM
                    },
                    name = current.goalName
                )
            )
            FirebaseUtils.logEvent("create_plan_completed", Bundle().apply {
                putString("country_cluster", current.pricingStrategy?.marketCluster)
                putString("language", current.userPreferences.locale.language)
            })
        }
        return true
    }

    fun setReminderSaved() {
        state.update { it.copy(reminderSaved = true) }
    }

    fun setSelectedBillingPlan(plan: BillingPlan) {
        state.update {
            it.copy(
                selectedBillingPlan = plan,
                hasUserSelectedBillingPlan = true,
                billingMessage = null
            )
        }
        FirebaseUtils.logEvent("paywall_plan_selected", Bundle().apply {
            putString("billing_plan", plan.name.lowercase(Locale.US))
            putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            putString("source", "onboarding_paywall")
            putString("onboarding_paywall_variant", state.value.onboardingPaywallVariant.remoteValue)
            putString("onboarding_experiment", state.value.onboardingConversionExperiment)
            putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
            putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
        })
    }

    fun updateLanguage(languageTag: String) {
        val updated = state.value.userPreferences.copy(languageTag = languageTag)
        applyPreferenceState(updated, shouldApplyLocale = false)
        viewModelScope.launch {
            userPreferencesRepository.savePreferences(updated)
            userLocaleManager.apply(userPreferencesRepository.loadPreferences())
        }
    }

    fun updateCurrency(currencyCode: String) {
        val updated = state.value.userPreferences.copy(currencyCode = currencyCode)
        applyPreferenceState(updated, shouldApplyLocale = false)
        viewModelScope.launch {
            userPreferencesRepository.savePreferences(updated)
        }
    }

    private fun applyPreferenceState(
        preferences: pt.ms.myshare.domain.model.UserPreferences,
        shouldApplyLocale: Boolean
    ) {
        if (shouldApplyLocale) {
            userLocaleManager.apply(preferences)
        }
        val updatedPricing = resolvePricingStrategyUseCase.execute(preferences.locale)
        state.update { current ->
            current.copy(
                userPreferences = preferences,
                pricingStrategy = updatedPricing,
                selectedBillingPlan = if (current.hasUserSelectedBillingPlan) {
                    current.selectedBillingPlan
                } else {
                    currentProductConfig.paywallDefaultPlan.resolve(updatedPricing.heroPlan)
                }
            )
        }
    }

    fun purchasePremium(activity: android.app.Activity) {
        val storeProductId = PremiumSubscriptionProducts.productIdFor(state.value.selectedBillingPlan)
        viewModelScope.launch {
            FirebasePerformanceUtils.traceSuspend(
                name = "onboarding_purchase_launch",
                attributes = mapOf(
                    "billing_plan" to state.value.selectedBillingPlan.name.lowercase(Locale.US),
                    "paywall_variant" to state.value.onboardingPaywallVariant.remoteValue,
                    "onboarding_experiment" to state.value.onboardingConversionExperiment,
                    "trial_framing" to state.value.paywallTrialFraming.remoteValue
                )
            ) { trace ->
                state.update {
                    it.copy(
                        isBillingActionInProgress = true,
                        billingMessage = BillingStatusMessageKeys.STARTING
                    )
                }
                val products = entitlementRepository.availableProducts.first()
                val product = PremiumStoreProductSelector.standardProduct(
                    products = products,
                    plan = state.value.selectedBillingPlan
                )
                if (product == null) {
                    trace?.putMetricSafely("product_available", 0L)
                    state.update {
                        it.copy(
                            isBillingActionInProgress = false,
                            billingMessage = BillingStatusMessageKeys.PRODUCTS_UNAVAILABLE
                        )
                    }
                    FirebaseUtils.logEvent("purchase_unavailable", Bundle().apply {
                        putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
                        putString("price_cluster", state.value.pricingStrategy?.marketCluster)
                        putString("product_id", storeProductId)
                        putString("source", "onboarding_paywall")
                        putString("onboarding_paywall_variant", state.value.onboardingPaywallVariant.remoteValue)
                        putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                        putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                        putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
                    })
                    Timber.tag("OnboardingBilling").e("Cannot purchase: Product %s not found in store", storeProductId)
                    return@traceSuspend
                }
                trace?.putMetricSafely("product_available", 1L)
                FirebaseUtils.logEvent("purchase_started", Bundle().apply {
                    putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
                    putString("price_cluster", state.value.pricingStrategy?.marketCluster)
                    putString("product_id", product.productId)
                    putBoolean("has_trial", product.hasFreeTrial)
                    putString("source", "onboarding_paywall")
                    putString("onboarding_paywall_variant", state.value.onboardingPaywallVariant.remoteValue)
                    putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                    putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                    putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
                })
                val launchResult = entitlementRepository.purchasePlan(activity, product)
                state.update {
                    it.copy(
                        isBillingActionInProgress = false,
                        billingMessage = BillingStatusMessageMapper.fromLaunchResult(launchResult)
                    )
                }
                logBillingLaunchResult(launchResult, product.productId, "onboarding_paywall")
            }
        }
    }

    fun connectGoogleAccount(idToken: String) {
        viewModelScope.launch {
            state.update {
                it.copy(
                    isGoogleConnectionInProgress = true,
                    googleConnectionMessage = null,
                    googleConnectionError = null
                )
            }

            val result = authRepository.connectGoogleAccount(idToken)
            result.fold(
                onSuccess = { user ->
                    plannerRepository.syncLocalStateIfAuthenticated()
                    userPreferencesRepository.syncFromFirestore()
                    state.update {
                        it.copy(
                            isAnonymousUser = user.isAnonymous,
                            shouldSecurePremiumAccess = it.isPremium &&
                                user.isAnonymous &&
                                !it.hasDismissedSecurePremiumAccessPrompt,
                            isGoogleConnectionInProgress = false,
                            googleConnectionMessage = "home_more_account_connect_google_success",
                            googleConnectionError = null
                        )
                    }
                    FirebaseUtils.logEvent("google_account_connected", Bundle().apply {
                        putString("source", "onboarding_paywall")
                    })
                    Timber.tag(TAG).d("Google account connected from onboarding paywall")
                },
                onFailure = { throwable ->
                    state.update {
                        it.copy(
                            isGoogleConnectionInProgress = false,
                            googleConnectionMessage = null,
                            googleConnectionError = "home_more_account_connect_google_error_generic"
                        )
                    }
                    FirebaseUtils.logEvent("google_account_connect_failed", Bundle().apply {
                        putString("source", "onboarding_paywall")
                    })
                    Timber.tag(TAG).e(throwable, "Google account connection failed from onboarding paywall")
                }
            )
        }
    }

    fun setGoogleConnectionCredentialError(errorKey: String) {
        state.update {
            it.copy(
                isGoogleConnectionInProgress = false,
                googleConnectionMessage = null,
                googleConnectionError = errorKey
            )
        }
    }

    fun dismissSecurePremiumAccessPrompt() {
        state.update {
            it.copy(
                shouldSecurePremiumAccess = false,
                hasDismissedSecurePremiumAccessPrompt = true
            )
        }
        FirebaseUtils.logEvent("premium_account_link_skipped", Bundle().apply {
            putString("source", "onboarding_paywall")
        })
        Timber.tag(TAG).d("Premium account link prompt skipped from onboarding paywall")
    }


    fun signInWithGoogle(idToken: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            state.update { it.copy(isSignupActionInProgress = true, error = null) }
            val result = authRepository.signInWithGoogle(idToken)
            
            if (result.isSuccess) {
                plannerRepository.syncLocalStateIfAuthenticated()
                userPreferencesRepository.syncFromFirestore()
                onboardingAnalyticsLogger.logSignupMode("google", state.value.selectedFocus, state.value.pricingStrategy)
                FirebaseUtils.logEvent("login_success")
                onComplete()
            } else {
                FirebaseUtils.logEvent("login_failed")
                Timber.tag(TAG).e(result.exceptionOrNull(), "Google sign-in failed")
                state.update {
                    it.copy(
                        isSignupActionInProgress = false,
                        error = "error_authentication_failed"
                    )
                }
            }
        }
    }

    fun continueLocally(onComplete: () -> Unit) {
        viewModelScope.launch {
            state.update { it.copy(isSignupActionInProgress = true, error = null) }
            onboardingAnalyticsLogger.logSignupMode("local", state.value.selectedFocus, state.value.pricingStrategy)
            FirebaseUtils.logEvent("local_mode_selected")
            Timber.tag(TAG).d("Continuing onboarding in local-only mode")
            yield()
            onComplete()
        }
    }


    fun skipToHomeWithDefaultPlan() {
        state.update {
            it.copy(
                netIncomePerPayday = BigDecimal("1500"),
                payFrequency = PayFrequency.MONTHLY,
                monthlyPayday = 1,
                monthlyFixedCosts = BigDecimal("600"),
                preset = AllocationPreset.BALANCED,
                strategy = AllocationStrategy.BALANCED_SAVINGS,
                customStrategyName = "",
                allocatedFlexibleSpend = BigDecimal("400"),
                allocatedSavings = BigDecimal("300"),
                allocatedInvesting = BigDecimal("200"),
                allocatedCrypto = BigDecimal.ZERO,
                allocatedDebt = BigDecimal.ZERO,
                allocationIsPercentage = false,
                goalName = "",
                goalAmount = BigDecimal("5000"),
                planSaved = true,
                reminderSkipped = true
            )
        }
        viewModelScope.launch {
            val plan = SalaryPlan(
                focus = PlanningFocus.SAVE_WITHOUT_STRESS,
                netIncomePerPayday = BigDecimal("1500"),
                monthlyFixedCosts = BigDecimal("600"),
                payFrequency = PayFrequency.MONTHLY,
                monthlyPayday = 1,
                preset = AllocationPreset.BALANCED,
                strategy = AllocationStrategy.BALANCED_SAVINGS,
                customStrategyName = null
            )
            plannerRepository.savePlan(plan)
            plannerRepository.saveGoal(
                Goal(
                    name = "",
                    targetAmount = BigDecimal("5000"),
                    type = GoalType.EMERGENCY_FUND
                )
            )
            plannerRepository.setOnboardingCompleted(true)
            state.update { it.copy(onboardingCompleted = true) }
        }
    }

    fun restorePurchases(onRestored: (Boolean) -> Unit) {
        viewModelScope.launch {
            state.update { it.copy(isBillingActionInProgress = true, billingMessage = "paywall_restore_checking") }
            FirebaseUtils.logEvent("purchase_restore_started", Bundle().apply {
                putString("source", "onboarding_paywall")
            })
            entitlementRepository.restorePurchases()
            val restored = entitlementRepository.isPro.first()
            state.update {
                it.copy(
                    isPremium = restored,
                    isBillingActionInProgress = false,
                    billingMessage = if (restored) "paywall_restore_success" else "paywall_restore_none"
                )
            }
            FirebaseUtils.logEvent("purchase_restore_completed", Bundle().apply {
                putString("source", "onboarding_paywall")
                putBoolean("restored", restored)
            })
            Timber.tag("OnboardingBilling").d("Restore purchases completed. restored=%s", restored)
            onRestored(restored)
        }
    }

    private fun logBillingLaunchResult(
        result: BillingFlowLaunchResult,
        productId: String,
        source: String
    ) {
        when (result) {
            BillingFlowLaunchResult.Launched -> Timber.tag("OnboardingBilling").d(
                "Billing launch accepted product=%s source=%s",
                productId,
                source
            )
            BillingFlowLaunchResult.ProductUnavailable -> Timber.tag("OnboardingBilling").e(
                "Billing launch unavailable product=%s source=%s",
                productId,
                source
            )
            is BillingFlowLaunchResult.Failed -> Timber.tag("OnboardingBilling").e(
                "Billing launch failed product=%s source=%s code=%d message=%s",
                productId,
                source,
                result.responseCode,
                result.debugMessage
            )
        }
    }

    private fun logBillingPurchaseEvent(event: BillingPurchaseEvent, source: String) {
        when (event) {
            BillingPurchaseEvent.Completed -> {
                FirebaseUtils.logEvent("purchase_completed", Bundle().apply {
                    putString("source", source)
                    putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
                    putString("product_id", PremiumSubscriptionProducts.productIdFor(state.value.selectedBillingPlan))
                    putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                    putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                    putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
                })
                Timber.tag("OnboardingBilling").d(
                    "Billing purchase completed source=%s",
                    source
                )
            }
            BillingPurchaseEvent.Pending -> {
                FirebaseUtils.logEvent("purchase_pending", Bundle().apply {
                    putString("source", source)
                    putString("product_id", PremiumSubscriptionProducts.productIdFor(state.value.selectedBillingPlan))
                    putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                    putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                })
                Timber.tag("OnboardingBilling").d(
                    "Billing purchase pending source=%s",
                    source
                )
            }
            BillingPurchaseEvent.Canceled -> {
                FirebaseUtils.logEvent("purchase_canceled", Bundle().apply {
                    putString("source", source)
                    putString("product_id", PremiumSubscriptionProducts.productIdFor(state.value.selectedBillingPlan))
                    putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                    putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                })
                Timber.tag("OnboardingBilling").d(
                    "Billing purchase canceled source=%s",
                    source
                )
            }
            is BillingPurchaseEvent.Failed -> {
                FirebaseUtils.logEvent("purchase_failed", Bundle().apply {
                    putString("source", source)
                    putString("product_id", PremiumSubscriptionProducts.productIdFor(state.value.selectedBillingPlan))
                    putInt("response_code", event.responseCode)
                    putString("onboarding_experiment", state.value.onboardingConversionExperiment)
                    putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
                })
                Timber.tag("OnboardingBilling").e(
                    "Billing purchase failed source=%s code=%d message=%s",
                    source,
                    event.responseCode,
                    event.debugMessage
                )
            }
        }
    }

    fun completeOnboarding() {
        val current = state.value
        if (!current.planSaved) {
            state.update { it.copy(error = "onboarding_error_plan_required") }
            return
        }
        if (!current.reminderSaved && !current.reminderSkipped) {
            state.update { it.copy(error = "onboarding_error_reminder_required") }
            return
        }

        viewModelScope.launch {
            if (plannerRepository.loadPlan() != null) {
                plannerRepository.setOnboardingCompleted(true)
                if (state.value.isPremium) {
                    enablePremiumWatchForOnboarding("existing_entitlement")
                }
                state.update { it.copy(onboardingCompleted = true) }
                FirebaseUtils.logEvent("onboarding_completed")
            } else {
                state.update { it.copy(error = "onboarding_error_valid_plan_required") }
            }
        }
    }

    private suspend fun enablePremiumWatchForOnboarding(source: String) {
        plannerRepository.saveAutomationEnabled(true)
        FirebaseUtils.logEvent("premium_watch_enabled_after_onboarding", Bundle().apply {
            putString("source", source)
        })
        Timber.tag(TAG).d("Premium watch enabled during onboarding. source=%s", source)
    }

    fun saveReminderConfiguration(time: LocalTime, cadence: ReminderCadence, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val configuration = ReminderConfiguration(
                enabled = true,
                hourOfDay = time.hour,
                minute = time.minute,
                cadence = cadence
            )
            plannerRepository.saveReminderConfiguration(configuration)
            reminderWorkScheduler.sync(configuration)
            state.update { it.copy(reminderSaved = true, reminderSkipped = false) }
            FirebaseUtils.logEvent("reminder_enabled")
            onSaved()
        }
    }

    fun skipReminderConfiguration(onSkipped: () -> Unit = {}) {
        viewModelScope.launch {
            val configuration = ReminderConfiguration(enabled = false)
            plannerRepository.saveReminderConfiguration(configuration)
            reminderWorkScheduler.sync(configuration)
            state.update { it.copy(reminderSaved = false, reminderSkipped = true) }
            FirebaseUtils.logEvent("reminder_skipped")
            onSkipped()
        }
    }

    fun logPaywallViewed() {
        FirebaseUtils.logEvent("paywall_viewed", Bundle().apply {
            putString("price_cluster", state.value.pricingStrategy?.marketCluster)
            putString("billing_plan", state.value.selectedBillingPlan.name.lowercase(Locale.US))
            putString("onboarding_paywall_variant", state.value.onboardingPaywallVariant.remoteValue)
            putString("onboarding_experiment", state.value.onboardingConversionExperiment)
            putString("paywall_trial_framing", state.value.paywallTrialFraming.remoteValue)
            putString("paywall_value_frame", PAYWALL_VALUE_FRAME)
        })
    }

    fun logSignupStarted() {
        FirebaseUtils.logEvent("signup_started")
    }

    fun logTrajectoryViewed() {
        FirebaseUtils.logEvent("trajectory_viewed")
    }

    private fun buildPlan(current: OnboardingState, income: BigDecimal, fixedCosts: BigDecimal): SalaryPlan? {
        val nextBiweeklyPayday = if (current.payFrequency == PayFrequency.BIWEEKLY) {
            runCatching { LocalDate.parse(current.nextBiweeklyPaydayText) }.getOrElse {
                state.update { it.copy(error = "onboarding_error_next_payday_format") }
                return null
            }
        } else {
            null
        }

        val hasExplicitAllocations = listOf(
            current.allocatedFlexibleSpend,
            current.allocatedSavings,
            current.allocatedInvesting,
            current.allocatedCrypto,
            current.allocatedDebt
        ).any { it != null }

        val rules = if (hasExplicitAllocations) {
            mutableListOf<PaydayRule>().apply {
                current.allocatedSavings?.let {
                    if (it > BigDecimal.ZERO) add(PaydayRule(name = "Savings", amount = it, type = PaydayRuleType.SAVINGS, isPercentage = current.allocationIsPercentage))
                }
                current.allocatedInvesting?.let {
                    if (it > BigDecimal.ZERO) add(PaydayRule(name = "Investing", amount = it, type = PaydayRuleType.INVESTING, isPercentage = current.allocationIsPercentage))
                }
                current.allocatedCrypto?.let {
                    if (it > BigDecimal.ZERO) add(PaydayRule(name = "Crypto", amount = it, type = PaydayRuleType.CRYPTO, isPercentage = current.allocationIsPercentage))
                }
                current.allocatedDebt?.let {
                    if (it > BigDecimal.ZERO) add(PaydayRule(name = "Debt", amount = it, type = PaydayRuleType.DEBT, isPercentage = current.allocationIsPercentage))
                }
            }
        } else {
            resolveAllocationStrategyRulesUseCase.execute(
                focus = current.selectedFocus,
                preset = current.preset,
                strategy = current.strategy
            )
        }

        return SalaryPlan(
            focus = current.selectedFocus,
            netIncomePerPayday = income,
            monthlyFixedCosts = fixedCosts,
            payFrequency = current.payFrequency,
            monthlyPayday = current.monthlyPayday.coerceIn(1, 28),
            nextBiweeklyPayday = nextBiweeklyPayday,
            preset = current.preset,
            strategy = current.strategy,
            customStrategyName = current.customStrategyName.takeIf { current.strategy == AllocationStrategy.CUSTOM && it.isNotBlank() },
            rules = rules
        )
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
        private const val PAYWALL_VALUE_FRAME = "payday_cycle_proof"
        const val SETUP_STEP_TOTAL = 4
        const val FIXED_COSTS_EXCEED_INCOME_ERROR = "onboarding_fixed_costs_error_exceeds_income"
    }
}
