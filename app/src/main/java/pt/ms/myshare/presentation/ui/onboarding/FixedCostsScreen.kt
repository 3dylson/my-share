package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal

@Composable
fun FixedCostsScreen(
    initialFixedCosts: BigDecimal?,
    incomePerPayday: BigDecimal?,
    initialPreset: AllocationPreset,
    initialStrategy: AllocationStrategy,
    initialCustomStrategyName: String,
    userPreferences: UserPreferences,
    error: String? = null,
    onBack: () -> Unit,
    onNext: (BigDecimal, AllocationPreset, AllocationStrategy, String?) -> Unit
) {
    val context = LocalContext.current
    val locale = userPreferences.locale
    var fixedCostsText by remember(userPreferences.languageTag) { mutableStateOf(initialFixedCosts?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) } ?: "") }
    var preset by remember { mutableStateOf(initialPreset) }
    var strategy by remember { mutableStateOf(initialStrategy) }
    var customStrategyName by remember { mutableStateOf(initialCustomStrategyName) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale, userPreferences.currencyCode) {
        LocalizedAmountFormatter.currencySymbol(locale, userPreferences.currencyCode)
    }
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }
    val fixedCosts = LocalizedAmountFormatter.parseAmount(fixedCostsText, locale)
    val invalidNumber = fixedCostsText.isNotBlank() && fixedCosts == null
    val requiredError = validationRequested && fixedCostsText.isBlank()
    val negativeError = validationRequested && fixedCosts != null && fixedCosts < BigDecimal.ZERO
    val exceedsIncomeError = fixedCosts != null && incomePerPayday != null && fixedCosts > incomePerPayday
    val fixedCostsError = requiredError || invalidNumber || negativeError || exceedsIncomeError
    val protectedAmountPreview = fixedCosts
        ?.takeIf { it >= BigDecimal.ZERO }
        ?.let {
            LocalizedAmountFormatter.formatCurrency(
                amount = it,
                locale = locale,
                currencyCode = userPreferences.currencyCode
            )
        }
    val remainingAfterFixedCosts = if (
        fixedCosts != null &&
        fixedCosts >= BigDecimal.ZERO &&
        incomePerPayday != null &&
        fixedCosts <= incomePerPayday
    ) {
        incomePerPayday.subtract(fixedCosts)
    } else {
        null
    }
    val remainingPreview = remainingAfterFixedCosts?.let {
        LocalizedAmountFormatter.formatCurrency(
            amount = it,
            locale = locale,
            currencyCode = userPreferences.currencyCode
        )
    }
    val isFixedCostsValid = fixedCosts != null &&
        fixedCosts >= BigDecimal.ZERO &&
        (incomePerPayday == null || fixedCosts <= incomePerPayday)

    fun continueIfValid() {
        validationRequested = true
        if (isFixedCostsValid) {
            onNext(fixedCosts ?: BigDecimal.ZERO, preset, strategy, customStrategyName)
        }
    }

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_fixed_costs_title),
        subtitle = stringResource(R.string.onboarding_fixed_costs_subtitle),
        actionText = stringResource(R.string.continue_button),
        progressStep = 3,
        progressTotal = OnboardingViewModel.SETUP_STEP_TOTAL,
        onBack = onBack,
        onAction = ::continueIfValid
    ) {
        FixedCostsProtectionCard(
            protectedAmountPreview = protectedAmountPreview,
            remainingPreview = remainingPreview
        ) {
            PremiumTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = LocalizedAmountFormatter.sanitizeAmountInput(it, locale) },
                label = stringResource(R.string.onboarding_fixed_costs_label_amount),
                placeholder = amountPlaceholder,
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                isError = fixedCostsError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            when {
                requiredError -> OnboardingValidationText(stringResource(R.string.onboarding_fixed_costs_error_required))
                invalidNumber -> OnboardingValidationText(stringResource(R.string.error_invalid_number))
                negativeError -> OnboardingValidationText(stringResource(R.string.onboarding_fixed_costs_error_negative))
                exceedsIncomeError -> OnboardingValidationText(stringResource(R.string.onboarding_fixed_costs_error_exceeds_income))
            }
        }

        Spacer(Modifier.height(16.dp))

        FixedCostsAutomationCue()

        Spacer(Modifier.height(22.dp))

        Text(
            stringResource(R.string.onboarding_fixed_costs_preset_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))

        val presetOptions = listOf(
            FixedCostPresetOption(
                preset = AllocationPreset.CONSERVATIVE,
                title = stringResource(R.string.onboarding_fixed_costs_preset_conservative),
                description = stringResource(R.string.onboarding_fixed_costs_preset_conservative_desc),
                icon = Icons.Default.Security,
                accentColor = MyShareWarning
            ),
            FixedCostPresetOption(
                preset = AllocationPreset.BALANCED,
                title = stringResource(R.string.onboarding_fixed_costs_preset_balanced),
                description = stringResource(R.string.onboarding_fixed_costs_preset_balanced_desc),
                icon = Icons.Default.Tune,
                accentColor = MySharePrimary
            ),
            FixedCostPresetOption(
                preset = AllocationPreset.GROWTH,
                title = stringResource(R.string.onboarding_fixed_costs_preset_growth),
                description = stringResource(R.string.onboarding_fixed_costs_preset_growth_desc),
                icon = Icons.Default.AutoGraph,
                accentColor = MySharePositive
            )
        )
        FixedCostPresetSelector(
            options = presetOptions,
            selectedPreset = preset,
            onPresetSelected = { preset = it }
        )

        Spacer(Modifier.height(22.dp))

        Text(
            stringResource(R.string.onboarding_fixed_costs_strategy_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(10.dp))

        val strategyOptions = listOf(
            FixedCostStrategyOption(
                strategy = AllocationStrategy.BALANCED_SAVINGS,
                title = stringResource(R.string.onboarding_strategy_balanced_savings),
                description = stringResource(R.string.onboarding_strategy_balanced_savings_desc),
                icon = Icons.Default.Savings,
                accentColor = MySharePositive
            ),
            FixedCostStrategyOption(
                strategy = AllocationStrategy.NO_SAVINGS_NOW,
                title = stringResource(R.string.onboarding_strategy_no_savings),
                description = stringResource(R.string.onboarding_strategy_no_savings_desc),
                icon = Icons.Default.Tune,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            FixedCostStrategyOption(
                strategy = AllocationStrategy.DEBT_FIRST,
                title = stringResource(R.string.onboarding_strategy_debt_first),
                description = stringResource(R.string.onboarding_strategy_debt_first_desc),
                icon = Icons.Default.AccountBalance,
                accentColor = MyShareWarning
            ),
            FixedCostStrategyOption(
                strategy = AllocationStrategy.INVESTING_FIRST,
                title = stringResource(R.string.onboarding_strategy_investing_first),
                description = stringResource(R.string.onboarding_strategy_investing_first_desc),
                icon = Icons.Default.AutoGraph,
                accentColor = MySharePositive
            ),
            FixedCostStrategyOption(
                strategy = AllocationStrategy.FLEXIBLE_BUDGET_ONLY,
                title = stringResource(R.string.onboarding_strategy_flexible_only),
                description = stringResource(R.string.onboarding_strategy_flexible_only_desc),
                icon = Icons.Default.Flag,
                accentColor = MySharePrimary
            ),
            FixedCostStrategyOption(
                strategy = AllocationStrategy.CUSTOM,
                title = stringResource(R.string.onboarding_strategy_custom),
                description = stringResource(R.string.onboarding_strategy_custom_desc),
                icon = Icons.Default.Edit,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        FixedCostStrategyGrid(
            options = strategyOptions,
            selectedStrategy = strategy,
            onStrategySelected = { strategy = it }
        )

        if (strategy == AllocationStrategy.CUSTOM) {
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = customStrategyName,
                onValueChange = { customStrategyName = it },
                label = stringResource(R.string.onboarding_strategy_custom_label),
                placeholder = stringResource(R.string.onboarding_strategy_custom_placeholder)
            )
        }

        if (error != null) {
            val errorText = remember(error) {
                val resId = context.resources.getIdentifier(error, "string", context.packageName)
                if (resId != 0) context.getString(resId) else error
            }
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp)
            )
        }
    }
}

