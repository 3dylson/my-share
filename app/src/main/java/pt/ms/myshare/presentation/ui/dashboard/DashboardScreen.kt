package pt.ms.myshare.presentation.ui.dashboard

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.InvestAmount
import pt.ms.myshare.presentation.ui.theme.MyShareTheme
import java.math.BigDecimal
import java.text.NumberFormat

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(DashboardEvent.OnRefresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DashboardScreen(
        modifier = modifier,
        uiState = uiState,
        onChipSelected = { viewModel.onEvent(DashboardEvent.OnChipSelected(it)) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    uiState: DashboardState,
    onChipSelected: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState.error != null) {
            Text(text = uiState.error!!, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Column(modifier = Modifier.weight(1f)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.chipOptions) { chip ->
                        FilterChip(
                            selected = uiState.selectedChipId == chip.id,
                            onClick = { onChipSelected(chip.id) },
                            label = { Text(text = stringResource(id = chip.labelRes)) }
                        )
                    }
                }

                Text(
                    text = uiState.date,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.investments) { investment ->
                        InvestmentCard(investment = investment)
                    }
                }
            }
            AdvertView()
        }
    }
}

@Composable
fun InvestmentCard(investment: InvestAmount) {
    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocalConfiguration.current.locales[0]
    } else {
        @Suppress("DEPRECATION")
        LocalConfiguration.current.locale
    }
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = investment.category, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = currencyFormatter.format(investment.value.divide(BigDecimal(100))),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            investment.chipIcon?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun AdvertView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = context.getString(R.string.admob_banner_ad_unit_id)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MyShareTheme {
        DashboardScreen(
            uiState = DashboardState(
                investments = listOf(
                    InvestAmount(category = "Stocks", value = BigDecimal("123456"), chipIcon = R.drawable.ic_baseline_show_chart),
                    InvestAmount(category = "Crypto", value = BigDecimal("567890"), chipIcon = R.drawable.ic_baseline_currency_bitcoin),
                    InvestAmount(category = "Savings", value = BigDecimal("200000"), chipIcon = R.drawable.savings_48px)
                ),
                date = "October 26, 2023"
            ),
            onChipSelected = {}
        )
    }
}
