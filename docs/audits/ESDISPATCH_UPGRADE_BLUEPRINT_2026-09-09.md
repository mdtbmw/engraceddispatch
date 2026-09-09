# ESDispatch Upgrade Blueprint: Making the App Feel Alive, Premium and Operationally Real

Date: 2026-09-09  
Workspace: `D:\Eng App`  
Document type: product upgrade blueprint, separate from the end-to-end audit.  
Operating constraint: preserve the current brand identity, current visual direction and current layout structure. Upgrade behavior, depth, motion, intelligence, reliability and flow without breaking the UI.

## 1. Product Vision

ESDispatch should feel like a living dispatch system, not a set of screens that wait for users to refresh or guess what happened. The app should breathe with real operational events: requests arriving, riders becoming free, queue positions moving, maps updating with fresh GPS, admin actions resolving blockers, proofs being captured, payments settling and notifications clearing properly.

The goal is not to make the app louder. The goal is to make it calmer, smarter and more responsive. The current layout can stay recognizable. The upgrade should add better data, better transitions, better feedback, better algorithms and better state handling inside the existing surfaces.

The north star:

- A customer always knows what is happening and why.
- A rider always knows the next physical action.
- An admin always sees the oldest urgent work and can act without hunting.
- The system always knows whether an order is queued, reserved, assigned, active, blocked, delivered or settling.
- Notifications appear once, deep-link correctly and clear permanently.
- Motion and polish come from real state changes, not fake certainty.
- AI-level intelligence means explainable dispatch decisions, not random "AI" labels.
- The product remains Benin City-focused and premium.

This blueprint assumes the audit findings are accepted. It does not repeat every defect. It describes the upgraded product the app should become.

## 2. Design Principles

### Preserve the current layout shell

Do not tear the product apart visually. Keep the existing navigation, dashboard zones, cards, tracking screen shape, rider dashboard structure and admin sections where possible. Upgrade them from within:

- replace thin cards with richer live content;
- add drawers and inline controls instead of new full-page detours;
- use progressive disclosure rather than more top-level tabs;
- preserve brand colors and spacing language;
- use the official gold `#FFB800`;
- keep the logo and slogan locked;
- keep dark/light theme rules intact.

### Make every promise state-backed

Every label should be backed by real data. If the screen says live, it needs fresh telemetry. If it says queued, the backend must store queue position or queue reason. If it says rider assigned, the rider must be reserved or accepted under a server rule. If it says delivered, proof and handover must exist or an approved exception must exist.

### Make motion meaningful

Motion should communicate state changes:

- a queue card softly advances when its position changes;
- a rider card settles into "reserved next";
- a delivery timeline pulse lands on the new milestone;
- an exception badge expands only when action is needed;
- a notification clears and stays cleared;
- a map marker breathes only when telemetry is fresh.

Motion should never hide uncertainty. If GPS is stale, the marker should fade or show "last seen 7 min ago", not animate.

### Make intelligence explainable

"AI-level" dispatch should mean a scoring model that the admin can inspect. It should explain:

- why this rider is best;
- why another rider is blocked;
- how queue age affected the decision;
- whether traffic, distance, rider load, vehicle type or customer priority changed the ranking;
- what confidence means and what data is missing.

The app should avoid fake precision such as 98.6 percent unless it is a measured model score with known inputs.

## 3. Upgrade Architecture

The app needs a shared event-driven product core. Keep the same UI shell, but add a smarter state layer underneath it.

Recommended layers:

1. Delivery lifecycle service: owns stages, transitions, actor permissions and event log.
2. Dispatch allocation service: owns queue, rider availability, reservation, assignment and capacity.
3. Routing service: owns distance, ETA, route geometry, batching and stop order.
4. Notification service: owns dedupe, delivery, read state, deep links and audience.
5. Finance service: owns payment, wallet, settlement and ledger.
6. Proof service: owns OTP/challenge, photo/signature, upload retry and final proof state.
7. Experience state store: exposes customer/rider/admin read models optimized for each screen.

The frontend should subscribe to read models instead of reconstructing business truth from raw mutable documents.

## 4. Delivery Lifecycle Upgrade

Replace the loose status model with one shared lifecycle contract. Keep customer wording simple, but store enough operational detail for admin and rider flows.

Core fields:

- `deliveryId`
- `publicTrackingToken`
- `createdAt`
- `receivedAt`
- `queueSequence`
- `queueState`
- `assignmentState`
- `fulfillmentStage`
- `exceptionState`
- `paymentState`
- `proofState`
- `settlementState`
- `currentRiderId`
- `reservedRiderId`
- `activeJobId`
- `version`
- `lastEventAt`
- `customerVisibleState`
- `adminNextAction`
- `riderNextAction`

Public customer states:

- Request received
- Address review
- Payment pending
- Queued
- Rider reserved
- Rider assigned
- Heading to pickup
- At pickup
- Picked up
- On the way
- At destination
- Proof in progress
- Delivered
- Action needed
- Cancelled

Admin/rider operational stages:

- `RECEIVED`
- `ADDRESS_REVIEW`
- `PAYMENT_REVIEW`
- `QUEUED`
- `OFFERED`
- `RESERVED_NEXT`
- `ASSIGNED_ACCEPTED`
- `HEADING_TO_PICKUP`
- `ARRIVED_PICKUP`
- `PICKED_UP`
- `IN_TRANSIT`
- `ARRIVED_DROPOFF`
- `HANDOVER_VERIFYING`
- `PROOF_PENDING`
- `DELIVERED`
- `SETTLEMENT_PENDING`
- `CLOSED`
- `EXCEPTION`
- `RETURN_PENDING`
- `CANCELLED`

Do not expose all of those as a customer dropdown. Use them to power simple and truthful UI.

## 5. Queue and Preassignment Upgrade

This is the breakthrough flow the app is missing. It should feel like a small dispatch brain working in real time.

### Queue behavior

When a request is accepted but no eligible rider is free, the backend writes:

