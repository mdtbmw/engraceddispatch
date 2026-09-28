package com.esdispatch.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.esdispatch.data.ParcelStatus
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.util.FormatUtils
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

    val parcels by viewModel.parcels.collectAsState()
    val riderAssignments by viewModel.riderAssignments.collectAsState()
    val parcel = remember(parcels, riderAssignments, parcelId) {
        parcels.find { it.id == parcelId } ?: riderAssignments.find { it.id == parcelId }
    }

    val isPickup = remember(parcel?.status) {
        parcel?.status == ParcelStatus.ASSIGNED ||
        parcel?.status == ParcelStatus.ARRIVED_PICKUP
    }

    var otpInput by remember { mutableStateOf("") }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpErrorMessage by remember { mutableStateOf<String?>(null) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSignatureStep by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("Processing...") }

    androidx.activity.compose.BackHandler {
        if (isSignatureStep) {
            isSignatureStep = false
        } else if (capturedBitmap != null) {
            capturedBitmap = null
        } else {
            navController.popBackStack()
        }
    }

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
            Toast.makeText(context, "Camera permission is required to capture proof.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    LaunchedEffect(hasCameraPermission, lifecycleOwner, capturedBitmap, isSignatureStep) {
        if (hasCameraPermission && capturedBitmap == null && !isSignatureStep) {
            try {
                cameraController.unbind()
                cameraController.bindToLifecycle(lifecycleOwner)
            } catch (e: Exception) {
                Log.e("POD", "Failed to bind camera: ${e.message}")
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraController.unbind()
            } catch (e: Exception) {
                Log.e("POD", "Failed to unbind camera on dispose: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = when {
                    isPickup -> "Pickup Photo Proof"
                    isSignatureStep -> "Customer Signature"
                    capturedBitmap != null -> "Handover & PIN"
                    else -> "Proof of Delivery"
                },
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
            // Header Info Pill: Tracking ID & Step Badge
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
                            text = FormatUtils.formatDisplayTrackingId(parcelId),
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
                        val stepBadgeText = when {
                            isPickup -> "STEP 1/1 • PICKUP PHOTO"
                            isSignatureStep -> if (signatureRequired) "STEP 3/3 • SIGNATURE" else "STEP 2/2 • SIGNATURE"
                            capturedBitmap != null -> if (signatureRequired) "STEP 2/3 • RECIPIENT PIN" else "STEP 2/2 • RECIPIENT PIN"
                            else -> if (signatureRequired) "STEP 1/3 • PHOTO PROOF" else "STEP 1/2 • PHOTO PROOF"
                        }
                        Text(
                            text = stepBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            when {
                // STEP 3 (Optional): Customer Signature Screen
                isSignatureStep -> {
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
                            text = "Please ask the recipient to sign inside the signature pad below to confirm receipt.",
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
                }

                // STEP 2: Photo Captured -> Enter PIN (Delivery) or Confirm Pickup (Pickup)
                capturedBitmap != null -> {
                    if (isPickup) {
                        // PICKUP CONFIRMATION
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                    contentDescription = "Collected Parcel Proof",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Obsidian.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Gold)
                                ) {
                                    Text(
                                        text = "Pickup Photo Preview",
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
                                        uploadStatusText = "Uploading Pickup Proof..."

                                        val bitmap = capturedBitmap!!
                                        val maxDim = 1024
                                        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                                            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                            val (w, h) = if (ratio > 1f) maxDim to (maxDim / ratio).toInt() else (maxDim * ratio).toInt() to maxDim
                                            Bitmap.createScaledBitmap(bitmap, w, h, true)
                                        } else {
                                            bitmap
                                        }

                                        val stream = ByteArrayOutputStream()
                                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                        val photoBytes = stream.toByteArray()

                                        viewModel.uploadPickupPhoto(parcelId, photoBytes) { uploadOk, _ ->
                                            viewModel.updateParcelStatusByRider(parcelId, ParcelStatus.PICKED_UP, 0.40f) { success, err ->
                                                isUploading = false
                                                if (success) {
                                                    Toast.makeText(context, "Package collected! Status updated to Picked Up.", Toast.LENGTH_SHORT).show()
                                                    navController.popBackStack()
                                                } else {
                                                    Toast.makeText(context, err ?: "Failed to update pickup status", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.6f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                                    enabled = !isUploading
                                ) {
                                    if (isUploading) {
                                        CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(uploadStatusText, color = Obsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Obsidian, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CONFIRM PICKUP", color = Obsidian, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // DELIVERY HANDOVER: PHOTO PREVIEW + RECIPIENT PIN & CONTACT
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Compact Photo Preview Card with Retake Action
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Charcoal,
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        bitmap = capturedBitmap!!.asImageBitmap(),
                                        contentDescription = "Delivery Proof Thumbnail",
                                        modifier = Modifier
                                            .size(94.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Arrival Photo Captured",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Photo proof ready for upload",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Surface(
                                            onClick = { if (!isUploading && !isVerifyingOtp) capturedBitmap = null },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Gold.copy(alpha = 0.15f),
                                            border = BorderStroke(0.8.dp, Gold)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Retake Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Receiver Name & Quick Contact Action Buttons
                            val receiverPhone = parcel?.receiverPhone ?: ""
                            val receiverName = parcel?.receiverName?.ifBlank { "Recipient" } ?: "Recipient"

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Charcoal,
                                border = BorderStroke(1.dp, Slate.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = "Recipient",
                                            fontSize = 10.sp,
                                            color = TextGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = receiverName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Call Receiver Button
                                        Surface(
                                            onClick = {
                                                if (receiverPhone.isNotBlank()) {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$receiverPhone"))
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Recipient phone number not available", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Gold.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Gold)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Phone, contentDescription = "Call", tint = Gold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            }
                                        }

                                        // SMS Receiver Button
                                        Surface(
                                            onClick = {
                                                if (receiverPhone.isNotBlank()) {
                                                    val formattedId = FormatUtils.formatDisplayTrackingId(parcelId)
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$receiverPhone")).apply {
                                                        putExtra("sms_body", "Hello $receiverName, your ESDispatch courier has arrived with delivery ($formattedId). Please provide your 4-digit handover PIN.")
                                                    }
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Recipient phone number not available", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Charcoal,
                                            border = BorderStroke(1.dp, TextGray.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Sms, contentDescription = "SMS", tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Recipient Handover PIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Ask the recipient for their 4-digit security PIN to complete handover.",
                                fontSize = 12.sp,
                                color = TextGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // 4-Digit Numeric PIN Input
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() }.take(4)
                                    otpInput = digitsOnly
                                    otpErrorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        text = "• • • •",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextGray.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Gold,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 12.sp
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = Gold.copy(alpha = 0.35f),
                                    focusedContainerColor = Charcoal,
                                    unfocusedContainerColor = Charcoal,
                                    cursorColor = Gold
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height(66.dp)
                            )

                            if (otpErrorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = otpErrorMessage!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF5252),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // VERIFY & COMPLETE HANDOVER BUTTON
                            Button(
                                onClick = {
                                    if (otpInput.length == 4 && !isVerifyingOtp && !isUploading) {
                                        isVerifyingOtp = true
                                        otpErrorMessage = null
                                        uploadStatusText = "Verifying PIN & Finalizing..."

                                        val bitmap = capturedBitmap!!
                                        val maxDim = 1024
                                        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                                            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                            val (w, h) = if (ratio > 1f) maxDim to (maxDim / ratio).toInt() else (maxDim * ratio).toInt() to maxDim
                                            Bitmap.createScaledBitmap(bitmap, w, h, true)
                                        } else {
                                            bitmap
                                        }

                                        val stream = ByteArrayOutputStream()
                                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                        val photoBytes = stream.toByteArray()

                                        viewModel.verifyDeliveryOtpByRider(parcelId, otpInput) { otpSuccess, err ->
                                            if (otpSuccess) {
                                                viewModel.uploadDeliveryPhotoAndVerify(parcelId, photoBytes, "proof") { photoSuccess, _ ->
                                                    isVerifyingOtp = false
                                                    if (signatureRequired) {
                                                        isSignatureStep = true
                                                    } else {
                                                        Toast.makeText(context, "Handover verified & delivery completed!", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    }
                                                }
                                            } else {
                                                isVerifyingOtp = false
                                                otpErrorMessage = err ?: "Invalid 4-digit PIN. Please verify with recipient."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .tactilePress(scaleDown = 0.96f),
                                enabled = otpInput.length == 4 && !isVerifyingOtp && !isUploading,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = Obsidian,
                                    disabledContainerColor = Gold.copy(alpha = 0.3f),
                                    disabledContentColor = Obsidian.copy(alpha = 0.5f)
                                )
                            ) {
                                if (isVerifyingOtp || isUploading) {
                                    CircularProgressIndicator(color = Obsidian, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(uploadStatusText, color = Obsidian, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                } else {
                                    Icon(
                                        imageVector = if (signatureRequired) Icons.Filled.DriveFileRenameOutline else Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Obsidian,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (signatureRequired) "VERIFY PIN & PROCEED TO SIGNATURE" else "VERIFY PIN & COMPLETE HANDOVER",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Obsidian
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 1 (DEFAULT): DIRECT CAMERA VIEWFINDER
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            if (hasCameraPermission) {
                                AndroidView(
                                    factory = { ctx ->
                                        PreviewView(ctx).apply {
                                            layoutParams = android.view.ViewGroup.LayoutParams(
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                                            scaleType = PreviewView.ScaleType.FILL_CENTER
                                            controller = cameraController
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
                                            text = if (isPickup) "Position collected parcel inside frame" else "Position parcel proof inside frame",
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
                                    if (isUploading) return@Button
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
