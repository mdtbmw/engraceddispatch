# AI Coding Agent Guidelines

This document establishes the official user interface rules, color standards, styling conventions, and strict branding locks for this application. These specifications ensure a clean, unified, and premium aesthetic across both light and dark modes.

> [!CRITICAL]
> **STRICT LOCK: BRANDING, LOGO & IDENTITY CONSTRAINTS**
> Under NO circumstances should any developer (human or AI) modify, redesign, or alter the branding, logo, color schema, or visual flows of this application. This visual identity is locked and must not be touched:
> 1. **Brand Name**: "ESDispatch" (or "ESDISPATCH") is the official brand name.
> 2. **Official Slogan**: "PREMIUM LOGISTICS & DISPATCH". Keep it fully capitalized and exactly as written.
> 3. **The Brand Logo**:
>    - The logo asset (`R.drawable.ic_logo`) is a custom-designed vector icon.
>    - Do not replace, wrap, or modify the logo asset itself.
>    - **First Splash Screen**: Must remain clean and minimalist. It displays **ONLY** the raw logo in gold (`Gold`) and has absolutely **NO texts** and **NO background square** container on it. It must be centered on the clean dark luxury background.
>    - **Second Preloader Screen**: Must **NOT** feature any circular/arc spinner indicator.
>      - The layout features:
>        - A top-edge horizontal loading progress bar (`progressAnim` filling the width of a slim 4dp track).
>        - A centered, scaled brand row: A Gold rounded square (`RoundedCornerShape(22.dp)`) acting as the logo container, containing the Obsidian-tinted logo, with the text "ES" (top line) and "DISPATCH" (bottom line) aligned vertically and positioned on its right.
>        - A bottom section featuring the capitalized slogan "PREMIUM LOGISTICS & DISPATCH", followed by a smooth step-by-step progress status text.

## Dark Mode UI Rules

1. **Header Consistency**
   - **In Dark Mode**: All screen headers must use a solid **Gold** background.
   - **In Light Mode**: All screen headers must use a solid **Obsidian** (dark gray/black) background.
   - All interactive screens (except the main dashboard) must use the same `ScreenHeader` component with consistent back-navigation and title text.

2. **Contrast & Color Pairings (MANDATORY)**
   - **No White on Gold**: Under no circumstances should white text or white icons be placed directly on a Gold background (including buttons, tags, or headers). Use **Obsidian** (black) for maximum legibility and premium contrast.
   - **No Gold on White**: Under no circumstances should Gold text, Gold icons, or Gold outlines be rendered directly on a white or light card surface. Use **Obsidian** or **TextGray** on light surfaces.
   - Gold elements are reserved for highlight highlights (e.g., primary buttons, tags with Obsidian text, or dark-background status overlays).

3. **Global Theme Adaptation**
   - Ensure dark mode applies to **every single UI component** seamlessly:
     - **Page Backgrounds**: Must dynamically map to `AppBackground` (`LuxuryBlack` which resolves to `BackgroundDark` in dark mode).
     - **Cards & Surfaces**: Must dynamically map to `AppSurface` (`Charcoal` which resolves to `Obsidian` in dark mode).
     - **Bottom Floating Dock**: The bottom capsule floating navigation bar must automatically adapt to the theme, matching the app's surface and keeping high contrast icons (e.g. using `AppSurface` and adaptive selected colors).
     - **Outlines and Rec Shapes**: Soften all outlines on rounded rectangular shapes. Remove unnecessary borders/outlines that do not actively contribute to the premium Material 3 appearance.

## Driver System Capabilities & End-to-End Features
1. **Driver Profile & Gig Statistics**: Displays Online/Offline status toggle, total deliveries completed, cumulative tips earned, and 5-star average rating based on customer feedback.
2. **Proximity-Based Arrived Trigger**: Automatically prompts the driver to change delivery status to 'Arrived' when GPS coordinates match the delivery address location within a 50-meter radius.
3. **Completed Deliveries Dashboard**: Fetches past delivered shipments assigned to the driver from Firestore, showing date, recipient, and tip amount received.
4. **Mapbox Marker Breathing Animation**: Features a subtle pulsing circle animation on the driver marker in the customer's tracking view to emphasize real-time movement.
5. **Admin & System Control Center**: Provides admins and drivers complete control over the system, including toggling the points and loyalty system ON/OFF, enabling/disabling the driver tip system, managing dashboard sections visibility, customizing hero banner cards and slide intervals, and executing master system overrides (such as broadcasting surge pricing and synchronizing the fleet).
6. **Sub-Admin Permissions & User Management**: Allows assigning specific permissions to sub-admin accounts (e.g. 'View Only' for reporting or 'Content Manager' for updating app sliders and images).
7. **Secure Activity Audit Log**: Tracks critical actions such as settings toggles, card visibility updates, and account status changes in Firestore.
8. **Bulk Action Delivery Management**: Enables administrators to select multiple pending deliveries and update their status or reassign them to a different driver at once.

## Code Standards
- Use modern Jetpack Compose layouts and Material 3 components.
- Do not hardcode static colors for text or card containers; use the dynamic `Charcoal`, `LuxuryBlack`, and `AppTextColor` color tokens which automatically adjust to the system's dark theme state.
- Keep the overall user interface predictable, professional, and symmetrical.

