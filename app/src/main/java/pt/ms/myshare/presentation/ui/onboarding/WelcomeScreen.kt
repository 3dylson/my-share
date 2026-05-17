package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.preferences.CurrencyPickerDialog
import pt.ms.myshare.presentation.ui.preferences.LanguagePickerDialog
import pt.ms.myshare.presentation.ui.preferences.currencyLabel
import pt.ms.myshare.presentation.ui.preferences.languageLabel
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun WelcomeScreen(
    userPreferences: UserPreferences,
    onLanguageSelected: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onContinue: () -> Unit,
    // Dev-only: use a lambda parameter so release builds pass a no-op
    onSkipDev: (() -> Unit)? = null
) {
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            selectedLanguageTag = userPreferences.languageTag,
            onDismissRequest = { showLanguagePicker = false },
            onLanguageSelected = {
                showLanguagePicker = false
                onLanguageSelected(it)
            }
        )
    }
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCurrencyCode = userPreferences.currencyCode,
            locale = userPreferences.locale,
            onDismissRequest = { showCurrencyPicker = false },
            onCurrencySelected = {
                showCurrencyPicker = false
                onCurrencySelected(it)
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Premium Visual Anchor - Glow effect
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.onboarding_welcome_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WelcomePreferenceRow(
                    title = stringResource(R.string.preferences_language_title),
                    value = languageLabel(userPreferences.languageTag),
                    icon = Icons.Default.Language,
                    onClick = { showLanguagePicker = true }
                )
                WelcomePreferenceRow(
                    title = stringResource(R.string.preferences_currency_title),
                    value = currencyLabel(userPreferences.currencyCode, userPreferences.locale),
                    icon = Icons.Default.Payments,
                    onClick = { showCurrencyPicker = true }
                )
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                PremiumButton(
                    text = stringResource(R.string.onboarding_welcome_button_get_started),
                    onClick = onContinue
                )

                // Only shown in debug builds; compiled away in release
                if (onSkipDev != null) {
                    TextButton(onClick = onSkipDev) {
                        Text(
                            stringResource(R.string.onboarding_welcome_button_skip_dev), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomePreferenceRow(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
