package com.esdispatch.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esdispatch.ui.components.ConfettiEffect
import com.esdispatch.ui.components.ImagePickerField
import com.esdispatch.ui.components.RoundedSheet
import com.esdispatch.ui.components.ScreenHeader
import com.esdispatch.ui.theme.*
import com.esdispatch.viewmodel.DeliveryViewModel
import com.esdispatch.viewmodel.MarketplaceItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VendorPortalScreen(
    viewModel: DeliveryViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    // Live Firestore states
    val userName by viewModel.userName.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val deliveryCount by viewModel.deliveryCount.collectAsState()
    val vendorStoreExists by viewModel.vendorStoreExists.collectAsState()
    val isVendorVerified by viewModel.isVendorVerified.collectAsState()
    val vendorStore by viewModel.vendorStore.collectAsState()
    val marketplaceProducts by viewModel.marketplaceProducts.collectAsState()
    val firebaseUserId by viewModel.firebaseUserId.collectAsState()
    val vendorOrders by viewModel.vendorOrders.collectAsState()

    // Filter products belonging to this vendor
    val myProducts = remember(marketplaceProducts, firebaseUserId) {
        marketplaceProducts.filter { it.vendorId == firebaseUserId }
    }
    val vendorKycSubmitted by viewModel.vendorKycSubmitted.collectAsState()
    val storeName = (vendorStore?.get("storeName") as? String)
        ?: if (userName.isNotBlank()) "${userName}'s Store" else "ESDispatch Partner Store"
    val vendorBalance = (vendorStore?.get("vendorWallet") as? Number)?.toDouble() ?: 0.0
    val totalSales = (vendorStore?.get("totalSales") as? Number)?.toLong() ?: 0L
    val storeRating = (vendorStore?.get("storeRating") as? Number)?.toDouble() ?: 0.0
    val isPendingReview = vendorStore?.get("isPendingReview") as? Boolean ?: false

    // UI state
    var activeTab by remember { mutableIntStateOf(0) }
    var showAddProductSheet by remember { mutableStateOf(false) }
    var showEditProductSheet by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<MarketplaceItem?>(null) }
    var showPayoutSheet by remember { mutableStateOf(false) }
    var showRegisterSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Registration form
    var regStoreName by remember { mutableStateOf("") }
    var regCategory by remember { mutableStateOf("General") }
    var regDescription by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regRegNumber by remember { mutableStateOf("") }
    var regLogoUri by remember { mutableStateOf("") }
    var regCoverUri by remember { mutableStateOf("") }

    // KYC form
    var kycFullName by remember { mutableStateOf(userName) }
    var kycAddress by remember { mutableStateOf("") }
    var kycIdType by remember { mutableStateOf("NIN") }
    var kycIdNumber by remember { mutableStateOf("") }
    var kycBankName by remember { mutableStateOf("") }
    var kycAccountName by remember { mutableStateOf("") }
    var kycAccountNumber by remember { mutableStateOf("") }
    val idTypeOptions = listOf("NIN", "BVN", "Driver's Licence", "Voter's Card", "International Passport")

    // Product form (shared for add & edit)
    var productTitle by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf("Packaging") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productStock by remember { mutableStateOf("10") }
    var productImageUrl by remember { mutableStateOf("") }

    // Payout form
    var payoutAmount by remember { mutableStateOf("") }
    var payoutBank by remember { mutableStateOf("") }
    var payoutAccount by remember { mutableStateOf("") }

    // Category lists
    val storeCategories = listOf("General", "Electronics", "Fashion", "Food & Beverages", "Packaging", "Lubricants", "Safety", "Accessories", "Services")
    val productCategories = listOf("Packaging", "Safety", "Accessories", "Lubricants", "Apparel", "Delivery Gear", "Electronics", "General")

    // Confetti celebration when the store becomes verified/live
    var verifiedCelebration by remember { mutableStateOf(false) }
    LaunchedEffect(isVendorVerified) {
        if (isVendorVerified) verifiedCelebration = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        ConfettiEffect(trigger = verifiedCelebration, onFinished = { verifiedCelebration = false })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HeaderBgColor)
        ) {
            ScreenHeader(
                title = "Vendor Control Hub",
                onBack = { onNavigate("Dashboard") }
            )

            RoundedSheet(modifier = Modifier.weight(1f)) {

                // ── NOT REGISTERED ──────────────────────────────────────────
                if (!vendorStoreExists) {
                    VendorGateContent(
                        deliveryCount = deliveryCount,
                        userRole = userRole,
                        isDark = isDark,
                        onRegister = { showRegisterSheet = true }
                    )
                    return@RoundedSheet
                }

                // ── KYC REQUIRED (unlock to go live) ────────────────────────
                if (!vendorKycSubmitted) {
                    VendorKycContent(
                        isDark = isDark,
                        isLoading = isLoading,
                        fullName = kycFullName, onFullName = { kycFullName = it },
                        address = kycAddress, onAddress = { kycAddress = it },
                        idType = kycIdType, idTypeOptions = idTypeOptions, onIdType = { kycIdType = it },
                        idNumber = kycIdNumber, onIdNumber = { kycIdNumber = it },
                        bankName = kycBankName, onBankName = { kycBankName = it },
                        accountName = kycAccountName, onAccountName = { kycAccountName = it },
                        accountNumber = kycAccountNumber, onAccountNumber = { kycAccountNumber = it },
                        onSubmit = {
                            if (kycFullName.isBlank() || kycAddress.isBlank() ||
                                kycBankName.isBlank() || kycAccountName.isBlank() || kycAccountNumber.isBlank()
                            ) {
                                Toast.makeText(context, "Please fill all KYC fields", Toast.LENGTH_SHORT).show()
                                return@VendorKycContent
                            }
                            isLoading = true
                            viewModel.submitVendorKyc(
                                kycFullName.trim(), kycAddress.trim(), kycIdType, kycIdNumber.trim(),
                                kycBankName.trim(), kycAccountName.trim(), kycAccountNumber.trim()
                            ) { ok, msg ->
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    return@RoundedSheet
                }

                // ── PENDING ADMIN VERIFICATION ──────────────────────────────
                if (!isVendorVerified) {
                    PendingVerificationContent(storeName = storeName, isDark = isDark)
                    return@RoundedSheet
                }

                // ── VERIFIED VENDOR DASHBOARD ────────────────────────────────

                // Status Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Verified, null, tint = Obsidian, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "VERIFIED VENDOR",
                                fontSize = 12.sp, fontWeight = FontWeight.Black, color = AppTextColor
                            )
                            Text(
                                "$storeName  •  Active Partner",
                                fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Analytics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VendorStatCard(
                        label = "Vendor Balance",
                        value = "₦${String.format("%,.0f", vendorBalance)}",
                        valueColor = Gold,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    VendorStatCard(
                        label = "Total Orders",
                        value = "$totalSales",
                        valueColor = AppTextColor,
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                    VendorStatCard(
                        label = "Store Rating",
                        value = if (storeRating > 0) "${String.format("%.1f", storeRating)} ★" else "New",
                        valueColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Products (${myProducts.size})",
                        "Orders (${vendorOrders.size})",
                        "Earnings"
                    ).forEachIndexed { i, label ->
                        Button(
                            onClick = { activeTab = i },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == i) Gold else if (isDark) Charcoal else GoldenWhite,
                                contentColor = if (activeTab == i) Obsidian else AppTextColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab content
                when (activeTab) {
                    0 -> ProductsTab(
                        products = myProducts,
                        isDark = isDark,
                        onAddProduct = {
                            productTitle = ""; productCategory = "Packaging"
                            productDescription = ""; productPrice = ""
                            productStock = "10"; productImageUrl = ""
                            showAddProductSheet = true
                        },
                        onEditProduct = { item ->
                            editTarget = item
                            productTitle = item.title
                            productCategory = item.category
                            productDescription = item.description
                            productPrice = item.price.toString()
                            productStock = item.stock.toString()
                            productImageUrl = item.imageUrl
                            showEditProductSheet = true
                        },
                        onDeleteProduct = { item ->
                            viewModel.deleteVendorProduct(item.id) { ok, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    1 -> OrdersTab(orders = vendorOrders, isDark = isDark)

                    2 -> EarningsTab(
                        balance = vendorBalance,
                        totalSales = totalSales,
                        isDark = isDark,
                        onRequestPayout = { showPayoutSheet = true }
                    )
                }
            }
        }
    }

    // ── STORE REGISTRATION SHEET ──────────────────────────────────────────────
    if (showRegisterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRegisterSheet = false },
            containerColor = if (isDark) Charcoal else GoldenWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Register Your Store",
                    fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTextColor
                )
                Text(
                    "Your store will only go live after you complete your basic KYC verification.",
                    fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(20.dp))

                VendorTextField("Store Name", regStoreName, { regStoreName = it })
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Description", regDescription, { regDescription = it }, minLines = 3)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Category", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                CategoryChips(storeCategories, regCategory) { regCategory = it }

                Spacer(modifier = Modifier.height(14.dp))
                VendorTextField("Phone Number", regPhone, { regPhone = it }, keyboardType = KeyboardType.Phone)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Business Address", regAddress, { regAddress = it }, minLines = 2)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField(
                    "Business Registration No. (optional)",
                    regRegNumber, { regRegNumber = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ImagePickerField(
                        title = "Store Logo",
                        onImagePicked = { regLogoUri = it },
                        modifier = Modifier.weight(1f)
                    )
                    ImagePickerField(
                        title = "Cover Photo",
                        onImagePicked = { regCoverUri = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (regStoreName.isBlank() || regDescription.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        viewModel.registerVendorStore(
                            regStoreName.trim(), regCategory, regDescription.trim(),
                            regPhone.trim(), regAddress.trim(),
                            logoUrl = "", coverUrl = "", businessRegNumber = regRegNumber.trim()
                        ) { ok, msg ->
                            if (!ok) {
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                return@registerVendorStore
                            }
                            val storeUid = com.esdispatch.data.FirebaseManager.auth?.currentUser?.uid
                            if (storeUid == null) {
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                showRegisterSheet = false
                                return@registerVendorStore
                            }
                            fun pushImage(kind: String, uri: String, next: () -> Unit) {
                                if (uri.isBlank()) { next(); return }
                                viewModel.uploadStoreImage(storeUid, kind, uri) { upOk, upMsg ->
                                    if (!upOk) Toast.makeText(context, upMsg, Toast.LENGTH_SHORT).show()
                                    next()
                                }
                            }
                            pushImage("logo", regLogoUri) {
                                pushImage("cover", regCoverUri) {
                                    isLoading = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    showRegisterSheet = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Obsidian, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Storefront, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Store Profile", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // ── ADD PRODUCT SHEET ─────────────────────────────────────────────────────
    if (showAddProductSheet) {
        ProductFormSheet(
            title = "Add Product",
            productTitle = productTitle, onTitleChange = { productTitle = it },
            productCategory = productCategory, onCategoryChange = { productCategory = it },
            productDescription = productDescription, onDescriptionChange = { productDescription = it },
            productPrice = productPrice, onPriceChange = { productPrice = it },
            productStock = productStock, onStockChange = { productStock = it },
            productImageUrl = productImageUrl, onImageUrlChange = { productImageUrl = it },
            categories = productCategories,
            isDark = isDark,
            isLoading = isLoading,
            onDismiss = { showAddProductSheet = false },
            onSubmit = {
                val price = productPrice.toDoubleOrNull()
                val stock = productStock.toIntOrNull() ?: 0
                if (productTitle.isBlank() || price == null || price <= 0) {
                    Toast.makeText(context, "Please enter a valid title and price", Toast.LENGTH_SHORT).show()
                    return@ProductFormSheet
                }
                isLoading = true
                viewModel.addVendorProduct(
                    productTitle.trim(), productCategory, productDescription.trim(),
                    price, stock, productImageUrl.trim()
                ) { ok, msg ->
                    isLoading = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) showAddProductSheet = false
                }
            }
        )
    }

    // ── EDIT PRODUCT SHEET ────────────────────────────────────────────────────
    if (showEditProductSheet && editTarget != null) {
        ProductFormSheet(
            title = "Edit Product",
            productTitle = productTitle, onTitleChange = { productTitle = it },
            productCategory = productCategory, onCategoryChange = { productCategory = it },
            productDescription = productDescription, onDescriptionChange = { productDescription = it },
            productPrice = productPrice, onPriceChange = { productPrice = it },
            productStock = productStock, onStockChange = { productStock = it },
            productImageUrl = productImageUrl, onImageUrlChange = { productImageUrl = it },
            categories = productCategories,
            isDark = isDark,
            isLoading = isLoading,
            onDismiss = { showEditProductSheet = false; editTarget = null },
            onSubmit = {
                val price = productPrice.toDoubleOrNull()
                val stock = productStock.toIntOrNull() ?: 0
                if (productTitle.isBlank() || price == null || price <= 0) {
                    Toast.makeText(context, "Please enter a valid title and price", Toast.LENGTH_SHORT).show()
                    return@ProductFormSheet
                }
                isLoading = true
                viewModel.updateVendorProduct(
                    editTarget!!.id, productTitle.trim(), productCategory,
                    productDescription.trim(), price, stock, productImageUrl.trim()
                ) { ok, msg ->
                    isLoading = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) { showEditProductSheet = false; editTarget = null }
                }
            }
        )
    }

    // ── PAYOUT REQUEST SHEET ──────────────────────────────────────────────────
    if (showPayoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPayoutSheet = false },
            containerColor = if (isDark) Charcoal else GoldenWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Request Payout", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                Text(
                    "Available: ₦${String.format("%,.2f", vendorBalance)}",
                    fontSize = 13.sp, color = Gold, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                VendorTextField("Amount (₦)", payoutAmount, { payoutAmount = it }, keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Bank Name", payoutBank, { payoutBank = it })
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Account Number", payoutAccount, { payoutAccount = it }, keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val amt = payoutAmount.toDoubleOrNull()
                        if (amt == null || amt <= 0 || payoutBank.isBlank() || payoutAccount.isBlank()) {
                            Toast.makeText(context, "Please fill all payout details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        viewModel.requestVendorPayout(amt, payoutBank, payoutAccount) { ok, msg ->
                            isLoading = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (ok) showPayoutSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Obsidian, strokeWidth = 2.dp)
                    else Text("Request Payout", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── SUB-COMPOSABLES ───────────────────────────────────────────────────────────

@Composable
private fun VendorGateContent(
    deliveryCount: Int,
    userRole: String,
    isDark: Boolean,
    onRegister: () -> Unit
) {
    val isPrivilegedVendor = userRole == "vendor" || userRole == "admin" || userRole == "super_admin"
    val meetsDeliveryReq = deliveryCount >= 10 || isPrivilegedVendor
    var triggerConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(meetsDeliveryReq) {
        if (meetsDeliveryReq) {
            triggerConfetti = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ConfettiEffect(trigger = triggerConfetti, onFinished = { triggerConfetti = false })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (meetsDeliveryReq) Icons.Filled.Stars else Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isPrivilegedVendor) "Official Vendor Access" else if (meetsDeliveryReq) "🎉 Vendor Milestone Unlocked!" else "Become a Vendor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = AppTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPrivilegedVendor)
                    "Your account has been provisioned with official vendor access. Open your store profile to begin listing products."
                else if (meetsDeliveryReq)
                    "Congratulations! You completed 10 deliveries and earned your VIP Vendor Qualification Badge. Open your store now."
                else
                    "List your products on the ESDispatch Marketplace and reach thousands of logistics professionals.",
                fontSize = 13.sp,
                color = AppTextColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Qualification Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (meetsDeliveryReq) Gold.copy(alpha = 0.12f) else if (isDark) Charcoal else GoldenWhite
                ),
                border = BorderStroke(1.dp, if (meetsDeliveryReq) Gold else if (isDark) BorderDark else Slate)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Qualification Status", fontSize = 13.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                        Text(
                            if (isPrivilegedVendor) "Authorized Vendor" else "$deliveryCount / 10 Deliveries",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (meetsDeliveryReq) Gold else AppTextColor.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { if (isPrivilegedVendor) 1f else (deliveryCount / 10f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Gold,
                        trackColor = if (isDark) BorderDark else Slate.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    RequirementRow(
                        label = if (isPrivilegedVendor) "Account verified as official Vendor" else "Min. 10 completed deliveries",
                        met = meetsDeliveryReq,
                        detail = if (isPrivilegedVendor) "Enterprise Partner" else if (meetsDeliveryReq) "Unlocked!" else "Need ${10 - deliveryCount} more deliveries"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RequirementRow(
                        label = "Store reviewed & verified by ESDispatch",
                        met = false,
                        detail = "After submission"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RequirementRow(
                        label = "Valid Nigerian bank account",
                        met = false,
                        detail = "For payouts"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRegister,
                enabled = meetsDeliveryReq,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold, contentColor = Obsidian,
                    disabledContainerColor = Gold.copy(alpha = 0.3f), disabledContentColor = Obsidian.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (meetsDeliveryReq) "Register My Store Now" else "Complete ${10 - deliveryCount} More Deliveries to Unlock",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RequirementRow(label: String, met: Boolean, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) Color(0xFF10B981) else AppTextColor.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppTextColor)
            Text(detail, fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun PendingVerificationContent(storeName: String, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape)
                .background(Gold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.HourglassTop, null, tint = Gold, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Under Review", fontSize = 20.sp, fontWeight = FontWeight.Black, color = AppTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "\"$storeName\" KYC has been submitted and is awaiting review by ESDispatch. We'll notify you as soon as your store is verified and live.",
            fontSize = 13.sp, color = AppTextColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VendorKycContent(
    isDark: Boolean,
    isLoading: Boolean,
    fullName: String, onFullName: (String) -> Unit,
    address: String, onAddress: (String) -> Unit,
    idType: String, idTypeOptions: List<String>, onIdType: (String) -> Unit,
    idNumber: String, onIdNumber: (String) -> Unit,
    bankName: String, onBankName: (String) -> Unit,
    accountName: String, onAccountName: (String) -> Unit,
    accountNumber: String, onAccountNumber: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape)
                .background(Gold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.VerifiedUser, null, tint = Gold, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Verify Your Store", fontSize = 20.sp, fontWeight = FontWeight.Black, color = AppTextColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Complete your business profile to protect buyers and unlock your live store. Identity documents are optional.",
            fontSize = 13.sp, color = AppTextColor.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Charcoal else GoldenWhite
            ),
            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Identity (optional)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Business / Legal Name", fullName, onFullName)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Business Address", address, onAddress, minLines = 2)
                Spacer(modifier = Modifier.height(10.dp))
                Text("ID Type (optional)", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                CategoryChips(idTypeOptions, idType, onIdType)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("ID Number (optional)", idNumber, onIdNumber)

                Spacer(modifier = Modifier.height(18.dp))
                Text("Payout Account", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Gold)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Bank Name", bankName, onBankName)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField("Account Name", accountName, onAccountName)
                Spacer(modifier = Modifier.height(10.dp))
                VendorTextField(
                    "Account Number (NUBAN)",
                    accountNumber,
                    onAccountNumber,
                    keyboardType = KeyboardType.Number
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Obsidian, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Badge, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit KYC & Go Live", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your details are only used for verification and payouts.",
            fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun ProductsTab(
    products: List<MarketplaceItem>,
    isDark: Boolean,
    onAddProduct: () -> Unit,
    onEditProduct: (MarketplaceItem) -> Unit,
    onDeleteProduct: (MarketplaceItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Products", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AppTextColor)
            Button(
                onClick = onAddProduct,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Inventory2, null, tint = Gold.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No products yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                    Text("Tap \"Add Product\" to list your first item", fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products, key = { it.id }) { item ->
                    VendorProductCard(
                        item = item,
                        isDark = isDark,
                        onEdit = { onEditProduct(item) },
                        onDelete = { onDeleteProduct(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VendorProductCard(
    item: MarketplaceItem,
    isDark: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal else GoldenWhite),
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(LuxuryBlack.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Icon(Icons.Filled.ShoppingBag, null, tint = Gold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                Text(
                    "${item.category}  •  Stock: ${item.stock}",
                    fontSize = 10.sp, color = AppTextColor.copy(alpha = 0.55f)
                )
                Text(
                    "₦${String.format("%,.0f", item.price)}",
                    fontSize = 13.sp, fontWeight = FontWeight.Black, color = Gold
                )
            }
            // Edit
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, null, tint = AppTextColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
            // Delete
            IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, null, tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Product?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${item.title}\" will be removed from the marketplace.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun OrdersTab(orders: List<Map<String, Any>>, isDark: Boolean) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.ReceiptLong, null, tint = Gold.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No orders yet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                Text("Customer orders for your products appear here", fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.5f))
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(orders) { order ->
                val orderId = order["orderId"] as? String ?: "—"
                val total = (order["totalAmount"] as? Number)?.toDouble() ?: 0.0
                val payout = (order["vendorPayout"] as? Number)?.toDouble() ?: 0.0
                val status = order["status"] as? String ?: "PENDING"
                val buyer = order["userName"] as? String ?: "Customer"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal else GoldenWhite),
                    border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(orderId, fontSize = 12.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                            Text(buyer, fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.6f))
                            Text(
                                "Total ₦${String.format("%,.0f", total)}  •  Your payout ₦${String.format("%,.0f", payout)}",
                                fontSize = 10.sp, color = Gold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (status) {
                                "PAID" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                "DELIVERED" -> Gold.copy(alpha = 0.15f)
                                else -> AppTextColor.copy(alpha = 0.08f)
                            }
                        ) {
                            Text(
                                status, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    "PAID" -> Color(0xFF10B981)
                                    "DELIVERED" -> Gold
                                    else -> AppTextColor
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsTab(
    balance: Double,
    totalSales: Long,
    isDark: Boolean,
    onRequestPayout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Available to Withdraw", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("₦${String.format("%,.2f", balance)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Gold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$totalSales total orders processed", fontSize = 11.sp, color = AppTextColor.copy(alpha = 0.5f))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal else GoldenWhite),
            border = BorderStroke(1.dp, if (isDark) BorderDark else Slate)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Commission Structure", fontSize = 13.sp, fontWeight = FontWeight.Black, color = AppTextColor)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Platform commission", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.7f))
                    Text("8.5%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vendor payout", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.7f))
                    Text("91.5%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payout processing", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.7f))
                    Text("2–3 business days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTextColor)
                }
            }
        }

        Button(
            onClick = onRequestPayout,
            enabled = balance > 0,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold, contentColor = Obsidian,
                disabledContainerColor = Gold.copy(alpha = 0.3f)
            )
        ) {
            Icon(Icons.Filled.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (balance > 0) "Request Payout" else "No funds available",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VendorStatCard(
    label: String, value: String, valueColor: Color,
    modifier: Modifier, isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Charcoal else GoldenWhite),
        border = BorderStroke(1.dp, if (isDark) BorderDark else Slate),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 9.sp, color = AppTextColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = valueColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormSheet(
    title: String,
    productTitle: String, onTitleChange: (String) -> Unit,
    productCategory: String, onCategoryChange: (String) -> Unit,
    productDescription: String, onDescriptionChange: (String) -> Unit,
    productPrice: String, onPriceChange: (String) -> Unit,
    productStock: String, onStockChange: (String) -> Unit,
    productImageUrl: String, onImageUrlChange: (String) -> Unit,
    categories: List<String>,
    isDark: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Charcoal else GoldenWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTextColor)
            Spacer(modifier = Modifier.height(16.dp))

            VendorTextField("Product Title", productTitle, onTitleChange)
            Spacer(modifier = Modifier.height(10.dp))
            VendorTextField("Description", productDescription, onDescriptionChange, minLines = 2)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VendorTextField("Price (₦)", productPrice, onPriceChange,
                    modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                VendorTextField("Stock Qty", productStock, onStockChange,
                    modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }
            Spacer(modifier = Modifier.height(10.dp))
            VendorTextField("Image URL (optional)", productImageUrl, onImageUrlChange)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Category", fontSize = 12.sp, color = AppTextColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            CategoryChips(categories, productCategory, onCategoryChange)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Obsidian),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Obsidian, strokeWidth = 2.dp)
                else Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VendorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        minLines = minLines,
        maxLines = if (minLines > 1) 4 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = AppTextColor.copy(alpha = 0.3f),
            focusedLabelColor = Gold
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            FilterChip(
                selected = cat == selected,
                onClick = { onSelect(cat) },
                label = { Text(cat, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold,
                    selectedLabelColor = Obsidian,
                    containerColor = AppTextColor.copy(alpha = 0.08f),
                    labelColor = AppTextColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = cat == selected,
                    selectedBorderColor = Color.Transparent,
                    borderColor = AppTextColor.copy(alpha = 0.2f)
                )
            )
        }
    }
}
