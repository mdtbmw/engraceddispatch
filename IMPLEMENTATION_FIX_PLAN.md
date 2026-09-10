# IMPLEMENTATION FIX PLAN

Date: 2026-09-09  
Project: `D:\Eng App`  
Purpose: Convert the master audit into phased implementation work. This is not a duplicate audit; it is a repair sequence for future development.

## Phase 1 - Safety, Secrets and Product-Language Cleanup

### FIX-001 - Remove user-facing developer/crash text

Problem: Normal users can see crash text and instructions to send details to a developer.

Files/components affected: `mobile/app/src/main/java/com/esdispatch/MainActivity.kt`

Required change: Replace crash dialog with user-safe recovery and optional support report flow. Do not show raw crash content to ordinary users.

Expected behavior: User sees a calm recovery state.

Acceptance criteria: No stack trace, "developer", raw exception or clipboard crash dump appears in normal UI.

Dependencies: None.

Regression risks: Support may lose ad-hoc crash copy. Add structured crash reporting if needed.

### FIX-002 - Remove Firebase/OAuth setup copy from auth UI

Problem: Auth screens expose SHA-1, package name, Firebase Console and OAuth setup wording.

Files/components affected: `AuthScreens.kt`

Required change: Replace with normal product copy. Move technical details to logs or developer-only diagnostics.

Expected behavior: User sees "Google sign-in is temporarily unavailable. Try email sign-in or contact support."

Acceptance criteria: No normal auth error mentions SHA-1, Firebase Console, package name or OAuth setup.

Dependencies: Product-language rules in `AGENTS.md`.

Regression risks: Developers lose visible setup hints. Keep debug logs.

### FIX-003 - Remove fake/mock/simulation language from production UI

Problem: Screens contain "Fake Score", simulation, seed/demo and implementation-status language.

Files/components affected: `AIDispatchManagerScreen.kt`, `RiderScreens.kt`, `AdminDashboard.tsx`, marketplace/admin settings.

Required change: Rewrite or gate terms:

- "Fake Score" -> "Review confidence" only if real.
- "Simulation" -> remove from production.
- "Seed" -> developer-only.
- "LIVE ON APP" -> "Available to customers".

Expected behavior: Product surfaces sound professional.

Acceptance criteria: AI-trace scan shows no inappropriate user-facing terms.

Dependencies: Developer-only environment gate.

Regression risks: Admins may need seed tools in staging. Provide a staging-only tools panel.

### FIX-004 - Secure delivery/OTP/proof reads and writes

Problem: Delivery data, OTP and proof are too exposed.

Files/components affected: `firestore.rules`, `storage.rules`, `functions/src/index.ts`, `FirebaseManager.kt`, `DeliveryViewModel.kt`, `TrackingScreen.kt`

Required change: Introduce redacted read models and server-owned OTP/proof commands. Remove OTP secret from customer/rider-readable delivery docs.

Expected behavior: Public tracking cannot expose private data or OTP.

Acceptance criteria: Rules tests deny unrelated/public delivery reads; rider/customer payloads contain no OTP secret.

Dependencies: Lifecycle command design.

Regression risks: Old clients may rely on raw fields. Add compatibility migration.

### FIX-005 - Move money mutation to server commands

Problem: Wallet, payment and payout mutations happen through inconsistent client/server paths.

Files/components affected: `WalletViewModel.kt`, `FirebaseManager.kt`, `functions/src/index.ts`, admin wallet UI.

Required change: Use payment intents, server verification and ledger postings. Remove direct client increments for sensitive money fields.

Expected behavior: All credits/debits reconcile.

Acceptance criteria: Payment reference verifies amount/currency/UID; double submit posts once.

Dependencies: Ledger schema.

Regression risks: Payment UI must handle pending/retry states.

## Phase 2 - Core Dispatch Lifecycle

### FIX-006 - Define shared lifecycle schema

Problem: Android, web, functions and email automation use inconsistent status names.

Files/components affected: `Models.kt`, `AdminDashboard.tsx`, `functions/src/index.ts`, tracking/admin/rider screens.

Required change: Add versioned lifecycle fields: queue, assignment, fulfilment, proof, payment, settlement and exception.

Expected behavior: Every client reads the same delivery truth.

Acceptance criteria: One order advanced by rider appears consistently in admin, customer app, public tracking and notifications.

Dependencies: Migration plan.

Regression risks: Old status mapping ambiguity, especially `ARRIVED` and `OUT_FOR_DELIVERY`.

### FIX-007 - Implement FIFO queue

