# ESDispatch Design System Recommendations

Date: 2026-09-09  
Scope: Practical design-system rules for improving the existing ESDispatch mobile app and admin dashboard without changing the locked brand identity or replacing the current layout direction.

## 1. Design-system purpose

ESDispatch already has a recognizable identity: dark premium surfaces, gold accents, large rounded forms, and a dispatch/logistics tone. The missing layer is a strict component system that makes every screen feel intentionally made by the same product team.

This document defines the recommended rules for tokens, reusable components, state design, layout rhythm, motion, responsive behavior, and future UI governance.

The system should make the product feel:

- premium
- operationally clear
- fast to scan
- alive without being noisy
- trustworthy with real data
- consistent between customer, rider, and admin views

## 2. Brand constraints to preserve

These rules must remain locked:

- Brand name: ESDispatch or ESDISPATCH.
- Official slogan: PREMIUM LOGISTICS & DISPATCH.
- Logo asset: do not replace, wrap, recolor outside approved contexts, or redesign.
- Gold is a brand accent and must stay premium, not overused.
- Obsidian/dark luxury surfaces are part of the identity.
- Do not use white text directly on gold.
- Do not use gold text directly on white/light card surfaces for important labels.
- Do not redesign the overall layout direction. Improve composition and quality within the existing product shape.

## 3. Design-system architecture

Create a small system with three layers:

1. **Tokens:** colors, typography, spacing, radius, elevation, icon sizes, motion.
2. **Primitives:** text, surface, badge, button, icon action, input, divider, sheet, modal.
3. **Product components:** delivery card, route display, rider identity, dispatch drawer, history card, marketplace product card, notification row.

The app should avoid screen-local design decisions for repeated UI. If a component appears in three places, it should probably be shared or built from shared primitives.

## 4. Color tokens

### 4.1 Core tokens

| Token | Role |
|---|---|
| `brand.gold` | Primary brand accent and filled primary action background |
| `brand.goldPressed` | Pressed/active state for gold controls |
| `brand.goldSoft` | Subtle highlight on dark surfaces only |
| `brand.obsidian` | Main dark brand surface and text on gold |
| `surface.page` | App page background, adaptive light/dark |
| `surface.card` | Card background, adaptive light/dark |
| `surface.raised` | Bottom sheets, dialogs, floating bars |
| `border.subtle` | Low-contrast card and row borders |
| `text.primary` | Main readable text |
| `text.secondary` | Supporting metadata |
| `text.muted` | Low-emphasis helper text |
| `text.inverse` | Text on dark surfaces |

### 4.2 Status tokens

Status should be a product language, not a random color decision.

| Status | Customer label | Admin/rider label | Tone |
|---|---|---|---|
| `received` | Received | Received | neutral/gold-accent |
| `queued` | Queued | Waiting for rider | amber |
| `reserved` | Rider reserved | Reserved next | purple/blue |
| `assigned` | Rider assigned | Assigned | blue |
| `pickup` | Heading to pickup | Pickup phase | indigo |
| `transit` | In transit | In transit | gold/blue |
| `arrived` | Rider arrived | Arrived | teal |
| `delivered` | Delivered | Delivered | green |
| `cancelled` | Cancelled | Cancelled | red/neutral |
| `issue` | Needs attention | Issue | red |

Rules:

- Same status must use the same label family across customer, rider, and admin.
- Do not show "Pending" for every early state. It is too vague.
- If no rider is available, customer-facing state should say queued/received, not appear stuck.
- If a rider is preassigned, customer-facing state should explain when it becomes active.

## 5. Typography system

### 5.1 Mobile text scale

| Token | Size | Weight | Use |
|---|---:|---|---|
| `display` | 28sp | Black | major amount/metric |
| `screenTitle` | 24sp | ExtraBold | screen title |
| `sectionTitle` | 20sp | ExtraBold | dashboard sections |
| `cardTitle` | 16sp | Bold/ExtraBold | parcel/product/card name |
| `body` | 14sp | Medium | normal readable copy |
| `bodySmall` | 12-13sp | Medium/SemiBold | metadata |
| `caption` | 11-12sp | SemiBold/Bold | badges, short labels |
| `micro` | 10-11sp | Medium | rare non-critical helper labels |