- queue position;
- reason: no rider, address review, payment pending, service zone, vehicle type, priority hold;
- oldest eligible timestamp;
- next reconsideration time;
- customer visible text;
- admin next action.

Customer sees "Queued - request received". Admin sees the queue sorted by oldest eligible first. Riders do not see unassigned noise unless self-claiming is part of the business policy.

### Preassignment behavior

If a rider is busy but the system predicts they are a strong next fit, admin or system can reserve them:

- order becomes `RESERVED_NEXT`;
- rider sees it under "Next";
- customer sees "Rider reserved, starts after current delivery";
- admin sees current job, estimated release and risk;
- reservation expires if rider goes offline, job overruns or admin cancels it.

When the rider completes the current job, the reserved order is promoted into an offer or accepted current job depending on policy.

### Reserve-next rules

Reserve-next should require:

- rider approved and active;
- rider not suspended;
- fresh GPS or known last zone;
- current job has expected completion window;
- package fits vehicle/capacity;
- service area matches;
- reservation count below limit;
- no higher-priority job already reserved.

### Customer copy

Use honest copy:

- "All riders are currently completing deliveries. Your request is in line."
- "Osas has been reserved for your delivery after his current drop-off."
- "Your rider has accepted and is preparing to head to pickup."
- "Your rider has started moving to pickup."

This is more premium than pretending the rider is already coming.

## 6. Smart Dispatch Algorithm

The dispatch algorithm should score eligible riders and explain the result. Start with deterministic rules, then improve with learned estimates later.

### Hard eligibility filters

A rider cannot be considered if:

- offline;
- suspended or deactivated;
- missing today's shift;
- missing required inspection;
- no fresh GPS and no acceptable fallback;
- outside Benin operating zone;
- active exclusive job with no reserve-next policy;
- already reserved beyond capacity;
- vehicle cannot carry package;
- phone/device unreachable;
- account requires admin review.

### Scoring factors

Score only eligible riders:

- distance to pickup;
- predicted time to pickup;
- queue fairness;
- rider current load;
- reserve-next release time;
- vehicle type and package fit;
- historical reliability;
- customer/rider zone familiarity;
- battery/GPS freshness;
- traffic risk;
- service level;
- cancellation/no-show pattern;
- admin override reason.

Example scoring model:

```text
score =
  30 * pickup_eta_score +
  15 * queue_fairness_score +
  12 * vehicle_fit_score +
  12 * workload_score +
  10 * gps_freshness_score +
   8 * reliability_score +
   6 * zone_familiarity_score +
   5 * traffic_risk_score +
   2 * service_priority_score
```

The admin should see the top reason, not the math dump:

- "Best fit: 8 min to pickup, free now, motorcycle fits 2.5 kg parcel."
- "Reserve next: currently 11 min from completing another job."
- "Blocked: GPS stale for 18 minutes."
- "Blocked: package exceeds motorcycle limit."

### Algorithm maturity stages

Stage 1: rule-based dispatch.  
Stage 2: route-aware dispatch using travel time.  
Stage 3: capacity and reserve-next optimizer.  
Stage 4: ETA prediction from historical Benin delivery data.  
Stage 5: exception prediction and proactive intervention.

Useful official references for implementation planning:

- OR-Tools vehicle routing and pickup-delivery constraints: https://developers.google.com/optimization/routing
- Google Route Optimization API: https://developers.google.com/maps/documentation/route-optimization
- Mapbox Optimization API: https://docs.mapbox.com/api/navigation/optimization/

## 7. Uber-Level Routing Without Overpromising

"Uber-level" routing means the system understands real-time location, demand, capacity, ETA, matching and status. It does not mean the app should invent routes when data is missing.

### Routing features to add

1. Fresh route geometry between rider, pickup and drop-off.
2. ETA range instead of exact false precision.
3. Traffic-aware travel time where provider data is available.
4. Route refresh when rider deviates significantly.
5. Stop ordering for multi-stop deliveries.
6. Pickup-before-drop-off constraints.
7. Vehicle capacity constraints.
8. Batch route planning for a rider's manifest.
9. Dispatch heat map for Benin neighborhoods.
10. Stale GPS fallback that pauses live claims.

### Multi-stop routing

For batch or marketplace flows, every stop must have type:

- pickup;
- drop-off;
- return;
- transfer;
- exception review.

The optimizer must never drop off an item before its pickup. It must understand capacity. If a rider can carry 10 kg and already has 7 kg, it should not assign another 6 kg parcel unless the route drops something first.

### ETA design

ETA should be:

- a range, such as 18-25 minutes;
- source-labeled, such as "based on rider GPS 2 min ago";
- recalculated only when inputs change meaningfully;
- degraded when GPS/traffic/address data is weak.

Avoid fake labels like "98.6 percent precision" unless a real model evaluation exists.

## 8. Live Map Upgrade

The map should feel alive but truthful.

Customer map behavior:

- Before rider starts: show pickup and destination, not fake rider motion.
- Rider reserved: show rider profile card, but no moving marker unless active tracking is live.
- Rider heading to pickup: show rider marker if GPS fresh.
- Picked up: show rider and destination.
- GPS stale: marker fades and text says "Last update X min ago."
- GPS unavailable: map switches to route overview and tells the truth.
- Arrival: use measured proximity as a prompt, not proof.

Admin map behavior:

- show all available riders with freshness;
- show busy riders with current leg and expected release;
- show queued orders by neighborhood;
- show exception orders with warning style;
- avoid auto-zooming away from the dispatcher while they are inspecting.

Rider map behavior:

- show current leg only;
- show next reserved job in small secondary panel;
- show call/navigate/exception actions near the map;
- stop simulation from writing production coordinates.

Map marker design:

- fresh GPS: subtle breathing marker;
- stale GPS: dim marker with timestamp;
- offline: grey static last-known marker;
- simulated/demo: only in demo environment with explicit label;
- exception: warning ring with reason.

## 9. Notification System Upgrade

Notifications should feel dependable. Once the user closes or reads a notification, it should not keep coming back because another listener saw the same document.

### Notification event model

