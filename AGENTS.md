# Engraced Smile Dispatch — Full System Blueprint

This document is the **single source of truth** for the Engraced Smile Dispatch Android application. It captures every screen, every component, every data model, every API endpoint, and every styling rule in the current prototype. 

## CARDINAL RULE — Zero Mocks, All Real

1. **Everything must be connected to real backend data.** No hardcoded arrays, no fake data, no random generation, no local-only state. If a screen shows data, that data MUST come from the API.
2. **Every "Coming Soon" placeholder MUST be replaced with a real implementation** that connects to the appropriate backend endpoint.
3. **Offline fallback is acceptable** (using Room DB + DataStore as local cache when the server is unreachable), but the primary data path is the API.
4. **Any AI agent modifying this codebase must NEVER remove existing screens, features, or UI elements.** You may only ADD to the existing structure, ENHANCE its functionality, or COMPLETE placeholder/mock implementations with real connections.
5. **If you find a mock/simulation, replace it with a real API call.** Throw away the fake data, keep the UI structure.
6. **The backend API base URL is `https://api.engraceddispatch.com/v1/`** (defined in `ApiClient.kt`). All API calls go through Retrofit + OkHttp via `ApiService`.

---

## 1. Project Structure

```
com.example/
├── MainActivity.kt               # Entry point: BackHandler, nav bar, theme, AnimatedContent router
├── data/
│   ├── Delivery.kt                # Room @Entity for deliveries table
│   ├── DeliveryDao.kt             # Room DAO with CRUD + tracking queries
│   ├── DeliveryRepository.kt      # Wraps DeliveryDao
│   ├── AppDatabase.kt             # Room database singleton
│   ├── api/
│   │   ├── ApiClient.kt           # Retrofit + OkHttp singleton (BASE_URL, interceptors)
│   │   ├── ApiService.kt          # Retrofit interface — ALL API endpoints
│   │   └── NetworkResult.kt       # sealed class: Success<T>, Error, Loading
│   ├── models/
│   │   ├── User.kt                # User data class + LoginRequest, SignUpRequest, AuthResponse
│   │   ├── Transaction.kt         # Transaction data class (CREDIT/DEBIT, PENDING/COMPLETED/FAILED)
│   │   └── WalletResponse.kt      # Wallet API response (balance, currency, userId, updatedAt)
│   ├── preferences/
│   │   └── UserPreferences.kt     # DataStore — persists auth, user profile, settings, wallet
│   └── repository/
│       └── AuthRepository.kt      # Wraps auth API calls (login, register)
├── ui/
│   ├── DeliveryViewModel.kt       # Single ViewModel for the entire app — auth, deliveries, wallet, navigation
│   ├── navigation/
│   │   └── AppNavigation.kt       # sealed class AppView — all screen destinations
│   ├── screens/
│   │   ├── SplashScreen.kt        # 2-phase: premium splash → onboarding pager → login
│   │   ├── AuthScreens.kt         # LoginScreen + SignUpScreen
│   │   ├── DashboardScreen.kt     # Collapsing header, promos, services, deliveries, stats
│   │   ├── BookingFormScreen.kt   # New delivery booking form with wallet check
│   │   ├── TrackingScreen.kt      # Active delivery tracking + timeline + OTP verification
│   │   ├── OrderLogsScreen.kt     # All deliveries list with search + status badges
│   │   ├── WalletScreen.kt        # Wallet card, fund/withdraw sheets, transaction list
│   │   ├── ProfileScreen.kt       # User profile, edit, payment methods, help, about sheets
│   │   └── SettingsScreen.kt      # Full settings with 8 BottomSheet drawers
│   ├── components/
│   │   └── CommonComponents.kt    # Shared composables: PremiumGradientButton, ScreenHeader, etc.
│   └── theme/
│       └── Color.kt               # All color tokens and BrandGradient
```

---

## 2. Navigation — AppView Sealed Class

Defined in `AppNavigation.kt`. Every screen destination:

| View | Type | Description |
|---|---|---|
| `Splash` | `data object` | Splash screen + onboarding pager |
| `Login` | `data object` | Login screen |
| `SignUp` | `data object` | Registration screen |
| `Dashboard` | `data object` | Main dashboard (home) |
| `BookingDetails` | `data object` | New delivery booking form |
| `ActiveTracking` | `data class(trackingNumber: String)` | Live tracking for a delivery |
| `OrderLogs` | `data object` | Delivery history list |
| `Wallet` | `data object` | Wallet balance + transactions |
| `Profile` | `data object` | User profile |
| `Settings` | `data object` | App settings |

**Navigation rules:**
- `navigateTo(view)` — pushes current view onto `_navigationHistory` stack, navigates to `view`
- `navigateBack()` — pops last view from stack, or goes to Dashboard if stack is empty
- `navigateToRoot(view)` — clears stack, navigates to `view` (used for logout, post-login redirect)
- Bottom nav bar is visible on: Dashboard, OrderLogs, Wallet, Profile
- Back gesture (Android system back) calls `navigateBack()` — never exits the app
- All transitions use `FastOutSlowInEasing` with `slideInHorizontally`/`slideOutHorizontally`

