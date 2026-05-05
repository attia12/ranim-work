# Campway — Notifications, Stripe Payment & Analytics

A plain-language explanation of three systems: real-time notifications (WebSocket), online payment (Stripe), and the admin analytics dashboard.

---

## 1. Real-Time Notifications (WebSocket + STOMP)

### What it does
When something happens to a user's booking — confirmed, cancelled, proposal approved/rejected — the user instantly sees a notification in the header bell icon **without refreshing the page**. This is done using WebSocket (a permanent connection between browser and server).

### The technology
- **STOMP** — a messaging protocol that runs on top of WebSocket. Think of it like a postal system: messages go to named "topics".
- **SockJS** — a fallback library. If WebSocket is blocked by the browser or network, SockJS tries other techniques (long-polling etc.) automatically.
- **RxStomp** — the Angular client library that wraps STOMP into RxJS observables.

---

### Backend files

#### `config/WebSocketConfig.java`
Registers and configures the WebSocket server.

| What it sets up | Explanation |
|-----------------|-------------|
| Endpoint `/ws` | The URL the browser connects to: `http://localhost:9099/ws` |
| Allows origin `localhost:4200` | Only the Angular dev server can connect (security) |
| SockJS enabled | Auto-fallback if pure WebSocket fails |
| Prefix `/app` | Messages **from** the client go here |
| Broker `/topic`, `/queue`, `/user` | Messages **to** clients go here |
| User prefix `/user` | Enables per-user private messaging |

#### `config/WebSocketAuthInterceptor.java`
**The security guard.** Every time a browser tries to open a WebSocket connection, this interceptor runs first.

**Flow:**
1. Browser sends a `CONNECT` frame with header `Authorization: Bearer <jwt>`
2. Interceptor extracts the JWT, calls `JwtService` to decode it
3. Loads the user from DB, validates the token
4. If valid: sets the authenticated user on the session (`accessor.setUser(auth)`) — Spring now knows who this WebSocket belongs to
5. If invalid: logs a warning, connection proceeds as anonymous (no user-specific messages will reach them)

#### `dto/notification/NotificationPayload.java`
The object that gets sent over WebSocket. Very simple:

```java
{
  "type":        "CAMPSITE_BOOKING_CONFIRMED",   // what happened
  "message":     "Your booking #42 has been confirmed.",
  "referenceId": 42,                             // booking ID, for linking
  "timestamp":   "2026-05-05T10:30:00"
}
```

#### `services/WsNotificationService.java`
The single service that actually sends messages. Two methods:

| Method | What it does |
|--------|-------------|
| `sendToUser(userId, payload)` | Sends to `/topic/notif-{userId}` — only that user's browser receives it |
| `sendBroadcast(topic, payload)` | Sends to `/topic/{topic}` — everyone subscribed to that topic receives it |

**Why `/topic/notif-42` instead of `/user/queue/notif`?**
Using the user's ID in the topic name is simpler and avoids Spring's more complex user-session routing. Only the correct user subscribes to their own topic, so it's effectively private.

---

### Where notifications are triggered (backend)

| Event | File | Notification type |
|-------|------|-------------------|
| Booking confirmed by owner | `ICampsiteBookingServiceImpl.confirm()` | `CAMPSITE_BOOKING_CONFIRMED` |
| Booking cancelled | `ICampsiteBookingServiceImpl.cancel()` | `CAMPSITE_BOOKING_CANCELLED` |
| Outdoor proposal approved | `IOutdoorCampsiteServiceImpl` | `OUTDOOR_PROPOSAL_APPROVED` |
| Outdoor proposal rejected | `IOutdoorCampsiteServiceImpl` | `OUTDOOR_PROPOSAL_REJECTED` |
| Outdoor booking cancelled | `IOutdoorBookingServiceImpl` | `OUTDOOR_BOOKING_CANCELLED` |

Each call looks like this:
```java
wsNotificationService.sendToUser(
    booking.getCamper().getId(),
    NotificationPayload.builder()
        .type("CAMPSITE_BOOKING_CONFIRMED")
        .message("Your campsite booking #" + id + " has been confirmed.")
        .referenceId(id)
        .build()
);
```

---

### Frontend files

#### `services/notification.service.ts`
The brain of the Angular notification system. It does four things:

**1. Connect WebSocket on login**
```
initForUser(userId) is called by AuthService after login
  → opens SockJS connection to /ws
  → sends JWT in CONNECT headers
  → subscribes to /topic/notif-{userId}
```

**2. Receive messages**
When a message arrives on the topic:
- Parses the JSON body
- Maps `type` string → visual notification object (title, icon, color, link)
- Wraps in `ngZone.run()` so Angular detects the change and updates the UI

