package pt.ms.myshare.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import pt.ms.myshare.utils.logs.FirebaseUtils
import timber.log.Timber

class ConsentManager(private val context: Context) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    fun requestConsent(activity: Activity, onConsentGathered: (Boolean) -> Unit) {
        // Set tag for under age of consent. false means users are not under age
        // of consent.
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
            
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // The consent information state was updated.
                // We are now ready to check if a form is available.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Timber.tag("ConsentManager").w(
                            "Consent form load/show error: %s",
                            loadAndShowError.message
                        )
                    }

                    // Consent has been gathered.
                    val canRequestAds = consentInformation.canRequestAds()
                    Timber.tag("ConsentManager").d("canRequestAds: %s", canRequestAds)
                    
                    if (!canRequestAds) {
                         FirebaseUtils.logEvent("ad_suppressed", android.os.Bundle().apply {
                             putString("reason", "no_consent")
                         })
                    }
                    onConsentGathered(canRequestAds)
                }
            },
            { requestConsentError ->
                Timber.tag("ConsentManager").w(
                    "Consent info update error: %s",
                    requestConsentError.message
                )
                onConsentGathered(false)
            }
        )
    }

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }
}