Create one event for one meaningful transition:

- `eventId`
- `recipientId`
- `recipientRole`
- `deliveryId`
- `eventType`
- `title`
- `body`
- `deepLink`
- `createdAt`
- `dedupeKey`
- `readAt`
- `dismissedAt`
- `deliveredAt`
- `deliveryStatus`

The dedupe key can be:

```text
deliveryId + ":" + eventType + ":" + transitionVersion + ":" + recipientId
```

### Fix repeated notifications

The app should not notify from raw delivery `ADDED` or every `MODIFIED` snapshot. It should notify from notification events. Each event should be shown once per recipient and then marked read/dismissed. GPS changes should not create stage notifications.

### Clearing behavior

When the user clears a notification:

1. update local UI immediately;
2. write `dismissedAt` or archive flag for that recipient;
3. suppress the same `dedupeKey` forever unless a newer transition version exists;
4. keep immutable event history for support/admin if needed.

### Deep links

Every notification must open the exact order or exact support ticket:

- customer delivery update opens that delivery;
- rider assignment opens that job action;
- admin exception opens the dispatch drawer;
- support reply opens the ticket;
- wallet alert opens the transaction.

### FCM guidance

Android should avoid rendering both notification and data payloads as separate alerts. Use one normalized payload and one local rendering path. Android notification permission behavior and FCM message handling should follow current official guidance:

- Firebase Cloud Messaging docs: https://firebase.google.com/docs/cloud-messaging
- Android notification permission docs: https://developer.android.com/develop/ui/views/notifications/notification-permission

## 10. Admin UI Upgrades Without Layout Breakage

Keep the existing admin page and sections, but upgrade the interactions.

### Dashboard upgrades

Add these within existing cards:

- live queue age;
- oldest unassigned request;
- rider availability summary;
- exceptions count;
- settlement issues count;
- notification failures count;
- "open dispatch queue" action;
- "oldest waiting" shortcut.

Replace decorative active request modal with an operational drawer:

- full order details;
- full contacts;
- payment status;
- address confidence;
- rider selector;
- reserve-next option;
- next action;
- exception button;
- timeline.

### Shipment table upgrades

Keep the table layout, but add:

- current state always visible;
- legal next action button;
- queue age column;
- payment state column;
- rider state badge;
- GPS freshness badge;
- blocker badge;
- row drawer;
- stable filters;
- direct ID deep-linking.

### Rider selector upgrades

Rider cards should show:

- Available now, Busy, Reserved, Offline, Blocked;
- current job;
- estimated release;
- distance to pickup;
- route to pickup;
- vehicle type;
- capacity;
- GPS age;
- phone reachability;
- rating based on verified deliveries;
- reason ineligible.

### Motion and feel

Small upgrades that make the admin feel alive:

- queue card gently slides when position changes;
- new request row glows once, then settles;
- conflict message appears inline, not as generic alert;
- assignment button changes from "Assign" to "Offering" to "Accepted";
- rider card moves from Available to Busy with a soft transition;
- drawer timeline animates the new event only once;
- clearing a notification collapses it and does not resurrect it.

## 11. Customer App Upgrades

The customer app should feel premium because it is clear and responsive.

### Booking

Upgrade booking with:

- address confidence chips;
- "confirm pin" step for weak addresses;
- Benin service area result;
- price quote version;
- payment reservation state;
- package handling recommendation;
- saved pickup/drop-off contacts;
- delivery notes for gate/landmark/compound;
- clear received/queued state after submit.

### Tracking

Upgrade tracking with:

- customer-visible lifecycle states;
- rider reserved versus rider en route distinction;
- verified rider card;
- ETA range with freshness;
- map marker freshness;
- proof/receipt after delivery;
- report issue button;
- cancellation/edit request before pickup;
- support linked to order.

### Notifications

Customer notifications should be grouped by order:

- newest important event on top;
- clear all read;
- dismissed means dismissed;
- no repeat alerts for old statuses;
- order-specific notification settings;
- quiet updates for GPS movement;
- loud updates for assignment, exception, arrival, delivered and refund.

### Delight features

Add delight only where it helps:

- live timeline that fills stage by stage;
- soft receipt animation after delivery;
- rider "started your delivery" moment;
- package-safe tips/rating flow;
- saved favorite locations;
- smart reorder for frequent addresses;
- polite delay explanation when queue changes.

## 12. Rider App Upgrades

The rider app should become a field tool.

### Current job first

The main rider dashboard should always show:

- current job;
- next action;
- pickup/drop-off leg;
- call button;
- navigate button;
- exception button;
- proof requirement;
- GPS freshness;
- next reserved job.

### Availability

Replace a simple online toggle with an eligibility panel:

- account approved;
- shift active;
- GPS on;
- notifications enabled;
- vehicle checked;
- no blocking incident;
- current job load;
- next reservation.

The rider can still tap one switch, but the app explains why they are or are not dispatchable.

### Field exception kit

Every job needs:

- sender not reachable;
- parcel not ready;
- wrong pickup;
- customer not reachable;
- wrong drop-off;
- recipient refused;
- damaged item;
- rider breakdown;
- police/road block;
- unsafe delivery point;
- return to sender;
- request admin call.

Each exception should capture:

- reason;
- note;
- photo when needed;
- location;
- contact attempt;
- timestamp;
- admin decision.

### Proof

Proof flow should support:

- OTP/challenge;
- photo;
- signature;
- receiver name;
- receiver phone confirmation;
- alternative proof exception;
- offline pending upload;
- retry queue;
- clear success/failure state.

Do not mark final delivered until proof is stored or approved.

## 13. Widgets and Live Surfaces

Widgets can make the product feel alive without changing the main layout.

### Customer widget

Home screen widget:

- active delivery status;
- queue/rider/ETA;
- last update freshness;
- tap to exact tracking screen;
- compact map-free version for battery;
- "Action needed" state.

### Rider widget

Rider widget:

- current job next action;
- start/navigate/call shortcut;
- next reserved job;
- shift availability;
- GPS issue warning.

### Admin mini-board

