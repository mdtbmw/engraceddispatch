# ESDispatch Component Polish Plan

Date: 2026-09-09  
Purpose: Turn the micro-UI audit into a practical component refactor plan for customer mobile, rider mobile, and admin web. This plan does not propose changing the locked ESDispatch brand identity, logo, slogan, or core layout direction. It improves the quality, consistency, resilience, and liveliness of the existing interface.

## 1. Product-quality goal

The goal is to make ESDispatch feel composed at every touchpoint. The app should still look like ESDispatch: black/gold premium logistics, rounded cards, floating bottom dock, bold identity, and luxury tone. The upgrade is about making the interface smarter and more deliberate.

The current UI has three main component problems:

1. Similar components are built separately in many screens.
2. Information-heavy cards do not have a strong internal grid.
3. Operational state is shown as loose labels instead of a clear lifecycle.

This plan fixes the system underneath the visible UI. It should reduce repeated redesign work, improve usability, and make future feature changes safer.

## 2. Non-negotiable polish principles

- Keep the locked brand identity: ESDispatch / ESDISPATCH, gold, obsidian, premium dispatch language, and existing brand logo.
- Preserve the broad layout direction. Improve composition, spacing, states, motion, and data resilience without replacing the whole UI.
- Make status truthful. Never show a rider-dependent state unless rider assignment/preassignment data supports it.
- Design for Benin City data. Long addresses, real landmarks, local phone numbers, variable route names, and mixed service types must not break cards.
- Make every tappable control visibly tappable and comfortably touchable.
- Use motion as feedback. Motion should make the product feel alive without distracting from operations.
- Fix shared components before fixing one screen at a time.

## 3. Phase 1 - Establish foundation tokens

Before refactoring cards, lock the small design decisions that every component depends on.

### 3.1 Spacing tokens

Create a shared spacing scale for mobile and a matching CSS variable scale for web admin.

| Token | Mobile | Web | Use |
|---|---:|---:|---|
| `space.1` | 4dp | 4px | icon/text micro gaps |
| `space.2` | 8dp | 8px | chip padding, compact vertical rhythm |
| `space.3` | 12dp | 12px | card inner group spacing |
| `space.4` | 16dp | 16px | default card padding |
| `space.5` | 20dp | 20px | section internal spacing |
| `space.6` | 24dp | 24px | screen horizontal padding |
| `space.8` | 32dp | 32px | section separation |
| `space.10` | 40dp | 40px | large header/body separation |

Rules:

- Screen horizontal padding should normally be 24dp/24px.
- Card inner padding should normally be 16dp, 20dp, or 24dp based on density.
- Card-to-card spacing should be 12dp for related list items and 16dp when cards need stronger separation.
- Avoid odd one-off values unless a shape requires them.

### 3.2 Typography tokens

Create semantic text styles rather than local font sizes scattered across components.

| Token | Mobile suggestion | Web suggestion | Use |
|---|---|---|---|
| `displayMetric` | 28sp / heavy | 30-34px / black | admin/customer metric values |
| `screenTitle` | 24-28sp / extra bold | 28-32px / extra bold | screen headings |
| `sectionTitle` | 20-22sp / extra bold | 20-24px / extra bold | list section titles |
| `cardTitle` | 15-17sp / bold | 15-17px / bold | parcel/product/card title |
| `body` | 14sp / medium | 14px / medium | standard readable text |
| `bodySmall` | 12-13sp / medium | 12-13px / medium | metadata and supporting text |
| `caption` | 11-12sp / semi-bold | 11-12px / semi-bold | badges and compact metadata |
| `micro` | 10-11sp only | 10-11px only | non-critical labels, never core status/action |

Rules:

- Core status labels should not be 9sp.
- Route text should not rely on tiny semi-bold text to carry meaning.
- Use bold weight to indicate hierarchy, not everywhere.
- Price should use tabular numerals where possible.

### 3.3 Radius tokens

| Token | Value | Use |
|---|---:|---|
| `radius.sm` | 8dp/px | compact badges, tiny controls |
| `radius.md` | 12dp/px | chips, inputs, small image frames |
| `radius.lg` | 16dp/px | buttons, compact cards |
| `radius.xl` | 20dp/px | product cards, modal controls |
| `radius.2xl` | 24dp/px | major cards |
| `radius.3xl` | 28-32dp/px | hero cards, bottom sheets, dashboard panels |
| `radius.nav` | custom | bottom capsule dock only |

