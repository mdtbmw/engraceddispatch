# ESDispatch Micro-UI, Component Quality & Visual Polish Audit

Date: 2026-09-09  
Scope: Customer mobile app, rider mobile surfaces, admin web dashboard, marketplace surfaces, reusable UI components, and visible micro-interactions.  
Evidence used: source inspection, the two supplied dashboard screenshots, previously created product audit documents, and local repository structure. The pasted audit brief was treated as a user-provided scope document, not as higher-priority instructions.

## 1. Executive judgment

ESDispatch has the bones of a premium logistics product, but the micro-UI quality is not yet premium. The problem is not only missing features. The problem is that many small UI pieces are composed as isolated one-off layouts: delivery cards, recent history cards, admin stat cards, marketplace product cards, status badges, rider assignment rows, icon buttons, segmented filters, toasts, and CTA strips all use slightly different density, spacing, text hierarchy, radius, action placement, and state behavior.

That makes the app feel assembled instead of designed. Users can still complete some actions, but the interface asks them to work too hard to understand what matters, what state an order is in, and what they should do next. For a dispatch business operating in Benin City, where delivery decisions are urgent and often made under pressure, this hurts trust and speed.

The supplied screenshots show the issue clearly:

- The active delivery cards use a large rounded cream surface but the content does not use the card space intelligently.
- The header overlaps and visually crushes the filter row and scroll content.
- The route/timeline area has weak hierarchy, awkward address wrapping, and unbalanced left/right columns.
- The recent history cards leave too much empty space while long route text is forced into a narrow column.
- The "Book This Route Again" action is visually detached from the order information.
- The bottom dock is stylish, but it consumes a lot of screen space and competes with list content near the bottom.

Source inspection confirms that this is systemic. Dashboard cards are built inside `mobile/app/src/main/java/com/esdispatch/ui/screens/DashboardScreen.kt`, marketplace product cards are separately built inside `mobile/app/src/main/java/com/esdispatch/ui/screens/MarketplaceScreen.kt`, bottom navigation lives in `mobile/app/src/main/java/com/esdispatch/ui/components/Components.kt`, and the admin dashboard defines many UI primitives directly inside `src/app/engdadmin/AdminDashboard.tsx`. These components share visual intentions, but not enough common structure.

## 2. Evidence limits

This audit is intentionally blunt, but it should be read with clear evidence labels.

- **Screenshot-confirmed** means the issue is visible in the attached screenshots.
- **Source-confirmed** means the issue is visible in the repository source.
- **Pattern-inferred** means the source shows a repeated implementation pattern that likely affects runtime UI.
- **UNVERIFIED - REQUIRES RUNTIME TESTING** means the issue depends on live behavior, device size, data volume, network timing, or scrolling interaction that was not tested in a running device/browser session during this pass.

No browser-based admin walkthrough or device runtime walkthrough was completed in this pass. The audit is therefore strongest on component structure, visual hierarchy, layout resilience, and source-confirmed design-system issues.

## 3. Component inventory

| Component / pattern | Location | What it displays | Interaction model | Reusability assessment | Redesign need |
|---|---|---|---|---|---|
| Customer dashboard header | `DashboardScreen.kt` around header overlay | Avatar, greeting, notification icon, brand treatment | Sticky/overlay header, notification tap | Should be a shared premium header primitive with safe-area rules | High |
| Customer active filter segmented control | `DashboardScreen.kt:1192-1237` | "All Active", "In Transit" | Tap to filter local list | Should be shared segmented control | High |
| Active shipment parcel card | `DashboardScreen.kt`, `ParcelCard` area near active list and route CTA | ID, type, status, route, timeline, actions | Card tap, quick view, copy ID, archive swipe | Should become shared `DeliveryCard` with variants | Critical |
| Recent history card | `DashboardScreen.kt:1387-1477` | item, ID, date, route, price, delivered status, rebook CTA | CTA tap rebooks route | Should become shared `HistoryOrderCard` or `DeliveryCard(history)` | Critical |
| Empty active shipments state | `DashboardScreen.kt:1288-1341` | empty illustration, explanatory text, CTA | CTA books shipment | Should be shared empty state | Medium |
| Dashboard skeleton card | `DashboardScreen.kt:1241-1286` | shimmer placeholders | Loading state | Needs shared skeleton system | Medium |
| Bottom capsule navigation | `Components.kt:253-458` | home/order/tracking/profile plus central action | Tap nav items and central action | Shared already, but needs better state and responsive rules | High |
| Bottom nav item | `Components.kt:460+` | icon + label | Tap | Shared, but item sizing/density is rigid | Medium |
| Marketplace product card | `MarketplaceScreen.kt:993-1185` | product image, vendor, rating, price, favorite/cart controls | Tap, swipe, favorite, add cart | Should align to card system and action system | High |
| Marketplace floating cart bar | `MarketplaceScreen.kt:390-416` | cart count and checkout CTA | Tap checkout | Should use shared floating action bar | Medium |
| Marketplace favorites sheet | `MarketplaceScreen.kt:865-980` | favorite products and empty state | bottom sheet, close, item actions | Should use shared bottom sheet, list row, empty state | Medium |
| Product/store horizontal cards | `DashboardScreen.kt`, `MarketplaceScreen.kt` | recommended shops/products | taps, carousel/list | Should use consistent image card ratio and badge rules | Medium |
| Booking form section cards | `BookingFormScreen.kt`, `ExpressBookingScreen.kt`, `EconomyBookingScreen.kt`, `MultiBookingScreen.kt`, `BatchBookingScreen.kt` | address, package details, pricing, CTA | inputs, validation, bottom CTA | Likely duplicated; needs shared form sections | High |
| Rider assignment card | `DashboardScreen.kt:3175+`, `RiderScreens.kt` | rider avatar, plate, rating, current assignment | visible status; rider actions elsewhere | Should share rider identity and assignment components | High |
| OTP / POD instruction block | `DashboardScreen.kt:3300+`, rider screens | secure code, proof of delivery guidance | scan/verify/upload flows | Needs shared security instruction component | Medium |
| Admin stat card | `AdminDashboard.tsx:198-203` | label, metric, subtext, icon | hover effect | Shared within admin only; tokenization weak | Medium |
| Admin quick button | `AdminDashboard.tsx:205-208` | action label and description | click, disabled/loading | Needs action priority variants | Medium |
| Admin section card | `AdminDashboard.tsx:210-214` | titled content container | static container | Should be admin-wide section primitive | Medium |
| Admin inline edit | `AdminDashboard.tsx:216-222` | value editing | click-to-edit, blur save | Needs clearer state and validation feedback | High |
| Admin confirm modal | `AdminDashboard.tsx:223-234` | destructive confirmation | confirm/cancel | Needs modal system and keyboard/focus QA | Medium |
| Admin search input | `AdminDashboard.tsx:236-241` | search field | typed filter | Should be shared input primitive | Medium |
| Admin save button | `AdminDashboard.tsx:243-248` | save action | click/loading | Should use button variants | Medium |
| Admin custom select | `AdminDashboard.tsx:251-284` | selectable options | custom dropdown | Risky inside tables/overflow; needs robust select pattern | High |
| Admin toast container | `AdminDashboard.tsx:287-295` | stacked notifications | passive timed notification | Needs dismiss, dedupe, and lifecycle design | High |
| Admin active bookings row | `AdminDashboard.tsx:740-768` | active delivery list with rider/time | row click/hover, tab jump | Should support inline dispatch actions | Critical |
| Admin active request pop-up/list | `AdminDashboard.tsx:695-734` evidence | request details and assignment prompt | tab switching to shipments | Needs decision-ready pop-up actions | Critical |
| Admin fleet summary card | `AdminDashboard.tsx:770-790` | fleet count, active drivers, revenue | assign riders CTA | Needs better operational meaning | High |

