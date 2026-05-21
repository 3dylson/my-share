package pt.ms.myshare.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class MyShareDialogActionStyle {
    Primary,
    Destructive,
    Text
}

@Composable
fun MyShareAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Default.Info,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    actionStyle: MyShareDialogActionStyle = MyShareDialogActionStyle.Primary,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scrimColor = Color.Black.copy(alpha = if (isDark) 0.72f else 0.42f)
    val interactionSource = remember { MutableInteractionSource() }
    val contentScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onDismissRequest
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = maxHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = if (isDark) 0.dp else 12.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    if (message != null || content != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(contentScrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (message != null) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Start
                                )
                            }
                            content?.invoke(this)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    DialogActions(
                        confirmText = confirmText,
                        onConfirm = onConfirm,
                        dismissText = dismissText,
                        onDismiss = onDismiss ?: onDismissRequest,
                        actionStyle = actionStyle
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
    actionStyle: MyShareDialogActionStyle
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
        if (shouldStack) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfirmDialogAction(
                    text = confirmText,
                    onClick = onConfirm,
                    actionStyle = actionStyle,
                    modifier = Modifier.fillMaxWidth()
                )
                if (dismissText != null) {
                    DismissDialogAction(
                        text = dismissText,
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dismissText != null) {
                    DismissDialogAction(
                        text = dismissText,
                        onClick = onDismiss
                    )
                }
                ConfirmDialogAction(
                    text = confirmText,
                    onClick = onConfirm,
                    actionStyle = actionStyle
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialogAction(
    text: String,
    onClick: () -> Unit,
    actionStyle: MyShareDialogActionStyle,
    modifier: Modifier = Modifier
) {
    when (actionStyle) {
        MyShareDialogActionStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            DialogActionText(text = text)
        }
        MyShareDialogActionStyle.Destructive -> Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            DialogActionText(text = text)
        }
        MyShareDialogActionStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp)
        ) {
            DialogActionText(text = text)
        }
    }
}

@Composable
private fun DismissDialogAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        DialogActionText(text = text)
    }
}

@Composable
private fun DialogActionText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}