Rules:

- Do not use large radius everywhere by default.
- Dense admin rows should not inherit the same radius as hero/product cards.
- Badges should look like badges, not tiny cards.

### 3.4 Elevation tokens

Use low, purposeful elevation.

- `elevation.none`: flat list cards and dense operational rows.
- `elevation.soft`: modals, floating cart bar, bottom sheets.
- `elevation.raised`: rare, for active floating surfaces.
- Avoid heavy shadow on admin metrics and modals unless the background needs separation.

### 3.5 Color tokens

Align mobile and admin tokens.

- `brand.gold`: official gold.
- `brand.goldDark`: pressed/gradient companion.
- `surface.app`: light/dark adaptive card surface.
- `surface.page`: light/dark adaptive background.
- `text.primary`, `text.secondary`, `text.muted`.
- `status.received`, `status.queued`, `status.reserved`, `status.assigned`, `status.transit`, `status.arrived`, `status.delivered`, `status.cancelled`, `status.issue`.

Rules:

- Do not use gold text on light card surfaces for important copy.
- Do not use white text on gold.
- Do not scatter arbitrary gold hex values across mobile/admin.

## 4. Phase 2 - Build shared primitives

### 4.1 `StatusBadge`

One component should render status everywhere. It should support:

- Size: `compact`, `default`, `large`.
- Tone: received, queued, reserved, assigned, transit, arrived, delivered, cancelled, issue.
- Optional icon.
- Optional short explanation on admin hover/tap.
- Light/dark safe colors.

Acceptance:

- Same delivery state has same visual meaning across customer, rider, and admin.
- The customer never sees a vague "Pending" when the operational truth is "Received", "Queued", or "Waiting for rider".

### 4.2 `RouteDisplay`

Replace concatenated route strings with a structured component.

Variants:

- `compact`: one-line pickup/dropoff preview, used in dense admin rows.
- `card`: two clear endpoint blocks, used in delivery cards.
- `timeline`: progress rail with route stages, used when status progression matters.
- `detail`: full pickup/dropoff/contact/map actions, used in drawers and detail screens.

Required behavior:

- Separate pickup and dropoff.
- Max line rules.
- Expand for full address.
- Copy/open-map actions where useful.
- Missing address fallback.

Acceptance:

- Long Benin City addresses do not destroy layout.
- Pickup and dropoff are never merged into a hard-to-scan single sentence.

### 4.3 `PriceDisplay`

One money display component for NGN.

Variants:

- `compact`: no decimals for customer list/product cards.
- `standard`: grouped amount with decimals where needed.
- `admin`: tabular numerals, aligns in columns.
- `muted`: supporting fee/tip/subtotal.

Acceptance:

- Amounts align consistently.
- Large amounts do not wrap awkwardly.
- Admin financial values can be scanned column by column.

### 4.4 `IconActionButton`

Unify icon-only actions.

Required behavior:

- Visual icon 18-24dp.
- Touch target at least 44dp.
- Press feedback.
- Disabled state.
- Loading state where needed.
- Optional badge/dot.
- Accessible label.

Use for:

- Copy tracking ID.
- Open map/pin.
- Notifications.
- Favorite.
- Cart.
- More/menu.
- Admin row actions.

Acceptance:

- Every icon-only control has a predictable hit area and state.

### 4.5 `ActionButton`

Standardize filled, tonal, ghost, destructive, and text actions.

Variants:

- `primary`: gold fill with obsidian text.
- `secondary`: neutral surface with strong text.
- `ghost`: transparent text/icon.
- `danger`: red/destructive.
- `success`: completion/confirmation.

States:

- default
- pressed
- focused
- disabled
- loading
- success
- error

Acceptance:

- Admin quick actions, mobile CTAs, bottom sheet buttons, and form buttons use the same action language.

### 4.6 `SectionHeader`

Shared section heading with optional action.

Use for:

- Active Deliveries
- Recent History
- Recommended Shops
- Marketplace sections
- Admin panels

Acceptance:

- Section titles and "View All" actions align consistently.
- View-all actions never look like random chips.

### 4.7 `EmptyStateCard`

Shared empty states.

Slots:

- illustration/icon
- title
- explanation
- primary action
- secondary action
- optional support link

Acceptance:

- Empty active deliveries, empty favorites, empty marketplace, empty notifications, and empty admin lists all feel from the same product.

