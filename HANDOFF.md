# Handoff: WebSocket Notifications + Campsite Status + Stripe + Analytics

**Generated**: 2026-04-27
**Branch**: master
**Status**: Working — all 4 features fully tested end-to-end

## Goal

Full-stack Campway project (Spring Boot 3.4.4 + Angular 18.2). Four major features added:
1. Real-time WebSocket notifications (STOMP/SockJS)
2. Automatic campsite status management (weather/date/capacity rules + scheduler)
3. Stripe card payments (PaymentIntent + Elements)
4. Admin analytics dashboard (KPIs, revenue chart, occupancy, fraud detection, CSV export)

## Completed

- [x] WebSocket notifications — topic-per-user routing, JWT auth, all 5 notification types firing
- [x] Campsite status management — PENDING/ACTIVE/FULL/SUSPENDED/EXPIRED/DELETED, Open-Meteo weather, hourly scheduler, history tracking, admin+owner UI
- [x] Stripe payment — PaymentIntent flow, Stripe Elements replaces fake card form
- [x] Analytics dashboard at `/admin/campsite-analytics` — KPIs, revenue bar chart, occupancy table, fraud detection (≥5 cancellations), CSV export
- [x] Fraud query bug fixed — native SQL used `u.firstname`/`u.lastname`, corrected to `u.first_name`/`u.last_name`
- [x] Auth interceptor bug fixed — was logging out on 403 (not just 401)
- [x] Campsite owner dashboard — lat/lng fields + "Refresh Status" button added
- [x] Test data seeded — 13 bookings, 6 payments (1,015 TND revenue), 5 cancellations for fraud test user

## Not Yet Done

- [ ] Nothing committed — all 31 modified + 20 new files are uncommitted
- [ ] Stripe refund — `PATCH /api/v1/campsite-payments/{id}/refund` only changes DB status, does NOT call `stripe.refunds.create()` — real Stripe refund not wired
- [ ] Remove debug `console.log` statements in `notification.service.ts` before production

## Failed Approaches (Don't Repeat These)

> `convertAndSendToUser(email, "/queue/notifications", payload)` — backend logged "sent" but MESSAGE frame never arrived at client. `SimpUserRegistry` silently fails to resolve sessions by principal name. **Fixed**: topic-per-user — `convertAndSend("/topic/notif-{userId}", payload)`. **Never revert to `convertAndSendToUser`.**

> `npm install` without `--legacy-peer-deps` — ERESOLVE conflict (`@angular/localize@18.2.0` vs `@angular/compiler@18.2.14`). **Always use `--legacy-peer-deps` in `front/`.**

> `@stomp/stompjs` missing alongside `@stomp/rx-stomp` — Angular build error: `Could not resolve "@stomp/stompjs"`. Fixed: `npm install @stomp/stompjs --legacy-peer-deps`.

> SockJS uses Node.js globals (`global`, `process`) not available in browser bundles — runtime crash. Fixed: `front/src/window-global-fix.ts` polyfill + registered in `angular.json` + `"types": ["node"]` in `tsconfig.app.json`.

> MySQL `ddl-auto=update` does NOT alter existing ENUM columns — `Data truncated for column 'status'`. Fixed: manual `ALTER TABLE` (see Setup Required).

> Auth interceptor logged out user on 403 — campsite owner clicking "Refresh Status" was getting 403 (endpoint was ADMIN-only) which triggered logout. Fixed: interceptor now only logs out on 401; endpoint changed to `hasAnyRole('ADMIN', 'COMPSITEOWNERS')`.

> Analytics fraud query used `u.firstname`/`u.lastname` — MySQL error: `Unknown column 'u.firstname'`. Actual columns are `u.first_name`/`u.last_name` (Spring JPA snake_case convention). Fixed in `CampsiteBookingRepository.java`.

> Backend port is `9099` — NOT `8080` or `8222`. No API gateway in this project.

> Angular project uses traditional `NgModule` — do NOT create standalone components. All components must be declared in `app.module.ts`.