**Bottom Navigation Bar (Overlay Notched Dock):**
- 4 nav items: Home (Dashboard), Deliveries (OrderLogs), Wallet, Profile
- Center FAB (Scan button) triggers `BookingDetails`
- `NotchedNavShape(38.dp)` — custom `Shape` with a semi-circular notch cutout in the top center
- FAB floats above the notch at `y = (-28).dp` offset
- Each nav item clips click ripple to `RoundedCornerShape(16.dp)` before `.clickable`

---

## 3. Every Screen — Complete Specification

### 3.1 SplashScreen (`SplashScreen.kt`)
**File:** `ui/screens/SplashScreen.kt`

**Phases:**
1. **Phase 1 (Splash)**: LuxuryBlack background, animated ambient orbs pulsing on Canvas, 120dp circular logo with spring-bounce scale animation (`spring(dampingRatio=0.4f, stiffness=Spring.StiffnessLow)`), "ENGRACED" (32sp Black, letterSpacing 6sp) + "SMILE DISPATCH" (14sp Bold, BiroBlue), pulsing dots row. Duration 2200ms.
2. **Phase 2 (Onboarding Pager)**: Crossfade transition (alpha 0→1). 3 pages:
   - Page 0: "Swift Delivery" — TwoWheeler icon, features: Same-day delivery, Real-time pricing, Multiple vehicle types
   - Page 1: "Live Tracking" — NearMe icon, features: GPS live map, Rider location, Delivery timeline
   - Page 2: "Secure Pay" — AccountBalanceWallet icon, features: Wallet & cards, Bank transfer, Digital receipts
   - Each page has an animated organic `BlobShape` with pulsing geometry and a gradient fill
   - Page indicators (animated dot width 28dp active / 8dp inactive)
   - "Skip" TextButton + circular gradient "Next/Get Started" Button

**Auto-login:** If `isLoggedIn` is true in preferences, navigate directly to Dashboard after 600ms.

**Real data needed:** None (splash/onboarding is UI-only).

### 3.2 Auth — LoginScreen + SignUpScreen (`AuthScreens.kt`)
**File:** `ui/screens/AuthScreens.kt`

Both screens use the standard Rounded-Sheet layout (Box(LuxuryBlack) > Column(BrandGradient) > Gradient header > Card(topRounded=40dp) > scrollable content).

**LoginScreen:**
- Email + Password fields with validation (blank check)
- "Login to Dispatch" PremiumGradientButton → calls `viewModel.login(email, password)` → navigates to Dashboard
- "Login with Biometrics" OutlinedGradientButton → fills blank fields with defaults, calls login
- "Don't have an account? Sign Up" clickable text link
- Error message display from `viewModel.error`

**SignUpScreen:**
- Full Name, Email, Phone, Password fields with validation
- "Create Account" button → calls `viewModel.register(name, email, phone, password)` → navigates to Dashboard
- "Already have an account? Login" clickable text link

**Backend endpoints:**
- `POST /v1/auth/login` — body: `LoginRequest(email, password)` → response: `AuthResponse(token, user, message)`
- `POST /v1/auth/register` — body: `SignUpRequest(fullName, email, phone, password)` → response: `AuthResponse(token, user, message)`
- `POST /v1/auth/refresh` — body: `RefreshTokenRequest(token)` → response: `AuthResponse`

**Offline fallback:** If API is unreachable, create a local token, persist user data to DataStore, and navigate to Dashboard.

### 3.3 DashboardScreen (`DashboardScreen.kt`)
**File:** `ui/screens/DashboardScreen.kt`

**Collapsing Header:**
- `Box` with `BrandGradient` background, bottom rounded corners `40.dp`
- Height animates from `220.dp` (expanded) to `115.dp` (collapsed) via `derivedStateOf` from `LazyColumn` scroll offset
- Pull-to-refresh via `NestedScrollConnection` — overscroll shows "Pull to refresh" → "Release to refresh"
- **Expanded state** (progress <= 0.6): 40dp profile photo (clickable → Profile), greeting + display name, info row (active count + wallet balance), search bar (clickable to activate search, fades with scroll)
- **Collapsed state** (progress > 0.6): 36dp profile photo, greeting + display name on one line, notification bell icon (clickable → Profile)
- Profile photo uses `AsyncImage` with fallback to `Icons.Default.Person` (when `photoUrl` is empty)

**LazyColumn content (with `top = 236.dp` padding):**
1. **Search mode** — `OutlinedTextField` filtering deliveries via `viewModel.searchDeliveries(query)`, search results display with status badges
2. **HeroCard** — "Move Anything, Anywhere" title, description, "+ New Delivery" PremiumGradientButton → BookingDetails
3. **ServicesRow** — 4 service cards in a Row: Express (Bolt), Economy (LocalMall), Batch (Inventory2), Multi (AltRoute). Each card clickable → sets delivery type → navigates to BookingDetails
4. **GradientPromos** — HorizontalPager with 3 promo cards (Express 15% Off, Same-Day Free, Referral Bonus £2000), animated scale/alpha page transitions, pill dot indicators
5. **ActiveDeliveryCard** — Shows current active delivery (PENDING/ASSIGNED/PICKED_UP/OUT_FOR_DELIVERY) with tracking number, ETA, pickup/delivery addresses, rider info, call/chat buttons. Clickable → ActiveTracking. Shows "No Active Shipments" if none.
6. **StatsGrid** — 4 stat tiles: Active count, Completed count, Wallet balance (clickable → Wallet), Rating
7. **ReferralCard** — BrandGradient background, "Refer & Earn" with gift icon
8. **RecentDeliveriesSection** — Last 3 deliveries with tracking number, route, status badge. Clickable → ActiveTracking.

