# ESDispatch Deep Audit — Booking Form Inputs, Route/Destination Logic & Tracking Map

**Date:** 2026-09-27 · **Scope:** booking screens address inputs, coordinate/route resolution, booking persistence, tracking-screen embedded map (design + logic)
**Method:** full source trace (`mobile/app/src/main/java/com/esdispatch`), every cited line re-read and spot-verified by the lead auditor after the automated pass. READ-ONLY — no code was changed.

---

## Direct answers to your three questions

### 1. "What is wrong with the booking form?? Is it supposed to even be called booking?"

The name is deliberate — the repo's own motion spec (`AGENTS.md`, §"Interaction Physics") calls the fare counter a **"Booking Fare Live Ticker"**, so "booking" is the established internal language. (If you want customer-facing copy to say "Send a delivery" instead of "Book", that's a wording change, not a spec violation.)

But your instinct that something is deeply wrong is correct — **the form doesn't actually book what you typed.** The address field is a *text fiction*:

- `SearchResultItem` carries exact `lat/lng` from autocomplete (`GeocoderUtils.kt:12-13`) and `updateDraftPickup(address, lat, lng)` accepts them (`DeliveryViewModel.kt:4953`), **yet every screen throws the coordinates away** and passes text only:
  - `EconomyBookingScreen.kt:477` → `pickup = item.displayInput` (also `:625`, Express `:503/:698`, Batch `:463/:830`, Multi `:475/:699`, Marketplace `:795`)
  - All call sites: `updateDraftPickup(pickup)` (`Economy:228`, `Express:200`, `Batch:224`, `Multi:207`)
- From there the app **re-guesses your coordinates from the text — six separate times, in six different places, each with its own fallback logic.** All of them converge on the same hardcoded point: **King's Square / Ring Road (6.3350, 5.6037)**.

So: pick a place, book it, and the pin, the price, the rider dispatch, the arrival trigger and the map each independently *guess* where you meant. When any guess fails — they all silently agree on city centre.

### 2. "I don't trust the input fields" — you shouldn't

The fields accept everything, validate inconsistently, and much of what you type **never survives the booking** (package description, declared value, Express category/date, edited stops — all silently dropped, details in §C). Fabricated addresses are pre-filled as defaults and already pass validation (`BatchBookingScreen.kt:91`, `MultiBookingScreen.kt:78`).

### 3. "Is the map 100% perfect?" — No

Against the repo's own mandatory `AGENTS.md` Live Tracking Map Standards: **2 of 4 rules FAIL** (§D), and the map has independent logic defects (§E) — including one where **the real route distance/duration from OSRM is measured, sent to Android, and then discarded by an empty default callback.**

---

## Root cause: the coordinate resolution chain

```
┌─ 1. INPUT ───────────────────────────────────────────────────────────┐
│ AddressAutocompleteField: suggestion tapped → TEXT ONLY kept         │
│ (lat/lng present in the object, discarded at Economy:477 etc.)       │
│ Detect-location button: returns coords too — ALSO discarded          │
│ (GeocoderUtils:619 detectUserLocation returns only .address)         │
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 2. DRAFT ───────────────▼───────────────────────────────────────────┐
│ updateDraftPickup(text) → AddressDatabase.getCoordinates(text)       │
│ = FIRST-OVER-SUBSTRING-TAGS match (AddressDatabase.kt:241)           │
│ "Sapele Road" → Evbuoriaria Industrial Layout (4.7 km off)           │
│ any "...market..." → Oba Market; any "...hospital..." → Central Hosp.│
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 3. QUOTE/PRICE ──────────▼──────────────────────────────────────────┐
│ geocodeAddress() → geocodeAddressLocalOnly() (DeliveryViewModel:5148)│
│ local fn ALWAYS returns non-null (:5102 else → 6.3350,5.6037)        │
│ → Google Geocoder branch (:5151) is DEAD CODE                        │
│ → "Failed to resolve address" state (:5336) can NEVER fire           │
│ → garbage address = cheapest price (distance collapses to 0)         │
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 4. BOOKING SAVE ────────▼───────────────────────────────────────────┐
│ pickupLat = draft.pickupLat ?: phone GPS (:5429)                     │
│ (draft.pickupLat null in practice → customer GPS ≠ entered address)  │
│ deliveryLat = draft.deliveryLat → null                               │
│ Batch: pickup = phone GPS, delivery coords never set (:5638)         │
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 5. RIDER DISPATCH ──────▼───────────────────────────────────────────┐
│ FirebaseManager broadcast: (parcel.pickupLat ?: 6.3350) (:1795)      │
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 6. TRACKING MAP ────────▼───────────────────────────────────────────┐
│ stored coords? → else MAIN-THREAD geocoder in remember() (:3184,     │
│ network-on-main throws) → catch → :3197 return 6.3350,5.6037        │
│ pickup & delivery badges stack on Ring Road → OSRM route aborts      │
│ (JS: |origin-target| < 0.0001 → no route line at all)               │
└──────────────────────────┬───────────────────────────────────────────┘
┌─ 7. 50 m ARRIVAL TRIGGER ▼───────────────────────────────────────────┐
│ LocationService compares rider GPS vs those coords;                 │
│ null OR (0.0,0.0) coords disable it entirely (:192,:194,:200)       │
│ → "Arrived" never auto-fires when destination was mis-resolved      │
└──────────────────────────────────────────────────────────────────────┘
```

