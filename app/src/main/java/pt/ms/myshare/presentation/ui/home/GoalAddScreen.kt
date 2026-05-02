package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
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
import pt.ms.myshare.presentation.ui.components.*
import pt.ms.myshare.presentation.ui.theme.MyShareBackground
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MyShareSecondary

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
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.goal_add_delete_confirm_title)) },
            text = { Text(stringResource(R.string.goal_add_delete_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.goal_add_delete_confirm_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.goal_add_delete_cancel_btn))
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
                        text = if (state.goalId != null) stringResource(R.string.goal_add_title_edit) else stringResource(R.string.goal_add_title_new), 
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
                    containerColor = MyShareBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumAppHeader(
                title = if (state.goalId != null) stringResource(R.string.goal_add_header_title_edit) else stringResource(R.string.goal_add_header_title_new),
                subtitle = if (state.goalId != null) stringResource(R.string.goal_add_header_subtitle_edit) else stringResource(R.string.goal_add_header_subtitle_new)
            )

            PremiumInfoCard(
                title = stringResource(R.string.goal_add_info_title),
                body = stringResource(R.string.goal_add_info_body),
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
                prefix = { Text(stringResource(R.string.currency_prefix), color = MyShareSecondary) }
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PremiumButton(
                text = if (state.isLoading) stringResource(R.string.goal_add_button_loading) else if (state.goalId != null) stringResource(R.string.goal_add_button_edit) else stringResource(R.string.goal_add_button_new),
                onClick = onSave,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
