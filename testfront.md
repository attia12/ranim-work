# Campway — Frontend Test Documentation

**Project:** Campway — Camping Platform
**Stack:** Angular 18.2.0 · Jasmine · Karma · ChromeHeadless
**Modules tested:** Official Campsite & Booking (Module 1) · Outdoor Campsite & Booking (Module 2)

---

## How to Run the Tests

```bash
cd front
npm test
```

> Tests run in **ChromeHeadless** (no visible browser window). Results print directly to the terminal.

---

## Test Results Summary

**Total: 297 tests — 297 PASSED, 0 FAILED**

| # | File | Description | Tests | Result |
|---|------|-------------|-------|--------|
| 1 | `services/campsite.service.spec.ts` | HTTP calls for campsite CRUD, search, suspend/activate | 11 | PASS |
| 2 | `services/campsite-booking.service.spec.ts` | HTTP calls for bookings and payments | 11 | PASS |
| 3 | `services/outdoor-campsite.service.spec.ts` | HTTP calls for outdoor campsites and bookings | 13 | PASS |
| 4 | `pages/campsites/campsites.component.spec.ts` | Component creation | 1 | PASS |
| 5 | `pages/campsite-detail/campsite-detail.component.spec.ts` | Load campsite, gallery navigation, bookNow auth guard | 8 | PASS |
| 6 | `pages/campsite-booking/campsite-booking.component.spec.ts` | Form validation, nights/price calculation, submit flow | 10 | PASS |
| 7 | `pages/campsite-payment/campsite-payment.component.spec.ts` | Payment method selection, PAYPAL/BANK pay, error handling | 9 | PASS |
| 8 | `pages/my-bookings/my-bookings.component.spec.ts` | Load bookings, cancel modal, canCancel rule, badge classes | 11 | PASS |
| 9 | `pages/outdoor-trips/outdoor-trips.component.spec.ts` | Component creation | 1 | PASS |
| 10 | `pages/outdoor-campsite-detail/outdoor-campsite-detail.component.spec.ts` | Load site, gallery, difficulty badge, bookNow auth guard | 10 | PASS |
| 11 | `pages/outdoor-booking/outdoor-booking.component.spec.ts` | Form validation, nights calculation, submit and error | 10 | PASS |
| 12 | `pages/propose-outdoor/propose-outdoor.component.spec.ts` | Form validation, minLength, hasError, submit, error | 10 | PASS |
| 13 | `pages/my-proposals/my-proposals.component.spec.ts` | Load proposals, statusBadgeClass all statuses, error | 10 | PASS |
| 14 | `dashboards/campsite-owner/campsite-owner.component.spec.ts` | Admin vs owner load, CRUD, feature toggle, confirmBooking | 14 | PASS |
| 15 | `dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component.spec.ts` | Approve, reject modal, confirmReject, difficultyBadge | 12 | PASS |
| — | *Other pre-existing spec files* | Other modules (auth, forum, shop, etc.) | 147 | PASS |

---

## What Each Test File Covers

### Services (HTTP Layer)

#### `campsite.service.spec.ts`
Tests every method of `CampsiteService` using `HttpTestingController` to intercept and assert HTTP requests without hitting a real server.

| Test | Assertion |
|------|-----------|
| `search()` with filters | GET `/api/v1/campsites` with country and city params |
| `search()` without filters | Optional params are omitted from the request |
| `getCampsiteById()` | GET `/api/v1/campsites/42` |
| `getMyCampsites()` | GET `/api/v1/campsites/my` |
| `getAllAdmin()` | GET `/api/v1/campsites/all` |
| `create()` | POST `/api/v1/campsites` with correct body |
| `update()` | PUT `/api/v1/campsites/7` with correct body |
| `delete()` | DELETE `/api/v1/campsites/3` |
| `suspend()` | PATCH `/api/v1/campsites/5/suspend` |
| `activate()` | PATCH `/api/v1/campsites/5/activate` |

#### `campsite-booking.service.spec.ts`
Tests booking creation, retrieval, cancellation, confirmation, and payment recording.

| Test | Assertion |
|------|-----------|
| `create()` | POST `/api/v1/campsite-bookings` with booking data |
| `getById()` | GET `/api/v1/campsite-bookings/10` |
| `getMyBookings()` | GET `/api/v1/campsite-bookings/my` |
| `getByCampsite()` | GET `/api/v1/campsite-bookings/campsite/1` |
| `getAll()` | GET `/api/v1/campsite-bookings` |
| `cancel()` with reason | Body contains `{ reason: 'changed plans' }` |
| `cancel()` without reason | Body is empty `{}` |
| `confirm()` | PATCH `/api/v1/campsite-bookings/5/confirm` |
| `pay()` | POST `/api/v1/campsite-payments` |
| `getPaymentByBooking()` | GET `/api/v1/campsite-payments/booking/5` |