## 4. Screenshot-specific critique

### Screenshot 1: active shipments dashboard

The screen has an attractive black/gold direction, but the composition is fighting itself. The header takes a large share of the first viewport and appears to overlap the segmented filter row. The filter labels are partially hidden, so the control looks broken before the user even reaches the delivery cards.

The active shipment cards have several micro-layout problems. The ID line is centered, but the status sits on the far right and the parcel illustration floats at the edge, so the top row lacks a clean grid. The copy and pin icons are useful, but their placement makes the header feel like loose objects rather than a composed metadata row.

The route timeline is the weakest part. The pickup address, middle status, and destination address are distributed across a wide card, but the information does not align to a deliberate grid. The center circle and "To" label consume attention without explaining the current delivery decision. The status chips "Booked" and "Pending" are visually heavy, yet they do not show what happens next. The destination circle is pale and looks disabled, but the top-right status already says pending, so the state is duplicated without giving the customer more clarity.

The card height is large, but the extra height does not create calm. It creates vacancy. Long addresses wrap into uneven chunks, which makes Benin City routes look messy even when they are valid. A premium route card should compress pickup/dropoff into clear labeled blocks, use a purposeful progress rail, and make the next action obvious.

### Screenshot 2: recent history cards

The recent history cards are a perfect example of information-dense content being put into a layout that does not protect hierarchy. The title, ID/date, route, price, status, and rebook action are all valid, but the layout makes them compete. The left side has an invisible image column or reserved space, while the route text is pushed into a narrow middle column. The right side holds price and status, but the delivered badge is too small to function as a confident state marker.

The lower CTA band feels attached after the fact. "Book This Route Again" is useful, but centering it under every card creates repetition and adds height without making the action faster. A better pattern would make the whole card scannable first, then place rebook as a stable secondary action aligned with the card grid.

The clipped "Track Delivery Details" row at the top of the screenshot is also serious. It suggests the header, content padding, sticky overlay, or scroll state does not account for safe area and previous card height reliably. Even if this only happens on certain device sizes, it damages trust because users immediately see content trapped under chrome.

## 5. Micro issue catalog

### MICRO-001

**Category:** Layout / overlay  
**Severity:** P1  
**Component:** Dashboard header overlay  
**Location:** `DashboardScreen.kt:317`, `DashboardScreen.kt:599`, `DashboardScreen.kt:1482-1488`; screenshot 1 and 2  
**Current problem:** The fixed header visually overlaps or crowds the content below it. In the screenshots, the segmented control and a previous "Track Delivery Details" row appear partially hidden under the header.  
**Why it matters:** Users interpret clipped content as a broken app. It also reduces trust in scrolling and makes the dashboard feel unstable.  
**Expected design:** The header should own its space, respect status bar and safe area, and release content cleanly below its rounded bottom edge.  
**Recommended fix:** Replace dynamic overlay compensation with a measured header scaffold: expose header height through layout, add content top inset, and test collapsed/expanded states on small and tall screens.  
**Reusability:** Shared. Any screen using large overlay headers needs the same safe-area rule.  
**Related components:** Dashboard header, rider dashboard header, marketplace header, sticky admin mobile header if added.  
**Acceptance criteria:** No filter, card, CTA, or section heading can be clipped by header at initial load, after scroll, after refresh, or after returning from another screen.

### MICRO-002

**Category:** Segmented control  
**Severity:** P1  
**Component:** Active shipment filter  
**Location:** `DashboardScreen.kt:1192-1237`; screenshot 1  
**Current problem:** The filter row is visually trapped under the header and lacks enough vertical breathing room.  
**Why it matters:** Filters define what list the user is viewing. If they look clipped, users lose confidence in the active state.  
**Expected design:** A segmented control with a clear selected pill, readable labels, and stable spacing from the header and list.  
**Recommended fix:** Create a shared `SegmentedFilterBar` with fixed min height, selected indicator animation, and container padding based on design tokens.  
**Reusability:** Shared. The same pattern should serve customer dashboard, order logs, marketplace filters, and admin tabs where appropriate.  
**Related components:** Order logs filters, marketplace category chips, admin tab filters.  
**Acceptance criteria:** Labels never clip, selected state is readable, and touch target height is at least 44dp/44px.

### MICRO-003

**Category:** Information hierarchy  
**Severity:** P1  
**Component:** Active shipment card  
**Location:** `ParcelCard` usage in `DashboardScreen.kt:1345-1363`; screenshot 1  
**Current problem:** The card does not answer "what should I know first?" quickly. ID, icons, status, package icon, chips, route endpoints, and CTA compete.  
**Why it matters:** Active shipments are time-sensitive. Customers need current state, pickup/dropoff, rider/status, and next action in seconds.  
**Expected design:** Header row: package type and ID left, status badge right. Body: route summary and progress. Footer: one clear primary action.  
**Recommended fix:** Redesign the active card as a strict internal grid with named slots: `titleMeta`, `status`, `route`, `progress`, `riderPreview`, `primaryAction`.  
**Reusability:** Shared. This should become the core `DeliveryCard` pattern.  
**Related components:** Admin active booking cards, rider assignment cards, tracking summary cards.  
**Acceptance criteria:** A user can identify order type, current state, route, and action in under 3 seconds without reading every label.

### MICRO-004