**Real data needed from backend:**
- `GET /v1/deliveries` — list of all deliveries (with auth token)
- `GET /v1/wallet/balance` — current wallet balance
- `GET /v1/users/me` — current user profile (name, photoUrl, etc.)

### 3.4 BookingFormScreen (`BookingFormScreen.kt`)
**File:** `ui/screens/BookingFormScreen.kt`

**Layout:** Custom header (not using ScreenHeader — has its own back button + type-specific title) + standard RoundedSheet.

**Content:**
- Error card (animated, dismissable) — shown when `viewModel.error` is non-null
- Wallet balance card — shows balance, total cost for selected type, "Sufficient"/"Insufficient" badge
- Schedule date row — 5 chip-style cards: Immediate, Today, Tomorrow, Next 3 Days, Weekend (clickable, selected gets BiroBlue background)
- "Routing Addresses" section — Pickup Location + Delivery Destination fields with icons
- "Package Information" — Item name field + Weight (KG) numeric field
- PriceBreakdown card — Base Price, Peak Surcharge (if Express), Priority Rider fee (FREE), Transit Safety Cover (£0), total estimate
- "Confirm Booking" PremiumGradientButton — validates form, checks wallet, calls `viewModel.createBooking(...)` → navigates to ActiveTracking

**Real data needed:**
- `POST /v1/deliveries` — create a new delivery (with auth token)
- Pricing should come from the backend, not hardcoded `getBasePrice()`/`getSurgeAmount()`

### 3.5 ActiveTrackingScreen (`TrackingScreen.kt`)
**File:** `ui/screens/TrackingScreen.kt`

**Loading state:** Shows CircularProgressIndicator + "Loading delivery..." + "Back to Home" button

**Loaded content** (within RoundedSheet):
1. **RiderInfoCard** — Rider photo/icon, rider name, motorcycle number, star rating, ETA timer
2. **StatusTimelineCard** — 5-step timeline: Order Placed, Rider Dispatched, Package Picked Up, Out for Delivery, Delivered. Each step has colored circle (green=completed, blue=active, gray=pending) with connecting line
3. **DeliveryDetailsCard** — Pickup address (blue dot), Destination (orange dot), Item, Type, Amount, Schedule
4. **OtpVerificationCard** — Shows OTP code, status badge (VERIFIED/PENDING), "Verify Delivery" button → opens OTP BottomSheet
5. **PhotoProofCard** — Delivery proof photo + verified handover text
6. **Action buttons:** "Advance Status" (moves to next status for demo), "Verify & Complete" (for delivered not yet verified), "Back to Dashboard" (for complete)

**OTP Verification Sheet (ModalBottomSheet):**
- Shows the OTP code prominently
- 4-digit input field with validation
- "Verify OTP" button → calls `viewModel.verifyOtp()` → shows SuccessBottomSheet

**Real data needed:**
- `GET /v1/deliveries/{tracking}` — get single delivery by tracking number
- `PUT /v1/deliveries/{tracking}/status` — advance status
- `POST /v1/deliveries/{tracking}/verify` — verify OTP

### 3.6 OrderLogsScreen (`OrderLogsScreen.kt`)
**File:** `ui/screens/OrderLogsScreen.kt`

Uses `ScreenScaffold` with count badge in right header.

**Content:**
- Search bar filtering by tracking number, item name, delivery type
- `LazyColumn` of `DeliveryLogCard` items:
  - Colored icon (Express=BiroBlue TwoWheeler, Economy=SuccessGreen Share, others=orange)
  - Tracking number (bold), schedule text
  - Status badge (colored pill: PENDING=amber, ASSIGNED=blue, PICKED_UP=indigo, DELIVERED=green, CANCELLED=red)
  - Route: "Pickup → Destination"
  - Item name + weight
  - Amount (BiroBlue, bold)
- EmptyState when no matching deliveries

### 3.7 WalletScreen (`WalletScreen.kt`)
**File:** `ui/screens/WalletScreen.kt`

**Header:** "DISPATCH WALLET" via `ScreenScaffold`

**Content:**
- Virtual card UI with gradient background, shadow elevation 8dp:
  - "ES DISPATCH WALLET" label
  - User full name (uppercase)
  - Available balance (large 28sp ExtraBold, Naira formatted)
  - Masked card number **** **** **** 8241 + expiry 12/29
- Action buttons row: "Fund Wallet" + "Withdraw" (both PremiumGradientButton style)
- "Recent Transactions" section — list of TransactionCard items showing:
  - Icon (green AddCard for CREDIT, blue CreditCard for DEBIT)
  - Title + description
  - Amount with +/- prefix (green for credit, dark for debit)
  - Date formatted "d MMM, h:mm a"

**BottomSheets:**
- **FundWithdrawBottomSheet** — shared for both fund/withdraw:
  - Title, NGN label, large amount input field
  - Available balance display (for withdraw)
  - Validation: must be positive, must not exceed balance (for withdraw)
  - "Fund Wallet" / "Withdraw" button
  - "Cancel" OutlinedButton