### 4.8 `LoadingSkeleton`

Skeleton variants should mirror actual components:

- `DeliveryCardSkeleton`
- `HistoryCardSkeleton`
- `ProductCardSkeleton`
- `AdminRowSkeleton`
- `AdminMetricSkeleton`

Acceptance:

- Loading does not cause major layout shift when real data appears.

## 5. Phase 3 - Redesign key cards

### 5.1 `DeliveryCard` for active customer shipments

Current issue:

The active card spreads information across a large surface without a strong grid. Route status, package icon, ID, copy icon, status text, and CTA compete.

Recommended structure:

```text
[Package type + ID]                         [StatusBadge]
[Created time / service speed]          [Utility icons]

[RouteDisplay card variant]
From: pickup address
To:   delivery address

[Progress rail: Received -> Queued/Assigned -> In Transit -> Arrived -> Delivered]

[Rider preview or queue message]
[Primary action: Track details]
```

Rules:

- The card should not center the ID as the dominant object.
- The package illustration should never sit on the card edge.
- Status badge belongs in the top-right.
- Route endpoints should have labels.
- Progress rail should show meaningful stages only.
- Customer-visible state should change when the delivery is queued, reserved, assigned, and in transit.

Acceptance:

- On a 360px wide screen, two active shipments can be viewed without visual clipping.
- Long pickup/dropoff addresses remain legible.
- The customer understands what happens next.

### 5.2 `HistoryOrderCard`

Current issue:

Recent history cards reserve space poorly, route text is squeezed, amount/status compete, and the rebook CTA feels detached.

Recommended structure:

```text
[Express Parcel]                           [NGN 11,200]
[ID PC-43985 · Today]                      [Delivered badge]

[RouteDisplay compact/card variant]
Ring Road -> G.R.A, 17 Upper Adesuwa...

[Rebook route] [View receipt]
```

Rules:

- Remove image slot if no image is meaningful.
- Use the full card width for route text below the top row.
- Keep rebook as a secondary action attached to the grid.
- Delivered badge should be readable.

Acceptance:

- Three history cards can scan cleanly without each one feeling like a mini form.
- Price and status remain aligned.

### 5.3 `AdminDispatchRequestCard`

Current issue:

Admin active request surfaces do not give enough information or inline controls to make fast decisions.

Recommended structure:

```text
[Queue #03] [Received 12m ago]             [StatusBadge]
[Customer name + phone]                    [Service type]
Pickup: ...
Dropoff: ...
Package: item, size/weight, payment state
Rider: unassigned / reserved / assigned
Availability: 2 riders nearby, 1 free now, 1 completing job

[Assign rider] [Reserve next available] [Queue] [Call] [Open map]
```

Rules:

- Default sort is queue/SLA priority.
- Every action is valid for the current state.
- The drawer/card should allow dispatch without forcing tab switching.

Acceptance:

- Admin can decide from the drawer in one pass.
- Admin can assign/reserve/queue/contact without losing context.

### 5.4 `RiderMissionCard`

Recommended structure:

```text
[Current mission / Next reserved job]
[StatusBadge]
Pickup / Dropoff
Customer contact
Package notes
Allowed next action only
```

Rules:

- Rider sees only authorized next statuses.
- Next reserved job is visually separate from current mission.
- The card should support offline/online and arrival prompt states.

Acceptance:

- Rider cannot accidentally act on the wrong mission.

### 5.5 `MarketplaceProductCard`

Current issue:

The product card has useful interactions, but title, vendor, rating, price, favorite, and cart actions need stronger state and density rules.

Recommended structure:

```text
[Image] [Product title]                 [Price]
        [Vendor / store]                [Stock badge]
        [Rating or New]
        [Favorite] [Add]
```

Rules:

- Image placeholder must look intentional.
- Missing rating should say "New" or hide rating.
- Favorite/cart hit targets should be at least 44dp.
- Swipe gestures are optional shortcuts, not primary UX.

Acceptance:

- Product card still feels compact but no action feels tiny.

## 6. Phase 4 - Admin control center polish

### 6.1 Replace dashboard pop-ups with dispatch decision drawers

The admin should not click an active request, see a shallow pop-up, jump to another tab, search again, and then update status. That flow is broken for real operations.

Build a `DispatchDecisionDrawer`:

