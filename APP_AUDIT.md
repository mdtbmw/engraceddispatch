# MASTER APP AUDIT, UX LOGIC, PRODUCTION READINESS AND AI-TRACE ELIMINATION

Audit date: 2026-09-09  
Project: `D:\Eng App`  
Scope: Android customer/rider app, web admin, Firebase Functions, Firestore/Storage rules, marketplace, notifications, OTP/auth, role behavior, navigation, empty/error states, user-facing language and production-readiness posture.  
Method: source-code review of the current working tree plus existing audit evidence already produced in `docs/audits/`. Runtime UI walkthrough is marked unverified where applicable.

## Executive Summary

The application has the outline of a premium logistics product, but it still carries too much evidence of being assembled by instructions rather than shaped into a coherent commercial product. The most visible problem is not just "AI wording." The deeper issue is that screens make claims the system cannot fully support: live assignment without a durable queue worker, production language where the user needs plain product copy, fake/simulated route confidence, OTP/proof flows exposed in the wrong places, admin controls that describe implementation status, and UI surfaces that mix operations with debug/demo/admin seed behavior.

The app should never talk to customers like a developer console. Customers should not see Firebase, SHA-1, package names, developer copy, fake score, production-ready claims, mock/simulation language, debug crash text, or implementation status. Admins can see operational diagnostics, but those diagnostics must be role-scoped and worded like a real control room, not a coding-agent checklist.

The app is not production-ready as a serious public logistics product yet. It has promising visual ambition and many features, but the underlying product logic, security boundaries, notification behavior, lifecycle model and language discipline are not ready for commercial trust.

## Readiness Scores

Scores are honest launch-readiness scores out of 10, based on source review.

| Area | Score | Why |
| --- | ---: | --- |
| Functional correctness | 4 | Many screens exist, but booking, dispatch, marketplace, proof and settlement have broken chains. |
| UX logic | 3 | Key interactions do not match user expectations, especially admin dispatch and tracking states. |
| UI polish | 6 | The app has a premium visual direction, but inconsistent copy, hidden controls and trace language weaken it. |
| Navigation | 5 | Most screens are reachable, but deep links, admin handoffs, support context and role flows are weak. |
| Authentication | 4 | Firebase Auth exists, but user-facing setup errors and weak deactivation/session enforcement remain. |
| Security | 2 | Delivery/OTP/PII, wallet, support and role boundaries are too broad in important places. |
| Data integrity | 3 | Multiple clients write business truth; statuses, payments, proof and ledgers disagree. |
| Error handling | 3 | Many failures are logged, swallowed, or shown as generic toasts. Partial failure handling is weak. |
| Network/offline behavior | 4 | Local storage exists, but users are not clearly told what is synced versus only local. |
| Performance | 5 | Live listeners exist, but broad collection listeners and giant admin modules will not scale cleanly. |
| Accessibility | 4 | Some content descriptions exist, but tiny text, custom controls and forced font scale issues remain. |
| Animations | 6 | There is motion ambition, but some motion communicates fake liveliness instead of real state. |
| Marketplace | 4 | Discovery/cart/favorites exist, but checkout, disabled state, vendor flow and fulfilment are incomplete. |
| Dispatch workflow | 2 | No complete queue, reserve-next, capacity, proof-before-delivery or exception engine. |
| Role permissions | 3 | UI roles exist, but backend enforcement is incomplete or inconsistent. |
| Notifications | 3 | Push/in-app plumbing exists, but dedupe, clearing, schema and deep links are unreliable. |
| Empty states | 5 | Some helpful empty states exist, but disabled/failed states often look like no data. |
| Loading states | 4 | Loading flags exist in places, but loading/error/empty are often not separated. |
| Edge cases | 2 | Cancellation, reassignment, no rider, stale GPS, proof failure and payment partials are weak. |
| Maintainability | 3 | Huge files, nested components, duplicated logic and conflicting instructions make future work risky. |

Overall readiness assessment: approximately 3.8/10. This is a visually ambitious prototype with some real backend wiring, but it should not be treated as a polished, production-grade logistics system until the P0/P1 findings are resolved.

## Navigation Map

### Web

- `/` public site.
- `/engdadmin` admin dashboard.
- `/engadmin` redirect alias to `/engdadmin`.
- `/track/:id` public tracking.
- `*` not found.

The root app currently behaves as a Vite app, while older project instructions still describe Next.js. That documentation drift matters because future deployments and agents may verify the wrong runtime.

### Android

The Android app includes customer dashboard, booking flows, tracking, marketplace, cart/checkout sheet, favorites sheet, profile/settings, notifications, customer assistant, rider dashboard, rider delivery actions, proof of delivery, vendor portal and admin/AI dispatch manager screens. There are many surfaces, but not all share the same lifecycle or authority model.

## AI-Trace and Development-Trace Summary

The app contains user-facing or likely user-facing terms that should be removed or rewritten:

