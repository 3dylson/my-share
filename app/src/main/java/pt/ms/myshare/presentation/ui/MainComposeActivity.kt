package pt.ms.myshare.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import pt.ms.myshare.utils.ads.ConsentManager
import pt.ms.myshare.utils.isDarkTheme
import pt.ms.myshare.utils.logs.FirebaseUtils
import java.util.Locale

@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private lateinit var consentManager: ConsentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("myshare_prefs", MODE_PRIVATE)
        val sessions = prefs.getInt("session_count", 0) + 1
        prefs.edit().putInt("session_count", sessions).apply()

        consentManager = ConsentManager(this)
        consentManager.requestConsent(this) { canRequestAds ->
            if (canRequestAds && sessions >= 2) {
                MobileAds.initialize(this) {}
            }
        }
        
        enableEdgeToEdge()
        setContent {
            MyShareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AppNavigation()
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