- Full order summary.
- Customer contact.
- Pickup and dropoff.
- Package/payment/SLA.
- Rider availability.
- Suggested rider assignment.
- Queue position.
- Status timeline.
- Valid next actions.
- Audit log preview.

Action rules:

- If no rider is free, show "Queue request" and "Reserve next available".
- If rider is currently busy, show "Preassign after current mission".
- If rider is assigned, show customer-visible rider card.
- If rider action is required, admin sees status but cannot impersonate rider-only steps unless explicitly allowed by a supervisor override.

### 6.2 Upgrade active bookings list

Replace dense table rows with operational rows:

- Queue rank.
- SLA/age.
- Service type.
- Pickup zone.
- Dropoff zone.
- Rider state.
- Next action.

Sorting:

1. Urgent SLA breach risk.
2. FCFS inside priority group.
3. Service tier.
4. Rider availability match.

### 6.3 Fix admin toasts

Toasts should:

- dedupe by event key
- auto-expire non-critical messages
- persist dismissed IDs for repeated live updates
- support manual close
- never cover critical controls
- group repeated events: "5 marketplace products updated"

### 6.4 Reduce admin visual noise

Tone down:

- hover scale on metric cards
- heavy shadows on routine panels
- excessive tiny uppercase gold labels
- one-off rounded values
- repeated animation classes on dense lists

Keep motion for:

- successful save
- failed save
- new request arrival
- status change
- drawer open/close
- dispatch assignment confirmation

## 7. Phase 5 - Customer dashboard polish

### 7.1 Header and safe-area repair

Tasks:

- Measure header height instead of guessing from scroll progress.
- Add reliable content top inset.
- Test expanded/collapsed header at common device heights.
- Prevent clipped filters and clipped previous card content.
- Make notification icon and avatar respect safe-area.

Acceptance:

- First visible content starts below the header on all tested devices.

### 7.2 Active shipment card rewrite

Tasks:

- Use `DeliveryCard`.
- Use `RouteDisplay`.
- Use unified status badges.
- Add customer-friendly queue/rider messages.
- Move utility icons to one action cluster.
- Make CTA row more substantial and clearly tappable.

Acceptance:

- Active card feels readable, not oversized.

### 7.3 Recent history rewrite

Tasks:

- Use `HistoryOrderCard`.
- Remove meaningless image slot when no image is useful.
- Put route on full-width row.
- Align amount and delivered badge.
- Move rebook into card action row.

Acceptance:

- Cards scan cleanly in a list.

### 7.4 Bottom dock polish

Tasks:

- Rename "Order" to "Orders" if layout permits.
- Ensure labels are readable.
- Reserve proper bottom content inset.
- Gate breathing pulse.
- Add selected indicator travel/settle motion if not already fully implemented.

Acceptance:

- Last card scrolls above nav and nav does not cover list content.

## 8. Phase 6 - Rider view polish

### 8.1 Current mission card

Create a rider card with:

- current mission
- pickup/dropoff
- customer contact
- proof of pickup/delivery status
- allowed next action
- issue reporting

### 8.2 Next reserved mission

When admin preassigns a rider:

- Rider sees "Reserved next" after current mission.
- Customer sees "Rider reserved" or "Queued for rider" until active assignment begins.
- Admin sees the transition reason.

### 8.3 Authorized status action strip

Rider should see only valid next actions:

- Accept
- Heading to pickup
- Picked up
- In transit
- Arrived
- Delivered
- Report issue

The UI should not present illegal jumps.

## 9. Phase 7 - Notification and state lifecycle

Create a unified notification state model:

- `eventId`
- `eventType`
- `entityId`
- `severity`
- `createdAt`
- `readAt`
- `dismissedAt`
- `expiresAt`
- `actionUrl`
- `dedupeKey`

Behavior:

- Closing a notification persists dismissal.
- Live updates do not revive dismissed notifications unless the event changes.
- Critical events can reappear only with changed severity or new action.
- Mobile and admin share event types.

Acceptance:

- No notification pops again and again after closing.

## 10. Phase 8 - Motion language

Use the existing interaction physics rules, but apply them with restraint.

### 10.1 Frame-zero touch response

Apply to:

- primary buttons
- bottom nav items
- card CTAs
- icon buttons
- segmented controls

### 10.2 Meaningful state animations

Use animation for:

- new active request appears
- rider assigned
- request moves from queued to assigned
- card archived
- notification dismissed
- payment confirmed
- delivery completed