- Firebase/OAuth/SHA-1/package-name details in authentication dialogs.
- "Fake Score" inside a visible AI/POD area.
- "Simulation" and live-transmission wording in rider GPS areas.
- "AI Confidence", "AI Optimized", "AI Operations", "Autonomous Platform" style claims where the feature is not a real user-facing AI product.
- "Backend", "Firebase", "Database", "Production", "Demo", "Seed", "Mock" terms in admin/customer-adjacent surfaces.
- Crash dialog telling a normal user to copy a crash and send it to the developer.
- Sample/seed controls in normal admin screens.
- "Marketplace is LIVE ON APP", "ENABLED (LIVE ON MOBILE APP)" and similar implementation-status copy.

The fix is not to hide every technical word everywhere. Admin diagnostics can exist. The rule is that normal product surfaces must speak like a product, not like the build process.

## Findings

### AUDIT-001

Severity: P0  
Category: Security / Privacy / OTP  
Location: `firestore.rules`, `mobile/app/src/main/java/com/esdispatch/data/Models.kt`, delivery documents.

Current Behavior: Delivery documents include OTP, phone numbers, addresses and rider/customer details, while audited rules allow broad delivery reads.

Why It Is Wrong: Public tracking or broad document access must not expose private delivery data or handover secrets.

Expected Behavior: Public tracking uses a redacted tokenized document. OTP/challenge secrets are server-only.

Recommended Fix: Create safe read models for public/customer/rider/admin. Move OTP secret storage and verification into a server command.

User Impact: A customer/rider/company can lose trust if private details or handover codes are exposed.

Acceptance Criteria: Unauthenticated users cannot enumerate delivery documents; rider/customer payloads do not contain OTP secret values; public tracking shows only safe status data.

### AUDIT-002

Severity: P0  
Category: Dispatch / Data Integrity  
Location: `functions/src/index.ts`, `AdminDashboard.tsx`, `FirebaseManager.kt`

Current Behavior: Auto-dispatch and admin assignment write `ASSIGNED` without one shared capacity/reservation/acceptance command.

Why It Is Wrong: "Online" is not the same as available. A busy rider can be assigned another active delivery.

Expected Behavior: Rider availability is server-computed and assignment atomically reserves capacity.

Recommended Fix: Build `assignOrReserveRider` with order version, rider state, active load, GPS freshness, shift/inspection, vehicle fit and reservation rules.

User Impact: Customers may see assigned riders who are not actually coming; admins cannot trust rider availability.

Acceptance Criteria: One rider cannot receive conflicting active jobs; busy riders become reserve-next, not active, unless multi-stop capacity rules allow it.

### AUDIT-003

Severity: P0  
Category: Rider / Location Integrity  
Location: `RiderScreens.kt`

Current Behavior: A rider-facing GPS simulator labelled like live transmission can write simulated route movement and status progress.

Why It Is Wrong: Simulated motion in production makes the product lie.

Expected Behavior: Only fresh hardware GPS writes live rider telemetry in production.

Recommended Fix: Remove production simulator writes; isolate demo simulation behind a non-production build and demo dataset.

User Impact: A stationary rider can look like they are moving, destroying tracking trust.

Acceptance Criteria: Device stationary means marker stationary; no simulator can write production delivery coordinates.

### AUDIT-004

Severity: P1  
Category: Admin UX / Dispatch  
Location: `AdminDashboard.tsx` dashboard active request popup.

Current Behavior: Clicking active requests opens a thin list, then sends admin to Shipment Management to act.

Why It Is Wrong: Admin cannot decide or dispatch quickly from the popup.

Expected Behavior: The popup/drawer contains full order facts and direct legal actions.

Recommended Fix: Replace category popup with an order drawer: queue age, pickup/dropoff, contacts, payment, rider selector, reserve-next, exception and timeline.

User Impact: Dispatcher wastes time hunting for the same request again.

Acceptance Criteria: Admin can assign, reserve, queue, cancel or open exception from the same order context.

### AUDIT-005

Severity: P1  
Category: Queue / Customer Trust  
Location: `functions/src/index.ts`, `TrackingScreen.kt`

Current Behavior: Backend logs "queued" when no rider exists, but customer tracking says assigning nearest courier.

Why It Is Wrong: A log line is not a customer-visible queue.

Expected Behavior: No-rider requests enter a durable FIFO queue with customer-visible queued state.

Recommended Fix: Add queue records, queue position/reason, queue-drain worker and customer states for received/queued/reserved.

User Impact: Customer waits with misleading assignment copy.

Acceptance Criteria: With zero riders, customer sees queued and admin sees oldest eligible request first.

### AUDIT-006

Severity: P1  
Category: AI Trace / UX Language  
Location: `mobile/app/src/main/java/com/esdispatch/MainActivity.kt:111-130`

Current Behavior: Crash dialog says "Copy this and send it to the developer" and shows crash text.

Why It Is Wrong: Normal users should never see developer workflow or crash internals.

Expected Behavior: User sees a calm recovery message and optional "Send report" if a proper reporting channel exists.

Recommended Fix: Replace with "The app had trouble starting. You can continue or send a report to support." Do not expose stack traces.

User Impact: User sees unfinished/developer-facing behavior after a crash.

Acceptance Criteria: Crash recovery screen contains no stack trace, developer instruction or raw implementation text for normal users.

### AUDIT-007

Severity: P1  
Category: AI Trace / Authentication UX  
Location: `AuthScreens.kt:658-690`, `AuthScreens.kt:1728-1748`

