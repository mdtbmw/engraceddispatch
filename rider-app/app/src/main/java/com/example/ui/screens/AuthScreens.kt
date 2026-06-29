package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(viewModel: DeliveryViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val googleSignInClient: GoogleSignInClient = remember {
        GoogleSignIn.getClient(context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.example.R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModel.loginWithGoogle(idToken) { viewModel.navigateTo(AppView.Dashboard) }
            }
        } catch (e: Exception) {
            viewModel.clearError()
        }
    }

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            Box(Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(AppView.Splash) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    Text("PREMIUM ACCESS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Box(Modifier.size(48.dp))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.size(64.dp).background(BiroBlue.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BiroBlue, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(20.dp))
                    Text("PREMIUM RIDER LOGIN", color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                    Text("Engraced Fleet Access", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    Text("Enter your rider credentials", color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp, bottom = 32.dp))
                    error?.let { Text(it, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                    LoginField(value = email, onValueChange = { email = it; emailError = false }, label = "Email Address", icon = Icons.Default.Email, isError = emailError)
                    Spacer(Modifier.height(14.dp))
                    LoginField(value = password, onValueChange = { password = it; passwordError = false }, label = "Password", icon = Icons.Default.Lock, isError = passwordError, isPassword = true)
                    Spacer(Modifier.height(24.dp))
                    PremiumGradientButton(text = if (isLoading) "Signing in..." else "Login to Dispatch", onClick = {
                        emailError = email.isBlank(); passwordError = password.isBlank()
                        if (!emailError && !passwordError) viewModel.login(email, password) { viewModel.navigateTo(AppView.Dashboard) }
                    }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
                    Spacer(Modifier.height(14.dp))
                    OutlinedGradientButton(text = "Sign in with Google", icon = Icons.Default.AccountCircle, onClick = {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun LoginField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isError: Boolean = false, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = BiroBlue) },
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextMain, unfocusedTextColor = TextMain, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray, focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
