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
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import pt.ms.myshare.presentation.ui.ads.AdsConsentManager
import pt.ms.myshare.utils.isDarkTheme
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private lateinit var consentManager: AdsConsentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("myshare_prefs", MODE_PRIVATE)
        val sessions = prefs.getInt("session_count", 0) + 1
        prefs.edit().putInt("session_count", sessions).apply()

        consentManager = AdsConsentManager(this)
        consentManager.gatherConsent(object : AdsConsentManager.OnConsentGatheringFinishedListener {
            override fun onConsentGatheringFinished(error: String?) {
                if (error != null) {
                    Timber.tag("AdsConsent").w("Consent error: %s", error)
                }
                
                if (consentManager.canRequestAds && sessions >= 2) {
                    MobileAds.initialize(this@MainComposeActivity) {
                        pt.ms.myshare.presentation.ui.ads.InterstitialAdManager.loadAd(this@MainComposeActivity)
                    }
                }
            }
        })
        
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
                            consentManager.showPrivacyOptionsForm { formError ->
                                if (formError != null) {
                                    Timber.tag("AdsConsent").w("Privacy options form error: %s", formError.message)
                                }
                            }
                        },
                        adsConsentManager = consentManager
                    )
                }
            }
        }
        logAppStart()
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
