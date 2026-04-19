package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MyShareBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Goal", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                title = "Vision Expansion",
                subtitle = "Define your next financial milestone."
            )

            PremiumInfoCard(
                title = "Multiple Goals",
                body = "You can now track multiple goals simultaneously. This helps you split your savings effectively.",
                icon = Icons.Default.Flag
            )

            PremiumTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = "Goal Name",
                placeholder = "e.g. New Home Deposit"
            )

            PremiumTextField(
                value = state.amount,
                onValueChange = onAmountChanged,
                label = "Target Amount",
                placeholder = "e.g. 50000",
                prefix = { Text("$ ", color = MyShareSecondary) }
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
                text = if (state.isLoading) "Saving..." else "Save Goal",
                onClick = onSave,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
