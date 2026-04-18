package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PayFrequency
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) { 
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
            
            Text(
                "Your Cash Flow", 
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            
            Text(
                "We need to know when your money arrives to build a plan that actually works.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Next payday amount (Net)") },
                placeholder = { Text("e.g. 2500.00") },
                singleLine = true,
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(32.dp))

            Text("Pay frequency", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PremiumChoiceCard(
                    text = "Monthly",
                    selected = frequency == PayFrequency.MONTHLY,
                    modifier = Modifier.weight(1f),
                    onClick = { frequency = PayFrequency.MONTHLY }
                )
                PremiumChoiceCard(
                    text = "Every 2 weeks",
                    selected = frequency == PayFrequency.BIWEEKLY,
                    modifier = Modifier.weight(1f),
                    onClick = { frequency = PayFrequency.BIWEEKLY }
                )
            }

            Spacer(Modifier.height(32.dp))

            if (frequency == PayFrequency.MONTHLY) {
                OutlinedTextField(
                    value = paydayText,
                    onValueChange = { if (it.length <= 2) paydayText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Typical payday (Day of month)") },
                    placeholder = { Text("e.g. 28") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                OutlinedTextField(
                    value = biweeklyPaydayText,
                    onValueChange = { biweeklyPaydayText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Next payday date") },
                    placeholder = { Text("e.g. Apr 25") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    onNext(income ?: BigDecimal.ZERO, frequency, payday, biweeklyPaydayText)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = income != null && income > BigDecimal.ZERO,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
