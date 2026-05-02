package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumMetricCard
import pt.ms.myshare.presentation.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun TrajectoryScreen(
    preview: PlanPreview?,
    goalName: String,
    onNext: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val scrollState = rememberScrollState()
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(40.dp))
            
            Text(
                stringResource(R.string.onboarding_trajectory_title), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MyShareOnSurface
            )
            
            Text(
                stringResource(R.string.onboarding_trajectory_subtitle), 
                color = MyShareSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(32.dp))
            
            preview?.let {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumMetricCard(
                        label = stringResource(R.string.onboarding_trajectory_label_payday),
                        value = it.summary,
                        icon = Icons.Default.Savings,
                        subtitle = stringResource(R.string.onboarding_trajectory_desc_payday)
                    )
                    
                    it.goalTargetDate?.let { date ->
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, date.year)
                            set(Calendar.MONTH, date.monthValue - 1)
                        }
                        val dateString = android.text.format.DateFormat.format("MMMM yyyy", calendar).toString()
                        PremiumMetricCard(
                            label = stringResource(R.string.onboarding_trajectory_label_goal),
                            value = dateString,
                            icon = Icons.Default.Event,
                            subtitle = stringResource(R.string.onboarding_trajectory_desc_goal, goalName)
                        )
                    }
                }
                
                Spacer(Modifier.height(40.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.onboarding_trajectory_intensity_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MyShareSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        currencyFormat.format(it.savingsPerPayday),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MySharePrimary
                    )
                    Text(
                        stringResource(R.string.onboarding_trajectory_intensity_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MyShareOnSurface,
                        lineHeight = 24.sp
                    )
                }
            } ?: Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }

            Spacer(Modifier.weight(1f))

            Text(
                stringResource(R.string.onboarding_trajectory_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = MyShareSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            PremiumButton(
                text = stringResource(R.string.onboarding_trajectory_button),
                onClick = onNext
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

