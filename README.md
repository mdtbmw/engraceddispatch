# 👑 ENGRACED DISPATCH: PREMIUM LOGISTICS & DISPATCH

Welcome to the **Engraced Dispatch** mobile application codebase. This repository contains the complete production-grade Android application and Firebase Cloud Functions backend designed for high-efficiency, premium logistics operations. 

This document serves as the absolute single source of truth for human developers and future AI agents on how the system is organized, how components communicate, why the architecture is designed this way, and how to maintain and scale it.

---

## 🎨 1. BRAND IDENTITY & CONSTRAINTS

To maintain the luxury premium positioning of **Engraced Dispatch**, specific design and branding parameters are strictly locked in `AGENTS.md` and resource files:
*   **Official Brand Name**: `ENGRACED DISPATCH`
*   **Official Slogan**: `PREMIUM LOGISTICS & DISPATCH`
*   **Primary Assets**: The logo vector asset is `R.drawable.ic_logo`.
*   **Visual Guardrails**:
    *   **Splash Screen**: Centered logo in `Gold` on a clean, solid, minimalist `LuxuryBlack` background. No text, no container.
    *   **Preloader Screen**: Horizontal slim progress bar at the top, rounded square gold container enclosing the logo with "ENGRACE" and "DISPATCH" vertically aligned on the right. Custom progress statuses update smoothly.
    *   **Color Theme Contrasts**: 
        *   *No White on Gold*: High-contrast dark `Obsidian` text/icons must be used over gold components.
        *   *No Gold on White*: Dark surfaces should use Gold highlights; light surfaces must use `Obsidian` or `TextGray` for maximum readability.
        *   *Adaptive System Bar Headers*: Dynamic headers use solid **Gold** in Dark Mode and solid **Obsidian** in Light Mode.

---

## 🏗️ 2. HIGH-LEVEL SYSTEM ARCHITECTURE

The application is structured around a modern **MVVM (Model-View-ViewModel)** pattern combined with modern declarative Jetpack Compose UI.

```
                  ┌──────────────────────────────────┐
                  │        Jetpack Compose UI        │
                  │ (Customer/Driver/Admin Screens)  │
                  └─────────────────▲────────────────┘
                                    │  Observes Flow State
                  ┌─────────────────▼────────────────┐
                  │       DeliveryViewModel          │
                  │   (Centralized State & Logic)    │
                  └─────────────────▲────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌──────────────┐
│  Room Cache  │             │   Firebase   │             │   Gemini AI  │
│ (Local DB)   │             │  Realtime DB │             │   API Client │
└──────────────┘             └──────────────┘             └──────────────┘
```

### Why It Is Structured This Way
1.  **State Unification (StateFlow)**: To prevent race conditions, UI states (parcels, active tracking, shift status, and courier positions) are held in state flows inside `DeliveryViewModel` and collected in Composables with `collectAsStateWithLifecycle()`.
2.  **Offline-First Resilience**: If the internet or Firebase connection drops, local actions are cached in a robust **Room Database** and synced when connection is restored.
3.  **Real-Time Map Pan-to-Follow**: In `TrackingScreen.kt`, the custom Leaflet.js-based WebView map includes a bridge to dynamically center/pan the viewport around the courier whenever coordinate updates arrive (`updateCourierCoordinates` in JavaScript pans via `map.panTo`).

---

## 📂 3. CODEBASE DIRECTORY STRUCTURE

