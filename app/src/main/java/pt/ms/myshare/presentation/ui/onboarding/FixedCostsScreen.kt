package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

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

    val currencySymbol = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).currency?.symbol ?: ""
    }
    val fixedCosts = fixedCostsText.toBigDecimalOrNull()
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onBack, 
                contentPadding = PaddingValues(0.dp)
            ) { 
                Text(stringResource(R.string.back), color = MyShareSecondary) 
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                stringResource(R.string.onboarding_fixed_costs_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_fixed_costs_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            PremiumTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                label = stringResource(R.string.onboarding_fixed_costs_label_amount),
                placeholder = "0.00",
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                isError = fixedCostsText.isNotEmpty() && fixedCosts == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (fixedCostsText.isNotEmpty() && fixedCosts == null) {
                Text(
                    text = stringResource(R.string.error_invalid_number),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.onboarding_fixed_costs_preset_title), 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                )
            }

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = stringResource(R.string.continue_button),
                onClick = {
                    onNext(fixedCosts ?: BigDecimal.ZERO, preset)
                },
                enabled = fixedCosts != null && fixedCosts >= BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
