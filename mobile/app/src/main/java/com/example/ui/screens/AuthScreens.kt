package com.example.ui.screens

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RoundedSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.components.PinInputField
import com.example.ui.theme.*
import com.example.viewmodel.DeliveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

val GoogleLogo: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Red top arch
        path(
            fill = SolidColor(Color(0xFFEA4335)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 5.04f)
            curveTo(13.86f, 5.04f, 15.53f, 5.68f, 16.85f, 6.94f)
            lineTo(20.12f, 3.67f)
            curveTo(18.13f, 1.81f, 15.31f, 0.68f, 12.0f, 0.68f)
            curveTo(7.33f, 0.68f, 3.32f, 3.36f, 1.39f, 7.28f)
            lineTo(5.4f, 10.39f)
            curveTo(6.34f, 7.3f, 9.21f, 5.04f, 12.0f, 5.04f)
            close()
        }
        // Green bottom arch
        path(
            fill = SolidColor(Color(0xFF34A853)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 18.96f)
            curveTo(9.21f, 18.96f, 6.34f, 16.7f, 5.40f, 13.61f)
            lineTo(1.39f, 16.72f)
            curveTo(3.32f, 20.64f, 7.33f, 23.32f, 12.0f, 23.32f)
            curveTo(15.19f, 23.32f, 18.06f, 22.18f, 20.08f, 20.24f)
            lineTo(15.93f, 17.02f)
            curveTo(14.88f, 18.25f, 13.51f, 18.96f, 12.0f, 18.96f)
            close()
        }
        // Blue right and bar
        path(
            fill = SolidColor(Color(0xFF4285F4)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(23.32f, 12.0f)
            curveTo(23.32f, 11.23f, 23.25f, 10.45f, 23.11f, 9.71f)
            lineTo(12.0f, 9.71f)
            lineTo(12.0f, 14.3f)
            lineTo(18.35f, 14.3f)
            curveTo(18.08f, 15.78f, 17.21f, 17.09f, 15.93f, 17.02f)
            lineTo(20.08f, 20.24f)
            curveTo(22.52f, 17.99f, 23.32f, 14.82f, 23.32f, 12.0f)
            close()
        }
        // Yellow left arch
        path(
            fill = SolidColor(Color(0xFFFBBC05)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(5.4f, 13.61f)
            curveTo(5.15f, 12.87f, 5.01f, 12.08f, 5.01f, 11.27f)
            curveTo(5.01f, 10.46f, 5.15f, 9.67f, 5.4f, 8.93f)
            lineTo(1.39f, 5.82f)
            curveTo(0.51f, 7.57f, 0.0f, 9.54f, 0.0f, 11.27f)
            curveTo(0.0f, 13.0f, 0.51f, 14.97f, 1.39f, 16.72f)
            lineTo(5.4f, 13.61f)
            close()
        }
    }.build()

enum class LoginStep {
    EMAIL, PIN
}

enum class LoginMode {
    USER, ADMIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    var step by remember { mutableStateOf(LoginStep.EMAIL) }
    var loginMode by remember { mutableStateOf(LoginMode.USER) }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var isValidatingPin by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Google Sign-In & Biometrics
    var showBiometricEnroll by remember { mutableStateOf(false) }
    var showBiometricAuth by remember { mutableStateOf(false) }

    val registeredPin by viewModel.userPin.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName ?: "Google User"
                val googleEmail = account.email ?: ""
                val idToken = account.idToken ?: "google_oauth_token_${googleEmail}"
                
                isValidatingPin = true
                viewModel.signInWithGoogle(idToken, name, googleEmail) { success, errorText ->
                    if (success) {
                        val isProfileIncomplete = viewModel.userPhone.value.isBlank() || viewModel.userPin.value.isBlank()
                        if (isProfileIncomplete) {
                            viewModel.setGoogleAuthInProgress(true)
                            Toast.makeText(context, "Google authenticated! Let's complete your registration details.", Toast.LENGTH_LONG).show()
                            onNavigate("SignUp")
                        } else {
                            viewModel.setGoogleAuthInProgress(false)
                            Toast.makeText(context, "Welcome Back, $name!", Toast.LENGTH_LONG).show()
                            onNavigate("Preloader")
                        }
                    } else {
                        Toast.makeText(context, errorText ?: "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                    }
                    isValidatingPin = false
                }
            } else {
                Toast.makeText(context, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            android.util.Log.w("GoogleSignIn", "Google Sign-In caught exception with code: ${e.statusCode}.", e)
            val isCancelled = e.statusCode == 12501 || e.statusCode == 12502 || e.statusCode == 16 || e.statusCode == 4
            if (isCancelled) {
                Toast.makeText(context, "Google Sign-In cancelled.", Toast.LENGTH_SHORT).show()
            } else {
                val msg = when (e.statusCode) {
                    7 -> "Network error. Check your connection."
                    10 -> "Developer configuration issue: SHA-1 fingerprint mismatch in Firebase console (error 10)."
                    12500 -> "Google Sign-In configuration error."
                    else -> "Google Sign-In failed (code: ${e.statusCode}). Please try again."
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            isValidatingPin = false
        }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val fieldBg = Charcoal
    val fieldBorder = if (isLight) Slate else Gold.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        // Full screen background image, fixed behind the drawer so it doesn't move when drawer scrolls or keyboard opens
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark premium overlay over the entire background image for superior text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // Column containing the spacer and the bottom drawer
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer to push the bottom drawer down, exposing the fixed background image behind it
            Spacer(modifier = Modifier.weight(1.2f))

            // Rounded Rectangle Drawer sitting on top of the fixed background image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundLight,
                border = if (isDark) BorderStroke(1.5.dp, Gold) else BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                        // Title & Subtitle based on step with dynamic transition
                        AnimatedContent(
                            targetState = step,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(450, easing = EaseInOutQuart)) + scaleIn(initialScale = 0.96f, animationSpec = tween(450)))
                                    .togetherWith(fadeOut(animationSpec = tween(350, easing = EaseInOutQuart)) + scaleOut(targetScale = 0.96f, animationSpec = tween(350)))
                            },
                            label = "loginStepAnimation"
                        ) { currentStep ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (currentStep == LoginStep.EMAIL) {
                                    Text(
                                        text = "Welcome Back!",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppOnSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sign in to track your parcels and deliveries.",
                                        fontSize = 15.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "Verify Identity",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppOnSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Enter your secure 4-digit PIN to authenticate.",
                                        fontSize = 15.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // EMAIL FIELD - Height increased to 62.dp as requested!
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { if (currentStep == LoginStep.EMAIL) email = it },
                                    enabled = currentStep == LoginStep.EMAIL,
                                    modifier = Modifier.fillMaxWidth().height(62.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    placeholder = { Text("Email Address", color = TextGray, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = TextGray) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isLight) Obsidian else Gold,
                                        unfocusedBorderColor = fieldBorder,
                                        disabledBorderColor = fieldBorder.copy(alpha = 0.5f),
                                        focusedContainerColor = fieldBg,
                                        unfocusedContainerColor = fieldBg,
                                        disabledContainerColor = fieldBg.copy(alpha = 0.6f),
                                        focusedTextColor = AppOnSurface,
                                        unfocusedTextColor = AppOnSurface,
                                        disabledTextColor = AppOnSurface.copy(alpha = 0.6f),
                                        focusedPlaceholderColor = TextGray,
                                        unfocusedPlaceholderColor = TextGray
                                    )
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                if (currentStep == LoginStep.PIN) {
                                    if (loginMode == LoginMode.ADMIN) {
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            label = { Text("Password", fontFamily = SpaceGrotesk, color = TextGray) },
                                            placeholder = { Text("Enter admin password", fontFamily = SpaceGrotesk, color = TextGray.copy(alpha = 0.5f)) },
                                            leadingIcon = { Icon(Icons.Filled.Lock, null, tint = TextGray) },
                                            singleLine = true,
                                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                            textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (isLight) Obsidian else Gold,
                                                unfocusedBorderColor = fieldBorder,
                                                disabledBorderColor = fieldBorder.copy(alpha = 0.5f),
                                                focusedContainerColor = fieldBg,
                                                unfocusedContainerColor = fieldBg,
                                                disabledContainerColor = fieldBg.copy(alpha = 0.6f),
                                                focusedTextColor = AppOnSurface,
                                                unfocusedTextColor = AppOnSurface,
                                                disabledTextColor = AppOnSurface.copy(alpha = 0.6f),
                                                focusedPlaceholderColor = TextGray,
                                                unfocusedPlaceholderColor = TextGray
                                            ),
                                            modifier = Modifier.fillMaxWidth().height(62.dp),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                    } else {
                                        // PIN STEP (user mode)
                                        PinInputField(
                                            pin = pin,
                                            onPinChange = {
                                                pin = it
                                                isPinError = false
                                            },
                                            isError = isPinError,
                                            obscureText = true
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Automatic validation effect (user PIN mode only)
                                        LaunchedEffect(pin) {
                                            if (pin.length == 4) {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                                isValidatingPin = true
                                                delay(1000)
                                                viewModel.signInWithFirebase(email, pin) { success, errorText ->
                                                    if (success) {
                                                        Toast.makeText(context, "Access Granted! Welcome Back.", Toast.LENGTH_SHORT).show()
                                                        onNavigate("Preloader")
                                                    } else {
                                                        isPinError = true
                                                        Toast.makeText(context, errorText ?: "Invalid email or secure PIN.", Toast.LENGTH_SHORT).show()
                                                        pin = ""
                                                    }
                                                    isValidatingPin = false
                                                }
                                            }
                                        }
                                    }

                                    if (isValidatingPin) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Gold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Authorizing security access...",
                                                fontSize = 12.sp,
                                                color = Gold,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }

                    // BOTTOM ACTIONS BAR (Highly reachable & Pinned to bottom, just above footer!)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Sign-In Button on the left
                        Surface(
                            onClick = {
                                val webClientId = try { com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID } catch (e: Throwable) { "" }
                                val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestProfile()
                                if (webClientId.isNotBlank() && webClientId != "google_web_client_id_placeholder") {
                                    gsoBuilder.requestIdToken(webClientId)
                                }
                                val gso = gsoBuilder.build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, fieldBorder),
                            color = fieldBg,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = GoogleLogo,
                                    contentDescription = "Sign Up / Log In with Google",
                                    tint = Color.Unspecified, // Uses original Google brand colors!
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Continue / Verify Button in the middle
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (step == LoginStep.EMAIL) {
                                    if (email.isBlank()) {
                                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (!email.contains("@")) {
                                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    step = LoginStep.PIN
                                } else {
                                    if (loginMode == LoginMode.ADMIN) {
                                        if (password.isBlank()) {
                                            Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isValidatingPin = true
                                        viewModel.signInWithAdmin(email, password) { success, errorText ->
                                            if (success) {
                                                Toast.makeText(context, "Admin Access Granted!", Toast.LENGTH_SHORT).show()
                                                onNavigate("Preloader")
                                            } else {
                                                Toast.makeText(context, errorText ?: "Invalid admin credentials.", Toast.LENGTH_SHORT).show()
                                            }
                                            isValidatingPin = false
                                        }
                                    } else {
                                        if (pin.length != 4) {
                                            Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isValidatingPin = true
                                        viewModel.signInWithFirebase(email, pin) { success, errorText ->
                                            if (success) {
                                                Toast.makeText(context, "Access Granted! Welcome Back.", Toast.LENGTH_SHORT).show()
                                                onNavigate("Preloader")
                                            } else {
                                                isPinError = true
                                                Toast.makeText(context, errorText ?: "Invalid email or secure PIN.", Toast.LENGTH_SHORT).show()
                                                pin = ""
                                            }
                                            isValidatingPin = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLight) Obsidian else Gold,
                                contentColor = if (isLight) Gold else Obsidian
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (step == LoginStep.EMAIL) "Continue" else if (loginMode == LoginMode.ADMIN) "Admin Login" else "Verify & Login", 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (isLight) Gold else Obsidian
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                                    contentDescription = null, 
                                    tint = if (isLight) Gold else Obsidian, 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Biometric/Fingerprint Login Button on the right
                        Surface(
                            onClick = {
                                val isRegistered = viewModel.biometricRegistered.value
                                if (isRegistered) {
                                    showBiometricAuth = true
                                } else {
                                    showBiometricEnroll = true
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, fieldBorder),
                            color = fieldBg,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Login",
                                    tint = if (isLight) Obsidian else Gold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottom Sign Up or Back to Email link
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (step == LoginStep.EMAIL) {
                            Text(
                                text = "Don't have an account? ",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                            Text(
                                text = "Sign Up",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Obsidian else Gold,
                                modifier = Modifier.clickable { onNavigate("SignUp") }
                            )
                        } else {
                            Text(
                                text = "Back to Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Obsidian else Gold,
                                modifier = Modifier.clickable { step = LoginStep.EMAIL }
                            )
                        }
                    }
                }
            }
        }

        // Floating back button if on PIN step (drawn on top of the background image/drawer)
        if (step == LoginStep.PIN) {
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, start = 20.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { step = LoginStep.EMAIL }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            modifier = if (isDark) Modifier.border(1.5.dp, Gold, RoundedCornerShape(28.dp)) else Modifier,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Customer Support",
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk,
                    color = if (isDark) Color.White else Obsidian
                )
            },
            text = {
                Column {
                    Text(
                        text = "Need help signing in or tracking your package?",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hotline: +234 803 123 4567\nEmail: support@engraceddispatch.com\nAvailable: 24/7 Premium Logistics",
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Gold else Obsidian,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSupportDialog = false
                        try {
                            val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:+2348031234567"))
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Direct dial not available. Please call +234 803 123 4567.", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Call Now", color = if (isDark) Gold else Obsidian, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Close", color = TextGray)
                }
            },
            containerColor = if (isDark) Obsidian else Color.White
        )
    }

    if (showBiometricEnroll) {
        if (registeredPin.isNotEmpty()) {
            FingerprintRegisterDialog(
                isDark = isDark,
                correctPin = registeredPin,
                onDismiss = { showBiometricEnroll = false },
                onRegistered = { verifiedPin ->
                    showBiometricEnroll = false
                    val registeredEmail = if (email.isNotEmpty()) email else viewModel.userEmail.value
                    viewModel.saveBiometricCredentials(registeredEmail, verifiedPin)
                    Toast.makeText(context, "Biometric Credentials Enrolled & Linked Successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showBiometricAuth) {
        val creds = viewModel.getBiometricCredentials()
        val authEmail = creds?.first ?: email.ifEmpty { viewModel.userEmail.value }
        val biometricPin = creds?.second
        if (biometricPin != null) {
            FingerprintAuthDialog(
                isDark = isDark,
                email = authEmail,
                correctPin = biometricPin,
                onDismiss = { showBiometricAuth = false },
                onSuccess = {
                    showBiometricAuth = false
                    isValidatingPin = true
                    viewModel.signInWithFirebase(authEmail, biometricPin) { success, errorText ->
                        if (success) {
                            Toast.makeText(context, "Biometrics Verified! Welcome Back.", Toast.LENGTH_SHORT).show()
                            onNavigate("Preloader")
                        } else {
                            Toast.makeText(context, errorText ?: "Invalid credentials loaded from biometrics.", Toast.LENGTH_SHORT).show()
                        }
                        isValidatingPin = false
                    }
                }
            )
        } else {
            if (registeredPin.isNotEmpty() && authEmail.isNotEmpty()) {
                isValidatingPin = true
                viewModel.signInWithFirebase(authEmail, registeredPin) { success, errorText ->
                    if (success) {
                        Toast.makeText(context, "Biometrics Verified! Welcome Back.", Toast.LENGTH_SHORT).show()
                        onNavigate("Preloader")
                    } else {
                        Toast.makeText(context, errorText ?: "Invalid PIN for $authEmail.", Toast.LENGTH_SHORT).show()
                    }
                    isValidatingPin = false
                }
            } else {
                Toast.makeText(context, "Please enroll biometrics first.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

enum class SignUpStep {
    NAME_SETUP,
    CONTACT_INFO,
    PIN_SETUP
}

@Composable
fun SignUpScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val googleAuthInProg by viewModel.isGoogleAuthInProgress.collectAsState()
    var signUpStep by remember { mutableStateOf(SignUpStep.NAME_SETUP) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(true) }
    
    var isEmailTaken by remember { mutableStateOf(false) }
    var isPhoneTaken by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        if (email.contains("@") && email.contains(".") && !googleAuthInProg) {
            kotlinx.coroutines.delay(600)
            viewModel.checkEmailExists(email) { exists ->
                isEmailTaken = exists
            }
        } else {
            isEmailTaken = false
        }
    }

    LaunchedEffect(phone) {
        val clean = phone.filter { it.isDigit() }
        if (clean.length >= 10) {
            kotlinx.coroutines.delay(600)
            viewModel.checkPhoneExists(phone) { exists ->
                isPhoneTaken = exists
            }
        } else {
            isPhoneTaken = false
        }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val fieldBg = Charcoal
    val fieldBorder = if (isLight) Slate else Gold.copy(alpha = 0.3f)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val registeredPin by viewModel.userPin.collectAsState()
    val vmName by viewModel.userName.collectAsState()
    val vmEmail by viewModel.userEmail.collectAsState()

    LaunchedEffect(googleAuthInProg, vmName, vmEmail) {
        if (googleAuthInProg && vmEmail.isNotEmpty()) {
            email = vmEmail
            val parts = vmName.trim().split(" ")
            if (parts.isNotEmpty()) {
                firstName = parts[0]
                if (parts.size > 1) {
                    lastName = parts.subList(1, parts.size).joinToString(" ")
                }
            }
            if (signUpStep == SignUpStep.NAME_SETUP) {
                signUpStep = SignUpStep.CONTACT_INFO
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName ?: "Google User"
                val googleEmail = account.email ?: ""
                val idToken = account.idToken ?: "google_oauth_token_${googleEmail}"
                
                isRegistering = true
                val finalName = if (firstName.isNotBlank()) "$firstName $lastName".trim() else name
                val finalEmail = googleEmail
                val finalPhone = if (phone.isNotBlank()) phone.trim() else null
                val finalPin = if (pin.isNotBlank()) pin.trim() else null
                
                viewModel.signInWithGoogle(
                    idToken = idToken,
                    name = finalName,
                    email = finalEmail,
                    customPhone = finalPhone,
                    customPin = finalPin
                ) { success, errorText ->
                    if (success) {
                        val isProfileIncomplete = viewModel.userPhone.value.isBlank() || viewModel.userPin.value.isBlank()
                        if (isProfileIncomplete) {
                            viewModel.setGoogleAuthInProgress(true)
                            Toast.makeText(context, "Google authenticated! Let's complete your profile details below.", Toast.LENGTH_LONG).show()
                            // Stay on SignUpScreen. The LaunchedEffect will handle copying the Google profile details and advancing the step.
                        } else {
                            viewModel.setGoogleAuthInProgress(false)
                            Toast.makeText(context, "Welcome Back, $finalName!", Toast.LENGTH_LONG).show()
                            onNavigate("Preloader")
                        }
                    } else {
                        Toast.makeText(context, errorText ?: "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                    }
                    isRegistering = false
                }
            } else {
                Toast.makeText(context, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            android.util.Log.w("GoogleSignIn", "Google Sign-In caught exception with code: ${e.statusCode}.", e)
            val isCancelled = e.statusCode == 12501 || e.statusCode == 12502 || e.statusCode == 16 || e.statusCode == 4
            if (isCancelled) {
                Toast.makeText(context, "Google Sign-In cancelled.", Toast.LENGTH_SHORT).show()
            } else {
                val msg = when (e.statusCode) {
                    7 -> "Network error. Check your connection."
                    10 -> "Developer configuration issue: SHA-1 fingerprint mismatch in Firebase console (error 10)."
                    12500 -> "Google Sign-In configuration error."
                    else -> "Google Sign-In failed (code: ${e.statusCode}). Please try again."
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            isRegistering = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        // Full screen background image, fixed behind the drawer so it doesn't move when drawer scrolls or keyboard opens
        Image(
            painter = painterResource(id = R.drawable.signup),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark premium overlay over the entire background image for superior text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // Column containing the spacer and the bottom drawer
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer to push the bottom drawer down, exposing the fixed background image behind it
            Spacer(modifier = Modifier.weight(1.2f))

            // Rounded Rectangle Drawer sitting on top of the fixed background image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = if (isDark) BackgroundDark else BackgroundLight,
                border = if (isDark) BorderStroke(1.5.dp, Gold) else BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                        // Custom elegant stepper progress bar (3 steps)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isLight) Obsidian else Gold)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (signUpStep == SignUpStep.CONTACT_INFO || signUpStep == SignUpStep.PIN_SETUP) {
                                            if (isLight) Obsidian else Gold
                                        } else {
                                            TextGray.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (signUpStep == SignUpStep.PIN_SETUP) {
                                            if (isLight) Obsidian else Gold
                                        } else {
                                            TextGray.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }

                        Text(
                            text = when (signUpStep) {
                                SignUpStep.NAME_SETUP -> "Step 1 of 3"
                                SignUpStep.CONTACT_INFO -> "Step 2 of 3"
                                SignUpStep.PIN_SETUP -> "Step 3 of 3"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Obsidian.copy(alpha = 0.6f) else Gold.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Multi-step animated content block
                        AnimatedContent(
                            targetState = signUpStep,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(450, easing = EaseInOutQuart)) + scaleIn(initialScale = 0.96f, animationSpec = tween(450)))
                                    .togetherWith(fadeOut(animationSpec = tween(350, easing = EaseInOutQuart)) + scaleOut(targetScale = 0.96f, animationSpec = tween(350)))
                            },
                            label = "signUpStepAnimation"
                        ) { currentStep ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (currentStep == SignUpStep.NAME_SETUP) {
                            // STEP 1: Name Details (First and Second Name)
                            Text(
                                text = "Your Name",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = AppOnSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter your first name and second name to begin registration.",
                                fontSize = 14.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (googleAuthInProg) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Charcoal else GoldenWhiteLight,
                                    border = BorderStroke(1.5.dp, if (isDark) Gold else Obsidian)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Authenticated",
                                            tint = if (isDark) Gold else Obsidian,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Authenticated via Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White else Obsidian
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val webClientId = try { com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID } catch (e: Throwable) { "" }
                                        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestEmail()
                                            .requestProfile()
                                        if (webClientId.isNotBlank() && webClientId != "google_web_client_id_placeholder") {
                                            gsoBuilder.requestIdToken(webClientId)
                                        }
                                        val gso = gsoBuilder.build()
                                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                        // Explicitly sign out to bypass automatic cached account selection
                                        googleSignInClient.signOut().addOnCompleteListener {
                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Charcoal else GoldenWhiteLight,
                                        contentColor = if (isDark) Color.White else Obsidian
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Gold.copy(alpha = 0.3f) else Slate)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = GoogleLogo,
                                            contentDescription = "Google Logo",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Sign Up with Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // First Name
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                modifier = Modifier.fillMaxWidth().height(62.dp),
                                shape = RoundedCornerShape(24.dp),
                                placeholder = { Text("First Name", color = TextGray, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = TextGray) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorder,
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    focusedTextColor = AppOnSurface,
                                    unfocusedTextColor = AppOnSurface,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Second Name (Last Name)
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                modifier = Modifier.fillMaxWidth().height(62.dp),
                                shape = RoundedCornerShape(24.dp),
                                placeholder = { Text("Second Name (Last Name)", color = TextGray, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Filled.Person, null, tint = TextGray) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorder,
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    focusedTextColor = AppOnSurface,
                                    unfocusedTextColor = AppOnSurface,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Next button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (firstName.isBlank() || lastName.isBlank()) {
                                        Toast.makeText(context, "Please fill in your first and second name", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    signUpStep = SignUpStep.CONTACT_INFO
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLight) Obsidian else Gold,
                                    contentColor = if (isLight) Gold else Obsidian
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Continue to Contact",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isLight) Gold else Obsidian
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isLight) Gold else Obsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                        } else if (signUpStep == SignUpStep.CONTACT_INFO) {
                            // STEP 2: Basic Contact Details
                            Text(
                                text = "Contact Details",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = AppOnSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Provide your email address and phone number for dispatch updates.",
                                fontSize = 14.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { if (!googleAuthInProg) email = it },
                                enabled = !googleAuthInProg,
                                isError = isEmailTaken,
                                modifier = Modifier.fillMaxWidth().height(62.dp),
                                shape = RoundedCornerShape(24.dp),
                                placeholder = { Text("Email Address", color = TextGray, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Filled.Email, null, tint = TextGray) },
                                trailingIcon = if (googleAuthInProg) {
                                    { Icon(Icons.Filled.Lock, "Locked email", tint = Gold) }
                                } else null,
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorder,
                                    disabledBorderColor = fieldBorder.copy(alpha = 0.5f),
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    disabledContainerColor = fieldBg.copy(alpha = 0.6f),
                                    focusedTextColor = AppOnSurface,
                                    unfocusedTextColor = AppOnSurface,
                                    disabledTextColor = AppOnSurface.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray
                                )
                            )
                            if (isEmailTaken) {
                                Text(
                                    text = "This email address is already registered",
                                    color = Color(0xFFEA4335),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp).align(Alignment.Start)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Phone
                            val isPhoneWell = remember(phone) { com.example.util.FormatUtils.isPhoneBeginningWell(phone) }
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { input ->
                                    val cleanInput = buildString {
                                        input.forEachIndexed { index, char ->
                                            if (char == '+' && index == 0) {
                                                append(char)
                                            } else if (char.isDigit()) {
                                                append(char)
                                            }
                                        }
                                    }
                                    if (cleanInput.isEmpty()) {
                                        phone = ""
                                    } else {
                                        var maxDigits = 15
                                        if (cleanInput.startsWith("0")) {
                                            maxDigits = 11
                                        } else if (cleanInput.startsWith("234")) {
                                            maxDigits = 13
                                        } else if (cleanInput.startsWith("+234")) {
                                            maxDigits = 14
                                        } else if (cleanInput.startsWith("+1")) {
                                            maxDigits = 12
                                        } else if (cleanInput.startsWith("+44")) {
                                            maxDigits = 13
                                        }
                                        phone = cleanInput.take(maxDigits)
                                    }
                                },
                                isError = (isPhoneTaken || !isPhoneWell) && phone.isNotEmpty(),
                                visualTransformation = com.example.util.PhoneVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().height(62.dp),
                                shape = RoundedCornerShape(24.dp),
                                placeholder = { Text("Phone Number", color = TextGray, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = AppOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isLight) Obsidian else Gold,
                                    unfocusedBorderColor = fieldBorder,
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    focusedTextColor = AppOnSurface,
                                    unfocusedTextColor = AppOnSurface,
                                    focusedPlaceholderColor = TextGray,
                                    unfocusedPlaceholderColor = TextGray,
                                    errorBorderColor = Color(0xFFEA4335)
                                )
                            )
                            if (isPhoneTaken && phone.isNotEmpty()) {
                                Text(
                                    text = "This phone number is already registered",
                                    color = Color(0xFFEA4335),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp).align(Alignment.Start)
                                )
                            } else if (!isPhoneWell && phone.isNotEmpty()) {
                                Text(
                                    text = "Invalid prefix. Must start with local (07/08/09/01) or country code (234/+234)",
                                    color = Color(0xFFEA4335),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp).align(Alignment.Start)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Next step button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (email.isBlank() || phone.isBlank()) {
                                        Toast.makeText(context, "Please fill in all registration fields", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (!email.contains("@")) {
                                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (isEmailTaken) {
                                        Toast.makeText(context, "This email is already registered", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (isPhoneTaken) {
                                        Toast.makeText(context, "This phone number is already registered", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val cleanPhone = phone.filter { it.isDigit() }
                                    if (cleanPhone.length < 10 || cleanPhone.length > 15) {
                                        Toast.makeText(context, "Please enter a valid phone number (10 to 15 digits)", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val blacklistedSequences = listOf(
                                        "1234567890", "0123456789", "1122334455", "5544332211", "9876543210", "0987654321",
                                        "0000000000", "1111111111", "2222222222", "3333333333", "4444444444", "5555555555",
                                        "6666666666", "7777777777", "8888888888", "9999999999"
                                    )
                                    if (cleanPhone in blacklistedSequences || 
                                        cleanPhone.all { it == cleanPhone[0] } || 
                                        cleanPhone.contains("1234567") || 
                                        cleanPhone.contains("000000") ||
                                        cleanPhone.contains("111111") ||
                                        cleanPhone.contains("999999")
                                    ) {
                                        Toast.makeText(context, "Please enter a genuine, active phone number, not a fake or placeholder number", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val validPrefix = cleanPhone.startsWith("234") || 
                                                     cleanPhone.startsWith("07") || 
                                                     cleanPhone.startsWith("08") || 
                                                     cleanPhone.startsWith("09") || 
                                                     cleanPhone.startsWith("01") ||
                                                     cleanPhone.startsWith("1") || 
                                                     cleanPhone.startsWith("44")
                                    if (!validPrefix) {
                                        Toast.makeText(context, "Please enter a valid phone number format with a standard country or mobile operator prefix", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    signUpStep = SignUpStep.PIN_SETUP
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLight) Obsidian else Gold,
                                    contentColor = if (isLight) Gold else Obsidian
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Continue to PIN",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isLight) Gold else Obsidian
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isLight) Gold else Obsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                        } else {
                            // STEP 3: Choose 4-Digit PIN Setup
                            Text(
                                text = "Choose Security PIN",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = AppOnSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Secure your new account with a personalized access PIN.",
                                fontSize = 14.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Custom interactive personalization summary card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isLight) GoldenWhiteLight else Charcoal)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isLight) Obsidian else Gold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = if (isLight) Gold else Obsidian,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "$firstName $lastName".trim(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppOnSurface
                                        )
                                        Text(
                                            text = email,
                                            fontSize = 12.sp,
                                            color = TextGray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Choose Secure 4-Digit PIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppOnSurface,
                                modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, bottom = 8.dp)
                            )

                            // Secure 4-Digit PIN Card Input
                            PinInputField(
                                pin = pin,
                                onPinChange = {
                                    pin = it
                                    isPinError = false
                                },
                                isError = isPinError,
                                obscureText = false // let them see what they chose
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "This PIN serves as your high-security password for future sign-ins.",
                                fontSize = 12.sp,
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { agreeToTerms = !agreeToTerms }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreeToTerms,
                                    onCheckedChange = { agreeToTerms = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = if (isLight) Obsidian else Gold,
                                        uncheckedColor = TextGray,
                                        checkmarkColor = if (isLight) Gold else Obsidian
                                    )
                                )
                                Text(
                                    text = "I agree to the Terms of Service & Privacy Policy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppOnSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // SignUp Button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (!agreeToTerms) {
                                        Toast.makeText(context, "Please agree to the Terms & Conditions to proceed.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (pin.isBlank()) {
                                        Toast.makeText(context, "Please choose a 4-digit security PIN", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (pin.length != 4) {
                                        isPinError = true
                                        Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (viewModel.phoneVerificationRequired.value && !viewModel.isValidNigerianPhoneNumber(phone)) {
                                        Toast.makeText(context, "Phone Verification is enabled. Please enter a valid Nigerian mobile number.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    isRegistering = true
                                    val fullName = "$firstName $lastName".trim()
                                    if (googleAuthInProg) {
                                        viewModel.completeGoogleSignUp(phone, pin) { success, errorText ->
                                            isRegistering = false
                                            if (success) {
                                                viewModel.setGoogleAuthInProgress(false)
                                                Toast.makeText(context, "Google Registration Complete!", Toast.LENGTH_SHORT).show()
                                                onNavigate("Preloader")
                                            } else {
                                                Toast.makeText(context, errorText ?: "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        viewModel.signUpWithFirebase(fullName, email, phone, pin, "customer", "") { success, errorText ->
                                            isRegistering = false
                                            if (success) {
                                                Toast.makeText(context, "Account Created with Security PIN!", Toast.LENGTH_SHORT).show()
                                                onNavigate("Preloader")
                                            } else {
                                                Toast.makeText(context, errorText ?: "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isRegistering,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLight) Obsidian else Gold,
                                    contentColor = if (isLight) Gold else Obsidian
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isRegistering) {
                                        CircularProgressIndicator(
                                            color = if (isLight) Gold else Obsidian,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Register Now",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isLight) Gold else Obsidian
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = if (isLight) Gold else Obsidian,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Back to contact info link
                            Text(
                                text = "Change Registration Info",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Obsidian else Gold,
                                modifier = Modifier
                                    .clickable { signUpStep = SignUpStep.NAME_SETUP }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                        Spacer(modifier = Modifier.height(24.dp))

                    // Pinned bottom back-to-login navigation
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = "Log In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Obsidian else Gold,
                            modifier = Modifier.clickable {
                                viewModel.setGoogleAuthInProgress(false)
                                onNavigate("Login")
                            }
                        )
                    }
                }
            }
        }

        // Floating back button (drawn on top of the background image/drawer)
        Box(
            modifier = Modifier
                .padding(top = 40.dp, start = 20.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable {
                    when (signUpStep) {
                        SignUpStep.PIN_SETUP -> {
                            signUpStep = SignUpStep.CONTACT_INFO
                        }
                        SignUpStep.CONTACT_INFO -> {
                            viewModel.setGoogleAuthInProgress(false)
                            signUpStep = SignUpStep.NAME_SETUP
                        }
                        SignUpStep.NAME_SETUP -> {
                            viewModel.setGoogleAuthInProgress(false)
                            onNavigate("Login")
                        }
                    }
                }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// CUSTOM BIOMETRIC DIALOGS
// ==========================================

@Composable
fun FingerprintRegisterDialog(
    isDark: Boolean,
    correctPin: String,
    onRegistered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinValue by remember { mutableStateOf("") }
    var isPinVerified by remember { mutableStateOf(false) }
    var isPinError by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var isScanning by remember { mutableStateOf(false) }
    var enrollmentError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(isPinVerified) {
        if (isPinVerified && activity != null) {
            authenticateBiometric(
                activity = activity,
                title = "Engraced Dispatch Biometric Enrollment",
                subtitle = "Link secure credentials",
                description = "Scan your fingerprint to link secure access credentials",
                onSuccess = {
                    scanProgress = 1f
                    onRegistered(correctPin)
                },
                onError = { err ->
                    android.util.Log.e("FingerprintRegister", "Native biometric error: $err")
                    enrollmentError = "Biometric enrollment requires a physical fingerprint sensor. Please use your secure PIN instead."
                }
            )
        }
    }
    
    // Animate scanning scale
    val scale by animateFloatAsState(
        targetValue = if (isScanning) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerPulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDark) Obsidian else BackgroundLight)
            .border(1.5.dp, if (isDark) Gold else Obsidian.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Gold else Obsidian),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint icon",
                        tint = if (isDark) Obsidian else Gold,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (!isPinVerified) "Verify Security Access" else "Enroll Biometrics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Obsidian,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (enrollmentError != null) {
                        enrollmentError!!
                    } else if (!isPinVerified) {
                        "Enter your secure 4-digit PIN to authorize biometric pairing."
                    } else if (scanProgress < 1f) {
                        "Hold your finger on the scanner below to capture print."
                    } else {
                        "Biometric fingerprint enrollment complete!"
                    },
                    fontSize = 13.sp,
                    color = if (enrollmentError != null) Color(0xFFEA4335) else TextGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                if (!isPinVerified) {
                    PinInputField(
                        pin = pinValue,
                        onPinChange = {
                            pinValue = it
                            isPinError = false
                        },
                        isError = isPinError,
                        obscureText = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (pinValue == correctPin) {
                                isPinVerified = true
                            } else {
                                isPinError = true
                                pinValue = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Gold else Obsidian,
                            contentColor = if (isDark) Obsidian else Gold
                        )
                    ) {
                        Text(
                            text = "Verify PIN",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Obsidian else Gold
                        )
                    }
                } else {
                    if (enrollmentError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PIN-only access is fully enabled. Biometric enrollment is optional and can be skipped.",
                            fontSize = 13.sp,
                            color = TextGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        // Fingerprint scan area!
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = if (isDark) Gold else Obsidian,
                                strokeWidth = 6.dp,
                                trackColor = (if (isDark) Gold else Obsidian).copy(alpha = 0.1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background((if (isDark) Gold else Obsidian).copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint Sensor",
                                    tint = if (scanProgress >= 1f) (if (isDark) Gold else Obsidian) else TextGray,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (scanProgress < 1f) {
                                "Waiting for secure system credential scan..."
                            } else {
                                "Success! Security Credentials Linked."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scanProgress >= 1f) Color(0xFF34A853) else (if (isDark) Gold else Obsidian)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isPinVerified && (scanProgress >= 1f || enrollmentError != null)) {
                Button(
                    onClick = {
                        if (enrollmentError != null) {
                            onRegistered(correctPin)
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Gold else Obsidian,
                        contentColor = if (isDark) Obsidian else Gold
                    )
                ) {
                    Text(
                        text = if (enrollmentError != null) "Proceed with PIN Security" else "Complete Registration",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Obsidian else Gold
                    )
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
            }
        },
        containerColor = if (isDark) BackgroundDark else BackgroundLight
    )
}

@Composable
fun FingerprintAuthDialog(
    isDark: Boolean,
    email: String,
    correctPin: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var authError by remember { mutableStateOf<String?>(null) }
    var fallbackPinValue by remember { mutableStateOf("") }
    var fallbackPinError by remember { mutableStateOf(false) }
    var showFallback by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(Unit) {
        if (activity != null) {
            authenticateBiometric(
                activity = activity,
                title = "Engraced Dispatch Biometric Verification",
                subtitle = "Authorize secure access",
                description = "Scan your fingerprint or use your face to unlock your secure dispatcher console.",
                onSuccess = {
                    onSuccess()
                },
                onError = { err ->
                    android.util.Log.e("FingerprintAuth", "Native biometric error: $err")
                    authError = "Biometrics unavailable or authentication failed."
                    showFallback = true
                }
            )
        } else {
            authError = "Biometric prompt is not supported in this context."
            showFallback = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDark) Obsidian else BackgroundLight)
            .border(1.5.dp, if (isDark) Gold else Obsidian.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Gold else Obsidian),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint icon",
                        tint = if (isDark) Obsidian else Gold,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Biometric Verification",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Obsidian,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (showFallback) "Biometrics unavailable. Please use fallback PIN." else "Scan your biometric key to unlock secure dispatches for $email",
                    fontSize = 13.sp,
                    color = if (showFallback) Color(0xFFEA4335) else TextGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                if (showFallback) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter secure 4-digit PIN password:",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Obsidian,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    PinInputField(
                        pin = fallbackPinValue,
                        onPinChange = {
                            fallbackPinValue = it
                            fallbackPinError = false
                        },
                        isError = fallbackPinError,
                        obscureText = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (fallbackPinValue == correctPin) {
                                onSuccess()
                            } else {
                                fallbackPinError = true
                                fallbackPinValue = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Gold else Obsidian,
                            contentColor = if (isDark) Obsidian else Color.White
                        )
                    ) {
                        Text(
                            text = "Unlock with PIN",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Obsidian else Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Authenticating with system BiometricPrompt...",
                        fontSize = 14.sp,
                        color = if (isDark) Gold else Obsidian,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showFallback = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Obsidian else BackgroundLight,
                            contentColor = if (isDark) Gold else Obsidian
                        )
                    ) {
                        Text(
                            text = "Use PIN / Password Fallback",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Gold else Obsidian
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Gold else Obsidian
                )
            }
        },
        containerColor = if (isDark) BackgroundDark else BackgroundLight
    )
}

@Composable
fun CompleteProfileScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val currentGoogleName by viewModel.userName.collectAsState()
    val currentGoogleEmail by viewModel.userEmail.collectAsState()
    
    var fullName by remember { mutableStateOf("") }
    
    LaunchedEffect(currentGoogleName) {
        if (currentGoogleName != "Elite Member" && currentGoogleName != "Google User" && currentGoogleName.isNotEmpty()) {
            fullName = currentGoogleName
        }
    }
    
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val fieldBg = Charcoal
    val fieldBorder = if (isLight) Slate else Gold.copy(alpha = 0.3f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
    ) {
        Image(
            painter = painterResource(id = R.drawable.signup),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Elegant Gold Header Logo
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Engraced Dispatch Logo",
                tint = Gold,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "ENGRACED DISPATCH",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "PREMIUM LOGISTICS & DISPATCH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF121212).copy(alpha = 0.85f) else Color.White),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Complete Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Obsidian
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please set up your profile details & PIN to complete your registration.",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Full Name Input
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name", color = if (isDark) Gold.copy(alpha = 0.7f) else Obsidian.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) Gold else Obsidian,
                            unfocusedBorderColor = fieldBorder,
                            focusedContainerColor = fieldBg,
                            unfocusedContainerColor = fieldBg,
                            focusedTextColor = if (isDark) Color.White else Obsidian,
                            unfocusedTextColor = if (isDark) Color.White else Obsidian
                        ),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = if (isDark) Gold else Obsidian) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email Input (Read-only)
                    OutlinedTextField(
                        value = currentGoogleEmail,
                        onValueChange = {},
                        label = { Text("Email (Locked)", color = if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = fieldBorder.copy(alpha = 0.5f),
                            unfocusedBorderColor = fieldBorder.copy(alpha = 0.5f),
                            focusedContainerColor = fieldBg.copy(alpha = 0.5f),
                            unfocusedContainerColor = fieldBg.copy(alpha = 0.5f),
                            focusedTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.5f),
                            unfocusedTextColor = if (isDark) Color.White.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = if (isDark) Gold.copy(alpha = 0.5f) else Obsidian.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone Number Input
                    val isPhoneWell = remember(phone) { com.example.util.FormatUtils.isPhoneBeginningWell(phone) }
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input ->
                            val cleanInput = buildString {
                                input.forEachIndexed { index, char ->
                                    if (char == '+' && index == 0) {
                                        append(char)
                                    } else if (char.isDigit()) {
                                        append(char)
                                    }
                                }
                            }
                            if (cleanInput.isEmpty()) {
                                phone = ""
                            } else {
                                var maxDigits = 15
                                if (cleanInput.startsWith("0")) {
                                    maxDigits = 11
                                } else if (cleanInput.startsWith("234")) {
                                    maxDigits = 13
                                } else if (cleanInput.startsWith("+234")) {
                                    maxDigits = 14
                                } else if (cleanInput.startsWith("+1")) {
                                    maxDigits = 12
                                } else if (cleanInput.startsWith("+44")) {
                                    maxDigits = 13
                                }
                                phone = cleanInput.take(maxDigits)
                            }
                        },
                        isError = !isPhoneWell && phone.isNotEmpty(),
                        visualTransformation = com.example.util.PhoneVisualTransformation(),
                        label = { Text("Phone Number", color = if (isDark) Gold.copy(alpha = 0.7f) else Obsidian.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) Gold else Obsidian,
                            unfocusedBorderColor = fieldBorder,
                            focusedContainerColor = fieldBg,
                            unfocusedContainerColor = fieldBg,
                            focusedTextColor = if (isDark) Color.White else Obsidian,
                            unfocusedTextColor = if (isDark) Color.White else Obsidian,
                            errorBorderColor = Color(0xFFEA4335)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = if (isDark) Gold else Obsidian) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isPhoneWell && phone.isNotEmpty()) {
                        Text(
                            text = "Invalid prefix. Must start with local (07/08/09/01) or country code (234/+234)",
                            color = Color(0xFFEA4335),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp).align(Alignment.Start)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 4-Digit PIN Input
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                        label = { Text("4-Digit Security PIN", color = if (isDark) Gold.copy(alpha = 0.7f) else Obsidian.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) Gold else Obsidian,
                            unfocusedBorderColor = fieldBorder,
                            focusedContainerColor = fieldBg,
                            unfocusedContainerColor = fieldBg,
                            focusedTextColor = if (isDark) Color.White else Obsidian,
                            unfocusedTextColor = if (isDark) Color.White else Obsidian
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PIN", tint = if (isDark) Gold else Obsidian) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (phone.isBlank()) {
                                Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (pin.length != 4) {
                                Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            isSaving = true
                            scope.launch {
                                val emailVal = currentGoogleEmail.ifBlank { "google_user@gmail.com" }
                                viewModel.updateProfile(fullName, emailVal, phone)
                                viewModel.setUserPin(pin)
                                
                                val prefs = context.getSharedPreferences("engraced_dispatch_prefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putString("local_name", fullName)
                                    .putString("local_phone", phone)
                                    .putString("local_pin", pin)
                                    .apply()
                                
                                delay(500)
                                isSaving = false
                                Toast.makeText(context, "Profile Setup Complete!", Toast.LENGTH_SHORT).show()
                                onNavigate("Preloader")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Gold else Obsidian,
                            contentColor = if (isDark) Obsidian else GoldenWhiteLight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = if (isDark) Obsidian else GoldenWhiteLight
                            )
                        } else {
                            Text("SAVE & CONTINUE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}