# Rider / driver and dispatch handoff audit notes

Audit date: 9 September 2026. Scope: current working-tree source, including existing uncommitted edits. This is a read-only source audit, not a claim that the current build was installed, exercised, or that these rules/functions are deployed. No production data was changed. No app code was edited. The existing system-audit document was not used as evidence.

The root and mobile/app AGENTS.md were read. Root branding instructions supplied in this task retain ESDispatch; the nested file still calls the brand ENGRACED DISPATCH and says employee fleet rather than gig work. That documentation conflict itself deserves cleanup, but branding changes are outside this audit. The user operates only in Benin City.

## Executive finding

The rider app has enough screens to look like an operations system, but the central delivery contract is not reliable. Status, route position, rider availability, payment, proof, shift records and customer-facing promises are updated by different paths with different rules. A driver can see a fleet dashboard full of formal terminology while critical features are local-only or simulated. The strongest example is a control labelled START LIVE GPS TRANSMISSION that starts a route simulation writing invented movement and status changes to the delivery. It can run concurrently with real hardware GPS.

This is not principally an animation problem. The first priority is one server-enforced lifecycle, one assignment/capacity model, genuine location telemetry, and an idempotent completion/settlement command. Only then should the dashboard be simplified around the current job and next action.

### Source key

All paths below are relative to `D:\Eng App`, with exact current line numbers:

- **Rider UI**: `mobile/app/src/main/java/com/esdispatch/ui/screens/RiderScreens.kt`
- **VM**: `mobile/app/src/main/java/com/esdispatch/viewmodel/DeliveryViewModel.kt`
- **Firebase**: `mobile/app/src/main/java/com/esdispatch/data/FirebaseManager.kt`
- **Model**: `mobile/app/src/main/java/com/esdispatch/data/Models.kt`
- **POD UI**: `mobile/app/src/main/java/com/esdispatch/ui/screens/ProofOfDeliveryScreen.kt`
- **GPS service**: `mobile/app/src/main/java/com/esdispatch/util/LocationService.kt`
- **Local DB**: `mobile/app/src/main/java/com/esdispatch/data/LocalDatabase.kt`
- **Functions**: `functions/src/index.ts`
- **Rules**: `firestore.rules`

Severity: P0 = integrity/security or false operational truth; P1 = failed core dispatch/delivery; P2 = usability/reporting/supporting workflow. Confirmed means the source directly demonstrates the behavior. Risk means a likely consequence needing isolated runtime validation. Missing means a required operational flow has no implementation in the traced path, rather than claiming every possible file was exhaustively disproved.

## What is already present and should be preserved

1. Real Firestore listeners exist for pending deliveries and rider assignments (Firebase:1329,1432), so the app is not wholly disconnected or static.
2. Rider self-acceptance uses a transaction, rereads the order, rejects a non-PENDING order, and checks the rider's online value (Firebase:1546–1557). This provides some protection against two compliant rider clients claiming one order. It does not reserve rider capacity or secure the rule boundary.
3. The current rider waybill no longer prints the OTP: it says ENTER UPON HANDOVER (Rider UI:2862–2875). Do not report that screen as visibly exposing the OTP. The underlying document and rider model still expose it.
4. Status cards contain pickup, destination, recipient and a context-dependent action, and the update sheet supports chat (Rider UI:1022–1114,1172–1203). The issue is incomplete and inconsistent task context, not an absence of every action.
5. A real fused-location foreground service exists (GPS service:25–78), alongside the problematic simulator. A real 50m distance calculation also exists (VM:5346–5359). Claims that all GPS functionality is fake would be inaccurate.
6. Photo and signature capture/upload paths exist (POD UI:105–178); the defect is the ordering, trust model and failure handling.
7. Driver tips can be summed from delivered assignments (VM:859–861), and a completed filter exists (Rider UI:111). This is a foundation for a meaningful completed-jobs view, not yet the promised complete earnings history.

## Detailed findings

### R01 — P0 — A production-facing GPS control transmits simulated journeys as real movement

**Confirmed source defect.** The rider status sheet includes `GpsMovementSimulator` for picked-up TRANSIT and OUT_FOR_DELIVERY jobs (Rider UI:1209–1216). Its button is labelled START LIVE GPS TRANSMISSION (1893). It fetches route waypoints, falls back to a straight interpolated route, then writes every waypoint through `updateCourierLocationByRider` (1820–1847). At the same time, if permission exists, it starts the real hardware tracker (1804–1817). It also overwrites status and progress at each step (1865–1872). Simulation speed can be 1,2,5,10 (1507).

**User/company consequence:** a stationary rider can appear to move to the customer, two coordinate producers can fight over the same document, arrival can be announced from invented coordinates, and dispatch can mistake artificial activity for actual progress. A map that moves attractively but does not tell the truth is worse than an honest tracking-unavailable state. Repeated calls to the status updater also emit notifications (Firebase:1642–1655), even when the human did not change an operational milestone.

**Fix:** remove all simulated coordinate/status writes from production journeys. Keep any simulation only in a separate debug/demo data environment with an unmistakable label and no production credentials. Hardware telemetry must never directly advance pickup/delivery milestones.

**Acceptance:** stationary device remains stationary for 10 minutes; no permission produces a clear GPS-unavailable state and no coordinate writes; route preview never changes rider location; one physical movement produces a monotonic timestamped stream; status notifications occur once per accepted milestone.

### R02 — P0 — Unknown addresses become invented geographic coordinates

