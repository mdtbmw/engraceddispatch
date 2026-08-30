package com.esdispatch.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.esdispatch.ui.theme.*
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.viewmodel.DeliveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfDeliveryScreen(
    navController: NavController,
    viewModel: DeliveryViewModel,
    parcelId: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSignatureMode by remember { mutableStateOf(false) }
    
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Proof of Delivery",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Parcel: $parcelId",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isSignatureMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isSignatureMode) Gold else Color.DarkGray)
                ) {
                    Text("Take Photo", color = if (!isSignatureMode) Obsidian else Color.White)
                }
                Button(
                    onClick = { isSignatureMode = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSignatureMode) Gold else Color.DarkGray)
                ) {
                    Text("Get Signature", color = if (isSignatureMode) Obsidian else Color.White)
                }
            }

            if (isSignatureMode) {
                SignaturePadView { bitmap ->
                    val stream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                    viewModel.uploadPodAndCompleteParcel(parcelId, stream.toByteArray(), "SIGNATURE")
                    navController.popBackStack()
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(12.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                this.controller = cameraController
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(context)
                        cameraController.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    Log.d("POD", "Photo captured successfully!")
                                    val buffer = image.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    val rotation = image.imageInfo.rotationDegrees
                                    image.close()
                                    val raw = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    val rotated = if (rotation != 0 && raw != null) {
                                        val m = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                                        android.graphics.Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
                                    } else {
                                        raw
                                    }
                                    if (rotated != null) {
                                        val stream = java.io.ByteArrayOutputStream()
                                        rotated.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                                        viewModel.uploadPodAndCompleteParcel(parcelId, stream.toByteArray(), "PHOTO")
                                    } else {
                                        viewModel.markParcelDelivered(parcelId)
                                    }
                                    navController.popBackStack()
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("POD", "Photo capture failed: ${exception.message}")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Capture & Complete", color = Obsidian, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SignaturePadView(onComplete: (android.graphics.Bitmap) -> Unit) {
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var padPx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { padPx = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, Gold, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                            paths = paths + newPath
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
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
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = { paths = emptyList() },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Clear", color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (padPx.width < 10f || padPx.height < 10f) return@Button
                    val w = padPx.width.toInt()
                    val h = padPx.height.toInt()
                    val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
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
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("Submit Signature", color = Obsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