> `RestTemplate` for weather (not WebClient) — no WebFlux in pom.xml. Do not add WebFlux; conflicts with existing MVC setup.

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Topic-per-user WS routing `/topic/notif-{userId}` | `SimpUserRegistry` session lookup silently fails; topic routing bypasses it entirely |
| Long userId (not email) for WS topics | Frontend already has `user.id` (numeric); avoids encoding special chars in email |
| Open-Meteo (free, no key) | Sufficient for hourly polling; zero setup |
| `RestTemplate` not WebClient | No WebFlux in pom.xml; adding it would conflict with MVC |
| Stripe PaymentIntent + Elements | PCI compliance: raw card numbers must never touch backend |
| Only 401 triggers logout | 403 = authenticated but wrong role; should NOT clear session |
| Native SQL for analytics | `DATE_FORMAT` unavailable in JPQL; native queries needed for monthly grouping |
| CSS/Bootstrap bar charts | No new dependency; AdminLTE progress bars sufficient for demo |

## Current State

**Working** (all confirmed via API calls and UI):
- WS notifications fire and arrive at browser — bell badge increments in real time
- Status scheduler runs hourly, logs `Status refresh complete: N campsite(s) updated`
- Stripe PaymentIntent creates successfully, card confirmed via Stripe Elements
- Analytics: overview KPIs, revenue by month, occupancy per campsite, fraud detection, CSV download
- Test data: 1,015 TND revenue, 13 bookings across 2 campsites, fraud suspect with 5 cancellations detected

**Broken**: Nothing known.

**Uncommitted**: Everything — 31 modified + 20 new files. Nothing committed yet.

## Files to Know

| File | Why It Matters |
|------|----------------|
| `back/.../config/WebSocketConfig.java` | STOMP broker — simple broker has `/topic`, `/queue`, `/user` |
| `back/.../config/WebSocketAuthInterceptor.java` | JWT on STOMP CONNECT, logs at INFO |
| `back/.../services/WsNotificationService.java` | `sendToUser(Long userId, payload)` → `/topic/notif-{userId}` |
| `back/.../services/CampsiteStatusEvaluator.java` | Rule chain: expired→pending→weather→full→active |
| `back/.../services/CampsiteStatusScheduler.java` | `@Scheduled` hourly, `campway.status.refresh-rate-ms` |
| `back/.../controllers/CampsiteStatusController.java` | `POST /{id}/refresh` (ADMIN+COMPSITEOWNERS), `GET /{id}/history` |
| `back/.../controllers/StripeController.java` | `POST /api/v1/stripe/payment-intent` → `{clientSecret, publishableKey, paymentIntentId}` |
| `back/.../services/StripeService.java` | Creates PaymentIntent, amount×100 in EUR cents |
| `back/.../controllers/AnalyticsController.java` | 4 endpoints + CSV, all `@PreAuthorize("hasRole('ADMIN')")` |
| `back/.../repositories/CampsiteBookingRepository.java` | Native queries: occupancy, fraud (`first_name`/`last_name`), CSV export |
| `back/.../repositories/CampsitePaymentRepository.java` | Native query: revenue by month (`DATE_FORMAT`) |
| `back/.../jwt/SecurityConfig.java` | In `jwt/` not `config/` — add all new security matchers here |
| `back/src/main/resources/application.properties` | Stripe keys (hardcoded test keys), port 9099, scheduler rate |
| `front/.../services/notification.service.ts` | `initForUser(userId)` → subscribes to `/topic/notif-${userId}` |
| `front/.../pages/campsite-payment/campsite-payment.component.ts` | Stripe Elements mount + `confirmCardPayment` flow |
| `front/.../dashboards/campsite-analytics/` | New component: KPIs, revenue bars, occupancy, fraud, CSV |
| `front/src/window-global-fix.ts` | SockJS polyfill: `window.global = window` |

## Code Context

