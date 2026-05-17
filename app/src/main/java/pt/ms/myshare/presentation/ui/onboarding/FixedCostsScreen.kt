package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.AllocationStrategy
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.util.Locale

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
        onBack = onBack,
        onAction = ::continueIfValid
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

            Spacer(Modifier.height(28.dp))

            Text(
                stringResource(R.string.onboarding_fixed_costs_preset_title), 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val presets = listOf(
                    Triple(AllocationPreset.CONSERVATIVE, stringResource(R.string.onboarding_fixed_costs_preset_conservative), stringResource(R.string.onboarding_fixed_costs_preset_conservative_desc)),
                    Triple(AllocationPreset.BALANCED, stringResource(R.string.onboarding_fixed_costs_preset_balanced), stringResource(R.string.onboarding_fixed_costs_preset_balanced_desc)),
                    Triple(AllocationPreset.GROWTH, stringResource(R.string.onboarding_fixed_costs_preset_growth), stringResource(R.string.onboarding_fixed_costs_preset_growth_desc))
                )
                
                presets.forEach { (p, label, desc) ->
                    PremiumChoiceCard(
                        title = label,
                        description = desc,
                        isSelected = preset == p,
                        onClick = { preset = p }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                stringResource(R.string.onboarding_fixed_costs_strategy_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val strategies = listOf(
                    Triple(AllocationStrategy.BALANCED_SAVINGS, stringResource(R.string.onboarding_strategy_balanced_savings), stringResource(R.string.onboarding_strategy_balanced_savings_desc)),
                    Triple(AllocationStrategy.NO_SAVINGS_NOW, stringResource(R.string.onboarding_strategy_no_savings), stringResource(R.string.onboarding_strategy_no_savings_desc)),
                    Triple(AllocationStrategy.DEBT_FIRST, stringResource(R.string.onboarding_strategy_debt_first), stringResource(R.string.onboarding_strategy_debt_first_desc)),
                    Triple(AllocationStrategy.INVESTING_FIRST, stringResource(R.string.onboarding_strategy_investing_first), stringResource(R.string.onboarding_strategy_investing_first_desc)),
                    Triple(AllocationStrategy.FLEXIBLE_BUDGET_ONLY, stringResource(R.string.onboarding_strategy_flexible_only), stringResource(R.string.onboarding_strategy_flexible_only_desc)),
                    Triple(AllocationStrategy.CUSTOM, stringResource(R.string.onboarding_strategy_custom), stringResource(R.string.onboarding_strategy_custom_desc))
                )

                strategies.forEach { (item, label, desc) ->
                    PremiumChoiceCard(
                        title = label,
                        description = desc,
                        isSelected = strategy == item,
                        onClick = { strategy = item }
                    )
                }
            }

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
