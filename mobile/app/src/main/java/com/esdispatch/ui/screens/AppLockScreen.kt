package com.esdispatch.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.esdispatch.R
import com.esdispatch.ui.theme.*
import com.esdispatch.util.BiometricHelper
import com.esdispatch.viewmodel.DeliveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    viewModel: DeliveryViewModel,
    onUnlocked: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userName by viewModel.userName.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }

    val biometricRegistered by viewModel.biometricRegistered.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    val biometricAvailable = remember(context) {
        BiometricHelper.isBiometricAvailable(context)
    }
    val canUseBiometrics = biometricAvailable && biometricRegistered && biometricEnabled

    val triggerBiometrics: () -> Unit = {
        (context as? FragmentActivity)?.let { activity ->
            BiometricHelper.authenticate(
                activity = activity,
                title = "ESDispatch Biometric Security",
                subtitle = "Authenticate to unlock your dispatch console",
                description = "Confirm fingerprint to continue",
                onSuccess = {
                    viewModel.unlockApp()
                    com.esdispatch.util.SoundManager.playSuccessArpeggio()
                    onUnlocked()
                },
                onError = { err ->
                    errorMessage = err
                }
            )
        }
    }

    // Automatically prompt biometrics on launch only if enrolled & enabled
    LaunchedEffect(canUseBiometrics) {
        delay(350)
        if (canUseBiometrics) {
            triggerBiometrics()
        }
    }

    val onDigitPress: (String) -> Unit = { digit ->
        if (pinInput.length < 4) {
            val newPin = pinInput + digit
            pinInput = newPin
            isError = false
            errorMessage = ""

            if (newPin.length == 4) {
                coroutineScope.launch {
                    val isValid = viewModel.verifyUserPin(newPin)
                    if (isValid) {
                        viewModel.unlockApp()
                        com.esdispatch.util.SoundManager.playSuccessArpeggio()
                        onUnlocked()
                    } else {
                        isError = true
                        errorMessage = "Incorrect PIN. Try again."
                        com.esdispatch.util.SoundManager.playErrorBuzz()
                        // Shake animation
                        shakeOffset.animateTo(
                            targetValue = 24f,
                            animationSpec = keyframes {
                                durationMillis = 350
                                0f at 0
                                -24f at 70
                                24f at 140
                                -16f at 210
                                16f at 280
                                0f at 350
                            }
                        )
                        delay(200)
                        pinInput = ""
                    }
                }
            }
        }
    }

    val onDeletePress: () -> Unit = {
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
            isError = false
            errorMessage = ""
        }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isDark = !isLight
    val screenBg = if (isDark) BackgroundDark else BackgroundLight
    val keyBg = Charcoal
    val keyBorder = if (isDark) Color(0xFF2C2C2C) else BorderLight
    val keyTextColor = AppTextColor
    val iconColor = AppTextColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .navigationBarsPadding()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header Brand & Greeting
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "ESDispatch Logo",
                    tint = Gold,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PREMIUM LOGISTICS & DISPATCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Gold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (userName.isNotBlank()) "Welcome Back, $userName" else "Welcome Back",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = keyTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your 4-digit security PIN",
                    fontSize = 13.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // PIN dots with shake animation on error
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(x = shakeOffset.value.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        val dotColor = when {
                            isError -> Color(0xFFEF4444)
                            isFilled -> Gold
                            else -> if (isDark) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    1.5.dp,
                                    if (isFilled || isError) dotColor else if (isDark) Color(0xFF404040) else Color(0xFFCBD5E1),
                                    CircleShape
                                )
                        )
                    }
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Keypad (4 rows: 1-3, 4-6, 7-9, Biometrics/0/Delete)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "BIO" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(if (canUseBiometrics) keyBg else Color.Transparent)
                                            .then(if (canUseBiometrics) Modifier.border(1.dp, keyBorder, CircleShape) else Modifier)
                                            .clickable(enabled = canUseBiometrics) {
                                                triggerBiometrics()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (canUseBiometrics) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Fingerprint Login",
                                                tint = Gold,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }
                                }
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(keyBg)
                                            .border(1.dp, keyBorder, CircleShape)
                                            .clickable { onDeletePress() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = iconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(keyBg)
                                            .border(1.dp, keyBorder, CircleShape)
                                            .clickable { onDigitPress(key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = keyTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch account option
            Text(
                text = "Log In with Password or Switch Account",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGray,
                modifier = Modifier
                    .clickable {
                        viewModel.unlockApp()
                        onSignOut()
                    }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
