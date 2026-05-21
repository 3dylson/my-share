package pt.ms.myshare.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.PaydayCountdownAction
import pt.ms.myshare.presentation.ui.theme.MySharePrimary
import pt.ms.myshare.presentation.ui.theme.MyShareWarning

@Composable
fun PaydayCountdownCueCard(
    cue: PaydayCountdownCueState,
    isPremium: Boolean,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReviewAction = cue.action == PaydayCountdownAction.REVIEW_PAYDAY
    val accentColor = if (isReviewAction) MyShareWarning else MySharePrimary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isReviewAction) { onReviewClick() },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = cue.action.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(9.dp).size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(cue.titleRes, cue.daysUntilPayday),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(cue.bodyRes(isPremium)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            if (isReviewAction) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private val PaydayCountdownAction.icon: ImageVector
    get() = when (this) {
        PaydayCountdownAction.FINISH_SETUP -> Icons.Default.Event
        PaydayCountdownAction.KEEP_GUIDE -> Icons.Default.EventAvailable
        PaydayCountdownAction.REVIEW_PAYDAY -> Icons.Default.RateReview
    }

private val PaydayCountdownCueState.titleRes: Int
    get() = when (daysUntilPayday) {
        0L -> R.string.home_plan_payday_cue_title_today
        1L -> R.string.home_plan_payday_cue_title_tomorrow
        else -> R.string.home_plan_payday_cue_title_days
    }

private fun PaydayCountdownCueState.bodyRes(isPremium: Boolean): Int {
    return when (action) {
        PaydayCountdownAction.FINISH_SETUP -> R.string.home_plan_payday_cue_body_finish_setup
        PaydayCountdownAction.KEEP_GUIDE -> R.string.home_plan_payday_cue_body_keep_guide
        PaydayCountdownAction.REVIEW_PAYDAY -> if (isPremium) {
            R.string.home_plan_payday_cue_body_review_premium
        } else {
            R.string.home_plan_payday_cue_body_review
        }
    }
}
