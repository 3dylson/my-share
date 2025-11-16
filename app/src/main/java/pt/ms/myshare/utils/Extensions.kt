package pt.ms.myshare.utils

import android.content.Context
import android.content.res.Configuration

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}