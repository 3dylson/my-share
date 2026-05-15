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
    error: String? = null,
    onBack: () -> Unit,
    onNext: (BigDecimal, AllocationPreset) -> Unit
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    var fixedCostsText by remember { mutableStateOf(initialFixedCosts?.let { LocalizedAmountFormatter.formatEditableAmount(it, locale) } ?: "") }
    var preset by remember { mutableStateOf(initialPreset) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale) { LocalizedAmountFormatter.currencySymbol(locale) }
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
            onNext(fixedCosts ?: BigDecimal.ZERO, preset)
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
                placeholder = stringResource(R.string.amount_placeholder_decimal),
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
                color = MyShareOnSurface
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
