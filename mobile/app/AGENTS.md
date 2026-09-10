# ESDISPATCH - AI Coding Agent Guidelines & Architecture

## Official Brand & System Constraints
- **Brand Name**: "ESDispatch" (or "ESDISPATCH")
- **Slogan**: "PREMIUM LOGISTICS & DISPATCH"
- **App Model**: Premium logistics and dispatch for the owner's real operating model. Do not invent gig, fleet, employee, payroll, vendor, city, or service-scope claims unless they match the current product requirements and backend behavior.

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

## Permanent Product Language, AI-Trace & Production UI Rules

These rules apply to every Android customer, rider, vendor, admin, notification, email, widget, support, loading, empty and error surface.

1. Never expose developer instructions, implementation status, audit wording, AI-agent wording, test terminology, stack traces, raw Firebase/OAuth/package details, service names, tokens, OTP values, API responses, mock status, simulation status, TODO/FIXME text, seed tools, sample-data controls, or debug content to normal users.
2. Developer instructions describe behavior. They are not product copy. Do not paste the wording of a user's request or an agent instruction into the UI.
3. If a feature is real, let the flow prove it. Do not write "real OTP", "not simulated", "backend connected", "production ready", "implementation complete", or similar self-congratulatory copy.
4. Product copy should sound like a professional logistics app. Use clear wording such as "Verification code sent", "Google sign-in is temporarily unavailable", "Request received", "Queued", "Rider reserved", "Proof upload failed, retrying", and "Last GPS update 2 min ago."
5. A feature that is disabled, empty, offline, loading, blocked, permission-denied, or failed must have a deliberate state with the next useful action.
6. Icons must do what users expect: heart for favorites, cart for cart, bell for notifications, search for search, phone for call, map/navigation for route.
7. Motion must communicate real feedback or real state change. Never animate fake delivery progress, fake GPS, fake assignment, or fake AI confidence in production.
8. Do not claim a flow works because it compiles. Follow the release APK, install and physical-device verification rules for Android code changes.

## 11. Completion Rule

Do not stop after a local code change. Verify the entire user flow and check regressions. For code changes, follow the build, release, install, typecheck, commit and deploy-readiness rules above. For documentation-only changes, clearly state that no runtime code was changed.

---

# COMPONENT QUALITY & MICRO-UI RULES

All repeated UI patterns must be implemented through shared components or shared primitives unless there is a clear product reason not to.

Required shared primitives include status badges, route displays, price displays, icon-only action buttons, section headers, empty states, loading skeletons, bottom sheets, admin panels, and notification banners.

Cards must use a deliberate internal grid. Do not place order IDs, prices, statuses, addresses, icons, and actions wherever they are easiest to code. Position information by hierarchy:
- what the item is
- current state
- route or operational context
- owner/rider/customer where relevant
- valid next action

All delivery status UI must reflect the real delivery lifecycle. Do not allow customer, rider, or admin UI to show rider-dependent progress unless rider assignment, rider reservation, or rider action data supports it.

All address and route UI must handle long Benin City addresses, missing optional data, and different screen widths without clipping, overlap, or broken rhythm.

Icon-only controls must provide at least 44dp/44px hit area, visible pressed state, disabled state where relevant, and accessible labels.

Notifications must support dedupe, dismissal persistence, expiry, and action state. A notification dismissed by the user must not reappear unless the underlying event changes.

Every new or modified component must be checked in light mode, dark mode, small screen width, long text, missing data, loading state, empty state, error state, disabled state, and reduced-motion mode where applicable.