- **SuccessBottomSheet** — checkmark icon, "Success", message, "Done" button

**Real data needed:**
- `POST /v1/wallet/fund` — body: `FundRequest(amount)` → `WalletResponse`
- `POST /v1/wallet/withdraw` — body: `FundRequest(amount)` → `WalletResponse`
- `GET /v1/wallet/balance` → `WalletResponse`
- `GET /v1/wallet/transactions` → `List<Transaction>`

### 3.8 ProfileScreen (`ProfileScreen.kt`)
**File:** `ui/screens/ProfileScreen.kt`

**Header:** "MY PROFILE" via `ScreenHeader`

**Content:**
- Profile photo (100dp circle with 3dp BiroBlue border, AsyncImage or Person icon fallback)
- Full name (22sp ExtraBold), email, phone
- **Badge:** `"ENGRACED VERIFIED MEMBER"` — green verified badge pill (NOT "DISPATCHER")
- Stats row: Deliveries count, Rating (star), Earned (Naira formatted), Member since
- Menu rows with rounded cards, BiroBlue icon circles, arrow forward icon:
  - **Edit Profile** → opens `ProfileEditSheet` (full name, email, phone fields + Save/Cancel)
  - **Payment Methods** → opens `PaymentMethodsSheet` (Visa ending in 8241, GTBank Savings, Add New button)
  - **App Settings** → navigates to Settings screen
  - **Help & Support** → opens `HelpSupportSheet` (Call Support, Live Chat, Email Support, FAQ)
  - **About Engraced Smile** → opens `AboutProfileSheet` (logo, version, build, platform, package)
- "Logout" PremiumGradientButton → calls `viewModel.logout()` → navigates to Splash

**All BottomSheets use:** `ModalBottomSheet` with `skipPartiallyExpanded = true`, `containerColor = Color.White`, `shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`

**Real data needed:**
- `PUT /v1/profile` — update user name, email, phone
- `GET /v1/users/me` — current user details
- `GET /v1/wallet/payment-methods` — linked payment methods

### 3.9 SettingsScreen (`SettingsScreen.kt`)
**File:** `ui/screens/SettingsScreen.kt`

**Header:** "SETTINGS" via `ScreenHeader`

**Content** (4 sections):

**Account:**
- Edit Profile → navigates to Profile screen
- Change Password → `ChangePasswordSheet` (current password, new password, confirm, validation)
- Security → `SecuritySheet` (Two-Factor Auth "Active", Active Sessions, Login History, Login Locations)

**Preferences:**
- Push Notifications — ToggleRow (Switch)
- Dark Mode — ToggleRow (Switch)
- Biometric Login — ToggleRow (Switch)
- Language → `LanguageSheet` (English, French, Yoruba, Hausa, Igbo with radio buttons)

**Payment & Delivery:**
- Payment Methods → `SettingsPaymentSheet` (Visa card, Add New button)
- Default Addresses → `DefaultAddressSheet` (Home + Work address fields, Save button)
- Preferred Riders → `PreferredRidersSheet` (Sani Ibrahim 4.9, Chukwuemeka Obi 4.8, Tunde Bakare 4.7 — radio selection)
- Delivery History → navigates to OrderLogs

**Support:**
- Contact Support → opens dialer `tel:+2348001234567`
- Live Chat → `LiveChatSheet` (agent greeting, message input, Send button)
- Terms of Service → opens URL `https://engraceddispatch.com/terms`
- Privacy Policy → opens URL `https://engraceddispatch.com/privacy`
- About & Version → `AboutSheet` (version, build, platform, min SDK, package)

**Account Management:**
- "Logout of Account" danger-themed OutlinedButton → calls `viewModel.logout()`

**All menu rows** have `.clip(RoundedCornerShape(16.dp)).clickable(...)` to prevent sharp-edged ripple shadows.

**Real data needed:**
- `POST /v1/auth/change-password` — change password
- `PUT /v1/users/preferences` — save notification/biometric/language prefs to server
- `GET /v1/users/addresses` — saved default addresses
- `PUT /v1/users/addresses` — update default addresses
- `POST /v1/auth/logout` — server-side logout

---

## 4. Data Models — Complete Reference

### User (`data/models/User.kt`)
```kotlin
data class User(
    val id: Long = 0,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val isVerified: Boolean = true,
    val rating: Float = 5.0f,
    val totalDeliveries: Int = 0,
    val totalEarned: Double = 0.0,
    val memberSince: String = ""  // "MMM yyyy" format
)
data class LoginRequest(val email: String, val password: String)
data class SignUpRequest(val fullName: String, val email: String, val phone: String, val password: String)
data class AuthResponse(val token: String, val user: User, val message: String = "Success")
```

### Delivery (`data/Delivery.kt`) — Room @Entity
```kotlin
@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackingNumber: String,              // e.g. "ESD-EXP-8241"
    val deliveryType: String,                // "Express", "Economy", "Batch", "Multi-Pickup"
    val status: String,                      // "PENDING", "ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    val totalAmount: Double,
    val scheduledAt: String,                 // "Immediate" or "Today, 2:30 PM"
    val createdAt: Long = System.currentTimeMillis(),
    val pickupAddress: String,
    val deliveryAddress: String,
    val itemName: String,
    val itemWeight: Double,
    val otpCode: String,                     // 4-digit string
    val otpVerified: Boolean = false,
    val photoProofUri: String? = null,       // URL from backend
    val riderName: String,
    val riderBikeNumber: String,
    val riderRating: Float = 4.8f,
    val etaMinutes: Int = 15
)
```

