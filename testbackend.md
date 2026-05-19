# Campway — Backend JUnit Test Report

**Module:** Official Campsite & Booking (M1) · Outdoor Campsite & Booking (M2)
**Stack:** Spring Boot 3.4.4 · JUnit 5 · Mockito · Spring MockMvc
**Date run:** 2026-05-19
**Final result:** ✅ 38 / 38 tests passed — 0 failures, 0 errors

---

## Test Files

| File | Layer | Tests | Status |
|------|-------|-------|--------|
| `CampsiteControllerTest.java` | Controller (MockMvc) | 6 | ✅ All pass |
| `CampsiteBookingControllerTest.java` | Controller (MockMvc) | 4 | ✅ All pass |
| `ICampsiteServiceImplTest.java` | Service (Unit) | 11 | ✅ All pass |
| `ICampsiteBookingServiceImplTest.java` | Service (Unit) | 9 | ✅ All pass |
| `IOutdoorCampsiteServiceImplTest.java` | Service (Unit) | 8 | ✅ All pass |

---

## Test Descriptions

### CampsiteControllerTest — 6 tests

`@WebMvcTest(CampsiteController.class)` loaded with real `SecurityConfig` and `PasswordConfig` via `@Import`. `JwtService`, `CustomUserDetailsService`, and `ICampsiteService` are mocked.

| Test Method | What Is Verified |
|-------------|-----------------|
| `getById_publicEndpoint_returns200` | `GET /api/v1/campsites/1` — no auth needed → HTTP 200, body has correct `id` and `name` |
| `search_publicEndpoint_returns200` | `GET /api/v1/campsites` — no auth needed → HTTP 200, first result in `content[]` matches |
| `create_ownerRole_returns201` | `POST /api/v1/campsites` with `COMPSITEOWNERS` role → HTTP 201 |
| `create_camperRole_returns403` | `POST /api/v1/campsites` with `COMPERS` role → HTTP 403 (access denied) |
| `suspend_adminRole_returns200` | `PATCH /api/v1/campsites/1/suspend` with `ADMIN` role → HTTP 200 |
| `create_unauthenticated_returns403` | `POST /api/v1/campsites` with no credentials → HTTP 403 |

---

### CampsiteBookingControllerTest — 4 tests

`@WebMvcTest(CampsiteBookingController.class)` with same security setup. `ICampsiteBookingService` is mocked.

| Test Method | What Is Verified |
|-------------|-----------------|
| `create_authenticated_returns201` | `POST /api/v1/campsite-bookings` with authenticated user → HTTP 201, response `id` matches |
| `create_unauthenticated_returns401or403` | `POST` with no credentials → HTTP 401 or 403 |
| `getMyBookings_authenticated_returns200` | `GET /api/v1/campsite-bookings/my` with authenticated user → HTTP 200, paginated result |
| `cancel_authenticated_returns200` | `PATCH /api/v1/campsite-bookings/1/cancel` with authenticated user → HTTP 200 |

---

### ICampsiteServiceImplTest — 11 tests

Pure `@ExtendWith(MockitoExtension.class)` unit tests against `ICampsiteServiceImpl`. No Spring context loaded.

| Test Method | What Is Verified |
|-------------|-----------------|
| `create_ownerRole_success` | Owner creates campsite → response id=10, `save()` called once |
| `create_camperRole_throwsException` | Camper tries to create → `IllegalStateException: "Only OWNER or ADMIN"` |
| `create_userNotFound_throwsException` | Unknown userId → `ResourceNotFoundException` |
| `update_byOwner_success` | Owner updates own campsite → non-null response |
| `update_byWrongOwner_throwsException` | Different owner tries to update → `IllegalStateException: "do not own"` |
| `delete_byAdmin_hardDeletes` | Admin deletes → all 5 repository delete methods called in correct FK-safe order |
| `getById_found_returnsResponse` | Existing campsite → response id=10, name="Test Campsite" |
| `getById_notFound_throwsException` | Missing id=99 → `ResourceNotFoundException` |
| `suspend_changesStatusToSuspended` | `suspend(10)` → campsite status becomes `SUSPENDED` |
| `activate_changesStatusToActive` | `activate(10)` on SUSPENDED → campsite status becomes `ACTIVE` |
| `getByOwner_returnsPaginatedPage` | Owner query → page has 1 element |

---

### ICampsiteBookingServiceImplTest — 9 tests

Unit tests for `ICampsiteBookingServiceImpl`.

| Test Method | What Is Verified |
|-------------|-----------------|
| `create_validRequest_success` | Valid dates, capacity available → booking saved, response `campsiteId=10` |
| `create_capacityExceeded_throwsException` | 45 guests already, +10 requested, capacity=50 → `IllegalStateException: "capacity"` |
| `create_checkInNotBeforeCheckOut_throwsException` | checkIn == checkOut → `IllegalArgumentException` |
| `create_pastCheckIn_throwsException` | checkIn in the past → `IllegalArgumentException` |
| `cancel_byCamperWithin2Days_throwsException` | checkIn = today+1 → `IllegalStateException: "2 days"` |
| `cancel_byCamperAfter2Days_success` | checkIn = today+5 → booking status becomes `CANCELLED` |
| `cancel_byOtherUser_throwsException` | Stranger tries to cancel → `IllegalStateException: "Only the booking camper"` |
| `confirm_pendingBooking_statusBecomesConfirmed` | PENDING booking confirmed → status becomes `CONFIRMED`, email sent |
| `getById_notFound_throwsException` | Missing id=99 → `ResourceNotFoundException` |

---

### IOutdoorCampsiteServiceImplTest — 8 tests

Unit tests for `IOutdoorCampsiteServiceImpl`.