```
├── .env                  # Project secrets (API keys, maps, DB configurations) - NEVER PUSHED
├── .env.example          # Template showing necessary keys for setup
├── settings.gradle.kts   # Project and module definitions
├── build.gradle.kts      # Project-level Gradle scripts
├── AGENTS.md             # UI/UX guidelines and strict branding configurations
├── functions/            # Backend Firebase Cloud Functions
│   ├── src/
│   │   └── index.ts      # Cloud Function Triggers (welcome FCM, status sync, claims)
│   └── package.json
└── app/
    ├── build.gradle.kts  # App-level build configurations & dependencies
    └── src/main/java/com/example/
        ├── MainActivity.kt        # Entry Point, NavHost, and System UI setup
        ├── DispatchApplication.kt # Firebase Context initialization
        ├── data/
        │   ├── FirebaseManager.kt # Firebase Database reference handles and online state
        │   └── database/          # Room Entity, DAO, and Cache Database
        ├── ui/
        │   ├── theme/             # Material 3 colors, typography, shapes, and custom classes
        │   ├── components/        # Symmetrical components, custom cards, app bars, and dialogs
        │   └── screens/
        │       ├── OnboardingScreens.kt # Splash, Preloader, and Onboarding Welcome flows
        │       ├── AuthScreens.kt       # Sign In, Sign Up, Biometric Lock, PIN Setup
        │       ├── DashboardScreen.kt   # Customer parcel booking, lists, history, and status
        │       ├── TrackingScreen.kt    # WebView Map tracking, leafet routing, and timeline
        │       └── ScannerScreen.kt     # CameraX Scanner with ZXing barcode analyzer
        └── viewmodel/
            └── DeliveryViewModel.kt # Central Business logic, authentication, and state flows
```

---

## 🚚 4. COMPONENT & ROLE COMMUNICATION

The platform coordinates real-time interaction between three primary user roles: **Customers**, **Drivers/Couriers**, and **Admins**.

```
    ┌──────────┐              Creates Shipment             ┌──────────┐
    │ Customer ├──────────────────────────────────────────►│ Firebase │
    └──────────┘                                           │ Realtime │
                                                           │ Database │
    ┌──────────┐             Scans & Updates Status        │          │
    │  Driver  ├──────────────────────────────────────────►│          │
    └──────────┘                                           │          │
                                                           │          │
    ┌──────────┐             Monitors & Simulates          │          │
    │  Admin   ├──────────────────────────────────────────►│          │
    └──────────┘                                           └────┬─────┘
                                                                │
                                                    Triggers    │
                                                    Cloud       ▼
                                                    Functions ┌───────────────┐
                                                              │  FCM Send to  │
                                                              │  Device Alert │
                                                              └───────────────┘
```

### A. How Customer Booking Communicates
1.  **Booking Submission**: A customer Books a shipment on `DashboardScreen`. It writes a new parcel record directly to the Firebase Realtime Database path: `/parcels/{parcelId}`.
2.  **In-App / Push Notification**: Firebase Realtime Database triggers the Cloud Function `onShipmentStatusUpdated` (under `functions/src/index.ts`) which sends an FCM push notification directly to the user's registered device.

### B. How the Driver App Works
1.  **Console Unlock**: The Driver logs in using their secure PIN or system biometric authentication (`BiometricPrompt` with fingerprint/face validation defined in `FingerprintAuthDialog` in `AuthScreens.kt`).
2.  **Accepting a Shipment**: Drivers see assigned dispatches, tap "Go Online" (shift state), and scan the parcel's barcode or QR code using the integrated **CameraX / ZXing Analyzer** (`QrCodeAnalyzer` in `ScannerScreen.kt`).
3.  **Real-Time Coordinates**: While on transit, the driver's device streams coordinate updates to Firebase Realtime Database `/riders/{uid}/location` and `/parcels/{parcelId}/courierLocation`. The customer's active tracking map detects this shift and pans to follow the driver in real-time.

### C. How Admin Changes Work
1.  **Real-Time Monitor Dashboard**: Admins have specialized views to monitor total performance, active shipments, delivery completion rates, and rider performance lists.
2.  **Coordinate & Status Simulation**: Admins can directly adjust transit statuses or trigger route simulations. Adjustments are committed to `/parcels/{parcelId}`, immediately cascading down to the customer's Leaflet WebView map via Realtime Sync.

---

## 🔑 5. SECRETS & ENVIRONMENT CONFIGURATION

Credentials and private integrations are isolated inside the root-level `.env` file:
*   **Plugin Mechanism**: The app uses the **Secrets Gradle Plugin** configured inside `app/build.gradle.kts`.
*   **Resolution Scope**:
    *   The plugin looks for `/.env` in the root directory.
    *   It parses environment variables and automatically generates them as `BuildConfig` constants (e.g., `com.example.BuildConfig.FIREBASE_API_KEY`) during compilation.
    *   If no `.env` is found at the root, it falls back to `/.env.example` as a default.
