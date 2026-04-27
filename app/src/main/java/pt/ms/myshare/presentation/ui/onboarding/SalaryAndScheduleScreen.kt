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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal

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
                Text("Back", color = MyShareSecondary) 
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Your Cash Flow", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                "We need to know when your money arrives to build a plan that actually works.", 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            PremiumTextField(
                value = incomeText,
                onValueChange = { incomeText = it.replace(',', '.') },
                label = "Next Net Payday Amount",
                placeholder = "0.00",
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Pay Frequency", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumChoiceCard(
                    title = "Monthly",
                    description = "I get paid once a month.",
                    isSelected = frequency == PayFrequency.MONTHLY,
                    icon = Icons.Default.CalendarMonth,
                    onClick = { frequency = PayFrequency.MONTHLY }
                )
                PremiumChoiceCard(
                    title = "Every 2 weeks",
                    description = "I get paid bi-weekly.",
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
                    label = "Typical Payday (Day of month)",
                    placeholder = "e.g. 28",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                PremiumTextField(
                    value = biweeklyPaydayText,
                    onValueChange = { biweeklyPaydayText = it },
                    label = "Next Payday Date",
                    placeholder = "e.g. Apr 25"
                )
            }

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = "Continue",
                onClick = {
                    onNext(income ?: BigDecimal.ZERO, frequency, payday, biweeklyPaydayText)
                },
                enabled = income != null && income > BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

