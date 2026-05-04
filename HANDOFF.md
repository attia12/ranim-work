# Handoff: Campway Full-Stack Feature Fixes & Enhancements

**Generated**: 2026-05-04
**Branch**: master
**Status**: In Progress

## Goal

Fix and enhance the Campway campsite booking platform (Spring Boot + Angular). Multiple features implemented and bugs fixed in a single session on top of the previous WebSocket/Stripe/Analytics implementation.

## Completed

- [x] Replace all TND currency with EUR across the entire frontend (12 files)
- [x] Add Google Maps Places Autocomplete to campsite creation/edit form — auto-fills city, country, address, lat, lng
- [x] Campsite booking: validate dates against availability windows in real-time (frontend blocks submit)
- [x] Campsite booking: total price now multiplies by number of guests (reactive)
- [x] OUTDOOR campsite type: price hidden everywhere in the project (not shown as FREE either — completely absent)
- [x] OUTDOOR campsite form: price input hidden, shows disabled "FREE" field, auto-sets pricePerNight=0, validators removed
- [x] OUTDOOR booking: skips payment page entirely, navigates directly to `/my-bookings` after confirm
- [x] My Bookings: OUTDOOR campsite bookings appear in the "Outdoor (Free)" tab, not "Official"
- [x] Cancel booking 404 fix: OUTDOOR campsite bookings cancel via `/api/v1/campsite-bookings/{id}/cancel`
- [x] Cancel booking → campsite status immediately re-evaluated (no longer waits for hourly scheduler)
- [x] Weather: switched from current weather snapshot to daily forecast over campsite's `startDate → endDate`
- [x] Removed startDate PENDING block from status evaluator (was preventing weather evaluation)
- [x] Backend: added `campsiteType` field to `CampsiteBookingResponse` DTO and mapper

## Not Yet Done

- [ ] Backend `create` booking: `totalPrice` still computes `nights × pricePerNight` only — does NOT multiply by `numberOfGuests`. Frontend shows `nights × price × guests`. **These are inconsistent.**
- [ ] No campsite owner accounts seeded — `DataInitializer` only seeds campers, providers, delivery agents, admins. Must register manually.
- [ ] OUTDOOR booking: no success confirmation message shown after booking (just redirects to `/my-bookings` silently)
- [ ] `PENDING` status no longer reachable — startDate check removed. Campsites with future startDates now get weather-evaluated immediately. If PENDING is needed for future use, re-add after weather check.

## Failed Approaches (Don't Repeat These)

> **Merging OUTDOOR campsite bookings into `outdoorBookings: OutdoorBookingResponse[]`**: Mapped `CampsiteBookingResponse` to `OutdoorBookingResponse` shape and pushed into one array. Cancel called `outdoorService.cancelBooking()` → hit `/api/v1/outdoor-bookings/{id}/cancel` → **404** because the ID belonged to `campsite_bookings` table, not `outdoor_bookings`. Fixed by keeping a separate `outdoorCampsiteBookings: CampsiteBookingResponse[]` array with its own cancel method pointing to `bookingService.cancel()`.

> **Weather check using startDate guard**: The evaluator checked `startDate > today → PENDING` before weather, so changing coordinates never reached the weather step. Everything returned PENDING regardless of coordinates. Removed the check entirely; weather is now always evaluated.

> **Current weather only (`getCurrentWeather`)**: Only checked present conditions. If weather was fine today but campsite dates are in a stormy week, status would show ACTIVE. Replaced with `getForecastWeather()` using Open-Meteo daily forecast API over the full campsite date range.

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| OUTDOOR price completely hidden (not shown as FREE) | User explicitly: "hide completely, don't display the price" |
| OUTDOOR bookings use campsite booking service, not outdoor service | OUTDOOR campsites created by owners go through campsite booking flow, not the separate outdoor campsite module |
| Immediate status re-evaluation on booking cancel | Hourly scheduler caused campsite to stay FULL after cancellation. Re-evaluate inline fixes it instantly |
| Forecast range = campsite `startDate → endDate` | User wants weather checked for actual operating period, not just today |
| Open-Meteo free tier max 16 days ahead | Forecast end date clamped to `today+16` as hard API limit |
| Google Maps Places Autocomplete via vanilla JS | No library installed; `(window as any).google` used directly. Re-initialized on each modal open via `setTimeout(() => initPlacesAutocomplete(), 100)` |

## Current State

