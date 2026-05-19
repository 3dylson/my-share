package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.domain.model.PlanningFocus
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter
import pt.ms.myshare.presentation.ui.theme.*
import java.math.BigDecimal

@Composable
fun GoalPickerScreen(
    initialFocus: PlanningFocus,
    initialGoalName: String,
    initialGoalAmount: BigDecimal,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNext: (PlanningFocus, String, BigDecimal) -> Unit
) {
    var selectedFocus by remember { mutableStateOf(initialFocus) }
    val defaultEmergencyGoalName = stringResource(R.string.goal_default_emergency_fund)
    val defaultInvestingGoalName = stringResource(R.string.goal_default_investing_base)
    val defaultCashBufferGoalName = stringResource(R.string.goal_default_cash_buffer)
    val defaultSharedGoalName = stringResource(R.string.goal_default_shared_safety_net)
    val defaultGoalNames = remember(
        defaultEmergencyGoalName,
        defaultInvestingGoalName,
        defaultCashBufferGoalName,
        defaultSharedGoalName
    ) {
        setOf(
            defaultEmergencyGoalName,
            defaultInvestingGoalName,
            defaultCashBufferGoalName,
            defaultSharedGoalName
        )
    }
    var goalName by remember { mutableStateOf(initialGoalName.ifBlank { defaultEmergencyGoalName }) }
    val locale = userPreferences.locale
    var goalAmountText by remember(userPreferences.languageTag) { mutableStateOf(LocalizedAmountFormatter.formatEditableAmount(initialGoalAmount, locale)) }
    var isCustomGoalSelected by remember { mutableStateOf(false) }
    var validationRequested by remember { mutableStateOf(false) }

    val currencySymbol = remember(locale, userPreferences.currencyCode) {
        LocalizedAmountFormatter.currencySymbol(locale, userPreferences.currencyCode)
    }
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
        progressStep = 1,
        progressTotal = OnboardingViewModel.SETUP_STEP_TOTAL,
        onBack = onBack,
        onAction = ::continueIfValid
    ) {
        GoalFocusGrid(
            emergencySelected = selectedFocus == PlanningFocus.SAVE_WITHOUT_STRESS && !isCustomGoalSelected,
            debtSelected = selectedFocus == PlanningFocus.STOP_OVERSPENDING && !isCustomGoalSelected,
            investmentSelected = selectedFocus == PlanningFocus.INVEST_WITH_DISCIPLINE && !isCustomGoalSelected,
            purchaseSelected = selectedFocus == PlanningFocus.PLAN_TOGETHER && !isCustomGoalSelected,
            customSelected = isCustomGoalSelected,
            onEmergencyClick = { setDefaultsForFocus(PlanningFocus.SAVE_WITHOUT_STRESS) },
            onDebtClick = { setDefaultsForFocus(PlanningFocus.STOP_OVERSPENDING) },
            onInvestmentClick = { setDefaultsForFocus(PlanningFocus.INVEST_WITH_DISCIPLINE) },
            onPurchaseClick = { setDefaultsForFocus(PlanningFocus.PLAN_TOGETHER) },
            onCustomClick = {
                isCustomGoalSelected = true
                selectedFocus = PlanningFocus.PLAN_TOGETHER
                if (goalName in defaultGoalNames) goalName = ""
            }
        )

        Spacer(Modifier.height(16.dp))

        GoalPickerGuidanceCard()

        Spacer(Modifier.height(16.dp))

        GoalDetailsCard(
            goalName = goalName,
            onGoalNameChange = { goalName = it },
            goalNameError = goalNameError,
            goalAmountText = goalAmountText,
            onGoalAmountChange = {
                goalAmountText = LocalizedAmountFormatter.sanitizeAmountInput(it, locale)
            },
            goalAmountError = goalAmountError,
            currencySymbol = currencySymbol,
            amountPlaceholder = amountPlaceholder
        )
    }
}

@Composable
private fun GoalFocusGrid(
    emergencySelected: Boolean,
    debtSelected: Boolean,
    investmentSelected: Boolean,
    purchaseSelected: Boolean,
    customSelected: Boolean,
    onEmergencyClick: () -> Unit,
    onDebtClick: () -> Unit,
    onInvestmentClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onCustomClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoalFocusCard(
                title = stringResource(R.string.onboarding_goal_picker_emergency),
                icon = Icons.Default.Security,
                accentColor = MySharePrimary,
                isSelected = emergencySelected,
                onClick = onEmergencyClick,
                modifier = Modifier.weight(1f)
            )
            GoalFocusCard(
                title = stringResource(R.string.onboarding_goal_picker_debt),
                icon = Icons.Default.AccountBalance,
                accentColor = MyShareWarning,
                isSelected = debtSelected,
                onClick = onDebtClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoalFocusCard(
                title = stringResource(R.string.onboarding_goal_picker_investment),
                icon = Icons.Default.AutoGraph,
                accentColor = MySharePositive,
                isSelected = investmentSelected,
                onClick = onInvestmentClick,
                modifier = Modifier.weight(1f)
            )
            GoalFocusCard(
                title = stringResource(R.string.onboarding_goal_picker_purchase),
                icon = Icons.Default.Flag,
                accentColor = MySharePrimary,
                isSelected = purchaseSelected,
                onClick = onPurchaseClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GoalFocusCard(
                title = stringResource(R.string.onboarding_goal_picker_custom),
                icon = Icons.Default.Edit,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                isSelected = customSelected,
                onClick = onCustomClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun GoalPickerGuidanceCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(8.dp).size(19.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_goal_picker_guidance_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.onboarding_goal_picker_guidance_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun GoalFocusCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 74.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = accentColor.copy(alpha = if (isSelected) 0.18f else 0.11f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_selected),
                        tint = MySharePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun GoalDetailsCard(
    goalName: String,
    onGoalNameChange: (String) -> Unit,
    goalNameError: Boolean,
    goalAmountText: String,
    onGoalAmountChange: (String) -> Unit,
    goalAmountError: Boolean,
    currencySymbol: String,
    amountPlaceholder: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MySharePrimary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(8.dp).size(19.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_goal_picker_details_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.onboarding_goal_picker_details_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            PremiumTextField(
                value = goalName,
                onValueChange = onGoalNameChange,
                label = stringResource(R.string.onboarding_goal_picker_label_name),
                placeholder = stringResource(R.string.onboarding_goal_picker_placeholder_name),
                isError = goalNameError
            )
            if (goalNameError) {
                OnboardingValidationText(stringResource(R.string.onboarding_goal_picker_error_name))
            }
            PremiumTextField(
                value = goalAmountText,
                onValueChange = onGoalAmountChange,
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
}