Admin web can add a compact pinned panel:

- oldest queued request;
- exceptions;
- available riders;
- delivery listener health;
- settlement failures.

### Live activity style

If platform support exists later, add live status surfaces for active orders. But first build the event model so the widget does not repeat stale notifications.

## 14. Smart Management Features

Management should see business health, not vanity numbers.

### Operations intelligence

Add:

- queue age by neighborhood;
- rider utilization;
- missed SLA reasons;
- cancellation reasons;
- failed assignment reasons;
- proof failure rate;
- GPS stale rate;
- delivery duration by route;
- pickup wait time;
- recipient wait time;
- exception recovery time.

### Rider management

Add:

- availability calendar;
- shift start/end;
- verified vehicle profile;
- inspection history;
- current job and next reserved job;
- reliable completion rate;
- average pickup wait;
- customer rating sample size;
- incident history;
- earnings/settlement report.

### Customer management

Add:

- order history;
- payment state;
- support issues;
- repeated address problems;
- cancellation history;
- refund history;
- saved contacts;
- trust status based on real behavior, not vague "anti-cancellation flags".

### Finance management

Add ledger-backed views:

- wallet liability;
- cash collected;
- delivery revenue;
- marketplace GMV;
- vendor payout;
- rider payout;
- refunds;
- unsettled deliveries;
- failed payment verifications;
- manual adjustments with reason.

## 15. AI and Algorithmic Features That Are Actually Useful

Avoid fake AI. Add useful intelligence.

### Dispatch recommendation engine

Shows top rider recommendation and explanation:

- best now;
- best reserve-next;
- blocked riders;
- confidence and missing data;
- expected pickup time;
- effect on queue fairness.

### ETA prediction

Start deterministic:

- current rider location;
- route distance;
- road/traffic provider if available;
- historical delivery time by neighborhood and time of day;
- pickup wait estimates;
- GPS freshness.

Later train a model on completed deliveries, but only after enough clean data exists.

### Exception prediction

Predict orders likely to become late or fail:

- weak address confidence;
- rider GPS stale;
- pickup wait exceeding normal;
- recipient not reached;
- high demand and low rider availability;
- weather/traffic input if real.

Surface as "needs attention", not scary fake risk scores.

### Smart support assistant

Admin-facing assistant should summarize:

- what happened;
- current blocker;
- last rider/customer message;
- suggested next action;
- refund/proof/payment state.

Customer-facing assistant should only execute supported commands or guide to the correct screen.

### Demand forecasting

Forecast:

- busy neighborhoods;
- expected queue pressure;
- rider staffing needed;
- marketplace fulfilment volume;
- rain/traffic operational impact if real inputs exist.

### Fraud and abuse checks

Use measured signals:

- repeated failed OTP attempts;
- repeated cancellation after rider arrival;
- mismatched payment reference;
- device/account anomalies;
- impossible location jumps;
- unusual wallet adjustment patterns.

Do not block customers silently. Create review tasks.

## 16. Visual and Motion Upgrade Pack

Keep the layout; improve the feel.

### Micro-interactions

- Buttons compress on press and spring back.
- Cards settle into new state.
- Status chips morph color/text when stage changes.
- Timeline dots pulse once when completed.
- Toasts are replaced by anchored inline confirmations where possible.
- Loading skeletons match the final layout.
- Empty states include the next useful action.
- Offline/stale states have calm warning treatment.

### Admin motion

- New order enters the queue with a brief highlight.
- Assigning rider shows a short progress state.
- Accepted assignment moves row from queue lane to active lane.
- Exception opens with a focused warning panel.
- Dismissing notification animates away and stays away.

### Customer motion

- Booking confirmation becomes a receipt card.
- Queue position changes with subtle motion.
- Rider assigned moment reveals verified rider card.
- Map marker breathes only with fresh GPS.
- Delivered proof card appears after final state.

### Rider motion

- Start job button responds immediately.
- Arrival prompt slides up near destination.
- Proof capture has tactile progress.
- Offline sync badge shows pending proof/status.
- Next job promotion has a clear transition.

## 17. UI Bug Repair List to Pair With Upgrades

These fixes should be bundled into the upgrade work:

1. Notifications clear permanently per recipient and dedupe key.
2. Notification taps open the exact order.
3. Admin search handoff opens selected order directly.
4. Current status always appears in dropdowns.
5. Dashboard popup updates from live data.
6. Admin nested tab components stop resetting on live updates.
7. Table selection state is scoped to visible rows and reviewed before bulk action.
8. Loading/error/empty states are distinct.
9. No gold-on-white or white-on-gold violations.
10. Brand name and slogan are consistent.
11. Benin-only dispatch copy is consistent.
12. Fake Lagos defaults are removed from dispatch flows.
13. No production demo seed controls in daily dashboard.
14. No raw OTP in any visible or readable delivery path.
15. No stock/random rider portraits as verified identity.
16. Manual delivery creation uses the same backend flow.
17. Support reply draft survives failed send.
18. Admin support ticket selection does not jump.
19. Rider active count excludes cancelled/terminal orders.
20. Marketplace order IDs cannot collide.

## 18. Scalability Plan

The product should be built as if Benin City volume grows from 10 deliveries a day to 10,000.

### Data scale

- query by indexed states;
- paginate histories;
- use read models for dashboards;
- avoid listening to whole collections where not needed;
- use server timestamps;
- archive old events;
- keep immutable ledger/event logs separate from current delivery summary.

### Dispatch scale

- allocate by zone and rider availability;
- queue by eligibility, not raw creation only;
- reserve next jobs;
- batch marketplace pickups;
- support manual override with audit;
- run queue drain workers on rider availability, job completion and schedule.

### Support scale

- link tickets to orders;
- classify issue types;
- show SLA;
- preserve ownership;
- summarize current order truth;
- avoid generic inbox-only support.

### Finance scale

- integer minor units for money;
- ledger postings;
- settlement retries;
- reconciliation exports;
- payment reference uniqueness;
- immutable adjustment reasons.

