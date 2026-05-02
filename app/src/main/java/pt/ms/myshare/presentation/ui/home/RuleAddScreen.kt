package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                        text = if (state.ruleId != null) stringResource(R.string.rule_add_title_edit) else stringResource(R.string.rule_add_title_new), 
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            PremiumAppHeader(
                title = if (state.ruleId != null) stringResource(R.string.rule_add_header_title_edit) else stringResource(R.string.rule_add_header_title_new),
                subtitle = stringResource(R.string.rule_add_header_subtitle_new)
            )

            PremiumInfoCard(
                title = stringResource(R.string.rule_add_info_title),
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
                            label = { Text(type.name.lowercase().replaceFirstChar { it.titlecase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MySharePrimary.copy(alpha = 0.1f),
                                selectedLabelColor = MySharePrimary
                            )
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PremiumButton(
                text = if (state.isLoading) stringResource(R.string.rule_add_button_loading) else if (state.ruleId != null) stringResource(R.string.rule_add_button_edit) else stringResource(R.string.rule_add_button_new),
                onClick = onSave,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
