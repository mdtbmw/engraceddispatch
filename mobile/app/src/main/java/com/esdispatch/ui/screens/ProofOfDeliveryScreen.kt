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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian
import com.esdispatch.ui.theme.TextGray
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
            TopAppBar(
                title = { Text("Proof of Delivery", color = Gold, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Obsidian)
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
                SignaturePadView {
                    // Logic to upload signature would go here
                    viewModel.markParcelDelivered(parcelId)
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
                                    image.close()
                                    viewModel.markParcelDelivered(parcelId)
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
fun SignaturePadView(onComplete: () -> Unit) {
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                onClick = onComplete,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("Submit Signature", color = Obsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