**3. Persist to localStorage**
Notifications are saved in `localStorage` under key `campway_notifs_{userId}`. This means if you refresh the page, your past notifications are still there. Max 50 stored.

**4. Disconnect on logout**
```
clearSession() closes the WebSocket and wipes the in-memory list
```

**Notification types mapped in `wsPayloadToNotif()`:**

| Backend type | Title shown | Icon color |
|-------------|-------------|------------|
| `CAMPSITE_BOOKING_CONFIRMED` | Booking Confirmed | Green |
| `CAMPSITE_BOOKING_CANCELLED` | Booking Cancelled | Red |
| `OUTDOOR_PROPOSAL_APPROVED` | Proposal Approved | Green |
| `OUTDOOR_PROPOSAL_REJECTED` | Proposal Rejected | Red |
| `OUTDOOR_BOOKING_CANCELLED` | Outdoor Booking Cancelled | Red |

#### `layout/header/header.component.ts`
Reads `notification.service.notifications$` (an Observable) and shows the unread count badge on the bell icon. Calls `markAllRead()` when the dropdown is opened.

---

### Full notification flow (one example)

```
Owner clicks "Confirm" on booking #42
  → POST /api/v1/campsite-bookings/42/confirm
  → ICampsiteBookingServiceImpl.confirm()
      → sets booking.status = CONFIRMED
      → sends email to camper
      → wsNotificationService.sendToUser(camper.id, payload)
          → messagingTemplate.convertAndSend("/topic/notif-7", payload)
              → Spring pushes the message over WebSocket
                  → RxStomp in browser receives it on /topic/notif-7
                      → NotificationService.push() called inside NgZone
                          → BehaviorSubject emits new list
                              → Header bell updates with +1 badge
                                  → User sees "Booking Confirmed" popup
```

---

## 2. Stripe Payment

### What it does
When a camper wants to pay for a confirmed booking, they are taken to a payment page with a Stripe card form. The payment is processed securely through Stripe's servers — **card details never touch our backend**.

### The technology
- **Stripe** — online payment platform. Our backend creates a "PaymentIntent" (a payment session), Stripe handles the actual card charge.
- **Stripe.js / stripe-js** — Stripe's Angular library. Renders the card input form inside an iframe so card numbers never go to our code.
- **PaymentIntent** — Stripe's object that represents one payment attempt. Has a `clientSecret` used to confirm it.

---

### Backend files

#### `dto/stripe/CreatePaymentIntentRequest.java`
What the frontend sends to start a payment:
```json
{ "bookingId": 42, "amount": 175.00 }
```

#### `dto/stripe/CreatePaymentIntentResponse.java`
What our backend sends back:
```json
{
  "clientSecret":     "pi_xxx_secret_yyy",
  "publishableKey":   "pk_test_...",
  "paymentIntentId":  "pi_xxx"
}
```
The `clientSecret` is what Stripe.js uses to confirm the payment. The `publishableKey` is needed to initialize the Stripe library in the browser.

#### `services/StripeService.java`
One method: `createPaymentIntent(request)`

**What it does step by step:**
1. Reads `stripe.secret-key` from `application.properties` and sets it on Stripe SDK at startup (`@PostConstruct`)
2. Converts amount to **cents** (Stripe requires integer cents: `175.00 EUR × 100 = 17500`)
3. Creates a `PaymentIntentCreateParams` with: amount, currency EUR, description "Campway booking #42", metadata `bookingId: 42`
4. Calls Stripe API → gets back a `PaymentIntent` object
5. Returns `clientSecret` + `publishableKey` + `paymentIntentId` to frontend

#### `controllers/StripeController.java`
Single endpoint:
```
POST /api/v1/stripe/payment-intent
Requires: authenticated user
Body: { bookingId, amount }
Returns: { clientSecret, publishableKey, paymentIntentId }
```

---

### Frontend files

#### `pages/campsite-payment/campsite-payment.component.ts`
The payment page component. Handles the full UI flow.

**`ngOnInit()`**
Reads `bookingId` and `amount` from URL query params (set when user clicks "Pay Now" on the booking page).

**`mountCardElement()`**
1. Calls `POST /api/v1/stripe/payment-intent` to get `clientSecret` + `publishableKey`
2. Loads Stripe.js with the publishable key: `await loadStripe(data.publishableKey)`
3. Creates a Stripe `card` element (an iframe with the card form) and mounts it into `#stripe-card-element` div
4. Saves `clientSecret` for later use in `pay()`