#### `outdoor-campsite.service.spec.ts`
Tests the full outdoor campsite and booking HTTP surface.

| Test | Assertion |
|------|-----------|
| `getApproved()` | GET `/api/v1/outdoor-campsites` |
| `getById()` | GET `/api/v1/outdoor-campsites/3` |
| `propose()` | POST with site data |
| `update()` | PUT `/api/v1/outdoor-campsites/3` |
| `getPending()` | GET `/api/v1/outdoor-campsites/pending` |
| `moderate()` — APPROVE | PATCH with `{ action: 'APPROVE' }` |
| `moderate()` — REJECT | PATCH includes `adminNote` in body |
| `getMyProposals()` | GET `/api/v1/outdoor-campsites/my` |
| `book()` | POST `/api/v1/outdoor-bookings` |
| `getBookingById()` | GET `/api/v1/outdoor-bookings/7` |
| `getMyOutdoorBookings()` | GET `/api/v1/outdoor-bookings/my` |
| `cancelBooking()` | PATCH `/api/v1/outdoor-bookings/7/cancel` |

---

### Components (Module 1 — Official Campsite & Booking)

#### `campsite-detail.component.spec.ts`
Tests the campsite detail page behaviour with a mocked `CampsiteService` and `AuthService`.

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads campsite on init | `getCampsiteById(1)` called, `campsite` is set |
| Error on load failure | `error` = `'Campsite not found.'` |
| `setActiveImage()` | `activeImageIndex` updated |
| `activeImage` by index | Returns correct picture URL |
| `activeImage` with no pictures | Returns placeholder image path |
| `bookNow()` — not logged in | Navigates to `/login` |
| `bookNow()` — logged in | Navigates to `/campsites/1/book` |

#### `campsite-booking.component.spec.ts`
Tests the booking form including reactive validation and price computation.

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads campsite on init | `getCampsiteById(1)` called |
| Error on load failure | `error` = `'Campsite not found.'` |
| Form invalid when empty | `bookingForm.valid` is false |
| Form valid when filled | All required fields present → valid |
| `nights` computed | 4 nights for Jul 1–5 |
| `nights` returns 0 | When dates are not set |
| `totalPrice` = nights × price × guests | 4 × 50 × 2 = 400 for OFFICIAL |
| `totalPrice` = 0 for OUTDOOR type | Free outdoor campsite |
| `submit()` no-ops when invalid | `create()` not called |
| `submit()` calls `create()` | Correct payload sent |
| `submit()` sets error on failure | Error message shown |

#### `campsite-payment.component.spec.ts`
Tests the payment flow (PAYPAL and BANK_TRANSFER paths; Stripe is stubbed).

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Reads params on init | `bookingId=5`, `amount=150` from query params |
| Default method is CARD | `selectedMethod === 'CARD'` |
| `selectMethod()` changes method | `selectedMethod` updated |
| `selectMethod()` clears error | `error` reset to empty |
| `pay()` PAYPAL | Calls `bookingService.pay()` with `method: 'PAYPAL'`, sets `paid=true` |
| `pay()` BANK_TRANSFER | Calls `bookingService.pay()` with `method: 'BANK_TRANSFER'` |
| `pay()` sets error on failure | `error` message shown, `paid` stays false |
| `goToBookings()` | Navigates to `/my-bookings` |

#### `my-bookings.component.spec.ts`
Tests the tabbed bookings page for official and outdoor bookings.

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads both booking types on init | Both services called with page 0, size 50 |
| Separates official and outdoor campsite bookings | Filter by `campsiteType` field |
| Populates outdoor bookings | From `OutdoorCampsiteService` |
| Loading false after data | `loading` = false |
| `openCancelModal()` | Sets id, resets reason, shows modal |
| `confirmCancel()` calls cancel | Booking status updated in list |
| `confirmCancel()` no-op when no id | `cancel()` not called |
| `canCancel()` true for far future | Date > today + 2 days |
| `canCancel()` false for tomorrow | Date ≤ today + 2 days |
| `badgeClass()` all statuses | Correct Bootstrap badge class |
| Default tab is official | `activeTab === 'official'` |

---

### Components (Module 2 — Outdoor Campsite & Booking)

#### `outdoor-campsite-detail.component.spec.ts`

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads site on init | `getById(3)` called, `site` is set |
| Error on load failure | `error` = `'Outdoor campsite not found.'` |
| `setActiveImage()` | `activeImageIndex` updated |
| `activeImage` by index | Returns correct picture URL |
| `activeImage` with no pictures | Returns outdoor placeholder |
| `difficultyBadge()` EASY | `badge-success` |
| `difficultyBadge()` MODERATE | `badge-warning` |
| `difficultyBadge()` HARD | `badge-danger` |
| `bookNow()` — not logged in | Navigates to `/login` |
| `bookNow()` — logged in | Navigates to `/outdoor-campsites/3/book` |

