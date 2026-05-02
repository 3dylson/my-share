package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
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
    var incomeText by remember { mutableStateOf(initialIncome?.toPlainString() ?: "") }
    var frequency by remember { mutableStateOf(initialFrequency) }
    var paydayText by remember { mutableStateOf(initialMonthlyPayday.toString()) }
    var biweeklyPaydayText by remember { mutableStateOf(initialNextBiweeklyPaydayText) }

    val currencySymbol = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).currency?.symbol ?: ""
    }
    val income = incomeText.toBigDecimalOrNull()
    val payday = paydayText.toIntOrNull() ?: 1
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onBack, 
                contentPadding = PaddingValues(0.dp)
            ) { 
                Text(stringResource(R.string.back), color = MyShareSecondary) 
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                stringResource(R.string.onboarding_salary_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_salary_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            PremiumTextField(
                value = incomeText,
                onValueChange = { incomeText = it.replace(',', '.') },
                label = stringResource(R.string.onboarding_salary_label_amount),
                placeholder = "0.00",
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.onboarding_salary_frequency_title), 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

            Spacer(Modifier.height(32.dp))

            if (frequency == PayFrequency.MONTHLY) {
                PremiumTextField(
                    value = paydayText,
                    onValueChange = { if (it.length <= 2) paydayText = it },
                    label = stringResource(R.string.onboarding_salary_label_payday),
                    placeholder = "e.g. 28",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                PremiumTextField(
                    value = biweeklyPaydayText,
                    onValueChange = { biweeklyPaydayText = it },
                    label = stringResource(R.string.onboarding_salary_label_next_date),
                    placeholder = "e.g. Apr 25"
                )
            }

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = stringResource(R.string.continue_button),
                onClick = {
                    onNext(income ?: BigDecimal.ZERO, frequency, payday, biweeklyPaydayText)
                },
                enabled = income != null && income > BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