Current Behavior: Google sign-in error dialog exposes OAuth, SHA-1 fingerprint, package name and Firebase Console.

Why It Is Wrong: That is developer setup information, not user-facing auth copy.

Expected Behavior: User sees "Google sign-in is temporarily unavailable. Use email sign-in or contact support."

Recommended Fix: Move technical setup details to logs/admin diagnostics only.

User Impact: Customer sees a broken engineering setup instead of a product issue.

Acceptance Criteria: No normal auth error mentions Firebase Console, SHA-1, package name or OAuth setup.

### AUDIT-008

Severity: P2  
Category: AI Trace / Admin UX  
Location: `AIDispatchManagerScreen.kt:920-945`

Current Behavior: A visible POD tool can display "Fake Score: X% (Verified)".

Why It Is Wrong: "Fake Score" is not professional product language and is contradictory with "Verified".

Expected Behavior: Use "Image review result", "Possible mismatch" or "Needs manual review" with clear evidence.

Recommended Fix: Rename the field and only show confidence if backed by a real model. Otherwise show manual checklist.

User Impact: Admins lose trust in the tool and the product looks AI-generated.

Acceptance Criteria: No visible screen contains "Fake Score"; POD review language is professional and evidence-based.

### AUDIT-009

Severity: P1  
Category: OTP / Proof  
Location: `DeliveryViewModel.kt`, `FirebaseManager.kt`, `ProofOfDeliveryScreen.kt`

Current Behavior: OTP is generated/stored client-side in some paths, and delivery can be marked delivered before proof upload is complete.

Why It Is Wrong: Handover verification and proof are the trust boundary for delivery completion.

Expected Behavior: Server issues/verifies challenge; proof succeeds or exception is approved before final delivered.

Recommended Fix: Use one server handover/proof command and one completion event.

User Impact: Delivery can look complete without reliable proof.

Acceptance Criteria: Failed proof upload cannot close delivery as delivered; OTP secret is not readable by client documents.

### AUDIT-010

Severity: P0  
Category: Payments / Settlement  
Location: `WalletViewModel.kt`, `FirebaseManager.kt`, `functions/src/index.ts`

Current Behavior: Wallet top-up, delivery payout and marketplace settlement use inconsistent paths and formulas.

Why It Is Wrong: Money must be server-verified, ledger-backed and idempotent.

Expected Behavior: One payment intent, one verified top-up path, one payout formula, one ledger.

Recommended Fix: Move all money mutations to server commands with payment reference validation and idempotency.

User Impact: Customers and riders can see wrong balances or failed credits.

Acceptance Criteria: Gateway reference, amount, currency and user are verified server-side; delivery settlement posts once.

### AUDIT-011

Severity: P1  
Category: Notifications  
Location: `MyFirebaseMessagingService.kt`, `FirebaseManager.kt`, `AdminDashboard.tsx`, `functions/src/index.ts`

Current Behavior: Notifications can be created by several writers, schemas differ, foreground messages can duplicate and clearing can fail or reappear.

Why It Is Wrong: Notifications are product state, not incidental side effects.

Expected Behavior: One notification event service with recipient state, dedupe key, read/dismiss state and deep link.

Recommended Fix: Replace raw document-change alerting with notification events.

User Impact: Users see repeated alerts after dismissing and may open the wrong order.

Acceptance Criteria: A dismissed notification never reappears unless a newer event version exists; tapping opens exact order/ticket.

### AUDIT-012

Severity: P1  
Category: Marketplace  
Location: `MarketplaceScreen.kt`, `DeliveryViewModel.kt`, `AdminDashboard.tsx`

Current Behavior: Marketplace has products, favorites, cart and checkout, but checkout and fulfilment are weak. Admin disabled-state copy describes hiding app features, not a designed customer experience.

Why It Is Wrong: Marketplace must remain intentional when disabled and must create real fulfilment orders when enabled.

Expected Behavior: Disabled marketplace shows a useful product/service state; enabled checkout creates server-validated vendor fulfilment and delivery tasks.

Recommended Fix: Add marketplace availability state, designed disabled state, server checkout, vendor readiness and real pickup contacts.

User Impact: Customers may see empty/broken catalog states or receive vague marketplace delivery tracking.

Acceptance Criteria: Turning marketplace off produces a polished customer state; checkout cannot create fake fulfilment points.

### AUDIT-013

Severity: P1  
Category: Marketplace / Data Integrity  
Location: `DeliveryViewModel.kt:5883-6122`

Current Behavior: Marketplace order IDs use time suffixes; checkout transaction reads some docs after writes; marketplace delivery uses generic vendor fulfilment data.

Why It Is Wrong: IDs can collide, transactions can fail, and riders do not get real pickup instructions.

Expected Behavior: Server creates collision-resistant IDs and validated fulfilment tasks.

Recommended Fix: Move checkout to backend and create one fulfilment plan per vendor/order.

User Impact: Customer checkout may fail or create undeliverable jobs.

Acceptance Criteria: Concurrent marketplace orders do not collide; riders receive actual vendor pickup address/contact.

### AUDIT-014