**Category:** Card density  
**Severity:** P2  
**Component:** Active shipment card  
**Location:** Screenshot 1  
**Current problem:** The card uses a lot of vertical space, but the important content still feels cramped because it is spread poorly.  
**Why it matters:** Large cards reduce list scan speed. On mobile, the user sees fewer active shipments and must scroll more.  
**Expected design:** Space should be used to separate meaningful groups, not to create empty zones.  
**Recommended fix:** Reduce uncontrolled vertical whitespace; use a two-zone body with route and state arranged by hierarchy.  
**Reusability:** Shared delivery card system.  
**Related components:** Recent history cards, marketplace product rows, admin delivery rows.  
**Acceptance criteria:** At least two active shipments can fit comfortably on common 360x800 screens without clipped content.

### MICRO-005

**Category:** Route display  
**Severity:** P1  
**Component:** Route timeline  
**Location:** `DashboardScreen.kt:2460-2502`; screenshot 1  
**Current problem:** Address labels wrap unevenly around a centered timeline marker. The current "To" marker is not meaningful enough to justify its visual weight.  
**Why it matters:** Route clarity is central to dispatch. Messy route text makes real Benin City addresses look unreliable.  
**Expected design:** Pickup and dropoff should be clear labeled blocks with consistent max lines and optional expand affordance.  
**Recommended fix:** Create `RouteDisplay` with pickup/dropoff labels, icons, max lines, and progressive disclosure for full address. Use the timeline rail only when it improves comprehension.  
**Reusability:** Shared across active card, history card, admin shipment drawer, rider manifest, tracking screen.  
**Related components:** Booking summary, order history, admin request pop-up.  
**Acceptance criteria:** Long addresses wrap predictably and do not push badges, price, or actions out of alignment.

### MICRO-006

**Category:** Status design  
**Severity:** P1  
**Component:** Status chips/badges  
**Location:** `DashboardScreen.kt:2463-2475`, `DashboardScreen.kt:1432-1442`, `AdminDashboard.tsx:187-193`  
**Current problem:** Status styling varies by context. Some statuses are tiny, some are dark chips, some are pastel labels, and some duplicate other state labels.  
**Why it matters:** Status is the operational language of the product. Inconsistency makes users and admins interpret the same state differently.  
**Expected design:** A unified `StatusBadge` with variants for customer, rider, and admin, using the same labels and visual progression.  
**Recommended fix:** Define status tokens for `Queued`, `Received`, `Assigned`, `Reserved Next`, `Picked Up`, `In Transit`, `Arrived`, `Delivered`, `Cancelled`, and `Issue`.  
**Reusability:** Shared.  
**Related components:** Dashboard cards, admin tables, rider manifest, notification rows, tracking screen.  
**Acceptance criteria:** Same status always has same label, color family, icon, and explanation across customer/rider/admin surfaces.

### MICRO-007

**Category:** Icon placement  
**Severity:** P2  
**Component:** Active card top action icons  
**Location:** Screenshot 1; copy/location actions in active card  
**Current problem:** Copy and location icons sit near the ID but do not read as a coherent action group.  
**Why it matters:** Tiny icon controls without labels can be misread, especially when placed in a dense card header.  
**Expected design:** Utility actions should be grouped in a small trailing action cluster with clear hit targets and optional tooltips/feedback.  
**Recommended fix:** Use shared `IconActionButton` with 40dp hit area, neutral surface, active/pressed state, and toast feedback.  
**Reusability:** Shared.  
**Related components:** Marketplace favorite/cart icon buttons, admin row action icons, notification controls.  
**Acceptance criteria:** Icon buttons align to the card grid and all tappable icons have at least a 40dp touch area.

### MICRO-008

**Category:** Illustration placement  
**Severity:** P3  
**Component:** Parcel/package icon  
**Location:** Screenshot 1  
**Current problem:** The package illustration is pushed to the right edge and appears crowded or partially clipped.  
**Why it matters:** Decorative graphics should support the content. When clipped, they look accidental.  
**Expected design:** Decorative icons should sit inside a stable reserved cell or be removed from information-dense cards.  
**Recommended fix:** Reserve a 48dp visual slot in the top-right only if it does not collide with status. Otherwise use a small package glyph next to title.  
**Reusability:** Active cards and empty states.  
**Related components:** Marketplace image placeholders, order empty states.  
**Acceptance criteria:** No decorative icon touches or exits card padding.

### MICRO-009

**Category:** Typography  
**Severity:** P1  
**Component:** Recent history card route text  
**Location:** `DashboardScreen.kt:1421-1427`; screenshot 2  
**Current problem:** Route text is small, semi-bold, and forced into multiple lines inside a narrow column.  
**Why it matters:** It is hard to scan, and it competes with price/status.  
**Expected design:** Route text should be secondary but readable, with pickup/dropoff visually separated.  
**Recommended fix:** Use `AddressPair` with two lines max per endpoint, "From" and "To" micro-labels, and a collapse/expand rule for long addresses.  
**Reusability:** Shared.  
**Related components:** Active delivery card, order logs, admin shipment rows.  
**Acceptance criteria:** Long routes remain readable without pushing the price column into cramped spacing.

### MICRO-010

**Category:** Card grid  
**Severity:** P1  
**Component:** Recent history card  
**Location:** `DashboardScreen.kt:1387-1477`; screenshot 2  
**Current problem:** The main row uses image slot + content column + price column, but the screenshot shows empty left space and crowded route text.  
**Why it matters:** The layout wastes space while making important content harder to read.  
**Expected design:** History cards should prioritize title, amount, status, route, and rebook action in a stable grid.  
**Recommended fix:** If image is absent or decorative, remove the image slot. Use a two-column top row with order title left and amount/status right, then full-width route block.  
**Reusability:** Shared history card.  
**Related components:** Order logs cards, admin completed deliveries list.  
**Acceptance criteria:** History card content starts on a consistent left edge and route text uses the full available width below metadata.

### MICRO-011

**Category:** Action design  
**Severity:** P2  
**Component:** "Book This Route Again" CTA  
**Location:** `DashboardScreen.kt:1447-1475`; screenshot 2  
**Current problem:** The CTA is centered in a separated lower band and feels detached from the order content.  
**Why it matters:** Rebooking is useful, but the repeated band adds visual bulk and makes the list feel heavy.  
**Expected design:** Rebook should be a clear secondary action attached to the card's action row.  
**Recommended fix:** Convert the bottom band into a compact footer with left-aligned icon/text or a trailing secondary button aligned to the card grid.  
**Reusability:** History/order cards.  
**Related components:** Admin row manage links, marketplace cart actions.  
**Acceptance criteria:** CTA is visually connected to the delivery it affects and does not add unnecessary card height.

### MICRO-012