## ⚠️ MANDATORY BUILD, RELEASE & DEPLOY WORKFLOW (ALWAYS FOLLOW)
The production project spans an **Android app (`mobile/`)** and a **Next.js web admin (repo root, deployed on Vercel)**. Two past incidents happened because work was "verified" by a partial build only (debug compile) and never packaged/installed, and because fixes were left uncommitted so the live site kept failing. Enforce the steps below on EVERY change.

### Android (mobile/)
1. **After every code change**, run: `.\gradlew.bat :app:compileDebugKotlin --console=plain` from `D:\Eng App\mobile`. Fix all errors.
2. **ALWAYS produce the release APK before declaring a task done**: `.\gradlew.bat :app:assembleRelease --console=plain` (allow a timeout ≥ 1,800,000 ms — R8 is slow).
   - Artifact: `D:\Eng App\mobile\app\build\outputs\apk\release\app-release.apk`
3. **Install & visually verify on the physical device** (USB debugging + file transfer):
   - `C:\Users\USER\AppData\Local\Android\Sdk\platform-tools\adb.exe devices` (must show a device)
   - `& "C:\Users\USER\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "D:\Eng App\mobile\app\build\outputs\apk\release\app-release.apk"`
4. **`BUILD SUCCESSFUL` ≠ done.** A feature is only done after it is installed and exercised on the device.

### Web admin (Next.js / Vercel)
1. **After every web change**, run `npx tsc --noEmit` from the repo root (`D:\Eng App`) and fix all type errors.
2. **Never leave changes uncommitted.** The live site 404 was caused by a committed `vercel.json` that was fixed only in the working tree. Before handing off:
   - `git status` → review `git diff` → commit the intended files → confirm the branch is pushed.
   - The admin portal is at **`/engdadmin`** (with the `d`); `/engadmin` is a redirect alias.
3. **Deployment is manual by the owner.** The user redeploys on Vercel after a commit/push. Never claim a web fix is "live" — say it is committed and ready to redeploy.

### Full-stack data consistency
- Vendor storefronts (`marketplace_stores`) and products (`marketplace_products` → `vendorId`) must stay schema-compatible between mobile and admin. When adding fields to store/product docs, update BOTH `DeliveryViewModel` (mobile listeners) and `AdminDashboard.tsx` (seed + create/upgrade handlers).

---

# ESDISPATCH INTERACTION PHYSICS & MOTION LANGUAGE

This section defines the official motion engineering, tactile feedback, and interaction physics standards for ESDispatch across Android Jetpack Compose and Web.

## 1. The 6-Stage Response Model
Every interactive element must follow this physical causality loop:
$$\text{REST} \longrightarrow \text{TOUCH CONTACT} \longrightarrow \text{PRESS ENGAGEMENT} \longrightarrow \text{ACTIVE} \longrightarrow \text{RELEASE} \longrightarrow \text{SETTLE / EQUILIBRIUM}$$

- **Zero-Lag Tactile Feedback**: Down-press acknowledgment must render on frame 0 (< 16ms) via `Modifier.tactilePress()` or CSS active transforms.
- **No Abrupt Snapping**: Elements never jump between states; they settle with spring physics.

## 2. Standardized Spring Tokens

| Token Name | Stiffness | Damping Ratio | Application Domain |
| :--- | :--- | :--- | :--- |
| **`TouchPress`** | `400f` | `0.70f` (Low Bouncy) | Quick action chips, primary buttons, bottom bar icons |
| **`SoftElastic`** | `280f` | `0.75f` (Smooth Settle) | Shipment cards, bottom sheets, modal dialogs |
| **`SnappyPill`** | `450f` | `0.85f` (No Overshoot) | Floating capsule dock traveling indicator, filter tabs |
| **`RubberBand`** | `220f` | `0.60f` (Elastic Boundary) | List overscroll, geofence boundary resistance |
| **`SignatureMoment`**| `180f` | `0.65f` (Resonant Pulse) | Dispatch broadcast send, 50m arrival alert, escrow settlement |

## 3. Domain Interaction Specifications
1. **Bottom Capsule Floating Dock**:
   - The indicator capsule must travel continuously with momentum, stretching slightly along the movement axis and settling cleanly around the target icon.
   - Inactive icons softly compress (`scale 0.88f`) and release upon touch.
2. **Live GPS Telemetry & Courier Marker**:
   - Continuous interpolation without coordinate jumping.
   - Ambient breathing beacon pulse (`Modifier.breathingPulse(1800ms)`) signals active GPS telemetry.
3. **Proof of Delivery (POD) & OTP**:
   - Signature canvas renders vector curves with velocity-responsive stroke widths.
   - Camera shutter contracts mechanically (`scale 1.0 -> 0.92 -> 1.0`).
   - OTP boxes spring-advance on digit input with horizontal tension on error.
4. **Booking Fare Live Ticker**:
   - Fare cost counters animate smoothly with spring interpolation as package weight or distance sliders move.
5. **Pure Web Audio Synthesizer**:
   - Zero external `.mp3` dependencies; pure Web Audio API oscillators for dispatch sweeps, geofence pings, and escrow settlement arpeggios. Toggle developer lab via `Ctrl+Shift+E`.


