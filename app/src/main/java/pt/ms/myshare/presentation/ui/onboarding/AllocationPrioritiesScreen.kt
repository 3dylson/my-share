package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.bringFocusedInputIntoView
import pt.ms.myshare.presentation.ui.components.rememberInputKeyboardActions
import pt.ms.myshare.presentation.ui.formatting.AllocationAmountConverter
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AllocationPrioritiesScreen(
    initialFlexibleSpend: BigDecimal,
    initialSavings: BigDecimal,
    initialInvesting: BigDecimal,
    initialCrypto: BigDecimal,
    initialDebt: BigDecimal,
    totalAvailable: BigDecimal,
    initialAllocationIsPercentage: Boolean,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNext: (BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, Boolean) -> Unit
) {
    val locale = userPreferences.locale
    val currencyFormat = NumberFormat.getCurrencyInstance(locale).apply { currency = userPreferences.currency }
    val currencySymbol = LocalizedAmountFormatter.currencySymbol(locale, userPreferences.currencyCode)
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }

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
    var debtAmount by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialDebt, locale)) }
    var flexPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialFlexibleSpend.formatPercentOf(totalAvailable, locale)) }
    var savPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialSavings.formatPercentOf(totalAvailable, locale)) }
    var invPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialInvesting.formatPercentOf(totalAvailable, locale)) }
    var cryPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialCrypto.formatPercentOf(totalAvailable, locale)) }
    var debtPercent by remember(userPreferences.languageTag, totalAvailable) { mutableStateOf(initialDebt.formatPercentOf(totalAvailable, locale)) }

    val parsedFlex = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) flexPercent else flexAmount, locale) ?: BigDecimal.ZERO
    val parsedSav = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) savPercent else savAmount, locale) ?: BigDecimal.ZERO
    val parsedInv = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) invPercent else invAmount, locale) ?: BigDecimal.ZERO
    val parsedCry = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) cryPercent else cryAmount, locale) ?: BigDecimal.ZERO
    val parsedDebt = LocalizedAmountFormatter.parseAmount(if (allocationIsPercentage) debtPercent else debtAmount, locale) ?: BigDecimal.ZERO

    val allocated = parsedFlex + parsedSav + parsedInv + parsedCry + parsedDebt
    val targetAllocation = if (allocationIsPercentage) BigDecimal("100") else totalAvailable
    val remaining = targetAllocation - allocated
    val hasAvailableIncome = totalAvailable > BigDecimal.ZERO
    val isOverBudget = remaining < BigDecimal.ZERO
    val isValid = hasAvailableIncome && remaining.compareTo(BigDecimal.ZERO) == 0
    val totalAvailableLabel = currencyFormat.format(totalAvailable)
    val allocatedLabel = if (allocationIsPercentage) {
        LocalizedAmountFormatter.formatPercentage(allocated, locale)
    } else {
        currencyFormat.format(allocated)
    }
    val remainingLabel = if (allocationIsPercentage) {
        LocalizedAmountFormatter.formatPercentage(remaining, locale)
    } else {
        currencyFormat.format(remaining)
    }
    val remainingMetricLabel = if (remaining >= BigDecimal.ZERO) {
        remainingLabel
    } else {
        stringResource(
            R.string.onboarding_priorities_over,
            if (allocationIsPercentage) {
                LocalizedAmountFormatter.formatPercentage(remaining.abs(), locale)
            } else {
                currencyFormat.format(remaining.abs())
            }
        )
    }
    val progress = if (targetAllocation > BigDecimal.ZERO) {
        (allocated.toDouble() / targetAllocation.toDouble()).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    @Composable
    fun equivalentText(value: BigDecimal): String {
        return if (allocationIsPercentage) {
            val amount = AllocationAmountConverter.percentageToFixedAmount(value, totalAvailable) ?: BigDecimal.ZERO
            stringResource(R.string.onboarding_priorities_equivalent_amount, currencyFormat.format(amount))
        } else {
            val percentage = AllocationAmountConverter.fixedAmountToPercentage(value, totalAvailable) ?: BigDecimal.ZERO
            stringResource(R.string.onboarding_priorities_equivalent_percent, LocalizedAmountFormatter.formatPercentage(percentage, locale))
        }
    }

    fun changeMode(usePercentage: Boolean) {
        if (usePercentage == allocationIsPercentage) return
        if (usePercentage) {
            flexPercent = fixedAmountTextToPercentageText(flexAmount)
            savPercent = fixedAmountTextToPercentageText(savAmount)
            invPercent = fixedAmountTextToPercentageText(invAmount)
            cryPercent = fixedAmountTextToPercentageText(cryAmount)
            debtPercent = fixedAmountTextToPercentageText(debtAmount)
        } else {
            flexAmount = percentageTextToFixedAmountText(flexPercent)
            savAmount = percentageTextToFixedAmountText(savPercent)
            invAmount = percentageTextToFixedAmountText(invPercent)
            cryAmount = percentageTextToFixedAmountText(cryPercent)
            debtAmount = percentageTextToFixedAmountText(debtPercent)
        }
        allocationIsPercentage = usePercentage
    }
    val inputKeyboardActions = rememberInputKeyboardActions(
        onDone = {
            if (isValid) {
                onNext(parsedFlex, parsedSav, parsedInv, parsedCry, parsedDebt, allocationIsPercentage)
            }
        }
    )

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_priorities_title),
        subtitle = stringResource(R.string.onboarding_priorities_subtitle, totalAvailableLabel),
        actionText = if (isValid) {
            stringResource(R.string.onboarding_priorities_button_ready)
        } else {
            stringResource(R.string.onboarding_priorities_button_balance)
        },
        actionEnabled = isValid,
        onBack = onBack,
        onAction = { onNext(parsedFlex, parsedSav, parsedInv, parsedCry, parsedDebt, allocationIsPercentage) }
    ) {
        AllocationSummaryCard(
            totalAvailableLabel = totalAvailableLabel,
            allocatedLabel = allocatedLabel,
            remainingMetricLabel = remainingMetricLabel,
            progress = progress,
            isOverBudget = isOverBudget
        )

        if (!hasAvailableIncome) {
            OnboardingValidationText(
                text = stringResource(R.string.onboarding_priorities_error_no_available_income),
                modifier = Modifier.padding(top = 12.dp)
            )
        } else if (isOverBudget) {
            OnboardingValidationText(
                text = stringResource(R.string.onboarding_priorities_error_over_budget),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        AllocationAdaptiveCue()

        Spacer(Modifier.height(22.dp))

        Text(
            text = stringResource(R.string.onboarding_priorities_mode_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        AllocationModeSelector(
            percentageSelected = allocationIsPercentage,
            onPercentageSelected = { changeMode(true) },
            onFixedSelected = { changeMode(false) }
        )

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AllocationCategoryCard(
                title = stringResource(R.string.onboarding_priorities_label_flex),
                description = stringResource(R.string.onboarding_priorities_desc_flex),
                icon = Icons.Default.Flag,
                accentColor = MySharePrimary,
                value = if (allocationIsPercentage) flexPercent else flexAmount,
                onValueChange = {
                    if (allocationIsPercentage) {
                        flexPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    } else {
                        flexAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    }
                },
                prefixText = if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else currencySymbol,
                placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                inputLabel = if (allocationIsPercentage) stringResource(R.string.onboarding_priorities_input_percent) else stringResource(R.string.onboarding_priorities_input_amount),
                equivalentText = equivalentText(parsedFlex),
                imeAction = ImeAction.Next,
                keyboardActions = inputKeyboardActions
            )
            AllocationCategoryCard(
                title = stringResource(R.string.onboarding_priorities_label_sav),
                description = stringResource(R.string.onboarding_priorities_desc_sav),
                icon = Icons.Default.Savings,
                accentColor = MySharePositive,
                value = if (allocationIsPercentage) savPercent else savAmount,
                onValueChange = {
                    if (allocationIsPercentage) {
                        savPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    } else {
                        savAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    }
                },
                prefixText = if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else currencySymbol,
                placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                inputLabel = if (allocationIsPercentage) stringResource(R.string.onboarding_priorities_input_percent) else stringResource(R.string.onboarding_priorities_input_amount),
                equivalentText = equivalentText(parsedSav),
                imeAction = ImeAction.Next,
                keyboardActions = inputKeyboardActions
            )
            AllocationCategoryCard(
                title = stringResource(R.string.onboarding_priorities_label_inv),
                description = stringResource(R.string.onboarding_priorities_desc_inv),
                icon = Icons.Default.AutoGraph,
                accentColor = MySharePositive,
                value = if (allocationIsPercentage) invPercent else invAmount,
                onValueChange = {
                    if (allocationIsPercentage) {
                        invPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    } else {
                        invAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    }
                },
                prefixText = if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else currencySymbol,
                placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                inputLabel = if (allocationIsPercentage) stringResource(R.string.onboarding_priorities_input_percent) else stringResource(R.string.onboarding_priorities_input_amount),
                equivalentText = equivalentText(parsedInv),
                imeAction = ImeAction.Next,
                keyboardActions = inputKeyboardActions
            )
            AllocationCategoryCard(
                title = stringResource(R.string.onboarding_priorities_label_cry),
                description = stringResource(R.string.onboarding_priorities_desc_cry),
                icon = Icons.Default.AutoAwesome,
                accentColor = MySharePrimary,
                value = if (allocationIsPercentage) cryPercent else cryAmount,
                onValueChange = {
                    if (allocationIsPercentage) {
                        cryPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    } else {
                        cryAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    }
                },
                prefixText = if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else currencySymbol,
                placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                inputLabel = if (allocationIsPercentage) stringResource(R.string.onboarding_priorities_input_percent) else stringResource(R.string.onboarding_priorities_input_amount),
                equivalentText = equivalentText(parsedCry),
                imeAction = ImeAction.Next,
                keyboardActions = inputKeyboardActions
            )
            AllocationCategoryCard(
                title = stringResource(R.string.onboarding_priorities_label_debt),
                description = stringResource(R.string.onboarding_priorities_desc_debt),
                icon = Icons.Default.AccountBalance,
                accentColor = MyShareWarning,
                value = if (allocationIsPercentage) debtPercent else debtAmount,
                onValueChange = {
                    if (allocationIsPercentage) {
                        debtPercent = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    } else {
                        debtAmount = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    }
                },
                prefixText = if (allocationIsPercentage) stringResource(R.string.percentage_prefix) else currencySymbol,
                placeholder = if (allocationIsPercentage) stringResource(R.string.rule_add_hint_rate) else amountPlaceholder,
                inputLabel = if (allocationIsPercentage) stringResource(R.string.onboarding_priorities_input_percent) else stringResource(R.string.onboarding_priorities_input_amount),
                equivalentText = equivalentText(parsedDebt),
                imeAction = ImeAction.Done,
                keyboardActions = inputKeyboardActions
            )
        }
    }
}

@Composable
private fun AllocationSummaryCard(
    totalAvailableLabel: String,
    allocatedLabel: String,
    remainingMetricLabel: String,
    progress: Float,
    isOverBudget: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(8.dp).size(19.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_priorities_summary_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = totalAvailableLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.onboarding_priorities_summary_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackMetrics = maxWidth < 240.dp
                if (stackMetrics) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllocationMetric(
                            label = stringResource(R.string.onboarding_priorities_metric_allocated),
                            value = allocatedLabel,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AllocationMetric(
                            label = stringResource(R.string.onboarding_priorities_metric_remaining),
                            value = remainingMetricLabel,
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MySharePrimary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AllocationMetric(
                            label = stringResource(R.string.onboarding_priorities_metric_allocated),
                            value = allocatedLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        AllocationMetric(
                            label = stringResource(R.string.onboarding_priorities_metric_remaining),
                            value = remainingMetricLabel,
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MySharePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MySharePrimary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AllocationMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AllocationAdaptiveCue() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(8.dp).size(19.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_priorities_adaptive_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.onboarding_priorities_adaptive_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun AllocationModeSelector(
    percentageSelected: Boolean,
    onPercentageSelected: () -> Unit,
    onFixedSelected: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AllocationModeCard(
            title = stringResource(R.string.onboarding_priorities_mode_percentage),
            selected = percentageSelected,
            onClick = onPercentageSelected,
            modifier = Modifier.weight(1f)
        )
        AllocationModeCard(
            title = stringResource(R.string.onboarding_priorities_mode_fixed),
            selected = !percentageSelected,
            onClick = onFixedSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AllocationModeCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MySharePrimary.copy(alpha = if (selected) 0.18f else 0.11f)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(6.dp).size(17.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.content_description_selected),
                    tint = MySharePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AllocationCategoryCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    value: String,
    onValueChange: (String) -> Unit,
    prefixText: String,
    placeholder: String,
    inputLabel: String,
    equivalentText: String,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(8.dp).size(19.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .bringFocusedInputIntoView(debugLabel = title)
                    .fillMaxWidth(),
                label = { Text(inputLabel) },
                placeholder = { Text(placeholder) },
                prefix = {
                    if (prefixText.isNotBlank()) {
                        Text("$prefixText ")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MySharePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MySharePrimary,
                    cursorColor = MySharePrimary
                )
            )

            Text(
                text = equivalentText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun BigDecimal.formatPercentOf(total: BigDecimal, locale: Locale): String {
    if (total <= BigDecimal.ZERO) return LocalizedAmountFormatter.formatEditableAmount(BigDecimal.ZERO, locale)
    val percent = divide(total, 6, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
    return LocalizedAmountFormatter.formatEditableAmount(percent, locale)
}
