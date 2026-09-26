# ESDISPATCH — FULL-SCOPE PRODUCTION ASSURANCE AUDIT

**Report date:** 2026-09-26
**Scope:** Android customer/rider/vendor app (`mobile/`), web admin + public site (`src/`), Vercel serverless (`api/`), Cloud Functions (`functions/`), Firestore/Storage rules, indexes, CI/CD, dependencies, privacy/compliance, cloud-cost exposure.
**Method:** Independent source-code audit of the current working tree (read-only), re-verification of all prior audit claims, live build/typecheck/vulnerability verification, evidence-based findings with `file:line` citations. Runtime/device walkthrough is marked "runtime-unverified" with manual test steps in Appendix B.
**Preceded by:** `APP_AUDIT.md`, `ESDISPATCH_FULL_SYSTEM_AUDIT.md`, `PRODUCTION_AUDIT.md`, `MICRO_UI_AUDIT.md`, `docs/audits/*`, `audit/*` (all re-verified in Appendix A).

---

## 1. EXECUTIVE SUMMARY (PLAIN LANGUAGE)

**Can a real customer successfully complete a delivery today?**
**Partially — as a demo, yes; as a business, no.** The core booking → payment → tracking chain does run end-to-end in code (journey J5 scored 4/5), and today's builds are green: `npx tsc --noEmit` exits 0, `gradle :app:compileDebugKotlin` is BUILD SUCCESSFUL. But the product fails the moment it has to be trusted with money, with other people's data, or with real users:

1. **Anyone on the internet can take over the admin panel.** The public `/engdadmin` page offers "Create an admin account" with **Super Admin** pre-selected, and Firestore rules grant super-admin purely by email domain (`AdminDashboard.tsx:1911`, `firestore.rules:13-19`). No hacking required — just visit the URL.
2. **The database is effectively open.** 21 `allow … if true` blocks in `firestore.rules` mean any unauthenticated client can read and write `deliveries`, `users` (including PINs, wallet balances, OTPs), `transactions`, and every subcollection (`firestore.rules:64,81-84,91-100,143-145`). This is a **regression** — an earlier, tighter version was loosened by commit `aad53c3`.
3. **Money is client-authoritative.** Wallet top-ups are credited from the app after a WebView callback with no gateway verification, even though a correct server verifier (`verifyPaymentAndTopUp`, `functions/src/index.ts:459`) exists and **is never called**. Paystack-funded marketplace orders never debit the buyer at all (`DeliveryViewModel.kt:7781-7900`). Riders are never paid: the payout calculation exists but is never credited (`DeliveryViewModel.kt:1360`, `FirebaseManager.kt:2185`).
4. **Secrets are burned.** The SMTP password is committed in 9 files **and shipped in the public browser bundle** (`vite.config.ts:21`, `EmailStudioTab.tsx:247`, `dist/assets/index-*.js`). The Android release signing keystore is committed with password fallback `android123` (`mobile/app/release-key.jks`, `build.gradle.kts:66-68`).
5. **The public website is broken and off-brand.** 4 of 5 header links and 8 footer links 404 (`Header.jsx:66-75`, `Footer.jsx:46-81`); the homepage hero reads "Engraced Dispatch", the share metadata advertises a **car rental** company, and the contact phone is `+234 800 123 4567` (`useSiteContent.js:5-33`, `dist/index.html`).
6. **Failure is silent everywhere.** 305 error references in the app, **zero retry affordances**, 69 empty catch blocks, payment and email flows that report success on failure (`FirebaseManager.kt:190`, `DeliveryViewModel.kt:4251`). A user cannot tell a working feature from a broken one.
7. **Quality gates are false-green.** `tsc` only checks 29 of 65 files (excludes the shipped SPA), unit tests run with `|| true`, lint is disabled, and a deploy workflow can ship with no tests at all.
8. **A single typing habit can cost hundreds of dollars a month.** Address autocomplete calls **Google Places on every debounced keystroke** — modeled at **$480–$1,690/month at 1,000 daily users** (`GeocoderUtils.kt:268-276`), plus leaking listeners, 3-second GPS writes, and 15 unbounded admin listeners.

**Bottom line:** the app's visual ambition and breadth are real, but the trust layer (security, money, failure handling, verification) is not yet built. The prior 2026-09-09 audits were roughly half-right: UX/navigation/build fixes landed (~75% fixed), while **security and money findings remain ~70% open, with two actively regressed.**

---

## 2. VERIFICATION RESULTS (RUN TODAY)

| Gate | Command | Result |
|---|---|---|
| Web typecheck | `npx tsc --noEmit` (repo root) | ✅ **exit 0** — but scope is limited: `tsconfig.json:27` includes only `src/app/**, src/lib/**, src/types/**`; `src/pages/**`, `src/components/**`, `src/App.tsx`, `src/main.tsx` are **never checked**. Full-`src` check surfaces **21 latent errors** (15× missing `motion/react` module, 6× LucideIcon type errors). |
| Android compile | `gradle :app:compileDebugKotlin` | ✅ **BUILD SUCCESSFUL** (9 s, all up-to-date) |
| Dependency vulnerabilities | `npm audit` | ❌ **24 vulnerabilities: 1 critical (next), 5 high (nodemailer, brace-expansion, browserslist, postcss, undici), 18 moderate** |
| Android unit tests | `gradle :app:testDebugUnitTest` | ⚠️ runs in CI but with `\|\| true` (`.github/workflows/build-and-test.yml:37`) — cannot fail |
| Git state | `git status --porcelain` | ❌ **25 modified/untracked files** on `main`, including `firestore.rules`, `DeliveryViewModel.kt`, `package.json`, and an **untracked production endpoint** `api/email/verification.js` |
| Release APK build | `assembleRelease` | Not run in this audit (R8 slow); CI produces it on every push |
| Device install / runtime flows | — | **Runtime-unverified** → Appendix B manual test scripts |

---

## 3. MATURITY SCORECARD (HONEST LAUNCH-READINESS /10)

| # | Area | Score | Justification |
|---|---|---:|---|
| 1 | Product completeness | **3** | 28 dead/unreachable components, 9 unreachable routes, wrong brand on public site, rider earnings never credited |
| 2 | Functional correctness | **3** | Main journeys mostly chain in code, but money paths (Paystack debit, payout credit, refund parity) are wrong or dead |
| 3 | UX / UI | **4** | Premium visual direction and strong admin shipments tab; 0 retry affordances, fake stats, error→"not found" lies |
| 4 | Accessibility | **2** | 146 `contentDescription = null`, 12 aria-labels vs 41 icon-only web buttons, no reduced-motion, 27 touch targets <44dp |
| 5 | Architecture / code quality | **3** | Two god files (10,749 + 8,461 lines) ≈20% of codebase; 4 parallel email implementations; 69 empty catches |
| 6 | Data integrity | **2** | 4 competing status machines, clock-derived IDs that silently overwrite orders, mixed timestamp types, client-writable balances |
| 7 | Security | **1** | Open admin signup, 21 world-writable rule blocks, secrets in git + browser bundle, OTP returned in API responses, no rate limits |
| 8 | Privacy / compliance | **2** | Privacy policy & terms components exist but are unrouted; PII world-readable; no consent link at signup; targetSdk 34 |
| 9 | Reliability / offline | **2** | Offline queue never populated, Room store self-destructs on error, no idempotency, TOCTOU status validation |
| 10 | Mobile performance | **4** | Startup attaches ~15 listeners; pull-to-refresh duplicates them unboundedly; main-thread decode/geocode; 25.8 MB APK |
| 11 | Web performance | **3** | One **2,916 KB (836 KB gz)** JS chunk ships admin code to every visitor; 34 MB of images; 421 KB blocking CSS |
| 12 | Cost efficiency | **2** | Google Places on every keystroke ($480–$1,690/mo @1k DAU), 3 s GPS writes, leaked tracking listener, no TTL |
| 13 | Scalability | **3** | 500-op batch wall at ~500 users, full-table admin listeners, notification fan-out to all users, single-doc hotspots |
| 14 | Testing / CI | **1** | ~10 real tests total; tests gated with `\|\| true`; typecheck excludes shipped app; no lint; deploy not gated on tests |
| 15 | Observability | **2** | Crashlytics present but symbolication **off**, no analytics events, no web error boundary, no alerting, success-on-failure reporting |
| 16 | Parity / release readiness | **2** | Three deploy paths, two hosting targets, frozen `versionCode=2`, no staging, 25 uncommitted files, docs point at wrong project path |

---

## 4. TOP 20 LAUNCH BLOCKERS (RANKED)

