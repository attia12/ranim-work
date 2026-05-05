# Handoff: Campway — AI Recommendation System & Campsite Enhancements

**Generated**: 2026-05-05
**Branch**: master
**Status**: In Progress

## Goal

Full AI-powered campsite recommendation engine (FastAPI + Spring Boot + Angular) plus a series of campsite management improvements: natural features field end-to-end, admin hard-delete, weather NPE fix, and admin loading all campsites.

---

## Completed

- [x] FastAPI microservice (`campsite-recommender/`) — deterministic scoring engine (terrain/budget/season/rating/weather)
- [x] Spring Boot proxy: `AiServiceClient`, `RecommendationService`, `CampsiteRecommendationController`
- [x] Redis cache on recommendations (30min TTL, per user)
- [x] Angular recommendation carousel on home page (OnPush, skeleton, fallback, auto-advance)
- [x] Data seeding: 10 campsites with `naturalFeatures`, 21 CONFIRMED bookings across 4 campers
- [x] Fixed 422 from FastAPI: `LocalDate` serialized as `[2024,7,5]` array → changed to `String` in `BookingHistoryDTO`
- [x] Fixed navigation error in carousel: `@JsonProperty` on `RecommendedCampsiteDTO` serialized camelCase fields as snake_case to Angular → switched to `@JsonAlias` (read-only)
- [x] Fixed `weatherIcon()` NPE: null guard added
- [x] Carousel redesign: image overlay, glassmorphism chips, score ring on image, hover CTA
- [x] `naturalFeatures` field added end-to-end: DB entity → `CampsiteRequest`/`CampsiteResponse` DTOs → service create/update/mapToResponse → Angular model → owner form (toggle chips) → public listing cards
- [x] Admin hard-delete: ADMIN role physically removes campsite + all related records (payments → bookings → status history → availabilities). Owner still soft-deletes (status=DELETED).
- [x] Weather NPE fix: Open-Meteo returns null entries in `weathercode` array — `isForecastSevere()` and `getFirstSevereDay()` now skip nulls
- [x] Admin campsite dashboard: now calls `getAllAdmin()` (all campsites) instead of `getMyCampsites()` (own only)
- [x] `RECOMMENDATION_SYSTEM.md` — plain-language doc of the full flow

---

## Not Yet Done

