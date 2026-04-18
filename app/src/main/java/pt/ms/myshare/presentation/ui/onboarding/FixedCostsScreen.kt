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
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) { 
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
            
            Text(
                "Essentials & Bills", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "What must leave your account every month for rent, utilities, and absolute essentials?", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly fixed costs") },
                placeholder = { Text("e.g. 1200.00") },
                singleLine = true,
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = fixedCostsText.isNotEmpty() && fixedCosts == null
            )

            if (fixedCostsText.isNotEmpty() && fixedCosts == null) {
                Text(
                    text = "Please enter a valid number for costs.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text("Allocation style", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PresetChip("Conservative", preset == AllocationPreset.CONSERVATIVE, Modifier.weight(1f)) { preset = AllocationPreset.CONSERVATIVE }
                PresetChip("Balanced", preset == AllocationPreset.BALANCED, Modifier.weight(1f)) { preset = AllocationPreset.BALANCED }
                PresetChip("Growth", preset == AllocationPreset.GROWTH, Modifier.weight(1f)) { preset = AllocationPreset.GROWTH }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text, 
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PresetChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ChoiceCard(text = text, selected = selected, modifier = modifier, onClick = onClick)
}
