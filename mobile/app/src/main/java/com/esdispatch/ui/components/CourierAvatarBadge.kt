package com.esdispatch.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian

@Composable
fun CourierAvatarBadge(
    avatarUrl: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderWidth: Dp = 1.5.dp,
    borderColor: Color = Gold
) {
    val initials = remember(name) {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "ES"
        }
    }

    val isValidUrl = remember(avatarUrl) {
        avatarUrl.isNotBlank() &&
            (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) &&
            !avatarUrl.contains("unsplash.com")
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .background(Obsidian),
        contentAlignment = Alignment.Center
    ) {
        if (isValidUrl) {
            Image(
                painter = rememberAsyncImagePainter(avatarUrl),
                contentDescription = "Courier Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                color = Gold,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.36f).sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