**Category:** Currency formatting  
**Severity:** P2  
**Component:** Price display  
**Location:** `DashboardScreen.kt:1430`, `MarketplaceScreen.kt:1164-1168`  
**Current problem:** Currency is formatted independently in different components and can create inconsistent decimals, sizes, and alignment.  
**Why it matters:** Price is a trust signal. Inconsistent money formatting makes the product feel less mature.  
**Expected design:** A shared `PriceDisplay` should format NGN consistently by context.  
**Recommended fix:** Centralize NGN formatting with variants: compact no decimals for product cards, full two-decimal where accounting matters, tabular numerals for admin.  
**Reusability:** Shared mobile/admin logic.  
**Related components:** Wallet, marketplace, delivery cards, admin revenue cards.  
**Acceptance criteria:** Same amount renders consistently across customer, rider, and admin views unless a documented variant applies.

### MICRO-013

**Category:** Color hierarchy  
**Severity:** P2  
**Component:** Gold text on light cards  
**Location:** `DashboardScreen.kt:1469-1472`, `MarketplaceScreen.kt:1141-1145`, `AdminDashboard.tsx:202`, `AdminDashboard.tsx:207`, `AdminDashboard.tsx:212`  
**Current problem:** Gold is used as text on light surfaces in multiple places. The project rules warn against gold on white/light surfaces.  
**Why it matters:** Gold can look premium on dark backgrounds, but on cream/white it often becomes low-contrast and visually noisy.  
**Expected design:** On light cards, use Obsidian/TextGray for text and reserve Gold for filled controls, small icons, or dark-surface highlights.  
**Recommended fix:** Add lintable color usage guidelines and component tokens: `accentTextOnDark`, `accentFill`, `accentIcon`, `bodyText`, `mutedText`.  
**Reusability:** Global.  
**Related components:** CTAs, section headings, vendor name text, admin stat subtext.  
**Acceptance criteria:** No critical label or CTA relies on gold text directly on light surfaces.

### MICRO-014

**Category:** Touch target  
**Severity:** P2  
**Component:** Marketplace favorite/cart icon buttons  
**Location:** `MarketplaceScreen.kt:1172-1185` and following add-cart icon area  
**Current problem:** Icon buttons are 34dp, below the comfortable 44dp touch target guideline.  
**Why it matters:** Small controls create accidental taps and frustration, especially on moving phones.  
**Expected design:** Visual icon may be 18-22dp, but hit area should be at least 44dp.  
**Recommended fix:** Use a shared `IconActionButton` with min interactive size independent of visual icon size.  
**Reusability:** Shared.  
**Related components:** dashboard copy icon, bell button, admin row icons.  
**Acceptance criteria:** All icon-only actions pass a 44dp/44px minimum hit-area check.

### MICRO-015

**Category:** Gesture discoverability  
**Severity:** P3  
**Component:** Marketplace product swipe actions  
**Location:** `MarketplaceScreen.kt:1001-1094`  
**Current problem:** Product cards support swipe right for favorite and swipe left for cart, but the UI also shows visible favorite/cart buttons. The gesture may be hidden and duplicate visible controls.  
**Why it matters:** Hidden gestures are fine as shortcuts, but they should not be required or conflict with scrolling.  
**Expected design:** Swipe actions should be optional accelerators with onboarding hint or peek affordance.  
**Recommended fix:** Add a subtle first-use hint or keep swipe actions but ensure visible controls remain primary.  
**Reusability:** Marketplace card only, but gesture rules should be global.  
**Related components:** Swipe-to-archive delivery card.  
**Acceptance criteria:** A first-time user can complete favorite/add-cart without discovering swipe.

### MICRO-016

**Category:** Image fallback  
**Severity:** P2  
**Component:** Marketplace product card image  
**Location:** `MarketplaceScreen.kt:1102-1128`  
**Current problem:** Broken/missing images fall back to an icon, but image aspect, placeholder style, sold-out overlay, and loading state are not part of a broader image system.  
**Why it matters:** Marketplace quality depends heavily on image consistency. Bad placeholders make the marketplace look incomplete.  
**Expected design:** Shared image container with loading shimmer, error fallback, object fit, radius, and state overlay rules.  
**Recommended fix:** Create `ProductImageFrame` and `AvatarFrame` primitives.  
**Reusability:** Marketplace, stores, rider avatars, customer avatars.  
**Related components:** dashboard recommended shops, rider profile, admin user table avatars.  
**Acceptance criteria:** Missing image state looks intentional and consistent across all image-bearing cards.

### MICRO-017

**Category:** Text truncation  
**Severity:** P2  
**Component:** Marketplace product card title/vendor  
**Location:** `MarketplaceScreen.kt:1133-1146`  
**Current problem:** Product title is `maxLines = 1`; vendor text has no visible max-line rule in the snippet. Long names will either truncate too aggressively or disturb layout.  
**Why it matters:** Benin vendors/products can have long names. Truncating core names without a detail affordance harms comprehension.  
**Expected design:** Titles get 1-2 lines depending on card type; vendor names get one line with tooltip/detail view access.  
**Recommended fix:** Define text max-line rules by component variant and use consistent overflow behavior.  
**Reusability:** Global text system.  
**Related components:** delivery item names, admin user names, store cards.  
**Acceptance criteria:** Long product and vendor names do not overlap price/actions and can be fully viewed on detail.

### MICRO-018

**Category:** Navigation labeling  
**Severity:** P3  
**Component:** Bottom navigation "Order" label  
**Location:** `Components.kt:379-392`; screenshot 1 and 2  
**Current problem:** The label is singular "Order" while the target is order logs.  
**Why it matters:** Small naming mismatches make navigation feel less polished.  
**Expected design:** Bottom labels should match destinations: Home, Orders, Tracking, Profile.  
**Recommended fix:** Change label to "Orders" if space allows; test bottom dock width.  
**Reusability:** Bottom nav.  
**Related components:** Admin tabs and mobile section labels.  
**Acceptance criteria:** Nav labels match screen titles and user mental model.

### MICRO-019

**Category:** Bottom navigation density  
**Severity:** P2  
**Component:** Bottom capsule navigation  
**Location:** `Components.kt:286-456`; screenshots  
**Current problem:** The bottom dock is visually strong and large; list content approaches it closely, making the last cards feel crowded.  
**Why it matters:** Persistent navigation should support movement, not compete with the content being scanned.  
**Expected design:** The dock should reserve enough bottom content inset and keep labels legible.  
**Recommended fix:** Standardize bottom list padding based on measured nav height and safe-area inset.  
**Reusability:** Global mobile scaffold.  
**Related components:** all list screens using bottom nav.  
**Acceptance criteria:** Last list item can scroll fully above the dock with at least 16dp breathing room.

### MICRO-020

