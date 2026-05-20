package pt.ms.myshare.presentation.review

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

class PlayStoreListingOpener(private val context: Context) {

    fun open(packageName: String) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(marketIntent)
            Timber.tag(TAG).d("Opened Play Store app listing")
        } catch (exception: ActivityNotFoundException) {
            Timber.tag(TAG).e(exception, "Play Store app unavailable; opening web listing")
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }

    private companion object {
        const val TAG = "PlayStoreListing"
    }
}