## 19. Timeless Product Details

Timeless product quality comes from small details that keep working:

- every screen has a truthful empty state;
- every destructive action has reason and recovery;
- every background failure becomes a visible task;
- every important event has a timestamp;
- every actor has a name and role;
- every status has a next action;
- every customer-visible promise is backed by data;
- every financial number ties to ledger;
- every notification has a lifecycle;
- every route has a freshness label.

This is what makes the app feel premium after the first week, not just on launch day.

## 20. Implementation Roadmap

### Sprint 1: Truth and safety

- remove production GPS simulation writes;
- restrict delivery/OTP/proof/fleet/wallet writes;
- create server command skeleton;
- normalize notification events;
- fix notification clearing/dedupe;
- remove fake assistant action claims;
- separate loading/error/empty states.

### Sprint 2: Lifecycle and queue

- add shared lifecycle schema;
- add queue state and FIFO sorting;
- add rider availability computation;
- add reserve-next;
- add assignment conflict protection;
- update customer queued/reserved states;
- update admin drawer.

### Sprint 3: Proof and settlement

- move OTP/challenge to server;
- require proof before final delivery;
- build offline proof retry;
- unify settlement formula;
- add idempotent ledger posting;
- show settlement status.

### Sprint 4: Routing and live experience

- integrate routing provider or OR-Tools planning;
- add ETA ranges;
- add GPS freshness;
- add route deviation detection;
- add multi-stop constraints;
- upgrade map marker states.

### Sprint 5: Smart management

- operations metrics;
- rider utilization;
- queue SLA reporting;
- finance reconciliation;
- support order summaries;
- exception prediction.

### Sprint 6: Polish and field validation

- refine motion;
- accessibility pass;
- mobile widget upgrades;
- admin keyboard flows;
- staging full-lifecycle tests;
- real rider/customer/admin field test in Benin City.

## 21. Award-Winning Experience Checklist

The app becomes award-worthy when it feels calm, alive and truthful at once:

1. A customer books and immediately sees the right state.
2. If riders are busy, queue state is honest and reassuring.
3. Admin sees the oldest urgent work first.
4. Rider assignment happens in one drawer.
5. Busy riders can be reserved as next.
6. Rider identity is verified and beautiful.
7. Map motion reflects real GPS freshness.
8. ETA uses ranges and updates gracefully.
9. Exceptions are handled inside the app.
10. Notifications never repeat after dismissal.
11. Notification deep links always land correctly.
12. Proof is captured before delivered.
13. Finance reconciles automatically.
14. Support sees the full order story.
15. AI recommendations explain themselves.
16. UI motion is soft, fast and purposeful.
17. The app never hides uncertainty.
18. Benin City scope is consistent.
19. Admin, rider and customer see one shared truth.
20. Every important action has a visible result.

## 22. Final Product Direction

The upgrade should make ESDispatch feel like a premium operations room in your pocket. The same existing layout can remain, but the behavior underneath must become event-driven, intelligent and honest.

Do not add more fake "AI" labels. Add real dispatch recommendations. Do not add more animated maps. Add fresh GPS, stale-state handling and truthful ETA ranges. Do not add more status dropdowns. Add legal next actions. Do not add more notifications. Add fewer, smarter, reliable notifications that clear forever.

The breakthrough version of ESDispatch is not a flashy redesign. It is the current product becoming deeply aware of every order, rider, customer, payment, proof, exception and next action. That is what will make it feel alive for real.

## 23. Screen-by-Screen Upgrade Map

This section keeps the existing layout concept and upgrades what each screen knows, shows and does.

### Customer dashboard

Keep the hero, stats, recommended shops and active shipments structure. Upgrade the data behind it.

Add:

- active delivery card with true lifecycle state;
- "queued" state if no rider is available;
- "rider reserved" state if preassigned;
- one-tap track current delivery;
- one-tap contact support for active delivery;
- saved address shortcuts with serviceability confidence;
- delivery credit/wallet shown from server ledger, not local assumptions;
- reward points tied to verified events;
- marketplace recommendation based on actual availability.

The dashboard should not become a wall of explanations. It should surface the most important live thing. If there is an active order, that order owns the screen. If there is no active order, the dashboard can show booking shortcuts and market recommendations.

### Customer booking screens

Preserve the current booking layout but add a "truth layer":

- address confidence shown beside each address;
- Benin service-zone pass/fail;
- "pin confirmed" check for weak addresses;
- contact completeness indicator;
- package fit recommendation;
- price quote version;
- payment readiness;
- final review with customer-visible promises.

The final booking button should create a server request and then show one of these outcomes:

- Request received;
- Payment pending;
- Address review needed;
- Saved offline;
- Failed with retry.

### Customer tracking

Keep the premium tracking screen and map, but make every motion honest.

Add:

- state-specific map behavior;
- GPS freshness label;
- ETA range;
- rider reserved/assigned/en route distinction;
- verified rider identity;
- proof/receipt card after delivery;
- exception message if order is blocked;
- notification preference for this order;
- deep link safety so every alert opens this exact tracking view.

Remove or replace:

- fake AI precision;
- simulated approach when no fresh GPS exists;
- stale Lagos fallbacks;
- generic "active riders in your dispatch zone" if no queue worker is active.

### Customer notifications

Keep the notifications list, but change the source. It should read notification events, not infer alerts from raw delivery snapshots.

Add:

- grouped by order;
- unread count by event type;
- dismiss one, dismiss all read;
- permanent suppression of same event;
- deep link preview;
- "why am I seeing this?" metadata for major events;
- order-specific notification settings.

### Rider dashboard

Keep the rider dashboard structure, but make the current job dominate.

Add:

- current job card;
- next reserved job card;
- dispatchability checklist;
- route leg indicator;
- one primary action;
- call/navigate/exception actions;
- GPS freshness;
- proof requirement;
- offline sync status.

De-emphasize:

- analytics while rider is on active job;
- local-only HR/fleet screens;
- demo optimizer panels;
- anything that distracts from the current physical delivery.

### Rider job sheet