Severity: P2  
Category: Marketplace / Empty State  
Location: `MarketplaceScreen.kt:339-356`, `MarketplaceScreen.kt:903-916`

Current Behavior: Marketplace and favorites empty states exist, but disabled marketplace is not a clearly designed customer state.

Why It Is Wrong: Disabled features should feel intentional, not empty or broken.

Expected Behavior: Marketplace disabled state should explain the current customer option, such as delivery services, saved vendors, support or return later.

Recommended Fix: Add a state driven by `marketplaceEnabled` and distinguish disabled from no products/no search results.

User Impact: Customer cannot tell whether the store is unavailable, empty or broken.

Acceptance Criteria: Disabled, no-products, no-search-results and no-favorites states are visually and textually distinct.

### AUDIT-015

Severity: P1  
Category: Admin / State Management  
Location: `AdminDashboard.tsx`

Current Behavior: Major admin tabs are nested components with local state. Live parent updates can remount them and reset filters/forms/selections.

Why It Is Wrong: Real-time admin tools must not lose context while data updates.

Expected Behavior: Live data refreshes rows while selected order, draft reply, filters and open drawer remain stable.

Recommended Fix: Move tabs to module scope or separate files; key state by route/order/ticket.

User Impact: Admins lose work and trust in the tool.

Acceptance Criteria: Open drawer/support draft/filter survives new delivery and GPS updates.

### AUDIT-016

Severity: P2  
Category: Admin / AI Trace  
Location: `AdminDashboard.tsx:454-485`, `AdminDashboard.tsx:4179-4227`

Current Behavior: Admin UI uses implementation-status language like "LIVE ON APP", "HIDDEN ON APP", "Seed Marketplace Data" and sample data creation in normal surfaces.

Why It Is Wrong: Admin operations should use product language. Seed/demo controls should not sit in daily operations.

Expected Behavior: "Marketplace available to customers" / "Marketplace paused" for real settings; sample data only in developer-only tools.

Recommended Fix: Move seed tools behind developer-only environment gate; rewrite marketplace switch copy.

User Impact: Admin feels like a test console, not a business tool.

Acceptance Criteria: Production admin contains no seed/demo controls unless owner enables developer tools.

### AUDIT-017

Severity: P1  
Category: Role Permissions  
Location: `AdminDashboard.tsx`, `firestore.rules`

Current Behavior: UI roles exist, but backend capabilities are not consistently enforced by specific permission.

Why It Is Wrong: UI hiding is not security.

Expected Behavior: Each sensitive command checks role/capability server-side.

Recommended Fix: Build role capability map and enforce it in Cloud Functions/rules.

User Impact: Limited users may access too much or legitimate users may be blocked unpredictably.

Acceptance Criteria: Customer, rider, dispatcher, support, finance, admin and owner test matrix passes against backend.

### AUDIT-018

Severity: P1  
Category: Navigation / Deep Links  
Location: `MyFirebaseMessagingService.kt:99-110`, admin Manage flow.

Current Behavior: Push notification pending intent uses request code `0`; admin popup Manage relies on search handoff.

Why It Is Wrong: Users expect alerts/actions to open the exact item.

Expected Behavior: Every notification/action carries a stable destination and opens selected order/ticket.

Recommended Fix: Use unique/deep-link pending intents and route-based selected order drawers.

User Impact: User taps alert and lands in the wrong place or has to search again.

Acceptance Criteria: Multiple alerts open their own delivery/ticket even after cold start.

### AUDIT-019

Severity: P1  
Category: Dispatch / State Machine  
Location: `Models.kt`, admin/web/rider status maps.

Current Behavior: Android, web and email automation use different status names and incomplete transition maps.

Why It Is Wrong: One delivery cannot have different meanings across clients.

Expected Behavior: Shared lifecycle contract and migration mapping.

Recommended Fix: Define versioned lifecycle codes and customer/admin/rider presentation labels.

User Impact: Status appears stale, blank, unsupported or contradictory.

Acceptance Criteria: One order advanced by rider appears correctly in customer, admin, public tracking and notifications.

### AUDIT-020

Severity: P2  
Category: Visual Consistency  
Location: Root/mobile/admin UI.

Current Behavior: Brand copy still mixes ESDispatch, ENGRACED DISPATCH, alternate gold values and old wording.

Why It Is Wrong: Brand inconsistency makes the product feel stitched together.

Expected Behavior: Locked ESDispatch identity and `#FFB800` gold across product UI.

Recommended Fix: Centralize tokens and copy; remove old brand strings from user-facing UI.

User Impact: Users see inconsistent company identity.

Acceptance Criteria: No normal screen uses old brand name or non-approved gold where the locked token applies.

### AUDIT-021

Severity: P1  
Category: Authentication / Session  
Location: Admin user deletion, auth/rules.

Current Behavior: User deactivation sets a display flag but does not reliably revoke auth sessions or remove assignment eligibility everywhere.

Why It Is Wrong: "Deactivate" must mean access/eligibility changes, not only UI filtering.

Expected Behavior: Deactivation is a server command affecting auth, claims, eligibility and active work.

Recommended Fix: Implement account lifecycle command with reassignment flow for active riders.

User Impact: Removed users may still transact or receive work.

