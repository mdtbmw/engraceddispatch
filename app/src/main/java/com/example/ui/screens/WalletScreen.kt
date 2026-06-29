package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.FirebaseService
import com.example.data.models.Transaction
import com.example.data.models.TransactionType
import com.example.ui.DeliveryViewModel
import com.example.ui.navigation.AppView
import com.example.ui.theme.*
import com.example.ui.components.SuccessBottomSheet
import com.example.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: DeliveryViewModel) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val cardNumber by viewModel.preferences.walletCardNumber.collectAsState(initial = "8241")
    val cardExpiry by viewModel.preferences.walletCardExpiry.collectAsState(initial = "12/29")
    var showFundSheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showPaystackSheet by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var paystackUrl by remember { mutableStateOf("") }
    var paystackRef by remember { mutableStateOf("") }
    var verifyingPayment by remember { mutableStateOf(false) }

    var initialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(700)
        initialLoading = false
    }

    ScreenScaffold(title = "DISPATCH WALLET", onBack = { viewModel.navigateBack() }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .background(BrandGradient, RoundedCornerShape(24.dp))
            ) {
                Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text("ES DISPATCH WALLET", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(Modifier.height(2.dp)); Text(user.fullName.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(22.dp))
                    }
                    Column { Text("AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp); Text("\u20A6${String.format("%,.2f", walletBalance)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("**** **** **** $cardNumber", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, letterSpacing = 1.5.sp); Text(cardExpiry, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp) }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { PremiumGradientButton("Fund Wallet", icon = Icons.Default.AddCard, onClick = { showFundSheet = true }, modifier = Modifier.weight(1f)); OutlinedGradientButton("Withdraw", icon = Icons.Default.CreditCard, onClick = { showWithdrawSheet = true }, modifier = Modifier.weight(1f)) }
            Spacer(Modifier.height(28.dp))
            Text("Recent Transactions", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            if (initialLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { ShimmerBox(height = 72.dp, corners = 16.dp) }
                }
            } else if (transactions.isEmpty()) EmptyState(Icons.Default.Receipt, "No transactions yet")
            else transactions.forEachIndexed { index, tx ->
                StaggeredItem(index) { TransactionCard(tx) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFundSheet) {
        FundWithdrawBottomSheet(
            title = "Fund Wallet",
            isWithdraw = false,
            maxAmount = 0.0,
            onConfirm = { amount ->
                showFundSheet = false
                viewModel.initPaystackPayment(amount) { result ->
                    if (result != null) {
                        paystackUrl = result.authorizationUrl
                        paystackRef = result.reference
                        showPaystackSheet = true
                    }
                }
            },
            onDismiss = { showFundSheet = false }
        )
    }

    if (showPaystackSheet && paystackUrl.isNotEmpty()) {
        PaystackCheckoutSheet(
            url = paystackUrl,
            isLoading = verifyingPayment,
            onDismiss = {
                showPaystackSheet = false
                paystackUrl = ""
                paystackRef = ""
            },
            onPaymentComplete = { reference ->
                showPaystackSheet = false
                verifyingPayment = true
                viewModel.verifyPaystackPayment(reference) { success ->
                    verifyingPayment = false
                    if (success) {
                        successMessage = "Wallet funded successfully via Paystack"
                        showSuccessSheet = true
                    }
                }
            }
        )
    }

    if (showWithdrawSheet) {
        FundWithdrawBottomSheet(
            title = "Withdraw Funds",
            isWithdraw = true,
            maxAmount = walletBalance,
            onConfirm = { amount ->
                val success = viewModel.withdrawFunds(amount)
                showWithdrawSheet = false
                if (success) {
                    successMessage = "\u20A6${String.format("%,.2f", amount)} has been withdrawn from your wallet."
                    showSuccessSheet = true
                }
            },
            onDismiss = { showWithdrawSheet = false }
        )
    }

    if (showSuccessSheet) {
        SuccessBottomSheet(message = successMessage) { showSuccessSheet = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaystackCheckoutSheet(
    url: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPaymentComplete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var paymentHandled by remember { mutableStateOf(false) }
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().height(500.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Paystack Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BiroBlue)
                } else {
                    IconButton(onClick = dismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray) }
                }
            }
            AndroidView(
                factory = { ctx ->
                    @SuppressLint("SetJavaScriptEnabled")
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                if (url.contains("callback") && !paymentHandled) {
                                    paymentHandled = true
                                    val ref = extractReference(url)
                                    if (ref != null) onPaymentComplete(ref)
                                    return true
                                }
                                return false
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun extractReference(url: String): String? {
    val refMatch = Regex("[?&]reference=([^&]+)").find(url)
    refMatch?.let { return it.groupValues[1] }
    val trxrefMatch = Regex("[?&]trxref=([^&]+)").find(url)
    trxrefMatch?.let { return it.groupValues[1] }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FundWithdrawBottomSheet(title: String, isWithdraw: Boolean, maxAmount: Double, onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            Spacer(Modifier.height(8.dp))
            Text("Enter amount in NGN", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; error = null },
                placeholder = { Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextMain, unfocusedTextColor = TextMain,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = BiroBlue, unfocusedBorderColor = CardBorderGray,
                    cursorColor = BiroBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            if (isWithdraw && maxAmount > 0) {
                Spacer(Modifier.height(8.dp))
                Text("Available: \u20A6${String.format("%,.2f", maxAmount)}", color = TextGray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(24.dp))
            PremiumGradientButton(
                text = if (isWithdraw) "Withdraw" else "Fund Wallet",
                onClick = {
                    val a = amount.toDoubleOrNull()
                    if (a == null || a <= 0) error = "Enter a valid amount"
                    else if (isWithdraw && a > maxAmount) error = "Insufficient balance"
                    else onConfirm(a)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = dismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), border = BorderStroke(1.dp, CardBorderGray)) {
                Text("Cancel", color = TextGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: Transaction) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorderGray), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(if (tx.type == TransactionType.CREDIT) SuccessGreen.copy(alpha = 0.12f) else BiroBlue.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(if (tx.type == TransactionType.CREDIT) Icons.Default.AddCard else Icons.Default.CreditCard, contentDescription = null, tint = if (tx.type == TransactionType.CREDIT) SuccessGreen else BiroBlue, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column { Text(tx.title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(tx.description, color = TextGray, fontSize = 10.sp) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${if (tx.type == TransactionType.CREDIT) "+" else "-"}\u20A6${String.format("%,.0f", tx.amount)}", color = if (tx.type == TransactionType.CREDIT) SuccessGreen else TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text(SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(tx.createdAt)), color = TextGray, fontSize = 9.sp)
            }
        }
    }
}