**Confirmed.** Rider UI:1435–1457 maps broad text matches to fixed landmark coordinates. Any unrecognized address is converted to a coordinate with `address.hashCode()`. Any address containing “benin” is collapsed to a generic city point (1451). These coordinates feed distance, route simulation and the 50m arrived prompt.

**Consequence:** two different houses can appear at the same landmark; invalid addresses acquire misleading precision; an arrival alert can refer to a guessed location. Benin delivery needs confirmed pickup/destination pins, landmark and access notes, not a coordinate synthesized from spelling.

**Fix:** persist confirmed coordinates at booking with source/accuracy metadata; geocode only as a suggestion; require pin confirmation or an operator address-resolution task when unresolved.

**Acceptance:** an unknown address remains unresolved; no hash or generic city fallback can authorize proximity arrival; editing the address requires reconfirming the destination pin; the order retains landmark, entrance, phone and instructions.

### R03 — P1 — Several dispatch decisions are still based on Lagos

**Confirmed.** `checkGeofenceBreach` accepts longitude 3.10–3.80 (VM:5364), while the app's Benin references use longitude around 5.60 (Rider UI:1441–1451). The displayed telemetry form defaults to 6.45,3.42 and calls the checker with the fake rider name Fleet Rider Alpha (Rider UI:2340–2341,2377–2379). Batch optimization begins at 6.454070,3.394670 (VM:5300–5301). Auto-dispatch defaults missing pickup/rider coordinates to Lagos 6.5244,3.3792 (Functions:100–109).

**Consequence:** legitimate Benin activity is classified outside the operational perimeter; routes and nearest-rider decisions can be wrong by hundreds of kilometres. A Lagos example is not a harmless placeholder when used in dispatch logic.

**Fix:** owner-approved Benin operational polygon, explicit service exclusions, one shared coordinate validation service, actual rider/start-depot location, and blocked decisions when coordinates are unknown. Do not infer the exact city boundary from this audit.

**Acceptance:** a verified Benin pickup/delivery inside the service zone passes all three surfaces; a deliberately outside-zone address is consistently rejected/reviewed; missing coordinates never quietly become Lagos; dispatcher sees why a match is unavailable.

### R04 — P0 — Status permission is almost unrestricted once someone owns or claims an order

**Confirmed.** Firebase:1615–1659 accepts any supplied `ParcelStatus` and progress and writes it without reading the current status inside the transaction, validating predecessor state, assignment, pickup evidence, OTP or actor role. Rules:182–186 allow a dispatcher/admin, current rider, current customer, or any authenticated user making a PENDING→ASSIGNED claim to update a delivery. The claim branch does not check that the caller is actually a rider, online, approved or available. Owner/rider writes are not restricted to an allowed field list.

**Consequence:** screen sequencing is cosmetic. A modified client or different app surface can skip pickup, deliver without assigned rider, revert delivered orders, change fields unrelated to their responsibility, or claim company dispatch as a customer. Real company policy must survive direct API calls.

**Fix:** server command such as advanceDelivery with authenticated role, assigned actor, expected version, allowed transition, evidence and reason validation. Customers can request cancellation or correct allowed fields through separate commands; they cannot directly mutate dispatch truth. Direct financial/security/status fields must be server-owned.

**Acceptance:** customer cannot claim a shipment; unassigned rider cannot advance another job; ASSIGNED→DELIVERED is rejected; delivered cannot go backwards; dispatcher override requires permission and reason; same request twice yields one event.

### R05 — P0 — OTP is readable by the same party it is supposed to verify, and by the public delivery read rule

**Confirmed.** Parcel stores `otpCode` (Model:40). Pending and rider-assignment listeners load it (Firebase:1375/1408,1478/1511). The delivery rule is `allow read: if true` (Rules:175–177), which is a collection document rule, not a restriction to a safe public projection or individual tracking lookup. It exposes addresses, phones and OTP together. Hiding the value in the waybill UI does not protect the data.

**Consequence:** OTP does not prove that the recipient shared a secret at handover. A rider or other reader can retrieve it, and a malicious caller can combine it with permissive status writes.

**Fix:** move handover verification secrets to server-only storage; expose only delivery details necessary to the viewer; public tracking uses a tokenized, redacted projection. Use a server-side challenge/verification flow bound to delivery, assignee, stage, expiry and attempt counter.

**Acceptance:** unauthenticated reads cannot enumerate deliveries/PII; rider JSON contains no OTP; recipient sees the challenge through an authorized channel; reading all data permitted to a rider still does not reveal the secret.

### R06 — P1 — Two incompatible OTP implementations create an unreliable and bypassable completion path

**Confirmed source defects; paid-order runtime rejection should be reproduced in isolation.** Rider UI calls the Android verifier (Rider UI:1403; VM:1088–1093). Android reads OTP, expiry, attempts and `payoutCredited` outside the transaction (Firebase:1760–1775). Within the transaction it writes delivery fields before reading rider data (1778–1789), a transaction-ordering defect for the positive-payout branch. Rules also reject ordinary rider wallet increases (Rules:86–102). The callable server verifier exists but the traced rider path does not invoke it. That callable authenticates any user, compares OTP, and completes the order without assigned-rider, current-status, expiry or retry checks (Functions:397–428).

**Consequence:** valid deliveries can fail at handover because completion mixes operational state and unauthorized wallet writes; another entry point can bypass protections that only exist in Android. Lockout counters and payout flags are also stale under concurrent requests.

