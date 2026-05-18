package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
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
import androidx.compose.ui.text.style.TextOverflow
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PremiumButton(
                        text = stringResource(R.string.onboarding_welcome_button_get_started),
                        onClick = onContinue
                    )

                    if (onSkipDev != null) {
                        TextButton(
                            onClick = onSkipDev,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                stringResource(R.string.onboarding_welcome_button_skip_dev),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MySharePrimary.copy(alpha = 0.24f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.onboarding_welcome_badge).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MySharePrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                stringResource(R.string.onboarding_welcome_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(22.dp))

            WelcomeOutcomeCard()

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
        }
    }
}

@Composable
private fun WelcomeOutcomeCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MySharePrimary.copy(alpha = 0.22f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            PaydayFlowVisual()
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            WelcomeOutcomeRow(
                title = stringResource(R.string.onboarding_welcome_outcome_weekly_title),
                body = stringResource(R.string.onboarding_welcome_outcome_weekly_body),
                icon = Icons.Default.Savings
            )
            WelcomeOutcomeRow(
                title = stringResource(R.string.onboarding_welcome_outcome_trust_title),
                body = stringResource(R.string.onboarding_welcome_outcome_trust_body),
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Composable
private fun PaydayFlowVisual() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaydayFlowNode(
            label = stringResource(R.string.onboarding_welcome_flow_income),
            icon = Icons.Default.Payments,
            tint = MySharePrimary,
            modifier = Modifier.weight(1f)
        )
        PaydayFlowConnector()
        PaydayFlowNode(
            label = stringResource(R.string.onboarding_welcome_flow_bills),
            icon = Icons.Default.AccountBalance,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        PaydayFlowConnector()
        PaydayFlowNode(
            label = stringResource(R.string.onboarding_welcome_flow_weekly),
            icon = Icons.Default.Savings,
            tint = MySharePositive,
            modifier = Modifier.weight(1f)
        )
        PaydayFlowConnector()
        PaydayFlowNode(
            label = stringResource(R.string.onboarding_welcome_flow_goal),
            icon = Icons.Default.Flag,
            tint = MySharePrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaydayFlowNode(
    label: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tint.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.18f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.padding(9.dp).size(21.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PaydayFlowConnector() {
    Surface(
        modifier = Modifier
            .width(14.dp)
            .height(2.dp),
        color = MySharePrimary.copy(alpha = 0.24f),
        shape = RoundedCornerShape(999.dp),
        content = {}
    )
}

@Composable
private fun WelcomeOutcomeRow(
    title: String,
    body: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MySharePrimary.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.padding(7.dp).size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(20.dp)
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