Six independent resolvers (`AddressDatabase.getCoordinates`, `geocodeAddressLocalOnly`, `TrackingScreen.resolveGeocodedCoords`, `detectUserLocationDetailed`, `FirebaseManager` defaults, JS-side fallbacks) all guess separately — this is the single root cause behind most findings below.

---

## A. Booking form — input fields

### CRITICAL

| # | Evidence | What's wrong | What you see |
|---|---|---|---|
| A1 | `EconomyBookingScreen.kt:477` (`pickup = item.displayInput`; same pattern 6 more places) | Suggestion tap keeps text, discards the suggestion's exact lat/lng | You pick an exact place; the app silently re-guesses where it is |
| A2 | `AddressDatabase.kt:241-244` — `entries.firstOrNull { ... entry.tags.any { tag -> a.contains(tag) } }` | Coordinate lookup = first substring-tag match, no scoring | "Sapele Road, Benin City" → Evbuoriaria Industrial Layout (≈4.7 km off); any address with "market" → Oba Market; "hospital" → Central Hospital instead of UBTH; "Ihama Road, GRA" → first "gra" tag |
| A3 | `DeliveryViewModel.kt:5148-5149` + `:5102` (`else -> Pair(6.3350, 5.6037)`) | Geocoding can never fail: local fn is always non-null → Google branch (`:5151`) dead, and the comment at `:5164` ("return null to prompt user… rather than fabricating fake coordinates") contradicts the code it sits in | Typing "123 Fake Street, London" still produces a price, enables Book, and shows no error anywhere |
| A4 | `BatchBookingScreen.kt:112-126` — `LaunchedEffect(draft)` rebuilds `batchStops` from `draft.deliveryAddress` on **every** draft change (and `:222-228` syncs every keystroke into `draft`) | Added stops 2-5 are destroyed by the next keystroke in pickup/sender; stop 1's item name/weight overwritten | Add 5 stops → type one letter in pickup → back to 1 stop |

### HIGH

| # | Evidence | What's wrong | What you see |
|---|---|---|---|
| A5 | `BatchBookingScreen.kt:91` — `else "14 Ihama Road, GRA, Benin City"`; validation = `length >= 6` (`:1079`) | Fabricated destination pre-filled, already passes validation | A customer who only fills the recipient books a delivery to an address they never chose |
| A6 | `MultiBookingScreen.kt:78` — `else "Ring Road (King's Square), City Center, Benin City"`; validation `:1020` | Same, for pickup | Multi-pick booking enabled with a fake pickup |
| A7 | `EconomyBookingScreen.kt:120` + `GeocoderUtils.kt:616` — permission denied → `DetectedLocation("Ring Road (King's Square)...", 6.3350, 5.6037)`, and `:619` keeps only the string | Denying location silently substitutes a fake address (Express `:112-124` identical) | Tap detect → deny permission → pickup silently becomes King's Square, priced from there |
| A8 | `AddressAutocompleteField.kt:86/:96` — new query with no matches never clears `searchResults` | Stale suggestions persist | Type a nonsense query → previous query's rows still tappable |
| A9 | `DeliveryViewModel.kt:5120-5124` + no screen checks `pickup == delivery` | Same-address booking accepted → 0 km price | Book pickup = delivery, get a near-zero fare |

