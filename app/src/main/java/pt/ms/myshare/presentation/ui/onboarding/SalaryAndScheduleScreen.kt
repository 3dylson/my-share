package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.components.rememberInputKeyboardActions
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal

@Composable
fun SalaryAndScheduleScreen(
    initialIncome: BigDecimal?,
    initialFrequency: PayFrequency,
    initialMonthlyPayday: Int,
    initialNextBiweeklyPaydayText: String,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNext: (BigDecimal, PayFrequency, Int, String) -> Unit
) {
    val locale = userPreferences.locale
    var incomeText by remember(userPreferences.languageTag) { mutableStateOf(initialIncome?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) } ?: "") }
    var frequency by remember { mutableStateOf(initialFrequency) }
    var paydayText by remember { mutableStateOf(initialMonthlyPayday.toString()) }
    var biweeklyPaydayText by remember { mutableStateOf(initialNextBiweeklyPaydayText) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale, userPreferences.currencyCode) {
        LocalizedAmountFormatter.currencySymbol(locale, userPreferences.currencyCode)
    }
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }
    val income = LocalizedAmountFormatter.parseAmount(incomeText, locale)
    val incomePreview = income
        ?.takeIf { it > BigDecimal.ZERO }
        ?.let {
            LocalizedAmountFormatter.formatCurrency(
                amount = it,
                locale = locale,
                currencyCode = userPreferences.currencyCode
            )
        }
    val payday = paydayText.toIntOrNull()
    val incomeError = validationRequested && (income == null || income <= BigDecimal.ZERO)
    val paydayError = validationRequested && frequency == PayFrequency.MONTHLY && (payday == null || payday !in 1..28)
    val biweeklyDateError = validationRequested && frequency == PayFrequency.BIWEEKLY && biweeklyPaydayText.isBlank()
    val isSalaryValid = income != null &&
        income > BigDecimal.ZERO &&
        when (frequency) {
            PayFrequency.MONTHLY -> payday != null && payday in 1..28
            PayFrequency.BIWEEKLY -> biweeklyPaydayText.isNotBlank()
        }

    fun continueIfValid() {
        validationRequested = true
        if (isSalaryValid) {
            onNext(income ?: BigDecimal.ZERO, frequency, payday ?: 1, biweeklyPaydayText)
        }
    }
    val inputKeyboardActions = rememberInputKeyboardActions(onDone = ::continueIfValid)

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_salary_title),
        subtitle = stringResource(R.string.onboarding_salary_subtitle),
        actionText = stringResource(R.string.onboarding_salary_action),
        progressStep = 2,
        progressTotal = OnboardingViewModel.SETUP_STEP_TOTAL,
        onBack = onBack,
        onAction = ::continueIfValid
    ) {
        SalaryInputCard(incomePreview = incomePreview) {
            PremiumTextField(
                value = incomeText,
                onValueChange = { incomeText = LocalizedAmountFormatter.sanitizeAmountInput(it, locale) },
                label = stringResource(R.string.onboarding_salary_label_amount),
                placeholder = amountPlaceholder,
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                isError = incomeError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = inputKeyboardActions
            )
            if (incomeError) {
                OnboardingValidationText(
                    if (incomeText.isBlank()) {
                        stringResource(R.string.onboarding_salary_error_income_required)
                    } else {
                        stringResource(R.string.onboarding_salary_error_income_positive)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.onboarding_salary_frequency_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SalaryFrequencyCard(
                title = stringResource(R.string.onboarding_salary_frequency_monthly),
                description = stringResource(R.string.onboarding_salary_frequency_monthly_desc),
                icon = Icons.Default.CalendarMonth,
                selected = frequency == PayFrequency.MONTHLY,
                onClick = { frequency = PayFrequency.MONTHLY },
                modifier = Modifier.weight(1f)
            )
            SalaryFrequencyCard(
                title = stringResource(R.string.onboarding_salary_frequency_biweekly),
                description = stringResource(R.string.onboarding_salary_frequency_biweekly_desc),
                icon = Icons.Default.EventRepeat,
                selected = frequency == PayFrequency.BIWEEKLY,
                onClick = { frequency = PayFrequency.BIWEEKLY },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        SalaryScheduleCard(
            body = if (frequency == PayFrequency.MONTHLY) {
                stringResource(R.string.onboarding_salary_schedule_body_monthly)
            } else {
                stringResource(R.string.onboarding_salary_schedule_body_biweekly)
            }
        ) {
            if (frequency == PayFrequency.MONTHLY) {
                PremiumTextField(
                    value = paydayText,
                    onValueChange = { if (it.length <= 2) paydayText = it },
                    label = stringResource(R.string.onboarding_salary_label_payday),
                    placeholder = stringResource(R.string.onboarding_salary_placeholder_payday),
                    isError = paydayError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = inputKeyboardActions
                )
                if (paydayError) {
                    OnboardingValidationText(stringResource(R.string.onboarding_salary_error_payday_range))
                }
            } else {
                PremiumTextField(
                    value = biweeklyPaydayText,
                    onValueChange = { biweeklyPaydayText = it },
                    label = stringResource(R.string.onboarding_salary_label_next_date),
                    placeholder = stringResource(R.string.onboarding_salary_placeholder_next_date),
                    isError = biweeklyDateError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = inputKeyboardActions
                )
                if (biweeklyDateError) {
                    OnboardingValidationText(stringResource(R.string.onboarding_salary_error_next_date_required))
                }
            }
        }
    }
}

@Composable
private fun SalaryInputCard(
    incomePreview: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SalarySectionHeader(
                title = incomePreview ?: stringResource(R.string.onboarding_salary_income_card_title),
                body = if (incomePreview == null) {
                    stringResource(R.string.onboarding_salary_income_card_body)
                } else {
                    stringResource(R.string.onboarding_salary_outcome_body_filled)
                },
                icon = if (incomePreview == null) Icons.Default.Payments else Icons.Default.AutoGraph,
                tint = MySharePrimary
            )
            content()
        }
    }
}

@Composable
private fun SalaryScheduleCard(
    body: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SalarySectionHeader(
                title = stringResource(R.string.onboarding_salary_schedule_title),
                body = body,
                icon = Icons.Default.Schedule,
                tint = MySharePositive
            )
            content()
        }
    }
}

@Composable
private fun SalarySectionHeader(
    title: String,
    body: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tint.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.padding(8.dp).size(19.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
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
private fun SalaryFrequencyCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_selected),
                        tint = MySharePrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