### Send a WS notification (backend)
```java
// Pass Long userId — NEVER email
wsNotificationService.sendToUser(user.getId(), NotificationPayload.builder()
    .type("CAMPSITE_BOOKING_CONFIRMED")
    .message("Your campsite booking #" + id + " has been confirmed.")
    .referenceId(id)
    .build());
```

### Add a new notification type
1. Backend: call `wsNotificationService.sendToUser(user.getId(), payload)` in relevant service
2. Frontend: add `case 'YOUR_TYPE':` to `wsPayloadToNotif()` in `notification.service.ts`

### WS notification types
| Backend `type` | Title | Link |
|---|---|---|
| `CAMPSITE_BOOKING_CONFIRMED` | Booking Confirmed | `/my-bookings` |
| `CAMPSITE_BOOKING_CANCELLED` | Booking Cancelled | `/my-bookings` |
| `OUTDOOR_PROPOSAL_APPROVED` | Proposal Approved | `/my-proposals` |
| `OUTDOOR_PROPOSAL_REJECTED` | Proposal Rejected | `/my-proposals` |
| `OUTDOOR_BOOKING_CANCELLED` | Outdoor Booking Cancelled | `/my-bookings` |

### Stripe payment flow
```
POST /api/v1/stripe/payment-intent {bookingId, amount}
← {clientSecret, publishableKey, paymentIntentId}

stripe.confirmCardPayment(clientSecret, {card: stripeCardElement})
← {paymentIntent: {status: 'succeeded', id: 'pi_xxx'}}

POST /api/v1/campsite-payments {bookingId, amount, method:'CARD', transactionId:'pi_xxx'}
← booking confirmed + WS notification fired
```

### Analytics API
```
GET /api/v1/analytics/overview
→ {totalRevenue, totalBookings, confirmedBookings, cancelledBookings, fraudSuspects}

GET /api/v1/analytics/revenue-by-month
→ [{month:"2026-04", revenue:1015.0}, ...]   ← last 12 months

GET /api/v1/analytics/occupancy
→ [{campsiteId, name, totalBookings, confirmedBookings, cancelledBookings, cancellationRate}, ...]

GET /api/v1/analytics/fraud-suspects?minCancellations=5
→ [{userId, email, fullName, cancellations, totalBookings, cancellationRate}, ...]

GET /api/v1/analytics/export/csv
→ CSV download: Booking ID, Camper Email, Campsite, Check-in, Check-out, Guests, Total Price, Status
```

### Status evaluation rule chain
```
endDate < today                                  → EXPIRED
startDate > today                                → PENDING
Open-Meteo: WMO code ≥65 OR windspeed > 60 km/h → SUSPENDED
sumGuestsOverlapping(today, today+1) >= capacity  → FULL
otherwise                                         → ACTIVE
```

### Campsite status endpoints
```
POST /api/v1/campsite-status/refresh           ADMIN: refresh all
POST /api/v1/campsite-status/{id}/refresh      ADMIN or COMPSITEOWNERS: refresh one
GET  /api/v1/campsite-status/{id}/history      ADMIN or COMPSITEOWNERS: audit log
GET  /api/v1/campsite-status/current/{id}      any authenticated: preview without applying
```

## Resume Instructions

1. **MySQL ENUM fix** (run once if DB was never altered):
   ```bash
   mysql -u root -proot CampwayDB -e "ALTER TABLE campsites MODIFY COLUMN status ENUM('PENDING','ACTIVE','FULL','SUSPENDED','EXPIRED','DELETED') NOT NULL DEFAULT 'ACTIVE';"
   ```

2. **Start backend**: `ProjetPiDevApplication` from `back/`
   - Expected: `WebSocket endpoint registered at /ws`
   - Expected: `Status refresh complete:` (scheduler fires at startup)

3. **Start frontend**: `cd front && ng serve`
   - If node_modules missing: `npm install --legacy-peer-deps`