### 5.2 Web admin text scale

| Token | Size | Use |
|---|---:|---|
| `adminMetric` | 30-34px | metric values |
| `adminPageTitle` | 28-32px | page header |
| `adminPanelTitle` | 18-22px | panel titles |
| `adminRowTitle` | 14-16px | delivery/customer/rider names |
| `adminBody` | 13-14px | standard body |
| `adminMeta` | 11-12px | metadata |

Rules:

- Avoid 9px/sp for status labels.
- Use line height deliberately: addresses need comfortable line height.
- Avoid overusing extra-bold. If everything is bold, nothing is prioritized.
- Use tabular numbers for prices, queue counts, dates, and admin metrics.

## 6. Spacing system

| Token | Value | Use |
|---|---:|---|
| `xs` | 4 | tiny icon/text gaps |
| `sm` | 8 | compact inner spacing |
| `md` | 12 | card group spacing |
| `lg` | 16 | default component padding |
| `xl` | 20 | roomy card padding |
| `2xl` | 24 | screen margins and major cards |
| `3xl` | 32 | section spacing |
| `4xl` | 40 | hero/header separation |

Rules:

- Use 24dp mobile horizontal page padding unless a component has a strong reason not to.
- Use 16dp or 20dp inner card padding for most cards.
- Use 12dp list item spacing for related cards.
- Use 24-32dp section spacing.
- Avoid random values such as 7, 13, 19, and 27 unless they are solving a known optical alignment issue.

## 7. Radius system

| Token | Value | Use |
|---|---:|---|
| `sm` | 8 | tiny badges |
| `md` | 12 | chips and small controls |
| `lg` | 16 | buttons and inputs |
| `xl` | 20 | compact cards |
| `2xl` | 24 | primary cards |
| `3xl` | 28-32 | bottom sheets, hero surfaces |
| `custom.nav` | custom | bottom capsule dock |

Rules:

- Use radius to communicate component type.
- Dense operational admin rows should not look like hero cards.
- Badges should be compact and legible, not miniature panels.

## 8. Elevation system

Use elevation sparingly.

| Token | Use |
|---|---|
| `none` | standard list cards, flat premium surfaces |
| `bordered` | most cards on light backgrounds |
| `soft` | floating bars, modals, bottom sheets |
| `active` | temporary elevated active surfaces |

Rules:

- Avoid muddy drop shadows under product cards and routine admin panels.
- Elevation should communicate layer, not decoration.
- In dark mode, use border and surface contrast more than shadow.

## 9. Icon system

### 9.1 Sizes

| Token | Size | Use |
|---|---:|---|
| `icon.xs` | 12 | metadata icons |
| `icon.sm` | 16 | labels and small buttons |
| `icon.md` | 20 | standard actions |
| `icon.lg` | 24 | primary icon buttons |
| `icon.xl` | 28-32 | hero/central action |

### 9.2 Rules

- Icon-only controls need at least 44dp/44px touch area.
- Icons must match the action. A package icon should not be used for order history if "Orders" is the mental model.
- Avoid mixing icon families with different stroke weights in the same component.
- Active, inactive, selected, disabled, and loading states must be visually distinct.

## 10. Motion system

Motion should make the app feel alive and responsive. It should not make it feel busy.

### 10.1 Motion tokens

| Token | Feel | Use |
|---|---|---|
| `touchPress` | quick compression/release | buttons, icon actions, nav items |
| `softSettle` | calm spring | cards, drawers, bottom sheets |
| `snappyPill` | controlled snap | segmented filters, nav indicator |
| `rubberBand` | elastic edge | overscroll, geofence boundary |
| `signatureMoment` | branded pulse | dispatch assigned, delivery completed, arrival prompt |

### 10.2 Required motion moments

