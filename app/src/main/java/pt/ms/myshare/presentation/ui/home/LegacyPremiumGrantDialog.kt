package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.MyShareAlertDialog
import pt.ms.myshare.presentation.ui.theme.MySharePrimary

@Composable
fun LegacyPremiumGrantDialog(
    isClaiming: Boolean,
    errorMessageKey: String?,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    MyShareAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Default.AutoAwesome,
        iconTint = MySharePrimary,
        title = stringResource(R.string.legacy_premium_grant_title),
        message = stringResource(R.string.legacy_premium_grant_body),
        confirmText = if (isClaiming) {
            stringResource(R.string.legacy_premium_grant_claiming)
        } else {
            stringResource(R.string.legacy_premium_grant_primary)
        },
        onConfirm = {
            if (!isClaiming) onClaim()
        },
        dismissText = stringResource(R.string.dialog_not_now),
        onDismiss = onDismiss
    ) {
        LegacyGrantReason(text = stringResource(R.string.legacy_premium_grant_reason_year))
        LegacyGrantReason(text = stringResource(R.string.legacy_premium_grant_reason_watch))
        LegacyGrantReason(text = stringResource(R.string.legacy_premium_grant_reason_no_charge))
        if (isClaiming) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MySharePrimary
                )
                Text(
                    text = stringResource(R.string.legacy_premium_grant_claiming_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        errorMessageKey?.let { key ->
            Text(
                text = resolvedString(key),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LegacyGrantReason(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MySharePrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun resolvedString(key: String): String {
    val context = LocalContext.current
    return remember(key) {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) context.getString(resId) else key
    }
}
