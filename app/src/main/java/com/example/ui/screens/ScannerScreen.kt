package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.DeliveryViewModel
import com.example.ui.components.*
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(viewModel: DeliveryViewModel) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (!granted) showError = true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(scannedCode) {
        scannedCode?.let { code ->
            if (code.startsWith("ESD-", ignoreCase = true)) {
                isProcessing = true
                viewModel.navigateTo(AppView.ActiveTracking(code.uppercase()))
            }
        }
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        if (!hasCameraPermission) {
            Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Camera Permission Required", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Enable camera access to scan tracking QR codes", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                PremiumGradientButton("Grant Permission", icon = Icons.Default.CameraAlt, onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                Spacer(Modifier.height(12.dp))
                if (showError) {
                    Text("Permission denied. Enable in Settings.", color = DangerRed, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                }
                OutlinedGradientButton("Back", onClick = { viewModel.navigateBack() })
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val barcodeScanner = BarcodeScanning.getClient()
                        val analysis = ImageAnalysis.Builder().setTargetResolution(Size(1280, 720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            if (isProcessing) { imageProxy.close(); return@setAnalyzer }
                            @androidx.camera.core.ExperimentalGetImage val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(inputImage).addOnSuccessListener { barcodes: List<Barcode> ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { value ->
                                            if (scannedCode == null && (value.startsWith("ESD-", ignoreCase = true) || value.length == 14)) {
                                                scannedCode = value
                                            }
                                        }
                                    }
                                }.addOnCompleteListener { imageProxy.close() }
                            } else { imageProxy.close() }
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(ctx as androidx.lifecycle.LifecycleOwner, cameraSelector, preview, analysis) } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
                IconButton(onClick = { viewModel.navigateBack() }, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Point camera at QR code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Scans ESD tracking numbers automatically", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(60.dp))
            }

            if (scannedCode != null && !isProcessing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(color = SuccessGreen.copy(alpha = 0.9f), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Code Scanned!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(scannedCode!!, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Redirecting...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
