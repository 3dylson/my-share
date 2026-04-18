package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

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
    val symbol = currency.currency?.symbol ?: "€"

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
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
                "Money Allocation", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                "Give every cent a job. Distribute ${currency.format(totalAvailable)} to build your plan.", 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(32.dp))

            // Progress Section
            val progress = if (totalAvailable > BigDecimal.ZERO) {
                (allocated.toDouble() / totalAvailable.toDouble()).coerceIn(0.0, 1.1).toFloat()
            } else 0f
            
            Column {
                LinearProgressIndicator(
                    progress = progress.coerceAtMost(1f),
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary,
                    trackColor = MySharePrimaryContainer,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Allocated: ${currency.format(allocated)}", 
                        style = MaterialTheme.typography.labelLarge,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MyShareSecondary
                    )
                    Text(
                        if (remaining >= BigDecimal.ZERO) "Remaining: ${currency.format(remaining)}" else "Over: ${currency.format(remaining.abs())}", 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    PremiumTextField(
                        value = flex,
                        onValueChange = { flex = it.replace(',', '.') },
                        label = "Flexible Spending",
                        prefix = { Text("$symbol ") },
                        placeholder = "0.00",
                        description = "Groceries, dining, hobbies, and day-to-day life."
                    )
                }
                item {
                    PremiumTextField(
                        value = sav,
                        onValueChange = { sav = it.replace(',', '.') },
                        label = "Savings",
                        prefix = { Text("$symbol ") },
                        placeholder = "0.00",
                        description = "Cushion for emergencies and short-term goals."
                    )
                }
                item {
                    PremiumTextField(
                        value = inv,
                        onValueChange = { inv = it.replace(',', '.') },
                        label = "Investing",
                        prefix = { Text("$symbol ") },
                        placeholder = "0.00",
                        description = "Long-term growth and market exposure."
                    )
                }
                item {
                    PremiumTextField(
                        value = cry,
                        onValueChange = { cry = it.replace(',', '.') },
                        label = "Speculative / Crypto",
                        prefix = { Text("$symbol ") },
                        placeholder = "0.00",
                        description = "High-risk allocations (optional)."
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            PremiumButton(
                text = if (isValid) "Build My Blueprint" else "Balance your allocation",
                onClick = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry) },
                enabled = isValid
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
