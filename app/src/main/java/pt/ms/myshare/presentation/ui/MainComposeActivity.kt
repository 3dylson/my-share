package pt.ms.myshare.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import pt.ms.myshare.presentation.AppOpenAdManager
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import pt.ms.myshare.presentation.ui.ads.AdsConsentManager
import pt.ms.myshare.utils.isDarkTheme
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private lateinit var consentManager: AdsConsentManager
    private val appOpenAdManager by lazy { AppOpenAdManager(application) }
    private var canRequestAdsForSession = false
    private var hasCheckedAppOpenForSession = false
    private var hasGatheredAdsConsentForSession = false
    private var hasInitializedAdsForSession = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("myshare_prefs", MODE_PRIVATE)
        val sessions = prefs.getInt("session_count", 0) + 1
        prefs.edit().putInt("session_count", sessions).apply()

        consentManager = AdsConsentManager(this)
        
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            val systemBarsColor = if (isDarkTheme) {
                pt.ms.myshare.presentation.ui.theme.MyShareBackground.toArgb()
            } else {
                pt.ms.myshare.presentation.ui.theme.MyShareBackground.toArgb()
            }

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
        }
        logAppStart()
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