Upgrade the sheet into a field workflow:

- accept job;
- start pickup leg;
- arrive pickup;
- confirm package;
- report pickup exception;
- start drop-off leg;
- arrive drop-off;
- verify handover;
- upload proof;
- complete;
- next job promotion.

Each step should show who will be notified and what the customer will see.

### Admin dashboard

Keep the dashboard, but change the first mental model from "stats" to "control tower".

Add:

- oldest queued request;
- active exceptions;
- available riders;
- busy riders with release time;
- settlement failures;
- delivery listener health;
- one-click open dispatch drawer.

### Admin shipment management

Keep the table, but make it a queue console.

Add:

- order age;
- current blocker;
- legal next action;
- assigned/reserved rider state;
- payment status;
- GPS freshness;
- address confidence;
- row drawer;
- exact order deep links.

### Admin support

Keep ticket layout, but add order context:

- linked order summary;
- current delivery state;
- last rider event;
- payment/refund state;
- suggested reply;
- internal note;
- actual admin identity on reply;
- stable selected ticket.

### Admin finance

Separate ledger-backed numbers from decorative revenue.

Add:

- wallet liability;
- collected delivery fees;
- marketplace GMV;
- vendor payable;
- rider payable;
- settlement pending;
- refund pending;
- failed payment verifications;
- manual adjustments with reason.

## 24. Notification Cleanup Specification

The notification problem deserves its own build spec because it affects every user.

### Current bad feeling

Users dismiss something, then it appears again. They tap a notification and do not land on the exact order. Foreground messages can produce duplicate alerts. Old delivery changes can trigger new notifications because the app listens to broad document changes rather than event records.

### Target behavior

A notification is an event, not a guess.

Rules:

1. Every meaningful event has one event ID.
2. Every recipient has one recipient-event record.
3. Dismissed means dismissed for that recipient.
4. Read means read for that recipient.
5. A new GPS coordinate is not a notification.
6. A new lifecycle transition is a notification if the user cares.
7. Every notification contains one deep link.
8. Every deep link resolves to the exact order/ticket/transaction.
9. Notification rendering happens once.
10. Notification settings are respected on the server before fanout.

### Event types

- `delivery.received`
- `delivery.queued`
- `delivery.rider_reserved`
- `delivery.rider_assigned`
- `delivery.pickup_started`
- `delivery.arrived_pickup`
- `delivery.picked_up`
- `delivery.in_transit`
- `delivery.arrived_dropoff`
- `delivery.handover_required`
- `delivery.delivered`
- `delivery.exception_opened`
- `delivery.exception_resolved`
- `delivery.cancelled`
- `payment.received`
- `payment.failed`
- `wallet.credited`
- `wallet.debited`
- `refund.started`
- `refund.completed`
- `support.reply`
- `rider.assignment_offer`
- `rider.assignment_reserved`
- `admin.exception_task`
- `admin.settlement_failed`

### Dedupe and clear flow

When a user dismisses:

```text
notificationRecipient.dismissedAt = serverTimestamp
notificationRecipient.state = "dismissed"
```

The client immediately hides it. On next app launch, dismissed/read state is fetched before rendering. If the same event ID returns, it stays hidden. Only a new transition version can create a new visible event.

### Notification UI upgrades

- Use grouped delivery notification cards.
- Show one-line reason and time.
- Swipe/dismiss animation should remove from list immediately.
- Add "Undo" locally for a few seconds if needed.
- "Clear all read" should only clear read items.
- Critical action-needed items remain in an "Action needed" section but should not repop as new alerts.

## 25. Dispatch Intelligence Engine

The dispatch intelligence engine should be explainable, deterministic at first and upgradeable later.

### Inputs

Order inputs:

- pickup coordinates and confidence;
- drop-off coordinates and confidence;
- package size/weight;
- service level;
- payment state;
- queue age;
- customer priority if real;
- required vehicle type;
- pickup time window;
- delivery time window;
- exception flags.

Rider inputs:

- location;
- location freshness;
- online/shift state;
- current job;
- next reservation;
- vehicle type;
- capacity;
- battery/device state if available;
- historical completion reliability;
- current zone;
- account status;
- recent cancellations/declines.

System inputs:

- Benin service zones;
- traffic provider ETA where available;
- historical pickup wait by zone;
- weather if a reliable source is integrated;
- current queue pressure;
- marketplace pickup batching opportunities.

### Outputs

The engine should output:

- recommended action;
- recommended rider;
- reserve-next candidate;
- blocked riders and reasons;
- estimated pickup start;
- ETA range;
- confidence level with missing inputs;
- customer-visible state;
- admin explanation.

Example admin explanation:

```text
Recommend Osas as Reserve Next.
Reason: currently 9-14 min from completing a drop-off near Ring Road, motorcycle fits 2.5 kg parcel, GPS updated 42 sec ago. Two free riders are farther from pickup and one has stale GPS.
```

This is much better than "AI matched 98.6 percent".

## 26. Real-Time Event Bus and Read Models

The UI should not listen to raw broad collections and infer everything itself. Create event records and read models.

### Event log

Every important operation creates an immutable event:

- actor;
- role;
- command;
- target;
- before state;
- after state;
- reason;
- evidence;
- timestamp;
- request ID;
- result.

### Delivery read model

Optimized for current state:

- current public state;
- admin next action;
- rider next action;
- queue rank;
- assigned/reserved rider;
- current blocker;
- ETA range;
- last event;
- payment/proof/settlement summaries.

### Customer read model

Only customer-safe fields:

- package summary;
- public state;
- rider safe profile;
- ETA range;
- proof receipt;
- support link;
- redacted timeline.

### Rider read model

Only rider-needed fields:

- current job;
- next job;
- route leg;
- contacts for current leg;
- proof requirements;
- exception actions;
- earnings summary if approved.

### Admin read model

Operationally dense:

- full order context;
- contacts;
- rider options;
- blockers;
- finance summary;
- support summary;
- internal timeline;
- command availability.

Read models make the app faster, safer and easier to reason about.