**`pay()`**
Decides which payment path to take:
- `CARD` → calls `payWithStripe()`
- `PAYPAL` or `BANK_TRANSFER` → calls `payDirect()` (simulated, no real integration)

**`payWithStripe()`**
1. Calls `stripe.confirmCardPayment(clientSecret, { card: cardElement, name: cardName })`
2. Stripe talks to its servers, validates the card
3. If result has error → shows error message
4. If result is `succeeded` → calls our backend `POST /api/v1/campsite-bookings/pay` to record the payment in our DB with the Stripe `paymentIntentId` as transaction reference
5. On success → `paid = true` → shows confetti animation

**`payDirect()`**
For PayPal/Bank Transfer: directly calls `POST /api/v1/campsite-bookings/pay` with a fake `transactionId` (`TXN-{timestamp}`). No real payment processing.

**`launchConfetti()`**
Fires a 4-second confetti animation using the `canvas-confetti` library after successful payment.

---

### Full payment flow

```
User clicks "Pay Now" on booking #42 (amount: €175)
  → Navigate to /campsite-payment?bookingId=42&amount=175

Component loads → mountCardElement()
  → POST /api/v1/stripe/payment-intent { bookingId: 42, amount: 175 }
      → StripeService creates PaymentIntent on Stripe servers
      → Returns { clientSecret, publishableKey, paymentIntentId }
  → loadStripe(publishableKey)
  → Stripe card iframe rendered in browser

User fills in card: 4242 4242 4242 4242, 12/34, 123
User clicks "Pay €175.00"
  → stripe.confirmCardPayment(clientSecret, { card: cardElement })
      → Stripe validates card, charges it
      → Returns { paymentIntent: { status: "succeeded", id: "pi_xxx" } }
  → POST /api/v1/campsite-bookings/pay { bookingId: 42, amount: 175, method: CARD, transactionId: "pi_xxx" }
      → Backend records payment in DB (status: PAID)
  → paid = true → confetti fires → "Payment Successful" screen shown
```

### Test card numbers (Stripe test mode)
| Card | Result |
|------|--------|
| `4242 4242 4242 4242` | Success |
| `4000 0000 0000 0002` | Card declined |
| `4000 0025 0000 3155` | Requires 3D Secure |
Use any future expiry date and any 3-digit CVV.

---

## 3. Analytics Dashboard

### What it does
Admin-only dashboard showing business metrics: revenue, booking trends, campsite occupancy, fraud detection, and a full CSV export of all bookings.

### Who can access it
Only users with role `ADMIN`. The controller is protected with `@PreAuthorize("hasRole('ADMIN')")`.

---

### Backend files

#### `controllers/AnalyticsController.java`
Four GET endpoints + one CSV download:

| Endpoint | What it returns |
|----------|----------------|
| `GET /api/v1/analytics/overview` | KPI summary: total revenue, total bookings, confirmed, cancelled, fraud suspects count |
| `GET /api/v1/analytics/revenue-by-month` | Revenue per month for last 12 months (for the bar chart) |
| `GET /api/v1/analytics/occupancy` | Per-campsite: total / confirmed / cancelled bookings + cancellation rate % |
| `GET /api/v1/analytics/fraud-suspects?minCancellations=5` | Users with >= 5 cancellations (potential fraud) |
| `GET /api/v1/analytics/export/csv` | Downloads all bookings as a CSV file |

#### `repositories/CampsitePaymentRepository.java`
Two analytics queries:

**`revenueByMonth()`**
```sql
SELECT DATE_FORMAT(paid_at, '%Y-%m') AS month, SUM(amount) AS revenue
FROM campsite_payments
WHERE status = 'PAID'
GROUP BY month ORDER BY month DESC LIMIT 12
```
Returns: `[{ month: "2026-05", revenue: 3200.00 }, ...]`

**`totalRevenue()`**
```sql
SELECT COALESCE(SUM(amount), 0) FROM campsite_payments WHERE status = 'PAID'
```
Returns: total money ever received (e.g. `12450.00`)

#### `repositories/CampsiteBookingRepository.java`
Three analytics queries:

**`occupancyPerCampsite()`**
```sql
SELECT c.id, c.name,
       COUNT(b.id) AS total,
       SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed,
       SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled
FROM campsites c LEFT JOIN campsite_bookings b ON c.id = b.campsite_id
GROUP BY c.id, c.name ORDER BY total DESC
```

**`fraudSuspects(minCancellations)`**
```sql
SELECT u.id, u.email, u.first_name, u.last_name,
       COUNT(b.id) AS cancellations,
       (SELECT COUNT(*) FROM campsite_bookings WHERE camper_id = u.id) AS total_bookings
FROM users u
JOIN campsite_bookings b ON b.camper_id = u.id
WHERE b.status = 'CANCELLED'
GROUP BY u.id HAVING cancellations >= :minCancellations
ORDER BY cancellations DESC
```
A user is flagged if they cancelled 5 or more bookings. Cancellation rate = (cancellations / total) × 100.