Acceptance Criteria: Deactivated logged-in rider cannot accept or receive new deliveries.

### AUDIT-022

Severity: P1  
Category: Empty / Error / Loading States  
Location: Admin listeners, Android marketplace, tracking, notifications.

Current Behavior: Empty lists can mean truly empty, loading failed, permission denied, disabled or offline.

Why It Is Wrong: Users need to know what happened and what to do next.

Expected Behavior: Every data surface has distinct loading, empty, disabled, offline, error and retry states.

Recommended Fix: Add source-specific state wrappers and UI copy for each state.

User Impact: Admin may believe no requests exist when listener failed.

Acceptance Criteria: Disconnect network or deny permission and screen shows error/retry, not "no data".

### AUDIT-023

Severity: P1  
Category: Benin City Scope  
Location: `DeliveryViewModel.kt`, `TrackingScreen.kt`, public site data.

Current Behavior: Benin-specific code exists, but Lagos/Abuja/Asaba/interstate references and defaults remain.

Why It Is Wrong: A Benin-only dispatch operation must not route, market or fallback to other cities.

Expected Behavior: Dispatch flows validate Benin serviceability; other business lines are separated.

Recommended Fix: Create service-area policy and remove/isolate non-Benin dispatch copy.

User Impact: Users may book unsupported routes or distrust local focus.

Acceptance Criteria: Non-Benin dispatch request is blocked/reviewed; normal screens do not use Lagos examples for Benin dispatch.

### AUDIT-024

Severity: P2  
Category: Accessibility  
Location: Admin custom controls, Android dense screens.

Current Behavior: Custom div selects, icon-only controls, tiny text and forced font scale patterns reduce accessibility.

Why It Is Wrong: A commercial app must work with screen readers, font scaling, keyboard/focus and touch targets.

Expected Behavior: Semantic controls, content descriptions, readable sizes, scalable text and visible focus.

Recommended Fix: Audit components for semantics and adopt shared accessible controls.

User Impact: Users and admins with accessibility needs struggle or make mistakes.

Acceptance Criteria: Keyboard/screen reader/touch target pass on core booking, tracking, rider and admin flows.

### AUDIT-025

Severity: P2  
Category: Micro-interactions  
Location: Android UI components and admin.

Current Behavior: Some motion exists, but it is not always tied to real state. Some repeated animations and fake live states can distract.

Why It Is Wrong: Motion should clarify feedback, not decorate uncertainty.

Expected Behavior: Press, success, error, list insertion, status transition and dismissal animations are subtle and state-backed.

Recommended Fix: Use existing motion tokens for tactile feedback and reduce animations where data is stale or unavailable.

User Impact: App can feel flashy but not trustworthy.

Acceptance Criteria: Motion never implies progress without a real event.

### AUDIT-026

Severity: P1  
Category: Customer Assistant / AI Trace  
Location: `DeliveryViewModel.kt:4731-4855`

Current Behavior: Assistant fallback can claim cancellation processed, suggest Lagos addresses and invent rider recommendations.

Why It Is Wrong: Assistant copy must never claim an action that was not executed.

Expected Behavior: Assistant either executes a real command or guides to the correct screen/support.

Recommended Fix: Replace canned command claims with command-aware responses.

User Impact: Customer may believe a cancellation or rider lock happened when nothing changed.

Acceptance Criteria: Assistant cannot state a business action succeeded unless backend command returns success.

### AUDIT-027

Severity: P1  
Category: Forms / Duplicate Actions  
Location: Booking, marketplace checkout, admin actions.

Current Behavior: Some buttons use local submitting states, but duplicate taps, partial failures and retry idempotency are not consistently handled.

Why It Is Wrong: Payments, orders and status updates must be idempotent.

Expected Behavior: Every mutating action has pending, success, failure, retry and duplicate-submission protection.

Recommended Fix: Add idempotency keys to server commands and preserve local pending UI state.

User Impact: Duplicate orders, repeated notifications or wrong balances.

Acceptance Criteria: Double-tap on booking/checkout/status creates one operation result.

### AUDIT-028

Severity: P2  
Category: Settings / Feature Flags  
Location: Admin settings and mobile global settings.

Current Behavior: Settings toggles imply system-wide control, but consumers are inconsistent or unclear.

Why It Is Wrong: Feature flags must map to real behavior and have visible disabled states.

Expected Behavior: Every setting documents internal behavior and drives all relevant clients.

Recommended Fix: Create typed settings schema with consumers and owner-only audit.

User Impact: Admin changes a setting and cannot tell what actually changed.

Acceptance Criteria: Toggle marketplace/notifications/tips/points and verify all expected screens change predictably.

### AUDIT-029

Severity: P1  
Category: Support  
Location: Admin support, Android support chat.

Current Behavior: Support is not tightly bound to order state and can use generic admin identity.

Why It Is Wrong: Delivery support needs context and accountability.

Expected Behavior: Ticket includes order, current state, actor identity, message ownership and next action.

Recommended Fix: Add structured support ticket model linked to delivery/customer/rider.

User Impact: Support staff answer slowly or incorrectly.

Acceptance Criteria: From an order, support sees full timeline and replies as actual admin user.