- Button press feedback.
- Favorite selection feedback.
- Cart add confirmation.
- Notification dismiss.
- New active request appears in admin.
- Rider assigned/reserved.
- Delivery moves to next lifecycle stage.
- Bottom sheet/drawer opening and closing.

### 10.3 Motion restraint

- Pause pulsing effects during scrolling.
- Respect reduced-motion settings.
- Avoid hover scale on dense admin metrics/lists.
- Do not animate every static card.

## 11. Layout grids

### 11.1 Customer active delivery card grid

Recommended layout:

```text
Top row:
[Package type + ID]                         [StatusBadge]

Meta/action row:
[Created time / service]                    [Copy] [Map]

Route block:
[From label] pickup address
[To label]   delivery address

Lifecycle:
Received -> Queued/Assigned -> In Transit -> Arrived -> Delivered

Rider/queue message:
[Rider profile preview OR queue explanation]

Footer:
[Track Delivery Details]
```

Rules:

- Do not center the tracking ID as the main anchor.
- Do not float a decorative package image outside the grid.
- Keep status in one stable place.
- Give route text enough width.
- Hide or collapse decorative elements when data is dense.

### 11.2 Recent history card grid

Recommended layout:

```text
[Parcel type/title]                         [Amount]
[ID + date]                                 [Delivered badge]

[RouteDisplay compact]
Pickup -> Dropoff

[Rebook route]        [Receipt/details]
```

Rules:

- Remove unused image space unless the image has real meaning.
- Let route use full width below the summary row.
- Keep rebook as a secondary action.
- Delivered badge must be readable.

### 11.3 Admin dispatch request drawer grid

Recommended layout:

```text
[Queue #] [Age/SLA]                         [StatusBadge]
[Customer]                                  [Service tier]
[Phone/WhatsApp]

[Pickup]
[Dropoff]
[Package]
[Payment]

[Rider availability]
[Suggested rider / Reserved rider / Assigned rider]

[Valid next actions]
[Audit log]
```

Rules:

- The drawer must be decision-ready.
- Do not force the admin to jump tabs for routine dispatch actions.
- Show why a rider is suggested.
- Show why a request is queued.

### 11.4 Rider mission card grid

Recommended layout:

```text
[Current Mission]                           [StatusBadge]
[Pickup]
[Dropoff]
[Customer contact]
[Proof/OTP requirement]
[Allowed next action]
[Issue report]
```

Rules:

- Current mission and next reserved mission must look different.
- Rider sees only valid next actions.
- Safety-critical instructions should be clear and short.

## 12. Component library recommendations

### 12.1 Mobile shared components

Create or harden:

- `AppScreenScaffold`
- `ScreenHeader`
- `SectionHeader`
- `DeliveryCard`
- `HistoryOrderCard`
- `RouteDisplay`
- `StatusBadge`
- `RiderIdentityCard`
- `PriceDisplay`
- `IconActionButton`
- `ActionButton`
- `SegmentedFilterBar`
- `EmptyStateCard`
- `LoadingSkeleton`
- `NotificationBanner`
- `BottomSheetFrame`
- `FormSectionCard`
- `AddressField`

### 12.2 Admin shared components

Create or harden:

- `AdminPageShell`
- `AdminPanel`
- `AdminMetricCard`
- `DispatchRequestCard`
- `DispatchDecisionDrawer`
- `AdminStatusBadge`
- `AdminRouteDisplay`
- `AdminActionButton`
- `AdminIconButton`
- `AdminSelect`
- `AdminSearchInput`
- `AdminToastStack`
- `AdminDataTable`
- `AdminEmptyState`
- `AdminAuditTimeline`

### 12.3 Cross-platform display-model components

Create shared display contracts, even if UI implementation differs:

- `DeliveryStatusDisplay`
- `RouteDisplayModel`
- `RiderAvailabilityDisplay`
- `MoneyDisplay`
- `NotificationDisplay`
- `QueuePositionDisplay`

These should prevent mobile and admin from interpreting the same delivery differently.

## 13. Delivery lifecycle design