**Fix:** retire duplicate verifiers; use one server transaction reading all relevant records before writing; derive payout later from an accepted completion event. Verify assignee, permissible stage, expiry and idempotency on the server.

**Acceptance:** normal paid delivery completes from a rider account under actual rules; arbitrary authenticated customer cannot invoke completion; two simultaneous submissions produce one completion; expired/locked OTP behaves identically across every client.

### R07 — P1 — OTP expires before the operational handover and “recipient notified” does not prove code delivery

**Confirmed.** Android booking creates a random four-digit code (VM:4068); Firebase save sets one-hour expiry at initial creation (Firebase:519–520). Changing OUT_FOR_DELIVERY only writes status/progress and sends a generic status message (Firebase:1628–1655). Rider UI claims this action sends the secure code to the recipient (1327), then reports Recipient notified on status-write success (1342). The actual helper routes notification to `parcelUserId`, which may be the sender/booker, not the recipient phone/account. No code refresh/reissue path was found in the traced rider and status-update path.

**Consequence:** queue time consumes the OTP lifetime before pickup; the recipient may never receive the code, and the rider is stranded at the door. Toast wording overstates what the server has acknowledged.

**Fix:** generate/refresh the handover challenge near a suitable stage; explicitly model booker versus recipient; show delivery channel and confirmation separately from status success; build dispatcher reissue, alternative handover and lockout recovery with audit.

**Acceptance:** order queued over one hour still has a usable handover process; receiver without an app has an approved reachable channel; failed notification is visible; reissue invalidates the prior code; rider sees recoverable instructions.

### R08 — P0 — Delivery is completed before proof, and even failed proof upload completes it again

**Confirmed.** OTP success first marks DELIVERED (Firebase:1778) and then navigates to ProofOfDelivery (Rider UI:1406–1408). POD upload invokes `markParcelDelivered` on URL retrieval failure, upload failure and catch (VM:1011–1024), and on nominal success before awaiting the delivery document stamp (998–1010). Bitmap decode failure also marks delivered (POD UI:166–169). POD can be abandoned using Back (63), because it is already marked delivered.

**Consequence:** the company can show Delivered, release money, and tell customers evidence exists while no usable proof is stored. “Delivery updated” is shown on upload failure and the page closes (POD UI:111–115,161–164). Lost proof is not a minor upload inconvenience: it undermines disputes and accountability.

**Fix:** model handover verification and proof collection separately; confirm proof persistence/validation before a single final completion command, or explicitly allow an audited proof-exception state. Retain pending proof locally with retry and clear upload status.

**Acceptance:** kill network during upload → no false success and no second payout; invalid bitmap → capture retry; back navigation cannot silently bypass required proof; completion displays a retrievable proof timestamp/type; retry produces one final completion.

### R09 — P0 — Delivery settlement has incompatible percentages and multiple credit paths

**Confirmed.** Android OTP path computes 80% of `price` (Firebase:1771–1794). POD's `markParcelDelivered` again computes 80% of `price` and attempts wallet credit (VM:940–968) without consulting `payoutCredited`. Server status trigger credits 70% of `deliveryFee`, or fallback 1500/2500, plus tips (Functions:213–224). The server trigger increments money for each observed transition into DELIVERED without an idempotency guard on the balance mutation; a deterministic ledger ID does not prevent repeating that increment (227–240). Nested AGENTS describes salaried employees, while UI promises “payout split” (Rider UI:1370).

**Consequence:** rider earnings are neither a consistently calculated salary/bonus scheme nor one consistent per-job share. Depending on which writes succeed, payout can fail, differ across views, or be repeated by retries/status reversal. This is a company reconciliation problem, not merely a label issue.

**Fix:** owner-approved compensation policy; one immutable calculation snapshot and one server-owned ledger command keyed by delivery completion version. Treat tips, base salary and delivery incentives as explicit components. Account for partial settlement failure with a visible pending/retry state.

**Acceptance:** same delivery through every valid completion route yields identical one-time compensation; trigger retry cannot add money twice; status reversal cannot duplicate vendor/rider credits; finance reconciles delivery count, amount and ledger entries.

### R10 — P1 — “Online” does not mean available for the next dispatch

**Confirmed.** Online switch updates local state first and starts/stops location service (VM:759–777). Firebase sets `isOnline`, `is_active`, `isActive`, and status active/offline, reporting failures only to logs (324–339). It never checks an active job, vehicle inspection, shift/break, GPS permission or capacity. Self-accept checks only isOnline plus order PENDING (1547–1557). Mobile admin assignment explicitly bypasses online validation (VM:903). The rider catalog maps status busy only if that field already says busy and reads workload defaults (Firebase:2051–2062), but assignment does not atomically maintain capacity/busy state.

**Consequence:** a busy rider remains ONLINE & READY (Rider UI:306); going offline can stop tracking mid-job with no handoff; failed online writes can leave UI and dispatch disagreeing; “available” riders may be unlocatable or off duty.

**Fix:** one operational state model: off duty, unavailable, available, reserved, active job, break after job, offline with active job exception. Availability is a computed result of approval, shift, vehicle readiness, reachable device, fresh location and capacity. A user preference alone is insufficient.

**Acceptance:** rider with active exclusive job cannot be immediately assigned another; offline during active job opens a handoff workflow; missing GPS explains availability restriction; update failure rolls UI back; switch, attendance and admin agree.

### R11 — P1 — No durable FIFO queue, preassignment or automatic start-next contract

