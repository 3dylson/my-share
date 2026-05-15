package pt.ms.myshare.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import pt.ms.myshare.presentation.ui.theme.*
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.res.stringResource
import pt.ms.myshare.R
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Column(content = content)
    }
}

@Composable
fun PremiumChoiceCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badge: String? = null
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MySharePrimaryContainer else MaterialTheme.colorScheme.surface
    )
    val borderColor by animateColorAsState(
        if (isSelected) MySharePrimary else MyShareOutline.copy(alpha = 0.5f)
    )
    val elevation by animateDpAsState(if (isSelected) 4.dp else 1.dp)

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
            shadowElevation = elevation
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MySharePrimary else MyShareSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MyShareOnPrimary else MyShareOnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MyShareOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareOnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_selected),
                        tint = MySharePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (badge != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (-8).dp),
                color = MySharePrimary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumMetricCard(
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MySharePrimary,
    indicatorColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.2f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (indicatorColor != null) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(indicatorColor)
                )
            }
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MyShareSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareOnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumGoalCard(
    goalName: String,
    targetAmountLabel: String,
    progress: Float,
    progressLabel: String,
    targetDateLabel: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MySharePrimary,
    onClick: (() -> Unit)? = null
) {
    val actualIcon = icon ?: Icons.Default.Flag
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = actualIcon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goalName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MyShareOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = targetAmountLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MyShareSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MyShareSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                PremiumProgressBar(
                    progress = progress,
                    color = color
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MyShareOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MyShareSecondary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MyShareSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = targetDateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MyShareSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    icon: ImageVector? = null,
    containerColor: Color = MySharePrimary,
    contentColor: Color = MyShareOnPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        contentPadding = contentPadding
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    PremiumButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        icon = icon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    placeholder: String? = null,
    description: String? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MySharePrimary,
                unfocusedBorderColor = MyShareOutline,
                focusedLabelColor = MySharePrimary,
                cursorColor = MySharePrimary
            ),
            prefix = prefix,
            isError = isError,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MyShareSecondary,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PremiumPaywallCard(
    title: String,
    price: String,
    period: String,
    description: String? = null,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) MySharePrimary else MyShareOutline.copy(alpha = 0.5f)
    )
    val backgroundColor by animateColorAsState(
        if (isSelected) MySharePrimaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MyShareOnSurface
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MyShareSecondary
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.content_description_selected),
                        tint = MySharePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MyShareOnSurface
                )
                if (!price.contains(period, ignoreCase = true)) {
                    Text(
                        text = stringResource(R.string.price_period_suffix, period),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MyShareSecondary,
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    )
                }
            }
        }

        if (badge != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = MySharePrimary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PremiumSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MyShareOnSurface
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MySharePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PremiumInfoCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = MySharePrimaryContainer.copy(alpha = 0.4f)
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MyShareOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MyShareSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PremiumPlanSummary(
    headline: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MySharePrimary.copy(alpha = 0.5f),
                spotColor = MySharePrimary
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MySharePrimary, MySharePrimary.copy(alpha = 0.85f))
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PremiumAppHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MyShareOnSurface,
            letterSpacing = (-1.5).sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MyShareSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MySharePrimary, MySharePrimary.copy(alpha = 0.5f))
                    )
                )
        )
    }
}

@Composable
fun PremiumAdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = stringResource(R.string.admob_banner_ad_unit_id)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
        
        // Overlay for debugging/identifying ad space if ads don't load in emulator
        Text(
            text = stringResource(R.string.advertisement_label),
            style = MaterialTheme.typography.labelSmall,
            color = MyShareSecondary.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
        )
    }
}

@Composable
fun PremiumBenefitCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.AutoAwesome,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MySharePrimary.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MySharePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MyShareOnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MyShareSecondary
                )
            }
        }
    }
}

