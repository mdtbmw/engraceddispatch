package com.esdispatch.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

enum class NotificationSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * Standardized NotificationBanner for in-app status updates (Section 14).
 */
@Composable
fun NotificationBanner(
    visible: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    severity: NotificationSeverity = NotificationSeverity.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val dark = isDarkTheme

    val (bg, textClr, icon) = when (severity) {
        NotificationSeverity.INFO -> Triple(
            if (dark) Color(0xFF1C1C20) else GoldenWhiteSurface,
            if (dark) Color.White else Obsidian,
            Icons.Filled.Info
        )
        NotificationSeverity.SUCCESS -> Triple(
            if (dark) Color(0xFF0D2818) else Color(0xFFD1FAE5),
            if (dark) Color(0xFF34D399) else Color(0xFF065F46),
            Icons.Filled.CheckCircle
        )
        NotificationSeverity.WARNING -> Triple(
            if (dark) Color(0xFF2E1C06) else Color(0xFFFEF3C7),
            if (dark) Color(0xFFFBBF24) else Color(0xFF92400E),
            Icons.Filled.Warning
        )
        NotificationSeverity.ERROR -> Triple(
            if (dark) Color(0xFF2D1215) else Color(0xFFFEE2E2),
            if (dark) Color(0xFFF87171) else Color(0xFF991B1B),
            Icons.Filled.Warning
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = BorderStroke(1.dp, if (dark) Color(0x33FFFFFF) else Slate),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (severity == NotificationSeverity.INFO) Gold else textClr,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 1.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = textClr
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        color = textClr.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )

                    if (!actionLabel.isNullOrBlank() && onAction != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onAction,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = actionLabel,
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss notification",
                        tint = textClr.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