**`allBookingsForExport()`**
Returns all booking rows ordered by creation date for the CSV download.

---

### Frontend files

#### `dashboards/campsite-analytics/campsite-analytics.component.ts`
Loads all data on `ngOnInit()` with parallel HTTP calls.

**`loadAll()`**
Makes 4 HTTP GET requests simultaneously:
- `/analytics/overview` → stored in `this.overview`
- `/analytics/revenue-by-month` → stored in `this.revenueData`, computes `maxRevenue` for bar width scaling
- `/analytics/occupancy` → stored in `this.occupancyData`
- `/analytics/fraud-suspects` → stored in `this.fraudSuspects`

**`revenueBarWidth(revenue)`**
Converts a month's revenue into a percentage bar width:
```
barWidth = (month revenue / max revenue) × 100
```
Example: max is €3200, this month is €1600 → bar is 50% wide.

**`exportCsv()`**
Cannot use a simple `<a href>` because the endpoint requires a JWT token in the `Authorization` header (which `<a>` tags can't send). So it uses `fetch()` with the token, gets the response as a Blob, creates a temporary object URL, clicks it programmatically, then revokes the URL.

**`fraudRiskClass(rate)`**
Colors the fraud badge based on cancellation rate:
- rate >= 80% → red (`badge-danger`)
- rate >= 50% → orange (`badge-warning`)
- below 50% → blue (`badge-info`)

#### `dashboards/campsite-analytics/campsite-analytics.component.html`
Pure HTML/CSS dashboard — no external chart library. Revenue bars are CSS `width` percentages driven by `revenueBarWidth()`.

---

### Full analytics flow

```
Admin opens /analytics page
  → CampsiteAnalyticsComponent.ngOnInit() → loadAll()
      → 4 parallel HTTP GET calls to /api/v1/analytics/*
          → AnalyticsController calls repository queries
              → Raw SQL runs on MySQL
                  → Results returned as JSON
                      → Component stores data
                          → Template renders:
                              - 5 KPI cards (revenue, bookings, confirmed, cancelled, fraud)
                              - Revenue bar chart (last 12 months)
                              - Occupancy table (per campsite)
                              - Fraud suspects table

Admin clicks "Export CSV"
  → exportCsv() fires fetch() with JWT
      → GET /api/v1/analytics/export/csv
          → AnalyticsController.exportCsv()
              → allBookingsForExport() SQL query
                  → Streams CSV text rows to response
                      → Browser downloads bookings-export.csv
```

---

## Summary: Which file does what

### Notifications
| File | Role |
|------|------|
| `config/WebSocketConfig.java` | Sets up the WebSocket server (endpoints, broker topics) |
| `config/WebSocketAuthInterceptor.java` | Validates JWT on WebSocket connect |
| `dto/notification/NotificationPayload.java` | The message shape: type + message + referenceId |
| `services/WsNotificationService.java` | Sends messages to specific users or broadcast topics |
| `services/notification.service.ts` | Angular: connects WebSocket, receives messages, persists to localStorage |
| `layout/header/header.component.ts/.html` | Shows bell icon with unread count and dropdown list |

### Stripe Payment
| File | Role |
|------|------|
| `dto/stripe/CreatePaymentIntentRequest.java` | Input: bookingId + amount |
| `dto/stripe/CreatePaymentIntentResponse.java` | Output: clientSecret + publishableKey + paymentIntentId |
| `services/StripeService.java` | Calls Stripe API, creates the PaymentIntent |
| `controllers/StripeController.java` | Exposes `POST /api/v1/stripe/payment-intent` |
| `pages/campsite-payment/campsite-payment.component.ts` | Angular: renders card form, confirms payment, records in backend |

### Analytics
| File | Role |
|------|------|
| `controllers/AnalyticsController.java` | 5 endpoints: overview, revenue-by-month, occupancy, fraud, CSV export |
| `repositories/CampsitePaymentRepository.java` | SQL: total revenue + revenue by month |
| `repositories/CampsiteBookingRepository.java` | SQL: occupancy per campsite + fraud suspects + all bookings for export |
| `dashboards/campsite-analytics/campsite-analytics.component.ts` | Angular: loads all data, computes bar widths, handles CSV download |
| `dashboards/campsite-analytics/campsite-analytics.component.html` | Pure CSS dashboard with KPI cards, bar chart, tables |
