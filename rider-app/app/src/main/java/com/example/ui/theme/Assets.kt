package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawMotorcycleSideView(modifier: Modifier = Modifier, bikeColor: Color = WarningOrange) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawOval(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.1f, h * 0.76f),
            size = Size(w * 0.8f, h * 0.08f)
        )

        val wheelRadius = h * 0.22f
        val backWheelCenter = Offset(w * 0.25f, h * 0.64f)
        val frontWheelCenter = Offset(w * 0.75f, h * 0.64f)

        drawCircle(color = Color(0xFF1E2026), radius = wheelRadius, center = backWheelCenter)
        drawCircle(color = Color(0xFF0D0E12), radius = wheelRadius * 0.72f, center = backWheelCenter)
        drawCircle(color = Color(0xFF323540), radius = wheelRadius * 0.65f, center = backWheelCenter, style = Stroke(width = 4f))
        for (i in 0 until 6) {
            val angle = (i * 30) * Math.PI / 180.0
            val dx = (wheelRadius * 0.6f * Math.cos(angle)).toFloat()
            val dy = (wheelRadius * 0.6f * Math.sin(angle)).toFloat()
            drawLine(color = Color(0xFF64748B), start = backWheelCenter, end = Offset(backWheelCenter.x + dx, backWheelCenter.y + dy), strokeWidth = 2.5f)
        }
        drawCircle(color = Color(0xFFCCCCCC), radius = wheelRadius * 0.18f, center = backWheelCenter)

        drawCircle(color = Color(0xFF1E2026), radius = wheelRadius, center = frontWheelCenter)
        drawCircle(color = Color(0xFF0D0E12), radius = wheelRadius * 0.72f, center = frontWheelCenter)
        drawCircle(color = Color(0xFF323540), radius = wheelRadius * 0.65f, center = frontWheelCenter, style = Stroke(width = 4f))
        for (i in 0 until 6) {
            val angle = (i * 30 + 15) * Math.PI / 180.0
            val dx = (wheelRadius * 0.6f * Math.cos(angle)).toFloat()
            val dy = (wheelRadius * 0.6f * Math.sin(angle)).toFloat()
            drawLine(color = Color(0xFF64748B), start = frontWheelCenter, end = Offset(frontWheelCenter.x + dx, frontWheelCenter.y + dy), strokeWidth = 2.5f)
        }
        drawCircle(color = Color(0xFFCCCCCC), radius = wheelRadius * 0.18f, center = frontWheelCenter)

        val exhaustStart = Offset(w * 0.35f, h * 0.62f)
        val exhaustEnd = Offset(w * 0.12f, h * 0.52f)
        drawLine(color = Color(0xFF475569), start = exhaustStart, end = exhaustEnd, strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(color = Color(0xFFE2E8F0), start = exhaustStart, end = Offset(w * 0.15f, h * 0.54f), strokeWidth = 4f, cap = StrokeCap.Round)

        val engineBox = Path().apply {
            moveTo(w * 0.32f, h * 0.65f)
            lineTo(w * 0.58f, h * 0.65f)
            lineTo(w * 0.52f, h * 0.44f)
            lineTo(w * 0.38f, h * 0.44f)
            close()
        }
        drawPath(path = engineBox, color = Color(0xFF1E293B))
        for (i in 0..4) {
            val yOffset = h * 0.46f + (i * h * 0.04f)
            drawLine(color = Color(0xFF0F172A), start = Offset(w * 0.36f, yOffset), end = Offset(w * 0.54f, yOffset), strokeWidth = 3f)
        }

        val bodyColor = if (bikeColor == Color(0xFF111111)) Color(0xFF1E2530) else bikeColor
        val bodyPanel = Path().apply {
            moveTo(w * 0.36f, h * 0.44f)
            cubicTo(w * 0.42f, h * 0.22f, w * 0.58f, h * 0.22f, w * 0.64f, h * 0.38f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.48f, h * 0.48f)
            close()
        }
        drawPath(path = bodyPanel, color = bodyColor)

        val tankHighlight = Path().apply {
            moveTo(w * 0.42f, h * 0.34f)
            cubicTo(w * 0.46f, h * 0.28f, w * 0.54f, h * 0.28f, w * 0.58f, h * 0.34f)
        }
        drawPath(path = tankHighlight, color = Color.White.copy(alpha = 0.25f), style = Stroke(width = 5f))

        drawLine(color = Color(0xFF64748B), start = frontWheelCenter, end = Offset(w * 0.65f, h * 0.26f), strokeWidth = 7f, cap = StrokeCap.Round)
        drawLine(color = Color.White, start = Offset(frontWheelCenter.x - 3f, frontWheelCenter.y - 12f), end = Offset(w * 0.67f, h * 0.34f), strokeWidth = 3f)
        drawLine(color = Color(0xFF0F172A), start = Offset(w * 0.63f, h * 0.26f), end = Offset(w * 0.56f, h * 0.26f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(color = WarningOrange, start = Offset(w * 0.58f, h * 0.28f), end = Offset(w * 0.55f, h * 0.28f), strokeWidth = 2.5f)

        val seatPath = Path().apply {
            moveTo(w * 0.26f, h * 0.44f)
            lineTo(w * 0.42f, h * 0.44f)
            quadraticTo(w * 0.45f, h * 0.48f, w * 0.46f, h * 0.48f)
            lineTo(w * 0.28f, h * 0.48f)
            close()
        }
        drawPath(path = seatPath, color = Color(0xFF0F172A))

        val headLightCenter = Offset(w * 0.68f, h * 0.32f)
        drawCircle(color = Color.White, radius = 7f, center = headLightCenter)
        drawCircle(color = WarningOrange, radius = 10f, center = headLightCenter, style = Stroke(width = 2.5f))
        val beam = Path().apply {
            moveTo(headLightCenter.x + 4f, headLightCenter.y - 2f)
            lineTo(w * 0.96f, h * 0.22f)
            lineTo(w * 0.96f, h * 0.48f)
            lineTo(headLightCenter.x + 4f, headLightCenter.y + 4f)
            close()
        }
        drawPath(path = beam, brush = Brush.horizontalGradient(colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), startX = headLightCenter.x, endX = w * 0.95f))

        val rearCowl = Path().apply {
            moveTo(w * 0.16f, h * 0.40f)
            lineTo(w * 0.28f, h * 0.44f)
            lineTo(w * 0.24f, h * 0.48f)
            close()
        }
        drawPath(path = rearCowl, color = bodyColor)
        drawLine(color = Color(0xFF0F172A), start = Offset(w * 0.18f, h * 0.44f), end = Offset(w * 0.12f, h * 0.54f), strokeWidth = 3f)
    }
}