**Confirmed missing in traced paths.** ParcelStatus has only PENDING, ASSIGNED, PICKED_UP, ARRIVED, OUT_FOR_DELIVERY, TRANSIT, DELIVERED, CANCELLED (Model:7–8). Parcel has no server creation timestamp, queue position, reservation state, active-versus-next designation, promised start or expected version (Model:12–44). Available-delivery query filters only status=PENDING and has no chronological ordering, location or capacity filter (Firebase:1337–1339). Rider assignments likewise have no ordered manifest (1440–1442). Auto-dispatch runs on create only; if no online rider it logs “queued” and returns without a queue state or reconsideration command (Functions:76–97).

**Consequence:** user-requested Received/Queued → preassigned → active mission is not implemented by merely assigning several records to a rider. The system cannot distinguish “this rider is taking your order next” from “your order is currently underway”, cannot explain waiting age, and does not reliably wake old requests when a rider becomes available.

**Fix:** separate queue state from assignment and execution. Server FIFO sequence/createdAt, explicit priority with reason, reservation to a rider's next slot, reservation expiry/acknowledgment, promotion after completion, and a dispatcher exception queue. Keep the customer wording honest at each phase.

**Acceptance:** all riders busy → request visibly queued; oldest eligible job is offered/reserved first; after completion one next job becomes ready; rider must start it before customer sees moving; cancelled reserved job releases slot; operator priority override is recorded.

### R12 — P1 — Auto-dispatch can overload riders or overwrite a concurrent dispatch decision

**Confirmed.** Functions:90–116 chooses any online rider nearest by user-profile coordinates; it checks no workload, shift, reserved slot, GPS freshness, vehicle capability or maximum approach time. It later updates the order unconditionally (121–131), rather than transactionally rechecking PENDING/assignment. The true GPS service writes fleet_locations rather than the user-profile location that this matcher reads (GPS service:65–76).

**Consequence:** several orders created together can all go to the same rider; a rider's current location may be stale; admin/self-accept/cancellation can win initially and then be overwritten by the delayed automatic handler. Notification says “Open app to accept” (Functions:139), but status is already ASSIGNED and rider UI next action is CONFIRM PICKUP (Rider UI:1106).

**Fix:** atomically reserve rider capacity and order version; use fresh telemetry; score eligibility before distance; introduce assignment offer/acknowledgment or clearly communicate mandatory employee assignment. Never say “accept” without an actual acceptance action.

**Acceptance:** 20 simultaneous orders respect configured rider capacity; admin action during dispatch is not overwritten; declined/timed-out offer releases reservation; notifications correspond to the actual available button.

### R13 — P1 — One real enum state is displayed UNKNOWN, while two meanings are encoded in a progress number

**Confirmed.** PICKED_UP exists in Model:8 but the rider badge falls to UNKNOWN because it has no explicit case (Rider UI:989–999). Rider “confirm pickup” writes TRANSIT with progress .4 (1269,1304). TRANSIT <=.35 means PICKUP, >.35 means PICKED UP (992–993). PICKED_UP falls into the sheet's generic OTP completion branch (1220–1361). A simulator can lower progress back to .30 (1865) and thereby tell UI to “confirm pickup” again.

**Consequence:** admin and rider using different legitimate status values can send the same shipment down different flows. Visual progress is controlling operational meaning; animation/percentage adjustments can corrupt next-action logic.

**Fix:** canonical explicit state machine shared across Android, web, rules/functions and public tracking. Derive progress visuals from stage; never derive stage from progress. Support legacy mapping with an audited migration.

**Acceptance:** every canonical state has the same label/next action on all screens; PICKED_UP never shows UNKNOWN; changing progress alone cannot reopen pickup or permit delivery; legacy TRANSIT records map deterministically.

### R14 — P1 — Reassignment changes identity partially and lacks handoff ownership

**Confirmed.** `updateParcelAssignment` changes riderId/bike number only (Firebase:1682–1694), retaining prior courier name/phone/avatar. It has no current-state/capacity checks, new-rider acknowledgment, custody evidence or previous-rider notification. VM bulk reassignment ignores per-order errors, updates its local list and logs success (VM:736–745). Single mobile admin assignment fabricates a bike identifier from rider UID suffix (VM:902).

**Consequence:** customer can see the wrong driver identity, previous rider can still be on the road, dispatcher believes a batch succeeded when some writes failed, and responsibility for parcels already collected is unclear.

**Fix:** one full assignee identity snapshot backed by verified rider profile, plus separate pre-pickup reassignment and post-pickup custody-transfer workflows. Bulk action must return per-order success/failure; keep previous assignment history.

**Acceptance:** reassignment updates name/photo/phone/vehicle everywhere; old rider loses authority; after-pickup transfer requires custody acknowledgment; partially failed bulk action leaves failed rows selected with reasons.

### R15 — P1 — The open rider action sheet is a stale snapshot

**Confirmed.** `selectedParcelForUpdate` stores a Parcel object (Rider UI:76), and the sheet passes that snapshot directly (808–818). It is not reselected by ID from the live assignment collection. The update method does not enforce expected status/version (Firebase:1623–1630).

**Consequence:** while admin cancels/reassigns or another source advances the job, an already-open sheet can continue showing the old action and write it back. This is the sort of microflow that produces “I clicked the correct button but the order changed underneath me.”

**Fix:** selected ID plus live normalized order data; show “order changed” feedback; command includes expectedVersion/currentStage, with a clear refresh/review resolution.

**Acceptance:** admin cancels with rider sheet open → rider sees cancelled and action disables; admin reassigns → former rider cannot progress; conflict feedback preserves input and explains next step.