### Transaction (`data/models/Transaction.kt`)
```kotlin
data class Transaction(
    val id: Long = 0,
    val title: String,
    val description: String,
    val amount: Double,
    val type: TransactionType,      // CREDIT or DEBIT
    val createdAt: Long = System.currentTimeMillis(),
    val reference: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED  // PENDING, COMPLETED, FAILED
)
enum class TransactionType { CREDIT, DEBIT }
enum class TransactionStatus { PENDING, COMPLETED, FAILED }
```

### WalletResponse (`data/models/WalletResponse.kt`)
```kotlin
data class WalletResponse(
    val balance: Double = 0.0,
    val currency: String = "NGN",
    val userId: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### NetworkResult (`data/api/NetworkResult.kt`)
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int = -1) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
```

---

## 5. API Endpoints — Complete Backend Specification

Base URL: `https://api.engraceddispatch.com/v1/`

### Auth
| Method | Path | Request Body | Response | Notes |
|---|---|---|---|---|
| POST | `/auth/login` | `LoginRequest` | `AuthResponse` | Returns JWT token + user object |
| POST | `/auth/register` | `SignUpRequest` | `AuthResponse` | Creates account + returns token |
| POST | `/auth/refresh` | `RefreshTokenRequest` | `AuthResponse` | Refresh expired JWT |
| POST | `/auth/logout` | — (header: token) | `{ success: true }` | Invalidate token server-side |

### User/Profile
| Method | Path | Request Body | Response | Notes |
|---|---|---|---|---|
| GET | `/users/me` | — | `User` | Current user profile |
| PUT | `/profile` | `User` | `User` | Update name, email, phone, photo |
| PUT | `/users/preferences` | `Preferences` | `Preferences` | notification/biometric/language settings |
| GET | `/users/addresses` | — | `List<Address>` | Saved default addresses |
| PUT | `/users/addresses` | `Address` | `Address` | Update default addresses |
| POST | `/auth/change-password` | `ChangePasswordRequest` | `{ success: true }` | Current + new password |

### Deliveries
| Method | Path | Request Body | Response | Notes |
|---|---|---|---|---|
| GET | `/deliveries` | — | `List<Delivery>` | All user's deliveries |
| POST | `/deliveries` | `Delivery` | `Delivery` | Create new delivery (deducts wallet) |
| GET | `/deliveries/{tracking}` | — | `Delivery` | Get single delivery by tracking |
| PUT | `/deliveries/{tracking}/status` | `{ status: String }` | `Delivery` | Advance delivery status |
| POST | `/deliveries/{tracking}/verify` | `{ otp: String }` | `Delivery` | Verify OTP to complete delivery |
| DELETE | `/deliveries/{id}` | — | `{ success: true }` | Cancel/delete a delivery |

### Wallet
| Method | Path | Request Body | Response | Notes |
|---|---|---|---|---|
| GET | `/wallet/balance` | — | `WalletResponse` | Current balance |
| POST | `/wallet/fund` | `FundRequest(amount)` | `WalletResponse` | Add funds |
| POST | `/wallet/withdraw` | `FundRequest(amount)` | `WalletResponse` | Withdraw funds |
| GET | `/wallet/transactions` | — | `List<Transaction>` | Transaction history |
| GET | `/wallet/payment-methods` | — | `List<PaymentMethod>` | Saved cards/banks |

### Request/Response Models (add to codebase as needed)
```kotlin
data class Address(val id: Long = 0, val label: String, val address: String, val isDefault: Boolean = false)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class Preferences(val notificationsEnabled: Boolean, val biometricEnabled: Boolean, val language: String)
data class PaymentMethod(val id: Long, val type: String, val last4: String, val expiry: String?, val isDefault: Boolean)
data class StatusUpdateRequest(val status: String)
data class OtpVerifyRequest(val otp: String)
```

---

## 6. Visual Theme & Styling Tokens

Defined in `ui/theme/Color.kt`. **Never change these token values.**

| Token | Hex | Usage |
|---|---|---|
| `LuxuryBlack` | `0xFF090D1A` | Root background for all screens |
| `BackgroundGray` | `0xFFF8FAFC` | Card body container (scrollable content area) |
| `BiroBlue` | `0xFF5C58FF` | Primary accent — borders, buttons, selections |
| `SuccessGreen` | `0xFF10B981` | Success badges, completed statuses, verified |
| `DangerRed` | `0xFFDC2626` | Errors, cancellations, logout |
| `WarningOrange` | `0xFFF97316` | Surge pricing, warnings |
| `TextGray` | `0xFF64748B` | Secondary/subtitle text |
| `CardBorderGray` | `0xFFE2E8F0` | Card borders, dividers |
| `DarkGradientBlue` | `0xFF0F172A` | Gradient start (slate dark blue) |
| `RoyalGold` | `0xFFD4AF37` | Reserved for premium badges |
| `TextMain` | `0xFF1A1D26` | Primary text |
| `TextMuted` | `0xFF8A8D9F` | Muted/hint text |