4. **Verify analytics** — login as admin → `/admin/campsite-analytics`:
   - KPIs: Revenue=1015 TND, Bookings=13, Cancellations=5, Fraud=1
   - Revenue bar for 2026-04
   - Occupancy: Pine Forest Camp (25% cancel), Pine ForestPine (17% cancel)
   - Fraud: Mohamed Yassine ATTIA — 5 cancellations, MEDIUM 38%
   - CSV download works

5. **Verify Stripe** — login as camper → book campsite → payment page:
   - Card: `4242 4242 4242 4242` / `12/29` / `123` → Pay
   - Expected: confetti + "Payment Successful" + bell notification fires

6. **Verify WS notification** — two browsers:
   - Browser A (camper): submit outdoor proposal
   - Browser B incognito (admin): approve it
   - Browser A: bell +1 with "Proposal Approved"

7. **Verify weather SUSPENDED** — owner dashboard → edit campsite → lat=`65.0` lng=`-18.0` → save → Refresh Status:
   - If wind > 60: SUSPENDED immediately
   - If wind 50–60: temporarily change `WeatherData.java` threshold to `> 50.0`, restart, test, revert

8. **Commit everything**:
   ```bash
   git add back/pom.xml back/src front/package.json front/package-lock.json front/angular.json front/tsconfig.app.json front/src HANDOFF.md
   git commit -m "feat: WebSocket notifications + campsite status management + Stripe payments + analytics dashboard"
   ```

## Setup Required

- MySQL on `localhost:3306`, DB `CampwayDB` — run ENUM ALTER once (step 1)
- Stripe test keys in `application.properties`:
  - Secret: `sk_test_51QCAHpBDW3LkkcbKFv9eNqLW...`
  - Publishable: `pk_test_51QCAHpBDW3LkkcbKNseYoK0...`
- Open-Meteo: free, no key needed
- MailDev: `docker run -d -p 1080:1080 -p 1025:1025 maildev/maildev`
- Test accounts (all use password `Dev@12345!`):
  - Admin: `admin1@campconnect.tn`
  - Camper (test data owner): `mohamedyassineattia.dev@gmail.com` / `94='Y3|T1f<+`
  - Campsite owner: `SELECT email FROM users WHERE role='COMPSITEOWNERS' LIMIT 1;`

## Edge Cases & Error Handling

- **User offline when WS fires** → message dropped silently, no persistence
- **Campsite has no lat/lng** → weather check skipped entirely, no false SUSPENDED
- **Open-Meteo unreachable** → `WeatherService` returns `Optional.empty()`, weather step silently skipped
- **Admin manually suspends campsite, scheduler runs** → scheduler skips (reason must start with "Severe weather" to be recoverable)
- **Stripe PaymentIntent created, user closes page** → intent abandoned on Stripe side, booking stays PENDING
- **Analytics on empty DB** → all endpoints return empty arrays/zero values, UI shows graceful empty states
- **Fraud threshold** → default 5, configurable via `?minCancellations=N` on the API (UI always uses 5)
- **Double payment attempt** → backend throws `IllegalStateException("Payment already exists for this booking")`

## Warnings

- **MySQL ENUM must be manually altered** — `ddl-auto=update` never modifies existing ENUM columns. If you add more status values, run `ALTER TABLE` again.
- `SecurityConfig.java` is in `back/.../jwt/` NOT `back/.../config/`.
- `npm install` in `front/` **always** needs `--legacy-peer-deps`.
- `WsNotificationService.sendToUser` takes `Long userId` (not email) — all 5 callers already updated.
- Analytics native queries use MySQL `DATE_FORMAT` — will NOT work on H2 test DB.
- Stripe amount is ×100 as EUR cents — UI shows TND, Stripe processes EUR (test/demo only).
- `campway.status.refresh-rate-ms` default = `3600000` (1 hr) — set to `60000` for quick testing, reset after.
- Weather threshold for SUSPENDED: `windspeed > 60 km/h OR weathercode >= 65` — Iceland coords `65.0,-18.0` are reliable for testing (wind fluctuates around 55–65 km/h).
