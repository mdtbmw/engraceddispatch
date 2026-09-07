package com.esdispatch.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ESDispatch Hugeicons Solid (Filled) Iconography System
 * 
 * Provides official solid (filled) vector icons conforming to Hugeicons standards
 * with built-in tactile spring animations and interaction physics.
 */
object Hugeicons {

    object Solid {

        /** Solid Home / Dashboard */
        val Home: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.09f)
                    lineTo(2.6f, 9.8f)
                    curveTo(2.22f, 10.11f, 2.0f, 10.58f, 2.0f, 11.07f)
                    verticalLineTo(20.0f)
                    curveTo(2.0f, 21.1f, 2.9f, 22.0f, 4.0f, 22.0f)
                    horizontalLineTo(9.0f)
                    curveTo(9.55f, 22.0f, 10.0f, 21.55f, 10.0f, 21.0f)
                    verticalLineTo(15.0f)
                    curveTo(10.0f, 14.45f, 10.45f, 14.0f, 11.0f, 14.0f)
                    horizontalLineTo(13.0f)
                    curveTo(13.55f, 14.0f, 14.0f, 14.45f, 14.0f, 15.0f)
                    verticalLineTo(21.0f)
                    curveTo(14.0f, 21.55f, 14.45f, 22.0f, 15.0f, 22.0f)
                    horizontalLineTo(20.0f)
                    curveTo(21.1f, 22.0f, 22.0f, 21.1f, 22.0f, 20.0f)
                    verticalLineTo(11.07f)
                    curveTo(22.0f, 10.58f, 21.78f, 10.11f, 21.4f, 9.8f)
                    lineTo(12.0f, 2.09f)
                    close()
                }
            }.build()
        }

        /** Solid Package / Box */
        val Package: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Package",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.72f, 2.14f)
                    curveTo(12.27f, 1.95f, 11.73f, 1.95f, 11.28f, 2.14f)
                    lineTo(3.28f, 5.56f)
                    curveTo(2.51f, 5.89f, 2.0f, 6.65f, 2.0f, 7.5f)
                    verticalLineTo(16.5f)
                    curveTo(2.0f, 17.35f, 2.51f, 18.11f, 3.28f, 18.44f)
                    lineTo(11.28f, 21.86f)
                    curveTo(11.73f, 22.05f, 12.27f, 22.05f, 12.72f, 21.86f)
                    lineTo(20.72f, 18.44f)
                    curveTo(21.49f, 18.11f, 22.0f, 17.35f, 22.0f, 16.5f)
                    verticalLineTo(7.5f)
                    curveTo(22.0f, 6.65f, 21.49f, 5.89f, 20.72f, 5.56f)
                    lineTo(12.72f, 2.14f)
                    close()
                    moveTo(12.0f, 4.15f)
                    lineTo(18.66f, 7.0f)
                    lineTo(12.0f, 9.85f)
                    lineTo(5.34f, 7.0f)
                    lineTo(12.0f, 4.15f)
                    close()
                    moveTo(4.0f, 8.8f)
                    lineTo(11.0f, 11.8f)
                    verticalLineTo(19.8f)
                    lineTo(4.0f, 16.8f)
                    verticalLineTo(8.8f)
                    close()
                    moveTo(13.0f, 19.8f)
                    verticalLineTo(11.8f)
                    lineTo(20.0f, 8.8f)
                    verticalLineTo(16.8f)
                    lineTo(13.0f, 19.8f)
                    close()
                }
            }.build()
        }

        /** Solid Map / Radar Route Waypoint */
        val Route: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Route",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.0f)
                    curveTo(8.13f, 2.0f, 5.0f, 5.13f, 5.0f, 9.0f)
                    curveTo(5.0f, 14.25f, 12.0f, 22.0f, 12.0f, 22.0f)
                    curveTo(12.0f, 22.0f, 19.0f, 14.25f, 19.0f, 9.0f)
                    curveTo(19.0f, 5.13f, 15.87f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 11.5f)
                    curveTo(10.62f, 11.5f, 9.5f, 10.38f, 9.5f, 9.0f)
                    curveTo(9.5f, 7.62f, 10.62f, 6.5f, 12.0f, 6.5f)
                    curveTo(13.38f, 6.5f, 14.5f, 7.62f, 14.5f, 9.0f)
                    curveTo(14.5f, 10.38f, 13.38f, 11.5f, 12.0f, 11.5f)
                    close()
                }
            }.build()
        }

        /** Solid Wallet */
        val Wallet: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Wallet",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(21.0f, 7.0f)
                    horizontalLineTo(5.0f)
                    curveTo(3.9f, 7.0f, 3.0f, 7.9f, 3.0f, 9.0f)
                    verticalLineTo(19.0f)
                    curveTo(3.0f, 20.1f, 3.9f, 21.0f, 5.0f, 21.0f)
                    horizontalLineTo(21.0f)
                    curveTo(21.55f, 21.0f, 22.0f, 20.55f, 22.0f, 20.0f)
                    verticalLineTo(8.0f)
                    curveTo(22.0f, 7.45f, 21.55f, 7.0f, 21.0f, 7.0f)
                    close()
                    moveTo(17.0f, 15.5f)
                    curveTo(16.17f, 15.5f, 15.5f, 14.83f, 15.5f, 14.0f)
                    curveTo(15.5f, 13.17f, 16.17f, 12.5f, 17.0f, 12.5f)
                    curveTo(17.83f, 12.5f, 18.5f, 13.17f, 18.5f, 14.0f)
                    curveTo(18.5f, 14.83f, 17.83f, 15.5f, 17.0f, 15.5f)
                    close()
                    moveTo(19.0f, 5.0f)
                    horizontalLineTo(5.0f)
                    curveTo(4.45f, 5.0f, 4.0f, 4.55f, 4.0f, 4.0f)
                    curveTo(4.0f, 3.45f, 4.45f, 3.0f, 5.0f, 3.0f)
                    horizontalLineTo(19.0f)
                    curveTo(19.55f, 3.0f, 20.0f, 3.45f, 20.0f, 4.0f)
                    curveTo(20.0f, 4.55f, 19.55f, 5.0f, 19.0f, 5.0f)
                    close()
                }
            }.build()
        }

        /** Solid User / Profile */
        val Profile: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Profile",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 12.0f)
                    curveTo(14.76f, 12.0f, 17.0f, 9.76f, 17.0f, 7.0f)
                    curveTo(17.0f, 4.24f, 14.76f, 2.0f, 12.0f, 2.0f)
                    curveTo(9.24f, 2.0f, 7.0f, 4.24f, 7.0f, 7.0f)
                    curveTo(7.0f, 9.76f, 9.24f, 12.0f, 12.0f, 12.0f)
                    close()
                    moveTo(12.0f, 14.5f)
                    curveTo(7.58f, 14.5f, 4.0f, 17.19f, 4.0f, 20.5f)
                    curveTo(4.0f, 21.33f, 4.67f, 22.0f, 5.5f, 22.0f)
                    horizontalLineTo(18.5f)
                    curveTo(19.33f, 22.0f, 20.0f, 21.33f, 20.0f, 20.5f)
                    curveTo(20.0f, 17.19f, 16.42f, 14.5f, 12.0f, 14.5f)
                    close()
                }
            }.build()
        }

        /** Solid Chat / Support Bubble */
        val Chat: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Chat",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(20.0f, 2.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                    verticalLineTo(16.0f)
                    curveTo(2.0f, 17.1f, 2.9f, 18.0f, 4.0f, 18.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(21.29f)
                    curveTo(6.0f, 21.89f, 6.71f, 22.22f, 7.17f, 21.83f)
                    lineTo(11.71f, 18.0f)
                    horizontalLineTo(20.0f)
                    curveTo(21.1f, 18.0f, 22.0f, 17.1f, 22.0f, 16.0f)
                    verticalLineTo(4.0f)
                    curveTo(22.0f, 2.9f, 21.1f, 2.0f, 20.0f, 2.0f)
                    close()
                }
            }.build()
        }

        /** Solid Merchant Storefront */
        val Storefront: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Storefront",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(21.9f, 8.89f)
                    lineTo(20.57f, 3.56f)
                    curveTo(20.35f, 2.65f, 19.53f, 2.0f, 18.59f, 2.0f)
                    horizontalLineTo(5.41f)
                    curveTo(4.47f, 2.0f, 3.65f, 2.65f, 3.43f, 3.56f)
                    lineTo(2.1f, 8.89f)
                    curveTo(1.88f, 9.77f, 2.14f, 10.69f, 2.78f, 11.33f)
                    curveTo(3.14f, 11.69f, 3.64f, 11.9f, 4.17f, 11.93f)
                    verticalLineTo(20.0f)
                    curveTo(4.17f, 21.1f, 5.07f, 22.0f, 6.17f, 22.0f)
                    horizontalLineTo(17.83f)
                    curveTo(18.93f, 22.0f, 19.83f, 21.1f, 19.83f, 20.0f)
                    verticalLineTo(11.93f)
                    curveTo(20.36f, 11.9f, 20.86f, 11.69f, 21.22f, 11.33f)
                    curveTo(21.86f, 10.69f, 22.12f, 9.77f, 21.9f, 8.89f)
                    close()
                    moveTo(12.0f, 20.0f)
                    horizontalLineTo(7.0f)
                    verticalLineTo(14.0f)
                    horizontalLineTo(12.0f)
                    verticalLineTo(20.0f)
                    close()
                }
            }.build()
        }

        /** Solid Notification Bell */
        val Bell: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Bell",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.0f)
                    curveTo(10.34f, 2.0f, 9.0f, 3.34f, 9.0f, 5.0f)
                    verticalLineTo(5.29f)
                    curveTo(6.14f, 6.64f, 4.25f, 9.53f, 4.25f, 13.0f)
                    verticalLineTo(16.5f)
                    lineTo(2.6f, 18.15f)
                    curveTo(2.13f, 18.62f, 2.46f, 19.43f, 3.12f, 19.43f)
                    horizontalLineTo(20.88f)
                    curveTo(21.54f, 19.43f, 21.87f, 18.62f, 21.4f, 18.15f)
                    lineTo(19.75f, 16.5f)
                    verticalLineTo(13.0f)
                    curveTo(19.75f, 9.53f, 17.86f, 6.64f, 15.0f, 5.29f)
                    verticalLineTo(5.0f)
                    curveTo(15.0f, 3.34f, 13.66f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 22.0f)
                    curveTo(13.1f, 22.0f, 14.0f, 21.1f, 14.0f, 20.0f)
                    horizontalLineTo(10.0f)
                    curveTo(10.0f, 21.1f, 10.9f, 22.0f, 12.0f, 22.0f)
                    close()
                }
            }.build()
        }

        /** Solid Shield Check / Security */
        val ShieldCheck: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.ShieldCheck",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 1.0f)
                    lineTo(3.5f, 4.8f)
                    curveTo(3.18f, 4.94f, 3.0f, 5.25f, 3.0f, 5.6f)
                    verticalLineTo(11.0f)
                    curveTo(3.0f, 16.55f, 6.84f, 21.74f, 11.66f, 22.95f)
                    curveTo(11.88f, 23.01f, 12.12f, 23.01f, 12.34f, 22.95f)
                    curveTo(17.16f, 21.74f, 21.0f, 16.55f, 21.0f, 11.0f)
                    verticalLineTo(5.6f)
                    curveTo(21.0f, 5.25f, 20.82f, 4.94f, 20.5f, 4.8f)
                    lineTo(12.0f, 1.0f)
                    close()
                    moveTo(10.2f, 16.2f)
                    lineTo(6.7f, 12.7f)
                    curveTo(6.31f, 12.31f, 6.31f, 11.68f, 6.7f, 11.29f)
                    curveTo(7.09f, 10.9f, 7.72f, 10.9f, 8.11f, 11.29f)
                    lineTo(10.9f, 14.08f)
                    lineTo(15.89f, 9.09f)
                    curveTo(16.28f, 8.7f, 16.91f, 8.7f, 17.3f, 9.09f)
                    curveTo(17.69f, 9.48f, 17.69f, 10.11f, 17.3f, 10.5f)
                    lineTo(11.6f, 16.2f)
                    curveTo(11.22f, 16.58f, 10.59f, 16.58f, 10.2f, 16.2f)
                    close()
                }
            }.build()
        }

        /** Solid Motorcycle Dispatch Courier */
        val Motorcycle: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Motorcycle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(19.5f, 13.0f)
                    curveTo(17.57f, 13.0f, 16.0f, 14.57f, 16.0f, 16.5f)
                    curveTo(16.0f, 18.43f, 17.57f, 20.0f, 19.5f, 20.0f)
                    curveTo(21.43f, 20.0f, 23.0f, 18.43f, 23.0f, 16.5f)
                    curveTo(23.0f, 14.57f, 21.43f, 13.0f, 19.5f, 13.0f)
                    close()
                    moveTo(4.5f, 13.0f)
                    curveTo(2.57f, 13.0f, 1.0f, 14.57f, 1.0f, 16.5f)
                    curveTo(1.0f, 18.43f, 2.57f, 20.0f, 4.5f, 20.0f)
                    curveTo(6.43f, 20.0f, 8.0f, 18.43f, 8.0f, 16.5f)
                    curveTo(8.0f, 14.57f, 6.43f, 13.0f, 4.5f, 13.0f)
                    close()
                    moveTo(14.8f, 7.0f)
                    horizontalLineTo(11.0f)
                    lineTo(8.5f, 11.0f)
                    horizontalLineTo(13.2f)
                    lineTo(15.2f, 9.0f)
                    horizontalLineTo(18.0f)
                    verticalLineTo(7.0f)
                    horizontalLineTo(14.8f)
                    close()
                    moveTo(9.8f, 12.0f)
                    lineTo(7.5f, 15.0f)
                    horizontalLineTo(12.0f)
                    lineTo(14.0f, 12.0f)
                    horizontalLineTo(9.8f)
                    close()
                }
            }.build()
        }

        /** Solid Scale Weight / Cargo Capacity */
        val Weight: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Weight",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 3.0f)
                    curveTo(10.9f, 3.0f, 10.0f, 3.9f, 10.0f, 5.0f)
                    curveTo(10.0f, 5.34f, 10.09f, 5.66f, 10.24f, 5.94f)
                    lineTo(5.17f, 19.16f)
                    curveTo(4.91f, 19.82f, 5.39f, 20.54f, 6.1f, 20.54f)
                    horizontalLineTo(17.9f)
                    curveTo(18.61f, 20.54f, 19.09f, 19.82f, 18.83f, 19.16f)
                    lineTo(13.76f, 5.94f)
                    curveTo(13.91f, 5.66f, 14.0f, 5.34f, 14.0f, 5.0f)
                    curveTo(14.0f, 3.9f, 13.1f, 3.0f, 12.0f, 3.0f)
                    close()
                    moveTo(12.0f, 14.0f)
                    curveTo(11.17f, 14.0f, 10.5f, 13.33f, 10.5f, 12.5f)
                    curveTo(10.5f, 11.67f, 11.17f, 11.0f, 12.0f, 11.0f)
                    curveTo(12.83f, 11.0f, 13.5f, 11.67f, 13.5f, 12.5f)
                    curveTo(13.5f, 13.33f, 12.83f, 14.0f, 12.0f, 14.0f)
                    close()
                }
            }.build()
        }

        /** Solid Warning Triangle Alert */
        val AlertTriangle: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.AlertTriangle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.0f)
                    lineTo(1.0f, 21.0f)
                    horizontalLineTo(23.0f)
                    lineTo(12.0f, 2.0f)
                    close()
                    moveTo(12.0f, 9.0f)
                    curveTo(12.55f, 9.0f, 13.0f, 9.45f, 13.0f, 10.0f)
                    verticalLineTo(14.0f)
                    curveTo(13.0f, 14.55f, 12.55f, 15.0f, 12.0f, 15.0f)
                    curveTo(11.45f, 15.0f, 11.0f, 14.55f, 11.0f, 14.0f)
                    verticalLineTo(10.0f)
                    curveTo(11.0f, 9.45f, 11.45f, 9.0f, 12.0f, 9.0f)
                    close()
                    moveTo(12.0f, 18.5f)
                    curveTo(11.17f, 18.5f, 10.5f, 17.83f, 10.5f, 17.0f)
                    curveTo(10.5f, 16.17f, 11.17f, 15.5f, 12.0f, 15.5f)
                    curveTo(12.83f, 15.5f, 13.5f, 16.17f, 13.5f, 17.0f)
                    curveTo(13.5f, 17.83f, 12.83f, 18.5f, 12.0f, 18.5f)
                    close()
                }
            }.build()
        }

        /** Solid Rating Star */
        val Star: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Star",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 17.27f)
                    lineTo(18.18f, 21.0f)
                    lineTo(16.54f, 13.97f)
                    lineTo(22.0f, 9.24f)
                    lineTo(14.81f, 8.63f)
                    lineTo(12.0f, 2.0f)
                    lineTo(9.19f, 8.63f)
                    lineTo(2.0f, 9.24f)
                    lineTo(7.46f, 13.97f)
                    lineTo(5.82f, 21.0f)
                    lineTo(12.0f, 17.27f)
                    close()
                }
            }.build()
        }

        /** Solid Arrow Left / Back Navigation */
        val ArrowLeft: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.ArrowLeft",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(20.0f, 11.0f)
                    horizontalLineTo(7.83f)
                    lineTo(13.42f, 5.41f)
                    curveTo(13.81f, 5.02f, 13.81f, 4.39f, 13.42f, 4.0f)
                    curveTo(13.03f, 3.61f, 12.4f, 3.61f, 12.01f, 4.0f)
                    lineTo(4.42f, 11.59f)
                    curveTo(4.03f, 11.98f, 4.03f, 12.61f, 4.42f, 13.0f)
                    lineTo(12.01f, 20.59f)
                    curveTo(12.4f, 20.98f, 13.03f, 20.98f, 13.42f, 20.59f)
                    curveTo(13.81f, 20.2f, 13.81f, 19.57f, 13.42f, 19.18f)
                    lineTo(7.83f, 13.0f)
                    horizontalLineTo(20.0f)
                    curveTo(20.55f, 13.0f, 21.0f, 12.55f, 21.0f, 12.0f)
                    curveTo(21.0f, 11.45f, 20.55f, 11.0f, 20.0f, 11.0f)
                    close()
                }
            }.build()
        }

        /** Solid Arrow Right */
        val ArrowRight: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.ArrowRight",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(4.0f, 11.0f)
                    horizontalLineTo(16.17f)
                    lineTo(10.58f, 5.41f)
                    curveTo(10.19f, 5.02f, 10.19f, 4.39f, 10.58f, 4.0f)
                    curveTo(10.97f, 3.61f, 11.6f, 3.61f, 11.99f, 4.0f)
                    lineTo(19.58f, 11.59f)
                    curveTo(19.97f, 11.98f, 19.97f, 12.61f, 19.58f, 13.0f)
                    lineTo(11.99f, 20.59f)
                    curveTo(11.6f, 20.98f, 10.97f, 20.98f, 10.58f, 20.59f)
                    curveTo(10.19f, 20.2f, 10.19f, 19.57f, 10.58f, 19.18f)
                    lineTo(16.17f, 13.0f)
                    horizontalLineTo(4.0f)
                    curveTo(3.45f, 13.0f, 3.0f, 12.55f, 3.0f, 12.0f)
                    curveTo(3.0f, 11.45f, 3.45f, 11.0f, 4.0f, 11.0f)
                    close()
                }
            }.build()
        }

        /** Solid Search / Magnifier */
        val Search: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Search",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(10.5f, 2.0f)
                    curveTo(5.81f, 2.0f, 2.0f, 5.81f, 2.0f, 10.5f)
                    curveTo(2.0f, 15.19f, 5.81f, 19.0f, 10.5f, 19.0f)
                    curveTo(12.49f, 19.0f, 14.33f, 18.31f, 15.79f, 17.15f)
                    lineTo(20.32f, 21.68f)
                    curveTo(20.71f, 22.07f, 21.34f, 22.07f, 21.73f, 21.68f)
                    curveTo(22.12f, 21.29f, 22.12f, 20.66f, 21.73f, 20.27f)
                    lineTo(17.25f, 15.79f)
                    curveTo(18.35f, 14.33f, 19.0f, 12.49f, 19.0f, 10.5f)
                    curveTo(19.0f, 5.81f, 15.19f, 2.0f, 10.5f, 2.0f)
                    close()
                    moveTo(10.5f, 4.0f)
                    curveTo(14.09f, 4.0f, 17.0f, 6.91f, 17.0f, 10.5f)
                    curveTo(17.0f, 14.09f, 14.09f, 17.0f, 10.5f, 17.0f)
                    curveTo(6.91f, 17.0f, 4.0f, 14.09f, 4.0f, 10.5f)
                    curveTo(4.0f, 6.91f, 6.91f, 4.0f, 10.5f, 4.0f)
                    close()
                }
            }.build()
        }

        /** Solid Phone */
        val Phone: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Phone",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(6.62f, 10.79f)
                    curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
                    lineTo(15.41f, 15.18f)
                    curveTo(15.69f, 14.9f, 16.08f, 14.82f, 16.43f, 14.93f)
                    curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20.0f, 15.5f)
                    curveTo(20.55f, 15.5f, 21.0f, 15.95f, 21.0f, 16.5f)
                    verticalLineTo(20.0f)
                    curveTo(21.0f, 20.55f, 20.55f, 21.0f, 20.0f, 21.0f)
                    curveTo(10.61f, 21.0f, 3.0f, 13.39f, 3.0f, 4.0f)
                    curveTo(3.0f, 3.45f, 3.45f, 3.0f, 4.0f, 3.0f)
                    horizontalLineTo(7.5f)
                    curveTo(8.05f, 3.0f, 8.5f, 3.45f, 8.5f, 4.0f)
                    curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
                    curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
                    lineTo(6.62f, 10.79f)
                    close()
                }
            }.build()
        }

        /** Solid Shopping Cart */
        val Cart: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Cart",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(7.0f, 18.0f)
                    curveTo(5.9f, 18.0f, 5.01f, 18.9f, 5.01f, 20.0f)
                    curveTo(5.01f, 21.1f, 5.9f, 22.0f, 7.0f, 22.0f)
                    curveTo(8.1f, 22.0f, 9.0f, 21.1f, 9.0f, 20.0f)
                    curveTo(9.0f, 18.9f, 8.1f, 18.0f, 7.0f, 18.0f)
                    close()
                    moveTo(17.0f, 18.0f)
                    curveTo(15.9f, 18.0f, 15.01f, 18.9f, 15.01f, 20.0f)
                    curveTo(15.01f, 21.1f, 15.9f, 22.0f, 17.0f, 22.0f)
                    curveTo(18.1f, 22.0f, 19.0f, 21.1f, 19.0f, 20.0f)
                    curveTo(19.0f, 18.9f, 18.1f, 18.0f, 17.0f, 18.0f)
                    close()
                    moveTo(7.2f, 14.63f)
                    lineTo(7.23f, 14.53f)
                    lineTo(8.1f, 13.0f)
                    horizontalLineTo(15.55f)
                    curveTo(16.3f, 13.0f, 16.96f, 12.59f, 17.3f, 11.97f)
                    lineTo(21.16f, 4.96f)
                    curveTo(21.41f, 4.5f, 21.08f, 4.0f, 20.55f, 4.0f)
                    horizontalLineTo(5.21f)
                    lineTo(4.27f, 2.0f)
                    horizontalLineTo(1.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(3.0f)
                    lineTo(6.6f, 11.59f)
                    lineTo(5.25f, 14.04f)
                    curveTo(4.52f, 15.37f, 5.48f, 17.0f, 7.0f, 17.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(15.0f)
                    horizontalLineTo(7.42f)
                    curveTo(7.29f, 15.0f, 7.17f, 14.83f, 7.2f, 14.63f)
                    close()
                }
            }.build()
        }

        /** Solid Heart / Favorite */
        val Heart: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Heart",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 21.35f)
                    lineTo(10.55f, 20.03f)
                    curveTo(5.4f, 15.36f, 2.0f, 12.28f, 2.0f, 8.5f)
                    curveTo(2.0f, 5.42f, 4.42f, 3.0f, 7.5f, 3.0f)
                    curveTo(9.24f, 3.0f, 10.91f, 3.81f, 12.0f, 5.09f)
                    curveTo(13.09f, 3.81f, 14.76f, 3.0f, 16.5f, 3.0f)
                    curveTo(19.58f, 3.0f, 22.0f, 5.42f, 22.0f, 8.5f)
                    curveTo(22.0f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                    lineTo(12.0f, 21.35f)
                    close()
                }
            }.build()
        }

        /** Solid Check Circle / Success */
        val CheckCircle: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.CheckCircle",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    curveTo(2.0f, 17.52f, 6.48f, 22.0f, 12.0f, 22.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
                    close()
                    moveTo(10.0f, 16.2f)
                    lineTo(5.8f, 12.0f)
                    lineTo(7.21f, 10.59f)
                    lineTo(10.0f, 13.37f)
                    lineTo(16.79f, 6.58f)
                    lineTo(18.2f, 8.0f)
                    lineTo(10.0f, 16.2f)
                    close()
                }
            }.build()
        }

        /** Solid Close / Cancel */
        val Close: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Close",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(19.0f, 6.41f)
                    lineTo(17.59f, 5.0f)
                    lineTo(12.0f, 10.59f)
                    lineTo(6.41f, 5.0f)
                    lineTo(5.0f, 6.41f)
                    lineTo(10.59f, 12.0f)
                    lineTo(5.0f, 17.59f)
                    lineTo(6.41f, 19.0f)
                    lineTo(12.0f, 13.41f)
                    lineTo(17.59f, 19.0f)
                    lineTo(19.0f, 17.59f)
                    lineTo(13.41f, 12.0f)
                    lineTo(19.0f, 6.41f)
                    close()
                }
            }.build()
        }

        /** Solid Flash / Lightning */
        val Flash: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Flash",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(7.0f, 2.0f)
                    verticalLineTo(13.0f)
                    horizontalLineTo(10.0f)
                    verticalLineTo(22.0f)
                    lineTo(17.0f, 10.0f)
                    horizontalLineTo(13.0f)
                    lineTo(16.0f, 2.0f)
                    horizontalLineTo(7.0f)
                    close()
                }
            }.build()
        }

        /** Solid Trash / Delete */
        val Trash: ImageVector by lazy {
            ImageVector.Builder(
                name = "Hugeicon.Solid.Trash",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(6.0f, 19.0f)
                    curveTo(6.0f, 20.1f, 6.9f, 21.0f, 8.0f, 21.0f)
                    horizontalLineTo(16.0f)
                    curveTo(17.1f, 21.0f, 18.0f, 20.1f, 18.0f, 19.0f)
                    verticalLineTo(7.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(19.0f)
                    close()
                    moveTo(19.0f, 4.0f)
                    horizontalLineTo(15.5f)
                    lineTo(14.5f, 3.0f)
                    horizontalLineTo(9.5f)
                    lineTo(8.5f, 4.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(6.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(4.0f)
                    close()
                }
            }.build()
        }
    }
}

/**
 * Animated Hugeicon Composable
 * 
 * Enforces standardized spring physics (Stiffness 400f, Damping 0.70f)
 * with tactile touch compression and smooth selection magnification.
 */
@Composable
fun AnimatedHugeIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            selected -> 1.08f
            else -> 1.0f
        },
        animationSpec = SpringPhysics.TouchPress,
        label = "hugeicon_scale"
    )

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .size(size)
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    )
}
