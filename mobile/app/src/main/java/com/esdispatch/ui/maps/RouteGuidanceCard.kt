package com.esdispatch.ui.maps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esdispatch.ui.theme.*

@Composable
fun RouteGuidanceCard(
    guidance: RouteGuidance,
    onRetry: () -> Unit,
    onNavigate: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Charcoal, shadowElevation = 0.dp) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(guidance.title, color = AppTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(guidance.detail, color = TextGray, fontSize = 12.sp)
            guidance.etaSeconds?.let { seconds ->
                val distance = guidance.distanceMeters?.let { " • %.1f km".format(it / 1000) }.orEmpty()
                Text("About ${kotlin.math.ceil(seconds / 60).toInt().coerceAtLeast(1)} min$distance" +
                    if (guidance.congested) " • Slow traffic" else "",
                    color = AppTextColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            if (guidance.canRetry || onNavigate != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (guidance.canRetry) TextButton(onClick = onRetry, enabled = !guidance.loading,
                        modifier = Modifier.heightIn(min = 44.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = AppTextColor)) { Text("Retry") }
                    if (onNavigate != null) TextButton(onClick = onNavigate, modifier = Modifier.heightIn(min = 44.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = AppTextColor)) { Text("Open navigation") }
                }
            }
        }
    }
}