**Category:** Motion  
**Severity:** P2  
**Component:** Bottom nav central action  
**Location:** `Components.kt:433-447`  
**Current problem:** The central plus has breathing pulse and tactile press. This is good, but constant pulsing can draw attention away from active delivery lists if not restrained.  
**Why it matters:** Motion should direct attention only when action is relevant. Constant motion can become visual noise.  
**Expected design:** Pulse should be subtle, paused during scroll or when modal overlays are open, and reduced when accessibility motion settings require it.  
**Recommended fix:** Gate breathing pulse by screen state and reduced-motion preference.  
**Reusability:** Motion system.  
**Related components:** tracking marker breathing pulse, arrival alert, dispatch broadcast.  
**Acceptance criteria:** Motion feels alive without becoming the most dominant element on every screen.

### MICRO-021

**Category:** State feedback  
**Severity:** P1  
**Component:** Notifications/toasts  
**Location:** `AdminDashboard.tsx:287-295`, `DashboardScreen.kt:254-265`; user-reported repeated notifications  
**Current problem:** Toasts/notifications can stack or reappear after closing, based on user report and source showing passive toast containers.  
**Why it matters:** Repeating dismissed notifications makes the app feel uncontrolled and can hide urgent operational messages.  
**Expected design:** Notifications need lifecycle: unread, seen, dismissed, expired, actionable, resolved.  
**Recommended fix:** Add dedupe keys, dismiss persistence, per-event TTL, and a notification center state model.  
**Reusability:** Global.  
**Related components:** admin toast container, mobile in-app notification, push notification preferences.  
**Acceptance criteria:** Closing a notification prevents the same non-critical event from resurfacing unless the underlying event changes.

### MICRO-022

**Category:** Admin workflow  
**Severity:** P0  
**Component:** Admin active request pop-up/list  
**Location:** `AdminDashboard.tsx:695-734`; previous user example  
**Current problem:** Admin sees request summary but must jump to shipments tab to assign rider or manage delivery.  
**Why it matters:** Dispatch operations need fast decisions. Tab-jumping forces admins to rediscover the same order and increases mistakes.  
**Expected design:** Active request pop-up should include all decision-critical details and inline actions: assign/reserve rider, queue, change valid status, contact, view map, print waybill.  
**Recommended fix:** Replace the read-only pop-up with a `DispatchDecisionDrawer` that can perform authorized next actions in place.  
**Reusability:** Admin dispatch system.  
**Related components:** active bookings list, shipments tab, rider assignment modal.  
**Acceptance criteria:** Admin can open a request and complete the next valid dispatch action without leaving the pop-up/drawer.

### MICRO-023

**Category:** Admin list ordering  
**Severity:** P0  
**Component:** Active requests list  
**Location:** Admin dashboard active request flow; user-reported issue  
**Current problem:** Active requests are not clearly organized by first-come-first-serve or operational urgency.  
**Why it matters:** Unclear ordering causes unfair dispatching and missed SLA priority.  
**Expected design:** Default ordering should be FCFS within priority buckets, with visible sort reason.  
**Recommended fix:** Add queue position, request time, SLA timer, service type priority, and manual sort controls.  
**Reusability:** Admin and customer queue status.  
**Related components:** customer queued state, rider preassignment queue.  
**Acceptance criteria:** Every request shows why it appears where it appears.

### MICRO-024

**Category:** Admin status logic  
**Severity:** P0  
**Component:** Status update controls  
**Location:** `AdminDashboard.tsx:195-196`, status options/source patterns; user-reported issue  
**Current problem:** Admin can change delivery status without assigning a rider.  
**Why it matters:** This breaks operational truth. Customers can see progress that has no rider behind it.  
**Expected design:** Status transitions should be guarded by business rules.  
**Recommended fix:** Disable invalid transitions and explain why: "Assign or reserve rider before moving beyond Received/Queued."  
**Reusability:** Backend, admin, rider, customer status displays.  
**Related components:** rider app authorized statuses, customer tracking.  
**Acceptance criteria:** A delivery cannot enter rider-dependent states without assigned/reserved rider data.

### MICRO-025

**Category:** Admin select component  
**Severity:** P1  
**Component:** Custom select  
**Location:** `AdminDashboard.tsx:251-284`  
**Current problem:** The dropdown is custom-built with absolute positioning and may be fragile in scrollable/overflow tables.  
**Why it matters:** Admin status and assignment controls must be reliable. A dropdown hidden behind a table or clipped by overflow directly blocks operations.  
**Expected design:** A robust select/popover with portal positioning, keyboard support, focus management, and viewport collision handling.  
**Recommended fix:** Move admin selects to a shared popover/select primitive or use an accessible headless component.  
**Reusability:** Admin-wide.  
**Related components:** status select, rider select, marketplace category select, settings selects.  
**Acceptance criteria:** Dropdowns are never clipped by parent containers and work with keyboard and pointer.

### MICRO-026

**Category:** Admin table density  
**Severity:** P1  
**Component:** Active bookings table/list  
**Location:** `AdminDashboard.tsx:740-768`  
**Current problem:** The list row compresses delivery task, rider, and estimated time/date into a table-like row, but it lacks inline next action and complete route context.  
**Why it matters:** Admin cannot make dispatch decisions from the row alone.  
**Expected design:** Operational rows should show status, queue age, pickup, dropoff, rider availability, and next valid action.  
**Recommended fix:** Use a dispatch row/card hybrid with progressive detail and inline action chips.  
**Reusability:** Admin dispatch surfaces.  
**Related components:** active request drawer, shipments table.  
**Acceptance criteria:** Admin can triage top 8 active requests without opening every detail page.

### MICRO-027

**Category:** Admin stat cards  
**Severity:** P2  
**Component:** `StatCard`  
**Location:** `AdminDashboard.tsx:198-203`  
**Current problem:** Metrics use large values, tiny labels, gold subtext, hover scale, and generic card styling. The subtext can be too low-contrast on light cards.  
**Why it matters:** Admin metrics should be calm, precise, and comparable.  
**Expected design:** Stat cards need consistent metric hierarchy, neutral subtext, trend/alert variants, and no distracting hover movement.  
**Recommended fix:** Create metric card variants: normal, warning, urgent, revenue, fleet. Use tokenized text and spacing.  
**Reusability:** Admin dashboard.  
**Related components:** fleet summary, revenue card, mobile stats grid.  
**Acceptance criteria:** Metrics are readable at a glance and do not depend on gold text for meaning.

### MICRO-028

