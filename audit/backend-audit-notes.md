# ESDispatch backend, trust boundary and cross-app flow audit notes

Audit date: 2026-09-09. Read-only review of the current working tree, including pre-existing edits. No application code changed, no production writes, no backend callables invoked, no payment initiated, no rules emulator or device session exercised. Paths and line numbers refer to the files inspected at review time. Findings describe the checked-in/current source contract; production deployment parity is not established.

## Executive interpretation

The fundamental problem is that the app has several independent actors writing an order's truth: customer Android code, rider Android code, Android admin code, web admin code, and Cloud Functions. Those actors disagree on stages, assignment rules, payment ownership, payout percentages, field names and notification schemas. Firestore often allows the wrong actor to change sensitive data while denying the ordinary writes a visible feature actually attempts. A screen can look convincing and still be connected to a workflow that cannot complete under these rules.

There are useful foundations: online self-claim checks and a transaction for competing claims to the same pending parcel; a web status transition map and assignment checks; a top-up callable with gateway verification and a reference ledger transaction; OTP expiry and attempt concepts; restricted server ledger writes; live listeners; soft deletion; and a dedicated audit collection. The defects are not that none of these ideas exist. The defects are that the boundaries do not line up, and the strongest paths are sometimes bypassed by the visible UI.

Severity convention: **P0** threatens money, private customer data or delivery integrity; **P1** breaks a principal operational/customer flow; **P2** creates misleading states, operational friction or maintainability exposure. **Confirmed** means the source directly establishes the mismatch. **Conditional** means the bad outcome depends on concurrent calls, trigger deployment, configuration or a particular branch. **Gap** means no implementation was found in the inspected mechanism; it is not a claim that a production team has no manual process.

## Dispatch and state control

### B01 — P1 / Confirmed: the alleged queue is a log message, not a dispatch lifecycle

- **Evidence:** `functions/src/index.ts:76-97` runs automatic assignment only on creation of an uppercase `PENDING` document. If no online rider exists, it logs that the delivery is queued and returns without changing the record. `mobile/app/src/main/java/com/esdispatch/data/Models.kt:7-8` has no `QUEUED`, `PREASSIGNED`, rider offer or reservation state. The function exports in `functions/src/index.ts` contain no rider-availability queue-draining trigger, scheduler or subsequent mission promotion worker.
- **User/company consequence:** the customer is left in an ambiguous waiting state. Operations cannot distinguish “received but no capacity” from “not yet acknowledged” or “automation failed.” Becoming available later does not itself cause the backend to revisit older requests. New orders can be matched immediately while older ones remain pending.
- **Required behavior:** persist an acknowledged queue entry with server receipt time, reason, priority, eligibility and next action. On rider availability or mission completion, allocate the oldest eligible order atomically. An explicit express priority must remain visible and accountable; it must not silently erase FIFO fairness.
- **Acceptance:** place three orders while all riders are busy; each shows “Queued — request received.” Release one suitable rider; exactly the first eligible order leaves the queue. A worker restart must not lose or duplicate jobs.

### B02 — P1 / Confirmed: “online” is treated as “available,” regardless of current workload

- **Evidence:** `functions/src/index.ts:89-131` filters `users` by role and `isOnline`, calculates nearest distance, then writes `ASSIGNED`. It does not inspect an active delivery, reserved job, maximum load, suspension, deletion, shift validity, device heartbeat or vehicle capability. `FirebaseManager.kt:1538-1567` verifies online only for self-claims; the comment explicitly exempts admin assignment. The self-claim transaction locks one parcel, not rider capacity across parcels.
- **Consequence:** the same rider can receive multiple simultaneous active jobs. An online rider finishing a mission is indistinguishable from a rider free to collect now. The customer sees an assignment that may not be actionable.
- **Remedy:** use separate authenticated presence, availability, active job and reserved-next-job fields, maintained by server commands. Enforce a capacity invariant for both auto and manual assignment. Admin override must capture a reason and the customer consequence.
- **Acceptance:** submit multiple pending orders concurrently against one free rider. Only one becomes active; the rest queue or reserve in a defined order. A suspended or stale rider is never auto-selected.

### B03 — P1 / Conditional: auto-assignment can overwrite a later human decision

- **Evidence:** `functions/src/index.ts:80-82` reads the creation-event snapshot; `121-131` later performs an unconditional update. There is no read of current status, assignment version or transaction before writing.
- **Consequence:** if an admin cancels or manually assigns during rider lookup, the creation trigger can overwrite that decision with `ASSIGNED` and a different rider. Two admins can also race through nontransactional web assignment at `src/app/engdadmin/AdminDashboard.tsx:1943-1947`.
- **Remedy:** one server assignment command with a compare-and-set expected order revision and rider reservation. Return a visible “This order changed; refresh to continue” conflict.
- **Acceptance:** pause matching, cancel the order, then resume matching; it stays cancelled. Two dispatchers assigning the same order produce one assignment and one clear conflict, with no duplicate rider notification.

### B04 — P1 / Confirmed: automatic dispatch is geographically wrong for a Benin-only operator