### AUDIT-030

Severity: P2  
Category: Project Maintainability  
Location: Giant source files, duplicate docs, conflicting agent instructions.

Current Behavior: Very large ViewModel/Admin files, generated `functions/lib`, root and nested AGENTS conflict on brand/model.

Why It Is Wrong: Future changes become risky and agents can follow stale instructions.

Expected Behavior: Clear architecture docs and aligned agent rules.

Recommended Fix: Split large modules gradually and keep one authoritative product rule set.

User Impact: Bugs keep returning because future edits are hard to reason about.

Acceptance Criteria: Root and mobile AGENTS agree on brand/language/security principles; new work follows smaller modules.

### AUDIT-031

Severity: P0  
Category: Secrets / Production Security  
Location: `functions/service-account.json`, `src/lib/firebase.js`, Android build/config paths.

Current Behavior: The repository contains a Firebase service-account JSON file path and web Firebase config fallbacks in source. The audit document does not reproduce secrets, but their presence/location is security-sensitive.

Why It Is Wrong: Service-account credentials should not live in the app repository. Public Firebase client config is often not secret by itself, but hardcoded fallbacks make environment separation weaker and encourage accidental production coupling.

Expected Behavior: Server credentials live only in secret management. Client config is environment-provided and rules enforce data access.

Recommended Fix: Rotate any exposed service-account credential, remove it from repository history if needed, use environment secrets, and document allowed public config separately.

User Impact: Credential leakage can compromise backend trust and customer data.

Acceptance Criteria: No private service-account credential exists in the repo; production functions use managed secrets.

### AUDIT-032

Severity: P1  
Category: Backup / Privacy  
Location: `mobile/app/src/main/res/xml/data_extraction_rules.xml`, `backup_rules.xml`

Current Behavior: Android backup rule files still contain template/TODO-style comments from generated Android project files.

Why It Is Wrong: Even if not user-facing, backup defaults affect whether local data, tokens, cached orders or preferences may be backed up/restored unexpectedly.

Expected Behavior: Backup rules intentionally include/exclude sensitive local data.

Recommended Fix: Audit Room database, encrypted preferences, notification cache, crash cache and tokens; explicitly exclude secrets and sensitive delivery records where appropriate.

User Impact: Restored or backed-up sensitive data can create privacy and session confusion.

Acceptance Criteria: Backup rules contain deliberate include/exclude policy and no template TODO remains.

### AUDIT-033

Severity: P2  
Category: Accessibility / Font Scaling  
Location: `MainActivity.kt:100-103`

Current Behavior: App configuration forces font scale to `1.0f` in the base context.

Why It Is Wrong: Users who need larger text may not get their chosen accessibility setting.

Expected Behavior: App supports system font scaling with layout constraints that prevent overlap.

Recommended Fix: Remove forced font-scale override after responsive text/layout testing, or provide a justified accessibility-compatible strategy.

User Impact: Low-vision users struggle to read the app.

Acceptance Criteria: Core screens remain usable at common enlarged font sizes.

### AUDIT-034

Severity: P2  
Category: Marketplace / Icon Semantics  
Location: `MarketplaceScreen.kt:117-170`, `MarketplaceScreen.kt:865-980`

Current Behavior: Heart and cart icons generally map to favorites/cart, which is good. However the same icon surfaces open sheets, toggle item state or add to cart depending on context, and some actions only show after swipe gestures.

Why It Is Wrong: Reused icons are acceptable only if the context is obvious. Hidden swipe actions can be missed.

Expected Behavior: Header heart opens favorites; product heart toggles favorite; cart opens cart or adds exact item with immediate feedback. Labels/tooltips/content descriptions should clarify context.

Recommended Fix: Add consistent content descriptions and feedback. Keep swipe actions as shortcuts, not the only path.

User Impact: Users may not discover favorites or may confuse save/open actions.

Acceptance Criteria: Every heart/cart icon has correct accessible label and expected result.

### AUDIT-035

Severity: P1  
Category: Marketplace / Quantity and Stock  
Location: `MarketplaceScreen.kt:635-674`, `DeliveryViewModel.kt:5752-5827`

Current Behavior: Cart quantity controls update local state quickly, but stock limits, deleted products and price changes are only fully checked at checkout. Quantity increment controls do not visibly communicate max stock before checkout.

Why It Is Wrong: A cart should guide the user before final failure.

Expected Behavior: Quantity is bounded by available stock, disabled product state is visible and price changes are reconciled before payment.

Recommended Fix: Store live stock/price state in cart rows and disable or warn before checkout.

User Impact: Customer can build a cart that fails late.

Acceptance Criteria: User cannot increase quantity past known stock; deleted/out-of-stock items show a clear cart issue.

### AUDIT-036

Severity: P1  
Category: Marketplace / Payment Method  
Location: `MarketplaceScreen.kt:803-857`, `DeliveryViewModel.kt:5883-6210`

Current Behavior: Paystack and wallet paths both call the same marketplace checkout function with payment method strings. Server verification and order fulfilment are not clearly separated.

Why It Is Wrong: Payment method strings are not enough proof of payment.

Expected Behavior: Paystack flow creates/verifies a server payment intent before fulfilment; wallet flow reserves wallet balance server-side.

