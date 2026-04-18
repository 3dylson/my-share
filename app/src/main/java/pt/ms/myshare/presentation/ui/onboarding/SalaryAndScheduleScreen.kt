package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.domain.model.PayFrequency
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
    var payFrequency by remember { mutableStateOf(initialFrequency) }
    var monthlyPaydayText by remember { mutableStateOf(initialMonthlyPayday.toString()) }
    var nextBiweeklyPaydayText by remember { mutableStateOf(initialNextBiweeklyPaydayText) }

    val income = incomeText.toBigDecimalOrNull()

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
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "We need to know when your money arrives to build a plan that actually works.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(32.dp))

            // Income Section
            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Next payday amount (Net)") },
                placeholder = { Text("e.g. 2500.00") },
                singleLine = true,
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(32.dp))

            Text("Pay frequency", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    text = "Monthly",
                    selected = payFrequency == PayFrequency.MONTHLY,
                    modifier = Modifier.weight(1f),
                    onClick = { payFrequency = PayFrequency.MONTHLY }
                )
                ChoiceCard(
                    text = "Every 2 weeks",
                    selected = payFrequency == PayFrequency.BIWEEKLY,
                    modifier = Modifier.weight(1f),
                    onClick = { payFrequency = PayFrequency.BIWEEKLY }
                )
            }

            Spacer(Modifier.height(24.dp))

            if (payFrequency == PayFrequency.MONTHLY) {
                OutlinedTextField(
                    value = monthlyPaydayText,
                    onValueChange = { monthlyPaydayText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Typical payday (Day of month)") },
                    placeholder = { Text("e.g. 25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                OutlinedTextField(
                    value = nextBiweeklyPaydayText,
                    onValueChange = { nextBiweeklyPaydayText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Next payday date (YYYY-MM-DD)") },
                    placeholder = { Text("2024-05-15") },
                    singleLine = true
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    onNext(
                        income ?: BigDecimal.ZERO,
                        payFrequency,
                        monthlyPaydayText.toIntOrNull() ?: 1,
                        nextBiweeklyPaydayText
                    )
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

@Composable
private fun ChoiceCard(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text, 
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
