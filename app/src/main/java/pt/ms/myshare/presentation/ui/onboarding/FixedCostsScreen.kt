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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.domain.model.AllocationPreset
import java.math.BigDecimal

@Composable
fun FixedCostsScreen(
    initialFixedCosts: BigDecimal?,
    initialPreset: AllocationPreset,
    error: String? = null,
    onBack: () -> Unit,
    onNext: (BigDecimal, AllocationPreset) -> Unit
) {
    var fixedCostsText by remember { mutableStateOf(initialFixedCosts?.toPlainString() ?: "") }
    var preset by remember { mutableStateOf(initialPreset) }

    val fixedCosts = fixedCostsText.toBigDecimalOrNull()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.height(8.dp))
            Text("How much must leave your account every month?", style = MaterialTheme.typography.headlineMedium)
            Text("Rent, utilities, and other absolute essentials.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly fixed costs") },
                placeholder = { Text("e.g. 1200.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = fixedCostsText.isNotEmpty() && fixedCosts == null
            )

            if (fixedCostsText.isNotEmpty() && fixedCosts == null) {
                Text(
                    text = "Please enter a valid number for costs.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("Plan style", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PresetChip("Conservative", preset == AllocationPreset.CONSERVATIVE) { preset = AllocationPreset.CONSERVATIVE }
                PresetChip("Balanced", preset == AllocationPreset.BALANCED) { preset = AllocationPreset.BALANCED }
                PresetChip("Growth", preset == AllocationPreset.GROWTH) { preset = AllocationPreset.GROWTH }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    onNext(fixedCosts ?: BigDecimal.ZERO, preset)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = fixedCosts != null && fixedCosts >= BigDecimal.ZERO,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ChoiceCard(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
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
