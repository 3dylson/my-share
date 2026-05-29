package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
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
import pt.ms.myshare.domain.model.OnboardingIntroVariant
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
    introVariant: OnboardingIntroVariant = OnboardingIntroVariant.PLAN_FIRST,
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val compactHeight = maxHeight < 700.dp
            val topPadding = if (compactHeight) 10.dp else 24.dp
            val heroSpacing = if (compactHeight) 14.dp else 20.dp
            val titleStyle = if (compactHeight) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.headlineLarge
            }
            val titleLineHeight = if (compactHeight) 36.sp else 42.sp
            val subtitleStyle = if (compactHeight) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            }
            val subtitleLineHeight = if (compactHeight) 21.sp else 24.sp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = topPadding, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(introVariant.titleRes),
                    style = titleStyle,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = titleLineHeight,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    stringResource(introVariant.subtitleRes),
                    style = subtitleStyle,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = if (compactHeight) 8.dp else 12.dp, start = 4.dp, end = 4.dp),
                    lineHeight = subtitleLineHeight
                )

                Spacer(Modifier.height(heroSpacing))

                PaydayChecklistCard(introVariant = introVariant)

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
}

@Composable
private fun PaydayChecklistCard(introVariant: OnboardingIntroVariant) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(introVariant.badgeRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            WelcomeOutcomeRow(
                index = "1",
                title = stringResource(introVariant.paydayTitleRes),
                body = stringResource(introVariant.paydayBodyRes),
                icon = Icons.Default.AccountBalance
            )
            WelcomeOutcomeRow(
                index = "2",
                title = stringResource(introVariant.weeklyTitleRes),
                body = stringResource(introVariant.weeklyBodyRes),
                icon = Icons.Default.Savings
            )
            WelcomeOutcomeRow(
                index = "3",
                title = stringResource(introVariant.trustTitleRes),
                body = stringResource(introVariant.trustBodyRes),
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Composable
private fun WelcomeOutcomeRow(
    index: String,
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
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
        ) {
            Text(
                text = index,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MySharePrimary,
                fontWeight = FontWeight.Bold
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
                lineHeight = 19.sp
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

private val OnboardingIntroVariant.titleRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_title
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_title
    }

private val OnboardingIntroVariant.subtitleRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_subtitle
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_subtitle
    }

private val OnboardingIntroVariant.badgeRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_badge
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_badge
    }

private val OnboardingIntroVariant.paydayTitleRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_payday_title
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_payday_title
    }

private val OnboardingIntroVariant.paydayBodyRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_payday_body
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_payday_body
    }

private val OnboardingIntroVariant.weeklyTitleRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_weekly_title
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_weekly_title
    }

private val OnboardingIntroVariant.weeklyBodyRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_weekly_body
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_weekly_body
    }

private val OnboardingIntroVariant.trustTitleRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_trust_title
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_trust_title
    }

private val OnboardingIntroVariant.trustBodyRes: Int
    get() = when (this) {
        OnboardingIntroVariant.PLAN_FIRST -> R.string.onboarding_welcome_outcome_trust_body
        OnboardingIntroVariant.SPEND_CLARITY -> R.string.onboarding_welcome_spend_clarity_outcome_trust_body
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
