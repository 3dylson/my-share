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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.domain.model.AllocationPreset
import java.math.BigDecimal

@Composable
fun FixedCostsScreen(
    initialFixedCosts: BigDecimal?,
    initialPreset: AllocationPreset,
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
            Text("Let's capture your fixed obligations", style = MaterialTheme.typography.headlineMedium)
            Text("Rent, utilities, subscriptions. What must be paid no matter what?", color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly fixed costs") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Plan style", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PresetChip("Conservative", preset == AllocationPreset.CONSERVATIVE) { preset = AllocationPreset.CONSERVATIVE }
                PresetChip("Balanced", preset == AllocationPreset.BALANCED) { preset = AllocationPreset.BALANCED }
                PresetChip("Growth", preset == AllocationPreset.GROWTH) { preset = AllocationPreset.GROWTH }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    onNext(fixedCosts ?: BigDecimal.ZERO, preset)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fixedCosts != null && fixedCosts >= BigDecimal.ZERO,
                shape = RoundedCornerShape(12.dp)
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