## 27. Benin City Operating Intelligence

The app should become excellent at one city before pretending to be national.

### Benin zones

Create owner-approved zones:

- GRA;
- Ring Road/Kings Square;
- Ugbowo/UNIBEN/UBTH;
- Airport Road;
- Sapele Road;
- Upper Sakponba;
- Ikpoba Hill;
- New Benin;
- Uselu;
- Ekenwan/Ekehuan;
- Aduwawa;
- Siluko/Ogida;
- other owner-defined zones.

Each zone can have:

- service availability;
- estimated pickup wait;
- common landmarks;
- rider density;
- road risk notes;
- average delivery duration;
- peak periods.

### Address intelligence

Replace fake coordinate fallback with:

- catalog match;
- geocoder result;
- customer confirmed pin;
- admin resolved pin;
- confidence score;
- last verified date;
- landmark/access note;
- invalid/out-of-zone state.

### Dispatch heat map

Admin should see:

- requests by zone;
- available riders by zone;
- busy riders by expected release zone;
- long-wait orders;
- exception clusters.

This makes Benin specialization feel powerful and local.

## 28. Marketplace Fulfilment Upgrade

Marketplace should not create vague delivery jobs. It should create real fulfilment work.

### Single-vendor order

Flow:

1. Customer checks out.
2. Server reserves stock.
3. Server confirms payment.
4. Vendor receives pick/pack task.
5. Dispatch receives pickup contact and address.
6. Rider is assigned when package is ready or scheduled.
7. Customer sees marketplace fulfilment state and delivery state.

### Multi-vendor order

Options:

- separate deliveries per vendor;
- consolidated pickup route;
- central fulfilment hub;
- scheduled batch pickup.

The chosen model must be explicit. Do not create one fake pickup point for many vendors unless the company truly operates a consolidation center.

### Vendor readiness

Add:

- vendor order accepted;
- preparing;
- ready for pickup;
- pickup window;
- unavailable item;
- substitution/refund request;
- vendor contact;
- proof of pickup from vendor.

This prevents dispatching a rider to collect items that are not ready.

## 29. Proof, Trust and Dispute Upgrade

Proof should become a beautiful, reliable trust layer.

### Proof types

- OTP/challenge;
- receiver signature;
- delivery photo;
- pickup photo;
- package condition photo;
- receiver name confirmation;
- call/contact attempt record;
- GPS/time stamp;
- dispatcher-approved exception.

### Proof states

- not required yet;
- required;
- capturing;
- uploading;
- upload failed;
- stored;
- exception requested;
- exception approved;
- rejected.

### Customer receipt

After delivery, customer sees:

- delivered time;
- rider name;
- proof type;
- receiver confirmation;
- route summary if appropriate;
- payment/receipt;
- report issue.

### Admin dispute pack

Admin sees:

- full timeline;
- rider GPS freshness;
- proof objects;
- contact attempts;
- support messages;
- payment/settlement state;
- actor IDs;
- override reasons.

This turns disputes from guessing into investigation.

## 30. Payment and Settlement Upgrade

Payment should feel invisible to users and exact to finance.

### Customer payment

Add:

- payment intent;
- server quote;
- wallet reservation;
- payment verification;
- failure recovery;
- receipt;
- refund state.

### Rider settlement

Add:

- agreed formula;
- immutable payout snapshot;
- delivery completion event;
- one ledger posting;
- settlement pending state;
- settlement failed state;
- retry without duplicate pay.

### Vendor settlement

Add:

- vendor subtotal;
- commission;
- vendor payout;
- fulfilment state;
- settlement state;
- statement export.

### Admin finance UI

Show:

- booked revenue;
- collected revenue;
- delivery fee revenue;
- marketplace GMV;
- company commission;
- vendor payable;
- rider payable;
- refunds;
- wallet liability;
- unresolved settlement.

Everything should tie to ledger records.

## 31. Support Experience Upgrade

Support should not be a chat floating beside the product. It should be inside the order story.

### Customer support

From an order, customer can choose:

- where is my rider;
- change address;
- cancel request;
- recipient unavailable;
- payment issue;
- item damaged;
- delivery dispute;
- marketplace item issue.

Each creates a structured ticket with the order attached.

### Admin support

Admin sees:

- ticket;
- order state;
- customer messages;
- rider messages;
- current blocker;
- suggested next action;
- refund/proof/payment state;
- internal notes;
- assignment owner.

### Support automation

Use automation carefully:

- summarize the order;
- suggest reply;
- detect urgency;
- route ticket to dispatch/finance/vendor;
- never claim an action unless command succeeded.

## 32. Operational Quality Metrics

Award-winning logistics products measure what matters.

Core metrics:

- median queue time;
- oldest queued order;
- assignment success rate;
- rider acceptance time;
- pickup travel time;
- pickup wait time;
- delivery travel time;
- recipient wait time;
- proof upload success rate;
- GPS freshness rate;
- notification delivery rate;
- exception rate;
- exception resolution time;
- cancellation reason mix;
- refund resolution time;
- settlement success rate.

Do not use vague "AI confidence" as a business metric. Use measurable performance.

## 33. Role-Based Admin Modes

Keep the admin product coherent by role.

### Owner mode

Can see everything:

- dispatch;
- finance;
- riders;
- support;
- marketplace;
- settings;
- audit;
- reports.

### Dispatcher mode

Default to:

- queue;
- active jobs;
- riders;
- exceptions;
- support handoff.

Hide or de-emphasize:

- branding;
- marketplace setup;
- finance adjustment;
- global settings.

### Finance mode

Default to:

- ledger;
- wallet;
- settlements;
- refunds;
- payment failures.

### Support mode

Default to:

- tickets;
- order context;
- customer timeline;
- escalation actions.

Role-based modes can use the same layout but change the default focus and available commands.

## 34. Premium Feel Without Layout Changes

Use the existing design language, but improve the physical quality.

### Texture of interaction