@Composable
private fun FixedCostsProtectionCard(
    protectedAmountPreview: String?,
    remainingPreview: String?,
    content: @Composable ColumnScope.() -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FixedCostsSectionHeader(
                title = protectedAmountPreview?.let {
                    stringResource(R.string.onboarding_fixed_costs_protection_amount, it)
                } ?: stringResource(R.string.onboarding_fixed_costs_protection_title),
                body = if (protectedAmountPreview == null) {
                    stringResource(R.string.onboarding_fixed_costs_protection_body)
                } else {
                    stringResource(R.string.onboarding_fixed_costs_protection_amount_body)
                },
                icon = Icons.Default.Security,
                tint = MySharePrimary
            )

            content()

            if (remainingPreview != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_fixed_costs_remaining_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = remainingPreview,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FixedCostsAutomationCue() {
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
                    text = stringResource(R.string.onboarding_fixed_costs_automation_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.onboarding_fixed_costs_automation_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FixedCostPresetSelector(
    options: List<FixedCostPresetOption>,
    selectedPreset: AllocationPreset,
    onPresetSelected: (AllocationPreset) -> Unit
) {
    val selectedOption = options.first { it.preset == selectedPreset }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FixedCostPresetTile(
                    option = option,
                    isSelected = option.preset == selectedPreset,
                    onClick = { onPresetSelected(option.preset) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        FixedCostOptionDescription(
            text = selectedOption.description,
            icon = selectedOption.icon,
            tint = selectedOption.accentColor
        )
    }
}

@Composable
private fun FixedCostPresetTile(
    option: FixedCostPresetOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = option.accentColor.copy(alpha = if (isSelected) 0.18f else 0.11f)
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.accentColor,
                        modifier = Modifier.padding(6.dp).size(17.dp)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_selected),
                        tint = MySharePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = option.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FixedCostStrategyGrid(
    options: List<FixedCostStrategyOption>,
    selectedStrategy: AllocationStrategy,
    onStrategySelected: (AllocationStrategy) -> Unit
) {
    val selectedOption = options.first { it.strategy == selectedStrategy }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowOptions.forEach { option ->
                    FixedCostStrategyCard(
                        option = option,
                        isSelected = option.strategy == selectedStrategy,
                        onClick = { onStrategySelected(option.strategy) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        FixedCostOptionDescription(
            text = selectedOption.description,
            icon = selectedOption.icon,
            tint = selectedOption.accentColor
        )
    }
}

@Composable
private fun FixedCostStrategyCard(
    option: FixedCostStrategyOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 76.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = option.accentColor.copy(alpha = if (isSelected) 0.18f else 0.11f)
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = option.accentColor,
                    modifier = Modifier.padding(7.dp).size(18.dp)
                )
            }
            Text(
                text = option.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
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
private fun FixedCostOptionDescription(
    text: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FixedCostsSectionHeader(
    title: String,
    body: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tint.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
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
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

private data class FixedCostPresetOption(
    val preset: AllocationPreset,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

private data class FixedCostStrategyOption(
    val strategy: AllocationStrategy,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)