### R16 — P1 — Foreground tracking, order tracking and driver matching use disconnected location sources

**Confirmed.** Foreground service writes only `fleet_locations/{uid}` (GPS service:65–78). High-frequency manual tracker writes delivery coordinates (VM:1040–1068; Firebase:1712–1735). Auto-dispatch reads user-profile lat/latitude (Functions:108–109). Location service logs missing permission without a user-visible readiness result (60–62). Order-coordinate writes have no observation timestamp/accuracy/source (Firebase:1724–1734). The manual tracker has no disabled-provider action beyond an empty callback (VM:1051–1052).

**Consequence:** HQ and customer can disagree about location, stale coordinates remain visually plausible, “online” can coexist with missing permission, and there is no clear reason why location is unavailable. The open GPS sheet's coroutine can be cancelled on dismissal without a matching cleanup effect in that component; hardware listener lifecycle needs device verification.

**Fix:** one rider telemetry source with observedAt, receivedAt, accuracy, source and freshness; authorized active deliveries reference that stream/projection; frontend displays last updated and degraded state. Start/stop must be lifecycle-safe and tied to operational duty, not the modal.

**Acceptance:** lock screen/navigation away preserves authorized tracking; denied permission and disabled GPS explain recovery; stale > threshold shows stale; active order, fleet view and match engine use same most recent valid position; ending shift stops correctly.

### R17 — P1 — Attendance, inspection, expenses and leave requests claim remote submission but save only on the phone

**Confirmed.** Rider dashboard shift buttons call `clockInStatus` (Rider UI:481), which changes local state and writes a local Room attendance row (VM:1114–1127). The dashboard's boolean inspection path saves locally (1131–1164); expense claim says submitted for HR/Payroll (1186–1189); leave says submitted to manager (1209–1212). Their repository methods only insert in Room (Local DB:305–318). Separate cloud-writing clock-in and inspection functions exist later (VM:5709–5750) but are not the traced dashboard actions and declare success without awaiting Firestore Tasks.

**Consequence:** manager may never receive the report, approval cannot flow back, reinstall can lose records, and ON_DUTY/ON_BREAK can contradict isOnline. The UI creates the impression of a company process without connecting both ends.

**Fix:** one authoritative server workflow per domain with local cache/outbox; explicit draft, pending upload, submitted, approved/rejected states; shared collection/schema and real manager inbox. Attendance must represent one shift session with break intervals and clock-out, not a fresh clock-in row on each toggle.

**Acceptance:** expense submitted by rider appears in admin and approval appears back; failed upload stays pending; break changes dispatch eligibility; clock-out calculates the correct session; inspection failure blocks readiness or creates an authorized override.

### R18 — P1 — Offline sync reports success before uploads finish and is not connected to the delivery commands

**Confirmed.** No call sites for `queueOfflineAction` were found beyond its definition in the source search. Normal rider status action calls Firebase directly. `syncOfflineQueue` issues asynchronous Firestore writes and immediately marks each item synced (VM:1235–1267), even when db is null, payload contains no valid order ID, or a write later fails. Generic items go to logs rather than executing expense/inspection business workflows (1256–1263). It claims all list items successfully synchronized (1273). Local queue flow is ordered newest first (Local DB:175), while this code uses `_offlineSyncQueueList`; status replay has no version check.

**Consequence:** low signal can silently lose a delivery milestone while driver sees synchronized; old status can overwrite newer truth; supposed expense sync merely logs a payload. The dialog promise “Auto-syncs upon connection restore” (Rider UI:2209) is unsupported by the traced command path.

**Fix:** durable FIFO outbox per actor/order, await accepted server command, idempotency key, retry/backoff and conflict resolution. Differentiate saved on device, uploading and server accepted. Never blindly replay historical status after reassignment/cancellation.

**Acceptance:** airplane mode stores authorized intended action; reconnection uploads oldest dependent command first; server denial remains actionable; process kill does not lose pending proof/status; sync success counts actual accepted records only.

### R19 — P2 — Dashboard places secondary tools above the work the rider must perform now

**Confirmed layout/usability issue.** Default filter is Available (Rider UI:75), even if the rider already has active assignments. Profile/earnings, traffic/weather banner, attendance, four supporting tools and five more miniature tool buttons all appear before filters and delivery list (Rider UI:230–249,362–413,415–649,651–743). Nine controls include Batch AI, manual Geofence, bonus calculator and service. Several use 8sp labels and 36dp button heights (582–644), and the order's address lines have maxLines=1 (1055–1067).

**Consequence:** busy riders must scroll past administrative distractions and switch away from Available to find their actual mission. Truncated addresses, tiny labels and small controls are poor field usability, especially outdoors or with gloves. A dense “corporate operations hub” is not an efficient next-job screen.

**Fix:** top priority Current job → large next action → pickup/dropoff contact, navigation and exceptions → Next reserved job → remaining queue; move payroll/roster/tools into a secondary work/account area. Display complete actionable addresses with landmark and call/navigation shortcuts. Preserve locked branding while changing information priority.

**Acceptance:** returning rider reaches current job and correct next action without searching/filter changes; large font remains usable; addresses expand; core actions have generous touch areas; empty/active/offline states explain exactly what to do.

### R20 — P2 — Active count includes cancelled deliveries, and the multi-stop manifest falsely claims optimization

