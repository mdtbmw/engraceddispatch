# ENGRACED DISPATCH - AI Coding Agent Guidelines & Architecture

## Official Brand & System Constraints
- **Brand Name**: "ENGRACED DISPATCH"
- **Slogan**: "PREMIUM LOGISTICS & DISPATCH"
- **App Model**: Enterprise Corporate Logistics & Employee Fleet Dispatch (NOT a gig-economy platform). All drivers are direct company employees managed via internal dispatch manifests, payroll, and fleet tracking.

## Fleet Driver System Specifications
1. **Corporate Fleet Manifest**: Replaced all freelance/gig terminology with formal company dispatch run-sheets.
2. **Real-Time GPS & Geofencing**: Live tracking with 50-meter proximity arrival triggers.
3. **Secure OTP Handshake**: 4-digit recipient verification for delivery completion.
4. **Salary & Performance Payroll**: Tracks base company salary, delivery bonuses, and customer tips.

## ⚠️ MANDATORY BUILD & RELEASE WORKFLOW (ALWAYS FOLLOW)
The Android app must always be shipped as a **Release APK**. Debug-only compiles (e.g. `compileDebugKotlin`) do NOT produce an installable artifact — a previous incident occurred where changes were merged and "verified" via debug compile only, but never packaged/installed, so the phone showed stale behavior. Follow these rules on EVERY code change:

### 1. Always compile AFTER every change
- Run: `.\gradlew.bat :app:compileDebugKotlin --console=plain` from `D:\Eng App\mobile`
- Fix ALL errors (warnings/deprecations are OK — they are pre-existing).

### 2. ALWAYS build the release APK before declaring a task done
- Run: `.\gradlew.bat :app:assembleRelease --console=plain` from `D:\Eng App\mobile`
- This takes 3–8 minutes (R8/minify is slow). Use a bash timeout of at least **1,800,000 ms**.
- Verify the artifact exists and record its size/timestamp:
  - APK path: `D:\Eng App\mobile\app\build\outputs\apk\release\app-release.apk`
- If `assembleRelease` fails due to R8/minification, fix the issue and re-run. Do NOT skip the release build.

### 3. Install & verify on the physical device (required for "it works" claims)
- Device must be connected via USB with USB debugging + file transfer enabled.
- Check: `C:\Users\USER\AppData\Local\Android\Sdk\platform-tools\adb.exe devices` (must show a device, not empty).
- Install: `& "C:\Users\USER\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "D:\Eng App\mobile\app\build\outputs\apk\release\app-release.apk"`
- A feature is NOT "done" until it has been installed and visually verified on the device.

### 4. Never claim a feature works based on compilation alone
- `BUILD SUCCESSFUL` only proves the code compiles. Features must be installed and exercised to be verified.

## Vendor Storefront & Marketplace Notes
- Customer-facing storefronts (`VendorStorefrontScreen`, Marketplace "Explore Stores") read from the **`marketplace_stores`** Firestore collection (doc id = vendor owner uid). Products link to a store via the **`vendorId`** field on `marketplace_products`.
- Public store browsing only surfaces `isVerified == true` stores in the Marketplace carousel; the storefront screen shows any store reached by id.
- Admin web tooling (`src/app/engdadmin/AdminDashboard.tsx`) can: seed linked stores/products, create a Vendor user (auto-creates an approved storefront), upgrade any user to Vendor, and enlist stores (auto-links by email or creates a vendor user record). Keep the mobile `isVerified` / `isPendingReview` / `kycStatus` schema in sync whenever touching store docs.

