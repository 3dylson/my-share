package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("What do you want to solve first?", style = MaterialTheme.typography.headlineMedium)
            Text("This personalizes the plan and the premium story without adding heavy setup.", color = MaterialTheme.colorScheme.onSurfaceVariant)

            FocusCard(
                title = "Save without stress",
                subtitle = "More calm, fewer money surprises.",
                selected = selectedFocus == PlanningFocus.SAVE_WITHOUT_STRESS,
                onClick = { setDefaultsForFocus(PlanningFocus.SAVE_WITHOUT_STRESS) }
            )
            FocusCard(
                title = "Invest with discipline",
                subtitle = "Keep contributions consistent every payday.",
                selected = selectedFocus == PlanningFocus.INVEST_WITH_DISCIPLINE,
                onClick = { setDefaultsForFocus(PlanningFocus.INVEST_WITH_DISCIPLINE) }
            )
            FocusCard(
                title = "Stop the money leak",
                subtitle = "Protect future money before flexible spending grows.",
                selected = selectedFocus == PlanningFocus.STOP_OVERSPENDING,
                onClick = { setDefaultsForFocus(PlanningFocus.STOP_OVERSPENDING) }
            )
            FocusCard(
                title = "Plan together",
                subtitle = "A clearer split for shared life and shared bills.",
                selected = selectedFocus == PlanningFocus.PLAN_TOGETHER,
                onClick = { setDefaultsForFocus(PlanningFocus.PLAN_TOGETHER) }
            )

            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal name") },
                singleLine = true
            )
            OutlinedTextField(
                value = goalAmountText,
                onValueChange = { goalAmountText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onNext(selectedFocus, goalName.ifBlank { "Emergency fund" }, goalAmount ?: BigDecimal.ZERO) },
                modifier = Modifier.fillMaxWidth(),
                enabled = goalAmount != null && goalAmount > BigDecimal.ZERO,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue")
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