**`BrandGradient`**: `Brush.horizontalGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF5C58FF)))`

---

## 7. Layout Architecture (Rounded-Sheet Paradigm)

Used by ALL secondary screens (Login, SignUp, Booking, Tracking, OrderLogs, Wallet, Profile, Settings):

```kotlin
Box(Modifier.fillMaxSize().background(LuxuryBlack)) {
    Column(Modifier.fillMaxSize().background(BrandGradient)) {
        ScreenHeader(title = "...", onBack = { ... })
        // OR Box with statusBarsPadding() for custom headers
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundGray),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            // Scrollable content with bottom padding minimum 120.dp
        }
    }
}
```

**Header (ScreenHeader):**
- `Box` with `statusBarsPadding()`
- Row with `padding(horizontal = 20.dp, vertical = 18.dp)`
- Left: `IconButton` with `Icons.AutoMirrored.Filled.ArrowBack` tinted `Color.White`
- Center: Title text, uppercase, `fontSize = 14.sp`, `letterSpacing = 1.5.sp`, `fontWeight = FontWeight.ExtraBold`, `Color.White`
- Right: Utility element (count badge, status pill, or `Spacer(Modifier.size(48.dp))`)

Dashboard header is SPECIAL — it's a collapsing header (not ScreenHeader), see section 3.3.

---

## 8. Dashboard Collapsing Header Rules

- Height: 220dp expanded → 115dp collapsed via `derivedStateOf`
- `NestedScrollConnection` for pull-to-refresh (overscroll → "Pull to refresh" → "Release to refresh" → spinner)
- Content padding: `PaddingValues(top = 236.dp, bottom = 120.dp)` on the LazyColumn
- Profile photo clickable → Profile screen
- Notification bell clickable → Profile screen
- Search bar clickable → activates inline search mode (not a separate screen)
- Shadow elevation: 8.dp, clip = false (shadow on the Box, not the content)

---

## 9. CTA Button Styling

```kotlin
// Primary CTA — BrandGradient background, white text
PremiumGradientButton(text, onClick, modifier, icon, enabled)
// Implementation: Button with transparent colors, Modifier.background(BrandGradient, shape)

// Outlined variant — BiroBlue border, BiroBlue text
OutlinedGradientButton(text, onClick, modifier, icon)
```

Used for: "+ New Delivery", "Fund Wallet", "Withdraw", "Confirm Booking", "Login to Dispatch", "Create Account", "Verify OTP", "Add New Payment Method", etc.

---

## 10. BottomSheet Patterns

Every BottomSheet in the app follows this exact pattern:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SomeSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            // Sheet content
        }
    }
}
```

**Currently used in:** Security, ChangePassword, DefaultAddress, About, Language (Settings) + PaymentMethods, FundWithdraw, Success, ProfileEdit, HelpSupport, AboutProfile, PreferredRiders, LiveChat, SettingsPayment, OtpVerification.

---

## 11. Click Target & Ripple Rules

Every clickable Card or Surface MUST clip its click target to prevent sharp-edged ripple shadows:

```kotlin
// CORRECT — clip before clickable
Card(
    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    ...
)
```

This pattern is applied in: ProfileScreen, SettingsScreen, DashboardScreen, OrderLogsScreen, BookingFormScreen, CommonComponents.

---

## 12. Skeleton Loader Styling

- **No shadow elevations on skeleton loaders**
- Flat border stroke: `1.dp` thickness, color `CardBorderGray`
- Use `ShimmerBox` composable (from CommonComponents) — animated linear gradient shimmer with `infiniteRepeatable(tween(1100))`

---

## 13. Entrance Animations & Motion

- **Navigation transitions**: `slideInHorizontally { w -> w / 3 } + fadeIn` / `slideOutHorizontally { -w / 3 } + fadeOut`, `tween(400, easing = FastOutSlowInEasing)`
- **Content entrance**: `animateDpAsState`/`animateFloatAsState` targeting `0.dp`/`1f` on launch
- **Ripple feedback**: Every touch target must have Material ripple — dead taps forbidden
- **Haptic feedback**: Bottom nav items + Scan FAB use `HapticFeedbackType.LongPress`

---

## 14. Offline/Local Strategy

- **Room DB** (`AppDatabase`) stores deliveries locally — serves as offline cache
- **DataStore** (`UserPreferences`) persists auth token, user profile, wallet balance, settings toggles
- **ViewModel fallback**: When API calls fail (NetworkResult.Error), the app creates local tokens and operates in offline mode using DataStore + Room
- **This is acceptable as a temporary fallback**, but the PRIMARY data source MUST be the backend API

---

## 15. Build & Verification Commands

```powershell
# Compile Kotlin only (fast)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew --no-daemon compileDebugKotlin

# Full build (APK)
./gradlew --no-daemon assembleDebug

# Clean + full build (for fresh install)
./gradlew --no-daemon clean assembleDebug