**Working**:
- All currency displays show EUR
- Google Maps Places Autocomplete in campsite owner form
- Availability validation in booking form (blocks submit if dates outside open windows or blocked)
- Price reactively updates when guest count changes
- OUTDOOR type: no price shown anywhere, booking skips payment, goes to my-bookings
- My Bookings tabs correctly separate official vs outdoor
- Cancel works for both outdoor campsite bookings and true outdoor bookings
- Weather status uses daily forecast over campsite date range
- Campsite reappears on `/campsites` immediately after booking is cancelled

**Known Divergence**:
- Backend `totalPrice = nights × pricePerNight` (ignores guests). Frontend displays `nights × price × guests`. The amount stored in DB and sent to payment page diverges from what the user sees.

**Uncommitted Changes**: None — all committed (`6792f72`, `11a2f7e`)

## Files to Know

| File | Why It Matters |
|------|----------------|
| `front/src/index.html` | Google Maps JS API script tag |
| `front/src/app/dashboards/campsite-owner/campsite-owner.component.ts` | Places autocomplete init, OUTDOOR type watcher removes validators, re-init on modal open |
| `front/src/app/dashboards/campsite-owner/campsite-owner.component.html` | OUTDOOR price field hidden, shows "FREE"; Places autocomplete input |
| `front/src/app/frontoffice/pages/campsite-booking/campsite-booking.component.ts` | Availability check, guest-reactive price, OUTDOOR skip-payment logic, loads availabilities on init |
| `front/src/app/frontoffice/pages/campsite-booking/campsite-booking.component.html` | Availability error alert, price summary hidden for OUTDOOR, button text changes |
| `front/src/app/frontoffice/pages/my-bookings/my-bookings.component.ts` | Three arrays: `officialBookings`, `outdoorCampsiteBookings`, `outdoorBookings` with separate cancel methods |
| `front/src/app/frontoffice/pages/my-bookings/my-bookings.component.html` | Outdoor tab shows both `outdoorCampsiteBookings` and `outdoorBookings` separately |
| `front/src/app/models/campsite-booking.model.ts` | Has `campsiteType?: 'OFFICIAL' \| 'OUTDOOR'` in `CampsiteBookingResponse` |
| `back/.../services/CampsiteStatusEvaluator.java` | startDate check removed; uses forecast range startDate→endDate |
| `back/.../services/WeatherService.java` | Added `getForecastWeather()` using Open-Meteo daily endpoint |
| `back/.../dto/weather/WeatherData.java` | Added `DailyForecast` inner class, `isForecastSevere()`, `getFirstSevereDay()` |
| `back/.../services/ICampsiteBookingServiceImpl.java` | Injects evaluator + updater; re-evaluates campsite status immediately on cancel |
| `back/.../dto/campsitebooking/CampsiteBookingResponse.java` | Added `campsiteType: CampsiteType` field |

## Code Context

**My Bookings — three separate arrays:**
```typescript
officialBookings: CampsiteBookingResponse[]        // campsiteType === 'OFFICIAL'
outdoorCampsiteBookings: CampsiteBookingResponse[] // campsiteType === 'OUTDOOR', cancel via bookingService
outdoorBookings: OutdoorBookingResponse[]           // true outdoor (separate module), cancel via outdoorService

cancelOutdoorCampsite(id) → bookingService.cancel(id)        // /api/v1/campsite-bookings/{id}/cancel
cancelOutdoor(id)          → outdoorService.cancelBooking(id) // /api/v1/outdoor-bookings/{id}/cancel
```

**Availability check in booking form:**
```typescript
get availabilityError(): string {
  // '' if no windows defined → booking allowed freely
  // error if: range overlaps blocked window
  // error if: no open window fully covers the range
  // error if: open window exists but numberOfPlaces < guests
}
// Submit disabled: bookingForm.invalid || nights===0 || !!availabilityError
```

**OUTDOOR price suppression pattern (all templates):**
```html
<ng-container *ngIf="campsite.type !== 'OUTDOOR'">
  <!-- price display here — completely absent for OUTDOOR -->
</ng-container>
```

**Weather forecast API (Open-Meteo):**
```
GET https://api.open-meteo.com/v1/forecast
  ?latitude={lat}&longitude={lon}
  &daily=weathercode,windspeed_10m_max
  &timezone=auto
  &start_date={campsite.startDate or today}
  &end_date={campsite.endDate or today+16, clamped to today+16 max}
```