The app needs a visual lifecycle that maps directly to business truth.

Recommended lifecycle:

1. **Received:** ESDispatch has received the request.
2. **Queued:** No rider is available yet or request is waiting by queue/SLA.
3. **Reserved:** A rider is selected for this request after current mission.
4. **Assigned:** Rider is actively assigned and visible to customer.
5. **Pickup:** Rider is heading to pickup or verifying pickup.
6. **In Transit:** Parcel is moving toward destination.
7. **Arrived:** Rider is within arrival/proximity threshold or manually confirmed.
8. **Delivered:** Proof/OTP completed.
9. **Issue:** Delivery needs admin/customer/rider attention.
10. **Cancelled:** Request closed before delivery.

Rules:

- Admin can assign or reserve riders, but cannot create impossible state.
- Rider can update only authorized rider-owned statuses.
- Customer sees simple human labels.
- Admin sees operational labels and audit context.

## 14. Notification design

Notifications must be stateful, not just temporary messages.

### 14.1 Notification fields

- ID
- event type
- entity ID
- title
- message
- severity
- created time
- read time
- dismissed time
- expiry time
- dedupe key
- action target

### 14.2 Behavior rules

- Dismissed notifications stay dismissed.
- Same event does not keep popping up.
- Critical updates can reappear only if severity or status changes.
- Toasts are for quick feedback.
- Notification center is for persistent history.
- Admin broadcast messages should show scope, audience, and delivery status.

### 14.3 Visual rules

- One toast stack position.
- Maximum visible toasts at once.
- Clear close button.
- Group repeated events.
- Do not cover primary dispatch actions.

## 15. Responsive and dynamic content rules

Design for real data:

- Very long Benin City addresses.
- Missing rider photo.
- Missing rider phone.
- Long customer names.
- Large amounts.
- Old and new order IDs.
- Offline rider.
- No rider available.
- Preassigned rider.
- Failed image loads.
- Empty marketplace catalog.
- No notifications.
- Multiple urgent admin requests.

Rules:

- Each text field gets max lines.
- Important hidden text must have an expand/detail path.
- Price/status/action columns should not be squeezed by route text.
- Address blocks should not be single concatenated strings.
- Detail drawers should hold full data, while cards hold scannable summaries.

## 16. Accessibility rules

- Respect user font scale.
- Avoid forced 1.0 font scale unless there is a documented technical reason and alternative accessibility path.
- Minimum touch target: 44dp/44px.
- Every icon-only button needs an accessible label.
- Color must not be the only indicator of status.
- Reduced-motion preference must reduce pulse and bounce.
- Admin dropdowns, modals, and drawers need keyboard/focus behavior.
- Tiny 9px/sp status text should be replaced with readable compact badges.

## 17. Admin usability rules

Admin UI must prioritize operations over decoration.

### 17.1 Request triage

Every active request row/drawer should show:

- queue position
- received time
- SLA age
- service type
- customer name
- contact action
- pickup
- dropoff
- package details
- payment state
- rider state
- valid next action

### 17.2 Assignment

Rider assignment UI should show:

- online/offline
- current mission
- next reserved mission
- distance from pickup
- estimated completion time
- rating/reliability
- capacity
- suggested match reason

### 17.3 Status changes

Admin should not choose from a flat list of statuses. Admin should see valid next actions:

- queue request
- assign rider
- reserve rider
- mark issue
- cancel
- supervisor override, if allowed

Rider-owned statuses should be visually shown but not casually editable by admin unless policy explicitly allows it.

## 18. Marketplace design rules

Marketplace cards need stronger product trust.

Rules:

- Missing image becomes a polished placeholder, not a broken icon.
- Missing rating says "New" or hides the rating.
- Stock states are consistent: In stock, Low stock, Sold out.
- Favorite and add-cart controls have proper touch targets.
- Swipe gestures are optional shortcuts.
- Vendor names should not use low-contrast gold text on light surfaces.
- Floating cart bar should not cover product rows.

## 19. Booking form design rules

