package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
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
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) { 
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
            
            Text(
                "Money Allocation", 
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )
            
            Text(
                "Give every dollar a job. You have ${currency.format(totalAvailable)} to distribute.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(24.dp))

            // Progress Section
            val progress = if (totalAvailable > BigDecimal.ZERO) {
                (allocated.toDouble() / totalAvailable.toDouble()).coerceIn(0.0, 1.1).toFloat()
            } else 0f
            
            Column {
                LinearProgressIndicator(
                    progress = progress.coerceAtMost(1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Allocated: ${currency.format(allocated)}", 
                        style = MaterialTheme.typography.labelMedium,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (remaining >= BigDecimal.ZERO) "Left: ${currency.format(remaining)}" else "Over: ${currency.format(remaining.abs())}", 
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    AllocationInput(
                        title = "Flexible Spending",
                        description = "Groceries, dining, hobbies, and day-to-day life.",
                        value = flex,
                        onValueChange = { flex = it.replace(',', '.') },
                        currency = currency.currency?.symbol ?: "€"
                    )
                }
                item {
                    AllocationInput(
                        title = "Savings",
                        description = "Cushion for emergencies and short-term goals.",
                        value = sav,
                        onValueChange = { sav = it.replace(',', '.') },
                        currency = currency.currency?.symbol ?: "€"
                    )
                }
                item {
                    AllocationInput(
                        title = "Investing",
                        description = "Long-term growth and market exposure.",
                        value = inv,
                        onValueChange = { inv = it.replace(',', '.') },
                        currency = currency.currency?.symbol ?: "€"
                    )
                }
                item {
                    AllocationInput(
                        title = "Speculative / Crypto",
                        description = "High-risk, high-reward allocations.",
                        value = cry,
                        onValueChange = { cry = it.replace(',', '.') },
                        currency = currency.currency?.symbol ?: "€"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValid,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isValid) "Build My Plan" else "Remaining: ${currency.format(remaining)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AllocationInput(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    currency: String
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("$currency ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}