### MEDIUM / LOW

- **Validation is inconsistent across the four screens** — Multi accepts any non-blank stop (`:1021`), Economy/Express/Batch require ≥6 chars (`Economy:1231`, `Express:1289`, `Batch:1079`); Batch demands every recipient phone (`:1081-1082`), Multi allows blanks (`:1024`).
- **Abandoned drafts leak** — draft reset only after successful booking (`DeliveryViewModel:5505`); `clearDraft()`/`loadDraftFromPrefs()` (`:7087/:7108`) have zero callers, while every screen restores the draft on open (`Economy:86-88`…). An abandoned booking pre-fills the next with no indication.
- **Stale suggestion ranking** — local database always prepended (`GeocoderUtils.kt:241`), Mapbox only when `<4` results (`:269`), Google only when `<3` (`:329`), and Mapbox results outside a Benin bbox are dropped (`:300,:306`) → typing "Market Square" shows Oba Market above the real place; non-Binin addresses can be typed but never suggested.
- **Price/Book bar hides while an address field has focus** (`Economy:1174` `if (focusedField == null)`, `Express:1232`).
- **Typo expansion mangles queries** — `expandTypos` substring-replaces "st"→"street", "sec"→"secretariat", "hos"→"hospital" anywhere in the string (`AddressDatabase:280-282`).
- **Quick-picks disagree with the database** — `AddressAutocompleteField:65` King's Square `6.3350, 5.6200` vs `AddressDatabase:61` `6.3315, 5.6262`.
- Dead code: `beninLandmarks` (`Batch:57`), `isBeninCity/isLagos/resolveBeninZone` (`AddressDatabase:192/:206/:220`), `isIntercityRoute`/`checkProximityArrival`/`aiCorrectAddress`/`pinDropNearestAddress`/`updateDeviceLocation` (all zero callers), duplicate `ParcelDraft` class (`Models.kt:201` vs `DeliveryViewModel.kt:8512`).

---

## B. Route addresses & destination coordinates

| # | Severity | Evidence | What's wrong |
|---|---|---|---|
| B1 | CRITICAL | `DeliveryViewModel.kt:5065-5102` (`geocodeAddressLocalOnly` — final `else -> Pair(6.3350, 5.6037)`) | Unresolvable addresses are **fabricated** into city centre; the quote error state (`:5336`) is unreachable |
| B2 | CRITICAL | `DeliveryViewModel.kt:4957/:4970` → `AddressDatabase.getCoordinates` first-match | Wrong-landmark resolution feeds price AND saved draft (see A2) |
| B3 | HIGH | `DeliveryViewModel.kt:5429-5431` — `effectivePickupLat = draft.pickupLat ?: _currentUserDeviceLocation` | Booked parcels get phone-GPS or null coords instead of the entered address; `_currentUserDeviceLocation` is only written by **rider** flows (`:1114`, `:1367`) — for a customer both sides are effectively null |
| B4 | HIGH | `DeliveryViewModel.kt:5638` — batch pickup = `_currentUserDeviceLocation`; **no `deliveryLat`/`deliveryLng` at all** in the batch `Parcel(...)` block (`:5614-5640`) | Batch parcels never store a drop-off pin; pickup pin = wherever the phone was |
| B5 | HIGH | `TrackingScreen.kt:3184` (blocking `geocoder.getFromLocationName` inside `remember(...)` at `:3230/:3236/:3241/:3247`) → `:3197 return Pair(6.3350, 5.6037)` | Geocoder runs on the UI thread during composition (ANR/jank risk); network-on-main throws → caught → **every unresolvable address pins at Ring Road** on the live map |
| B6 | HIGH | OSRM telemetry chain: `TrackingScreen.kt:4068-4069` fires `AndroidMap.onRouteTelemetry(route.distance, route.duration)` → `:3111` → `:3087` default `{ _, _ -> }` → `:3227` default → **call site `:860-888` never passes the parameter** | The route's REAL distance/duration are measured by OSRM and then **discarded** — any ETA/telemetry UI derived from them shows nothing |
| B7 | MEDIUM | `FirebaseManager.kt:1795-1796` — `"pickupLat" to (parcel.pickupLat ?: 6.3350)` | Null coords broadcast to riders as King's Square regardless of real pickup |
| B8 | MEDIUM | `LocationService.kt:192/:194/:200` — `stopLat ?: return`, `if (stopLat == 0.0…) return`, `distanceMeters <= 50.0f` | 50 m "Arrived" trigger silently disabled when coords are null/0,0 — and fires at the WRONG location when coords resolved to a wrong landmark |
| B9 | MEDIUM | `RouteDisplay.kt:188` — `deliveryAddress.ifBlank { "Destination pending" }` | Route row honestly shows "pending" — but only when text is blank; a wrong-but-present address looks authoritative |

