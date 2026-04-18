package pt.ms.myshare.presentation.ui.edit_profile

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumSectionHeader
import pt.ms.myshare.presentation.ui.components.PremiumTextField
import pt.ms.myshare.presentation.ui.theme.*

@Composable
fun EditProfileRoute(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EditProfileScreen(
        modifier = modifier,
        uiState = uiState,
        onNetSalaryChange = viewModel::onNetSalaryChange,
        onNetSalaryPercentageChange = viewModel::onNetSalaryPercentageChange,
        onStockPercentageChange = viewModel::onStockPercentageChange,
        onCryptoPercentageChange = viewModel::onCryptoPercentageChange,
        onSavingsPercentageChange = viewModel::onSavingsPercentageChange,
        onSave = viewModel::onSave,
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    uiState: EditProfileState,
    onNetSalaryChange: (String) -> Unit,
    onNetSalaryPercentageChange: (String) -> Unit,
    onStockPercentageChange: (String) -> Unit,
    onCryptoPercentageChange: (String) -> Unit,
    onSavingsPercentageChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(id = R.string.snackbar_saved_text)
    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocalConfiguration.current.locales[0]
    } else {
        @Suppress("DEPRECATION")
        LocalConfiguration.current.locale
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(savedMessage)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.edit_profile_label),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MySharePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                
                PremiumSectionHeader(title = "Salary Details")
                
                PremiumTextField(
                    value = uiState.netSalary,
                    onValueChange = onNetSalaryChange,
                    label = stringResource(id = R.string.your_net_salary_label, ""),
                    placeholder = "0.00"
                    // visualTransformation = CurrencyVisualTransformation(locale), // Keep transformation if possible
                )

                PremiumSectionHeader(title = "Allocations")
                
                PremiumTextField(
                    value = uiState.netSalaryPercentage,
                    onValueChange = onNetSalaryPercentageChange,
                    label = stringResource(id = R.string.investments_savings_percentage_label),
                    placeholder = "0%",
                    isError = uiState.netSalaryPercentageError != null
                )
                uiState.netSalaryPercentageError?.let { 
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
                }

                PremiumTextField(
                    value = uiState.stockPercentage,
                    onValueChange = onStockPercentageChange,
                    label = "Stocks Allocation",
                    placeholder = "0%",
                    isError = uiState.stockPercentageError != null
                )
                uiState.stockPercentageError?.let { 
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
                }

                PremiumTextField(
                    value = uiState.cryptoPercentage,
                    onValueChange = onCryptoPercentageChange,
                    label = "Crypto Allocation",
                    placeholder = "0%",
                    isError = uiState.cryptoPercentageError != null
                )
                uiState.cryptoPercentageError?.let { 
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
                }

                PremiumTextField(
                    value = uiState.savingsPercentage,
                    onValueChange = onSavingsPercentageChange,
                    label = "Savings Allocation",
                    placeholder = "0%",
                    isError = uiState.savingsPercentageError != null
                )
                uiState.savingsPercentageError?.let { 
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
                }

                Spacer(modifier = Modifier.height(32.dp))

                PremiumButton(
                    text = stringResource(id = R.string.btn_ep_confirm_text),
                    onClick = onSave,
                    isLoading = uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MySharePrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MyShareTheme {
        EditProfileScreen(
            uiState = EditProfileState(isLoading = false),
            onNetSalaryChange = {},
            onNetSalaryPercentageChange = {},
            onStockPercentageChange = {},
            onCryptoPercentageChange = {},
            onSavingsPercentageChange = {},
            onSave = {},
            onBack = {}
        )
    }
}
