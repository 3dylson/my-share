package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PayFrequency
import java.math.BigDecimal

@Composable
fun SalaryAndScheduleScreen(
    initialIncome: BigDecimal?,
    initialFixedCosts: BigDecimal?,
    initialFrequency: PayFrequency,
    initialMonthlyPayday: Int,
    initialNextBiweeklyPaydayText: String,
    initialPreset: AllocationPreset,
    onBack: () -> Unit,
    onNext: (BigDecimal, BigDecimal, PayFrequency, Int, String, AllocationPreset) -> Unit
) {
    var incomeText by remember { mutableStateOf(initialIncome?.toPlainString() ?: "") }
    var fixedCostsText by remember { mutableStateOf(initialFixedCosts?.toPlainString() ?: "") }
    var payFrequency by remember { mutableStateOf(initialFrequency) }
    var monthlyPaydayText by remember { mutableStateOf(initialMonthlyPayday.toString()) }
    var nextBiweeklyPaydayText by remember { mutableStateOf(initialNextBiweeklyPaydayText) }
    var preset by remember { mutableStateOf(initialPreset) }

    val income = incomeText.toBigDecimalOrNull()
    val fixedCosts = fixedCostsText.toBigDecimalOrNull()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Tell My Share about your payday", style = MaterialTheme.typography.headlineMedium)
            Text("Keep it light. These are the only numbers needed to create a first useful plan.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Net income per payday") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly fixed costs") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Pay frequency", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    text = "Monthly",
                    selected = payFrequency == PayFrequency.MONTHLY,
                    onClick = { payFrequency = PayFrequency.MONTHLY }
                )
                ChoiceCard(
                    text = "Every 2 weeks",
                    selected = payFrequency == PayFrequency.BIWEEKLY,
                    onClick = { payFrequency = PayFrequency.BIWEEKLY }
                )
            }

            if (payFrequency == PayFrequency.MONTHLY) {
                OutlinedTextField(
                    value = monthlyPaydayText,
                    onValueChange = { monthlyPaydayText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Typical payday day of month") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                OutlinedTextField(
                    value = nextBiweeklyPaydayText,
                    onValueChange = { nextBiweeklyPaydayText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Next payday (YYYY-MM-DD)") },
                    singleLine = true
                )
            }

            Text("Plan style", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PresetChip("Conservative", preset == AllocationPreset.CONSERVATIVE) { preset = AllocationPreset.CONSERVATIVE }
                PresetChip("Balanced", preset == AllocationPreset.BALANCED) { preset = AllocationPreset.BALANCED }
                PresetChip("Growth", preset == AllocationPreset.GROWTH) { preset = AllocationPreset.GROWTH }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    onNext(
                        income ?: BigDecimal.ZERO,
                        fixedCosts ?: BigDecimal.ZERO,
                        payFrequency,
                        monthlyPaydayText.toIntOrNull() ?: 1,
                        nextBiweeklyPaydayText,
                        preset
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = income != null && income > BigDecimal.ZERO && fixedCosts != null && fixedCosts >= BigDecimal.ZERO
            ) {
                Text("See my plan")
            }
        }
    }
}

@Composable
private fun ChoiceCard(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFD5DDE2)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    }
}

@Composable
private fun PresetChip(text: String, selected: Boolean, onClick: () -> Unit) {
    ChoiceCard(text = text, selected = selected, onClick = onClick)
}
