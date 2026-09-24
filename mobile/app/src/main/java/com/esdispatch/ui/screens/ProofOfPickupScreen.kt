package com.esdispatch.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.AppBackground
import com.esdispatch.ui.theme.AppSurface
import com.esdispatch.ui.theme.AppTextColor
import com.esdispatch.ui.theme.Charcoal
import com.esdispatch.ui.theme.Gold
import com.esdispatch.ui.theme.Obsidian
import com.esdispatch.ui.theme.TextGray
import com.esdispatch.viewmodel.DeliveryViewModel
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfPickupScreen(
    parcelId: String,
    navController: androidx.navigation.NavController,
    viewModel: DeliveryViewModel,
    isDark: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val parcel by remember {
        derivedStateOf { viewModel.parcels.value.find { it.id == parcelId } }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for proof of pickup", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }

    LaunchedEffect(hasCameraPermission, lifecycleOwner) {
        if (hasCameraPermission) {
            try {
                cameraController.unbind()
                cameraController.bindToLifecycle(lifecycleOwner)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Proof of Pickup",
                onBack = { navController.popBackStack() },
                
            )
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (parcel == null) {
                Text("Loading or parcel not found...", color = AppTextColor)
                return@Scaffold
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = Gold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Mandatory Verification",
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            fontSize = 12.sp
                        )
                        Text(
                            "Please capture a clear photo of the item before picking it up from the customer. This ensures transparency and verification of condition.",
                            color = AppTextColor,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera Permission Required", color = TextGray)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                controller = cameraController
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Capture Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (isSubmitting) return@FloatingActionButton
                                isSubmitting = true
                                val photoFile = File(context.cacheDir, "pickup_proof_${parcelId}_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                cameraController.takePicture(
                                    outputOptions,
                                    Executors.newSingleThreadExecutor(),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            val bytes = photoFile.readBytes()
                                            viewModel.uploadPickupPhotoAndVerify(parcelId, bytes) { success, err ->
                                                isSubmitting = false
                                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                    if (success) {
                                                        Toast.makeText(context, "Pickup Proof Saved! Status updated.", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    } else {
                                                        Toast.makeText(context, err ?: "Failed to upload photo", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isSubmitting = false
                                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                Toast.makeText(context, "Failed to capture photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            },
                            containerColor = Gold,
                            contentColor = Obsidian,
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
