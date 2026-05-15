package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
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
    val hasAvailableIncome = totalAvailable > BigDecimal.ZERO
    val isValid = hasAvailableIncome && remaining.compareTo(BigDecimal.ZERO) == 0

    val locale = Locale.getDefault()
    val currency = NumberFormat.getCurrencyInstance(locale)
    val symbol = currency.currency?.symbol ?: ""

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_priorities_title),
        subtitle = stringResource(R.string.onboarding_priorities_subtitle, currency.format(totalAvailable)),
        actionText = if (isValid) {
            stringResource(R.string.onboarding_priorities_button_ready)
        } else {
            stringResource(R.string.onboarding_priorities_button_balance)
        },
        actionEnabled = isValid,
        onBack = onBack,
        onAction = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry) }
    ) {
            // Progress Section
            val progress = if (totalAvailable > BigDecimal.ZERO) {
                (allocated.toDouble() / totalAvailable.toDouble()).coerceIn(0.0, 1.1).toFloat()
            } else 0f
            
            Column {
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary,
                    trackColor = MySharePrimaryContainer,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.onboarding_priorities_allocated, currency.format(allocated)), 
                        style = MaterialTheme.typography.labelLarge,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MyShareSecondary
                    )
                    Text(
                        if (remaining >= BigDecimal.ZERO) 
                            stringResource(R.string.onboarding_priorities_remaining, currency.format(remaining))
                        else 
                            stringResource(R.string.onboarding_priorities_over, currency.format(remaining.abs())), 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary
                    )
                }
            }

            if (!hasAvailableIncome) {
                OnboardingValidationText(
                    text = stringResource(R.string.onboarding_priorities_error_no_available_income),
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else if (remaining < BigDecimal.ZERO) {
                OnboardingValidationText(
                    text = stringResource(R.string.onboarding_priorities_error_over_budget),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                PremiumTextField(
                    value = flex,
                    onValueChange = { flex = it.replace(',', '.') },
                    label = stringResource(R.string.onboarding_priorities_label_flex),
                    prefix = { Text("$symbol ") },
                    placeholder = stringResource(R.string.amount_placeholder_decimal),
                    description = stringResource(R.string.onboarding_priorities_desc_flex),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = sav,
                    onValueChange = { sav = it.replace(',', '.') },
                    label = stringResource(R.string.onboarding_priorities_label_sav),
                    prefix = { Text("$symbol ") },
                    placeholder = stringResource(R.string.amount_placeholder_decimal),
                    description = stringResource(R.string.onboarding_priorities_desc_sav),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = inv,
                    onValueChange = { inv = it.replace(',', '.') },
                    label = stringResource(R.string.onboarding_priorities_label_inv),
                    prefix = { Text("$symbol ") },
                    placeholder = stringResource(R.string.amount_placeholder_decimal),
                    description = stringResource(R.string.onboarding_priorities_desc_inv),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = cry,
                    onValueChange = { cry = it.replace(',', '.') },
                    label = stringResource(R.string.onboarding_priorities_label_cry),
                    prefix = { Text("$symbol ") },
                    placeholder = stringResource(R.string.amount_placeholder_decimal),
                    description = stringResource(R.string.onboarding_priorities_desc_cry),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
    }
}
