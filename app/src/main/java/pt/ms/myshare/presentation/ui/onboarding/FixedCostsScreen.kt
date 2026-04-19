package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal

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
                Text("Back", color = MyShareSecondary) 
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Essentials & Bills", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                "What must leave your account every month for rent, utilities, and absolute essentials?", 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            PremiumTextField(
                value = fixedCostsText,
                onValueChange = { fixedCostsText = it.replace(',', '.') },
                label = "Monthly Fixed Costs",
                placeholder = "0.00",
                prefix = { Text("€ ") },
                isError = fixedCostsText.isNotEmpty() && fixedCosts == null
            )

            if (fixedCostsText.isNotEmpty() && fixedCosts == null) {
                Text(
                    text = "Please enter a valid number for costs.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Allocation Style", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val presets = listOf(
                    Triple(AllocationPreset.CONSERVATIVE, "Conservative", "Prioritize safety and stability."),
                    Triple(AllocationPreset.BALANCED, "Balanced", "A healthy mix of saving and spending."),
                    Triple(AllocationPreset.GROWTH, "Growth", "Focused on aggressive wealth building.")
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
                text = "Continue",
                onClick = {
                    onNext(fixedCosts ?: BigDecimal.ZERO, preset)
                },
                enabled = fixedCosts != null && fixedCosts >= BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
