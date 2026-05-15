package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaydayRuleType
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareBackground
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuleAddRoute(
    navController: NavController,
    viewModel: RuleAddViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            navController.popBackStack()
        }
    }

    RuleAddScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onPercentageToggle = viewModel::onPercentageToggle,
        onTypeChanged = viewModel::onTypeChanged,
        onSave = viewModel::saveRule,
        onDelete = viewModel::deleteRule,
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RuleAddScreen(
    state: RuleAddState,
    onNameChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onPercentageToggle: (Boolean) -> Unit,
    onTypeChanged: (PaydayRuleType) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditMode = state.requestedRuleId != null || state.ruleId != null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.rule_add_delete_confirm_title)) },
            text = { Text(stringResource(R.string.rule_add_delete_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.rule_add_delete_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.rule_add_delete_cancel_btn))
                }
            }
        )
    }

    Scaffold(
        containerColor = MyShareBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) stringResource(R.string.rule_add_title_edit) else stringResource(R.string.rule_add_title_new), 
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.rule_add_back))
                    }
                },
                actions = {
                    if (state.ruleId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.rule_add_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MyShareBackground
                )
            )
        },
        bottomBar = {
            if (!state.isMissingExistingRule) {
                Surface(color = MyShareBackground) {
                    PremiumButton(
                        text = if (state.isLoading) stringResource(R.string.rule_add_button_loading) else if (isEditMode) stringResource(R.string.rule_add_button_edit) else stringResource(R.string.rule_add_button_new),
                        onClick = onSave,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.isMissingExistingRule) {
            val resolvedError = state.error?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else it
            } ?: stringResource(R.string.rule_add_missing_body)
            MissingRuleContent(
                error = resolvedError,
                onBack = onBack,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            StrategyFormHeaderCard(
                title = if (isEditMode) stringResource(R.string.rule_add_header_title_edit) else stringResource(R.string.rule_add_header_title_new),
                body = stringResource(R.string.rule_add_info_body),
                icon = Icons.Default.Settings
            )

            PremiumTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = stringResource(R.string.rule_add_label_name),
                placeholder = stringResource(R.string.rule_add_hint_name)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.rule_add_label_type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MyShareSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.isPercentage,
                        onClick = { onPercentageToggle(true) },
                        label = { Text(stringResource(R.string.rule_add_type_percentage)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                            selectedLabelColor = MySharePrimary,
                            selectedLeadingIconColor = MySharePrimary
                        )
                    )
                    FilterChip(
                        selected = !state.isPercentage,
                        onClick = { onPercentageToggle(false) },
                        label = { Text(stringResource(R.string.rule_add_type_fixed)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                            selectedLabelColor = MySharePrimary,
                            selectedLeadingIconColor = MySharePrimary
                        )
                    )
                }
            }

            PremiumTextField(
                value = state.amount,
                onValueChange = onAmountChanged,
                label = if (state.isPercentage) stringResource(R.string.rule_add_label_rate) else stringResource(R.string.rule_add_label_amount),
                placeholder = if (state.isPercentage) stringResource(R.string.rule_add_hint_rate) else stringResource(R.string.rule_add_hint_amount),
                prefix = { 
                    Text(
                        text = if (state.isPercentage) stringResource(R.string.percentage_prefix) else stringResource(R.string.currency_prefix), 
                        color = MyShareSecondary
                    ) 
                }
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.rule_add_label_category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MyShareSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    PaydayRuleType.values().forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { onTypeChanged(type) },
                            label = { Text(stringResource(type.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = MySharePrimary
                            )
                        )
                    }
                }
            }

            if (state.error != null) {
                val errorText = remember(state.error) {
                    val resId = context.resources.getIdentifier(state.error, "string", context.packageName)
                    if (resId != 0) context.getString(resId) else state.error
                }
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private val PaydayRuleType.labelRes: Int
    get() = when (this) {
        PaydayRuleType.SAVINGS -> R.string.rule_type_savings
        PaydayRuleType.INVESTING -> R.string.rule_type_investing
        PaydayRuleType.CRYPTO -> R.string.rule_type_crypto
        PaydayRuleType.DEBT -> R.string.rule_type_debt
        PaydayRuleType.OTHER -> R.string.rule_type_other
    }

@Composable
private fun MissingRuleContent(
    error: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumInfoCard(
            title = stringResource(R.string.rule_add_missing_title),
            body = error,
            icon = Icons.Default.Warning
        )
        Spacer(modifier = Modifier.height(24.dp))
        PremiumButton(
            text = stringResource(R.string.rule_add_missing_button),
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