Booking forms should be broken into clear sections:

- sender/pickup
- receiver/dropoff
- package details
- service type
- schedule
- payment/fare
- review

Rules:

- Each section card uses the same spacing and title style.
- Required fields are clear.
- Validation errors appear near the field.
- Bottom CTA does not cover focused inputs.
- Price changes animate as a live ticker, but should not distract from form entry.

## 20. AGENTS.md recommendation block

The existing `AGENTS.md` already contains strong brand and build rules. The following additions are recommended as a future update. They are listed here rather than applied directly in this pass.

```md
## Component Quality & Micro-UI Rules

All repeated UI patterns must be implemented through shared components or shared primitives unless there is a clear product reason not to.

Required shared primitives include status badges, route displays, price displays, icon-only action buttons, section headers, empty states, loading skeletons, bottom sheets, admin panels, and notification banners.

Cards must use a deliberate internal grid. Do not place order IDs, prices, statuses, addresses, icons, and actions wherever they are easiest to code. Position information by hierarchy:
- what the item is
- current state
- route or operational context
- owner/rider/customer where relevant
- valid next action

All delivery status UI must reflect the real delivery lifecycle. Do not allow customer, rider, or admin UI to show rider-dependent progress unless rider assignment, rider reservation, or rider action data supports it.

All address and route UI must handle long Benin City addresses, missing optional data, and different screen widths without clipping, overlap, or broken rhythm.

Icon-only controls must provide at least 44dp/44px hit area, visible pressed state, disabled state where relevant, and accessible labels.

Notifications must support dedupe, dismissal persistence, expiry, and action state. A notification dismissed by the user must not reappear unless the underlying event changes.

Every new or modified component must be checked in light mode, dark mode, small screen width, long text, missing data, loading state, empty state, error state, disabled state, and reduced-motion mode where applicable.
```

## 21. Component review checklist

Before any UI change is considered finished, answer these questions:

- Does the component use shared tokens?
- Does it use the correct brand color pairing?
- Is the most important information visually first?
- Is status readable and truthful?
- Are actions visibly tappable?
- Are icon controls large enough to touch?
- Does long text wrap safely?
- Does missing data look intentional?
- Does loading match final layout?
- Does the component work in dark mode and light mode?
- Does it respect bottom nav/header/safe-area spacing?
- Does it avoid unnecessary animation?
- Does it feel like ESDispatch?

## 22. Recommended preview library

Create previews/stories for:

- Active delivery - received.
- Active delivery - queued, no rider.
- Active delivery - reserved rider.
- Active delivery - assigned rider with photo.
- Active delivery - in transit.
- Active delivery - arrived.
- Active delivery - delivered.
- Active delivery - issue.
- History card - short route.
- History card - long route.
- Marketplace product - normal.
- Marketplace product - no image.
- Marketplace product - sold out.
- Marketplace product - no rating.
- Admin dispatch drawer - no rider available.
- Admin dispatch drawer - one rider free.
- Admin dispatch drawer - rider preassigned.
- Admin notification - duplicate dismissed.
- Rider mission - current.
- Rider mission - reserved next.

## 23. Practical rollout strategy

Start with the components that appear everywhere and affect trust:

1. `StatusBadge`
2. `RouteDisplay`
3. `DeliveryCard`
4. `HistoryOrderCard`
5. `DispatchDecisionDrawer`
6. Notification lifecycle
7. `IconActionButton`
8. `PriceDisplay`
9. `BottomNav` inset and motion polish
10. Marketplace product card

This order fixes the visible dashboard screenshots, the admin dispatch workflow, and the underlying state language at the same time.

## 24. Final design-system position

ESDispatch does not need a new visual identity. It needs stricter component truth.

The current UI already has recognizable taste: dark luxury, gold accents, bold cards, rounded forms, and motion ambition. The next step is discipline. Give every status one meaning, every route one structure, every card a grid, every action a state, every notification a lifecycle, and every long Benin City address a safe place to live.

That is how the app becomes soft, fluid, alive, and still operationally serious.
