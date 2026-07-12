# Resolution-Independent Asset & Branding Delivery Strategy
## Cross-Platform Brand Asset System for Engraced Dispatch

To maintain the luxury premium look and feel of **Engraced Dispatch**, all visual assets—including the gold vector logo (`ic_logo`), launcher icons, onboarding illustrations, and custom badges—must render with absolute clarity. This strategy outlines the unified workflow, folder layouts, and scaling techniques to achieve 100% visual parity across both Retina (iOS) and multi-density (Android) screens.

---

## 1. Asset Classification and Format Mandates

To ensure maximum fidelity and minimal app bundle size, assets are strictly classified:

| Asset Type | Primary Format | Fallback Format | Resolution Strategy |
| :--- | :--- | :--- | :--- |
| **Icons & Line Art** | Vector SVG / PDF | Android Vector XML | Resolution-independent scalable vector graphic rendering. |
| **Logos & Badges** | Vector SVG / PDF | High-Density Raster PNG | Rendered using exact mathematical shapes; supports infinite scaling. |
| **Photographic Imagery** | Lossless WebP | Progressive JPEG | Dynamic density buckets (`@2x`, `@3x` / `xxhdpi`, `xxxhdpi`). |

---

## 2. Directory Mappings & Structure

To ensure that both build pipelines can access the resources correctly, assets must follow this rigid directory structure:

```
/assets
  ├── branding/
  │    ├── ic_logo.svg                # Master Vector SVG (Logo)
  │    ├── ic_logo_gold.svg           # Gold logo variant
  │    └── ic_logo_obsidian.svg       # Obsidian logo variant
  ├── onboarding/
  │    ├── ic_onboarding_sending.webp  # Lossless photographic/illustrations
  │    ├── ic_onboarding_tracking.webp
  │    └── ic_onboarding_wallet.webp
  └── system/
       ├── ios/
       │    └── Assets.xcassets/      # iOS Asset Catalog (Xcode structure)
       └── android/
            └── res/
                 ├── drawable/        # Android Vector XML Drawables
                 ├── drawable-xxhdpi/ # Raster assets for high-density screens
                 └── drawable-xxxhdpi/
```

---

## 3. Density Multiplier Mappings (Retina vs. Android DP)

| Physical Screen Class | Density Bucket (iOS) | Density Bucket (Android) | Scale Factor | Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **Standard / MDPI** | `@1x` (Legacy) | `mdpi` (160 dpi) | `1.0x` | Deprecated for core premium assets. |
| **Retina / XHDPI** | `@2x` (iPhone 8/SE) | `xhdpi` (320 dpi) | `2.0x` | Standard crisp density. |
| **Super Retina / XXHDPI** | `@3x` (iPhone Pro/Max) | `xxhdpi` (480 dpi) | `3.0x` | Premium standard. |
| **Ultra High / XXXHDPI** | `@3x` (Scaled) | `xxxhdpi` (640 dpi) | `4.0x` | Ultimate pixel density. |

---

## 4. Implementation Guidelines

### A. Android Jetpack Compose Vector Rendering
Use Android Vector Drawables (`VectorDrawable`) inside `res/drawable` to achieve 100% resolution independence.
* **Scale Mode**: Fit or Crop using `ContentScale.Fit` to prevent vector clipping.
* **Sample Composable**:
```kotlin
Image(
    painter = painterResource(id = R.drawable.ic_logo),
    contentDescription = "Engraced Dispatch Gold Logo",
    modifier = Modifier.size(120.dp),
    colorFilter = null // Avoid tinting unless explicitly required for contrast
)
```

### B. iOS SwiftUI Vector PDF & SVG Assets
SwiftUI supports vector rendering directly from `Assets.xcassets`.
1. Drag the master `.svg` or `.pdf` file into Xcode’s Assets Catalog.
2. In the Attributes Inspector for the asset:
   * Check **"Preserve Vector Data"** (this tells iOS to keep the raw curves).
   * Set **"Scales"** to **"Single Scale"**.
3. Display in SwiftUI:
```swift
Image("ic_logo")
    .resizable()
    .aspectRatio(contentMode: .fit)
    .frame(width: 120, height: 120)
```

---

## 5. Asset Export Automations
We provide an automated Node.js export script under `/assets/export-assets.js` that takes master `.svg` designs and generates the platform-specific output files (`.pdf` for iOS, `.xml` for Android) using vector optimization tools (`svgo` and `svg2vectordrawable`). This ensures that developers never check in unoptimized assets.
