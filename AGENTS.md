# AI Coding Agent Guidelines

This document establishes the official user interface rules, color standards, styling conventions, and strict branding locks for this application. These specifications ensure a clean, unified, and premium aesthetic across both light and dark modes.

> [!CRITICAL]
> **STRICT LOCK: BRANDING, LOGO & IDENTITY CONSTRAINTS**
> Under NO circumstances should any developer (human or AI) modify, redesign, or alter the branding, logo, color schema, or visual flows of this application. This visual identity is locked and must not be touched:
> 1. **Brand Name**: "ENGRACED DISPATCH" is the official brand name. Do not change it, rename it, or use any alternative spelling.
> 2. **Official Slogan**: "PREMIUM LOGISTICS & DISPATCH". Keep it fully capitalized and exactly as written.
> 3. **The Brand Logo**:
>    - The logo asset (`R.drawable.ic_logo`) is a custom-designed vector icon.
>    - Do not replace, wrap, or modify the logo asset itself.
>    - **First Splash Screen**: Must remain clean and minimalist. It displays **ONLY** the raw logo in gold (`Gold`) and has absolutely **NO texts** and **NO background square** container on it. It must be centered on the clean dark luxury background.
>    - **Second Preloader Screen**: Must **NOT** feature any circular/arc spinner indicator.
>      - The layout features:
>        - A top-edge horizontal loading progress bar (`progressAnim` filling the width of a slim 4dp track).
>        - A centered, scaled brand row: A Gold rounded square (`RoundedCornerShape(22.dp)`) acting as the logo container, containing the Obsidian-tinted logo, with the text "ENGRACE" (top line) and "DISPATCH" (bottom line) aligned vertically and positioned on its right.
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
