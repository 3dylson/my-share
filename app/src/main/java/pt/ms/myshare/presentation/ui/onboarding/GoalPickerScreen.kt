package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import java.math.BigDecimal

@Composable
fun GoalPickerScreen(
    initialFocus: PlanningFocus,
    initialGoalName: String,
    initialGoalAmount: BigDecimal,
    onBack: () -> Unit,
    onNext: (PlanningFocus, String, BigDecimal) -> Unit
) {
    var selectedFocus by remember { mutableStateOf(initialFocus) }
    var goalName by remember { mutableStateOf(initialGoalName) }
    var goalAmountText by remember { mutableStateOf(initialGoalAmount.toPlainString()) }

    val goalAmount = goalAmountText.toBigDecimalOrNull()

    fun setDefaultsForFocus(focus: PlanningFocus) {
        selectedFocus = focus
        when (focus) {
            PlanningFocus.SAVE_WITHOUT_STRESS -> {
                goalName = "Emergency fund"
                goalAmountText = "3000"
            }
            PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                goalName = "Investing base"
                goalAmountText = "10000"
            }
            PlanningFocus.STOP_OVERSPENDING -> {
                goalName = "Cash buffer"
                goalAmountText = "2000"
            }
            PlanningFocus.PLAN_TOGETHER -> {
                goalName = "Shared safety net"
                goalAmountText = "5000"
            }
        }
    }

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
                "Pick your priority", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "We'll tailor your experience based on your current financial focus.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PlanningFocus.values().forEach { focus ->
                    val (title, subtitle) = when (focus) {
                        PlanningFocus.SAVE_WITHOUT_STRESS -> "Save without stress" to "Build a safety net without overthinking."
                        PlanningFocus.INVEST_WITH_DISCIPLINE -> "Invest with discipline" to "Market consistency starts with a plan."
                        PlanningFocus.STOP_OVERSPENDING -> "Stop overspending" to "Identify and plug the money leaks."
                        PlanningFocus.PLAN_TOGETHER -> "Plan together" to "Coordinate finances with your partner."
                    }
                    PremiumChoiceCard(
                        text = title,
                        supportingText = subtitle,
                        selected = selectedFocus == focus,
                        onClick = { setDefaultsForFocus(focus) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Your specific goal", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("What are you saving for?") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = goalAmountText,
                        onValueChange = { goalAmountText = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Target Amount") },
                        prefix = { Text("€ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = { onNext(selectedFocus, goalName.ifBlank { "Emergency fund" }, goalAmount ?: BigDecimal.ZERO) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = goalAmount != null && goalAmount > BigDecimal.ZERO,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
