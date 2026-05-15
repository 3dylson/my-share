package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.presentation.ui.components.PremiumChoiceCard
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal
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
    val defaultEmergencyGoalName = stringResource(R.string.goal_default_emergency_fund)
    val defaultInvestingGoalName = stringResource(R.string.goal_default_investing_base)
    val defaultCashBufferGoalName = stringResource(R.string.goal_default_cash_buffer)
    val defaultSharedGoalName = stringResource(R.string.goal_default_shared_safety_net)
    var goalName by remember { mutableStateOf(initialGoalName.ifBlank { defaultEmergencyGoalName }) }
    val locale = Locale.getDefault()
    var goalAmountText by remember { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialGoalAmount, locale)) }
    var isCustomGoalSelected by remember { mutableStateOf(false) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale) { LocalizedAmountFormatter.currencySymbol(locale) }
    val amountPlaceholder = remember(locale) { LocalizedAmountFormatter.amountPlaceholder(locale) }
    val goalAmount = LocalizedAmountFormatter.parseAmount(goalAmountText, locale)
    val goalNameError = validationRequested && goalName.isBlank()
    val goalAmountError = validationRequested && (goalAmount == null || goalAmount <= BigDecimal.ZERO)
    val isGoalValid = goalName.isNotBlank() && goalAmount != null && goalAmount > BigDecimal.ZERO

    fun setDefaultsForFocus(focus: PlanningFocus) {
        isCustomGoalSelected = false
        selectedFocus = focus
        when (focus) {
            PlanningFocus.SAVE_WITHOUT_STRESS -> {
                goalName = defaultEmergencyGoalName
                goalAmountText = LocalizedAmountFormatter.formatEditableAmount(BigDecimal("3000"), locale)
            }
            PlanningFocus.INVEST_WITH_DISCIPLINE -> {
                goalName = defaultInvestingGoalName
                goalAmountText = LocalizedAmountFormatter.formatEditableAmount(BigDecimal("10000"), locale)
            }
            PlanningFocus.STOP_OVERSPENDING -> {
                goalName = defaultCashBufferGoalName
                goalAmountText = LocalizedAmountFormatter.formatEditableAmount(BigDecimal("2000"), locale)
            }
            PlanningFocus.PLAN_TOGETHER -> {
                goalName = defaultSharedGoalName
                goalAmountText = LocalizedAmountFormatter.formatEditableAmount(BigDecimal("5000"), locale)
            }
        }
    }

    fun continueIfValid() {
        validationRequested = true
        if (isGoalValid) {
            onNext(selectedFocus, goalName.trim(), goalAmount ?: BigDecimal.ZERO)
        }
    }

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_goal_picker_title),
        subtitle = stringResource(R.string.onboarding_goal_picker_subtitle),
        actionText = stringResource(R.string.continue_button),
        onBack = onBack,
        onAction = ::continueIfValid
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_emergency),
                    description = stringResource(R.string.onboarding_goal_picker_emergency_desc),
                    isSelected = selectedFocus == PlanningFocus.SAVE_WITHOUT_STRESS && !isCustomGoalSelected,
                    onClick = { setDefaultsForFocus(PlanningFocus.SAVE_WITHOUT_STRESS) }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_debt),
                    description = stringResource(R.string.onboarding_goal_picker_debt_desc),
                    isSelected = selectedFocus == PlanningFocus.STOP_OVERSPENDING && !isCustomGoalSelected,
                    onClick = { setDefaultsForFocus(PlanningFocus.STOP_OVERSPENDING) }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_investment),
                    description = stringResource(R.string.onboarding_goal_picker_investment_desc),
                    isSelected = selectedFocus == PlanningFocus.INVEST_WITH_DISCIPLINE && !isCustomGoalSelected,
                    onClick = { setDefaultsForFocus(PlanningFocus.INVEST_WITH_DISCIPLINE) }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_purchase),
                    description = stringResource(R.string.onboarding_goal_picker_purchase_desc),
                    isSelected = selectedFocus == PlanningFocus.PLAN_TOGETHER && !isCustomGoalSelected,
                    onClick = { setDefaultsForFocus(PlanningFocus.PLAN_TOGETHER) }
                )
                PremiumChoiceCard(
                    title = stringResource(R.string.onboarding_goal_picker_custom),
                    description = stringResource(R.string.onboarding_goal_picker_custom_desc),
                    isSelected = isCustomGoalSelected,
                    onClick = {
                        isCustomGoalSelected = true
                        selectedFocus = PlanningFocus.PLAN_TOGETHER
                        if (goalName == defaultEmergencyGoalName) goalName = ""
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                stringResource(R.string.onboarding_customize_goal), 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MyShareOnSurface
            )
            Spacer(Modifier.height(16.dp))
            
            PremiumTextField(
                value = goalName,
                onValueChange = {
                    goalName = it
                    isCustomGoalSelected = true
                },
                label = stringResource(R.string.onboarding_goal_picker_label_name),
                placeholder = stringResource(R.string.onboarding_goal_picker_placeholder_name),
                isError = goalNameError
            )
            if (goalNameError) {
                OnboardingValidationText(stringResource(R.string.onboarding_goal_picker_error_name))
            }
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = goalAmountText,
                onValueChange = {
                    goalAmountText = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
                    isCustomGoalSelected = true
                },
                label = stringResource(R.string.onboarding_goal_picker_label_amount),
                prefix = { if (currencySymbol.isNotEmpty()) Text("$currencySymbol ") },
                placeholder = amountPlaceholder,
                isError = goalAmountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            if (goalAmountError) {
                OnboardingValidationText(
                    if (goalAmountText.isBlank()) {
                        stringResource(R.string.onboarding_goal_picker_error_amount_required)
                    } else {
                        stringResource(R.string.onboarding_goal_picker_error_amount_positive)
                    }
                )
            }

    }
}
