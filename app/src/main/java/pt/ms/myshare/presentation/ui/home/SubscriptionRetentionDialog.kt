package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.MyShareAlertDialog
import pt.ms.myshare.presentation.ui.theme.MySharePrimary

@Composable
fun SubscriptionRetentionDialog(
    onDismissRequest: () -> Unit,
    onClaimOffer: () -> Unit,
    onContinueToGooglePlay: () -> Unit
) {
    MyShareAlertDialog(
        onDismissRequest = onDismissRequest,
        icon = Icons.Default.WorkspacePremium,
        iconTint = MySharePrimary,
        title = stringResource(R.string.subscription_retention_title),
        message = stringResource(R.string.subscription_retention_body),
        confirmText = stringResource(R.string.subscription_retention_primary),
        onConfirm = onClaimOffer,
        dismissText = stringResource(R.string.subscription_retention_secondary),
        onDismiss = onContinueToGooglePlay
    ) {
        SubscriptionRetentionReason(text = stringResource(R.string.subscription_retention_reason_review))
        SubscriptionRetentionReason(text = stringResource(R.string.subscription_retention_reason_watch))
        SubscriptionRetentionReason(text = stringResource(R.string.subscription_retention_reason_cancel))
    }
}

@Composable
private fun SubscriptionRetentionReason(
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
