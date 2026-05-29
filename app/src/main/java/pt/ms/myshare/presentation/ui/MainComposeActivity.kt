package pt.ms.myshare.presentation.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.domain.model.ProductExperienceConfig
import pt.ms.myshare.domain.repository.ProductConfigRepository
import pt.ms.myshare.domain.repository.UserPreferencesRepository
import pt.ms.myshare.presentation.ui.appupdate.AppUpdateGateState
import pt.ms.myshare.presentation.ui.appupdate.AppUpdateLoadingScreen
import pt.ms.myshare.presentation.ui.appupdate.ImmediateAppUpdateCoordinator
import pt.ms.myshare.presentation.ui.appupdate.RequiredUpdateScreen
import pt.ms.myshare.presentation.ui.localization.UserLocaleProvider
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import pt.ms.myshare.presentation.ui.ads.AdsConsentManager
import pt.ms.myshare.presentation.ui.ads.AdsOrchestrator
import pt.ms.myshare.presentation.ui.ads.LocalAdsOrchestrator
import pt.ms.myshare.domain.use_case.ResolveAppUpdateDecisionUseCase
import pt.ms.myshare.domain.use_case.RefreshEntitlementUseCase
import pt.ms.myshare.presentation.notifications.MyShareNotificationIntentFactory
import pt.ms.myshare.presentation.ui.home.HomeDestination
import pt.ms.myshare.utils.isDarkTheme
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    @Inject
    lateinit var resolveAppUpdateDecisionUseCase: ResolveAppUpdateDecisionUseCase

    @Inject
    lateinit var refreshEntitlementUseCase: RefreshEntitlementUseCase

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var productConfigRepository: ProductConfigRepository

    private lateinit var consentManager: AdsConsentManager
    private lateinit var adsOrchestrator: AdsOrchestrator
    private lateinit var immediateAppUpdateCoordinator: ImmediateAppUpdateCoordinator
    private lateinit var updateActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var appUpdateGateState by mutableStateOf<AppUpdateGateState>(AppUpdateGateState.Loading)
    private var hasCheckedAppOpenForSession = false
    private var hasGatheredAdsConsentForSession = false
    private var sessionCountForAds = 1
    private var isAppUpdateGateEvaluationInFlight = false
    private var lastAppUpdateGateEvaluationElapsedRealtime = 0L
    private var notificationHomeDestination by mutableStateOf<HomeDestination?>(null)
    private var hasLoggedAppReady = false
    private var appStartElapsedRealtime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appStartElapsedRealtime = SystemClock.elapsedRealtime()
        immediateAppUpdateCoordinator = ImmediateAppUpdateCoordinator(this)
        updateActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.d("Immediate app update flow completed")
            } else {
                Timber.d("Immediate app update flow ended without completion. resultCode=%d", result.resultCode)
            }
        }
        
        consentManager = AdsConsentManager(this)
        adsOrchestrator = AdsOrchestrator(this, consentManager)
        updateSessionCountForAds()
        handleNotificationIntent(intent)
        
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            val userPreferences by userPreferencesRepository.observePreferences()
                .collectAsStateWithLifecycle(initialValue = userPreferencesRepository.loadPreferences())
            val productConfig by productConfigRepository.config
                .collectAsStateWithLifecycle(initialValue = ProductExperienceConfig())

            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge()
            }

            LaunchedEffect(productConfig) {
                adsOrchestrator.updateConfig(productConfig)
            }

            UserLocaleProvider(languageTag = userPreferences.languageTag) {
                MyShareTheme {
                    when (val gateState = appUpdateGateState) {
                        AppUpdateGateState.Loading -> AppUpdateLoadingScreen()
                        AppUpdateGateState.Ready -> {
                            LaunchedEffect(Unit) {
                                logAppReady("home")
                            }
                            CompositionLocalProvider(LocalAdsOrchestrator provides adsOrchestrator) {
                                Surface(
                                    modifier = Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    AppNavigation(
                                        notificationHomeDestination = notificationHomeDestination,
                                        onNotificationHomeDestinationConsumed = {
                                            notificationHomeDestination = null
                                        },
                                        onManageAdsConsent = {
                                            ensureAdsConsentReady {
                                                consentManager.showPrivacyOptionsForm { formError ->
                                                    if (formError != null) {
                                                        Timber.tag("AdsConsent").w("Privacy options form error: %s", formError.message)
                                                    }
                                                    adsOrchestrator.updateConsent(consentManager.canRequestAds)
                                                }
                                            }
                                        },
                                        adsConsentManager = consentManager,
                                        onFreeHomeReady = { hasFirstPlan, isPremium ->
                                            ensureAdsConsentReady {
                                                adsOrchestrator.prepareEligibleFreeSession(
                                                    isPremium = isPremium,
                                                    hasFirstPlan = hasFirstPlan
                                                )
                                                if (!hasCheckedAppOpenForSession) {
                                                    hasCheckedAppOpenForSession = true
                                                    adsOrchestrator.showAppOpenIfEligible(
                                                        activity = this@MainComposeActivity,
                                                        isPremium = isPremium,
                                                        hasFirstPlan = hasFirstPlan
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        is AppUpdateGateState.RequiredUpdate -> RequiredUpdateScreen(
                            onOpenPlayStore = { openPlayStoreListing(gateState.policy.playStorePackageName) },
                            onOpenPlayStoreWeb = { openPlayStoreWebListing(gateState.policy.playStorePackageName) }
                        )
                    }
                }
            }
        }
        evaluateAppUpdateGate(
            reason = "startup",
            force = true
        )
        logAppStart()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshActiveEntitlement()
        evaluateAppUpdateGateOnResume()
    }

    private fun evaluateAppUpdateGateOnResume() {
        val gateState = appUpdateGateState
        if (gateState is AppUpdateGateState.RequiredUpdate && gateState.policy.immediateUpdateRequired) {
            immediateAppUpdateCoordinator.resumeImmediateUpdateIfInProgress(
                launcher = updateActivityResultLauncher,
                onFlowStarted = {
                    Timber.d("Resumed immediate app update flow")
                },
                onNoUpdateInProgress = {
                    Timber.d("Required update gate remains active without an in-progress Play update")
                }
            )
            return
        }

        evaluateAppUpdateGate(
            reason = "resume",
            force = false
        )
    }

    private fun shouldEvaluateAppUpdateGate(force: Boolean): Boolean {
        if (isAppUpdateGateEvaluationInFlight) {
            Timber.d("Skipping app update gate evaluation because another evaluation is in flight")
            return false
        }

        if (force) return true

        val elapsedSinceLastEvaluation =
            SystemClock.elapsedRealtime() - lastAppUpdateGateEvaluationElapsedRealtime
        val shouldEvaluate = elapsedSinceLastEvaluation >= APP_UPDATE_GATE_RESUME_RECHECK_INTERVAL_MS
        if (!shouldEvaluate) {
            Timber.d(
                "Skipping app update gate evaluation because last check is still fresh. elapsedMillis=%d",
                elapsedSinceLastEvaluation
            )
        }
        return shouldEvaluate
    }

    private fun refreshActiveEntitlement() {
        lifecycleScope.launch {
            runCatching { refreshEntitlementUseCase() }
                .onSuccess {
                    Timber.d("Active entitlement refresh requested on resume")
                }
                .onFailure { error ->
                    Timber.e(error, "Active entitlement refresh failed on resume")
                }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val destination = intent?.getStringExtra(MyShareNotificationIntentFactory.EXTRA_HOME_DESTINATION)
            ?.toHomeDestination()
        if (destination != null) {
            notificationHomeDestination = destination
            if (::adsOrchestrator.isInitialized) {
                adsOrchestrator.markNotificationLaunch(true)
            }
            val type = intent.getStringExtra(MyShareNotificationIntentFactory.EXTRA_NOTIFICATION_TYPE).orEmpty()
            FirebaseUtils.logEvent("notification_opened", android.os.Bundle().apply {
                putString("type", type)
                putString("destination", destination.name.lowercase(Locale.US))
            })
            Timber.d("Notification opened type=%s destination=%s", type, destination)
        }
    }

    private fun updateSessionCountForAds() {
        lifecycleScope.launch {
            sessionCountForAds = adsOrchestrator.incrementSessionCount()
            Timber.tag("AdsConsent").d("Session count updated for ads: %d", sessionCountForAds)
        }
    }

    private fun evaluateAppUpdateGate(reason: String, force: Boolean) {
        if (!shouldEvaluateAppUpdateGate(force)) return

        isAppUpdateGateEvaluationInFlight = true
        lastAppUpdateGateEvaluationElapsedRealtime = SystemClock.elapsedRealtime()
        lifecycleScope.launch {
            try {
                Timber.d("Evaluating app update gate. reason=%s", reason)
                val decision = resolveAppUpdateDecisionUseCase(BuildConfig.VERSION_CODE)
                if (decision.policyError != null) {
                    Timber.e(decision.policyError, "App update policy unavailable. Allowing app usage.")
                }

                val policy = decision.policy
                if (decision.mustBlockAppUse && policy != null) {
                    val wasAlreadyBlocked = appUpdateGateState is AppUpdateGateState.RequiredUpdate
                    Timber.d(
                        "Blocking app usage for required update. installedVersionCode=%d minimumSupportedVersionCode=%d",
                        BuildConfig.VERSION_CODE,
                        policy.minimumSupportedVersionCode
                    )
                    appUpdateGateState = AppUpdateGateState.RequiredUpdate(policy)
                    if (decision.shouldRequestImmediateUpdate && !wasAlreadyBlocked) {
                        requestImmediateAppUpdate()
                    }
                } else {
                    Timber.d("App update gate passed. installedVersionCode=%d", BuildConfig.VERSION_CODE)
                    appUpdateGateState = AppUpdateGateState.Ready
                }
            } finally {
                isAppUpdateGateEvaluationInFlight = false
            }
        }
    }

    private fun requestImmediateAppUpdate() {
        immediateAppUpdateCoordinator.requestImmediateUpdate(
            launcher = updateActivityResultLauncher,
            onFlowStarted = {
                Timber.d("Immediate app update flow started")
            },
            onNoImmediateUpdateAvailable = {
                Timber.d("Required update fallback screen remains active")
            }
        )
    }

    private fun openPlayStoreListing(packageName: String) {
        try {
            Timber.d("Opening Play Store listing. packageName=%s", packageName)
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    .setPackage("com.android.vending")
            )
        } catch (error: ActivityNotFoundException) {
            Timber.e(error, "Play Store app unavailable. Opening web listing instead.")
            openPlayStoreWebListing(packageName)
        }
    }

    private fun openPlayStoreWebListing(packageName: String) {
        Timber.d("Opening Play Store web listing. packageName=%s", packageName)
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }

    private fun ensureAdsConsentReady(onReady: () -> Unit = {}) {
        if (hasGatheredAdsConsentForSession) {
            adsOrchestrator.updateConsent(consentManager.canRequestAds)
            onReady()
            return
        }
        hasGatheredAdsConsentForSession = true
        Timber.tag("AdsConsent").d("Gathering ads consent after product context is ready")
        consentManager.gatherConsent(object : AdsConsentManager.OnConsentGatheringFinishedListener {
            override fun onConsentGatheringFinished(error: String?) {
                if (error != null) {
                    Timber.tag("AdsConsent").w("Consent error: %s", error)
                }

                adsOrchestrator.updateConsent(consentManager.canRequestAds)
                onReady()
            }
        })
    }

    private fun logAppStart() {
        FirebaseUtils.logEvent(
            "app_start",
            Bundle().apply {
                putString("device_language", Locale.getDefault().language)
                putString("is_system_dark_theme", this@MainComposeActivity.isDarkTheme().toString())
            },
        )
    }

    private fun logAppReady(destination: String) {
        if (hasLoggedAppReady) return
        hasLoggedAppReady = true
        val elapsedMillis = SystemClock.elapsedRealtime() - appStartElapsedRealtime
        FirebaseUtils.logEvent(
            "app_ready",
            Bundle().apply {
                putString("destination", destination)
                putLong("elapsed_ms", elapsedMillis)
                putInt("session_count", sessionCountForAds)
            }
        )
        Timber.d("App ready destination=%s elapsedMs=%d", destination, elapsedMillis)
    }

    companion object {
        private const val APP_UPDATE_GATE_RESUME_RECHECK_INTERVAL_MS = 60_000L
    }
}

private fun String.toHomeDestination(): HomeDestination? = when (lowercase(Locale.US)) {
    "plan" -> HomeDestination.PLAN
    "review" -> HomeDestination.REVIEW
    "strategy" -> HomeDestination.STRATEGY
    "more" -> HomeDestination.MORE
    else -> null
}