#### `outdoor-booking.component.spec.ts`

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads site on init | `getById(3)` called |
| Error on load failure | `error` set |
| Default guests = 1 | `numberOfGuests` default value |
| Form invalid when empty | `bookingForm.valid` is false |
| Form valid when filled | All required fields → valid |
| `nights` computed | 4 nights for Aug 1–5 |
| `nights` = 0 when no dates | Returns 0 |
| `submit()` no-ops when invalid | `book()` not called |
| `submit()` sends correct data | `outdoorCampsiteId`, dates, guests |
| `submit()` navigates to `/my-bookings` | On success |
| `submit()` sets error | On service failure |

#### `propose-outdoor.component.spec.ts`

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Form has required fields | name, country, city exist |
| difficulties list | `['EASY', 'MODERATE', 'HARD']` |
| Form invalid when empty | `form.valid` is false |
| Form valid when filled | name + country + city → valid |
| name requires min 3 chars | 2 chars → invalid, 3 chars → valid |
| `hasError()` for invalid touched | Returns true |
| `hasError()` for valid field | Returns false |
| `submit()` marks all touched | When form invalid |
| `submit()` calls `propose()` | With form values |
| `submit()` sets success | `success = true` |
| `submit()` sets error | Error message shown |

#### `my-proposals.component.spec.ts`

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads proposals on init | `getMyProposals(0, 50)` called |
| Populates proposals list | 3 proposals shown |
| Loading false after data | `loading` = false |
| Error + loading false on failure | Error message set |
| `statusBadgeClass()` PENDING | `badge-warning` |
| `statusBadgeClass()` APPROVED | `badge-success` |
| `statusBadgeClass()` REJECTED | `badge-danger` |
| `statusBadgeClass()` SUSPENDED | `badge-secondary` |
| `statusBadgeClass()` unknown | `badge-light` |

---

### Dashboard Components

#### `campsite-owner.component.spec.ts`
Tests the owner/admin dashboard with campsite CRUD, availability, and booking management.

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Non-admin calls `getMyCampsites()` | Owner sees own campsites |
| Admin calls `getAllAdmin()` | Admin sees all campsites |
| Campsites loaded on init | List populated from service |
| `selectCampsite()` | Sets selected, loads bookings |
| `openCreateForm()` | Resets form, hides editingCampsite |
| `openEditForm()` | Patches form with campsite data |
| `toggleFeature()` adds feature | Feature added to selectedFeatures |
| `toggleFeature()` removes feature | Feature removed if already present |
| `hasFeature()` | Reflects selectedFeatures state |
| `saveCampsite()` create | Calls `create()` when not editing |
| `saveCampsite()` update | Calls `update(id)` when editing |
| `confirmBooking()` | Calls `confirm(5)`, updates booking in list |
| `badgeClass()` booking statuses | Correct Bootstrap classes |
| `statusBadgeClass()` campsite statuses | Correct Bootstrap classes |

#### `admin-outdoor-moderation.component.spec.ts`
Tests the admin panel for approving and rejecting outdoor campsite proposals.

| Test | Assertion |
|------|-----------|
| Component creates | Component instance is truthy |
| Loads pending on init | `getPending(0, 50)` called, list populated |
| Loading false after data | `loading` = false |
| `approve()` calls moderate | `moderate(1, { action: 'APPROVE' })` |
| `approve()` removes from list | Site no longer in `pendingSites` |
| `approve()` sets success message | Message displayed |
| `approve()` sets error on failure | Error message shown |
| `openRejectModal()` | Sets selectedId, shows modal, resets note |
| `confirmReject()` no-op without id | `moderate()` not called |
| `confirmReject()` calls moderate | With REJECT action and adminNote |
| `confirmReject()` removes from list | Site no longer in `pendingSites` |
| `confirmReject()` hides modal first | `showRejectModal` = false before call |
| `difficultyBadge()` all difficulties | EASY/MODERATE/HARD/undefined badges |

---

## Test Infrastructure Fixes Applied

Two configuration fixes were needed before the tests could run:

### 1. `global is not defined` error
`sockjs-client` (used by Angular's WebSocket service) expects Node's `global` variable in the browser test environment. The existing polyfill `src/window-global-fix.ts` was already used in the build but was missing from the test config.

**Fix — `angular.json` test polyfills:**
```json
"polyfills": [
  "@angular/localize/init",
  "zone.js",
  "zone.js/testing",
  "src/window-global-fix.ts"
]
```

**Fix — `tsconfig.spec.json` include:**
```json
"include": [
  "src/**/*.spec.ts",
  "src/**/*.d.ts",
  "src/window-global-fix.ts"
]
```

### 2. Result after fixes
```
TOTAL: 297 SUCCESS
```
