package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.util.Locale

@Composable
fun SalaryAndScheduleScreen(
    initialIncome: BigDecimal?,
    initialFrequency: PayFrequency,
    initialMonthlyPayday: Int,
    initialNextBiweeklyPaydayText: String,
    onBack: () -> Unit,
    onNext: (BigDecimal, PayFrequency, Int, String) -> Unit
) {
    val locale = Locale.getDefault()
    var incomeText by remember { mutableStateOf(initialIncome?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) } ?: "") }
    var frequency by remember { mutableStateOf(initialFrequency) }
    var paydayText by remember { mutableStateOf(initialMonthlyPayday.toString()) }
    var biweeklyPaydayText by remember { mutableStateOf(initialNextBiweeklyPaydayText) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale) { LocalizedAmountFormatter.currencySymbol(locale) }
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }
    val income = LocalizedAmountFormatter.parseAmount(incomeText, locale)
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

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_salary_title),
        subtitle = stringResource(R.string.onboarding_salary_subtitle),
        actionText = stringResource(R.string.continue_button),
        onBack = onBack,
        onAction = ::continueIfValid
    ) {

            PremiumTextField(
                value = incomeText,
                onValueChange = { incomeText = LocalizedAmountFormatter.sanitizeAmountInput(it, locale) },
                label = stringResource(R.string.onboarding_salary_label_amount),
                placeholder = amountPlaceholder,
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                isError = incomeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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

            Spacer(Modifier.height(28.dp))

            Text(
                stringResource(R.string.onboarding_salary_frequency_title), 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_salary_frequency_monthly),
                    description = stringResource(R.string.onboarding_salary_frequency_monthly_desc),
                    isSelected = frequency == PayFrequency.MONTHLY,
                    icon = Icons.Default.CalendarMonth,
                    onClick = { frequency = PayFrequency.MONTHLY }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_salary_frequency_biweekly),
                    description = stringResource(R.string.onboarding_salary_frequency_biweekly_desc),
                    isSelected = frequency == PayFrequency.BIWEEKLY,
                    icon = Icons.Default.EventRepeat,
                    onClick = { frequency = PayFrequency.BIWEEKLY }
                )
            }

            Spacer(Modifier.height(28.dp))

            if (frequency == PayFrequency.MONTHLY) {
                PremiumTextField(
                    value = paydayText,
                    onValueChange = { if (it.length <= 2) paydayText = it },
                    label = stringResource(R.string.onboarding_salary_label_payday),
                    placeholder = stringResource(R.string.onboarding_salary_placeholder_payday),
                    isError = paydayError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    isError = biweeklyDateError
                )
                if (biweeklyDateError) {
                    OnboardingValidationText(stringResource(R.string.onboarding_salary_error_next_date_required))
                }
            }
    }
}