- [ ] Recommendations only return **1 campsite** currently (only 1 ACTIVE campsite after seeding, because the user's session may have existing campsites with no naturalFeatures). Verify DB has 10 seeded campsites.
- [ ] Recommendation cache invalidation: if a campsite is updated/created/deleted, the Redis cache for affected users is NOT evicted. Must add `@CacheEvict` or TTL is the only expiry.
- [ ] `naturalFeatures` filter on the public `/campsites` search page — UI chips exist but there is no filter param wired to the backend yet.
- [ ] Backend `totalPrice` still ignores `numberOfGuests` (known from previous session — not addressed here).

---

## Failed Approaches (Don't Repeat These)

> **`@JsonProperty` on `RecommendedCampsiteDTO`**: Used `@JsonProperty("campsite_id")` etc. to deserialize FastAPI's snake_case response. This also makes Jackson *serialize* the field as `campsite_id` when Spring returns the response to Angular. Angular's model had camelCase (`campsiteId`) → all fields were `undefined` → navigation crashed with "undefined segment at index 1". Fixed by switching to `@JsonAlias` which only affects deserialization.

> **`status.is5xxServerError()` in `AiServiceClient`**: Only caught 5xx responses. FastAPI returned 422 (Unprocessable Entity) when dates were sent as `[2024,7,5]` arrays. The 422 was silently swallowed and wrapped as "AI service unavailable". Fixed by changing to `status.isError()`.

> **`LocalDate` fields in `BookingHistoryDTO`**: Spring's WebClient serializes `LocalDate` as a JSON array `[2024, 7, 5]`. FastAPI's Pydantic v2 `date` field rejects arrays → 422. Fixed by changing `checkIn`/`checkOut` to `String` and calling `.toString()` in `RecommendationService`.

> **`code >= 65` in `isForecastSevere()`**: WMO code 80/81 (light/moderate rain showers) triggered SUSPENDED even though they are not dangerous for camping. Replaced with explicit `Set.of(65,66,67,73,75,77,82,95,96,99)`.

> **Open-Meteo `end_date = today+16`**: API's max is `today+15` (inclusive). Using `today+16` returned a 400 error. Clamped to `today.plusDays(15)`.

> **Truncating `availabilities` table during DB reset**: Table doesn't exist in this schema. Command was `SET FOREIGN_KEY_CHECKS=0; TRUNCATE campsite_status_history; TRUNCATE campsite_payments; TRUNCATE campsite_bookings; TRUNCATE campsites; SET FOREIGN_KEY_CHECKS=1;`

---

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| `@JsonAlias` instead of `@JsonProperty` on `RecommendedCampsiteDTO` | Read snake_case from FastAPI, write camelCase to Angular — two directions need different annotations |
| Dates as `String` in `BookingHistoryDTO` | WebClient serializes `LocalDate` as array; FastAPI Pydantic rejects it |
| Admin hard-delete, owner soft-delete | Admin needs real cleanup; owner "deleting" should just hide from public (could be re-activated) |
| `naturalFeatures` stored as CSV string | Consistent with existing `pictures` and `amenities` fields; split on read in mapToResponse |
| `@JsonAlias` fields deserialized from FastAPI's snake_case output | FastAPI always outputs snake_case; Spring reads it then re-serializes with camelCase to Angular |
| `getAllAdmin(0, 200)` for admin dashboard | Admin needs to see all campsites, not just their own; page size 200 to avoid pagination |

---

## Current State

**Working**:
- FastAPI recommender running on port 8001 returns scored recommendations
- Spring Boot proxies to FastAPI, caches in Redis, returns camelCase JSON to Angular
- Angular carousel shows cards with image, score ring, weather, feature chips, price
- naturalFeatures toggleable in owner form and displayed on public listing
- Admin hard-delete removes row from DB
- Weather scheduler no longer crashes with NPE on null weathercodes
- Admin dashboard loads all campsites

**Requires manual start**:
- FastAPI: `cd campsite-recommender && python -m uvicorn app.main:app --reload --port 8001`
- Redis must be running (Docker: `docker-compose up redis`)
- Spring Boot: port 9099
- Angular: `cd front && ng serve`

**Broken / To Verify**:
- If Redis is not running, Spring Boot will fail to start (cache configured as redis). Either start Redis or change `spring.cache.type=none` in `application.properties` temporarily.
- Recommendations return empty if no ACTIVE campsites exist — check DB with `SELECT COUNT(*) FROM campsites WHERE status='ACTIVE'`.

---

## Files to Know

| File | Why It Matters |
|------|----------------|
| `campsite-recommender/app/services/scoring_engine.py` | Core scoring: terrain(35%) budget(25%) season(20%) rating(10%) weather(10%) |
| `campsite-recommender/app/services/weather_service.py` | Calls Open-Meteo, 10min in-process cache, concurrent asyncio.gather |
| `campsite-recommender/app/services/profile_builder.py` | Builds UserProfile from booking history (preferred features, budget, season) |
| `back/.../services/AiServiceClient.java` | WebClient POST to FastAPI, 8s timeout, logs full error body on any HTTP error |
| `back/.../services/RecommendationService.java` | Fetches bookings + active campsites, builds `AiRequestDTO`, calls AiServiceClient |
| `back/.../dto/recommendation/RecommendedCampsiteDTO.java` | Uses `@JsonAlias` (not `@JsonProperty`) — critical for correct serialization |
| `back/.../dto/recommendation/BookingHistoryDTO.java` | `checkIn`/`checkOut` are `String`, not `LocalDate` |
| `back/.../config/DataInitializer.java` | Seeds 10 campsites + 21 bookings. Guard: skips if `campsiteRepo.count() > 0` |
| `back/.../dto/weather/WeatherData.java` | `isForecastSevere()` uses explicit SEVERE_CODES set, null-checks each entry |
| `back/.../services/ICampsiteServiceImpl.java` | `delete()`: ADMIN → hard delete in FK order; OWNER → soft delete |
| `front/.../recommendation-carousel/recommendation-carousel.component.ts` | `featureIcon()`, `weatherIcon()` (null-guarded), `svgCircle()` uses r=20 |
| `front/.../models/recommended-campsite.model.ts` | camelCase interface matching Spring's output |
| `front/.../dashboards/campsite-owner/campsite-owner.component.ts` | `isAdmin` flag → calls `getAllAdmin` or `getMyCampsites` |

---

## Code Context

**Recommendation API flow:**
```
Angular GET /api/v1/campsites/recommended
  → Spring: load bookings + active campsites → POST to FastAPI :8001/api/v1/campsites/recommended
  → FastAPI: build profile → score each campsite → fetch weather concurrently → return top 5
  → Spring: cache in Redis 30min → return JSON (camelCase)
  → Angular: render carousel
```

**FastAPI request shape (sent by Spring Boot):**
```json
{
  "user_id": 3,
  "bookings": [
    {
      "campsite_id": 1,
      "campsite_name": "Forest Camp",
      "natural_features": ["FOREST", "LAKE"],
      "price_per_night": 30.0,
      "check_in": "2024-07-10",
      "check_out": "2024-07-15",
      "latitude": 48.85,
      "longitude": 2.35
    }
  ],
  "reviews": [],
  "all_campsites": [
    {
      "id": 5, "name": "Camp Bretagne",
      "natural_features": ["BEACH", "PLAIN"],
      "price_per_night": 55.0,
      "latitude": 47.48, "longitude": -3.1,
      "global_avg_rating": 0.0, "image_url": "..."
    }
  ]
}
```

**FastAPI response shape (returned to Angular via Spring):**
```json
[{
  "campsite_id": 5,
  "campsite_name": "Camp Bretagne — Quiberon",
  "score": 40.77,
  "score_breakdown": { "terrain": 0.0, "budget": 0.73, "season": 0.5, "rating": 0.5, "weather": 0.75, "total": 40.77 },
  "weather_description": "Couvert, 15°C",
  "natural_features": ["BEACH", "PLAIN"],
  "price_per_night": 55.0,
  "image_url": "https://..."
}]
```
⚠️ Spring re-serializes this to camelCase before sending to Angular (`campsiteId`, `campsiteName`, etc.) because `@JsonAlias` is read-only.

**Hard delete order (FK constraint safe):**
```java
campsitePaymentRepository.deleteByBooking_Campsite_Id(id);   // payments first
campsiteBookingRepository.deleteByCampsite_Id(id);           // then bookings
statusHistoryRepository.deleteByCampsite_Id(id);             // then history
availabilityRepository.deleteByCampsite_Id(id);              // then availabilities
campsiteRepository.deleteById(id);                           // then campsite
```

**Angular model (must match Spring's camelCase output):**
```typescript
export interface RecommendedCampsite {
  campsiteId: number;
  campsiteName: string;
  score: number;
  scoreBreakdown: ScoreBreakdown;  // { terrain, budget, season, rating, weather, total }
  weatherDescription: string;
  naturalFeatures: string[];
  pricePerNight: number;
  imageUrl: string | null;
}
```

---

## Resume Instructions

1. **Start services** (in order):
   - Redis: `docker-compose up redis` from repo root
   - Spring Boot: run `ProjetPiDevApplication` (port 9099)
   - FastAPI: `cd campsite-recommender && python -m uvicorn app.main:app --reload --port 8001`
   - Angular: `cd front && ng serve`

2. **Verify DB has seeded campsites:**
   ```sql
   SELECT COUNT(*) FROM campsites WHERE status='ACTIVE';
   -- Expected: 10 (or more)
   -- If 0: truncate and restart Spring Boot to re-seed:
   -- SET FOREIGN_KEY_CHECKS=0; TRUNCATE campsite_status_history; TRUNCATE campsite_payments; TRUNCATE campsite_bookings; TRUNCATE campsites; SET FOREIGN_KEY_CHECKS=1;
   ```

3. **Test recommendations:**
   ```bash
   # Login as camper1
   TOKEN=$(curl -s -X POST http://localhost:9099/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"camper1@campconnect.tn","password":"Dev@12345!"}' | python -m json.tool | grep token | ...)

   curl -s http://localhost:9099/api/v1/campsites/recommended \
     -H "Authorization: Bearer $TOKEN" | python -m json.tool
   ```
   - Expected: JSON array of up to 5 recommended campsites with scores
   - If `{"fallback":true}`: check Spring Boot logs for FastAPI error, ensure FastAPI is running on 8001
   - If empty array `[]`: no ACTIVE campsites in DB — re-seed

4. **Test naturalFeatures form** (owner dashboard):
   - Login as owner → `/dashboard/campsite-owner` → Add New Site
   - Click feature chips (FOREST, LAKE, MOUNTAIN, BEACH, RIVER, PLAIN) — they should toggle green
   - Save → verify campsite shows green feature badges on `/campsites`

5. **Test admin hard-delete:**
   - Login as `admin1@campconnect.tn` / `Dev@12345!`
   - Go to admin campsite management → delete a campsite
   - Verify it no longer appears in `SELECT * FROM campsites WHERE id=X`

6. **Next task (cache eviction):** Add `@CacheEvict(value="recommendations", allEntries=true)` to `ICampsiteServiceImpl.create()`, `update()`, and `delete()` so stale recommendations are cleared when campsites change.

---

## Setup Required

- Redis: `docker-compose up redis` (port 6379)
- MySQL: `localhost:3306`, DB `CampwayDB`, user `root`, password `root`
- FastAPI port: **8001**
- Spring Boot port: **9099**
- Angular port: 4200
- Google Maps API Key: `AIzaSyApgByBPccLFuZ6Blef4a4aS7TmfkzdvII` (already in `front/src/index.html`)
- Test accounts (seeded): `camper1@campconnect.tn` through `camper4@campconnect.tn` / `Dev@12345!`
- Admin: `admin1@campconnect.tn` / `Dev@12345!`
- FastAPI deps: `pip install -r campsite-recommender/requirements.txt`

---

## Edge Cases & Error Handling

- **Redis not running** → Spring Boot fails to start. Either start Redis or set `spring.cache.type=none` in `application.properties`
- **FastAPI down** → Spring returns HTTP 503 `{"error":"recommendation_unavailable","fallback":true}` → Angular shows fallback banner
- **No booking history for user** → terrain/budget scores default to 0; ranking is by weather + rating only; still returns top 5
- **All campsites already booked by user** → empty response; fallback banner shown
- **Null weathercode from Open-Meteo** → skipped (continue), does not crash scheduler
- **Admin deletes campsite with bookings** → payments deleted first (FK safe), then bookings, then campsite

---

## Warnings

- **`@JsonAlias` vs `@JsonProperty`** on `RecommendedCampsiteDTO` — MUST stay as `@JsonAlias`. If changed back to `@JsonProperty`, Angular receives snake_case and all campsite IDs are `undefined` → navigation crashes.
- **Angular uses traditional `NgModule`** — do NOT create standalone components. All new components must be declared in `app.module.ts`.
- **`npm install` in `front/`** requires `--legacy-peer-deps`.
- **Open-Meteo max forecast** = `today+15` days (not 16). The +16 off-by-one was already fixed.
- **DataInitializer guard** is `campsiteRepo.count() > 0` — if ANY campsites exist, seeding is skipped entirely. Truncate all related tables first, then restart Spring Boot.
- **`SecurityConfig.java`** is in `back/.../jwt/` not `back/.../config/`.
- **Backend port is 9099** — not 8080 or 8222.
- **WMO codes 80/81** (light/moderate rain showers) must NOT trigger SUSPENDED. The explicit `SEVERE_CODES` set excludes them. Do not revert to `code >= 65`.