Problem: No durable queue exists when no rider is available.

Files/components affected: `functions/src/index.ts`, Firestore schema, admin dashboard, customer tracking.

Required change: Store queue state, queue position/reason and oldest eligible ordering. Add worker triggered by rider availability/job completion/schedule.

Expected behavior: Customer sees queued; admin sees oldest eligible first.

Acceptance criteria: Three no-rider orders are assigned in FIFO order when riders become available.

Dependencies: Rider availability model.

Regression risks: Priority orders require explicit policy.

### FIX-008 - Implement rider availability and reserve-next

Problem: Online riders are treated as available.

Files/components affected: rider state schema, admin rider selector, functions, rider app.

Required change: Compute availability from shift, approval, GPS freshness, active job, reserved job, vehicle capacity, account status and inspection.

Expected behavior: Busy riders can be reserved next without being shown as en route.

Acceptance criteria: Rider with active exclusive job cannot receive another active job; can receive next reservation if eligible.

Dependencies: Shared lifecycle.

Regression risks: Existing admin assignment UI must migrate to command path.

### FIX-009 - Replace raw status dropdowns with legal actions

Problem: Status edits can be blank, stale, illegal or bypassed.

Files/components affected: `AdminDashboard.tsx`, `RiderScreens.kt`, backend functions.

Required change: UI shows one legal next action from server state. Server command enforces actor, stage and evidence.

Expected behavior: No invalid status transition is possible.

Acceptance criteria: Delivered/cancelled cannot move backward; delivered requires proof path.

Dependencies: Lifecycle command API.

Regression risks: Legacy orders need compatibility actions.

### FIX-010 - Add exception engine

Problem: Real delivery problems have no first-class state.

Files/components affected: order schema, admin drawer, rider job sheet, customer tracking, support.

Required change: Add exception substates and workflows for sender unavailable, recipient unavailable, wrong address, breakdown, damaged parcel, return, cancellation and proof failure.

Expected behavior: Problems create visible tasks instead of fake forward progress.

Acceptance criteria: Recipient unavailable keeps custody open and alerts admin/customer with next action.

Dependencies: Lifecycle/event log.

Regression risks: More states require strong copy and simple UI.

## Phase 3 - Admin, Customer and Rider UX Repair

### FIX-011 - Build admin dispatch drawer

Problem: Admin must leave dashboard popup and search in shipments to act.

Files/components affected: `AdminDashboard.tsx`

Required change: Order drawer with full details, rider selector, reserve-next, next action, timeline, contacts, payment, proof and exception controls.

Expected behavior: Admin acts in one context.

Acceptance criteria: Clicking any active/queued order opens drawer and can assign/reserve without tab search.

Dependencies: Lifecycle and assignment commands.

Regression risks: Large admin file should be split carefully.

### FIX-012 - Stabilize admin state during live updates

Problem: Nested tab components can reset local state.

Files/components affected: `AdminDashboard.tsx`

Required change: Move tabs to module scope or separate components. Preserve selected order/ticket/filter/draft state.

Expected behavior: Live updates do not close drawers or clear drafts.

Acceptance criteria: New delivery arrives while support draft is open; draft remains.

Dependencies: Component extraction plan.

Regression risks: Props/refactoring errors; run web typecheck.

### FIX-013 - Fix customer tracking truth

Problem: Tracking shows assigning/live/AI precision without enough backing state.

Files/components affected: `TrackingScreen.kt`, `DeliveryViewModel.kt`

Required change: Show received, queued, reserved, assigned, heading, picked up, in transit, proof and delivered states from server read model.

Expected behavior: Tracking never invents movement or precision.

Acceptance criteria: No-rider order shows queued; stale GPS shows last update time.

Dependencies: Queue and GPS freshness.

Regression risks: Existing animation logic may need cleanup.

### FIX-014 - Rebuild rider current/next job UI

Problem: Rider app does not clearly separate current job from next reserved job.

Files/components affected: `RiderScreens.kt`, rider view model.

Required change: Current job card, next job card, one primary action, call/navigate/exception/proof actions.

Expected behavior: Rider knows what to do now.

Acceptance criteria: Reserved job cannot be started until promoted; current job action is always clear.

Dependencies: Reserve-next schema.

Regression risks: Existing active/available filters must be remapped.

### FIX-015 - Upgrade empty/loading/error states

Problem: Empty, disabled, offline and failed states blur together.

Files/components affected: marketplace, tracking, notifications, admin lists, history, support, riders.

Required change: Introduce data-state component pattern: loading, empty, error, disabled, offline, permission denied, retry.

