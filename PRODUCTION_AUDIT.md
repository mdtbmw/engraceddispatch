# ESDISPATCH — PRODUCTION-READINESS AUDIT & WORK PLAN

**Scope:** Native Android app (`mobile/`) + Next.js web site/admin (repo root → Vercel).
**Goal:** Complete every marketplace, vendor, rewards, address, notification, and UI system end-to-end & production-grade (real-time, no simulation, no half-stops), then unify app + admin.

**Decisions recorded:**
- Payments: **real Paystack via env var** (wallet top-up + marketplace checkout).
- Vendor verification: **both** — admins can approve/reject AND **auto-verify on completion** with an **admin toggle** to disable auto-verify. Additional verification steps (basic KYC to protect buyers) required after unlocking vendor-worthiness.
- Audit doc stored at repo root.

---

## 0. BLOCKING / DEPLOY GATE

| # | Item | Where | Fix | Sev | Done |
|---|------|-------|-----|-----|------|
| B1 | **Vercel all-routes 404** | committed `vercel.json` has `"outputDirectory": ".next"`; local copy removed it but uncommitted | Commit removal; Vercel Root Directory = repo root; env vars present; redeploy | **CRIT** | ☐ |
| B2 | Verify `src/app/page.tsx` route-group re-export | `src/app/page.tsx:1` | Keep/confirm; smoke-test `/` and `/engdadmin` after deploy | High | ☐ |
| B3 | Fake data injected on empty Firestore | `DeliveryViewModel.kt:4758-4764` seeds; `MarketplaceScreen.kt:62,72` fallback | Remove OEM fake products; real empty-state only | **CRIT** | ☐ |

---

## 1. ADDRESS & LOCATION (unify end-to-end)

| # | Item | Where | Fix | Sev |
|---|------|-------|-----|-----|
| A1 | **"Current location, <addr>" bug** | `GeocoderUtils.kt:15-17` concatenates `"$title, $fullAddress"` | GPS detect writes bare address; drop prefix | **CRIT** |
| A2 | One shared address component | `components/AddressAutocompleteField.kt` (only in Marketplace) | Use it on **all** booking screens + tracking | High |
| A3 | Same location string everywhere | duplicated inline autocomplete `BookingForm:239/Express:177/Economy:202`; inconsistent truncation `TrackingScreen:1106,1127`, `Dashboard:2227,3120,3235`, `BookingForm:1838` | One shared formatter; single source of truth | **CRIT** |
| A4 | Live search beats local DB | local `AddressDatabase` overrides Mapbox | Mapbox first; DB only as fallback | Medium |
| A5 | Reverse-geocode consistency | `GeocoderUtils:143`, `TrackingScreen:3405` (Nominatim), `TrackingScreen:3389` | One reverse tool, deterministic formatting, no hardcoded landmarks | High |

---

## 2. MARKETPLACE — PRODUCTION-GRADE

| # | Item | Where | Fix | Sev |
|---|------|-------|-----|-----|
| M1 | Real product pool (kill fake storefront) | `DeliveryViewModel:4758-4764`, `MarketplaceScreen:62,72` | Remove `defaultSampleProducts`; empty state | **CRIT** |
| M2 | **Loyalty-discount charged** | checkout shows `pointsDiscount`/reduced total but `checkoutMarketplaceCart` ignores it | Add points redemption; deduct points; persist discounted order | **CRIT** |
| M3 | Consistent totals (Wallet vs Paystack) | same | One billing path; single `grandTotal` | **CRIT** |
| M4 | **Real wallet top-up** | `WalletViewModel.topUpWallet:23-91` TODO | Top-up via Paystack (env key) | High |
| M5 | "My Products" broken filter | `DeliveryViewModel:4710` listener drops `vendorId` | Populate `vendorId`; owner filter works | **CRIT** |
| M6 | Real coupons | `applyPromoCode:5277` only `DISCOUNT10` | Coupon collection + redeem (Firestore) | Medium |
| M7 | `storeRating` live | write-only never read | Compute/populate from feedback | Medium |
| M8 | Marketplace rider real | hardcoded `rider_1 / Tunde Bakare` + fixed GPS (`:5072`) | Assign real online rider + live map | High |
| M9 | Stock truth | decrement `:5050` | Verify all paths | Medium |

