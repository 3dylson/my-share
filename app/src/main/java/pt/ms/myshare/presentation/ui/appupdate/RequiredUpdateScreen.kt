package pt.ms.myshare.presentation.ui.appupdate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R

@Composable
fun RequiredUpdateScreen(
    onOpenPlayStore: () -> Unit,
    onOpenPlayStoreWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp)
            .testTag(REQUIRED_UPDATE_SCREEN_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.required_update_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.required_update_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 480.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onOpenPlayStore,
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .testTag(REQUIRED_UPDATE_PLAY_STORE_BUTTON_TAG)
        ) {
            Text(text = stringResource(R.string.required_update_primary_action))
        }
        TextButton(
            onClick = onOpenPlayStoreWeb,
            modifier = Modifier.testTag(REQUIRED_UPDATE_WEB_BUTTON_TAG)
        ) {
            Text(text = stringResource(R.string.required_update_fallback_action))
        }
    }
}

const val REQUIRED_UPDATE_SCREEN_TAG = "required_update_screen"
const val REQUIRED_UPDATE_PLAY_STORE_BUTTON_TAG = "required_update_play_store_button"
const val REQUIRED_UPDATE_WEB_BUTTON_TAG = "required_update_web_button"