**How a wrong destination happens end-to-end (one realistic journey):**
You type *"Uselu Market, Benin City"*, autocomplete suggests it, you tap it → only the text is kept (A1). Draft resolution matches the first entry containing "market" → **Oba Market on Ring Road** (A2) → price is computed from that ~4 km distance (B2). Booking saves `pickupLat=null` for you (B3) — the rider dispatch broadcasts `6.3350, 5.6037` (B7). The tracking map tries stored coords, finds none, runs the geocoder on the UI thread, it throws on main → pins both badges on Ring Road (B5) → JS sees origin≈target and refuses to draw a route → **no route line**. The rider's 50 m geofence watches the wrong point, so "Arrived" never auto-fires (B8). Every component is confident; none of them agree with what you typed.

---

## C. Data typed by the customer that never saves (silent no-ops)

| # | Evidence | Field lost |
|---|---|---|
| C1 | `DeliveryViewModel.kt:5436` — `itemName = draft.itemName.ifBlank { "New Parcel (...)" }`; `updateDraftItemName` (`:5004`) has **zero callers** | **Package description** — confirmation and rider see "New Parcel (Economy)" instead of "50-inch TV" |
| C2 | `DeliveryViewModel.kt:5433-5461` — `Parcel(...)` omits `declaredValue` although the field exists (`Models.kt:87`) and is persisted on read (`FirebaseManager.kt:911`); only writer `updateDraftSpecs` (`:4987`) runs on the wallet path only | **Declared item value** — reads ₦0 on every card-funded booking |
| C3 | `ExpressBookingScreen.kt:92-93/:160/:238` — `selectedCategory`, `isInstantSpeed`, `selectedDate` are local `remember` state, never written to the draft; `Parcel.category = draft.selectedCategory.ifBlank{"Standard"}` always wins | **Express category, speed, delivery date** — cosmetic chips; shipment always "Standard" |
| C4 | `MultiBookingScreen.kt:211` — stops list only written when `stops.size > 1`; never cleared when reduced 2→1, while `:5453` publishes `draft.stops...` | A **deleted stop** still ships inside the booking |
| C5 | Economy `:1309-1313` / Express `:1367-1371` — Paystack path calls `finalizeDraftPrice` but skips `updateDraftSpecs` | Weight/dims/declared value entered before **card payment** are lost (weight partially back-filled by quote sync `:5370`, declaredValue never) |

---

## D. Tracking map — AGENTS.md compliance verdict

**Engine:** Leaflet bundled from local assets (`TrackingScreen.kt:3685-3686`, `assets/leaflet/leaflet.js`) — **no API token in source** ✅. `setWebContentsDebuggingEnabled` is debug-gated (`:3293-3297`) ✅.