**Category:** Admin quick actions  
**Severity:** P2  
**Component:** `QuickBtn`  
**Location:** `AdminDashboard.tsx:205-208`  
**Current problem:** Quick buttons use identical visual weight for actions that likely have different risk and importance.  
**Why it matters:** Dangerous, setup, routine, and promotional actions should not look equal.  
**Expected design:** Quick actions should have priority variants and destructive/setup labels.  
**Recommended fix:** Add `ActionTile` variants: primary, secondary, setup, destructive, disabled, loading.  
**Reusability:** Admin-wide.  
**Related components:** seed utilities, broadcast controls, fleet sync actions.  
**Acceptance criteria:** Admin can distinguish routine action from system-altering action visually before reading descriptions.

### MICRO-029

**Category:** Inline editing  
**Severity:** P1  
**Component:** `InlineEdit`  
**Location:** `AdminDashboard.tsx:216-222`  
**Current problem:** Click-to-edit silently saves on blur with limited visible edit state and unclear error handling.  
**Why it matters:** Admin data changes affect real users. Silent blur-save can create accidental edits.  
**Expected design:** Inline edits need edit state, save/cancel controls for sensitive fields, validation, and optimistic/error feedback.  
**Recommended fix:** Use inline edit only for low-risk content. For delivery/user state, use explicit save controls and audit logs.  
**Reusability:** Admin-wide.  
**Related components:** marketplace content, user profiles, settings rows.  
**Acceptance criteria:** Admin always knows when a value is editing, saved, failed, or reverted.

### MICRO-030

**Category:** Modal design  
**Severity:** P2  
**Component:** `ConfirmModal`  
**Location:** `AdminDashboard.tsx:223-234`  
**Current problem:** The modal uses strong blur/shadow/radius but no source-confirmed focus trapping, escape behavior, or destructive action detail beyond message text.  
**Why it matters:** Admin confirmations must prevent mistakes, not just look dramatic.  
**Expected design:** Confirm modals need focus management, clear object name, consequence, and safe cancel.  
**Recommended fix:** Standardize modal system with focus trap, keyboard behavior, and risk variants.  
**Reusability:** Admin-wide.  
**Related components:** delete user, delete marketplace item, broadcast override.  
**Acceptance criteria:** Keyboard and screen-reader users can complete or cancel safely; destructive modal names the exact target.

### MICRO-031

**Category:** Loading state  
**Severity:** P2  
**Component:** Dashboard skeleton cards  
**Location:** `DashboardScreen.kt:1241-1286`  
**Current problem:** Skeleton card shape does not match the real active shipment card content structure.  
**Why it matters:** Loading previews should teach the user what is coming. Mismatched skeletons make transitions feel jumpy.  
**Expected design:** Skeletons should mirror final card geometry.  
**Recommended fix:** Create skeleton variants matching `DeliveryCard`, `HistoryCard`, `ProductCard`, and admin row/card structures.  
**Reusability:** Global loading system.  
**Related components:** marketplace product loading, admin loading rows.  
**Acceptance criteria:** When data loads, skeleton-to-content transition does not shift major geometry unexpectedly.

### MICRO-032

**Category:** Empty states  
**Severity:** P2  
**Component:** No active shipments state  
**Location:** `DashboardScreen.kt:1288-1341`, `MarketplaceScreen.kt:339-356`, `MarketplaceScreen.kt:913`  
**Current problem:** Empty states are implemented separately and vary in tone, density, and action design.  
**Why it matters:** Empty states are onboarding moments. Inconsistent empty states feel like placeholders.  
**Expected design:** Shared empty state with icon/illustration, title, explanation, primary action, and optional secondary action.  
**Recommended fix:** Create `EmptyStateCard` with domain-specific copy slots.  
**Reusability:** Global.  
**Related components:** orders, marketplace, favorites, notifications, admin lists.  
**Acceptance criteria:** Empty states feel branded, helpful, and consistent.

### MICRO-033

**Category:** Dynamic data resilience  
**Severity:** P1  
**Component:** Delivery cards and admin rows  
**Location:** screenshots; `DashboardScreen.kt:1421-1427`, `AdminDashboard.tsx:756-764`  
**Current problem:** Long addresses, long receiver names, and long route strings can dominate card rows and break rhythm.  
**Why it matters:** Benin City addresses vary heavily in length and structure. Real data will not stay as tidy as seed data.  
**Expected design:** Components need explicit max lines, wrapping strategy, truncation, and expand behavior.  
**Recommended fix:** Define text resilience rules in design system: names 1 line, address preview 2 lines each, route summary expands on tap/drawer.  
**Reusability:** Global.  
**Related components:** customer cards, admin rows, rider manifest, notifications.  
**Acceptance criteria:** Layout survives long address strings without overlap, clipping, or action displacement.

### MICRO-034

**Category:** Accessibility  
**Severity:** P1  
**Component:** Tiny labels and badges  
**Location:** `DashboardScreen.kt:1438`, `MarketplaceScreen.kt:1152-1154`, `AdminDashboard.tsx:202`, `AdminDashboard.tsx:207`  
**Current problem:** Several labels use 9-11sp/text-[10px], which can be hard to read. MainActivity source also forces font scale to 1.0, which limits user accessibility preference.  
**Why it matters:** Dispatch apps are used outdoors, in motion, and in variable lighting. Tiny text is a real usability problem.  
**Expected design:** Important status and metadata should remain readable under accessibility font settings.  
**Recommended fix:** Avoid font-scale lock and define minimum text sizes by semantic role.  
**Reusability:** Global.  
**Related components:** badges, route metadata, admin helper text, bottom nav labels.  
**Acceptance criteria:** Core order state, amount, route, and actions remain readable at larger font sizes.

### MICRO-035

**Category:** Admin operational language  
**Severity:** P2  
**Component:** Helper text and action labels  
**Location:** `AdminDashboard.tsx:723-734`  
**Current problem:** Helper text says "Click Manage to access live GPS, OTP, and waybill printing", but the nearby action says "Open All in Shipments Tab".  
**Why it matters:** The admin is being told about a different action than the one available.  
**Expected design:** Helper text should explain the immediate next action.  
**Recommended fix:** Rename and restructure: "Manage selected category" or add inline Manage buttons per request.  
**Reusability:** Admin copy system.  
**Related components:** shipments tab, active request drawer.  
**Acceptance criteria:** Every helper text has a visible matching action.

### MICRO-036

**Category:** Visual noise  
**Severity:** P2  
**Component:** Admin shadows/animations  
**Location:** `AdminDashboard.tsx:198-231`, many Tailwind classes  
**Current problem:** Hover scale, fade-in, scale-in, shadow-md/shadow-2xl appear across operational UI.  
**Why it matters:** Dispatch admin tools need calm clarity. Too much motion/shadow makes the interface feel like a landing page, not a control center.  
**Expected design:** Motion should confirm actions and state changes, not decorate every card.  
**Recommended fix:** Reserve pronounced animation for major state transitions; use low-motion hover states for tables and cards.  
**Reusability:** Admin motion system.  
**Related components:** stat cards, modals, popovers, active rows.  
**Acceptance criteria:** Admin dashboard feels stable during triage and no core list content bounces unnecessarily.