**Status evaluator order (after changes):**
```
endDate < today                                       → EXPIRED
Any day in [startDate→endDate] forecast: code≥65 OR wind>60 → SUSPENDED
sumGuestsOverlapping(today, today+1) >= capacity      → FULL
otherwise                                             → ACTIVE
```
Note: `PENDING` status is no longer produced by the evaluator. StartDate check was removed.

**Google Maps autocomplete init (re-runs on every modal open):**
```typescript
// Called in openCreateForm() and openEditForm() with setTimeout 100ms
private initPlacesAutocomplete(): void {
  const input = document.getElementById('location-autocomplete') as HTMLInputElement;
  if (!input || !(window as any).google?.maps?.places) return;
  const ac = new (window as any).google.maps.places.Autocomplete(input, { types: ['geocode'] });
  ac.addListener('place_changed', () => {
    // fills city, country, address, latitude, longitude into campsiteForm
  });
}
```

## Resume Instructions

1. Start backend: `ProjetPiDevApplication` from `back/` (port 9099)
2. Start frontend: `cd front && ng serve` → `http://localhost:4200`
3. Register a campsite owner at `/register` (role: Campsite Owner)
4. Log in as campsite owner → `/dashboard/campsite-owner` → Add New Site
5. **Test Google Maps autocomplete**: type "Paris" in "Search Location" → dropdown appears → select → city/country/lat/lng auto-filled
6. **Test OUTDOOR type**: select type=OUTDOOR → price field replaced by disabled "FREE" input
7. **Test weather forecast**: create campsite with startDate=today, endDate=today+10, lat=`51.5085`, lng=`-0.1257` (London) → Refresh Status
   - Expected SUSPENDED: reason shows `Severe weather forecast (2026-05-04 → 2026-05-14): date=..., code=..., wind=...`
   - If ACTIVE: try Bergen Norway `60.3913, 5.3221` — higher chance of rain
   - If still ACTIVE: try `65.0, -18.0` (Iceland) — reliable wind > 60
8. **Fix backend price divergence** (not done): in `ICampsiteBookingServiceImpl.create()` multiply total by `numberOfGuests`:
   ```java
   BigDecimal total = campsite.getPricePerNight() != null
       ? campsite.getPricePerNight()
           .multiply(BigDecimal.valueOf(nights))
           .multiply(BigDecimal.valueOf(request.getNumberOfGuests()))
       : BigDecimal.ZERO;
   ```

## Setup Required

- Google Maps API Key: `AIzaSyApgByBPccLFuZ6Blef4a4aS7TmfkzdvII` (already in `front/src/index.html`)
- Backend port: `9099` (not 8080)
- MySQL on `localhost:3306`, DB `CampwayDB`
- No campsite owner accounts seeded — register manually
- All other setup same as previous handoff (Stripe keys, MailDev, etc.)
- Test password for seeded accounts: `Dev@12345!`
- Admin: `admin1@campconnect.tn` / `Dev@12345!`

## Edge Cases & Error Handling

- Campsite has no lat/lng → weather check skipped, status goes ACTIVE (safe fallback)
- Campsite has no startDate → forecast starts from today
- Campsite has no endDate → forecast ends at today+16 (Open-Meteo max)
- Availability windows not set → availability check skipped, booking allowed freely
- OUTDOOR campsite booked → skips payment page, navigates directly to `/my-bookings`
- Cancelled booking → campsite status re-evaluated immediately inline (not waiting for hourly scheduler)
- Open-Meteo unreachable → `Optional.empty()` returned, weather step silently skipped → status goes ACTIVE

## Warnings

- `PENDING` status no longer produced by evaluator — startDate check was removed by user request. If you need PENDING back, add it AFTER the weather check, not before.
- `outdoorCampsiteBookings` requires backend to return `campsiteType` in `CampsiteBookingResponse`. Already implemented in DTO and mapper. If this field is null/missing, all bookings fall into `officialBookings`.
- Open-Meteo forecast only goes 16 days ahead. endDate beyond today+16 is silently clamped.
- Google Maps script loaded async/defer in `index.html` — `initPlacesAutocomplete()` uses a 100ms setTimeout to wait for DOM + script. On slow connections this may fail silently (no error shown).
- Angular project uses traditional `NgModule` — do NOT create standalone components.
- `npm install` in `front/` always needs `--legacy-peer-deps`.
- Backend port is `9099` — not 8080 or 8222. No API gateway.
- `SecurityConfig.java` is in `back/.../jwt/` NOT `back/.../config/`.