| Rule | Verdict | Evidence |
|---|---|---|
| **(a) Flat pointers — `box-shadow: none`, no blurred shadows/glows** | ❌ **FAIL** | Courier core `:3420 box-shadow: 0 2px 6px rgba(0,0,0,0.5)`; user dot `:3602 box-shadow: 0 1px 4px rgba(0,0,0,0.4)`; animated glow halo `:3459-3460` (`box-shadow: 0 0 0 10px`); breathing beacon rendered as translucent halo `:3398-3404` at 2000 ms (spec: 1800 ms) |
| **(b) All controls/toggles/active states = `#FFB800`, never `#D4AF37`** | ✅ **PASS** | 0 hits for `D4AF37` in the whole file (case-insensitive); `.control-btn.active` = `background:#FFB800; color:#121212` (`:3570-3571`); 20 `#FFB800` hits on controls/route/markers. *Minor:* off-brand `Color(0xFFE5A93B)` in a non-map illustration at `:5023` |
| **(c) Street labels in all modes; ESRI `World_Transportation` layered on top of satellite** | ✅ **PASS** | Street: OSM/Google roadmap tiles with labels (`:3754/:3759`); satellite: Google hybrid + `esriTransportationTiles.addTo(map)` **after** base in both init (`:3782-3783`) and switch (`:4271-4272`) |
| **(d) ≥44 dp hit areas + pressed/disabled states** | ❌ **FAIL** | HTML STREET/SATELLITE chips ≈ 24 px (`:3561-3562`); HTML re-center 40×40 (`:3673-3674`); Compose map controls 42 dp (`:3064`, weather `:1143`); **zero `:active`/`:disabled` CSS** for controls; duplicate re-center affordances (`:4509` ≈32 dp vs HTML `:3702`) |

**Light mode:** the HTML theme is hardcoded `var isDarkTheme = true` (`:3719`) while Kotlin computes the real theme at `:3252` and **never injects it** — in light mode you get dark tiles, dark HUD, dark route casing (dead `.light-mode` styles at `:3623-3627`), violating the global theme-adaptation rule.

---

## E. Tracking map — logic defects

| # | Severity | Evidence | What's wrong / what you see |
|---|---|---|---|
| E1 | HIGH | `TrackingScreen.kt:3910/:3956/:4192` — address/courier strings concatenated into popup HTML (`innerHTML`) | Injection surface: an address containing markup executes in the WebView (with the JS bridge + `allowUniversalAccessFromFileURLs` at `:3285-3286` exposed); even benign text with `<` renders as garbage |
| E2 | HIGH | `:4481` — `safeAddr` escapes `"` and `'` but **not backslashes/newlines**; `:4411` double-quoted path misses `\r` | An address ending in `\` or containing a newline → `SyntaxError` → **pins/route silently never appear** for that shipment |
| E3 | HIGH | `:3719` (see D) | Map never adapts to light mode |
| E4 | MEDIUM | `:1206-1211` Traffic button, `:1199-1205` Follow button — `routeColor` is a `LiveMapView` parameter (`:3210`) **never read inside it**; no traffic layer exists in the HTML | Tapping Traffic flips the button gold and does **nothing** — while the "AI TRAFFIC REROUTING ACTIVE" banner (`:937`) claims a reroute; the route stays `#FFB800` regardless (`:839`) |
| E5 | MEDIUM | `:1148-1153` — a map chip cycles fabricated weather ("Rainy", "Stormy") and feeds it into `calculateEta` | **The user can change the ETA by tapping a button** — invented weather overrides the real Open-Meteo data fetched at `:604-638` |
| E6 | MEDIUM | `:4343-4360` — `clearMapRoutesAndPins()` removes `activeRouteLine` but **not `routeCasingLine`** | A dark 9 px stripe survives across the map (very visible over satellite) in the "no booking" state |
| E7 | MEDIUM | `:3864-3865/:3994-3995` — `if (!map) return;` with no queue; map init on `setTimeout(...,80)` (`:4383`) vs `isPageLoaded` in `onPageFinished` | Intermittent first-open with **no pins/route** until some later update re-fires; if `leaflet.js` fails to load (`:3263-3268` swallowed to `""`) the map is permanently blank with no error state |
| E8 | MEDIUM | `:3285-3290` — `allowFileAccessFromFileURLs`, `allowUniversalAccessFromFileURLs`, `MIXED_CONTENT_ALWAYS_ALLOW` | Unneeded for `loadDataWithBaseURL` and widens E1's blast radius |
| E9 | MEDIUM | `:3321-3322` console logging + no `Log` strip in proguard (`proguard-rules.pro` has no `-assumenosideeffects`) | Map JS warnings (incl. route errors) emitted to logcat in the **release** APK |
| E10 | LOW | `:4139` bearing `!== 0` check; `:4154` unbounded `traveledCoords`; `:3858` CANCELLED/DELIVERED still render live route + rider; `:3778-3779` tile-error fallbacks stack layers instead of swapping | Chevrons flip when heading due north; memory growth on long rides; cancelled orders look active; layered tiles on flaky networks |