- **Evidence:** `functions/src/index.ts:100-109` supplies Lagos coordinates `6.5244, 3.3792` when pickup or rider coordinates are absent. `FirebaseManager.kt:300-308` also initializes new rider profiles with coordinates outside Benin and invented battery/time values. `LocationService.kt:65-76` publishes GPS into `fleet_locations`, whereas the matching function reads location fields from `users`. The general parcel writer at `FirebaseManager.kt:492-526` supplies no pickup/drop-off coordinates.
- **Consequence:** nearest-rider selection can be based on defaults or a stale profile rather than the location service. Missing information is disguised as precise geography. There is no server-side Benin service-area gate in creation/dispatch rules or functions.
- **Remedy:** store geocoded pickup and delivery points with verification confidence; validate both against the operational Benin polygon; read timestamped telemetry from the canonical fleet source. Missing GPS must produce “location unavailable,” not a substitute city.
- **Acceptance:** a Benin job with missing pickup coordinates is queued for address validation; a Lagos request cannot be dispatch-ready; stale/offline rider GPS is clearly excluded or explicitly handled.

### B05 — P0 / Confirmed: order business rules are not enforced at the data boundary

- **Evidence:** `firestore.rules:175-186` allows customer-owned or rider-assigned delivery updates without an affected-field allowlist or transition validation. The pending claim branch checks neither rider role nor online status nor existing rider emptiness. Admin/dispatcher updates are unrestricted. Customer creation checks only ownership and number of fields, not price, paid state, status, rider assignment or service area.
- **Consequence:** a different UI, stale client or crafted request can bypass the orderly stage picker entirely. Customers can modify the status or financial inputs that backend settlement later trusts. Merely adding a better admin modal cannot create a reliable delivery system.
- **Remedy:** clients submit intent to authenticated server commands; clients cannot directly mutate assignment, status, financials or proof fields. Authorize role, ownership, current stage, rider availability, evidence and expected version together.
- **Acceptance:** ordinary customers cannot self-mark delivered or change rider/fee; an unapproved account cannot claim a request; a rider cannot edit another order or skip mandatory stages. Exercise these as rules/command tests outside the visible UI.

### B06 — P1 / Confirmed: stage vocabularies disagree across admin, rider and email automation

- **Evidence:** Android enum includes `PICKED_UP` and `ARRIVED` (`Models.kt:8`). Web steps at `AdminDashboard.tsx:155` omit both. Its transition map at `1869-1879` uses `PENDING → ASSIGNED → TRANSIT → OUT_FOR_DELIVERY → DELIVERED`; this map has no outgoing action for Android's `PICKED_UP`/`ARRIVED`. Email automation watches `IN_TRANSIT` at `functions/src/index.ts:791`, which is not an Android enum value.
- **Consequence:** a valid rider stage can look unsupported to admin. “Arrived” does not say pickup or drop-off. Email delivery progression does not match actual status writes. A customer timeline may not explain where their item physically is.
- **Remedy:** version one shared contract, ideally with pickup and drop-off stages separated. Keep public wording separate from machine codes. Migrate old values and test all clients before introducing new queue/preassignment values.
- **Acceptance:** advance one order through the approved lifecycle from rider, then view it in admin, customer tracking and public tracking; each stage has a consistent meaning and a legal next action.

### B07 — P1 / Confirmed: existing admin guards are partial and must be acknowledged accurately

- **Evidence:** `AdminDashboard.tsx:1882-1897` already prevents normal status actions into `ASSIGNED`, `TRANSIT`, or `OUT_FOR_DELIVERY` without a rider and validates the transition map. `DELIVERED` is absent from `requiresRider`, and the actual confirmation write at `1903-1908` does not re-read the order or enforce a revision. Rider helper `FirebaseManager.kt:1623-1630` writes any requested enum value without checking current state.
- **Consequence:** the user's concern about status without rider remains a system integrity concern, but “there is no guard at all” would be inaccurate for the current web picker. Legacy inconsistent records and alternate paths remain unsafe.
- **Remedy/acceptance:** enforce the complete guard server-side; final delivery must require actual rider ownership and evidence, including when an order arrived in a malformed legacy state.

### B08 — P1 / Gap: no preassignment acceptance, expiry, decline, promotion or release protocol

- **Evidence:** no queue/preassignment fields or states in `Models.kt:7-44`; assignment function at `functions/src/index.ts:121-143` immediately marks `ASSIGNED` yet the push tells the rider to open the app to accept. There is no offer expiry/acceptedAt path in this backend assignment mechanism.
- **Consequence:** “assigned” may mean system selection, dispatcher intention or rider commitment. The system cannot honestly promise the named rider will handle the next order, nor automatically release a reservation when that rider goes offline.
- **Remedy:** represent `assignmentState` separately from fulfilment stage: offered, accepted, reserved-next, active, released. Record busy reason, estimated release time, queue position, acceptance deadline and reassignment reason. Customer copy must distinguish waiting for availability from rider en route.
- **Acceptance:** reserve a second mission to a busy rider; customer sees verified rider identity and “scheduled after current delivery”; completion promotes it once; decline/offline/timeout returns it to queue and explains the change.

## Money, checkout and completion

### B09 — P0 / Confirmed: normal Paystack wallet funding bypasses the server verifier

- **Evidence:** `WalletViewModel.kt:23-44` calls client `updateUserWalletBalance` and `FirebaseManager.kt:423-432` directly raises `users.walletBalance`. The repository search found no Android invocation of `verifyPaymentAndTopUp`. Normal funding callbacks such as `ProfileScreens.kt:1476` and `BookingScreens.kt:1543` call `topUpWallet(amount)` without a gateway reference. Rules `firestore.rules:86-108` deny ordinary positive wallet updates.
- **Consequence:** under these rules, a customer may successfully complete the payment page but the ensuing credit operation is rejected and summarized as a network/retry problem. The real verifier exists but is disconnected from the visible funding flow. A retry without reference reconciliation cannot safely determine whether money was already taken.
- **Remedy:** pass the actual gateway reference to a single callable; persist a pending attempt before payment; verify server-side and reconcile on callback, retry and app resume. Clearly show “Payment received, wallet credit pending” rather than asking the customer to pay again.
- **Acceptance:** successful charge plus temporary app/network loss eventually produces one wallet credit for that reference, and the customer can resume without a second charge.