**Confirmed.** Active filter excludes only DELIVERED/PENDING (Rider UI:110), so cancelled assignments remain Active. Active count excludes only DELIVERED (117), so cancelled orders contribute. Any filtered list with >=2 rows outside Delivered shows “STOPS ACTIVE” and “Optimal dispatch sequence active” (685–736), including the default Available pool of unassigned orders. No optimizer output or sorted manifest drives those rows.

**Consequence:** rider workload is inflated, available unclaimed requests are described as an active route, and the app implies route optimization it has not performed. Admin may make a capacity decision using false workload.

**Fix:** shared terminal-state predicates; actual current manifest with explicitly ordered pickup/dropoff stops and assignment links; show optimization only after a real validated route is stored.

**Acceptance:** cancel a job → count and active list update; unassigned pool never becomes an “active trip”; two assigned jobs show a route based on actual coordinates and custody prerequisites; each stop links to its order action.

### R21 — P2 — Route optimization is a demonstration, not an operational trip planner

**Confirmed.** VM:5275–5340 uses nearest-neighbour straight-line distance from a hardcoded start, adds 3.2 km for unresolvable addresses, assumes 24km/h and 5 minutes per stop, reports fixed 96% confidence and status TSP_OPTIMIZED_SHORTEST_PATH. It does not optimize actual pickup-before-dropoff dependencies, rider current location, readiness, service windows, capacity, failed stops or Benin road constraints. The UI asks manually entered comma-separated addresses (Rider UI:2286), disconnected from assigned order IDs.

**Consequence:** a convincing “AI” label can induce trust in an unvalidated plan. Missing addresses silently contribute invented distance, and multiple-stop delivery has no per-stop confirmation/partial completion contract.

**Fix:** treat this as a planning draft until validated; feed actual assignment stops/coordinates into routing with precedence/capacity constraints; report assumptions rather than fixed confidence; let dispatch approve exceptions and rider report inaccessible roads.

**Acceptance:** parcel cannot be dropped off before picked up; unsupported addresses block plan approval; missing coordinates never become arbitrary kilometres; reordering preserves constraints; partial stop failure does not complete entire order.

### R22 — P2 — Performance, traffic and incident information includes fabricated certainty

**Confirmed.** Bonus calculator uses deliveredCount coerced to at least 12, on-time 97.5 and rating 4.9 (Rider UI:2633), while claiming automatic performance data (2648). The clear-condition banner states clear skies, normal traffic and ahead-of-schedule operations from one boolean (401–404). The rider catalog defaults rating 4.8, battery 90 and average time 20 (Firebase:2060–2062). Incident submission stamps `evidenceUploaded=true` and generic GPS/name fields without uploading evidence (VM:5387–5398), then returns success before awaiting remote persistence (5400).

**Consequence:** a new employee appears to have at least 12 deliveries and excellent performance; operations uses invented confidence; a safety incident claims evidence it does not contain. False data damages employee trust and incident investigations.

**Fix:** unknown must look unknown; derive metrics from verified events with date range/sample size; separate estimate from measured value; incident record needs linked order, real rider ID, actual location, optional evidence upload status and dispatcher acknowledgment.

**Acceptance:** new rider shows no measured performance; no fabricated minimum count; network unavailable incident remains pending; evidence flag becomes true only after stored evidence; traffic/weather claims identify a real source/freshness or are omitted.

### R23 — P1 — Essential delivery exception and recovery microflows are absent

**Missing design flow, grounded in the model/action branches.** The entire model has no attempt, failed-delivery, returning, refused, address issue, waiting-at-pickup, awaiting-customer, damaged/lost, cash discrepancy or reassignment-request stages (Model:7–44). The rider action sheet only accepts, confirms pickup, marks out for delivery or enters OTP (Rider UI:1220–1418). Generic incident dialog is not linked to order state/assignee/real location (VM:5384–5398).

**Consequence:** rider must improvise in phone calls/WhatsApp while the product falsely suggests forward progress. Admin cannot distinguish slow delivery from recipient unreachable, cannot approve a return, and cannot prevent custody/payment mistakes. In Benin, access directions, landmarks, compound entrances and unreachable recipients need first-class handling.

**Fix:** explicit exception action on every active job with structured reason, contact attempt, photo/note, waiting timer, dispatcher decision, retry/return/reschedule and customer message. Do not multiply status values indiscriminately: keep a clear execution stage plus exception substate with owner and next action.

**Acceptance:** recipient unreachable preserves package custody and prompts next contact/dispatch action; wrong address requires confirmation and possible fare update; breakdown triggers reassignment/custody handoff; refusal/return has proof and refund decision; no exception requires marking delivered to escape the screen.

### R24 — P1 — The rider cannot readily navigate/call the correct party from the core job action

**Confirmed omission in traced rider surface.** Card shows one-line pickup/dropoff, recipient name and status button (Rider UI:1022–1114). The sheet offers “CHAT WITH RECIPIENT” but no direct dial/maps action (1172–1203). Search of RiderScreens found ACTION/share only for waybill sharing, with no dial/map launch handler. Sender/receiver contact information is available in Parcel but not elevated into the immediate work surface. Recipient is not necessarily the signed-in customer in order chat.

**Consequence:** driver must find phone/address elsewhere, copy text or switch apps manually, increasing mistakes at the wrong pickup or handover. Chat may be directed at the booking account rather than the actual person at the door.

**Fix:** current-leg card with “Call sender” before pickup, “Navigate to pickup”, and corresponding recipient actions after pickup; explicit party labels and validated phone number; fallbacks when call/map app unavailable. Keep a persistent route summary in action sheet.