Recommended Fix: Replace payment method string contract with typed payment intent/reference records.

User Impact: Customer may see an order created without payment certainty or payment made without fulfilment certainty.

Acceptance Criteria: Marketplace order is created only after verified payment/reservation, with recoverable pending states.

### AUDIT-037

Severity: P2  
Category: Email / AI Trace  
Location: `functions/src/emails/emailTemplates.ts`

Current Behavior: Email footer/body copy includes phrasing like an autonomous platform transmission.

Why It Is Wrong: It sounds like a machine-generated system announcement rather than a polished logistics email.

Expected Behavior: Emails should use plain, trustworthy product copy.

Recommended Fix: Rewrite email templates around customer value: verification, receipt, delivery update and support.

User Impact: Emails feel automated in a bad way and reduce brand trust.

Acceptance Criteria: Emails contain no AI/autonomous/transmission/developer-style copy unless part of a deliberately branded admin diagnostic.

### AUDIT-038

Severity: P1  
Category: Email / Notification Reliability  
Location: `functions/src/index.ts`, email trigger paths.

Current Behavior: Email automation expects fields not consistently stored by normal bookings, and some send results are logged without full success propagation.

Why It Is Wrong: A feature that silently cannot send correct emails is worse than no feature.

Expected Behavior: Email requirements match booking schema and failures become retryable events.

Recommended Fix: Align recipient email/phone/channel fields and persist email delivery status.

User Impact: Customers may not receive codes, receipts or updates.

Acceptance Criteria: Normal Android/admin-created orders produce correct email/SMS/notification behavior or visible fallback.

### AUDIT-039

Severity: P1  
Category: Public Tracking  
Location: `src/pages/PublicTrackingPage.tsx`, `/track/:id`

Current Behavior: Public tracking maps status to a simple timeline and listens to a delivery document by ID.

Why It Is Wrong: Public tracking by raw ID risks exposing too much and does not model safe tokenized access.

Expected Behavior: Public tracking uses unguessable token and redacted read model.

Recommended Fix: Create `publicTracking/{token}` or callable lookup that returns safe fields only.

User Impact: Shared tracking link may leak private information or fail to reflect complex states.

Acceptance Criteria: Public tracking cannot fetch arbitrary delivery by raw ID and never shows OTP/private phone data.

### AUDIT-040

Severity: P2  
Category: Admin / Search  
Location: `AdminDashboard.tsx` header and shipment filters.

Current Behavior: Global search and local tab search can overlap. Some ID matching lowercases query but not the ID.

Why It Is Wrong: Search should help, not silently filter the wrong surface or miss exact IDs.

Expected Behavior: Global search is clearly global; local search is scoped; exact ID lookup is case-insensitive.

Recommended Fix: Separate search scopes and provide direct order selection by ID.

User Impact: Admin cannot find the exact order after clicking from another screen.

Acceptance Criteria: Searching any case variant of an order ID finds it; popup Manage opens exact drawer.

### AUDIT-041

Severity: P2  
Category: Admin / Bulk Actions  
Location: `AdminDashboard.tsx`

Current Behavior: Bulk selection and action flow does not provide enough per-order eligibility review.

Why It Is Wrong: Bulk dispatch actions can corrupt terminal, blocked or stale orders if treated as one uniform group.

Expected Behavior: Bulk action preview shows eligible, blocked and terminal rows separately.

Recommended Fix: Add preflight server validation and a review panel before bulk writes.

User Impact: Admin can accidentally update the wrong deliveries.

Acceptance Criteria: Bulk assign/update cannot apply to ineligible orders; admin sees why each blocked row is blocked.

### AUDIT-042

Severity: P1  
Category: Rider / Chain of Custody  
Location: Rider delivery actions, reassignment paths.

Current Behavior: Reassignment and rider status paths do not fully model package custody once pickup occurs.

Why It Is Wrong: After pickup, changing rider is physical custody transfer, not a simple field edit.

Expected Behavior: Custody transfer requires proof, actors, timestamps and admin/customer visibility.

Recommended Fix: Add custody events and transfer command.

User Impact: Company cannot resolve "who had my package?" disputes.

Acceptance Criteria: Reassignment after pickup creates a custody transfer event before new rider owns the job.

### AUDIT-043

Severity: P1  
Category: Cancellation / Refund  
Location: Customer assistant, tracking, admin status changes.

Current Behavior: Cancellation appears in copy/status but lacks a complete customer/admin/refund/custody flow.

Why It Is Wrong: Cancellation rules differ before assignment, after assignment, after pickup and after failed delivery.

Expected Behavior: Cancellation request creates a state with refund policy and package custody rules.

Recommended Fix: Add `requestCancellation`, `approveCancellation`, `rejectCancellation`, `refundPending` and `returnRequired` flows.

User Impact: Customer may think cancellation happened when it did not, or admin may cancel without refund clarity.

Acceptance Criteria: Cancellation before assignment, after assignment and after pickup each produce correct customer/admin/finance outcomes.

### AUDIT-044

Severity: P2  
Category: Logging / User Trust  
Location: Android logs, admin logs, Cloud Functions logs.