### MICRO-037

**Category:** Color token inconsistency  
**Severity:** P2  
**Component:** Web admin colors  
**Location:** `AdminDashboard.tsx` throughout, especially `#FFC542`; mobile brand rules mention `#FFB800` as official map/control gold  
**Current problem:** Admin uses `#FFC542`, while brand rules reference official gold `#FFB800` for strict contexts.  
**Why it matters:** Slight gold drift creates a less unified identity across mobile/admin.  
**Expected design:** One source of truth for brand gold and derived tones.  
**Recommended fix:** Move web/admin colors to theme variables aligned with mobile tokens and the official brand lock.  
**Reusability:** Global.  
**Related components:** buttons, badges, admin headings, map controls, bottom nav.  
**Acceptance criteria:** A token audit finds no arbitrary gold hex values outside the approved token file.

### MICRO-038

**Category:** Design-system duplication  
**Severity:** P1  
**Component:** Cards, buttons, inputs, badges  
**Location:** `DashboardScreen.kt`, `MarketplaceScreen.kt`, `Components.kt`, `AdminDashboard.tsx`  
**Current problem:** Similar UI primitives are implemented in each screen rather than pulled from shared components.  
**Why it matters:** Every future change becomes expensive and inconsistent.  
**Expected design:** Shared primitives should handle layout, states, accessibility, and tokens.  
**Recommended fix:** Build a small component system before adding more feature polish.  
**Reusability:** Global.  
**Related components:** all listed inventory components.  
**Acceptance criteria:** New card/button/badge patterns can be built from shared primitives instead of one-off layout code.

### MICRO-039

**Category:** Rider status surfaces  
**Severity:** P1  
**Component:** Rider assignment/profile card  
**Location:** `DashboardScreen.kt:3175-3223`, `DeliveryViewModel.kt:846-1017`  
**Current problem:** Rider identity/status appears after assignment, but the card system does not appear to fully unify availability, preassignment, live status, and customer-facing profile.  
**Why it matters:** Customer trust increases when assigned rider identity is clear and consistent. Admins need the same state to dispatch properly.  
**Expected design:** Unified rider identity component with photo, name, rating, plate, availability, current mission, reserved-next state.  
**Recommended fix:** Create shared data contract and UI component variants for customer, rider, and admin.  
**Reusability:** Global rider system.  
**Related components:** admin fleet summary, customer tracking, rider dashboard.  
**Acceptance criteria:** Rider shown to customer always matches admin assignment and rider app assignment.

### MICRO-040

**Category:** Queue/preassignment state  
**Severity:** P0  
**Component:** Dispatch state model  
**Location:** User-described flow; `AdminDashboard.tsx:700-703` reserved rider hint; `DeliveryViewModel.kt` rider assignment functions  
**Current problem:** There is some evidence of reserved rider state, but the customer/admin/rider flow is not clearly represented as a first-class visual model.  
**Why it matters:** When no rider is available, the correct user message is not "nothing happened"; it is "received and queued."  
**Expected design:** Delivery lifecycle should support Received, Queued, Reserved/Preassigned, Assigned, Pickup, Transit, Arrived, Delivered.  
**Recommended fix:** Make the status model and visual stepper support rider availability and preassignment explicitly.  
**Reusability:** Core product system.  
**Related components:** customer active card, admin drawer, rider next job queue, notifications.  
**Acceptance criteria:** Customer can see queued/preassigned/assigned changes without admin inventing manual explanations.

### MICRO-041

**Category:** Action authorization  
**Severity:** P0  
**Component:** Rider/admin status actions  
**Location:** `DeliveryViewModel.kt:928-979`, admin status controls  
**Current problem:** Status updates need stricter visible authorization. Admin and rider should not see or trigger invalid next steps.  
**Why it matters:** Incorrect statuses break operational accountability.  
**Expected design:** Every status action should be generated from allowed transitions for the actor role and current data.  
**Recommended fix:** Centralize transition rules and have UI ask for `availableActions` instead of hardcoding status options.  
**Reusability:** Backend/admin/mobile.  
**Related components:** customer tracker, rider dashboard, admin shipments.  
**Acceptance criteria:** UI never presents an action the backend would reject, and backend rejects invalid transitions anyway.

### MICRO-042

**Category:** Copy polish  
**Severity:** P2  
**Component:** Product/admin/mobile helper text  
**Location:** various; prior audit found developer-facing strings in auth/AI manager  
**Current problem:** Some UI copy exposes implementation details or feels generated rather than product-led.  
**Why it matters:** Customers and riders should not see Firebase/OAuth/internal AI scaffolding language.  
**Expected design:** Human, operational copy focused on what the user can do next.  
**Recommended fix:** Add UI copy review as part of component acceptance criteria.  
**Reusability:** Global.  
**Related components:** auth, AI dispatch manager, admin seed utilities, empty states.  
**Acceptance criteria:** No customer/rider-facing text references implementation tools, fake scores, placeholder demos, or developer setup.

### MICRO-043

**Category:** Admin seed/demo utilities  
**Severity:** P2  
**Component:** Developer & demo seed utilities  
**Location:** `AdminDashboard.tsx:793+` and seed functions around `299-415`  
**Current problem:** Seed utilities live near operational admin UI.  
**Why it matters:** Production admins should not see demo/data seeding controls in the main operational flow.  
**Expected design:** Dev utilities should be hidden behind environment/dev mode or separate settings.  
**Recommended fix:** Gate seed utilities behind non-production flag and move to a developer tools section.  
**Reusability:** Admin architecture.  
**Related components:** seed users, seed deliveries, seed marketplace.  
**Acceptance criteria:** Production admin UI does not show demo seed actions.

### MICRO-044

**Category:** Component states  
**Severity:** P1  
**Component:** Cards/buttons/selects/forms  
**Location:** global source pattern  
**Current problem:** Components often define default state but not full pressed, loading, disabled, error, empty, selected, focused, and success states.  
**Why it matters:** Users trust an app when every state looks intentional, especially network and payment failures.  
**Expected design:** Each shared component documents supported states.  
**Recommended fix:** Component polish plan should define a state matrix for each primitive.  
**Reusability:** Global.  
**Related components:** all cards, buttons, inputs, toasts, sheets.  
**Acceptance criteria:** QA can test each component against a documented state list.

### MICRO-045

