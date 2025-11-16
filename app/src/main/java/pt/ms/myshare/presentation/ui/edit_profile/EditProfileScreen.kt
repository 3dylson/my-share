package pt.ms.myshare.presentation.ui.edit_profile

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pt.ms.myshare.R
import pt.ms.myshare.presentation.ui.theme.MyShareTheme

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
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_profile_label)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null // stringResource(id = R.string.cd_back_button)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.netSalary,
                onValueChange = onNetSalaryChange,
                label = { Text(stringResource(id = R.string.your_net_salary_label, "")) },
                visualTransformation = CurrencyVisualTransformation(locale),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.netSalaryPercentage,
                onValueChange = onNetSalaryPercentageChange,
                label = { Text(stringResource(id = R.string.investments_savings_percentage_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            val stockLabel = stringResource(id = R.string.percentage_stocks_label)
            val stockAnnotatedString = buildAnnotatedString {
                val parts = stockLabel.split("<b>", "</b>")
                append(parts[0])
                if (parts.size > 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parts[1])
                    }
                    if (parts.size > 2) {
                        append(parts[2])
                    }
                }
            }
            OutlinedTextField(
                value = uiState.stockPercentage,
                onValueChange = onStockPercentageChange,
                label = { Text(stockAnnotatedString) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            val cryptoLabel = stringResource(id = R.string.percentage_crypto_label)
            val cryptoAnnotatedString = buildAnnotatedString {
                val parts = cryptoLabel.split("<b>", "</b>")
                append(parts[0])
                if (parts.size > 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parts[1])
                    }
                    if (parts.size > 2) {
                        append(parts[2])
                    }
                }
            }
            OutlinedTextField(
                value = uiState.cryptoPercentage,
                onValueChange = onCryptoPercentageChange,
                label = { Text(cryptoAnnotatedString) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            val savingsLabel = stringResource(id = R.string.percentage_savings_label)
            val savingsAnnotatedString = buildAnnotatedString {
                val parts = savingsLabel.split("<b>", "</b>")
                append(parts[0])
                if (parts.size > 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parts[1])
                    }
                    if (parts.size > 2) {
                        append(parts[2])
                    }
                }
            }
            OutlinedTextField(
                value = uiState.savingsPercentage,
                onValueChange = onSavingsPercentageChange,
                label = { Text(savingsAnnotatedString) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = onSave,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = stringResource(id = R.string.btn_ep_confirm_text))
                }
                if (uiState.isLoading) {
                    CircularProgressIndicator()
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
            uiState = EditProfileState(),
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
