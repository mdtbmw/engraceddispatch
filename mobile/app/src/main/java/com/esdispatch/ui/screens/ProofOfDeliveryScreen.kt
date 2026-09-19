package com.esdispatch.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfDeliveryScreen(
    navController: NavController,
    viewModel: DeliveryViewModel,
    parcelId: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val signatureRequired by viewModel.signatureVerificationEnabled.collectAsState()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSignatureStep by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("Uploading Proof...") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to capture proof of delivery.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember(hasCameraPermission) {
        if (hasCameraPermission) {
            LifecycleCameraController(context).apply {
                bindToLifecycle(lifecycleOwner)
            }
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = if (isSignatureStep) "Customer Signature" else "Proof of Delivery",
                onBack = {
                    if (isSignatureStep) {
                        isSignatureStep = false
                    } else if (capturedBitmap != null) {
                        capturedBitmap = null
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        containerColor = Color(0xFF0D0D11)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info Pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = Charcoal,
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Shipment ID",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = parcelId,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Gold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isSignatureStep) "STEP 2/2 • SIGNATURE" else "STEP 1/2 • PHOTO PROOF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (isSignatureStep) {
                // STEP 2: Customer Signature (when enabled by admin)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customer Signature Required",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Please ask the recipient to sign inside the signature box below to confirm package handover.",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    SignaturePadView { signatureBitmap ->
                        if (isUploading) return@SignaturePadView
                        isUploading = true
                        uploadStatusText = "Uploading Signature..."

                        val stream = ByteArrayOutputStream()
                        signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                        val sigBytes = stream.toByteArray()

                        viewModel.uploadSignatureAndCompleteDelivery(parcelId, sigBytes, "signature") { success ->
                            isUploading = false
                            Toast.makeText(
                                context,
                                if (success) "Signature saved & delivery completed!" else "Delivery updated.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        }
                    }
                }
            } else if (capturedBitmap != null) {
                // PHOTO PREVIEW STATE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Gold, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Product Proof",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Obsidian.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Text(
                            text = "Photo Proof Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isUploading) capturedBitmap = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color.Gray.copy(alpha = 0.5f)),
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isUploading) return@Button
                            isUploading = true
                            uploadStatusText = "Uploading Photo Proof..."

                            val bitmap = capturedBitmap!!
                            // Client-side downscaling (max dimension 1024px, 80% JPEG)
                            val maxDim = 1024
                            val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val (w, h) = if (ratio > 1f) {
                                    maxDim to (maxDim / ratio).toInt()
                                } else {
                                    (maxDim * ratio).toInt() to maxDim
                                }
                                Bitmap.createScaledBitmap(bitmap, w, h, true)
                            } else {
                                bitmap
                            }

                            val stream = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                            val photoBytes = stream.toByteArray()

                            viewModel.uploadDeliveryPhotoAndVerify(parcelId, photoBytes, "proof") { success, _ ->
                                isUploading = false
                                if (success) {
                                    if (signatureRequired) {
                                        isSignatureStep = true
                                    } else {
                                        Toast.makeText(context, "Proof verified & delivery completed!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to upload photo. Please retry.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uploadStatusText, color = Obsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Obsidian, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (signatureRequired) "Confirm & Proceed" else "Confirm & Complete",
                                color = Obsidian,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                // DIRECT CAMERA VIEWFINDER WITH FRAMING GUIDE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    if (hasCameraPermission && cameraController != null) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    this.controller = cameraController
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Visual Framing Guide Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .fillMaxHeight(0.72f)
                                .align(Alignment.Center)
                                .border(2.dp, Gold, RoundedCornerShape(18.dp))
                        ) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Obsidian.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "Position product inside frame",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        // Permission Fallback Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Camera Permission Required",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To capture proof of delivery and complete handover verification, please grant camera access.",
                                fontSize = 12.sp,
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("GRANT CAMERA ACCESS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // One-tap Shutter Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (isUploading || cameraController == null) return@Button
                            val executor = ContextCompat.getMainExecutor(context)
                            isUploading = true
                            uploadStatusText = "Capturing Photo..."
                            cameraController.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val rotation = image.imageInfo.rotationDegrees
                                        image.close()
                                        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        val rotated = if (rotation != 0 && raw != null) {
                                            val m = Matrix().apply { postRotate(rotation.toFloat()) }
                                            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
                                        } else {
                                            raw
                                        }
                                        capturedBitmap = rotated
                                        isUploading = false
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("POD", "Photo capture failed: ${exception.message}")
                                        isUploading = false
                                        Toast.makeText(context, "Camera capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(3.dp, Obsidian)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Take Photo Proof",
                                tint = Obsidian,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignaturePadView(onComplete: (Bitmap) -> Unit) {
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var padPx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var drawTick by remember { mutableStateOf(0L) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { padPx = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(2.dp, Gold, RoundedCornerShape(14.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                            paths = paths + newPath
                            drawTick++
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                            drawTick++
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tick = drawTick
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(
                            width = 5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    paths = emptyList()
                    currentPath = null
                    drawTick++
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, Color.Gray.copy(alpha = 0.5f))
            ) {
                Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (paths.isEmpty()) return@Button
                    try {
                        val w = padPx.width.toInt().coerceIn(100, 2048)
                        val h = padPx.height.toInt().coerceIn(100, 2048)
                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 10f
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            strokeJoin = android.graphics.Paint.Join.ROUND
                            isAntiAlias = true
                        }
                        paths.forEach { path -> canvas.drawPath(path.asAndroidPath(), paint) }
                        currentPath?.let { canvas.drawPath(it.asAndroidPath(), paint) }
                        onComplete(bitmap)
                    } catch (e: Exception) {
                        Log.e("POD", "Failed to generate signature bitmap: ${e.message}")
                    }
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                enabled = paths.isNotEmpty()
            ) {
                Text("Confirm Signature & Complete", color = Obsidian, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}