Current Behavior: Many failures are logged internally but not surfaced as product states. Some logs are client-authored.

Why It Is Wrong: Logs are not a substitute for user-visible recovery and auditable operations.

Expected Behavior: Operational failures create domain events/tasks; audit logs are server-owned.

Recommended Fix: Build exception/retry task records for failed dispatch/payment/proof/notification operations.

User Impact: Users see silence or false success when something failed.

Acceptance Criteria: Inject failure in payment/proof/notification and admin sees a specific recovery task.

### AUDIT-045

Severity: P2  
Category: Loading / Duplicate Feedback  
Location: Toast-heavy Android flows.

Current Behavior: Many operations use Toasts as the main success/failure feedback.

Why It Is Wrong: Toasts vanish, do not preserve state, and are poor for critical delivery/payment flows.

Expected Behavior: Critical actions show inline state, disabled pending button, result and retry path.

Recommended Fix: Use screen-level or card-level operation states for booking, checkout, proof, cancellation and assignment.

User Impact: User misses important failure and repeats action.

Acceptance Criteria: Payment/proof/booking errors remain visible until resolved or dismissed.

### AUDIT-046

Severity: P2  
Category: Customer History  
Location: Order history/tracking models.

Current Behavior: History relies on limited parcel fields and cannot explain full event, proof, refund and support context.

Why It Is Wrong: A real customer history must answer what happened.

Expected Behavior: Order details include event timeline, receipt, proof, support and refund state.

Recommended Fix: Add customer-safe delivery event read model.

User Impact: Customer cannot self-serve dispute or receipt questions.

Acceptance Criteria: Delivered order shows pickup, delivery, proof, rider and payment summary.

### AUDIT-047

Severity: P2  
Category: Vendor / Role UX  
Location: Vendor portal, marketplace admin, vendor store logic.

Current Behavior: Vendor creation, verification and marketplace visibility exist, but vendor readiness/order fulfilment states are weak.

Why It Is Wrong: Marketplace vendors need pick/pack/ready/unavailable/substitution flows.

Expected Behavior: Vendor order flow drives dispatch readiness.

Recommended Fix: Add vendor fulfilment states and admin oversight.

User Impact: Rider may be dispatched before vendor goods are ready.

Acceptance Criteria: Vendor must mark order ready or system schedules pickup before dispatch.

### AUDIT-048

Severity: P2  
Category: Performance / Scalability  
Location: Admin and Android Firestore listeners.

Current Behavior: Several screens listen to broad collections and map large client-side lists.

Why It Is Wrong: It may work with small data but degrade with real order volume.

Expected Behavior: Use indexed queries, pagination, read models and role-scoped subscriptions.

Recommended Fix: Replace broad listeners with targeted dashboard/queue read models.

User Impact: Admin/mobile app slows down as business grows.

Acceptance Criteria: Dashboard loads current operational read model without scanning whole collections.

### AUDIT-049

Severity: P3  
Category: Copy / Tone  
Location: Customer assistant, tracking, admin, emails.

Current Behavior: Copy often uses dramatic terms: autonomous, AI manager, fully synchronized, optimized precision, secure platform.

Why It Is Wrong: Over-polished machine language makes users skeptical.

Expected Behavior: Use simple, specific, human product language.

Recommended Fix: Create copy glossary for delivery, payment, support, proof and errors.

User Impact: Product feels less trustworthy and less local.

Acceptance Criteria: Core screens pass UX-language review by a normal customer/rider/admin.

### AUDIT-050

Severity: P1  
Category: Runtime Verification Gap  
Location: Whole application.

Current Behavior: This pass is source-based. Browser/device walkthrough and production Firebase parity were not completed.

Why It Is Wrong: Source review finds many issues, but launch readiness also requires real flows on device/browser/staging data.

Expected Behavior: Every release has source review plus runtime walkthrough.

Recommended Fix: Run staging flow with customer, rider and admin accounts after core fixes.

User Impact: Unseen layout/device/runtime failures may remain.

Acceptance Criteria: Physical Android device and browser admin walkthrough pass full delivery, marketplace, notification and support test matrix.

## Human Review Answers

Does this feel like a real product? Not consistently. It often looks like one, but too many screens expose implementation traces, fake certainty or incomplete business state.

Does anything make it feel generated or stitched together? Yes: old brand names, "AI" labels everywhere, fake score wording, Firebase/SHA-1 auth copy, seed/sample admin controls, simulation language and inconsistent status models.

Does anything feel like a developer talking? Yes: crash report dialog, Google setup error dialog, admin seed/visibility copy, Firebase OTP wording and implementation-status labels.

Where would a normal user become confused? Queue/assignment, marketplace disabled/empty states, notification repeats, tracking ETA, cancellation, proof/OTP and support outcomes.

Where would a user lose trust? When tracking moves without real GPS, notifications return after dismissal, assistant claims an action that did not happen, delivery says complete without proof, or payment/wallet state fails silently.

## Required Product Standard

The product must become coherent enough that nobody cares how it was built. Remove the development process from the UI. Replace fake-smart language with real state. Replace disconnected status edits with server-owned lifecycle commands. Replace broad notifications with deduped events. Replace "works in code" with complete user journeys.
