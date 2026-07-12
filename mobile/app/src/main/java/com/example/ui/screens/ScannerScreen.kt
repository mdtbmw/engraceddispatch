@file:Suppress("DEPRECATION")
package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.ScreenHeader
import com.example.ui.components.RoundedSheet
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val isDark by viewModel.darkModeEnabled.collectAsState()
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val activeViewMode by viewModel.activeViewMode.collectAsState()

    // Dynamic animated laser scanning bar (repeats infinite up and down)
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    val accentIconColor = if (isLight) Obsidian else Gold

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Box Scan",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(
                modifier = Modifier.weight(1f),
                containerColor = if (isDark) BackgroundDark else BackgroundLight
            ) {
                val context = LocalContext.current
                val availableParcels by viewModel.parcels.collectAsState()
                val scope = rememberCoroutineScope()
                var isProcessingScan by remember { mutableStateOf(false) }

                // Request camera permission using Accompanist
                val cameraPermissionState = rememberPermissionState(
                    android.Manifest.permission.CAMERA
                )

                // Body content inside a Box (since Scanner Preview fits the screen)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 150.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Outer scan viewfinder card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(40.dp))
                                .background(Obsidian)
                        ) {
                            if (cameraPermissionState.status.isGranted) {
                                // CameraX PreviewView Viewport
                                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                                var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

                                DisposableEffect(lifecycleOwner) {
                                    onDispose {
                                        try {
                                            cameraProvider?.unbindAll()
                                        } catch (e: Exception) {
                                            android.util.Log.e("CameraPreview", "Failed to unbind camera: ${e.message}")
                                        }
                                    }
                                }

                                AndroidView(
                                    factory = { ctx ->
                                        val previewView = PreviewView(ctx).apply {
                                            scaleType = PreviewView.ScaleType.FILL_CENTER
                                        }
                                        val executor = ContextCompat.getMainExecutor(ctx)
                                        cameraProviderFuture.addListener({
                                            try {
                                                val provider = cameraProviderFuture.get()
                                                cameraProvider = provider
                                                val preview = Preview.Builder().build().apply {
                                                    setSurfaceProvider(previewView.surfaceProvider)
                                                }
                                                val imageAnalysis = ImageAnalysis.Builder()
                                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                    .build()
                                                
                                                imageAnalysis.setAnalyzer(executor, QrCodeAnalyzer { scannedText ->
                                                    if (!isProcessingScan) {
                                                        isProcessingScan = true
                                                        viewModel.searchAndTrackParcel(
                                                            context = context,
                                                            trackingNumber = scannedText,
                                                            onSuccess = {
                                                                if (activeViewMode == "rider") {
                                                                    viewModel.setScannedRiderParcel(viewModel.selectedParcel.value)
                                                                    onNavigate("Dashboard")
                                                                } else {
                                                                    onNavigate("ActiveTracking")
                                                                }
                                                            },
                                                            onError = { msg ->
                                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                                scope.launch {
                                                                    delay(3000)
                                                                    isProcessingScan = false
                                                                }
                                                            }
                                                        )
                                                    }
                                                })

                                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                                provider.unbindAll()
                                                provider.bindToLifecycle(
                                                    lifecycleOwner,
                                                    cameraSelector,
                                                    preview,
                                                    imageAnalysis
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.e("CameraPreview", "Failed to bind camera: ${e.message}")
                                            }
                                        }, executor)
                                        previewView
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Fallback static image with transparent layout
                                Image(
                                    painter = rememberAsyncImagePainter("https://images.unsplash.com/photo-1589409514187-c21d14bf0d13?q=80&w=800&auto=format&fit=crop"),
                                    contentDescription = "Scan box camera",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(0.3f)
                                )
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Camera Access Required",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    androidx.compose.material3.Button(
                                        onClick = { cameraPermissionState.launchPermissionRequest() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Gold,
                                            contentColor = Obsidian
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Grant Permission", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Viewfinder mask overlays
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f))
                            )

                            // Target viewfinder brackets and scanning lines
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val lineLen = 40.dp.toPx()
                                val thick = 4.dp.toPx()

                                // 1. Viewfinder Corner Brackets
                                // Top Left
                                drawLine(Color.White, Offset(0f, 0f), Offset(lineLen, 0f), strokeWidth = thick)
                                drawLine(Color.White, Offset(0f, 0f), Offset(0f, lineLen), strokeWidth = thick)

                                // Top Right
                                drawLine(Color.White, Offset(w, 0f), Offset(w - lineLen, 0f), strokeWidth = thick)
                                drawLine(Color.White, Offset(w, 0f), Offset(w, lineLen), strokeWidth = thick)

                                // Bottom Left
                                drawLine(Color.White, Offset(0f, h), Offset(lineLen, h), strokeWidth = thick)
                                drawLine(Color.White, Offset(0f, h), Offset(0f, h - lineLen), strokeWidth = thick)

                                // Bottom Right
                                drawLine(Color.White, Offset(w, h), Offset(w - lineLen, h), strokeWidth = thick)
                                drawLine(Color.White, Offset(w, h), Offset(w, h - lineLen), strokeWidth = thick)

                                // 2. Animated scanning horizontal bar
                                val currentLaserY = h * laserPosition
                                drawLine(
                                    color = Gold,
                                    start = Offset(12.dp.toPx(), currentLaserY),
                                    end = Offset(w - 12.dp.toPx(), currentLaserY),
                                    strokeWidth = 6f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Point camera at a barcode to scan automatically.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Symmetrical Material 3 Bottom Controls Panel (Replaces broken Canvas layouts)
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppSurface
                        ),
                        border = BorderStroke(1.dp, if (isDark) BorderDark else BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var manualCodeInput by remember { mutableStateOf("") }
                            var manualCodeInputError by remember { mutableStateOf<String?>(null) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualCodeInput,
                                    onValueChange = {
                                        manualCodeInput = com.example.util.FormatUtils.formatTrackingId(it)
                                        manualCodeInputError = null
                                    },
                                    placeholder = { Text("Manual Tracking ID", fontSize = 12.sp, color = TextGray) },
                                    isError = manualCodeInputError != null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("scanner_manual_input"),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = AppTextColor),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = if (isDark) Gold else Obsidian,
                                        unfocusedBorderColor = if (isDark) Gold.copy(alpha = 0.3f) else Slate,
                                        cursorColor = if (isDark) Gold else Obsidian
                                    )
                                )

                                androidx.compose.material3.Button(
                                    onClick = {
                                        if (manualCodeInput.isNotBlank()) {
                                            val validationResult = com.example.util.Zod.string(manualCodeInput)
                                                .min(7, "Tracking ID must be at least 7 characters.")
                                                .max(12, "Tracking ID must not exceed 12 characters.")
                                                .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
                                                .safeParse()

                                            when (validationResult) {
                                                is com.example.util.ZodResult.Error -> {
                                                    manualCodeInputError = validationResult.message
                                                    Toast.makeText(context, validationResult.message, Toast.LENGTH_SHORT).show()
                                                }
                                                is com.example.util.ZodResult.Success -> {
                                                    manualCodeInputError = null
                                                    viewModel.searchAndTrackParcel(
                                                        context = context,
                                                        trackingNumber = manualCodeInput,
                                                        onSuccess = {
                                                            if (activeViewMode == "rider") {
                                                                viewModel.setScannedRiderParcel(viewModel.selectedParcel.value)
                                                                onNavigate("Dashboard")
                                                            } else {
                                                                onNavigate("ActiveTracking")
                                                            }
                                                        },
                                                        onError = { msg ->
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Please enter a tracking number.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("scanner_manual_submit"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Gold else Obsidian,
                                        contentColor = if (isDark) Obsidian else Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Submit Code",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf<DecodeHintType, Any>(
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8
            )
        )
        setHints(hints)
    }

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        
        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = reader.decode(binaryBitmap)
            onQrCodeScanned(result.text)
        } catch (e: Exception) {
            // Decoded nothing in this frame
        } finally {
            image.close()
        }
    }
}