---

## 3. CUSTOMER → VENDOR TRANSFORMATION

- **Milestone unlock** → **basic KYC** (protect buyers) → **verified vendor**.
- **Auto-verify ON completion + admin toggle to disable**.
- **Admin manual approve/reject**.
- Multiple verification steps after unlocking worthiness.

| # | Item | Where | Fix | Sev |
|---|------|-------|-----|-----|
| V1 | **Milestone base = bookings, not deliveries** | `confirmBooking()` increments `deliveryCount`+:3744-3753 | Count only `DELIVERED` parcels for vendor gate | **CRIT** |
| V2 | **Verification dead-end** | no path sets `isVerified=true` | Approve/Reject + Auto-verify + KYC | **CRIT** |
| V3 | KYC unlock flow | new | Collect ID, business name, bank account, BVN | High |
| V4 | Auto-verify toggle | ViewModel + Admin | `platformConfig.autoVerifyVendors` default ON | High |
| V5 | Admin approve/reject | web `/engdadmin` | Store-review list → set doc + notify | High |
| V6 | Progress-to-verified card | `VendorPortalScreen:517-568` | Milestone→KYC→Verify→Vendor (`done/pending`) | Medium |
| V7 | Vendor dashboard transforms | `VendorPortalScreen:132-263` | Confirm real CRUD on verify | High |
| V8 | `vendorId` ownership + admin stores | `:5165,:4795` | Listener carries `vendorId`; moderation | High |

---

## 4. REWARDS / MILESTONES / NOTIFICATIONS

| # | Item | Where | Fix | Sev |
|---|------|-------|-----|-----|
| R1 | **VIP/referral card dead code** | `dashboard/LoyaltyRewardsCard.kt` only in `V2DashboardScreen` | Wire into live `DashboardScreen` | **CRIT** |
| R2 | Referral **stub** | `redeemReferralCode(onComplete):5286` returns success doing nothing | Real redeem | **CRIT** |
| R3 | Points earned vs tiers + discount | `:3749`; tier logic | Unify with marketplace discount | High |
| R4 | Excitement UX | confetti + in-app on milestone/loyalty only | Add on: purchase, vendor approved, points claimed, referral redeemed | Medium |
| R5 | **System Monitor real, not simulated** | `ProfileScreens:2490-2584` inject fake events | Remove fake inject; live fleet metrics; broadcast gated to admins | **CRIT** |
| R6 | Uptime/CPU/server | not present | Show real metrics or label "Live Fleet" — no fake | Medium |

---

## 5. UI POLISH (app + admin)

| # | Item | Where | Fix | Sev |
|---|------|-------|-----|-----|
| U1 | **Tab active text invisible** | `Dashboard:2036` **CRIT**; `ProfileScreens:1830`, `Marketplace:2025`, `BookingScreens:1000`, `VendorPortal:73`, `OrderLogs:74` | Active text legible in **both** modes (Gold fill ⇒ Obsidian text) | **CRIT** |
| U2 | Broken 2-line buttons | `ProfileScreens:2510-2566`; `VendorPortal:573`; `RiderScreens:1785` | `maxLines=1` `TextOverflow` | High |
| U3 | Harsh shadows on press | `HeroCarousel:143`, `PromoCarousel:155`, `SupportButton:1320`, toast `MainActivity:360`, cart `Marketplace:230` | remove `shadowElevation`+`graphicsLayer` combos | High |
| U4 | Branding lock (AGENTS.md) | everywhere | Audit white-on-gold & gold-on-white | Medium |
| U5 | Web admin styling parity | `src/app/engdadmin/AdminDashboard.tsx` | same design + contrast | High |

---

## 6. ADMIN PORTAL (web `/engdadmin`)
- Marketplace moderation (products, categories, stock, pricing).
- **Vendor Store Review**: approve/reject + KYC docs view + `autoVerifyVendors` toggle.
- Rewards/points config, real broadcast.

---

## Verification / acceptance
- Real-time Firestore snapshots everywhere (no static "in-box" mock for core flows).
- Wallet and Paystack totals always identical; points discount actually charged.
- Same auto-detected address string on every screen.
- Vendor lifecycle completes without human DB edit.
- All active-tab text legible in dark + light.
- Painless shadows (no sharp edges) on press.