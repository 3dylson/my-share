package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.domain.model.AllocationPreset
import pt.ms.myshare.domain.model.PaySchedule
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun SalaryAndScheduleScreen(
    initialSalary: BigDecimal?,
    initialSchedule: PaySchedule?,
    initialPreset: AllocationPreset,
    onBack: () -> Unit,
    onPresetSelected: (AllocationPreset) -> Unit,
    onSeePlan: (BigDecimal, PaySchedule) -> Unit
) {
    var salaryText by remember { mutableStateOf(initialSalary?.toPlainString() ?: "") }

    var scheduleType by remember {
        mutableStateOf(
            when (initialSchedule) {
                is PaySchedule.BiWeekly -> ScheduleType.BIWEEKLY
                else -> ScheduleType.MONTHLY
            }
        )
    }

    var dayOfMonthText by remember {
        mutableStateOf(
            (initialSchedule as? PaySchedule.Monthly)?.dayOfMonth?.toString() ?: "1"
        )
    }
    var nextPaydayText by remember {
        mutableStateOf(
            (initialSchedule as? PaySchedule.BiWeekly)?.nextPayday?.toString()
                ?: LocalDate.now().plusDays(14).toString()
        )
    }

    var preset by remember { mutableStateOf(initialPreset) }

    fun parseSalary(): BigDecimal? = runCatching { BigDecimal(salaryText.trim()) }.getOrNull()

    fun buildSchedule(): PaySchedule? {
        return when (scheduleType) {
            ScheduleType.MONTHLY -> {
                val day = dayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: return null
                PaySchedule.Monthly(dayOfMonth = day)
            }
            ScheduleType.BIWEEKLY -> {
                val date = runCatching { LocalDate.parse(nextPaydayText.trim()) }.getOrNull() ?: return null
                PaySchedule.BiWeekly(nextPayday = date)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F8FA)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.weight(1f))
            }

            Text("How much do you take home?", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("We’ll calculate your payday amounts.", color = Color(0xFF546E7A))
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = salaryText,
                onValueChange = { salaryText = it.replace(',', '.') },
                label = { Text("Net salary (per payday)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Pay schedule", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceChip(
                    label = "Monthly",
                    selected = scheduleType == ScheduleType.MONTHLY,
                    onClick = { scheduleType = ScheduleType.MONTHLY }
                )
                ChoiceChip(
                    label = "Every 2 weeks",
                    selected = scheduleType == ScheduleType.BIWEEKLY,
                    onClick = { scheduleType = ScheduleType.BIWEEKLY }
                )
            }
            Spacer(Modifier.height(12.dp))
            when (scheduleType) {
                ScheduleType.MONTHLY -> {
                    OutlinedTextField(
                        value = dayOfMonthText,
                        onValueChange = { dayOfMonthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Day of month (1–31)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ScheduleType.BIWEEKLY -> {
                    OutlinedTextField(
                        value = nextPaydayText,
                        onValueChange = { nextPaydayText = it },
                        label = { Text("Next payday (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Style", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PresetCard(
                    title = "Conservative",
                    subtitle = "More savings",
                    selected = preset == AllocationPreset.CONSERVATIVE,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        preset = AllocationPreset.CONSERVATIVE
                        onPresetSelected(preset)
                    }
                )
                PresetCard(
                    title = "Balanced",
                    subtitle = "Steady",
                    selected = preset == AllocationPreset.BALANCED,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        preset = AllocationPreset.BALANCED
                        onPresetSelected(preset)
                    }
                )
                PresetCard(
                    title = "Growth",
                    subtitle = "More risk",
                    selected = preset == AllocationPreset.GROWTH,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        preset = AllocationPreset.GROWTH
                        onPresetSelected(preset)
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            val salary = parseSalary()
            val schedule = buildSchedule()
            Button(
                onClick = { onSeePlan(salary!!, schedule!!) },
                enabled = salary != null && salary > BigDecimal.ZERO && schedule != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("See my plan", fontSize = 18.sp)
            }
        }
    }
}

private enum class ScheduleType { MONTHLY, BIWEEKLY }

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, Color(0xFFE0E0E0))
    val bg = if (selected) Color(0xFFE3F2FD) else Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier
            .height(44.dp)
            .clickable { onClick() }
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, Color(0xFFE0E0E0))
    val bg = if (selected) Color(0xFFE3F2FD) else Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF607D8B))
        }
    }
}