@Composable
fun PremiumRuleCard(
    ruleName: String,
    amountLabel: String,
    typeLabel: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    PremiumMetricCard(
        label = ruleName,
        value = amountLabel,
        subtitle = typeLabel,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun PremiumSparkline(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MySharePrimary,
    fillColor: Color = MySharePrimary.copy(alpha = 0.1f)
) {
    if (points.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val xSpacing = width / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        // Starting point
        val startX = 0f
        val startY = height - (points[0] * height)
        path.moveTo(startX, startY)
        fillPath.moveTo(startX, height)
        fillPath.lineTo(startX, startY)

        for (i in 1 until points.size) {
            val x1 = (i - 1) * xSpacing
            val y1 = height - (points[i - 1] * height)
            val x2 = i * xSpacing
            val y2 = height - (points[i] * height)

            val cp1x = x1 + (x2 - x1) / 2f
            val cp2x = x2 - (x2 - x1) / 2f

            path.cubicTo(cp1x, y1, cp2x, y2, x2, y2)
            fillPath.cubicTo(cp1x, y1, cp2x, y2, x2, y2)
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(lineColor, lineColor.copy(alpha = 0.6f))
            ),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw the terminal point (latest performance)
        val lastX = width
        val lastY = height - (points.last() * height)
        
        // Terminal pulse glow
        drawCircle(
            color = lineColor.copy(alpha = 0.2f),
            radius = 10.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
        
        drawCircle(
            color = lineColor,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY),
            style = Stroke(width = 2.dp.toPx())
        )
        
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
    }
}

/**
 * A striking card for primary dashboard metrics. 
 * Uses a subtle gradient background and emphasized typography.
 */
@Composable
fun HeroMetricCard(
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MySharePrimary,
    contentColor: Color = Color.White
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = containerColor,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.9f),
                            containerColor.copy(alpha = 0.8f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = contentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(contentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllocationPreviewMetric(
    fixedLabel: String,
    fixedValue: String,
    flexibleLabel: String,
    flexibleValue: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = fixedLabel.uppercase(), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fixedValue, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MyShareOnSurface,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MyShareOutline.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
            
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = flexibleLabel.uppercase(), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = flexibleValue, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black, 
                    color = MyShareOnSurface,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

/**
 * An official-compliant Google Sign-In button.
 * Following Branding Guidelines: White Background, Gray border, Roboto-like font.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isLoading: Boolean = false
) {
    val buttonText = text ?: stringResource(R.string.google_sign_in_button)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(4.dp), 
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF4285F4),
                    strokeWidth = 2.dp
                )
            } else {
                // Drawing a minimalist "G" logo using quadrants
                Box(
                    modifier = Modifier
                        .size(18.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = 3.5.dp.toPx()
                        val radius = size.width / 2
                        
                        // Red Quadrant (Top)
                        drawArc(
                            color = Color(0xFFEA4335),
                            startAngle = 180f,
                            sweepAngle = 135f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        // Yellow Quadrant (Left)
                        drawArc(
                            color = Color(0xFFFBBC05),
                            startAngle = 135f,
                            sweepAngle = 45f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        // Green Quadrant (Bottom)
                        drawArc(
                            color = Color(0xFF34A853),
                            startAngle = 45f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        // Blue Quadrant (Right + Bar)
                        drawArc(
                            color = Color(0xFF4285F4),
                            startAngle = 315f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                        
                        // The "G" bar
                        drawLine(
                            color = Color(0xFF4285F4),
                            start = androidx.compose.ui.geometry.Offset(radius, radius),
                            end = androidx.compose.ui.geometry.Offset(size.width, radius),
                            strokeWidth = strokeWidthPx,
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                Spacer(Modifier.width(24.dp))
                
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.Black.copy(alpha = 0.54f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PremiumProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MySharePrimary,
    trackColor: Color = MySharePrimary.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier.fillMaxWidth().height(12.dp)) {
        val height = size.height
        val width = size.width
        
        // Track
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
        )
        
        // Progress
        if (progress > 0) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color, color.copy(alpha = 0.6f))
                ),
                size = size.copy(width = width * progress.coerceIn(0f, 1f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2, height / 2)
            )
        }
    }
}

@Composable
fun PremiumSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MyShareSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 24.dp),
                letterSpacing = 1.sp
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.1f)),
            shadowElevation = 2.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun PremiumSettingsRow(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = MyShareOnSurface,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MyShareOnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            if (trailingContent != null) {
                this.trailingContent()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MyShareSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 72.dp)
                    .height(1.dp)
                    .background(MyShareOutline.copy(alpha = 0.1f))
            )
        }
    }
}

@Composable
fun PremiumProfileHeader(
    email: String,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
    accountLabel: String = stringResource(R.string.home_more_account_profile_label),
    membershipLabel: String = stringResource(R.string.home_more_account_basic_member),
    premiumMembershipLabel: String = stringResource(R.string.home_more_account_premium_member),
    onAccountClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAccountClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MyShareOutline.copy(alpha = 0.1f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MySharePrimary, MyShareSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = email.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accountLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MyShareSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MyShareOnSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isPremium) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFDAA520),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = premiumMembershipLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDAA520),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Text(
                        text = membershipLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MyShareOnSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MyShareSecondary.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun PremiumSliderCard(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    formatValue: (Float) -> String = { "%.2f".format(it) }
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    PremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onValueChange((value - 10f).coerceAtLeast(valueRange.start)) 
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove, 
                        contentDescription = stringResource(R.string.content_description_decrease),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                androidx.compose.material3.Slider(
                    value = value,
                    onValueChange = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onValueChange(it) 
                    },
                    valueRange = valueRange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onValueChange((value + 10f).coerceAtMost(valueRange.endInclusive)) 
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = stringResource(R.string.content_description_increase),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
