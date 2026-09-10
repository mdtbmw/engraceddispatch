package com.esdispatch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

enum class IconActionVariant {
    GHOST,
    TONAL,
    FILLED_GOLD,
    BORDERED
}

/**
 * Standardized IconActionButton ensuring minimum 44dp hit area (Section 9.2).
 */
@Composable
fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IconActionVariant = IconActionVariant.TONAL,
    iconSize: Dp = 20.dp,
    hitSize: Dp = 44.dp,
    enabled: Boolean = true,
    badgeCount: Int? = null,
    hasDot: Boolean = false,
    customTint: Color? = null
) {
    val dark = isDarkTheme

    val (bg, defaultTint, border) = when (variant) {
        IconActionVariant.GHOST -> Triple(Color.Transparent, if (dark) Color.White else Obsidian, null)
        IconActionVariant.TONAL -> Triple(
            if (dark) Color(0xFF1E1E20) else GoldenWhiteSurface,
            if (dark) Color.White else Obsidian,
            BorderStroke(1.dp, if (dark) Color(0x33FFFFFF) else Slate)
        )
        IconActionVariant.FILLED_GOLD -> Triple(
            Gold,
            Obsidian, // Mandatory: NO white on gold
            null
        )
        IconActionVariant.BORDERED -> Triple(
            Color.Transparent,
            if (dark) Gold else Obsidian,
            BorderStroke(1.dp, if (dark) Gold.copy(alpha = 0.5f) else Slate)
        )
    }

    val finalTint = if (!enabled) TextGray.copy(alpha = 0.4f) else (customTint ?: defaultTint)

    Box(
        modifier = modifier
            .size(hitSize)
            .tactilePress(
                scaleDown = 0.92f,
                enabled = enabled,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((iconSize + 16.dp).coerceAtMost(hitSize))
                .clip(CircleShape)
                .background(if (enabled) bg else bg.copy(alpha = 0.5f))
                .then(
                    if (border != null && enabled) Modifier.border(border, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = finalTint,
                modifier = Modifier.size(iconSize)
            )

            // Optional Notification Dot
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Gold)
                )
            }

            // Optional Count Badge
            if (badgeCount != null && badgeCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    shape = CircleShape,
                    color = Gold
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        fontSize = 9.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Black,
                        color = Obsidian,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