Expected behavior: Every blank area explains what happened and the next action.

Acceptance criteria: Marketplace disabled differs from no products and no search results.

Dependencies: View state wrappers.

Regression risks: More copy must follow product-language rules.

## Phase 4 - Marketplace and Notifications

### FIX-016 - Add designed marketplace disabled state

Problem: Marketplace disabled state is described in admin, but mobile customer experience is not intentionally designed.

Files/components affected: `MarketplaceScreen.kt`, dashboard market entry, admin settings.

Required change: When disabled, show service-focused state or hide entry with replacement action according to owner policy.

Expected behavior: Customer does not see a broken/empty marketplace.

Acceptance criteria: Admin disables marketplace; customer sees intentional state, no blank catalog.

Dependencies: Global settings listener reliability.

Regression risks: Dashboard hero action must remain according to project rules.

### FIX-017 - Move marketplace checkout to server

Problem: Client checkout is fragile and creates weak fulfilment orders.

Files/components affected: `DeliveryViewModel.kt`, functions, marketplace screens, rules.

Required change: Backend reserves stock, verifies payment, creates order, creates vendor fulfilment and delivery task.

Expected behavior: Checkout is atomic and fulfilment-ready.

Acceptance criteria: Multi-vendor order creates valid fulfilment plan or separate deliveries.

Dependencies: Payment/ledger server work.

Regression risks: Vendor portal must understand new order states.

### FIX-018 - Implement notification event service

Problem: Notifications repeat, duplicate, fail to clear and open wrong destinations.

Files/components affected: functions, `FirebaseManager.kt`, `MyFirebaseMessagingService.kt`, profile notification UI, admin notification UI.

Required change: Event records, recipient records, dedupe keys, read/dismiss state, normalized FCM payload and deep links.

Expected behavior: Notifications appear once and clear permanently.

Acceptance criteria: Dismissed notification does not return after app restart; each notification opens exact item.

Dependencies: Lifecycle events.

Regression risks: Migration of old notifications.

## Phase 5 - Visual Consistency, Accessibility and Motion

### FIX-019 - Apply product copy system

Problem: Copy is inconsistent, over-technical and sometimes AI-generated sounding.

Files/components affected: all user-facing strings.

Required change: Central copy audit. Replace developer jargon, fake confidence, production claims and old brand strings.

Expected behavior: The app sounds like one product team wrote it.

Acceptance criteria: AI-trace scan returns only legitimate developer comments/logs, not production UI strings.

Dependencies: Updated AGENTS rules.

Regression risks: Accidental wording drift in future changes.

### FIX-020 - Accessibility pass

Problem: Some controls are too small, custom, low-semantics or poor for screen readers/font scaling.

Files/components affected: shared components, admin selects, Android icon buttons, tracking screen.

Required change: Validate touch targets, content descriptions, focus order, contrast, font scaling and motion sensitivity.

Expected behavior: Core flows are accessible.

Acceptance criteria: Booking, tracking, rider current job and admin dispatch drawer pass accessibility checklist.

Dependencies: Component cleanup.

Regression risks: Some layouts may need responsive adjustment.

### FIX-021 - Motion pass

Problem: Motion is sometimes decorative or tied to fake live state.

Files/components affected: Android components, tracking map, admin rows/drawers, marketplace cards.

Required change: Use subtle tactile motion for press, list insert/remove, status transitions, notification dismissal and proof success.

Expected behavior: App feels alive because real events animate.

Acceptance criteria: No animation implies live progress without real state.

Dependencies: Event/lifecycle model.

Regression risks: Excessive motion; provide reduce-motion path where needed.

## Phase 6 - Final QA and Release Evidence

### FIX-022 - Create release test matrix

Problem: Existing checks do not prove full product flow.

Files/components affected: docs/tests, emulator tests, admin tests.

Required change: Write and run tests for booking, queue, reserve-next, assignment, pickup, delivery, proof, settlement, cancellation, marketplace, notifications and support.

Expected behavior: Release readiness is based on complete journeys.

Acceptance criteria: Dedicated staging accounts complete full Benin lifecycle on admin/customer/rider.

Dependencies: All core fixes.

Regression risks: Requires staging data discipline.

### FIX-023 - Build/deploy verification discipline

Problem: Project history warns of partial verification and uncommitted web fixes.

Files/components affected: release process.

Required change: Follow root and mobile AGENTS build/release rules for any code change. Commit intended files and do not claim live deployment without owner redeploy.

Expected behavior: Fixes can be traced to artifacts.