Avoid animation for:

- every hover
- dense admin row load
- static metrics after initial mount

### 10.3 Reduced motion

Respect reduced-motion preference:

- turn pulse into static glow or subtle opacity
- remove non-essential bounce
- keep state changes immediate

## 11. Phase 9 - Dynamic-data fixtures

Create preview/test fixtures for each key component.

### Delivery card fixtures

- short pickup/dropoff
- very long Benin City pickup/dropoff
- missing rider
- queued because no rider available
- reserved rider
- assigned rider with photo
- rider in transit
- delivered
- cancelled
- issue reported
- huge price
- long customer name

### Admin drawer fixtures

- no rider available
- one rider online and free
- rider busy with next reservation
- delivery missing phone
- delivery missing address
- SLA breach
- manual supervisor override
- cancelled request

### Marketplace fixtures

- missing image
- sold out
- low stock
- no rating yet
- long product title
- long vendor name
- high price
- item in cart
- favorite selected

### Notification fixtures

- dismissible info
- urgent action
- duplicate event
- resolved event
- expired event

## 12. Phase 10 - QA checklist

Every polished component must pass:

- Light mode.
- Dark mode.
- Small phone width.
- Tall phone height.
- Large font.
- Long address.
- Missing optional data.
- Loading state.
- Empty state.
- Error state.
- Disabled state.
- Pressed state.
- Success state.
- Reduced motion.
- Keyboard/focus where web/admin.
- Screen reader label for icon-only actions.

## 13. Implementation order

### Sprint 1: stop visible breakage

1. Header/content overlap fix.
2. Bottom dock content inset fix.
3. Notification dedupe/dismiss lifecycle.
4. Status transition guards.
5. Admin pop-up to dispatch drawer baseline.

### Sprint 2: shared primitives

1. StatusBadge.
2. RouteDisplay.
3. PriceDisplay.
4. IconActionButton.
5. ActionButton.
6. EmptyStateCard.
7. LoadingSkeleton.

### Sprint 3: card rewrites

1. Active customer DeliveryCard.
2. Recent HistoryOrderCard.
3. Admin DispatchRequestCard.
4. Rider MissionCard.
5. MarketplaceProductCard.

### Sprint 4: polish and motion

1. Touch feedback across actions.
2. Card insertion/removal animations.
3. Rider assignment state transition animation.
4. Notification dismissal animation.
5. Reduced-motion support.

### Sprint 5: QA and cleanup

1. Remove duplicated components.
2. Remove arbitrary colors/spacings.
3. Add previews/fixtures.
4. Verify on physical Android device.
5. Verify admin in browser across desktop/tablet widths.

## 14. Component acceptance matrix

| Component | Must be shared? | Highest priority state | Main risk |
|---|---|---|---|
| DeliveryCard | Yes | queued/assigned/transit | unclear order state |
| HistoryOrderCard | Yes | delivered/rebook | wasted space and weak hierarchy |
| RouteDisplay | Yes | long addresses | broken wrapping |
| StatusBadge | Yes | all lifecycle states | inconsistent truth |
| RiderIdentityCard | Yes | reserved/assigned | customer/admin mismatch |
| DispatchDecisionDrawer | Admin shared | no rider available | slow admin workflow |
| IconActionButton | Yes | pressed/disabled | tiny hit targets |
| ActionButton | Yes | loading/disabled | mismatched importance |
| EmptyStateCard | Yes | no data | placeholder feel |
| Toast/Notification | Yes | dismissed/duplicate | repeated popups |
| BottomNav | Yes | selected/scroll | content collision |
| MarketplaceProductCard | Yes | missing image/sold out | low trust marketplace |

## 15. Definition of done

A component is polished only when:

- It uses shared tokens.
- It has documented states.
- It survives long real data.
- It has a readable hierarchy.
- Its actions have correct touch targets.
- Its color pairings obey brand rules.
- It works in light and dark mode.
- It has preview fixtures.
- It has no runtime clipping against headers, sheets, or bottom nav.
- It uses motion only where motion improves feedback.

## 16. Final recommendation

Do not fix the screenshots directly as isolated bugs. Use them as the proof that ESDispatch needs a stronger component system. The fastest path to a premium product is to repair the shared card, route, status, action, notification, and admin dispatch primitives first, then replace the one-off screen layouts with those primitives.

That is how the app becomes fluid and alive without changing the whole layout.