| # | Blocker | Evidence | Why it blocks launch |
|---|---|---|---|
| B01 | Public admin self-signup mints `super_admin` | `AdminDashboard.tsx:1911,2456-2465,2588` | Total takeover of refunds, payouts, roles, audit log |
| B02 | Super-admin granted by email domain alone (no `email_verified`) | `firestore.rules:13-19,35-38` | `attacker@esdispatch.com` signup = super admin |
| B03 | Firestore rules world-writable (21 `if true`) | `firestore.rules:64,81-84,91-100,103-115,143-145` | Anyone can forge deliveries, read OTPs/PII, rewrite wallets |
| B04 | `users` create has zero field validation → self-grant `role` | `firestore.rules:62-65` | Privilege escalation from any authenticated account |
| B05 | Owner can self-write `walletBalance` / `pin` / `status` | `firestore.rules:68-76` | Direct money printing from any phone |
| B06 | `system_config.apiBaseUrl` writable by any authed user | `firestore.rules:282-285`, `DeliveryViewModel.kt:310-316,528-545` | Fleet-wide OTP interception by redirecting the API base |
| B07 | SMTP password committed ×9 and shipped in browser bundle | `vite.config.ts:21`, `EmailStudioTab.tsx:247`, `dist/assets/index-*.js` | Mailbox takeover, phishing as the brand |
| B08 | Android release keystore committed, password fallback `android123` | `mobile/app/release-key.jks`, `build.gradle.kts:66-68` | Malicious signed update accepted by every installed app |
| B09 | OTP returned in unauthenticated, un-rate-limited API response | `api/email/verification.js:324-329` | Account takeover + open mail relay |
| B10 | Open email relay (`to/subject/html/From` from request body) | `api/email/test-send.js:46-89` | Domain-reputation destruction, phishing |
| B11 | `smtp-verify` SSRF sends real SMTP credentials to attacker host | `api/email/smtp-verify.ts:50-57` | Credential theft + internal port scanning |
| B12 | Wallet top-up credited client-side; server verifier never called | `WalletViewModel.kt:44-69`, `functions/src/index.ts:459` | Arbitrary wallet inflation |
| B13 | Paystack marketplace orders never debited | `DeliveryViewModel.kt:7781-7900` | Revenue received, ledger silent; vendors paid from nothing |
| B14 | Riders never credited (payout computed, unused) | `DeliveryViewModel.kt:1360`, `FirebaseManager.kt:2185-2186` | Riders permanently earn ₦0 → trust/legal exposure |
| B15 | Delivery OTP: 4-digit, client-generated, world-readable, shown on public tracking page | `DeliveryViewModel.kt:1672`, `firestore.rules:93`, `track/[id]/page.tsx:50` | Proof of delivery is worthless + data exposure |
| B16 | Clock-derived order IDs overwrite real orders every ~17 minutes | `DeliveryViewModel.kt:5199`, `:7669` | Paid orders silently vanish (data loss) |
| B17 | Two missing composite indexes break vendor orders + auto-dispatch | `firestore.indexes.json` vs `DeliveryViewModel.kt:8110-8113`, `functions/src/index.ts:100-102` | Production queries fail with `failed-precondition` |
| B18 | Rider notification tap crashes the app (route `RiderDeliveries` doesn't exist) | `MainActivity.kt:178,405-415` | Riders miss dispatch alerts; app dies |
| B19 | Public site nav → 404, wrong brand, car-rental share metadata | `Header.jsx:66-75`, `Footer.jsx:46-81`, `useSiteContent.js:5-33` | First impression is a broken, different company |
| B20 | Quality gates false-green (tests `\|\| true`, partial typecheck, lint off, deploy ungated) | `build-and-test.yml:37`, `tsconfig.json:27`, `build.gradle.kts:111-114` | Bad code ships believing it is verified |

---

## 5. BROKEN-JOURNEY MAP (SCORE: 40/90 ≈ 1.8/5)

Scale: 0 blocked · 1 broken/misleading · 2 usable with major gaps · 3 works with notable gaps · 4 solid · 5 exemplary. **Works = static code trace; runtime-unverified (Appendix B).**

| # | Journey | Score | Failing step (evidence) |
|---|---|---:|---|
| J1 | Visitor understands brand | **1** | Hero/footer default "Engraced Dispatch"; og meta = "Car Rental" (`useSiteContent.js:5,27`, `dist/index.html`) |
| J2 | Visitor navigates site | **0** | 12 links → 404 (`Header.jsx:66-75`, `Footer.jsx:46-81`); relative `href="contact-us"` (`Hero.jsx:63`) |
| J3 | Visitor downloads app | **1** | Store URLs empty → `href="#"` (`useSiteContent.js:30-31`, `Hero.jsx:78-79`) |
| J4 | Customer signs up / logs in | **2** | Works, but password = email + 4-digit PIN (`FirebaseManager.kt:141`); raw Firebase errors shown |
| J5 | Customer books & pays | **4** | Full chain works (`DeliveryViewModel.kt:5236`); gaps: client OTP `:1672`, payment race `ExpressBookingScreen.kt:1364-1378`, guest "confirmed" with no server record `:5388-5400` |
| J6 | Customer live-tracks | **3** | Rich map, correct gold/ESRI labels; 47 error refs, **0 retry** (`TrackingScreen.kt`) |
| J7 | Customer order history | **3** | Clean, AGENTS-compliant (`OrderLogsScreen.kt:106`); 0 error states |
| J8 | Guest tracks by waybill (web) | **2** | Listener **error shown as "Waybill Not Found"** (`PublicTrackingPage.tsx:107-109`); OTP rendered publicly `:404-417` |
| J9 | Rider goes online → receives dispatch | **4** | Toggle → `setRiderOnlineStatus` → live offers (`RiderScreens.kt:788,905,1102`) |
| J10 | Rider accept → POD → complete | **3** | Ladder works; silent no-op if unsigned-in (`DeliveryViewModel.kt:1357`); OTP verified client-side |
| J11 | Rider sees earnings | **0** | **Never credited** — payout computed and unused (`DeliveryViewModel.kt:1360`, `FirebaseManager.kt:2185`) |
| J12 | Rider taps dispatch notification | **0** | Navigates to non-existent route → **crash** (`MainActivity.kt:178,405-415`) |
| J13 | Vendor storefront → products → orders | **3** | Works; `VendorProfileScreen` has **0 loading, 0 error** states |
| J14 | Admin signs in | **0** | Open self-signup + self-heal grant `super_admin` (`AdminDashboard.tsx:2455-2482`) |
| J15 | Admin dispatch → assign → status | **4** | Strongest flow, retry helper, rider guard (`AdminDashboard.tsx:5541,5346`) |
| J16 | Admin cancel → refund | **3** | Modal path refunds correctly; **row-dropdown path refunds nothing**; failures `console.error` only (`:616-706` vs `:5423-5449`) |
| J17 | Admin approves tip payout | **3** | Logic sound but starts from ₦0 (J11) |
| J18 | Admin email campaign | **2** | Powerful DNS/HTML tools; **fabricates SPF/DMARC green** on failure (`EmailStudioTab.tsx:272-287`); SMTP creds in client |

---

## 6. SCREEN × STATE COVERAGE (ANDROID)

Counts = occurrences per file; **⌘ = zero**.

| Screen | Loading | Empty | Error | **Retry** | Offline | Permission |
|---|---:|---:|---:|:-:|---:|---:|
| DashboardScreen | 1 | 3 | 19 | ⌘ | ⌘ | ⌘ |
| OrderLogsScreen | 1 | 2 | 0 | ⌘ | 2 | ⌘ |
| MarketplaceScreen | 1 | 7 | 1 | ⌘ | ⌘ | ⌘ |
| TrackingScreen | 1 | 5 | 47 | ⌘ | 1 | 5 |
| ProfileScreens (9 screens) | 13 | 8 | 39 | ⌘ | 3 | 5 |
| RiderScreens | 7 | 6 | 40 | ⌘ | 14 | 6 |
| VendorPortalScreen | 34 | 4 | **0** | ⌘ | ⌘ | ⌘ |
| VendorProfileScreen | **0** | 2 | **0** | ⌘ | ⌘ | ⌘ |
| ProofOfDeliveryScreen | 3 | 1 | 23 | ⌘ | 1 | ⌘ |
| ExpressBookingScreen | 1 | 2 | 16 | ⌘ | ⌘ | 2 |
| AuthScreens | 4 | 2 | 95 | ⌘ | 2 | ⌘ |
| **TOTAL (38 composables)** | **68** | **56** | **305** | **0** | **26** | **23** |

**Zero "Retry"/"Try again"/"Reload" affordances exist in the entire Android codebase.** Web: admin `shipments` tab has retry ✅; `/track` maps error→notFound ❌; public site has no error boundary ❌.

---

## 7. FINDINGS REGISTER — SECURITY & PRIVACY (SEC)

**Severity: S1 critical · S2 high · S3 medium · S4 low.**

### S1 — Critical

| ID | Finding | Evidence | Fix |
|---|---|---|---|
| SEC-01 | Public `/engdadmin` self-signup creates `super_admin` (default role) | `AdminDashboard.tsx:1911,2456-2465,2582-2589`; `middleware.ts:11-17` does not block (and is not deployed) | Remove self-signup; provision admins via Admin SDK/claims only |
| SEC-02 | Super-admin by email domain, no `emailVerified` check | `firestore.rules:13-19,35-38` | Require `email_verified` + custom claim; delete domain branches |
| SEC-03 | `users` create unvalidated → arbitrary `role` at signup + self-heal | `firestore.rules:62-65`; `AdminDashboard.tsx:2472-2482` | Forbid role keys on create; roles only via Admin SDK |
| SEC-04 | Owner can self-write `walletBalance`, `pin`, `status` | `firestore.rules:68-76`; wallet written client-side `FirebaseManager.kt:681` | Profile-only allowlist; money/PIN via server txn |
| SEC-05 | `system_config` writable by any authed user → `apiBaseUrl` hijack (OTP interception, maintenance-mode DoS, surge manipulation) | `firestore.rules:282-285`; `DeliveryViewModel.kt:290,310-316,528-545,4225` | `allow write: if isAdmin()`; pin allowed hosts client-side |
| SEC-06 | All user profiles + **all subcollections** world read/write (PIN, wallet, OTP, cards, transactions) | `firestore.rules:64,81-84`; OTP at `FirebaseManager.kt:2943-2956`; PIN gate `DeliveryViewModel.kt:618-640` | Owner/admin scoping per subcollection; drop `if true` wildcard |
| SEC-07 | `deliveries` / `parcels` / `public_tracking` open read+write (addresses, phones, POD URLs, OTP, chat) | `firestore.rules:91-115`; triggers amplify: `functions/src/index.ts:86,199,1032` | Owner/rider/admin rules; field guards on `otpCode` |
| SEC-08 | Financial collections open: `transactions` unauthenticated; withdrawals/refunds/payouts any-authed | `firestore.rules:143-145,153-174,268-276` (contrast correct `system_ledger:182-185`) | Owner-or-admin read; backend-only status/amount |
| SEC-09 | OTP returned in HTTP response, no auth, no rate limit; CORS `*`; also in email subject | `api/email/verification.js:233,247,273,324-329`; `src/app/api/email/verification/route.ts:91-96` (dead in prod) | Never return OTP; server-side hash; rate limit; auth |
| SEC-10 | Unauthenticated open relay: `to/subject/html/from` from request body | `api/email/test-send.js:46-89`; duplicate `src/app/api/email/test-send/route.ts:11` | Delete or admin-gate with allow-lists |
| SEC-11 | `smtp-verify` SSRF: client-controlled host + real credentials, `rejectUnauthorized:false` | `api/email/smtp-verify.ts:50-57` | Delete from prod; host allow-list; TLS verify on |
| SEC-12 | SMTP password in 9 source files + browser bundle + git history | `vite.config.ts:21,199,307`, `EmailStudioTab.tsx:247`, `functions/src/emails/emailTransporter.ts:50`, `api/email/smtp-verify.ts:15`, `dist/assets/index-Ayi4vZXR.js:569`, commits `6b3fd9c`,`26d78db` | **Rotate now**; server-only env/Secret Manager; purge |
| SEC-13 | Release keystore committed + password fallback `android123` | `mobile/app/release-key.jks` (commit `368d041`), `build.gradle.kts:60-68`, workflow fallback | Rotate upload key; env-only; fail build if absent |

### S2 — High

| ID | Finding | Evidence |
|---|---|---|
| SEC-14 | Rating/tip update clause has **no owner check** → cross-user writes | `firestore.rules:74-75` |
| SEC-15 | Fleet telemetry/attendance/driver docs any-authed → GPS spoofing, attendance fraud | `firestore.rules:191-224` |
| SEC-16 | Any authed user writes `riders/{id}` → trigger downgrades victim's `users.role` | `firestore.rules:216-224`; `functions/src/index.ts:757-778` |
| SEC-17 | Reviews/ratings world-writable; marketplace products/stores updatable by anyone | `firestore.rules:121-137,244-259` |
| SEC-18 | All notifications world-readable | `firestore.rules:349-352` |
| SEC-19 | Loyalty/referral/support data forgeable (self-grant points/credits) | `firestore.rules:230-238,369-402` |
| SEC-20 | Delivery OTP: 4-digit, plaintext, world-readable, **shown on public tracking page**, attempt counter resettable (brute-force 10⁴) | `FirebaseManager.kt:2173-2178`; `track/[id]/page.tsx:50` |
| SEC-21 | Mobile email OTP generated/stored/compared entirely on-device | `AuthViewModel.kt` (`active_verification_otp` pref); `FirebaseManager.kt:2943-2956` |
| SEC-22 | Suspension cosmetic: no `revokeRefreshTokens`, no Auth disable, rules never check `status` | grep 0 matches in `firestore.rules`, `AuthScreens.kt`, `functions/src` |
| SEC-23 | Callable functions authorize off client-writable `users.role` | `functions/src/index.ts:659-660,687-693` |
| SEC-24 | Payment mock bypass unless `NODE_ENV==='production'` | `functions/src/index.ts:477-480` |
| SEC-25 | Storage: POD/chat/product uploads writable by any authed user; **`pickup_photos` rule missing** (default-deny → silent failure) | `storage.rules:40-73`; `DeliveryViewModel.kt:1429` |

### S3 — Medium (summary)

| ID | Finding |
|---|---|
| SEC-26 | No CSP/HSTS; only framing/sniffing headers (`vercel.json:9-19`) |
| SEC-27 | Privacy & Terms components **never routed**; mobile consent checkbox has no link (`AuthScreens.kt:1594-1610`, `PrivacyPanel.tsx` unreferenced) |
| SEC-28 | Audit trail forgeable: `audit_logs create: if isAuth()` (`firestore.rules:359-363`) |
| SEC-29 | Public `contacts`/`subscribers` create `if true` → spam + notification fan-out trigger (`firestore.rules:408-418`, `functions/src/index.ts:873-886`); `isReasonableWrite()` defined but never used |
| SEC-30 | TLS verification disabled on every SMTP transport (`rejectUnauthorized:false` ×6) |
| SEC-31 | **Next.js surface (`middleware.ts`, `src/app/api/**`) is dead in production** — deploy is Vite (`package.json "build": "vite build"`, `vercel.json`), so header/auth middleware never runs |
| SEC-32 | Functions `sendEmailOtp`/`verifyEmailOtp`/`testSmtpConnection` unauthenticated (`functions/src/index.ts:913,972,1011`) |
| SEC-33 | HMAC pepper + Google/Gemini API keys committed (`otpService.ts:11`, `build.gradle.kts:45-50`, `AdminDispatchBookingModal.tsx:285`) |
| SEC-34 | Weak local crypto: static-salt SHA-256, plaintext PIN fallback, XOR "legacy encryption", no PIN lockout (`DeliveryViewModel.kt:4383-4403,8360-8409`) |
| SEC-35 | Android: `allowBackup="true"` with `esdispatch_prefs` (OTP/PIN) not excluded; Room DB unencrypted |
| SEC-36 | WebView: universal file-URL access, `MIXED_CONTENT_ALWAYS_ALLOW`, JS interface with interpolated email (`TrackingScreen.kt`, `ProfileScreens.kt:6524-6660`) |
| SEC-37 | `incident_reports` + `fleet_broadcasts` written in code but **absent from rules** → default-deny, features silently broken (`FirebaseManager.kt:468,1671,2033`, `DeliveryViewModel.kt:7170`) |

### S4 — Low
SEC-38 no-op predicate `isAuth()||true` (`firestore.rules:354-355`) · SEC-39 dead `isReasonableWrite()` · SEC-40 PII in server logs · SEC-41 four conflicting production domains · SEC-42 no `networkSecurityConfig`.

**OWASP MASVS:** 12 FAIL · 9 PARTIAL · 4 PASS (full table in agent output; failures concentrate in STORAGE-7/9, CRYPTO-1/2/3, AUTH-1/2/4/5, NETWORK-3, PRIVACY-1/3, PROCESS-2).

---

## 8. FINDINGS REGISTER — DATA MODEL, INTEGRITY & PARITY (DATA)

### S1 — Critical

| ID | Finding | Evidence |
|---|---|---|
| DATA-01 | Open rules on `users`/`deliveries`/`transactions`/marketplace → balance self-writes, status forgery, ledger forgery, OTP/PII reads | `firestore.rules:64,68-76,81-84,93-94,143-144,246,268-269` |
| DATA-01c | Withdrawals/refunds/payout requests self-approvable by any authed user | `firestore.rules:153-174`; approval is client `updateDoc` |
| DATA-02 | `fleet_broadcasts` + `incident_reports` have **no rules** → default deny; errors swallowed → features silently write nothing | `FirebaseManager.kt:468,1671,2033`; `DeliveryViewModel.kt:7170` |
| DATA-03 | SMTP password in source AND readable via `system_settings` `if true` | `emailTransporter.ts:50`; `firestore.rules:287-289` |
| DATA-04 | Email-domain super-admin in rules | `firestore.rules:13-26,35-39` |
| DATA-14 | **Four competing status machines** (Android 19 states, admin 13, public 5-step, functions literal) with contradictory edges | `FirebaseManager.kt:1885-1917` vs `AdminDashboard.tsx:5325-5340` vs `track/[id]/page.tsx:150-165` vs `functions/src/index.ts:1041` |
| DATA-15 | Admin row-dropdown status change: **no transition validation, no refund** — cancels and keeps the money; modal path refunds fully; customer path deducts fee | `AdminDashboard.tsx:616-706` vs `:5346-5363,5423-5449` vs `DeliveryViewModel.kt:6109-6128` |
| DATA-22 | Paystack top-up credited client-side; `verifyPaymentAndTopUp` never called; bridge `success()` replayable | `WalletViewModel.kt:23-69`; `functions/src/index.ts:459`; `ProfileScreens.kt:6661` |
| DATA-23 | **Paystack-funded marketplace orders never debit or ledger a kobo** — stock decremented, vendors/rider credited from nothing | `DeliveryViewModel.kt:7670,7781-7900` |
| DATA-24 | Three different cancellation money outcomes for one event | `DeliveryViewModel.kt:6109-6128` vs `AdminDashboard.tsx:5423-5449` vs `:616-706` |
| DATA-26 | Absolute client-cache sync of `walletBalance`/`loyaltyPoints`/`deliveryCount` clobbers server increments; `initWelcomeGiftForNewUser` zeroes balance | `FirebaseManager.kt:1569-1600`; `DeliveryViewModel.kt:3484-3540,3662-3668` |
| DATA-30 | Withdrawal debit done client-side; approval rules allow self-write; `processVendorPayout` never invoked | `FirebaseManager.kt:649-700`; `functions/src/index.ts:682` |
| DATA-32/33 | **Missing composite indexes**: `marketplace_orders(vendorId, createdAt)` and `users(role, isOnline)` → production queries fail; 5 defined indexes unused | `firestore.indexes.json`; `DeliveryViewModel.kt:8110-8113`; `functions/src/index.ts:100-102` |
| DATA-37/38 | IDs from `System.currentTimeMillis().substring(8)` / `takeLast(6)` → **collision ~every 17 min silently overwrites a paid order** | `DeliveryViewModel.kt:5199,7669` |

### S2 — High

| ID | Finding |
|---|---|
| DATA-05 | Ledger `timestamp` mixes Long/`Timestamp.now()`/`Date.now()` → wallet history sorts **wrong forever** (`FirebaseManager.kt:627` vs `AdminDashboard.tsx:3279` vs `functions/src/index.ts:305`) |
| DATA-07 | Admin refund writes root `transactions` only — customer's wallet history shows no refund line (`AdminDashboard.tsx:5434` vs `:3270,3283`) |
| DATA-16 | Unknown status silently maps to `PENDING` — delivered parcel reappears as new (`FirebaseManager.kt:815-816,1933-1937`) |
| DATA-17 | TOCTOU: status validated outside the transaction (`FirebaseManager.kt:1930-1965`) |
| DATA-18 | Secure `verifyDeliveryOtp` callable dead (0 `httpsCallable` callers repo-wide) AND would write wrong status; enforced path is client-side compare (`functions/src/index.ts:580,625-630`) |
| DATA-20 | Public tracking shows handover-verified/returned/cancelled as **"Order Booked"**; `includes("ARRIVED")` matches `ARRIVED_PICKUP` (`track/[id]/page.tsx:150-165`) |
| DATA-25 | Loyalty awarded unconditionally in two admin paths with fallback inflation `\|\|350` → 0 becomes 365, double/triple awards (`AdminDashboard.tsx:647-667,5396-5415`) |
| DATA-27 | Rating+tip re-submission **double-charges customer, double-pays rider** (no `isRated` guard in txn; ledger written outside txn) (`FirebaseManager.kt:2347-2477`) |
| DATA-28 | Escrow settlement check-then-act, not transactional → double payout on retry race (`functions/src/index.ts:245-323`) |
| DATA-43/44 | Timestamps almost all client wall-clock (`Timestamp.now()`×91 admin, `System.currentTimeMillis()`×49 app) vs 23 `serverTimestamp` in functions |
| DATA-48 | Admin hard-delete leaves orphaned deliveries/orders/chats; subcollection sweep un-awaited with `catch(()=>{})` (`AdminDashboard.tsx:3000-3017`) |
| DATA-51 | `audit_logs create: if isAuth()` → forged immutable audit entries (`firestore.rules:359-362`) |

### S3/S4 (selected)
DATA-06 doc-id ≠ `id` ≠ `reference` · DATA-08 tip ledger missing `type/status/userId` · DATA-10 dead `EXPRESS` branch pays **fabricated ₦2,500/₦1,500** · DATA-11 OTP email can never render (field mismatch) · DATA-12 admin interface missing escrow/POD/batch fields · DATA-13 `userId:"guest_user"` orders orphaned · DATA-19 `'IN_TRANSIT'` vs `'TRANSIT'` dead email branch · DATA-21 web status tokens cover only 13/19 · DATA-29 marketplace fee hardcoded ₦1,500 ignoring configured pricing · DATA-45 `dateString` "Today" vs ISO filters disagree · DATA-46 presence client-asserted · DATA-49 `isDeleted` never set though filtered/indexed · DATA-50 no TTL anywhere (unbounded growth) · DATA-53 `parcels` has no delete rule · DATA-36 `PLATFORM_PARITY_GUIDE.md` claims iOS parity — **no iOS code exists**; wrong package names.

**Parity verdict:** status vocabulary (4 variants), wallet mutation (absolute vs delta), ledger shape, cancel refund, loyalty, payment authority, OTP handover, timestamps — **all broken across surfaces.**

---

## 9. FINDINGS REGISTER — CLOUD COST & SCALABILITY (COST / SCALE)

### Cost findings (monthly estimates at 1,000 DAU; Google pricing assumed $0.06/100k reads, $0.18/100k writes, Places $0.005–0.0176/req, Geocoding $0.005/req)

| ID | Sev | Finding / evidence | Trigger | Est. @1k DAU | Fix |
|---|---|---|---|---|---|
| COST-01 | Critical | **Google Places on every debounced keystroke** — `GeocoderUtils.kt:268-276` calls tier-3 Google unconditionally; 250 ms debounce `AddressAutocompleteField.kt:95` | Each address char ≥2 | **$480–$1,690/mo** (→ $4.8k–$16.9k at 10k DAU) | Only when local tiers under-return; min 4 chars; prefix cache; session tokens |
| COST-02 | Critical | Google Geocoding tried **before** Mapbox/Android; unbounded caches `GeocoderUtils.kt:398-422,508,121-122` | GPS button, reverse geocode | $10–$75 | Reorder tiers; bound caches |
| COST-03 | High | GPS 3 s/2 m → 3 Firestore writes per fix (`LocationService.kt:104-106,163-195`) | Rider on duty | $8.10 (+listener re-reads $4.04) | 10–15 s/10 m; write only `fleet_locations` |
| COST-04 | High | Rider heartbeat every 60 s → 3 writes (`DeliveryViewModel.kt:939-946`, `FirebaseManager.kt:445-455`, trigger `index.ts:757`) | Rider online | $3.50 | 5 min; single doc |
| COST-05 | High | **Two** `onUpdate` triggers on `deliveries` (`index.ts:199` and `:1032` — latter has no early-return) fire on every GPS write | Every delivery write | ~$1.50 | Merge; transition guard |
| COST-06 | High | Admin dashboard opens **15 whole-collection listeners** + 2-min presence heartbeat (`AdminDashboard.tsx:2130-2145,2154…2429`) | Opening admin | $2.34 (grows with data) | Tab-scoped `limit()` queries |
| COST-07 | High | App session: 53 `addSnapshotListener` sites, unbounded queries, duplicate banner listeners (`DeliveryViewModel.kt:2981,3283,3196,5989…`) | App launch | **$10.80** | `limit()`; lazy per-screen attach; dedupe |
| COST-08 | High | Public tracking re-reads delivery every telemetry write (`PublicTrackingPage.tsx:85-95`) | Open tracking view | $0.86 | Throttle writes; cache projection |
| COST-09/10 | Med | Notification fan-out = read+write **every user doc** (`index.ts:796-814`); admin broadcast one 500-op batch (`DeliveryViewModel.kt:5701-5725`) | Any notification/broadcast | ~$0.25, **unbounded under abuse** | Topic/segment fan-out; chunked batches |
| COST-11 | Med | Public site: same CMS doc listened twice per view (`Hero.jsx:6,9`, `Footer.jsx:14,18`) | Page view | $0.14 | Hoist provider; cached getDocs |
| COST-12 | Med | **2,916 KB single JS chunk** + 34 MB images + 421 KB CSS | Every visit | ~$0 → $20+ at 10k | Route lazy-load; WebP; purge |
| COST-13 | Med | **No TTL** on notifications/audit/timeline/chats | Time | Compounding storage | TTL policies |
| COST-14 | Med | Email per status transition, no dedupe; 3 unauthenticated email endpoints | Status change / abuse | ~$7 + unbounded abuse | Auth + rate limits; dedupe keys |
| COST-15/16/17/18/19 | Low | Welcome push to topic `all_users` (`index.ts:73`); timestamped POD photos never deleted (`DeliveryViewModel.kt:1564`); base64 avatar stored twice (`:4116-4151`); tracking re-geocodes destination every GPS fix (`TrackingScreen.kt:675-682`); rules `exists()`+`get()` per admin write (`firestore.rules:31-46`) | — | pennies–$0.50 | Topic targeting; canonical object names; URL-only avatars; memoize; custom claims |

**Modeled totals:** 100 DAU ≈ **$5–8/mo** (often $0 inside free tier) · 1,000 DAU ≈ **$570–$1,870/mo** (Firestore-only ≈ $31) · 10,000 DAU ≈ **$5,350–$17,450/mo**. **Google Places alone is 85–95% of the bill.**

### Scalability findings

| ID | Sev | Finding |
|---|---|---|
| SCALE-01 | Critical | Unauthenticated writes to `deliveries` fire auto-dispatch fan-out + 2 status triggers + emails → cost-abuse vector (`firestore.rules:94`, `index.ts:86-160`) |
| SCALE-02 | Critical | Three unauthenticated email endpoints = mail-relay + OTP-harvest + SMTP-credential abuse |
| SCALE-03 | Critical | **Leaked listener**: inner `onSnapshot` in public tracking never captured/unsubscribed → permanent billed reads on every failed lookup (`PublicTrackingPage.tsx:95,113`) |
| SCALE-04 | High | Admin broadcast uses single `db.batch()` → **fails silently at >500 users** (`DeliveryViewModel.kt:5701-5725`) |
| SCALE-05 | High | 7+ listeners never registered for cleanup; activity-scoped VM → live for whole session |
| SCALE-06 | High | GPS writes to `users` doc re-fire profile collector → **cancel/re-attach 3 listeners every ~2.5 s** (`DeliveryViewModel.kt:3093-3097`, `LocationService.kt:178`) |
| SCALE-07 | High | Hero banner listener attached **twice** (`DeliveryViewModel.kt:2981,3283`) |
| SCALE-08 | High | Admin purge button: O(all users × their deliveries) unbatched (`AdminDashboard.tsx:3315-3445`) |
| SCALE-10/11 | Med | Admin code (654 KB) eagerly in public bundle (`App.tsx:4-5`); duplicate delivery triggers without idempotency |
| SCALE-12 | Med | Live map rebuilds **all** markers + `fitBounds` every 2.5 s batch → unusable at scale (`LiveTrackingMap.tsx:97-178`) |
| SCALE-13 | Med | `billingCircuitBreaker.js` **disables billing at 100% budget → total outage** |
| SCALE-14/15/16 | Low | Unbounded chats/notifications queries; dead `src/app/api` duplicates confuse deploys; keystore committed |

---

## 10. FINDINGS REGISTER — UX, COMPLETENESS & AGENTS.md COMPLIANCE (UX)

### S1 — Critical

| ID | Finding | Evidence |
|---|---|---|
| UX-001 | Rider notification tap → **crash** (route `RiderDeliveries` not in NavHost, no try/catch) | `MainActivity.kt:178,405-415` |
| UX-002 | Public site: 12 nav links → 404; relative link missing `/` | `Header.jsx:66-75`, `Footer.jsx:46-81`, `Hero.jsx:63` |
| UX-003 | Unauthenticated visitor creates `super_admin` | `AdminDashboard.tsx:2588-2589,2456-2465` |
| UX-004 | Missing users doc → silent self-heal writes `super_admin` | `AdminDashboard.tsx:2472-2482` |
| UX-005 | Password = deterministic function of email + 4-digit PIN (~10k combos) | `FirebaseManager.kt:141`, `AdminDashboard.tsx:2755` |
| UX-006 | Plaintext PINs written by web, hashed by mobile; plaintext compare fallback | `AdminDashboard.tsx:3109-3112` vs `DeliveryViewModel.kt:4327,8384-8393` |
| UX-007 | SMTP creds committed + shipped to browser | `vite.config.ts`, `EmailStudioTab.tsx:246-248` |
| UX-008 | **Production homepage renders wrong brand** "Engraced Dispatch" + placeholder phone `+234 800 123 4567` | `useSiteContent.js:5-33`, `Brand.jsx:14`, `Cta.jsx:15-17` |

### S2 — High (selected)

| ID | Finding |
|---|---|
| UX-009 | Share metadata advertises "Engraced Logistics — Best **Car Rental** in Benin City", Unsplash car photo (`dist/index.html`) |
| UX-010 | **0 retry affordances app-wide** vs 305 error references |
| UX-011 | Admin listener failures console-only → silent stale data (`AdminDashboard.tsx:610,670,702`) |
| UX-012 | Errors reported as "not found" (`PublicTrackingPage.tsx:107-109`, `AdminDashboard.tsx:5953`) |
| UX-013 | **Rider earnings never credited** — payout computed, unused |
| UX-014 | POD completion silently no-ops when unsigned-in (`DeliveryViewModel.kt:1357`) |
| UX-016 | 27 touch targets <44dp (18dp map controls, 40dp back button) |
| UX-017 | Gold text on light surfaces ×12 in admin (≈1.7:1 contrast) |
| UX-018 | Fabricated "Promo Savings" stat: `delivered*750 + loyaltyPoints*10` (`DashboardScreen.kt:574`) |
| UX-019 | Seed personas (Marcus Vance, Sarah Jenkins) + fake earnings rendered as real (`DeliveryViewModel.kt:680-693`, `FirebaseManager.kt:2367-2371`) |
| UX-020 | Fake public hero stats (1,247 deliveries, 76%, 4.9★) animated as real (`Hero.jsx:134-145`) |
| UX-021/022 | Debug/test copy and raw Firebase errors in production UI (`AuthScreens.kt:1437`, `ProfileScreens.kt:7068,7105`, `FirebaseManager.kt:165,243,266,294,180`) |
| UX-023 | Default 4.9★ shown before any review (`DeliveryViewModel.kt:200`) |
| UX-024 | 146 `contentDescription=null`; web 12 aria-labels vs 41 icon-only buttons; 3 `focus-visible` total |
| UX-025 | No reduced-motion support on either platform |
| UX-026 | Store badges dead links (`href="#"`) |

### S3 — Moderate (selected)
UX-027 hero button says "Marketplace" not "Market" and Marketplace route silently redirects to Dashboard (`DashboardScreen.kt:1374,1408`, `MainActivity.kt:277-278`) · UX-028 **9 unreachable routes** (BookingForm/Details/Selection, Scanner, CustomerAssistant, RiderReview, Promotions, Referral, RiderDashboard) · UX-029 3 dead screens · UX-030 **28 unreferenced components including the mandated shared primitives** (`design-system/*` — 0 imports) · UX-031 unapproved golds `#F5A623/#FFC542/#F59E0B` · UX-032 public site has no dark mode · UX-033 admin login prefills `admin@engraced.com` · UX-034 four conflicting production domains · UX-035 CMS defaults Engraced-branded · UX-036 OTP generated client-side `(1000..9999).random()`.

### S4 — Hygiene
UX-037 25 uncommitted files · UX-038 Tailwind v4 syntax in dead `src/index.css` · UX-039 dead Next surface invites fixing wrong file · UX-040 header title clips long Benin addresses.

### AGENTS.md rule compliance: **12 PASS · 4 PARTIAL · 9 FAIL / 25**

| FAIL rules | Evidence |
|---|---|
| Branding lock (ESDispatch only) | "Engraced Dispatch" in defaults, static copy, og meta, CMS |
| Hero buttons (Wallet + Market) | Label "Marketplace"; Tracking fallback; route redirect |
| No gold-on-white (web) | 12 admin instances ~1.7:1 contrast |
| Product-language rule | Debug/test strings, raw Firebase errors |
| Empty-state rule | 0 retries; error→not-found |
| ≥44dp touch targets | 27 IconButtons under 44dp |
| Motion honesty | Fabricated promo savings, fake hero stats |
| Reduced-motion | 0 support |
| Shared primitives | Mandated components exist, **0 imports** |
| Accessibility labels | 146 null descriptions; 12/41 aria-labels |
| Deploy discipline | 25 uncommitted files incl. `firestore.rules` |
| Schema parity | PIN hashing diverges web vs mobile |

**PASS highlights:** splash/preloader spec, header theming, dashboard section order, HeroCarousel shadows/indicators, tracking gold + ESRI streets, OrderLogs clean layout, Settings 5 domains, icon semantics, interaction physics tokens.

---

## 11. FINDINGS REGISTER — ARCHITECTURE, RELIABILITY & ERROR HANDLING (ARCH / RLB)

**Top-15 largest files:** `AdminDashboard.tsx` **10,749** · `DeliveryViewModel.kt` **8,461** · `ProfileScreens.kt` 7,160 · `TrackingScreen.kt` 5,784 · `DashboardScreen.kt` 3,873 · `RiderScreens.kt` 3,867 · `FirebaseManager.kt` 3,002 · `AIDispatchManagerScreen.kt` 2,553 · `AuthScreens.kt` 2,528 · `EmailStudioTab.tsx` 1,952 · `Components.kt` 1,902 … **top two files = ~20% of all production source.**

**Error-handling grade: D+.** Android: 229 catches, **50 empty**, 0 `runCatching`, 51 success vs 37 failure listeners, 0 `try/finally`. Web: 139 catches, **19 empty**. Functions: genuinely good (`HttpsError`, amount validation, idempotency) — **but no client ever calls them** (0 `httpsCallable` repo-wide).

### S1 — Critical

| ID | Finding | Evidence |
|---|---|---|
| RLB-01 | Wallet top-up client-trusted | `WalletViewModel.kt:23-44` |
| RLB-02 | Paystack bridge `success()` replayable (no once-guard) | `ProfileScreens.kt:6661` |
| RLB-03 | Payment→booking race: charged, then "Insufficient balance" | `ExpressBookingScreen.kt:1364-1378` |
| ARCH-01 | World-writable rules make app's own state machine advisory | `firestore.rules:91-94` |
| RLB-04 | Delivery OTP client-generated on world-readable doc | `DeliveryViewModel.kt:5289,5446` |
| ARCH-02/04 | Admin self-signup; middleware not deployed | see SEC-01/31 |
| ARCH-05 | 10 hardcoded secrets incl. browser-shipped SMTP | see SEC-12 |
| ARCH-06 | DNS check **fabricates SPF/DMARC/MX = valid** on failure | `EmailStudioTab.tsx:272-287`, `api/email/dns-check.js:17-52` |
| RLB-05 | `initWelcomeGiftForNewUser` unconditionally zeroes balance/points | `DeliveryViewModel.kt:3484-3511` |
| RLB-06 | Absolute profile sync clobbers server balances | `DeliveryViewModel.kt:3513-3540` |
| ARCH-07 | Batch booking proceeds **after debit failure**; guest batch double-deducts | `DeliveryViewModel.kt:5561-5574,5581+5501` |
| REL-01* | `addPaymentCard` reports **success on failure** | `FirebaseManager.kt:190,207` |
| OBS-06* | Verification-email dispatch returns success **on both success and failure** | `DeliveryViewModel.kt:4251-4256` |

### S2 — High (selected)

| ID | Finding |
|---|---|
| RLB-07 | Status validation outside transaction (TOCTOU) — two riders both pass (`FirebaseManager.kt:1922-2010`) |
| RLB-08 | Geofence writes status directly, bypassing validation (`LocationService.kt:216-225`) |
| RLB-09 | Offline queue: **never populated (0 enqueue call sites)**, no failure listener, no backoff, replay skips state machine (`DeliveryViewModel.kt:1833-1900`) |
| RLB-10 | Room store **self-destructs**: `fallbackToDestructiveMigration` on v12 with 2 migrations, delete-on-error, `exportSchema=false` (`LocalDatabase.kt:291-310`) |
| RLB-11 | Booking lockout flag never reset if callback never fires (`DeliveryViewModel.kt:5234`) |
| RLB-12 | Batch booking has **no** double-submit guard (`:5407-5414`) |
| RLB-14 | LocationService `SupervisorJob` never cancelled; no `onTaskRemoved` (`LocationService.kt:38`) |
| ARCH-08 | 14 unbounded full-collection admin listeners (only 1 has `limit`) |
| ARCH-09 | Public tracking listener leak (`PublicTrackingPage.tsx:95`) |
| ARCH-10 | **No React error boundary anywhere** in `src/` |
| ARCH-11 | Typecheck gate excludes shipped app (false-green) |
| ARCH-12 | 3+ duplicate status state machines |
| ARCH-13 | Pricing divergence: mobile `distanceKm*180`/`stops*1500` vs admin rate table vs marketplace flat ₦1,500 |
| ARCH-15 | **Zero `rememberSaveable`/`SavedStateHandle`** in entire app → all form state lost on process death |
| ARCH-16 | Activity-scoped god VM: 129 StateFlows, 266 fns, 79 launches |
| RLB-17 | `logout()` doesn't reset wallet/loyalty flows or cancel listeners → next user inherits balances (`DeliveryViewModel.kt:4493-4545`) |
| RLB-19 | Retry helper re-runs non-idempotent `addDoc` (`AdminDashboard.tsx:5-16`) |
| RLB-20/21 | Artificial 1.5 s auth delay; transient error force-signs-out admin (`AdminDashboard.tsx:2111,2119`) |

### S3 — Selected
ARCH-18 4 parallel email implementations · ARCH-19 2 forked 1,200-line template engines (already drifted) · ARCH-20 duplicate tracking pages · ARCH-21 tests disabled · ARCH-22 lint disabled · ARCH-23 committed `functions/lib` drift + stale `dist/` · ARCH-24 single Firebase project, no staging · ARCH-26 Tailwind+Bootstrap coexisting · RLB-24 main-thread `Tasks.await` (`FirebaseManager.kt:1124`) · RLB-25 Crashlytics mapping upload off.

### Dead code inventory
15 unreferenced mobile composables (incl. `DeliverySummaryCard`) · 10+ unreferenced web components · `src/data.ts` (471 lines, 0 imports) · entire Next surface (`middleware.ts`, `src/app/api/**`) · `landing page/` 443 tracked files · `idempotencyKey` field declared, **never used anywhere** · 7 dead npm deps + 3 dead test deps · duplicate route aliases · `runBlockingCatch` (0 callers).

**Offline/failure matrix (critical flows):** Booking — no queue, no idempotency, lockout flag risk, guest "confirmed" with no server record. Payment — no gateway verification, replayable callback, race, client-generated references, fire-and-forget ledger. Status — TOCTOU, rules bypass, replay without re-validation, geofence bypass, mirror writes outside txn.

---

## 12. FINDINGS REGISTER — PERFORMANCE, TESTING, OBSERVABILITY, DEPENDENCIES, RELEASE

### Performance

| ID | Sev | Finding |
|---|---|---|
| PERF-01 | S1 | Cold start attaches ~15 open Firestore listeners before first frame (`DeliveryViewModel.kt:2419-2652,2688,2720,274,290,3453,5977`) |
| PERF-02 | S1 | **Pull-to-refresh re-attaches a full second set of listeners, unboundedly** — `refreshAllData()` re-runs `initializeDatabase()`; registrations cleared only in `onCleared()` (`DeliveryViewModel.kt:6394`, callers `DashboardScreen.kt:368,453`, `RiderScreens.kt:204,277`) |
| PERF-03 | S1 | Duplicate listeners: settings ×3, hero banners ×2, cart/store re-attach per UID emission |
| PERF-05 | S1 | Main-thread I/O in composition: sync `Geocoder` (`TrackingScreen.kt:3165-3194`), asset reads in `remember`, full-res photo decode in click handlers (`ProofOfDeliveryScreen.kt:655-680`), crash file read (`MainActivity.kt:99-124`) → ANR risk |
| PERF-08 | S1 | Broadcast loads all users into one 500-op batch → OOM/fail at scale (`DeliveryViewModel.kt:5710`) |
| PERF-13/14 | S1 | **2,916 KB (836.5 KB gz) single JS chunk**; admin dashboard (654 KB source) shipped to every public visitor; only 2 chunks exist (`vite.config.ts`, `App.tsx:10-16`) |
| PERF-04 | S2 | GPS high-accuracy 3 s interval forever; ~40 writes/min/rider with no stationary backoff |
| PERF-07 | S2 | Unbounded queries — only **2** `.limit()` calls in whole app; 35 LazyColumn containers, 12 keyed |
| PERF-09 | S2 | No image downsampling; base64 avatars in Firestore (33% overhead, 1 MB doc risk) |
| PERF-10 | S2 | R8 defeated by blanket `-keep` rules → APK 25.8 MB; mapping upload disabled |
| PERF-15/16 | S2 | 421 KB blocking CSS (5 stylesheets incl. Bootstrap); `dist/images` 34 MB, largest PNG 1.9 MB, 6 unreferenced (4.26 MB) |
| PERF-17 | S2 | Live map clears+rebuilds all markers and `fitBounds` every snapshot (`LiveTrackingMap.tsx:97-178`) |
| PERF-11/12/18/19 | S3 | Non-lazy forEach cards; permissions/crash dialog before UI; render-blocking fonts + inline Meta Pixel; timers without cleanup |

### Testing & CI

| ID | Sev | Finding |
|---|---|---|
| TEST-01 | S1 | **Zero web/server tests** — vitest/RTL installed, no `test` script, no config |
| TEST-02 | S1 | CI unit tests: `testDebugUnitTest \|\| true` → can never fail (`build-and-test.yml:37`) |
| TEST-04 | S1 | Typecheck covers 29/65 files; shipped SPA unchecked; 21 latent errors (15× missing `motion/react` module) |
| TEST-05 | S1 | Deploy workflow has **no `needs:`** on build/test → failing tests cannot block production deploy |
| TEST-03 | S2 | ~10 real tests total (6 model tests); none for ViewModel/payments/OTP/rules/functions |
| TEST-06 | S2 | Android lint disabled (`checkReleaseBuilds=false; abortOnError=false`); no detekt/ktlint/ESLint |
| TEST-07/08/09 | S2 | No E2E/instrumentation, no coverage, no bundle budget, no npm audit step; `functions/tsconfig` working-tree diff loosens `strict` |
| TEST-10/11 | S3 | Untracked `api/email/verification.js` absent from CI/clones; **firestore.rules never deployed or tested by any workflow** |

**CI reality:** `build-and-test.yml` (compile ✅ blocking · tests ❌ ignored · typecheck ⚠️ partial · lint ❌) and `deploy-admin-and-mobile.yml` (build only, secrets silently skipped via `if: env.X != ''`, keystore decoded from secrets overwriting tracked file, `versionCode=2` frozen).

### Observability

| ID | Sev | Finding |
|---|---|---|
| OBS-01 | S1 | Crashlytics `mappingFileUploadEnabled=false` → release crashes unreadable; no custom keys/non-fatals/`setUserId` |
| OBS-02 | S1 | Custom crash handler **shows raw stack trace dialog to users** (violates product-language rule) (`MainActivity.kt:99-124`) |
| OBS-03 | S1 | FCM device token logged in release (`DeliveryViewModel.kt:6407`, `MyFirebaseMessagingService.kt:24`) |
| OBS-04 | S1 | **Zero analytics events, zero performance monitoring** on any platform → no funnels, no vitals |
| OBS-05 | S1 | Web: no error boundary, no `window.onerror`, no `unhandledrejection` → white screen with no signal |
| OBS-06 | S1 | Email dispatch reports success on failure (`DeliveryViewModel.kt:4251-4256`) |
| OBS-07/08/09 | S2 | No alerting/uptime; 128 `Log.e` + PII unfiltered in release; **functions write 0 audit records** for financial/auth mutations |
| OBS-10/11 | S2 | No read/write cost telemetry (bill regressions invisible until invoiced); deploy jobs stay green while deploying nothing |

### Dependencies & secrets

| ID | Sev | Finding |
|---|---|---|
| DEP-01 | S1 | `npm audit`: **24 vulns (1 critical next, 5 high)**; no audit job/Dependabot |
| DEP-02 | S2 | Whole Next.js stack never deployed yet carries the critical CVE |
| DEP-04/05 | S1 | SMTP password ×9 incl. browser bundle; tracked release keystore + `android123` |
| DEP-06 | S2 | Google API key hardcoded as Gradle default (survives missing secrets) |
| DEP-07/08 | S2 | Uncommitted dependency changes; no Gradle dependency locking |
| DEP-12 | S2 | No dependency-review on PRs |
| DEP-13 | S2 | `targetSdk 34` behind Play requirement |
| DEP-09/10/11/14 | S3/S4 | Bootstrap+Tailwind duplication; legacy Firebase package entries; `landing page/`; stack ~1 year stale |

### Release readiness

| ID | Sev | Finding |
|---|---|---|
| RLR-01 | S1 | `versionCode = 2` frozen → second Play upload rejected |
| RLR-02 | S1 | Deploy not gated on build/test |
| RLR-03 | S1 | **Three deploy paths / two hosting targets** (Vercel `dist`, Firebase Hosting `dist`, `deploy.bat`) → the exact class of past live-404 incident |
| RLR-04 | S1 | Rules locked neither in repo nor deployed by CI |
| RLR-05 | S1 | 24 modified + 1 untracked production endpoint uncommitted |
| RLR-06 | S1 | OTP in response + no rate limits |
| RLR-07 | S2 | CI Android builds embed `.env.example` placeholder keys → Mapbox/Gemini/Paystack broken in CI artifacts |
| RLR-08 | S2 | Mobile fallback domain `engracedsmile.com` not deployed by this repo; canonical domain differs |
| RLR-09 | S2 | No `networkSecurityConfig`; WebView universal file access + mixed content |
| RLR-11 | S3 | AGENTS.md build paths reference `D:\Eng App` (actual `D:\Smiles Dispatch`) |
| RLR-13 | S2 | Spec drift: docs say 4-digit OTP, code 6-digit |

---

## 13. CROSS-PLATFORM PARITY TABLE

| Concern | Android | Web admin | Public web | Functions | Verdict |
|---|---|---|---|---|---|
| Status states | 19 | 13 | 5-step ramp | literal strings (dead `IN_TRANSIT`) | **Broken — 4 vocabularies** |
| Transition enforcement | validated + TOCTOU | modal ✓ / dropdown ✗ | none | none | **Broken** |
| Wallet mutation | client delta + absolute cache sync | `increment` + absolute `||350` | — | `increment` | **Broken** |
| Ledger row | Long timestamp, tips missing fields | Timestamp, refund root-only | — | serverTimestamp | **Broken** |
| Cancel refund | fee-based | full (modal) / none (dropdown) | — | — | **Broken** |
| Payment authority | client credit | — | — | verifier **unused** | **Broken** |
| OTP handover | client read+compare | sets flag directly | **rendered publicly** | callable dead + wrong status | **Broken** |
| Timestamps | `System.currentTimeMillis()` ×49 | `Timestamp.now()` ×91 | — | `serverTimestamp()` ×23 | **Broken** |

`PLATFORM_PARITY_GUIDE.md` claims Android↔iOS SwiftUI parity — **no iOS code exists in the repo**; its package references (`com.example.ui.theme`) are wrong. It contains no data-parity content at all.

---

## 14. COST-RISK REPORT (RANKED BY $ IMPACT)

| Rank | Risk | Trigger | Monthly @1k DAU | Monthly @10k DAU | Fix effort |
|---|---|---|---|---|---|
| 1 | Google Places per keystroke (COST-01) | Typing an address | **$480–$1,690** | **$4,800–$16,900** | M |
| 2 | GPS write frequency + listener churn (COST-03/04/06/07) | Riders online + app sessions | ~$27 | ~$390 | M |
| 3 | Google Geocoding tier order (COST-02) | GPS/track actions | $75 | $750 | S |
| 4 | Unauthenticated API/rules abuse (SCALE-01/02, COST-14) | Attacker or spam | Unbounded | Unbounded | M |
| 5 | Duplicate delivery triggers (COST-05) | Every delivery write | $1.50 | $16 | S |
| 6 | Full-table notification fan-out (COST-09/10) | Broadcasts | $0.25+ | $3.60+ | M |
| 7 | Leaked tracking listener (SCALE-03) | Each failed tracking lookup | Permanent reads | Permanent | S |
| 8 | 2.9 MB bundle + 34 MB images (COST-12) | Every visit | ~$0 | ~$20 | M |
| 9 | No TTL on logs/notifications (COST-13) | Time | <$1 | ~$5+ | S |
| 10 | Rules `exists()` per admin write (COST-19) | Admin actions | <$0.50 | — | S |

**Estimated total:** 100 DAU ≈ $5–8 · **1,000 DAU ≈ $570–$1,870 (Firestore-only ≈ $31)** · 10,000 DAU ≈ $5,350–$17,450. *Assumptions: Firestore $0.06/100k reads, $0.18/100k writes; Places $0.005–0.0176/req; Geocoding $0.005/req; Google $200/mo credit absorbed before totals.*

**Runaway risks:** unauthenticated email endpoints (bill + reputation), unauthenticated `deliveries` writes firing auto-dispatch fan-out, notification fan-out reading every user, `billingCircuitBreaker` disabling billing at 100% (outage rather than overspend).

---

## 15. PRIORITIZED REMEDIATION ROADMAP

Dependency-ordered. Each item independently verifiable. Effort: S < 0.5 day · M 1–2 days · L 3–5 days · XL > 1 week.

### PHASE 0 — STOP THE BLEEDING (do first; security + money + bill runaways)

| # | Item | Findings | Effort | Verification |
|---|---|---|---|---|
| 0.1 | **Rotate SMTP password** immediately; remove from all 9 source locations incl. `vite.config.ts` and `EmailStudioTab.tsx`; server env only | SEC-12, DEP-04 | S | `git grep` for old password = 0 hits; site bundle grep = clean |
| 0.2 | **Rotate Android upload key** (Play Console reset); remove `release-key.jks` from repo; delete `android123` fallbacks (fail build if env absent) | SEC-13, DEP-05 | S | Keystore absent from `git ls-files`; build fails without env vars |
| 0.3 | **Delete admin self-signup + self-heal**; provision admins server-side only | SEC-01/03, UX-003/004, ARCH-02 | S | Incognito visit to `/engdadmin` cannot create account |
| 0.4 | **Remove email-domain super-admin** from rules; require `email_verified` + custom claims; move callable auth to claims | SEC-02, SEC-23, DATA-04 | S | Rules unit tests (emulator) pass |
| 0.5 | **Rewrite `firestore.rules` deny-by-default**: ownership on users/deliveries/transactions/fleet/marketplace/notifications; `system_config` admin-only; delete all 21 `if true`; fix missing `incident_reports`/`fleet_broadcasts`; lock `audit_logs` create to admin | SEC-04/05/06/07/08/14-19/28/37, DATA-01, SCALE-01 | L | Rules emulator suite green; `grep "if true" firestore.rules` = 0 |
| 0.6 | **Kill API abuse surface**: delete `api/email/test-send*` + `smtp-verify*` from production; stop returning `otp`; add per-IP/email rate limits on all email endpoints | SEC-09/10/11/32, SCALE-02, RLR-06 | M | `curl` OTP endpoint returns no `otp` field; relay endpoint 401/429 |
| 0.7 | **Server-authoritative money**: wire `verifyPaymentAndTopUp` via `httpsCallable`; remove client `walletBalance` writes from rules; add Paystack debit ledger for marketplace orders; credit rider payout inside delivery-complete txn; single refund service; remove absolute profile syncs | DATA-22/23/24/26/30, RLB-01/02/03/05/06, SEC-04 | XL | Top-up without gateway reference fails; rider balance increments after delivery |
| 0.8 | **Fix ID generation** to Firestore auto-IDs for orders/notifications/tips | DATA-37/38/39/40 | S | Two bookings in same second both persist |
| 0.9 | **Deploy 2 missing composite indexes**; drop 5 unused | DATA-32/33/34 | S | Vendor orders + auto-dispatch queries succeed |
| 0.10 | **Gate Google Places** (local-first, min 4 chars, session tokens, prefix cache) + reorder geocode tiers | COST-01/02 | M | Network log: Google calls only on local shortage |
| 0.11 | **Commit the 25 uncommitted files** incl. untracked `api/email/verification.js` (after 0.1–0.10 code changes) | UX-037, RLR-05, TEST-10 | S | `git status` clean; push confirmed |
| 0.12 | **Add rate limiting + App Check** on public `contacts` create | SEC-29 | S | Spam test returns 429 |

### PHASE 1 — MAKE CORE JOURNEYS COMPLETABLE (trust & usability)

| # | Item | Findings | Effort |
|---|---|---|---|
| 1.1 | Fix rider notification crash (`RiderDeliveries` route) + guard all navigate calls | UX-001 | S |
| 1.2 | Rebuild public site nav (working pages or in-page anchors), fix relative links, real store URLs, working brand/share metadata/phone | UX-002/008/009/026, B19 | M |
| 1.3 | Unify status machine: one shared transition table (JSON in `system_config`) consumed by app/admin/functions; remove unvalidated admin dropdown path; unknown status to `UNKNOWN` not `PENDING`; fix public tracking step mapping; fix `IN_TRANSIT`/`TRANSIT` | DATA-14/15/16/19/20/21, ARCH-12 | L |
| 1.4 | Route OTP verification through server callable (delete client-side compare); stop rendering OTP on public tracking page; hash at rest, attempt-limited | SEC-20/21, DATA-18, UX-036 | M |
| 1.5 | Fix money edge cases: payment/booking race, batch debit-failure abort, guest double-deduct, booking lockout flag timeout, double-submit guard on batch, tip re-submission idempotency, escrow transactionality | RLB-02/03/07/11/12/13, DATA-27/28 | L |
| 1.6 | Fix `Parcel` to functions field contracts (`recipientEmail`, `trackingNumber`, `TRANSIT`, EXPRESS branch) so status emails can render | DATA-10/11/19 | S |
| 1.7 | Timestamp migration: `serverTimestamp()` everywhere; one ledger schema (type/status/userId/Timestamp); fix mixed sorting + refund mirror | DATA-05/06/07/08/09/43/44/45 | M |
| 1.8 | Rider earnings: credit payout + tips visibly; wire `payoutCredited`; fix `RiderScreens` totals | UX-013, J11 | M |
| 1.9 | Fix admin refund parity (dropdown path to shared `updateStatus`+refund); loyalty guard with `FieldValue.increment`; remove `||350` fallbacks | DATA-15/24/25 | M |
| 1.10 | Eliminate fake data: promo savings formula, hero stats, default 4.9 star, seed personas, system monitor simulation, prefilled admin email | UX-018/019/020/023/033, PRODUCTION R5 | M |
| 1.11 | Repair broken stores: `pickup_photos` storage rule, PIN hashing unification (hash web-side, reject plaintext), crash dialog to generic copy | SEC-25, UX-006, OBS-02 | M |
| 1.12 | Publish `/privacy` + `/terms`, link at both signups (mobile clickable span) | SEC-27 | S |

### PHASE 2 — RESILIENCE (errors, states, offline, observability)

| # | Item | Findings | Effort |
|---|---|---|---|
| 2.1 | Shared `ErrorState(error, onRetry)` primitive adopted across all screens; split error vs not-found | UX-010/011/012, D16 | L |
| 2.2 | Fix 69 empty catches (log/propagate); fix success-on-failure (`addPaymentCard`, email dispatch, DNS check fabrication); pair success/failure listeners | RLB-22/23, ARCH-06, OBS-06, REL-15 | L |
| 2.3 | Offline queue: actually enqueue booking/status/POD ops; failure listener + backoff + retry cap + re-validate on replay; stop Room self-destruction; add migrations; `exportSchema=true` | RLB-09/10, prior CRITICAL #11 | L |
| 2.4 | Listener lifecycle: register all in `listenerRegistrations`; make `refreshAllData` re-`get()` only; remove duplicates (banners x2, settings x3); fix public-tracking leak; fix `users`-GPS listener churn | PERF-02/03, SCALE-03/05/06/07, ARCH-09 | M |
| 2.5 | Move main-thread I/O to `Dispatchers.IO` (geocoder, asset reads, photo decode, `Tasks.await`, crash read) | PERF-05, RLB-24 | M |
| 2.6 | Observability baseline: enable Crashlytics mapping + custom keys + non-fatals; web ErrorBoundary + `window.onerror`; remove FCM token logging; stop PII release logging; add Analytics events (booking_created, otp_sent, payment_completed); functions emit `audit_logs` | OBS-01/03/04/05/09, ARCH-10 | L |
| 2.7 | Fix logout state reset (wallet/loyalty/listeners); fix admin force-signout on transient error; remove 1.5 s artificial delay | RLB-17/20/21 | S |
| 2.8 | Batch/chunk all >500-op writes (broadcast, purge) with progress + partial-failure reporting | SCALE-04/08, PERF-08 | M |
| 2.9 | GPS throttle (10–15 s / 10 m), single-target write, 5-min heartbeat, adaptive stationary backoff, cancel scope in `onDestroy` | COST-03/04, PERF-04, RLB-14 | M |
| 2.10 | TTL policies on notifications/audit/timeline/chats/fleet_locations | COST-13, DATA-50 | S |

### PHASE 3 — QUALITY (a11y, performance, tests, polish, cleanup)

| # | Item | Findings | Effort |
|---|---|---|---|
| 3.1 | Accessibility sweep: 44dp targets, contentDescriptions (146), aria-labels (41 buttons), focus-visible, reduced-motion, gold-on-white contrast (12 admin sites) | UX-016/017/024/025, D18/20/23 | L |
| 3.2 | Route-split web bundle (`React.lazy` admin + manualChunks), WebP/srcset images, purge Bootstrap, drop dead 4.26 MB assets | PERF-13/14/15/16, SCALE-10, COST-12 | M |
| 3.3 | Android size/perf: targeted ProGuard keeps, `mappingFileUploadEnabled=true`, image `inSampleSize`, avatar URL-only, lazy lists with keys/paging | PERF-07/09/10 | M |
| 3.4 | Test gates: remove `|| true`; fix `tsconfig.include` to `src/**` (fix 21 errors); add `test` script + smoke tests; lint on (ESLint/detekt); functions `strict:true`; rules emulator tests in CI; deploy `needs:` build-test; npm audit + dependency-review | TEST-01 to 11, ARCH-11/21/22, DEP-01/12, RLR-02 | L |
| 3.5 | One production host declared; single deploy pipeline; post-deploy smoke check; functions deploy step; fix AGENTS paths; `versionCode` automation; `targetSdk 35` | RLR-01/03/07/08/11, DEP-13, OBS-11 | M |
| 3.6 | Delete dead code: 28 unreferenced components, 9 unreachable routes, `src/data.ts`, Next dead surface (or commit to Next), `landing page/`, duplicate email/tracking implementations, unused deps, route aliases | UX-028/029/030/039, ARCH-18/19/20/27-32, DEP-02/09 | L |
| 3.7 | Adopt mandated shared primitives (design-system 0 imports to used) + missing `EmptyState`/`LoadingSkeleton`/`SectionHeader` | D22, COMPONENT_PLAN Phase 2 | M |
| 3.8 | AGENTS closure: hero button "Market" + route, header title overflow, domain unification, CMS brand defaults, `rememberSaveable` on forms | UX-027/034/035/040, ARCH-15 | M |
| 3.9 | Dependency upgrades: npm audit fix (next/nodemailer), Gradle catalog refresh, remove dead deps | DEP-01/03/14 | M |
| 3.10 | Rewrite `PLATFORM_PARITY_GUIDE.md` as real data-parity contract (shared status enum + ledger schema) | DATA-36 | S |

**Estimated total effort:** Phase 0 = 3–4 weeks · Phase 1 = 4–5 weeks · Phase 2 = 3–4 weeks · Phase 3 = 4+ weeks (partially parallelizable; Phase 0 blocks everything).

---

## APPENDIX A — RE-VERIFICATION OF PRIOR AUDIT CLAIMS (2026-09-09 to 2026-09-26)

~45 major claims re-checked against current code: **~50% fixed · ~45% still open · ~5% regressed.**

**Status by cluster:**
- **Security/money: ~70% still open, 2 REGRESSED.** Notably: `firestore.rules` on `deliveries` went from `allow get: if true; allow list: if isAuth()` to `allow read: if true; allow create, update: if true` (regression by commit `aad53c3`, current `firestore.rules:93-94`); public tracking still renders recipient OTP (`PublicTrackingPage.tsx:404-417`); OTP now also **returned in an API response** (new hole, `api/email/verification.js:327`); wallet top-up still client-credited (`WalletViewModel.kt:45`) with the server verifier unused; POD storage still actor-unowned.
- **UX/navigation/build/language: ~75% fixed.** Confirmed fixed: back-stack `popUpTo` (20-back-press bug), rebook prefill, booking-form sprawl (9 to 4), admin status-transition validation + confirmation + reassignment, dead "New Delivery" button, seed buttons hidden from UI, rider tips live values, ARRIVED badge, GPS simulator removed, Benin geofence (Lagos bounds bug), Vercel `dist` 404 fix, no SHA-1/Firebase-Console copy, no "Fake Score", `loadMockInitialData` now empties lists, loyalty discount actually charged, mark-all-read, active-tab contrast fix.
- **Still open (carry-forward):** crash dialog shows raw stack trace (`MainActivity.kt:106-118`), offline queue never populated (0 enqueue call sites), 18 full-collection admin listeners (was 11 — **worse**), no admin error boundary, mixed `read`/`isRead` notification schema, simulated system-monitor metrics, local-only leave requests, monolith growth (`AdminDashboard.tsx` 4,117 to **10,749** lines).
- **Not predicted by any prior audit:** dependency vulnerabilities (`npm audit` = 24), false-green typecheck scope, Google Places cost exposure, clock-collision order IDs.

---

## APPENDIX B — MANUAL DEVICE/BROWSER TEST SCRIPTS (for non-technical verification)

Run these after each remediation phase. Pass = checklist tick · Fail = the "Today" note applies.

### B1. Admin security (5 min, browser, incognito window)
1. Open `https://<your-domain>/engdadmin` in an incognito window.
   - Fail today: you can see "Create an admin account" with **Super Admin** preselected. Required: no signup option; sign-in only.
2. Try signing up with `tester@esdispatch.com` from incognito.
   - Fail today: account created with super-admin power. Required: signup impossible / role forced to lowest.
3. Log in as a normal customer, then visit `/engdadmin`.
   - Required: "Access denied" — no self-heal into admin.

### B2. Database privacy (5 min)
1. Run the rules emulator test suite (or have a developer check `grep "if true" firestore.rules` returns 0).
   - Fail today: 21 `if true` blocks. Required: 0.
2. Open a tracking link in a private browser window and view page source for an OTP/PIN value.
   - Fail today: OTP visible. Required: no OTP/PIN in HTML.

### B3. Money integrity (15 min, test device + test gateway)
1. Top up wallet via Paystack (small amount). Check wallet history shows the entry **once**.
2. Force-kill the app immediately after the Paystack success screen, reopen, check balance again — must not double-credit.
3. Send a delivery, complete it as rider, check **rider earnings increased** (Fail today: stays 0).
4. Pay for a marketplace order with **Paystack (not wallet)** — check buyer's wallet history shows a DEBIT (Fail today: no debit exists).
5. Cancel a delivery from the admin **row dropdown** — check customer refund arrived (Fail today: no refund on this path).

### B4. Core journeys (20 min, device)
1. Customer: signup, dashboard, book, pay, track, delivered, see it in history. Kill app mid-booking once — draft must survive.
2. Rider: tap a dispatch **notification** — must open the app, not crash (Fail today: crash).
3. Rider: go offline, change status, go online — status must sync without duplicates.
4. Public site: click **every** header and footer link — must land on real content (Fail today: 12 links hit 404).
5. Share the homepage link on WhatsApp/Twitter — preview must say ESDispatch, not car rental (Fail today: "Car Rental").

### B5. Failure behavior (10 min)
1. Turn on airplane mode, tap any "load" screen — must show an error with a **Retry button** (Fail today: none exist).
2. Enter a wrong OTP 5 times — must lock out (Fail today: unlimited).
3. Disconnect mid-photo-upload (POD) — must show queued/retry state, not silent success.

### B6. Cost sanity (10 min, dev tools, Network tab)
1. Type an address slowly in the booking form and watch requests: **Google Places calls must not fire on every keystroke** (Fail today: about 4 calls per field per booking).
2. Leave the dashboard open 5 minutes: listener traffic must not grow unbounded.
3. Open admin dashboard and watch Network/Firestore console: must not download the **entire** users and deliveries collections.

---

## APPENDIX C — COMMANDS RUN & OUTPUTS

| Command | Working dir | Result |
|---|---|---|
| `npx tsc --noEmit` | repo root | exit 0 (scope: 29/65 files — see TEST-04) |
| `npm audit --omit=dev` / `npm audit` | repo root | 24 vulns (1 critical, 5 high, 18 moderate) |
| `.\gradlew.bat :app:compileDebugKotlin --console=plain` | `mobile/` | BUILD SUCCESSFUL in 9 s (18 tasks up-to-date) |
| `git status --porcelain` | repo root | 25 modified/untracked entries |
| `git log --oneline -5` | repo root | HEAD `6b3fd9c` (email verification/SMTP work) |
| Static analysis via 7 parallel audit agents | — | Security · Cost/Scale · Data/Parity · UX/Completeness · Architecture/Reliability · Performance/Testing/Obs/Deps · Prior-audit re-verification |
| `assembleRelease`, device install, runtime flows | — | **Not run** — see Appendix B |

---

**Report ends.** Classification legend: S1 blocker (unusable / data-money loss / security breach) · S2 major · S3 moderate · S4 polish. All findings are code-evidence-based (`file:line`); items requiring runtime confirmation are marked runtime-unverified with manual steps in Appendix B. No source files were modified during this audit.
