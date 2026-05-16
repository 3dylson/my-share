package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
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
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.formatting.LocalizedAmountFormatter

@Composable
fun GoalAddRoute(
    navController: NavController,
    viewModel: GoalAddViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            navController.popBackStack()
        }
    }

    GoalAddScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onSave = viewModel::saveGoal,
        onDelete = viewModel::deleteGoal,
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalAddScreen(
    state: GoalAddState,
    onNameChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditMode = state.requestedGoalId != null || state.goalId != null

    if (showDeleteDialog) {
        MyShareAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Warning,
            iconTint = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.goal_add_delete_confirm_title),
            message = stringResource(R.string.goal_add_delete_confirm_msg),
            confirmText = stringResource(R.string.goal_add_delete_confirm_btn),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            dismissText = stringResource(R.string.goal_add_delete_cancel_btn),
            onDismiss = { showDeleteDialog = false },
            actionStyle = MyShareDialogActionStyle.Destructive
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) stringResource(R.string.goal_add_title_edit) else stringResource(R.string.goal_add_title_new), 
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.goal_add_back))
                    }
                },
                actions = {
                    if (state.goalId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.goal_add_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!state.isMissingExistingGoal) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PremiumButton(
                        text = if (state.isLoading) stringResource(R.string.goal_add_button_loading) else if (isEditMode) stringResource(R.string.goal_add_button_edit) else stringResource(R.string.goal_add_button_new),
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
        if (state.isMissingExistingGoal) {
            val resolvedError = state.error?.let {
                val resId = context.resources.getIdentifier(it, "string", context.packageName)
                if (resId != 0) context.getString(resId) else it
            } ?: stringResource(R.string.goal_add_missing_body)
            MissingGoalContent(
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
                title = if (isEditMode) stringResource(R.string.goal_add_header_title_edit) else stringResource(R.string.goal_add_header_title_new),
                body = if (isEditMode) stringResource(R.string.goal_add_info_body_edit) else stringResource(R.string.goal_add_info_body_new),
                icon = Icons.Default.Flag
            )

            PremiumTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = stringResource(R.string.goal_add_label_name),
                placeholder = stringResource(R.string.goal_add_hint_name)
            )

            PremiumTextField(
                value = state.amount,
                onValueChange = onAmountChanged,
                label = stringResource(R.string.goal_add_label_amount),
                placeholder = stringResource(R.string.goal_add_hint_amount),
                prefix = {
                    Text(
                        LocalizedAmountFormatter.currencySymbol(
                            state.userPreferences.locale,
                            state.userPreferences.currencyCode
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

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

@Composable
private fun MissingGoalContent(
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
            title = stringResource(R.string.goal_add_missing_title),
            body = error,
            icon = Icons.Default.Warning
        )
        Spacer(modifier = Modifier.height(24.dp))
        PremiumButton(
            text = stringResource(R.string.goal_add_missing_button),
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