---

## F. What you actually experience (symptom → cause)

1. **"Pins are always in weird places."** → A1 + A2 + B5 (input coords discarded, substring-tag guessing, UI-thread geocoder fallback to Ring Road).
2. **"Prices don't match the route."** → A3 + B2 + B6 (geocoding can never fail, wrong landmarks feed haversine distance, OSRM's real distance is discarded).
3. **"I typed X and the rider went to Y."** → B3 + B4 + B7 + B8 (saved coords are phone-GPS/null, broadcast defaults to King's Square, geofence watches the wrong point).
4. **"My package details are missing."** → C1-C5 (five classes of typed data silently dropped).
5. **"Batch booking eats my stops."** → A4 (+ A5 fake default destination waiting underneath).
6. **"The traffic button / weather / ETA seem fake."** → E4 + E5 (they are: the traffic toggle is wired to nothing and weather is a manual spinner feeding the ETA).
7. **"Light mode map stays dark."** → E3.
8. **"Sometimes the map opens with no pins or route."** → E2 + E7 (string-escaping SyntaxError, or injections dropped before map init).
9. **"'Arrived' doesn't trigger / triggers at the wrong place."** → B8.

---

## G. Recommended fix order (highest leverage first)

1. **Keep the coordinates the user picked.** One-line-class changes at 7 call sites (`updateDraftPickup(pickup, item.lat, item.lng)` / suggestion-tap handlers) — fixes the source of truth for everything downstream.
2. **Make geocoding able to fail.** Remove the `else -> 6.3350` fabrication in `geocodeAddressLocalOnly` (`DeliveryViewModel:5102`) and let `geocodeAddress` fall through to the real geocoder; wire the existing `PendingQuote.Error` state. (This also makes the price honest.)
3. **Replace `AddressDatabase.getCoordinates` first-match with the scoring already implemented in `search()`** — one function fix corrects draft, quote and rider dispatch at once (feeds A2/B2).
4. **Persist what the customer typed** (C1-C5): wire `updateDraftItemName`, add `declaredValue` to the `Parcel(...)` constructor, write Express category/date to the draft, clear stops properly, run `updateDraftSpecs` on the card path.
5. **One shared resolver, computed off the UI thread** — move `TrackingScreen`'s `remember { geocode }` to `Dispatchers.IO` (it already does this correctly for ETA at `:680-682`) and delete the duplicated fallbacks (B5, B7); fix the batch `Parcel` to store delivery coords (B4).
6. **Tracking map:** pass `onRouteTelemetry` through (`B6`), fix the two JS sanitizers (E2), escape popup HTML (E1), inject the real `isDarkTheme` (E3), remove the dead Traffic/Follow wiring or implement traffic (E4), gate the weather chip out of production (E5), remove `routeCasingLine` in clear (E6).
7. **Design-rule cleanup (D):** strip marker `box-shadow`s/glow animations to flat pointers, raise chips to 44 dp with `:active` states, deduplicate the re-center buttons.
8. **Delete dead code** listed in §A LOW and the stale comment at `DeliveryViewModel:5164`.

---

*All line numbers verified against working-tree source on 2026-09-27. Related reports: `PRODUCTION_ASSURANCE_AUDIT_2026-09-26.md`, `AUDIT_EMAIL_IDENTITY_OTP_2026-09-27.md`.*
