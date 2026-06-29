package com.example.rider.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rider.RiderViewModel
import com.example.rider.navigation.RiderView
import com.example.ui.theme.*
import com.example.ui.components.PremiumGradientButton

@Composable
fun RiderLoginScreen(viewModel: RiderViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
        Column(Modifier.fillMaxSize().background(BrandGradient)) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).background(Color.White.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("RIDER LOGIN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("Engraced Smile Dispatch", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 120.dp)) {
                    Text("Welcome Back", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Sign in to access your deliveries", color = TextGray, fontSize = 13.sp)
                    Spacer(Modifier.height(28.dp))

                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BiroBlue) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = fieldColors(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BiroBlue) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), colors = fieldColors(), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Forgot password?", color = BiroBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Surface(color = DangerRed.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) { Text(it, color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(12.dp).fillMaxWidth()) }
                    }

                    Spacer(Modifier.height(24.dp))
                    PremiumGradientButton(
                        if (isLoading) "Signing in..." else "Sign In",
                        icon = Icons.Default.Login,
                        onClick = { if (email.isNotBlank() && password.isNotBlank()) viewModel.login(email, password) { viewModel.navigateToRoot(RiderView.Dashboard) } },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
    focusedLabelColor = BiroBlue, unfocusedLabelColor = TextGray, cursorColor = BiroBlue
)
