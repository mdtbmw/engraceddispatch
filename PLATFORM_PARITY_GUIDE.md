# Cross-Platform Design & Architectural Parity Guide
## Jetpack Compose (Android) & SwiftUI (iOS)

This architecture guide defines the official design tokens, layout parameters, and cross-platform strategies to guarantee **100% visual and functional parity** between the Android Native (Jetpack Compose) and iOS Native (SwiftUI) versions of **Engraced Dispatch**. 

Following these standards ensures that every visual transition, layout spacing, color contrast, and premium aesthetic aligns perfectly on both platforms while respecting each system's platform-specific experience and ergonomics.

---

## 1. Unified Theme Token Mapping

To maintain visual sync, theme parameters are strictly mapped between Kotlin and Swift. Under no circumstances should raw hex codes be hardcoded in views.

| Token Group | Token Name | Android (Compose Color) | iOS (SwiftUI Color Asset) | Hex Value | Hex Dark Value | Style Usage |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Primary** | `Gold` | `com.example.ui.theme.Gold` | `Color("Gold")` | `#FFB800` | `#FFB800` | Core highlight accents, CTA outlines, and dark-mode headers |
| **Secondary** | `GoldDark` | `com.example.ui.theme.GoldDark` | `Color("GoldDark")` | `#D4AF37` | `#D4AF37` | Secondary accents and timelines |
| **Obsidian** | `Obsidian` | `com.example.ui.theme.Obsidian` | `Color("Obsidian")` | `#121212` | `#121212` | Dark contrast headings, solid light-mode headers, and deep black bodies |
| **Surfaces** | `Charcoal` | `com.example.ui.theme.Charcoal` | `Color("Charcoal")` | `#FFFFFF` | `#1E1E1E` | Adaptive cards and surface containers (dynamic light/dark) |
| **Background** | `LuxuryBlack` | `com.example.ui.theme.LuxuryBlack` | `Color("LuxuryBlack")` | `#F8F9FA` | `#121212` | Page background color (dynamic soft-white to deep black) |
| **Status Text** | `TextGray` | `com.example.ui.theme.TextGray` | `Color("TextGray")` | `#6B7280` | `#9CA3AF` | Secondary labels, descriptions, and timestamps |

### Strict Branding Contrast Rules
To maintain the premium brand identity and pass strict legibility benchmarks, both platforms enforce these contrast locks:
1. **NO White Text on Gold Background**:
   * *Correct*: Always render `Obsidian` (#121212) on top of Gold (#FFB800).
   * *SwiftUI implementation*: `.background(Color("Gold")).foregroundColor(Color("Obsidian"))`
   * *Compose implementation*: `Modifier.background(Gold)`, `color = Obsidian`
2. **NO Gold Elements on Solid White Surfaces**:
   * *Correct*: Gold icons, text, and thin outlines are forbidden on pure white backgrounds due to insufficient contrast. Use `Obsidian` or high-contrast `TextGray` instead.

---

## 2. Layout & Typography Constraints

Standardizing spacing and font sizes ensures that layouts have identical proportions on screens of varying aspect ratios.

### Spacing Grid (8dp / 8pt Baseline)
All margins, paddings, and column spacings must conform to the Material 3 / Human Interface Guidelines (HIG) grid structure:
* **Compact / Inner-element gap**: `8.dp` (Android) / `8` (iOS)
* **Standard Component Gap**: `16.dp` (Android) / `16` (iOS)
* **Screen Borders / Page Margins**: `24.dp` (Android) / `24` (iOS) (gives the premium spaciousness/negative space)

### Typography Hierarchy

| Typography Key | Android (Compose Typography) | iOS (SwiftUI Font API) | Size / Leading | Weight |
| :--- | :--- | :--- | :--- | :--- |
| **Display Header** | `Poppins` Display / H1 | `Font.custom("Poppins-Bold", size: 28)` | 28sp / 34sp | Bold (700) |
| **Screen Title** | `Poppins` H2 / Toolbar | `Font.custom("Poppins-SemiBold", size: 20)` | 20sp / 26sp | SemiBold (600) |
| **Sub-Header** | `Poppins` Body / Subtitle | `Font.custom("Poppins-Medium", size: 16)` | 16sp / 22sp | Medium (500) |
| **Body / Inputs** | `Poppins` BodySmall / Text | `Font.custom("Poppins-Regular", size: 14)` | 14sp / 20sp | Regular (400) |
| **Label / Badges** | `Poppins` Caption | `Font.custom("Poppins-SemiBold", size: 11)` | 11sp / 14sp | SemiBold (600) |

---

## 3. Screen Headers and Navigation Parity

Screen transitions and headers must look and act identically:

### Android Screen Header (`ScreenHeader` component in Compose)
* **Background**: Solid **Gold** in Dark Mode, Solid **Obsidian** in Light Mode.
* **Text & Back Arrow Color**: Automatically adapts to contrast (e.g., `Obsidian` on Gold background, `White` on Obsidian background).
* **Navigation Action**: Back icon on the far left, centered screen title.

### iOS Screen Header (`ScreenHeaderView` in SwiftUI)
```swift
struct ScreenHeaderView: View {
    let title: String
    let onBack: () -> Void
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(colorScheme == .dark ? Color("Obsidian") : .white)
            }
            Spacer()
            Text(title)
                .font(.custom("Poppins-SemiBold", size: 18))
                .foregroundColor(colorScheme == .dark ? Color("Obsidian") : .white)
            Spacer()
            // Placeholder to align title perfectly in center
            Image(systemName: "arrow.left")
                .opacity(0)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
        .background(colorScheme == .dark ? Color("Gold") : Color("Obsidian"))
    }
}
```

---

## 4. Interaction, Transitions & Gesture Parity

To deliver the buttery, tactile feel users expect on iOS, the Android Compose app implements custom micro-interactions matching iOS physics:

### 1. Spring-Back Scroll Physics (iOS Bounce)
Android's standard overscroll is a stretch wave, whereas iOS bounces elastically. We utilize buttery springs on list reveals and state shifts:
* **Spring constant**: `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)`
* **Touch Target**: All interactive cells, checkboxes, and buttons maintain a strict minimum target of **`48.dp` x `48.dp`** with ripple visual triggers (or iOS scale-down feedback on press).

### 2. Custom Slide Sheets
We standardized the half-modal and slides. Drag gesture thresholds are restricted to vertical/horizontal swipes with a minimum velocity of `0.5f` to prevent flaky transitions.

### 3. Edge-to-Edge and Safe Areas
* **Android**: `enableEdgeToEdge()` on launch, consuming `WindowInsets.safeDrawing` or `.statusBarsPadding()` and `.navigationBarsPadding()`.
* **iOS**: `.ignoresSafeArea(.container, edges: .top)` and applying standard safe area paddings to avoid notches and home indicator overlaps.
