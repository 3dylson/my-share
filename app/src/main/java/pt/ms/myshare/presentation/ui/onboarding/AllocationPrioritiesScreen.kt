package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AllocationPrioritiesScreen(
    initialFlexibleSpend: BigDecimal,
    initialSavings: BigDecimal,
    initialInvesting: BigDecimal,
    initialCrypto: BigDecimal,
    totalAvailable: BigDecimal,
    onBack: () -> Unit,
    onNext: (BigDecimal, BigDecimal, BigDecimal, BigDecimal) -> Unit
) {
    var flex by remember { mutableStateOf(initialFlexibleSpend.toPlainString()) }
    var sav by remember { mutableStateOf(initialSavings.toPlainString()) }
    var inv by remember { mutableStateOf(initialInvesting.toPlainString()) }
    var cry by remember { mutableStateOf(initialCrypto.toPlainString()) }

    val parsedFlex = flex.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedSav = sav.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedInv = inv.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedCry = cry.toBigDecimalOrNull() ?: BigDecimal.ZERO

    val allocated = parsedFlex + parsedSav + parsedInv + parsedCry
    val remaining = totalAvailable - allocated
    val isValid = remaining.compareTo(BigDecimal.ZERO) == 0

    val locale = Locale.getDefault()
    val currency = NumberFormat.getCurrencyInstance(locale)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.height(8.dp))
            Text("Let's give every dollar a job", style = MaterialTheme.typography.headlineMedium)
            Text(
                "After fixed costs, you have ${currency.format(totalAvailable)} to allocate.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = flex,
                onValueChange = { flex = it },
                label = { Text("Flexible spend") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sav,
                onValueChange = { sav = it },
                label = { Text("Savings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = inv,
                onValueChange = { inv = it },
                label = { Text("Investing") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cry,
                onValueChange = { cry = it },
                label = { Text("Crypto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            val remainingColor = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Text(
                "Remaining to allocate: ${currency.format(remaining)}",
                color = remainingColor,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValid,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isValid) "Build my plan" else "Balance to 0",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