- Cards should not jump; they should settle.
- Lists should preserve scroll and selected item during live updates.
- Buttons should respond instantly.
- Modal/drawer opening should feel anchored to the clicked row.
- Progress should move because events changed, not because time passed randomly.
- Skeletons should match final components.
- Empty states should be specific and calm.

### Text quality

Replace hype with clarity:

- "GPS updated 45 sec ago" instead of "Live precision tracking".
- "Rider reserved after current delivery" instead of "Rider assigned" when busy.
- "Address needs confirmation" instead of fake geocode success.
- "Proof upload failed, retrying" instead of "Delivery updated".
- "Payment received, wallet credit pending" instead of generic network error.

### Sound and haptics

Use sparingly:

- booking received;
- rider assigned;
- proof accepted;
- delivery completed;
- admin new urgent request.

Do not play sounds for every GPS/progress update.

## 35. Offline and Weak-Network Upgrade

Benin field operations need weak-network resilience.

### Customer offline

- save draft locally;
- show not yet received by ESDispatch;
- retry in background;
- notify when server receives request.

### Rider offline

- allow viewing current job details;
- queue proof uploads locally;
- queue non-sensitive event intents with expiration;
- never fake server completion;
- show pending sync clearly.

### Admin poor connection

- show listener health;
- disable unsafe commands when stale;
- keep draft notes/replies locally until sent;
- show last successful sync time.

### Conflict handling

If server rejects a stale command:

- do not show generic error;
- show what changed;
- offer the new legal next action.

## 36. Data Migration Strategy

The app has existing deliveries with old statuses and fields. Upgrade without breaking them.

Steps:

1. Add new fields alongside old status.
2. Create compatibility read model for old orders.
3. Map old statuses to new lifecycle states.
4. Prevent old clients from writing sensitive fields.
5. Release admin support for both old/new records.
6. Release mobile app support for both old/new records.
7. Migrate active orders carefully.
8. Archive or lock terminal old orders.
9. Remove old direct-write paths after adoption.

Old status mapping example:

- `PENDING` -> received or queued depending on rider/payment/address.
- `ASSIGNED` -> assigned or reserved depending on rider acceptance.
- `PICKED_UP` -> picked up.
- `TRANSIT` -> in transit.
- `OUT_FOR_DELIVERY` -> in transit or arrived drop-off depending on event.
- `ARRIVED` -> ambiguous; require context.
- `DELIVERED` -> delivered only if proof/settlement backfill is acceptable.
- `CANCELLED` -> cancelled with unknown reason unless reason exists.

## 37. Testing Strategy

The upgrade needs tests that follow real delivery life, not only model defaults.

### Contract tests

Test backend commands:

- invalid actor rejected;
- stale version rejected;
- illegal transition rejected;
- duplicate command idempotent;
- proof required;
- settlement posts once;
- notification event dedupes.

### Emulator tests

Run customer/rider/admin flows against local Firebase emulator:

- booking;
- queue;
- reserve-next;
- assignment;
- pickup;
- proof;
- delivery;
- settlement;
- cancellation;
- exception.

### Device tests

Release APK on physical device:

- GPS permission denied;
- GPS stale;
- network off during proof upload;
- notification tap cold start;
- notification clear/relaunch;
- customer tracking active delivery;
- rider current/next job.

### Admin tests

Browser tests:

- dashboard count opens exact list;
- order drawer stays open during live updates;
- assignment conflict;
- bulk review;
- support reply draft failure;
- notification clearing.

## 38. Build Guardrails for Future AI Agents

Future work should follow these product rules:

1. Do not add a status without defining actor, transition, customer wording and backend command.
2. Do not add a notification without dedupe and deep link.
3. Do not add AI copy without real inputs and explanation.
4. Do not add route motion without GPS freshness.
5. Do not add finance numbers without ledger source.
6. Do not add admin controls without role/capability enforcement.
7. Do not add marketplace delivery without real pickup identity.
8. Do not add customer promise text without server state.
9. Do not add rider action without exception/retry behavior.
10. Do not change layout/brand to cover missing state.

These rules keep the product timeless instead of accumulating shiny but disconnected features.

## 39. Upgrade Backlog

### P0 upgrades

- Server-owned delivery command API.
- Secure Firestore rules.
- Notification event/dedupe system.
- Remove production GPS simulation writes.
- Server OTP/challenge and proof-before-delivery.
- Idempotent settlement ledger.
- Payment intent and verified top-up.
- Public tracking redaction.

### P1 upgrades

- FIFO queue.
- Rider availability/capacity model.
- Reserve-next/preassignment.
- Admin dispatch drawer.
- Customer queued/reserved states.
- Rider current/next job separation.
- Benin service-zone validation.
- Address confidence and review.
- Assignment conflict handling.
- Exception workflows.

### P2 upgrades

- ETA range and freshness.
- Route optimization.
- Marketplace fulfilment tasks.
- Support order context.
- Role-based admin modes.
- Finance dashboards.
- Widget upgrades.
- Offline proof retry.
- Rider shift/inspection gating.
- Audit event viewer.

### P3 upgrades

- Polished motion pass.
- Better skeleton loaders.
- Sound/haptic tuning.
- Icon and chip consistency.
- Copy cleanup.
- Empty state improvements.
- Microcopy for delays.
- Admin keyboard shortcuts.
- Export/report polish.

## 40. Final Upgrade Brief

Build the next version as a premium Benin City dispatch operating system.

The app should feel alive because real events are flowing through it. A request enters the queue. A rider becomes available. The oldest eligible order moves. A busy rider is reserved next. The customer sees that truth. The rider sees the next action. Admin sees the blocker. A notification appears once. A proof upload succeeds. Settlement posts once. Support can explain the full story.

That is the breakthrough.

Keep the layout. Keep the brand. Keep the premium dark/gold language. But replace fragile local state, fake confidence and disconnected screens with a shared lifecycle, intelligent dispatch, truthful tracking, reliable notification events and ledger-backed operations.

The product will feel soft, fluid and sweet when the underlying truth is solid. The best polish is not extra decoration. It is the feeling that every tap, card, notification, rider marker and status change is connected to the real business.