**Acceptance:** one action opens correct leg navigation and phone; multiple contacts are never confused; invalid/missing number is obvious; customer account and recipient identity are distinguished.

### R25 — P2 — Completed history is too thin for a rider or payroll investigation

**Confirmed.** Delivered filter just reuses the same parcel card (Rider UI:107–113,742 onward). Card does not display completion time/date, tip, verified payment, recipient handover identity or proof state despite the requirements. `dateString` is a generic text and Parcel has no deliveredAt/assignment timestamp fields (Model:33,12–44). All assignments are fetched unpaginated (Firebase:1440–1442). Tips total is computed from all delivered assignment rows (VM:859–861) with no reporting period.

**Consequence:** rider cannot answer which completed order earned what or prove a disputed shift's deliveries. History grows without clear period/search/pagination; changing assignment can alter whose history owns a completed order.

**Fix:** immutable completed-job summary with actual deliveredAt, original executing rider, recipient/proof, payout/tip status and ledger reference. Date filter/search and server pagination. Reassignment must not rewrite historical execution ownership.

**Acceptance:** completed history reconciles to finance by date range; each line opens proof and component earnings; payout pending is distinct from paid; a month of data remains usable; support can find by full ID/recipient/phone.

### R26 — P1 — Tip and rating logic is financially inconsistent and does not reliably measure feedback

**Confirmed source defects; some paths may currently fail under rules.** Firebase:1947 writes parcel before subsequently reading customer/rider in transaction (1956,1968). If customer has insufficient balance it simply skips deduction (1959–1961) but the rider-credit branch is still unconditional for positive tip (1966–1971). No already-rated/delivered validation is shown. Rider aggregate rating updates only inside positive-tip branch (1973–1977), so a rating with zero tip does not update that aggregate. Separate `submitDriverRating` creates random rating IDs and recomputes average by non-transactional read/write (2211–2237), opening duplicate/lost-update risks.

**Consequence:** payment and feedback have the wrong dependency; users who choose not to tip do not count consistently; the same job can be rated through incompatible mechanisms; under stronger rules the UI may just fail. Financial integrity must not rely on a restrictive rule accidentally blocking a flawed transaction.

**Fix:** separate one rating per completed delivery from optional tip transfer; server-validated sufficient funds and idempotent two-sided ledger transfer; aggregate from rating events, not delivery count or a fake default.

**Acceptance:** zero-tip review affects average; insufficient balance creates no tip/credit; repeat submission does not charge twice; concurrent ratings produce correct average/sample count; no rider can alter own rating financially.

### R27 — P2 — Inspection starts “passed” and does not enforce fleet readiness

**Confirmed.** All six safety checklist booleans default true (Rider UI:1939–1944); passing is merely their conjunction (VM:1141). Failure writes local data and a toast but does not block online/acceptance. Online and assignment handlers do not consult inspections (VM:759–777; Firebase:1547–1557).

**Consequence:** “Mandatory corporate fleet safety check” is only a label. One submit can claim everything was checked, and failed brakes can coexist with accepted dispatch.

**Fix:** unchecked/not-inspected defaults, time/vehicle-bound inspection, proof when required, safety failure block and supervisor override with reason/expiry. Present availability eligibility next to the online switch.

**Acceptance:** unchecked checklist cannot pass; failed critical item blocks new work; prior-day inspection does not silently pass today; approved override is visible in admin audit.

### R28 — P2 — Customer driver identity is not trustworthy enough for physical handover

**Confirmed.** Parcel's avatar defaults to an Unsplash portrait (Model:31). Rider catalog always assigns the same stock image (Firebase:2069), rather than a verified profile photo. Mobile assignment fabricates bike ID from UID (VM:902), and reassignment preserves old courier fields (Firebase:1682–1694). Auto-dispatch uses fallback ES-MOTO-01 if bike number missing (Functions:125).

**Consequence:** customer may see a human face/vehicle code that does not identify the arriving employee. A reassuring-looking profile is dangerous if it is decorative rather than factual.

**Fix:** verified employee name/photo, phone contact policy, actual vehicle plate/unit ID; absence uses a neutral silhouette and explicit pending verification. Assignment takes a consistent verified snapshot with history.

**Acceptance:** customer profile corresponds to the assigned employee; unavailable photo never becomes an unrelated person's face; vehicle missing blocks dispatch or shows a deliberate authorized exception.

### R29 — P1 — Private fleet location, job chats and proof storage have overly broad access

**Confirmed.** `fleet_locations` is readable by any authenticated account despite comment saying private (Rules:162–166). Delivery chats are publicly readable, and any authenticated user may create messages without participant/sender binding (189–192). POD storage permits any authenticated writer under any delivery ID and all signed-in users to read (`storage.rules`:21–23). Size restriction is not assignment authorization or MIME/content validation.

**Consequence:** unrelated users can inspect fleet movement or job communications; actors can post into orders they do not participate in; unauthorized proof objects can be uploaded under another job. This undermines employee privacy and evidential credibility.

**Fix:** participant- and role-scoped reads/writes; bind sender to auth UID; restrict POD to active assignee or authorized supervisor with immutable metadata and content rules. Only customers with their own active order get the necessary courier projection.

**Acceptance:** unrelated customer denied other rider stream/chat/proof; former assignee denied after handoff; payload cannot spoof sender; proof upload recorded with actor/time and linked to the correct completion.

### R30 — P2 — Existing “flow” tests do not test the operational flow

