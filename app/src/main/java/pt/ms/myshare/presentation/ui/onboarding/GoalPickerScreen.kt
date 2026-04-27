package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
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
    val scrollState = rememberScrollState()

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
                "Pick your priority", 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                "We'll tailor your experience based on your current financial focus.", 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlanningFocus.values().forEach { focus ->
                    val (title, subtitle, icon) = when (focus) {
                        PlanningFocus.SAVE_WITHOUT_STRESS -> Triple("Save without stress", "Build a safety net without overthinking.", Icons.Default.Savings)
                        PlanningFocus.INVEST_WITH_DISCIPLINE -> Triple("Invest with discipline", "Market consistency starts with a plan.", Icons.Default.TrendingUp)
                        PlanningFocus.STOP_OVERSPENDING -> Triple("Stop overspending", "Identify and plug the money leaks.", Icons.Default.Warning)
                        PlanningFocus.PLAN_TOGETHER -> Triple("Plan together", "Coordinate finances with your partner.", Icons.Default.People)
                    }
                    PremiumChoiceCard(
                        title = title,
                        description = subtitle,
                        isSelected = selectedFocus == focus,
                        icon = icon,
                        onClick = { setDefaultsForFocus(focus) }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "Customize Goal", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            PremiumTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = "What are you saving for?",
                placeholder = "e.g. Travel, House, Wedding"
            )
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = goalAmountText,
                onValueChange = { goalAmountText = it.replace(',', '.') },
                label = "Target Amount",
                prefix = { Text("€ ") },
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = "Continue",
                onClick = { onNext(selectedFocus, goalName.ifBlank { "Emergency fund" }, goalAmount ?: BigDecimal.ZERO) },
                enabled = goalAmount != null && goalAmount > BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

