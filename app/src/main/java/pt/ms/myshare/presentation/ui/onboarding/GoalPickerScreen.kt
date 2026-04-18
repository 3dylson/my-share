package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PlanningFocus
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

    val goalAmount = goalAmountText.toBigDecimalOrNull()

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
                "Select your focus", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "We'll tailor your experience based on your current financial priority.", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FocusCard(
                    title = "Save without stress",
                    subtitle = "Build a safety net without overthinking.",
                    selected = selectedFocus == PlanningFocus.SAVE_WITHOUT_STRESS,
                    onClick = { setDefaultsForFocus(PlanningFocus.SAVE_WITHOUT_STRESS) }
                )
                FocusCard(
                    title = "Invest with discipline",
                    subtitle = "Market consistency starts with a plan.",
                    selected = selectedFocus == PlanningFocus.INVEST_WITH_DISCIPLINE,
                    onClick = { setDefaultsForFocus(PlanningFocus.INVEST_WITH_DISCIPLINE) }
                )
                FocusCard(
                    title = "Stop overspending",
                    subtitle = "Identify and plug the money leaks.",
                    selected = selectedFocus == PlanningFocus.STOP_OVERSPENDING,
                    onClick = { setDefaultsForFocus(PlanningFocus.STOP_OVERSPENDING) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Goal Section in a Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Target Goal", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("What are you saving for?") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    OutlinedTextField(
                        value = goalAmountText,
                        onValueChange = { goalAmountText = it.replace(',', '.') },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Target Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
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

@Composable
private fun FocusCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
