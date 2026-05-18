package pt.ms.myshare.presentation.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.presentation.AppOpenAdManager
import pt.ms.myshare.presentation.ui.appupdate.AppUpdateGateState
import pt.ms.myshare.presentation.ui.appupdate.AppUpdateLoadingScreen
import pt.ms.myshare.presentation.ui.appupdate.ImmediateAppUpdateCoordinator
import pt.ms.myshare.presentation.ui.appupdate.RequiredUpdateScreen
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import pt.ms.myshare.presentation.ui.ads.AdsConsentManager
import pt.ms.myshare.domain.use_case.ResolveAppUpdateDecisionUseCase
import pt.ms.myshare.domain.use_case.RefreshEntitlementUseCase
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

    private lateinit var consentManager: AdsConsentManager
    private lateinit var immediateAppUpdateCoordinator: ImmediateAppUpdateCoordinator
    private lateinit var updateActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val appOpenAdManager by lazy { AppOpenAdManager(application) }
    private var appUpdateGateState by mutableStateOf<AppUpdateGateState>(AppUpdateGateState.Loading)
    private var canRequestAdsForSession = false
    private var hasCheckedAppOpenForSession = false
    private var hasGatheredAdsConsentForSession = false
    private var hasInitializedAdsForSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        
        val prefs = getSharedPreferences("myshare_prefs", MODE_PRIVATE)
        val sessions = prefs.getInt("session_count", 0) + 1
        prefs.edit().putInt("session_count", sessions).apply()

        consentManager = AdsConsentManager(this)
        
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme = isSystemInDarkTheme()

            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme }
                )
            }

            MyShareTheme {
                when (val gateState = appUpdateGateState) {
                    AppUpdateGateState.Loading -> AppUpdateLoadingScreen()
                    AppUpdateGateState.Ready -> {
                        Surface(
                            modifier = Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            AppNavigation(
                                onManageAdsConsent = {
                                    ensureAdsConsentReady(sessions) {
                                        consentManager.showPrivacyOptionsForm { formError ->
                                            if (formError != null) {
                                                Timber.tag("AdsConsent").w("Privacy options form error: %s", formError.message)
                                            }
                                        }
                                    }
                                },
                                adsConsentManager = consentManager,
                                onFreeHomeReady = {
                                    canRequestAdsForSession = consentManager.canRequestAds && sessions >= 2
                                    if (canRequestAdsForSession && !hasCheckedAppOpenForSession) {
                                        hasCheckedAppOpenForSession = true
                                        initializeAdsForSession()
                                        Timber.tag("AdsConsent").d("Checking app-open ad after existing consent")
                                        appOpenAdManager.showAdIfAvailable(this@MainComposeActivity)
                                    }
                                }
                            )
                        }
                    }
                    is AppUpdateGateState.RequiredUpdate -> RequiredUpdateScreen(
                        onOpenPlayStore = { openPlayStoreListing(gateState.policy.playStorePackageName) },
                        onOpenPlayStoreWeb = { openPlayStoreWebListing(gateState.policy.playStorePackageName) }
                    )
                }
            }
        }
        evaluateAppUpdateGate()
        logAppStart()
    }

    override fun onResume() {
        super.onResume()
        refreshActiveEntitlement()
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
        }
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

    private fun evaluateAppUpdateGate() {
        lifecycleScope.launch {
            val decision = resolveAppUpdateDecisionUseCase(BuildConfig.VERSION_CODE)
            if (decision.policyError != null) {
                Timber.e(decision.policyError, "App update policy unavailable. Allowing app usage.")
            }

            val policy = decision.policy
            if (decision.mustBlockAppUse && policy != null) {
                Timber.d(
                    "Blocking app usage for required update. installedVersionCode=%d minimumSupportedVersionCode=%d",
                    BuildConfig.VERSION_CODE,
                    policy.minimumSupportedVersionCode
                )
                appUpdateGateState = AppUpdateGateState.RequiredUpdate(policy)
                if (decision.shouldRequestImmediateUpdate) {
                    requestImmediateAppUpdate()
                }
            } else {
                Timber.d("App update gate passed. installedVersionCode=%d", BuildConfig.VERSION_CODE)
                appUpdateGateState = AppUpdateGateState.Ready
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

    private fun ensureAdsConsentReady(sessionCount: Int, onReady: () -> Unit = {}) {
        if (hasGatheredAdsConsentForSession) {
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

                canRequestAdsForSession = consentManager.canRequestAds && sessionCount >= 2
                if (canRequestAdsForSession) {
                    initializeAdsForSession()
                }
                onReady()
            }
        })
    }

    private fun initializeAdsForSession() {
        if (hasInitializedAdsForSession) return
        hasInitializedAdsForSession = true
        MobileAds.initialize(this@MainComposeActivity) {
            pt.ms.myshare.presentation.ui.ads.InterstitialAdManager.loadAd(this@MainComposeActivity)
            appOpenAdManager.preload(this@MainComposeActivity)
        }
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
}
