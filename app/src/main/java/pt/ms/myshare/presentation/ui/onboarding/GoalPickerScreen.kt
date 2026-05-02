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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

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

    val currencySymbol = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).currency?.symbol ?: ""
    }
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
                Text(stringResource(R.string.back), color = MyShareSecondary) 
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                stringResource(R.string.onboarding_goal_picker_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_goal_picker_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_emergency),
                    description = "A safety net for life's surprises.",
                    isSelected = selectedFocus == PlanningFocus.EMERGENCY_FUND,
                    onClick = { selectedFocus = PlanningFocus.EMERGENCY_FUND }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_debt),
                    description = "Breaking free from past obligations.",
                    isSelected = selectedFocus == PlanningFocus.DEBT_PAYOFF,
                    onClick = { selectedFocus = PlanningFocus.DEBT_PAYOFF }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_investment),
                    description = "Making your capital work for you.",
                    isSelected = selectedFocus == PlanningFocus.INVESTMENT,
                    onClick = { selectedFocus = PlanningFocus.INVESTMENT }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_purchase),
                    description = "Saving for a specific milestone or asset.",
                    isSelected = selectedFocus == PlanningFocus.MAJOR_PURCHASE,
                    onClick = { selectedFocus = PlanningFocus.MAJOR_PURCHASE }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_custom),
                    description = "Define your own unique destination.",
                    isSelected = selectedFocus == PlanningFocus.OTHER,
                    onClick = { selectedFocus = PlanningFocus.OTHER }
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                stringResource(R.string.onboarding_customize_goal), 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            PremiumTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = stringResource(R.string.onboarding_goal_picker_label_name),
                placeholder = stringResource(R.string.onboarding_goal_picker_placeholder_name)
            )
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = goalAmountText,
                onValueChange = { goalAmountText = it.replace(',', '.') },
                label = stringResource(R.string.onboarding_goal_picker_label_amount),
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(40.dp))
            
            PremiumButton(
                text = stringResource(R.string.continue_button),
                onClick = { onNext(selectedFocus, goalName.ifBlank { "Emergency fund" }, goalAmount ?: BigDecimal.ZERO) },
                enabled = goalAmount != null && goalAmount > BigDecimal.ZERO
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