### B10 — P0 / Confirmed: the server verifier checks success but trusts the caller's amount and identity

- **Evidence:** `functions/src/index.ts:289-297` accepts `amount` and `reference`. At `317-319` it checks provider success only. At `345-357` it credits caller-supplied `amount` into the caller UID. No check binds provider amount in minor units, currency, customer or metadata to the request. Mock references are excluded only when `NODE_ENV === 'production'` (`302-305`), not by an emulator-only gate.
- **Consequence:** gateway success is not proof that the claimed amount belongs to this wallet. A transaction reference uniqueness check prevents simple replay but does not make the first claim correct. Configuration-dependent mock behavior remains an environment risk.
- **Existing control:** authenticated callers, missing-secret refusal for non-mock references, provider success check and an atomic globally unique reference ledger at `329-376` are valuable.
- **Remedy:** derive credit amount and currency from the provider, validate an immutable payment intent bound to UID, and remove mock acceptance from deployable live paths. Reject nonfinite, malformed, mismatched or unsupported payment results.
- **Acceptance:** a successful reference for another UID or a different amount/currency cannot credit this wallet; repeated same-user verification returns the existing credited result instead of encouraging another payment.

### B11 — P0 / Conditional: completed-delivery settlement is not idempotent or atomic

- **Evidence:** `functions/src/index.ts:182-246` increments vendor and rider balances whenever the event's new status is `DELIVERED`. It never checks an immutable settlement record, `settledAt` or `payoutCredited`. Vendor updates at `194-200` catch errors individually and then mark the marketplace order `SETTLED` at `204-206`; rider credit and ledger write at `219-239` are separate operations.
- **Consequence:** duplicate events, status rewinds/re-completion or partial retries can pay more than once. Failed vendor credits can still leave a settled order. The fixed ledger ID `EARN-{deliveryId}` can be overwritten while the balance is incremented repeatedly, hiding duplicates in history.
- **Platform evidence:** Firebase documents both unordered events and at-least-once delivery, so repeat processing is a normal design condition, not merely hypothetical abuse. [Firebase Firestore trigger limitations](https://firebase.google.com/docs/functions/firestore-events).
- **Remedy:** deterministic settlement ID, transactionally guarded state, separate immutable debit/credit postings and an explicit retryable failure state. Mark settled only when the required postings are durable and reconciled.
- **Acceptance:** deliver the same event twice and concurrently; balances change once. Inject failure at each posting boundary; the system displays settlement pending/failed until reconciled, never false success.

### B12 — P0 / Confirmed: settlement trusts customer-controlled commercial amounts

- **Evidence:** `functions/src/index.ts:189-200` pays `vendorSplits` from a customer-created marketplace order; `213-215` uses delivery fee/tip fields from the delivery. `firestore.rules:178`, `183`, `396` allow customer document creation or owner delivery edits without financial validation. Store owners also have unrestricted store-field updates at `389-390`.
- **Consequence:** delivery completion can convert unverified client values into trusted server balance credits. Company, vendor and rider earnings cannot be treated as authoritative accounting.
- **Remedy:** server-priced order snapshots, payment reservations, controlled commission plans and financial fields that clients cannot change. Derive splits from trusted catalog/store configuration and persist them before fulfilment.
- **Acceptance:** modified client price, tip, vendor payout, store balance or commission values do not affect settled money; the server returns a clear quote mismatch.

### B13 — P1 / Confirmed: ordinary rider OTP completion contains a transaction failure and a permission failure

- **Evidence:** `FirebaseManager.kt:1777-1789` starts writes to the delivery, then reads the rider profile inside that transaction. `1793` attempts to increase the rider's own wallet, denied by `firestore.rules:86-108`. A server OTP callable exists at `functions/src/index.ts:397`, but this mobile handler verifies locally instead.
- **Consequence:** with a normal unpaid positive-price delivery, entering the correct code can fail to finish the delivery. “OTP valid” and “order completed” are not reliably connected. The main handover flow is unsafe to call complete until tested on a real rider account under production-equivalent rules.
- **Technical evidence:** Firestore requires reads before writes. [Firebase transaction rules](https://firebase.google.com/docs/firestore/manage-data/transactions).
- **Remedy:** rider invokes a server completion command; server validates OTP, ownership, current stage and idempotency; settlement happens once through B11's design.
- **Acceptance:** correct code as assigned rider completes a real positive-price order; wrong actor, wrong stage, wrong code, duplicate request and offline retry behave predictably.

### B14 — P0 / Confirmed: mobile and server disagree on the rider's earnings

- **Evidence:** mobile OTP completion computes `price * 0.80` at `FirebaseManager.kt:1771-1775`; backend computes `deliveryFee * 0.70 + tipAmount` at `functions/src/index.ts:213-215`, using default fees of 1500 or 2500 when missing. The ordinary parcel serializer writes `price` but no `deliveryFee`/type at `FirebaseManager.kt:494-526`. The server does not honor the mobile `payoutCredited` field.
- **Consequence:** the same order has two earnings formulas, and ordinary deliveries may use a fallback unrelated to the amount quoted. If permissions are loosened just to make the mobile flow pass, both mobile and backend can pay independently. Marketplace delivery `price` includes merchandise value (`DeliveryViewModel.kt:6107`), which makes using total price as rider pay especially wrong.
- **Remedy:** one server-controlled earning contract with delivery subtotal, rider share, tip, commission, tax/discount policy where applicable, and immutable payout snapshot. Present the expected earning before acceptance.
- **Acceptance:** standard, express, discounted, marketplace and tipped orders each reconcile to one agreed formula and one ledger posting.

### B15 — P1 / Confirmed: marketplace checkout violates transaction ordering before it reaches completion

- **Evidence:** `DeliveryViewModel.kt:5989-6007` reads each product and immediately writes stock. The next cart item is then a read after a write; even with one product, wallet read at `6012` comes after stock update. Store read at `6035` also occurs after writes.
- **Consequence:** ordinary multi-product and wallet checkouts can fail atomically. The UI's stock revalidation and wallet checks are useful, but they do not repair an invalid transaction.
- **Remedy:** move the purchase to a server command; load all authoritative documents first, then perform validated writes. Do not patch only the first failing read and leave later store reads invalid.
- **Acceptance:** buy two products from one vendor and from two vendors using wallet, card and supported alternatives; verify stock, wallet, order, fulfilment and ledger consistency. Firebase's read-before-write rule is linked in B13.

### B16 — P1 / Confirmed: even after transaction repair, checkout attempts a vendor mutation forbidden to customers

- **Evidence:** checkout increments `marketplace_stores.vendorWallet`, sales and commission at `DeliveryViewModel.kt:6034-6044`. Store rules permit only admins or the store owner (`firestore.rules:389-390`). Ordinary customers cannot perform that write. Checkout creates a `PAID` order regardless of the payment method (`6057-6058`), and only the literal `Wallet` path checks/debits payment (`5898`, `5975-5984`, `6009-6026`).
- **Consequence:** a transaction-order repair alone still leaves purchases failing under normal rules. Making store writes public would create a financial vulnerability. Non-wallet orders also lack a server-confirmed paid state in this mechanism.
- **Remedy:** reserve/capture payment server-side, validate stock, keep vendor funds pending until the chosen release policy, and use independent payment and fulfilment states.
- **Acceptance:** ordinary customer permissions support purchase without permitting direct vendor balance edits; unverified card/bank/COD orders never appear as confirmed paid.

### B17 — P1 / Confirmed: marketplace fulfilment is under-specified for real pickup and handover

- **Evidence:** `DeliveryViewModel.kt:6093-6120` creates one dispatch record using only the first vendor name, a generic “Vendor Fulfilment Point,” dummy sender phone `08000000000`, fixed weight `2.5`, no coordinates and empty `otpCode`. The cart supports multiple vendor splits. `marketplace_orders` read rules at `firestore.rules:395` check only top-level first `vendorId`/`storeId`.
- **Consequence:** rider has no reliable pickup address/contact, multi-vendor carts are reduced to one pickup, additional vendors may not be authorized to read the order, and normal OTP completion rejects the empty code. Merchandise, delivery and store preparation are collapsed into one unsupported journey.
- **Remedy:** vendor-specific fulfilment suborders with exact pickup details, preparation acknowledgment, inventory reservation and independent pickup proof; a parent order aggregates status. Generate delivery proof only at the correct handover boundary.
- **Acceptance:** order from two Benin stores; each vendor sees only its items, the rider sees both real pickups and contacts, and customer delivery proof works once the whole appropriate consignment reaches the recipient.

### B18 — P0 / Confirmed: vendor payout approval does not execute or confirm a bank transfer

- **Evidence:** web approve at `AdminDashboard.tsx:4151-4156` only sets request `APPROVED`. Backend `processVendorPayout` at `functions/src/index.ts:467-486` likewise changes status and writes a ledger entry labeled `PAYSTACK_TRANSFER` / `COMPLETED` without invoking a transfer API or storing a provider transfer result.
- **Consequence:** approval is not payment. The vendor can be shown a reassuring state while money has not left the company bank. The server ledger can incorrectly claim a completed external transfer. A manual transfer process may exist outside this source, but the app does not capture or verify it.
- **Remedy:** separate review approval, transfer submitted, provider pending, paid, failed and reversed. For manual payouts require an external payment reference, actor and evidence; for automated payouts reconcile provider callbacks.
- **Acceptance:** approving alone does not show paid. A provider failure reopens an actionable task; success records the bank reference and reconciles the exact amount once.

### B19 — P0 / Conditional: payout reservation and rejection can misstate or duplicate vendor funds

- **Evidence:** `DeliveryViewModel.kt:5663-5665` validates cached vendor balance outside the transaction; `5686-5691` decrements without reading live balance and reports success before awaiting the transaction. Web rejection `AdminDashboard.tsx:4166-4168` changes status before refunding, with no transaction/current-state check. Backend rejection `functions/src/index.ts:452-495` reads pending outside a transaction and refunds before marking rejected.
- **Consequence:** concurrent requests can over-reserve; a failed async reservation can still be announced as submitted; two rejection actions can both return funds; a failure between status and refund can leave the vendor short.
- **Remedy:** server reservation with live balance, nonnegative invariant and idempotency key; transactionally guard each rejection; keep reserved and available balances separate.
- **Acceptance:** double-click request, two devices and two simultaneous rejecting admins never create excess reservations or duplicate refunds. Failure must leave a visible recoverable state.

### B20 — P0 / Confirmed: store financials and approval badges are editable by the store owner

- **Evidence:** `firestore.rules:387-390` checks ownership but has no restricted-field list. The same document contains `vendorBalance`, `vendorWallet`, commission and verification/approval fields populated in admin store handlers. Product stock-only updates are permitted to any authenticated user without a lower/upper bound or purchase linkage at `379`.
- **Consequence:** a badge or balance in the interface is not necessarily company verified. Customers can alter inventory independently of a purchase. Tightening these rules without moving business operations server-side would also expose the incompatible checkout path described above.
- **Remedy:** split public vendor-editable content from server financial/KYC/commission records; allow stock changes only through validated inventory or purchase commands.
- **Acceptance:** a vendor may edit approved descriptive fields but cannot approve itself or raise balances; a customer cannot independently change stock.

### B21 — P0 / Confirmed: financial protection has creation and welcome-gift holes

- **Evidence:** user creation at `firestore.rules:82-84` checks role and payload size but not wallet or loyalty initial values. Updates that change only nonfinancial fields are allowed at `89`; `welcomeGiftClaimed` is not immutable. The gift branch at `92-95` trusts that flag and absolute balances.
- **Consequence:** the intended “cannot arbitrarily increase balances” control does not establish a trustworthy opening balance or one-time entitlement. A claim marker editable by the beneficiary cannot be a durable anti-repeat control.
- **Remedy:** server provisions financial opening state and claims a dedicated immutable gift entitlement transactionally. Profile writes must have a public-field allowlist and cannot modify internal flags or approval state.
- **Acceptance:** arbitrary starting wallet/points values are rejected; a gift can be redeemed once across reinstalls/devices and after ordinary spending.

### B22 — P1 / Confirmed: ratings, tips and loyalty completion are not reliable cross-user operations

- **Evidence:** `FirebaseManager.kt:1884-1905` writes before reading in the loyalty completion transaction and then increases another user's points, prohibited for normal rider callers by profile rules. `rateAndTipRider` at `1947-1977` also writes before later reads; it attempts rider-wallet updates a customer cannot authorize. Its code skips a customer deduction on insufficient funds (`1958-1961`) while retaining the rider-credit branch. Profile rules `108` allow any authenticated caller to change any profile's rating/ratingCount without validating delivery participation or range.
- **Consequence:** rewarding a real completed delivery, tipping and rating can fail or be inaccurate. A high star value need not prove verified feedback. Loosening rules to fix denied customer tips would permit fabricated money.
- **Remedy:** one server command per rating/tip using order ownership, completed status, one-rating invariant, amount limits and atomic transfer. Recompute aggregate rating from verified ratings. Keep loyalty entitlements server-issued.
- **Acceptance:** zero-tip rating works; insufficient tip balance fails without a credit; duplicate tips/rating retries do not duplicate; unrelated users cannot alter rider rating summaries.

### B23 — P1 / Gap: cancellation is not a money and inventory recovery workflow

- **Evidence:** `functions/src/index.ts:176-247` has financial effects for delivered only, no cancellation refund, inventory release, rider reservation release, cancellation-fee policy or return-to-sender settlement. Web cancellation merely writes a stage and sends a message (`AdminDashboard.tsx:1908-1918`).
- **Consequence:** “Cancelled” tells a customer nothing about money, parcel possession or refund timing. Administrators must invent the recovery steps and remember to perform them elsewhere.
- **Remedy:** define cancellation by physical stage and payment state; model customer-requested cancellation, operator approval, refund pending/succeeded/failed, rider fee if applicable, stock release and return job where an item is already collected.
- **Acceptance:** cancel before rider selection, after collection and after partial multi-vendor preparation; each produces an auditable, correct recovery path without manually editing wallet totals.

## Privacy, identity and permissions

### B24 — P0 / Confirmed: public tracking exposes the complete delivery collection

- **Evidence:** `firestore.rules:177` grants `read` universally, including both individual reads and listing. The parcel document contains names, phone numbers, physical addresses, customer/rider IDs, GPS, price and plain handover code (`FirebaseManager.kt:494-526`). Chats under every delivery are also public at `190`.
- **Consequence:** public tracking is implemented as public private-data storage, not a deliberately limited tracking view. Knowing a specific link is not the only allowed route to this data under the source rules. The exposure includes the handover secret used to prove receipt.
- **Remedy:** private delivery record; limited opaque-token public projection without personal contacts, exact private destination, OTP, financial fields or unrestricted collection listing. Revoke/expire tracking tokens when appropriate and define recipient access intentionally.
- **Acceptance:** unauthenticated collection queries fail; a valid tracking token exposes only approved display fields; a customer sees only its own private data and permitted rider identity.

### B25 — P0 / Confirmed: proof of delivery is not protected by actor ownership

- **Evidence:** `functions/src/index.ts:397-425` checks authentication and OTP equality but does not require the assigned rider, correct stage, expiry, attempt limit, possession proof or already-completed guard. OTP is read from the public delivery record. `storage.rules:20-23` permits any authenticated user to read or write files at any `pod/{deliveryId}/{file}` path (subject only to size for write).
- **Consequence:** “OTP verified” and an uploaded image are not strong evidence of actual handover. Evidence can be unrelated to the assigned courier, and storage permissions permit cross-order overwrites.
- **Remedy:** private server-stored code hash, narrow rider-only handover command, fresh recipient authorization, bounded attempts, expiry and consumed-state check; immutable proof metadata binds file hash, order, stage, actor, time and location. Make proof read access participant-specific.
- **Acceptance:** unrelated authenticated users cannot view/write proof or verify someone else's order; duplicate completion is safe; expired/locked codes show a recovery process supervised by an authorized dispatcher.

### B26 — P1 / Confirmed: account deactivation is a display flag, not revocation

- **Evidence:** `AdminDashboard.tsx:1390` sets only `isDeleted` and `updatedAt`; dialog text `1641` promises the user can no longer log in or transact. Auth/rule helpers do not check `isDeleted` or suspended status, and auto-dispatch only checks role/isOnline. Profile owners can update nonfinancial flags without an allowlist (`firestore.rules:86-108`).
- **Consequence:** a removed/suspended account may still hold a valid authentication session and data rights; a removed online rider remains eligible to the backend query. The operator thinks access was removed when the trust boundary says otherwise.
- **Remedy:** server-admin account lifecycle command disables account or revokes appropriate sessions, enforces active status in authorization, removes assignment eligibility and resolves active/reserved work. Show the exact administrative state and partial failures.
- **Acceptance:** deactivate a logged-in rider on another device; it cannot accept new work or issue sensitive commands, and existing parcels enter a reassignment process.

### B27 — P0 / Confirmed: dispatcher permissions are simultaneously too broad and accidentally broken

- **Evidence:** `firestore.rules:82-84` lets dispatchers create user documents without role restriction. Conversely their update branch at `107` checks all keys of the resultant document, not changed fields, so any normal document containing a role is rejected for their intended non-role update. `sub_admins` records at `248-250` are admin-owned, but no permission lookup appears in rules helpers; role alone grants admin powers.
- **Consequence:** a supposedly limited dispatcher can create overprivileged records, while legitimate edits fail. A “view only” or “content manager” label does not enforce least privilege unless the actual command/rule checks it.
- **Remedy:** server-provision roles, separate capabilities and scoped changes; rules use changed-field allowlists where appropriate. Role assignment cannot be granted through generic document creation.
- **Acceptance:** test customer, rider, dispatcher, report-only, content manager, admin and super-admin against every privileged action; UI availability and server decisions must agree.

### B28 — P1 / Confirmed: activity logs are client-authored and editable by the people audited

- **Evidence:** web `addLog` at `AdminDashboard.tsx:967-972` executes independently of the business operation, catches write failure and still prepends a local log. `firestore.rules:317-320` permits admins to create, update and delete audit records. Rider/dispatcher operations lack a corresponding server-owned event journal in the inspected status functions.
- **Consequence:** an operator may see a log that never reached the server. A privileged actor can remove or rewrite the history of a sensitive action. Missing logs do not mean no action occurred, and visible logs are not complete evidence.
- **Remedy:** produce immutable audit events from authenticated server commands, binding actor ID/capability, target, before/after version, reason, timestamp, correlation ID and outcome. Retain client activity only as an auxiliary UI feed.
- **Acceptance:** intentionally fail log persistence; the system cannot present an unaudited sensitive action as fully complete. No application role can alter existing audit events.

### B29 — P1 / Confirmed: fleet and support privacy are broader than the product promise

- **Evidence:** comments say GPS is private to rider/admin/dispatcher, but `firestore.rules:165-166` permits all authenticated accounts to read all fleet locations. Support ticket updates allow the new `userId` to be the caller at `330`, rather than preserving ownership. A support message can be created in an arbitrary ticket merely by having caller `senderId` at `343`; membership is not required in that branch.
- **Consequence:** unrelated users can inspect fleet positions and inject support messages into conversations they do not own; ticket ownership is not immutable. Busy support staff must distinguish genuine customer follow-up from unrelated injected activity.
- **Remedy:** participant membership checks, immutable ticket owner and authenticated sender binding; give customers only their active courier's permitted tracking data, with lifecycle limits.
- **Acceptance:** two unrelated customer accounts cannot read each other's private support threads or post into them; a customer cannot query the general fleet.

## Notifications and operational truth

### B30 — P1 / Confirmed: notifications have incompatible writers, duplicate fan-out and wrong audience semantics

- **Evidence:** web `createNotification` writes a root notification and then fans out to every active user (`AdminDashboard.tsx:939-954`); backend `onNotificationCreated` fans that same root event out again (`functions/src/index.ts:548-584`) with random child IDs. Both write `description` and `read`. Android listener consumes `message` and `isRead` only (`FirebaseManager.kt:1125-1131`). Backend ignores audience/category and queries `isDeleted == false`, which excludes records missing that field. Full 500-document batch commits at `577` are not awaited.
- **Consequence:** when both mechanisms are deployed, customers can receive duplicate title-only notifications. Operational alerts are treated like broadcasts. Some accounts receive none, and at larger fan-out sizes the function can complete before all writes are confirmed. Notification read state cannot be consistently interpreted.
- **Remedy:** one server notification service, typed audience, one shared schema and deterministic recipient+event ID; await every batch and expose delivery results. Filter operational admin events from customer channels.
- **Acceptance:** one event yields one correctly populated notification for each intended recipient; none reaches the wrong role; retry creates no duplicate; a 501-recipient test completes all intended writes.

### B31 — P1 / Confirmed: rider/customer in-app notification writes are blocked by rules

- **Evidence:** `FirebaseManager.kt:346-367` writes a user notification directly, called on rider self-claim and status updates. `firestore.rules:119` allows notification creation only for admins. Notification clearing/deleting at `FirebaseManager.kt:374-408` is also denied to owners by `firestore.rules:121`.
- **Consequence:** the rider can change a status but fail to create the in-app customer record, leaving push and inbox disagreeing. Customer “clear/delete” actions cannot be trusted to persist. The function name promises FCM but its direct role is a Firestore write followed by an in-process event.
- **Remedy:** server-derived event notification; owner may mark read/archive using an explicitly allowed minimal action. Ensure UI waits for persistence or shows a retry state.
- **Acceptance:** ordinary rider assignment creates the customer's durable inbox item; customer clear/archive stays cleared after relaunch while retaining server audit requirements.

### B32 — P1 / Confirmed: push payloads can produce duplicate and unhelpful alerts

- **Evidence:** backend status push contains notification title/body plus data `parcelId/status`, but no data title/message (`functions/src/index.ts:262-271`). Android `onMessageReceived` processes data and notification blocks separately (`MyFirebaseMessagingService.kt:39-55`), emitting generic text for missing data title/message and then another notification with actual copy but null parcel ID. Assignment push uses `deliveryId` at `functions/src/index.ts:142-143` while Android reads `parcelId`.
- **Consequence:** foreground messages can generate two different local notifications or lose a useful deep link. Rider assignment may open the wrong context or no specific order. This recreates the user's complaint about hunting for a delivery after following an alert.
- **Remedy:** one normalized message envelope and one rendering path; deep link to the exact task/order and appropriate next action. Test background, foreground and cold-start delivery separately.
- **Acceptance:** a status/assignment push shows correct text once and opens the exact parcel with correct permissions in all app lifecycle states.

### B33 — P1 / Confirmed: missing welcome token causes a personalized broadcast to everybody

- **Evidence:** `functions/src/index.ts:37-64` looks for a newly created user's token and otherwise sends the personalized welcome payload to `all_users`, including `userId` in data. User profile/token creation can happen after the Auth creation trigger.
- **Consequence:** unrelated users may see a new user's name/welcome message instead of a private onboarding notification. Missing tokens should delay delivery, not change the audience.
- **Remedy:** store a pending user-specific event, deliver once the user's device token appears, and never broaden audience as an error fallback.
- **Acceptance:** create an account before token registration; no existing customer gets its welcome, and the new account later receives one private welcome.

### B34 — P1 / Confirmed: delivery email automation expects fields the booking flow does not store

- **Evidence:** handover email requires `recipientEmail` and `deliveryCode` at `functions/src/index.ts:793`, but ordinary bookings serialize `receiverName`, `receiverPhone`, `otpCode` with no recipient email at `FirebaseManager.kt:494-526`. Trigger uses `dropoffAddress`/`recipientName` instead of delivery/receiver naming. Receipt needs `senderEmail || customerEmail` at `817` and `price`/`basePrice` at `821-835` without checking actual captured payment.
- **Consequence:** the email feature can exist in source yet never deliver a real handover code for normal app orders. If enough mismatched optional fields are supplied, it can send an incomplete or misleading receipt. A delivered parcel alone does not prove payment.
- **Remedy:** align schema and explicit notification requirements; provide actual recipient channel consent/contact; receipts come from captured payment, while handover codes come from secure proof state.
- **Acceptance:** normal Android and admin-created deliveries exercise code/invoice notifications using real supported channels; missing recipient email produces a chosen alternative, not silent success.

### B35 — P2 / Confirmed: SMTP diagnostics and caching can mislead operations

- **Evidence:** `testSmtpConnection` has no auth/admin check (`functions/src/index.ts:761-774`) and returns SMTP host/user details. Transport cache fingerprint omits password (`emails/emailTransporter.ts:61-63`), so changing only a password can reuse old credentials. TLS certificate verification is disabled at `73`. Delivery email trigger calls `sendEmail` without checking its returned success at `803-808` and logs sent even though helper catches failures and returns `{success:false}` (`emailTransporter.ts:111-113`).
- **Consequence:** an email settings change may look saved but not take effect on a warm instance; logs can say sent when transport failed; a diagnostic described as admin-only is public callable behavior.
- **Remedy:** authorize diagnostics; safely invalidate transport for credential changes; retain certificate checks; propagate delivery result into a retriable notification record.
- **Acceptance:** rotate only SMTP password, send a test with actual result, then force a delivery failure and confirm the dashboard does not show sent.

### B36 — P2 / Conditional: OTP security foundations exist but verification is not a complete privileged-action protocol

- **Evidence:** `emails/otpService.ts:39`, `50-71`, `101-129` implements cryptographic codes, expiry, a cooldown and attempt limit. However key generation strips all email punctuation to underscores (`47`, `91`), creating possible collisions; read/check/update is not transactional, allowing concurrency races in generation and consumption. Purpose is cast from input rather than runtime validated at `functions/src/index.ts:665`/`724`; public verify returns success and only signup changes an account (`737-751`).
- **Consequence:** resetting a PIN/password requires a server-bound follow-on capability; simply showing “OTP verified” is not proof that an authorized reset was completed. Concurrent verification can consume the same code more than once; unrelated normalized addresses can share a key.
- **Remedy:** validate purpose, use collision-resistant email/purpose identifiers, transactionally consume codes, and bind a short-lived one-time reset capability to the exact account and action. Keep public signup/reset initiation separate from authenticated privileged operations.
- **Acceptance:** concurrent valid verifications permit only the intended one-time action; separate punctuated email addresses never collide; an OTP for signup cannot authorize a wallet PIN change.

## Cross-app consistency and acceptance priorities

### B37 — P1 / Confirmed: full-document parcel synchronization can destroy newer state

- **Evidence:** `FirebaseManager.kt:492-530` serializes a local `Parcel` and performs `.set(parcelMap)` without merge or revision check. This map omits backend metadata such as `assignedAt`, `autoDispatched`, `deliveryFee`, `deliveredAt`, and any future queue/reservation/payment fields; it also resets `otpExpiresAt` to one hour from the write. Root-only auto-dispatch has no mirror update (`functions/src/index.ts:121-131`), while mobile history has a personal-subcollection reader (`FirebaseManager.kt:549-550`), and many mobile mirror updates are best-effort after root writes (`1572-1587`, `1631-1641`, `1817-1825`).
- **Consequence:** a later synchronization of stale local data can overwrite current status, identity or proof metadata and extend code validity. Root and per-user histories can diverge. Introducing new queue fields only in the server is particularly unsafe while replacement writes remain.
- **Remedy:** one canonical order and server-issued revisions; clients mutate narrowly scoped public fields only. Use query projections or server-maintained read models instead of client-maintained duplicate truths. Migrate serializer behavior before expanding the lifecycle.
- **Acceptance:** replay a stale client snapshot after assignment/completion; the server refuses it or ignores stale fields. Customer current/history views remain consistent after every failure boundary.

### B38 — P1 / Confirmed: identifiers and ledger schemas weaken traceability

- **Evidence:** marketplace order reference is `ORD-MKT-` plus last six digits of current milliseconds (`DeliveryViewModel.kt:5897`), repeating every 1,000,000 ms and potentially colliding for concurrent shoppers. Order/dispatch writes use `.set` at `6049-6050`/`6122`. Wallet records mix numeric timestamps, Firestore timestamps and string dates (`FirebaseManager.kt:1805-1809`; `functions/src/index.ts:237-239`); customer ledger creation checks ownership/size rather than amount/reference contract (`firestore.rules:126-129`).
- **Consequence:** unrelated orders can share an identifier and overwrite or fail authorization inconsistently; reconciliation cannot assume a stable transaction ordering or authoritative ledger contents.
- **Remedy:** collision-resistant order IDs and separate human-friendly sequence numbers; server timestamps and unified immutable ledger schema with amount in integer minor units, currency, direction, actor, external reference and source order.
- **Acceptance:** create simultaneous orders from many users and across more than one ID cycle; no collision or overwrite occurs. Admin exports reconcile to the server ledger rather than display labels.

### B39 — P1 / Gap: there is no systemic exception/recovery engine

- **Evidence:** auto-dispatch and settlement catch/log errors and return (`functions/src/index.ts:149-152`, `244-246`); status FCM failure is logged only (`275-276`). The inspected backend has no failed-job record, retry ownership, queue age escalation, payment reconciliation sweep, stuck-order monitor or dead-letter work queue.
- **Consequence:** when automatic work fails, the admin sees a static order and has to guess which invisible step failed. “Connected” real-time listeners do not tell the company whether dispatch, payment, notification or settlement is healthy.
- **Remedy:** persist domain operation attempts and actionable exceptions with stage, cause, affected order, next retry, accountable role and recovery command. Prioritize customer money and physical parcel possession over decorative summary metrics.
- **Acceptance:** inject provider downtime, unavailable rider, denied write, failed proof upload and missing store; each creates a specific visible task with safe recovery and no false success.

### B40 — P2 / Gap: no evidence that production rules/functions match these files

- **Evidence:** `firebase.json` declares source rules/functions; `functions/package.json` deploys `lib/index.js`. This review did not read live rules, deployed function versions, payment configuration or production logs and did not execute operational scenarios.
- **Consequence:** code audit establishes many concrete defects but must not invent a claim that a payment or delivery was actually lost in production. Conversely a locally fixed UI cannot be treated as proof that the live business rules changed.
- **Remedy:** maintain release evidence linking app build, web commit, functions version, rules version and migration state. Use production-equivalent staging accounts and fixture orders for the acceptance flows above before owner deployment.
- **Acceptance:** the release checklist identifies exact versions and proves a paid Benin booking, queued dispatch, rider preassignment/activation, pickup, arrival, secure delivery, one settlement, customer receipt and auditable cancellation/refund.

## Minimum target operating model

Do not implement the proposed flow as a longer string dropdown. Separate four concerns: payment state, dispatch allocation, physical fulfilment and exception/recovery state. The same delivery can be payment-confirmed, queued, not-yet-collected and awaiting rider capacity without pretending one status word describes everything.

Recommended lifecycle: receive and validate a Benin booking; reserve/confirm payment under the selected method; acknowledge request; queue by eligible receipt time; offer to a suitable available rider or reserve for the rider's next slot; record acceptance; activate when their current mission ends; navigate to pickup; arrive at pickup; confirm actual collection; travel to recipient; arrive at drop-off; capture valid handover proof; finish fulfilment; settle money once; close with receipt and optional verified rating/tip. Cancellation, recipient unavailable, damaged item, wrong address, rider offline, rejected assignment, disputed delivery and return-to-sender are first-class branches, not manual editing of the happy-path status.

Every dispatch workspace needs to show the next responsible person, current stage age, pickup/destination, verified contacts, item/handling details, payment state, active/reserved rider with truthful profile and vehicle, GPS freshness, reason for queue/exception, and one explicit legal next action. The details should be available in the order context without hunting elsewhere. A customer-facing state must say what was received, what is happening, why they are waiting and what they can do next.

Repair sequence: first contain public/private-data and financial write boundaries; then wire one server command path for payment and fulfilment; repair atomicity and idempotency; migrate one shared schema; implement queue/reservation/exception handling; unify notifications and proof; then simplify the admin, rider and customer UI around the same truthful state. Do not fix permission failures by opening more writes to clients.