| Test Method | What Is Verified |
|-------------|-----------------|
| `propose_validRequest_returnsPending` | Valid proposer → response status is `PENDING` |
| `propose_userNotFound_throwsException` | Unknown proposer id → `ResourceNotFoundException` |
| `moderate_approve_changesStatusAndSendsEmail` | APPROVE action → status `APPROVED`, email sent with `approved=true`, WS notification sent |
| `moderate_reject_changesStatusAndSendsEmail` | REJECT action → status `REJECTED`, email sent with `approved=false`, WS notification sent |
| `moderate_unknownAction_throwsException` | Unknown action string → `IllegalArgumentException: "Unknown moderation action"` |
| `update_byProposer_pendingStatus_success` | Proposer edits PENDING site → non-null response |
| `update_byNonProposer_throwsException` | Non-proposer tries to edit → `IllegalStateException: "Only the proposer"` |
| `getById_notFound_throwsException` | Missing id=99 → `ResourceNotFoundException` |

---

## Fixes Applied

The 5 test files were originally written before several service dependencies were added. Running the tests produced **5 NullPointerException errors** in 3 files. The following fixes were made:

### 1. `ICampsiteServiceImplTest.java`

**Problem:** `ICampsiteServiceImpl` gained 4 additional repository dependencies after tests were written: `AvailabilityRepository`, `CampsiteBookingRepository`, `CampsitePaymentRepository`, `CampsiteStatusHistoryRepository`. The admin-delete path calls all four, but none were mocked, causing NPE.

Additionally, the test `delete_byAdmin_softDeletes` was incorrectly named and used a wrong assertion — admin performs a **hard delete** (physical DB removal), not a soft delete (setting status=DELETED).

**Fix:**
- Added `@Mock` for all 4 missing repositories
- Renamed test to `delete_byAdmin_hardDeletes`
- Changed assertion from `campsite.getStatus() == DELETED` to verifying all 5 delete repository calls in FK-safe order:
  ```
  campsitePaymentRepository.deleteByBooking_Campsite_Id(id)
  campsiteBookingRepository.deleteByCampsite_Id(id)
  statusHistoryRepository.deleteByCampsite_Id(id)
  availabilityRepository.deleteByCampsite_Id(id)
  campsiteRepository.deleteById(id)
  ```

### 2. `ICampsiteBookingServiceImplTest.java`

**Problem:** `ICampsiteBookingServiceImpl` gained 3 new dependencies (`WsNotificationService`, `CampsiteStatusEvaluator`, `CampsiteStatusUpdater`) for real-time notifications and automatic status re-evaluation after cancellation. None were mocked.

- `cancel_byCamperAfter2Days_success` — called `statusEvaluator.evaluate(campsite)` on null → NPE
- `confirm_pendingBooking_statusBecomesConfirmed` — called `wsNotificationService.sendToUser(...)` on null → NPE

**Fix:**
- Added `@Mock WsNotificationService wsNotificationService`
- Added `@Mock CampsiteStatusEvaluator statusEvaluator`
- Added `@Mock CampsiteStatusUpdater statusUpdater`
- In `cancel_byCamperAfter2Days_success`: stubbed `statusEvaluator.evaluate()` to return `Evaluation(ACTIVE, "...")`, stubbed `statusUpdater.applyIfChanged()` (returns `boolean`) to return `false`, and stubbed `wsNotificationService.sendToUser()` with `doNothing()`
- In `confirm_pendingBooking_statusBecomesConfirmed`: added `doNothing().when(wsNotificationService).sendToUser(...)`

### 3. `IOutdoorCampsiteServiceImplTest.java`

**Problem:** `IOutdoorCampsiteServiceImpl` gained `WsNotificationService` for sending real-time proposal notifications. Not mocked in tests, causing NPE in both moderation tests.

**Fix:**
- Added `@Mock WsNotificationService wsNotificationService`
- In both `moderate_approve_*` and `moderate_reject_*`: added `doNothing().when(wsNotificationService).sendToUser(any(), any())`
- Also added `verify(wsNotificationService).sendToUser(any(), any())` to confirm the notification is sent

---

## Maven Command Used

```bash
cd back
mvn test -Dtest="CampsiteControllerTest,CampsiteBookingControllerTest,ICampsiteServiceImplTest,ICampsiteBookingServiceImplTest,IOutdoorCampsiteServiceImplTest"
```

---

## Final Results

```
[INFO] Tests run: 4,  Failures: 0, Errors: 0, Skipped: 0  CampsiteBookingControllerTest
[INFO] Tests run: 6,  Failures: 0, Errors: 0, Skipped: 0  CampsiteControllerTest
[INFO] Tests run: 9,  Failures: 0, Errors: 0, Skipped: 0  ICampsiteBookingServiceImplTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0  ICampsiteServiceImplTest
[INFO] Tests run: 8,  Failures: 0, Errors: 0, Skipped: 0  IOutdoorCampsiteServiceImplTest

[INFO] Tests run: 38, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Test Architecture Notes

**Controller tests** use `@WebMvcTest` (no DB, no full Spring context) with real Spring Security loaded via `@Import`. Authentication is simulated with `SecurityMockMvcRequestPostProcessors.user(User.builder()...build())` using the actual domain `User` class, which is required because controllers use `@AuthenticationPrincipal User` rather than a generic `UserDetails`.

**Service tests** use `@ExtendWith(MockitoExtension.class)` (pure unit tests, no Spring at all). `@InjectMocks` constructs the service class via Lombok's `@RequiredArgsConstructor`-generated constructor, injecting all `@Mock`-annotated fields.

**Key rule:** every `final` field in the service implementation must have a corresponding `@Mock` in the test class, even if the test method under examination does not call that dependency — because `@InjectMocks` injects via constructor and leaves unmocked fields as `null`.