*   **Credentials Mapping**:
    *   `GEMINI_API_KEY`: Model generation, routing assistance, and AI risk analysis.
    *   `FIREBASE_API_KEY` / `FIREBASE_APPLICATION_ID` / `FIREBASE_PROJECT_ID` / `FIREBASE_DATABASE_URL`: Realtime Database connection parameters.
    *   `MAPBOX_ACCESS_TOKEN`: Location reverse-geocoding and route rendering.
    *   `PAYSTACK_PUBLIC_KEY`: Dynamic mock and real-world card payments processing.

---

## ⚙️ 7. ADMIN & SYSTEM CONTROL END-TO-END CAPABILITIES

The application provides a comprehensive Admin & System Control Center within the AI Dispatch Manager console, giving administrators and drivers full mastery over the platform:
1. **Points & Loyalty System Toggle**: Instantly enable or disable the customer loyalty points reward program across the application.
2. **Driver Tip System Toggle**: Control whether customers can attach tips for drivers on delivery checkouts.
3. **Dashboard Sections Visibility Management**: Toggle individual dashboard modules on or off (Promotional Hero Banner, Active Shipments Card, Quick Action Grid, Loyalty Rewards Panel).
4. **Card & Slider Customization**: Edit hero banner titles, subtitle text, and banner background image URLs in real-time.
5. **Driver System Master Controls**: Empower drivers and administrators to execute system-wide actions such as broadcasting surge pricing alerts and synchronizing fleet status.
6. **Sub-Admin Permissions & User Management**: Manage sub-admin accounts and assign role-based permissions ('View Only' for reporting or 'Content Manager' for updating app sliders and images).
7. **Secure Activity Audit Log**: Real-time audit log tracking critical administrator and driver actions such as system settings toggles, visibility updates, and driver account status changes.
8. **Bulk Action Delivery Management**: Multi-select shipments in the admin view to execute bulk status updates or reassign drivers instantly.

---

## 🛠️ 8. CI/CD & PRODUCTION PIPELINES

### What Gets Pushed to GitHub
*   **Included**: All Kotlin files, layouts, themes, drawables, gradle files (`build.gradle.kts`, `settings.gradle.kts`), testing frameworks, and cloud function typescript resources.
*   **Excluded (`.gitignore`)**:
    *   **`.env`**: Private credentials, production API keys.
    *   **`debug.keystore`**: Production keystores.
    *   **`/build` & `.gradle`**: Local artifacts and cached dependencies.

### What Gets Deployed
*   **Android App (APK/AAB)**: Generated via the Google AI Studio Android build container or GitHub Actions with local keys and signs.
*   **Firebase Backend Functions**: Deployed to Firebase Cloud Functions:
    ```bash
    cd functions/
    npm install
    npm run build
    firebase deploy --only functions
    ```
    *These functions handle user welcome triggers, custom JWT claims, and FCM updates on Firestore / Realtime DB status updates.*
*   **Web Console / Vercel**: If a companion admin/dispatcher console is used, its static layout is deployed to **Vercel** with the required environment variables pointing to the same Firebase instances.

---

## 🚀 7. BEST PRACTICES FOR WORKFLOWS & DEVELOPERS

1.  **Check Config State**: Always verify that `/.env` in the root directory contains valid Firebase API keys. If `FIREBASE_API_KEY` is empty, or uses a template value beginning with `AIzaSyFakeKey`, the app's safety constraints will transition it into **Under Maintenance Screen** mode to prevent unauthorized database access in production environments.
2.  **Surgical Code Edits**:
    *   Read files using `view_file` before making any modifications.
    *   For multi-line edits, use `multi_edit_file` to keep change-sets precise.
3.  **Compile & Test**:
    *   Run `compile_applet` regularly to ensure dependency chains are correct.
    *   For unit and local JVM tests, run `gradle :app:testDebugUnitTest`.

---
*Developed & maintained to elite standards for the Engraced Dispatch Logistics System.*