**Category:** Responsive web layout  
**Severity:** P1  
**Component:** Admin dashboard cards/tables  
**Location:** `AdminDashboard.tsx:740-790`  
**Current problem:** The admin uses dense grid/table structures that may work on desktop but need explicit behavior for narrower widths.  
**Why it matters:** Admins may operate from laptops or tablets, and dispatch decisions must remain accessible.  
**Expected design:** Admin dispatch rows should collapse into cards with preserved actions and status.  
**Recommended fix:** Define desktop/tablet/mobile admin breakpoints and component variants.  
**Reusability:** Admin-wide.  
**Related components:** shipments table, marketplace admin, users tab.  
**Acceptance criteria:** At tablet width, no action or critical delivery data is hidden behind horizontal scroll without clear affordance.

### MICRO-046

**Category:** Section rhythm  
**Severity:** P2  
**Component:** Dashboard sections  
**Location:** `DashboardScreen.kt:1160-1380`; screenshots  
**Current problem:** Section heading, filter, active cards, and recent history spacing lack a steady vertical rhythm.  
**Why it matters:** Rhythm helps users understand where one section ends and another begins.  
**Expected design:** Use a section spacing system: header-to-filter, filter-to-list, card-to-card, section-to-section.  
**Recommended fix:** Define dashboard vertical spacing tokens and apply them consistently.  
**Reusability:** Global section layout.  
**Related components:** marketplace, order logs, settings, admin sections.  
**Acceptance criteria:** Adjacent sections have consistent, intentional spacing on all major screen sizes.

### MICRO-047

**Category:** Address semantics  
**Severity:** P1  
**Component:** Route strings  
**Location:** `DashboardScreen.kt:1422`, admin rows using pickup address in metadata  
**Current problem:** Pickup and dropoff are often merged into a single string separated by `->`.  
**Why it matters:** Routes are not plain text; they are structured operational data.  
**Expected design:** Pickup/dropoff should render as structured fields with icons/labels and support copy/open map actions.  
**Recommended fix:** Replace route string concatenation with `RouteDisplay(pickup, dropoff, compact)` components.  
**Reusability:** Global.  
**Related components:** active cards, history cards, admin request details, rider manifest.  
**Acceptance criteria:** Every route surface has separate pickup and dropoff labels and can handle missing endpoint data.

### MICRO-048

**Category:** Data defaults  
**Severity:** P2  
**Component:** Marketplace ratings/stock/store data  
**Location:** `DeliveryViewModel.kt:5446-5530`  
**Current problem:** Marketplace listeners use default ratings/stock/vendor fields when data is missing. This can make UI look confident when data is incomplete.  
**Why it matters:** Users may trust fake-looking 4.9/5.0 values too much.  
**Expected design:** Missing data should render as "New", "No rating yet", or hidden rating, not invented confidence.  
**Recommended fix:** Use nullable display models and explicit placeholder states.  
**Reusability:** Marketplace and store cards.  
**Related components:** product cards, recommended shops, admin marketplace.  
**Acceptance criteria:** Missing rating/stock/vendor data does not display as a precise fabricated metric.

### MICRO-049

**Category:** Badge scale  
**Severity:** P2  
**Component:** Delivered badge in recent history  
**Location:** `DashboardScreen.kt:1432-1442`; screenshot 2  
**Current problem:** Delivered badge is too tiny to anchor status.  
**Why it matters:** Completion state is reassuring and should be scannable.  
**Expected design:** Badge text should be at least readable micro-label size with icon/color/state consistency.  
**Recommended fix:** Use shared `StatusBadge(size = Compact)` with minimum height 22dp and readable text.  
**Reusability:** Global status badges.  
**Related components:** admin status chips, active card status, tracking stepper.  
**Acceptance criteria:** Status badge remains readable in daylight and at increased font sizes.

### MICRO-050

**Category:** Component QA  
**Severity:** P1  
**Component:** Entire UI system  
**Location:** global  
**Current problem:** There is no visible component quality gate that tests screenshots against long text, missing data, tiny screens, dark mode, and action states.  
**Why it matters:** AI-generated or rapidly assembled UI often regresses through micro-breakage.  
**Expected design:** Every shared component has preview fixtures and screenshot QA states.  
**Recommended fix:** Add Compose previews and Storybook-style admin examples for each component state.  
**Reusability:** Global.  
**Related components:** all reusable components.  
**Acceptance criteria:** Each core component has fixtures for normal, long, empty, loading, disabled, error, and dark/light modes.

## 6. Component quality scorecard

| Dimension | Score / 10 | Judgment |
|---|---:|---|
| Layout composition | 5 | The visual direction is clear, but internal grids are weak. |
| Spacing system | 4 | Many components use local spacing values rather than a visible shared scale. |
| Typography | 5 | Brand weight is strong, but small labels, excessive bolding, and route wrapping hurt clarity. |
| Alignment | 4 | Screenshots show loose alignment inside cards and header/content overlap. |
| Information hierarchy | 4 | Status, route, amount, ID, and action often compete. |
| Responsiveness | 4 | Long addresses and small screens are not sufficiently protected. |
| Consistency | 4 | Similar cards/buttons/badges are implemented differently across app/admin. |
| Interaction feedback | 6 | Some tactile/motion patterns exist, but state coverage is incomplete. |
| Animation restraint | 5 | Motion exists, but needs clearer purpose and reduced-motion gating. |
| Accessibility | 4 | Tiny text and forced font scale are serious concerns. |
| Design-system consistency | 3 | Tokens exist, but many one-off values and components remain. |
| Dynamic-content resilience | 3 | Route/address/name/price edge cases can damage layouts. |
| Overall polish | 4 | The app has premium ambition, but not yet premium micro-composition. |

## 7. Priority repair order

1. Fix header/content overlap and bottom dock content inset.
2. Create shared `DeliveryCard`, `HistoryOrderCard`, `RouteDisplay`, `StatusBadge`, and `IconActionButton`.
3. Redesign active delivery and recent history cards around a deliberate internal grid.
4. Replace admin active request pop-up with an inline dispatch decision drawer.
5. Enforce delivery status transition rules in UI and backend.
6. Add notification dedupe/dismiss lifecycle.
7. Tokenize color, spacing, typography, radius, elevation, and motion across mobile/admin.
8. Add component preview fixtures for long Benin City addresses, missing riders, no available riders, queued delivery, reserved rider, assigned rider, and delivered history.

## 8. Human review conclusion

The product looks like it was designed with ambition, but many micro-components still behave like they were assembled one screen at a time. The most urgent design problem is not "make it prettier." The urgent problem is to make every operational component tell the truth clearly: what the order is, where it is going, who owns it, what state it is in, and what action is valid next.

Once the card system, route display, status model, admin dispatch drawer, and notification lifecycle are repaired, the same visual identity can start to feel premium instead of decorative.