Acceptance criteria: Web typecheck, Android release APK, device install and git state are recorded for code changes.

Dependencies: ADB/device availability.

Regression risks: Time cost; necessary for trust.

### FIX-024 - Rotate and remove private service credentials

Problem: Repository contains a Firebase service-account file path and hardcoded config fallbacks.

Files/components affected: `functions/service-account.json`, hosting/function environment config, repository history if needed.

Required change: Rotate exposed private credentials, remove private credential files, load server secrets from managed environment.

Expected behavior: No private credential is committed.

Acceptance criteria: Secret scan passes; functions still deploy/run using managed credentials.

Dependencies: Owner access to Firebase/Google Cloud.

Regression risks: Functions deployment can fail if environment secrets are not configured.

### FIX-025 - Define Android backup/privacy policy

Problem: Backup config appears template-like and may not intentionally protect local sensitive data.

Files/components affected: `data_extraction_rules.xml`, `backup_rules.xml`, local database/preferences.

Required change: Explicitly include/exclude local databases, encrypted preferences, crash cache, tokens and proof files.

Expected behavior: Sensitive data is not unintentionally restored/backed up.

Acceptance criteria: Backup files have no TODO template policy and pass privacy review.

Dependencies: Inventory local storage.

Regression risks: Users may lose useful cached state on device restore; decide intentionally.

### FIX-026 - Fix font scaling and accessibility foundations

Problem: App forces font scale and several controls are small or custom.

Files/components affected: `MainActivity.kt`, shared components, admin selects, Android screens.

Required change: Support system font scaling, touch targets, focus order and semantic labels.

Expected behavior: App remains usable at larger text sizes.

Acceptance criteria: Core flows pass accessibility review at default and enlarged font settings.

Dependencies: Layout QA.

Regression risks: Some screens may need responsive spacing changes.

### FIX-027 - Add product copy glossary and string review gate

Problem: UI copy varies between hype, technical language and old brand terms.

Files/components affected: all user-facing strings, emails, notifications, admin.

Required change: Create approved terms for request, queued, rider reserved, delivered, proof, wallet, refund, support, marketplace and errors.

Expected behavior: One professional product voice.

Acceptance criteria: No normal UI includes developer/AI trace wording; old brand strings are removed where user-facing.

Dependencies: Brand decisions from owner.

Regression risks: Rewriting copy can accidentally remove useful context; review by role.

### FIX-028 - Build marketplace availability and fulfilment states

Problem: Disabled marketplace, no products, search empty, vendor not ready and fulfilment states blur together.

Files/components affected: marketplace screen, dashboard market entry, vendor portal, admin marketplace.

Required change: Add distinct state model and UI for disabled, empty, loading, error, out-of-stock, vendor ready and fulfilment pending.

Expected behavior: Marketplace always feels intentional.

Acceptance criteria: Toggle marketplace off, delete all products, search no results and vendor not ready each show distinct helpful states.

Dependencies: Global settings reliability.

Regression risks: Dashboard hero rules must remain intact.

### FIX-029 - Add route/public tracking redaction

Problem: Public tracking by raw delivery document is too broad.

Files/components affected: public web tracking, functions, Firestore rules.

Required change: Use unguessable tracking token/read model with redacted fields.

Expected behavior: Shared tracking links are safe and useful.

Acceptance criteria: Raw delivery IDs do not expose private fields; token returns only customer-safe tracking state.

Dependencies: Delivery read model.

Regression risks: Existing shared links need migration/redirect policy.

### FIX-030 - Create full runtime QA script

Problem: Source review cannot prove product feel or runtime parity.

Files/components affected: QA docs, staging fixtures, device/browser test process.

Required change: Create a manual and automated QA script for customer, rider, admin, marketplace, notifications, OTP, proof, offline and support.

Expected behavior: Release signoff is based on exercised flows.

Acceptance criteria: QA checklist is run on physical Android device and admin browser against staging data.

Dependencies: Test accounts and device access.

Regression risks: Requires ongoing maintenance as flows change.

## Execution Order

1. Update AGENTS rules.
2. Remove/gate user-facing developer trace language.
3. Secure rules around delivery/OTP/proof/money/support.
4. Add server commands and lifecycle schema.
5. Add queue and reserve-next.
6. Rebuild admin drawer around lifecycle.
7. Rebuild customer/rider state presentation.
8. Fix notifications.
9. Move marketplace checkout/fulfilment server-side.
10. Polish accessibility/motion/copy.
11. Run full release QA.

## Final Rule

Do not patch symptoms with more labels. Every user-facing statement must be backed by real product behavior.