# Force all tasks to rerun (bypass Gradle build cache)
./gradlew --no-daemon --no-build-cache --no-configuration-cache clean assembleDebug
```

**Package:** `com.aistudio.engraceddispatch.kxmpzq`
**Output APK:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 16. Strict Agent Rules — Preservation & Enhancement

1. **NEVER remove a screen** — all 10 screens (Splash, Login, SignUp, Dashboard, BookingDetails, ActiveTracking, OrderLogs, Wallet, Profile, Settings) must always exist.
2. **NEVER remove a menu item or row** — every row in Profile and Settings must remain and be functional.
3. **NEVER remove a BottomSheet** — sheets are the primary interaction pattern; no dialogs/AlertDialogs.
4. **NEVER replace a ModalBottomSheet with a Dialog** — dialogs are forbidden except for system pickers.
5. **NEVER change the theme tokens** — the exact hex values in Color.kt are locked.
6. **NEVER simplify the dashboard collapsing header** — it must maintain its 220dp→115dp range.
7. **NEVER remove the Scan FAB or NotchedNavShape** — the overlay bottom nav is mandatory.
8. **ALWAYS add `.clip(shape)` BEFORE `.clickable(...)` on ANY clickable component (Cards, Boxes, Surfaces, Rows, Text, Icons, etc.) that has rounded corners, to prevent ugly sharp-edged/rectangular click ripples/shadows from overflowing.**
9. **NEVER use `.clickable(...)` BEFORE `.clip(shape)` on components with rounded backgrounds, borders, or parent shapes.**
10. **ALWAYS connect to the API** — no new mock data. If the endpoint doesn't exist yet, use the offline fallback but leave a TODO comment to connect it.
11. **ALWAYS use the exact BottomSheet pattern** from section 10 — `skipPartiallyExpanded = true`, `28.dp` top corners, white container.
12. **EVERY BottomSheet must have a slide-to-close gesture** — the hide animation must run before dismissal.
13. **The badge text must always say "ENGRACED VERIFIED MEMBER"** — never "DISPATCHER" or any rider/dispatcher terminology. The app is for customers.
14. **Every Settings row must open its own unique screen or sheet** — no duplicates, no toasts as placeholders.
15. **Keep all existing data model fields** — you may ADD new fields but never remove existing ones.

---

## 17. Known Mock/Placeholder Locations (To Be Connected to Backend)

| File | Line(s) | What | Replace With |
|---|---|---|---|
| `DeliveryViewModel.kt` | 164-188 | Login API error fallback (creates local token) | Only use when server unreachable |
| `DeliveryViewModel.kt` | 219-237 | Register API error fallback | Only use when server unreachable |
| `DeliveryViewModel.kt` | 254-261 | `updateProfile()` uses `delay(500)` + local save | Real API call to `PUT /v1/profile` |
| `DeliveryViewModel.kt` | 265-278 | `fundWallet()` local-only | Real API call to `POST /v1/wallet/fund` |
| `DeliveryViewModel.kt` | 281-296 | `withdrawFunds()` local-only | Real API call to `POST /v1/wallet/withdraw` |
| `DeliveryViewModel.kt` | 317-328 | `loadTransactions()` hardcoded mock data | Real API call to `GET /v1/wallet/transactions` |
| `DeliveryViewModel.kt` | 339-398 | `createBooking()` local-only with mock rider data | Real API call to `POST /v1/deliveries` |
| `DeliveryViewModel.kt` | 464-472 | `getBasePrice()`/`getSurgeAmount()` hardcoded | Fetch pricing from backend |
| `CommonComponents.kt` | 186-192 | `ShimmerBox` — purely visual placeholder | Keep (loading UI, not data) |
| `BookingFormScreen.kt` | 133 | `createBooking()` called with blank onSuccess | Use real callback |
| `DashboardScreen.kt` | 54-64 | Promo cards are hardcoded | Fetch from backend promotions endpoint |
| `TrackingScreen.kt` | 445-461 | `advanceDeliveryStatus()` local state only | Real API call to `PUT /v1/deliveries/{tracking}/status` |
| `SettingsScreen.kt` | 69 | Language sheet doesn't persist selection | Save to server + DataStore |
| `DeliveryViewModel.kt` | 421-442 | `verifyOtp()` local-only with Room DB | Real API call to `POST /v1/deliveries/{tracking}/verify` |
| `DeliveryViewModel.kt` | 504-507 | `deleteDelivery()` local-only | Real API call to `DELETE /v1/deliveries/{id}` |

---

## 18. Internal Rider Fleet Model

- **Company Employees:** Riders are internal staff members of the delivery company, not independent gig workers or third-party drivers.
- **Direct Assignment:** Administrators manage the rider fleet and assign dispatches directly to them.
- **Rider App Role:** The Rider App serves as a work tool for drivers to perform their daily duties (accept assignments, verify pickups/transits, and log delivery proof with customer OTP validation).

---

## 19. Admin Dashboard (`admin/`)

### 19.1 Tech Stack

| Layer | Technology |
|---|---|
| Framework | React 19 + TypeScript 6 |
| Build | Vite 8 + @tailwindcss/vite |
| Styling | Tailwind CSS v4 (CSS-based config via `@theme`) |
| Routing | react-router-dom v7 |
| HTTP | Axios |
| Charts | Recharts |
| Notifications | react-hot-toast |
| Icons | lucide-react |

### 19.2 Project Structure

```
admin/
├── index.html                    # HTML entry point + Google Fonts <link>
├── vite.config.ts                # Vite + React + TailwindCSS plugins + @/ alias + API proxy
├── tsconfig.app.json             # TypeScript config with @/* path alias
├── package.json                  # Deps: react, react-dom, react-router-dom, axios, recharts, lucide-react, tailwindcss, @tailwindcss/vite
├── src/
│   ├── main.tsx                  # Entry: StrictMode + Toaster + App
│   ├── App.tsx                   # BrowserRouter + Routes (login + protected layout routes)
│   ├── index.css                 # Tailwind import + @theme tokens (mobile colors) + custom CSS
│   ├── components/
│   │   ├── Layout.tsx            # Sidebar + Header + <Outlet /> wrapper
│   │   ├── Sidebar.tsx           # Nav sidebar (8 links) + system status card
│   │   ├── Header.tsx            # Search bar + Simulate button + action CTA
│   │   ├── KpiCard.tsx           # Reusable stat card (icon, label, value, description)
│   │   ├── StatusBadge.tsx       # Colored status pill (maps status string → color)
│   │   └── DataTable.tsx         # Generic table with typed columns
│   ├── pages/
│   │   ├── Login.tsx             # Admin login form → POST /v1/auth/login
│   │   ├── Dashboard.tsx         # KPI row + revenue line chart + pending assignments + recent dispatches
│   │   ├── DispatchCenter.tsx    # Full deliveries table + assign/advance/verify-OTP + create dispatch modal
│   │   ├── Riders.tsx            # Rider cards grid + add rider modal
│   │   ├── Zones.tsx             # Zone cards with enable/disable + add zone modal
│   │   ├── Pricing.tsx           # Pricing rule cards + edit modal per delivery type
│   │   ├── Users.tsx             # Customer table (name, contact, deliveries, rating, verified, member since)
│   │   ├── Settings.tsx          # System settings + auto-assign toggle + numeric thresholds
│   │   └── Finance.tsx           # Revenue KPI cards + bar chart (revenue vs expenses) + transaction list
│   ├── lib/
│   │   ├── api.ts                # Axios instance (base URL, interceptors, auth token)
│   │   └── mockData.ts           # All mock data in one file (deliveries, riders, transactions, zones, pricing, settings, customers)
│   └── types/
│       └── index.ts              # All TypeScript interfaces (User, Delivery, Rider, Transaction, WalletStats, Zone, PricingRule, SystemSettings, LoginRequest, AuthResponse)
```

### 19.3 Navigation & Routing

| Path | Component | Auth Required | Sidebar Label |
|---|---|---|---|
| `/login` | Login | No | — |
| `/dashboard` | Dashboard | Yes | Dashboard |
| `/dispatch` | DispatchCenter | Yes | Dispatch Center |
| `/riders` | Riders | Yes | Riders Registry |
| `/zones` | Zones | Yes | Delivery Zones |
| `/pricing` | Pricing | Yes | Pricing Rules |
| `/users` | Users | Yes | Customers |
| `/settings` | Settings | Yes | Settings |
| `/finance` | Finance | Yes | Finance |
| `/` | Redirect → `/dashboard` | — | — |

### 19.4 Auth Flow
- `localStorage.getItem('admin_token')` check in `ProtectedRoute` wrapper
- Login page POSTs to `/auth/login`, stores token, redirects to `/dashboard`
- 401 interceptor clears token and redirects to `/login`
- Local mock fallback: hardcoded `admin_token` in localStorage bypasses login

### 19.5 Color Tokens (Tailwind v4 `@theme`)

All mobile app colors (`index.css` `@theme`) available as Tailwind utility classes:
- `bg-luxury-black`, `text-biro-blue`, `bg-royal-gold/10`, `border-admin-border`, `bg-admin-surface`, etc.

### 19.6 Shared Component Usage

- **KpiCard**: `label`, `value`, `desc`, `icon` (LucideIcon), `color`, `bg` — used in Dashboard and Finance pages
- **StatusBadge**: Takes a status string, returns colored pill — used across all pages
- **DataTable**: Generic typed table with `columns: Column<T>[]` and `data: T[]` — available for any list page
- **Layout**: Wraps all authenticated pages with Sidebar + Header + ambient glow orbs

### 19.7 Data Flow (Current: Mock → Future: API)

All pages currently import mock data from `src/lib/mockData.ts`. To connect to the real backend:
1. Replace imports of mock data with API calls using `src/lib/api.ts`
2. Use `useEffect` + `useState` pattern (already scaffolded in most pages)
3. API service functions can be added to `src/lib/api.ts` or a separate service file

### 19.8 Build & Verify Commands

```powershell
# From admin/ directory:
cd admin

# Development server
npx vite

# Production build
npx vite build

# TypeScript type check
npx tsc -b
```

### 19.9 Design Rules (same as mobile apps)

1. **Every clickable Card/Row must have `.clip(rounded)` before `.clickable`** — prevents sharp-edged ripple shadows
2. **Dark theme**: `.bg-admin-bg` (#050508) background, `.bg-admin-surface` (#08080C) cards, `.border-admin-border` (#151522) borders
3. **Ambient glow orbs**: `.glow-orb` + `.glow-blue`/`.glow-purple` positioned fixed in the background
4. **No rider earnings or payout features** in any admin page — riders are internal employees
5. **Auto-assign toggle** is a global system setting (Settings page) that affects all dispatch operations