@Composable
fun DrawMotorcycleTopView(modifier: Modifier = Modifier, bikeColor: Color = WarningOrange) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawOval(color = Color.Black.copy(alpha = 0.3f), topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.7f, h * 0.7f))

        val tireWidth = w * 0.12f
        drawRoundRect(color = Color(0xFF1E2026), topLeft = Offset(w * 0.44f, h * 0.08f), size = Size(tireWidth, h * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
        drawRoundRect(color = Color(0xFF0D0E12), topLeft = Offset(w * 0.44f, h * 0.74f), size = Size(tireWidth, h * 0.20f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))

        drawLine(color = Color(0xFF0F172A), start = Offset(w * 0.18f, h * 0.26f), end = Offset(w * 0.82f, h * 0.26f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawCircle(color = BiroBlue, radius = 6.dp.toPx(), center = Offset(w * 0.18f, h * 0.26f))
        drawCircle(color = BiroBlue, radius = 6.dp.toPx(), center = Offset(w * 0.82f, h * 0.26f))

        drawLine(color = Color(0xFF475569), start = Offset(w * 0.30f, h * 0.26f), end = Offset(w * 0.24f, h * 0.18f), strokeWidth = 4f)
        drawLine(color = Color(0xFF475569), start = Offset(w * 0.70f, h * 0.26f), end = Offset(w * 0.76f, h * 0.18f), strokeWidth = 4f)
        drawOval(color = Color(0xFF1E293B), topLeft = Offset(w * 0.18f, h * 0.14f), size = Size(w * 0.12f, h * 0.06f))
        drawOval(color = Color(0xFF1E293B), topLeft = Offset(w * 0.70f, h * 0.14f), size = Size(w * 0.12f, h * 0.06f))

        val gasTank = Path().apply {
            moveTo(w * 0.5f, h * 0.24f)
            cubicTo(w * 0.25f, h * 0.30f, w * 0.25f, h * 0.52f, w * 0.5f, h * 0.58f)
            cubicTo(w * 0.75f, h * 0.52f, w * 0.75f, h * 0.30f, w * 0.5f, h * 0.24f)
            close()
        }
        drawPath(path = gasTank, color = bikeColor)

        drawCircle(color = Color(0xFFE2E8F0), radius = 10f, center = Offset(w * 0.5f, h * 0.36f))
        drawCircle(color = Color(0xFF334155), radius = 10f, center = Offset(w * 0.5f, h * 0.36f), style = Stroke(width = 3f))

        val highlight = Path().apply {
            moveTo(w * 0.38f, h * 0.34f)
            quadraticTo(w * 0.36f, h * 0.42f, w * 0.42f, h * 0.48f)
        }
        drawPath(path = highlight, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 4f, cap = StrokeCap.Round))

        val seat = Path().apply {
            moveTo(w * 0.34f, h * 0.56f)
            lineTo(w * 0.66f, h * 0.56f)
            lineTo(w * 0.60f, h * 0.74f)
            lineTo(w * 0.40f, h * 0.74f)
            close()
        }
        drawPath(path = seat, color = Color(0xFF1E293B))

        drawRoundRect(color = DangerRed, topLeft = Offset(w * 0.46f, h * 0.94f), size = Size(w * 0.08f, h * 0.02f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
    }
}

@Composable
fun DrawMotorcycleFrontView(modifier: Modifier = Modifier, bikeColor: Color = WarningOrange) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawOval(color = Color.Black.copy(alpha = 0.35f), topLeft = Offset(w * 0.25f, h * 0.88f), size = Size(w * 0.5f, h * 0.08f))

        drawRoundRect(color = Color(0xFF1E2026), topLeft = Offset(w * 0.43f, h * 0.52f), size = Size(w * 0.14f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f))
        drawLine(color = Color(0xFF0F172A), start = Offset(w * 0.5f, h * 0.56f), end = Offset(w * 0.5f, h * 0.88f), strokeWidth = 5f)

        drawLine(color = Color(0xFF64748B), start = Offset(w * 0.36f, h * 0.28f), end = Offset(w * 0.38f, h * 0.78f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(color = Color(0xFF64748B), start = Offset(w * 0.64f, h * 0.28f), end = Offset(w * 0.62f, h * 0.78f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(color = Color.White, start = Offset(w * 0.37f, h * 0.42f), end = Offset(w * 0.38f, h * 0.68f), strokeWidth = 3f)
        drawLine(color = Color.White, start = Offset(w * 0.63f, h * 0.42f), end = Offset(w * 0.62f, h * 0.68f), strokeWidth = 3f)

        val handlebarPath = Path().apply {
            moveTo(w * 0.5f, h * 0.26f)
            lineTo(w * 0.20f, h * 0.22f)
            lineTo(w * 0.16f, h * 0.24f)
        }
        drawPath(path = handlebarPath, color = Color(0xFF0F172A), style = Stroke(width = 8f, cap = StrokeCap.Round))
        val handlebarRight = Path().apply {
            moveTo(w * 0.5f, h * 0.26f)
            lineTo(w * 0.80f, h * 0.22f)
            lineTo(w * 0.84f, h * 0.24f)
        }
        drawPath(path = handlebarRight, color = Color(0xFF0F172A), style = Stroke(width = 8f, cap = StrokeCap.Round))

        drawLine(color = Color(0xFF475569), start = Offset(w * 0.30f, h * 0.24f), end = Offset(w * 0.20f, h * 0.10f), strokeWidth = 4f)
        drawLine(color = Color(0xFF475569), start = Offset(w * 0.70f, h * 0.24f), end = Offset(w * 0.80f, h * 0.10f), strokeWidth = 4f)
        drawCircle(color = Color(0xFF1E293B), radius = 18f, center = Offset(w * 0.20f, h * 0.10f))
        drawCircle(color = Color(0xFF1E293B), radius = 18f, center = Offset(w * 0.80f, h * 0.10f))
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 15f, center = Offset(w * 0.20f, h * 0.10f))
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 15f, center = Offset(w * 0.80f, h * 0.10f))

        val windshield = Path().apply {
            moveTo(w * 0.44f, h * 0.14f)
            lineTo(w * 0.56f, h * 0.14f)
            lineTo(w * 0.62f, h * 0.28f)
            lineTo(w * 0.38f, h * 0.28f)
            close()
        }
        drawPath(path = windshield, color = Color(0xCC111827))

        val fairing = Path().apply {
            moveTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.62f, h * 0.28f)
            lineTo(w * 0.65f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.56f)
            lineTo(w * 0.35f, h * 0.46f)
            close()
        }
        drawPath(path = fairing, color = bikeColor)

        val headlightCenter = Offset(w * 0.5f, h * 0.38f)
        drawCircle(color = Color.White, radius = 22f, center = headlightCenter)
        drawCircle(color = BiroBlue, radius = 22f, center = headlightCenter, style = Stroke(width = 4f))
        drawCircle(color = Color(0xFFE0F2FE), radius = 8f, center = headlightCenter)

        drawOval(color = WarningOrange, topLeft = Offset(w * 0.28f, h * 0.34f), size = Size(w * 0.06f, h * 0.03f))
        drawOval(color = WarningOrange, topLeft = Offset(w * 0.66f, h * 0.34f), size = Size(w * 0.06f, h * 0.03f))
    }
}
