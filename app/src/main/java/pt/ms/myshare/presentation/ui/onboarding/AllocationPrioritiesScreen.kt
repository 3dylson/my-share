package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.AllocationAmountConverter
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllocationPrioritiesScreen(
    initialFlexibleSpend: BigDecimal,
    initialSavings: BigDecimal,
    initialInvesting: BigDecimal,
    initialCrypto: BigDecimal,
    totalAvailable: BigDecimal,
    initialAllocationIsPercentage: Boolean,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNext: (BigDecimal, BigDecimal, BigDecimal, BigDecimal, Boolean) -> Unit
) {
    val locale = userPreferences.locale
    fun fixedAmountTextToPercentageText(amountText: String): String {
        val amount = LocalizedAmountFormatter.parseAmount(amountText, locale) ?: BigDecimal.ZERO
        val percentage = AllocationAmountConverter.fixedAmountToPercentage(amount, totalAvailable) ?: amount
        return LocalizedAmountFormatter.formatEditableAmount(percentage, locale)
    }

    fun percentageTextToFixedAmountText(percentageText: String): String {
        val percentage = LocalizedAmountFormatter.parseAmount(percentageText, locale) ?: BigDecimal.ZERO
        val amount = AllocationAmountConverter.percentageToFixedAmount(percentage, totalAvailable) ?: percentage
        return LocalizedAmountFormatter.formatEditableAmount(amount, locale)
    }

    var allocationIsPercentage by remember(initialAllocationIsPercentage) { mutableStateOf(initialAllocationIsPercentage) }
    var flexAmount by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialFlexibleSpend, locale)) }
    var savAmount by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialSavings, locale)) }
    var invAmount by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialInvesting, locale)) }
    var cryAmount by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialCrypto, locale)) }
    var flexPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialFlexibleSpend.formatPercentOf(totalAvailable, locale)) }
    var savPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialSavings.formatPercentOf(totalAvailable, locale)) }
    var invPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialInvesting.formatPercentOf(totalAvailable, locale)) }
    var cryPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialCrypto.formatPercentOf(totalAvailable, locale)) }

    val parsedFlex = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) flexPercent else flexAmount, locale) ?: BigDecimal.ZERO
    val parsedSav = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) savPercent else savAmount, locale) ?: BigDecimal.ZERO
    val parsedInv = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) invPercent else invAmount, locale) ?: BigDecimal.ZERO
    val parsedCry = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) cryPercent else cryAmount, locale) ?: BigDecimal.ZERO

    val allocated = parsedFlex + parsedSav + parsedInv + parsedCry
    val targetAllocation = if (allocationIsPercentage) BigDecimal("100") else totalAvailable
    val remaining = targetAllocation - allocated
    val hasAvailableIncome = totalAvailable > BigDecimal.ZERO
    val isValid = hasAvailableIncome && remaining.compareTo(BigDecimal.ZERO) == 0

    val currency = NumberFormat.getCurrencyInstance(locale).apply { currency = userPreferences.currency }
    val symbol = LocalizedAmountFormatter.currencySymbol(locale, userPreferences.currencyCode)
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }
    val allocatedLabel = if (allocationIsPercentage) {
        LocalizedAmountFormatter.formatPercentage(allocated, locale)
    } else {
        currency.format(allocated)
    }
    val remainingLabel = if (allocationIsPercentage) {
        LocalizedAmountFormatter.formatPercentage(remaining, locale)
    } else {
        currency.format(remaining)
    }

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
        onAction = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry, allocationIsPercentage) }
    ) {
            val progress = if (targetAllocation > BigDecimal.ZERO) {
                (allocated.toDouble() / targetAllocation.toDouble()).coerceIn(0.0, 1.1).toFloat()
            } else 0f
            
            Column {
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(Modifier.height(12.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val remainingText = if (remaining >= BigDecimal.ZERO) {
                        stringResource(R.string.onboarding_priorities_remaining, remainingLabel)
                    } else {
                        stringResource(
                            R.string.onboarding_priorities_over,
                            if (allocationIsPercentage) {
                                LocalizedAmountFormatter.formatPercentage(remaining.abs(), locale)
                            } else {
                                currency.format(remaining.abs())
                            }
                        )
                    }
                    val shouldStack = maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.3f
                    if (shouldStack) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AllocationProgressLabel(
                                text = stringResource(R.string.onboarding_priorities_allocated, allocatedLabel),
                                color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AllocationProgressLabel(
                                text = remainingText,
                                color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary,
                                emphasized = true
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AllocationProgressLabel(
                                text = stringResource(R.string.onboarding_priorities_allocated, allocatedLabel),
                                color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            AllocationProgressLabel(
                                text = remainingText,
                                color = if (remaining < BigDecimal.ZERO) MaterialTheme.colorScheme.error else MySharePrimary,
                                emphasized = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.rule_add_label_type),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = allocationIsPercentage,
                            onClick = {
                                if (!allocationIsPercentage) {
                                    flexPercent = fixedAmountTextToPercentageText(flexAmount)
                                    savPercent = fixedAmountTextToPercentageText(savAmount)
                                    invPercent = fixedAmountTextToPercentageText(invAmount)
                                    cryPercent = fixedAmountTextToPercentageText(cryAmount)
                                }
                                allocationIsPercentage = true
                            },
                            label = { Text(stringResource(R.string.rule_add_type_percentage)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = MySharePrimary,
                                selectedLeadingIconColor = MySharePrimary
                            )
                        )
                        FilterChip(
                            selected = !allocationIsPercentage,
                            onClick = {
                                if (allocationIsPercentage) {
                                    flexAmount = percentageTextToFixedAmountText(flexPercent)
                                    savAmount = percentageTextToFixedAmountText(savPercent)
                                    invAmount = percentageTextToFixedAmountText(invPercent)
                                    cryAmount = percentageTextToFixedAmountText(cryPercent)
                                }
                                allocationIsPercentage = false
                            },
                            label = { Text(stringResource(R.string.rule_add_type_fixed)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = MySharePrimary,
                                selectedLeadingIconColor = MySharePrimary
                            )
                        )
                    }
                }

                PremiumTextField(
                    value = if (allocationIsPercentage) flexPercent else flexAmount,
                    onValueChange = {
                        if (allocationIsPercentage) {
                            flexPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        } else {
                            flexAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        }
                    },
                    label = stringResource(R.string.onboarding_priorities_label_flex),
                    prefix = { Text(if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else "$symbol ") },
                    placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                    description = stringResource(R.string.onboarding_priorities_desc_flex),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = if (allocationIsPercentage) savPercent else savAmount,
                    onValueChange = {
                        if (allocationIsPercentage) {
                            savPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        } else {
                            savAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        }
                    },
                    label = stringResource(R.string.onboarding_priorities_label_sav),
                    prefix = { Text(if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else "$symbol ") },
                    placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                    description = stringResource(R.string.onboarding_priorities_desc_sav),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = if (allocationIsPercentage) invPercent else invAmount,
                    onValueChange = {
                        if (allocationIsPercentage) {
                            invPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        } else {
                            invAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        }
                    },
                    label = stringResource(R.string.onboarding_priorities_label_inv),
                    prefix = { Text(if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else "$symbol ") },
                    placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                    description = stringResource(R.string.onboarding_priorities_desc_inv),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                PremiumTextField(
                    value = if (allocationIsPercentage) cryPercent else cryAmount,
                    onValueChange = {
                        if (allocationIsPercentage) {
                            cryPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        } else {
                            cryAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                        }
                    },
                    label = stringResource(R.string.onboarding_priorities_label_cry),
                    prefix = { Text(if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else "$symbol ") },
                    placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                    description = stringResource(R.string.onboarding_priorities_desc_cry),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
    }
}

@Composable
private fun AllocationProgressLabel(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = modifier
    )
}

private fun BigDecimal.formatPercentOf(total: BigDecimal, locale: Locale): String {
    if (total <= BigDecimal.ZERO) return LocalizedAmountFormatter.formatEditableAmount(BigDecimal.ZERO, locale)
    val percent = divide(total, 6, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
    return LocalizedAmountFormatter.formatEditableAmount(percent, locale)
}