**Confirmed scope limitation.** `mobile/app/src/test/java/com/esdispatch/ParcelTrackingFlowTest.kt`:20–105 validates ID length/characters, enum membership and model defaults. It does not exercise assignment concurrency, online capacity, lifecycle guards, cancellation, OTP under actual rules, offline replay, proof failure, trigger retries or settlement. This is not a claim that no other tests exist; this specifically named flow suite does not establish end-to-end delivery correctness.

**Fix:** emulator/integration contract tests for the canonical lifecycle and rules, plus field walkthrough on release build using dedicated test orders. Reuse the same cases across customer/rider/admin and verify database events/ledger results.

**Acceptance:** a protected release checklist proves one complete Benin delivery, no-rider queue, busy-rider reservation, competing admin/rider update, wrong OTP lockout/reissue, network failure at POD, reassignment after pickup, recipient absent/return, cancellation/refund and duplicate trigger delivery.

## Proposed rider journey for the user's requested operating model

1. **Before work:** rider signs in to an approved employee profile; device GPS/notifications/network and today's assigned vehicle/inspection are checked; start shift results in Available only when eligible. Break/offline has clear consequences.
2. **Request received:** server records acceptedAt/FIFO sequence, confirmed Benin pickup/destination, contact roles, package constraints, payment readiness and requested window. Customer sees Received, then Queued if no eligible free capacity. Admin sees queue age and reason.
3. **Reservation:** dispatcher/system may reserve the rider's next slot while the rider handles the current job. Customer sees assigned rider with honest “starts after current delivery” wording and an estimate that admits uncertainty. Rider sees Next, not another simultaneous Current job. Reservation is acknowledged, can expire, and can be reassigned with reason.
4. **Start:** completion/abort of current mission frees capacity; next job becomes ready. Rider sees one Start next delivery action. Customer moves to Rider heading to pickup only after an accepted start event. No race can produce two current exclusive jobs.
5. **Pickup:** navigate/call sender; arrival prompt from measured location; waiting/parcel not ready/wrong item/cannot find sender are available; confirm custody with package count/condition, required photo/scan, timestamp and any exception. Customer sees Picked up only when this succeeds.
6. **Transit:** real hardware tracking with freshness/accuracy; rider has destination contact/map, clear instructions, incident/breakdown/correction escalation and no demo controls. Admin sees next expected milestone and lateness, not merely a status badge.
7. **Destination:** 50m prompt is an aid, never proof of handover. Rider can mark arrived and begin wait/contact workflow. OTP/alternate authorized proof tied to actual recipient, expiry and attempts; no completion if the package remains with rider.
8. **Completion:** proof and recipient verification accepted; one completion event creates Delivered and starts one settlement ledger workflow. Rider sees confirmation and next job; customer gets receipt/proof/rating; admin sees completion plus any pending payout, not an unexplained mixed state.
9. **Exception/return:** unreachable/refused/damaged/wrong address/breakdown have visible owner, action, timer and next decision. Returning is distinct from Delivered. Customer and rider receive the same truthful outcome; finance gets the correct refund/fee treatment.

## Essential lifecycle data that the current rider model does not adequately carry

Use a deliberate shared schema rather than many unrelated booleans: server created/accepted timestamps and queue sequence; execution stage and exception substate; assignment offer/reservation/active slot; rider acknowledgment; expected version; named timestamped stage events with actor; actual executing rider identity snapshot; verified coordinates plus source/accuracy; telemetry observedAt; estimated pickup/start and lateness; contact roles; package/custody proof; waiting and contact attempts; challenge state without secret; proof upload state and immutable object reference; completion record; compensation snapshot/settlement status; cancellation/return reason and refund linkage.

The schema must be read consistently by web admin, Android customer, Android rider, public tracking and server functions. An event log should answer: who changed what, from which stage, why, using what evidence, at what time, and which subsequent operation failed. A single mutable status string cannot answer those questions.

## Recommended critical-path field test pack

- New customer request, zero riders available: customer sees Queued, admin sees FIFO age, no fabricated assignee/location; turning a qualified rider available triggers reconsideration.
- Rider already executing one job: a second request becomes reserved/next; it cannot change the current route; after completion it becomes ready and requires a clear start action.
- Two admins and one rider acting on the same pending request: exactly one assignment/version wins; losing screens explain conflict without overwriting the winner.
- Rider opens sheet, admin cancels/reassigns: sheet updates immediately and stale action is rejected server-side.
- Pickup pin unresolved or outside owner-approved Benin zone: booking/dispatch is blocked or routed to review; no Lagos/hash fallback.
- Device stationary with permission granted: customer does not move; deny GPS or turn it off: screen tells the truth and no synthetic journey is produced.
- Wrong OTP five times, expired OTP after a long queue, code sent to sender while recipient has no app: each has an operator-visible recovery path and audit.
- Network drops on proof upload; process killed; reopen: pending proof survives, Delivered/credit is not duplicated, and user understands whether upload is pending or failed.
- Recipient absent/refuses, rider has a breakdown after pickup, damaged parcel, incorrect compound/entrance: custody remains explicit until reschedule/transfer/return decision.
- Delivery and settlement trigger retry, reversal attempt, repeated POD: one completion and one approved compensation amount, with exact ledger reconciliation.
- No-tip review and insufficient-wallet tip: honest rating result and zero accidental credit.
- Expense/inspection/leave submitted from rider: real admin receives it, review returns to rider, and offline status is explicit rather than a false submitted toast.

