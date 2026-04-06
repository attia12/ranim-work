# Campway — Module 1 & 2 Complete Implementation Documentation

> **Project:** Campway — Camping Platform
> **Stack:** Spring Boot 3.4.4 · Angular 18.2.0 · MySQL · Spring Security 6 (JWT)
> **Modules covered:** Official Campsite & Booking (Module 1) · Outdoor Campsite & Booking (Module 2)

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Module 1 — Official Campsite & Booking](#module-1--official-campsite--booking)
   - [Backend: Entities](#module-1-backend-entities)
   - [Backend: Enums](#module-1-backend-enums)
   - [Backend: DTOs](#module-1-backend-dtos)
   - [Backend: Repositories](#module-1-backend-repositories)
   - [Backend: Services](#module-1-backend-services)
   - [Backend: Controllers](#module-1-backend-controllers)
3. [Module 2 — Outdoor Campsite & Booking](#module-2--outdoor-campsite--booking)
   - [Backend: Entities](#module-2-backend-entities)
   - [Backend: Enums](#module-2-backend-enums)
   - [Backend: DTOs](#module-2-backend-dtos)
   - [Backend: Repositories](#module-2-backend-repositories)
   - [Backend: Services](#module-2-backend-services)
   - [Backend: Controllers](#module-2-backend-controllers)
4. [Security Configuration](#security-configuration)
5. [Email Notifications](#email-notifications)
6. [Backend Tests](#backend-tests)
7. [Frontend — Services](#frontend--services)
8. [Frontend — Components](#frontend--components)
9. [Frontend — Routes & Guards](#frontend--routes--guards)
10. [Module Comparison](#module-comparison)
11. [Full File Tree](#full-file-tree)
12. [Email System — MailDev & HTML Templates](#email-system--maildev--html-templates)
13. [Frontend Navigation Updates](#frontend-navigation-updates)

---

## Architecture Overview

```
back/src/main/java/tn/esprit/projetpidev/
├── domain/
│   ├── enums/
│   ├── Campsite.java
│   ├── Availability.java
│   ├── CampsiteBooking.java
│   ├── CampsitePayment.java
│   ├── OutdoorCampsite.java
│   ├── OutdoorAvailability.java
│   └── OutdoorBooking.java
├── dto/
│   ├── campsite/
│   ├── availability/
│   ├── campsitebooking/
│   ├── campsitepayment/
│   ├── outdoorcampsite/
│   ├── outdooravailability/
│   └── outdoorbooking/
├── repositories/
├── services/
│   └── impl/
├── controllers/
└── jwt/   (SecurityConfig — shared, not new)

front/src/app/
├── services/
│   ├── campsite.service.ts
│   ├── campsite-booking.service.ts
│   └── outdoor-campsite.service.ts
├── frontoffice/pages/
│   ├── campsites/
│   ├── campsite-detail/
│   ├── campsite-booking/
│   ├── campsite-payment/
│   ├── my-bookings/
│   ├── outdoor-trips/
│   ├── outdoor-campsite-detail/
│   ├── outdoor-booking/
│   ├── propose-outdoor/
│   └── my-proposals/
├── dashboards/
│   ├── campsite-owner/
│   └── admin-outdoor-moderation/
└── guards/
    └── require-auth.guard.ts
```

---

## Module 1 — Official Campsite & Booking

### Module 1 Backend Entities

---

#### `Campsite`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/Campsite.java`
**Table:** `campsites`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `name` | `String` | `@NotBlank` | Required |
| `description` | `String` | — | Optional text |
| `country` | `String` | `@NotBlank` | Required |
| `city` | `String` | `@NotBlank` | Required |
| `address` | `String` | — | Optional |
| `latitude` | `Double` | — | Optional GPS |
| `longitude` | `Double` | — | Optional GPS |
| `capacity` | `Integer` | `@Min(1)` | Max guests |
| `type` | `CampsiteType` | `@Enumerated(STRING)` | OFFICIAL or OUTDOOR |
| `pricePerNight` | `BigDecimal` | `@DecimalMin("0.0")` | Decimal |
| `pictures` | `String` | `@Column(columnDefinition="TEXT")` | Comma-separated URLs |
| `amenities` | `String` | `@Column(columnDefinition="TEXT")` | Comma-separated values |
| `rules` | `String` | — | Optional |
| `status` | `CampsiteStatus` | `@Enumerated(STRING)` | Default `ACTIVE` |
| `owner` | `User` | `@ManyToOne(fetch=LAZY)` | FK to `users` |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | Auto-set |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | Auto-set |

> **Note:** `pictures` and `amenities` are stored as CSV strings. They are split into `List<String>` when mapped to the response DTO.

---

#### `Availability`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/Availability.java`
**Table:** `campsite_availabilities`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `campsite` | `Campsite` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `campsites` |
| `startDate` | `LocalDate` | `@NotNull` | Window start |
| `endDate` | `LocalDate` | `@NotNull` | Window end |
| `numberOfPlaces` | `Integer` | `@Min(0)` | Available slots |
| `weatherCondition` | `String` | — | Optional description |
| `isBlocked` | `boolean` | — | Default `false` |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | Auto-set |

---

#### `CampsiteBooking`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/CampsiteBooking.java`
**Table:** `campsite_bookings`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `campsite` | `Campsite` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `campsites` |
| `camper` | `User` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `users` |
| `checkInDate` | `LocalDate` | `@NotNull` | Arrival date |
| `checkOutDate` | `LocalDate` | `@NotNull` | Departure date |
| `numberOfGuests` | `Integer` | `@Min(1)` | Guest count |
| `totalPrice` | `BigDecimal` | `@Column(precision=10, scale=2)` | Computed at create |
| `status` | `CampsiteBookingStatus` | `@Enumerated(STRING)` | Default `PENDING` |
| `cancellationReason` | `String` | — | Optional |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | Auto-set |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | Auto-set |

---

#### `CampsitePayment`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/CampsitePayment.java`
**Table:** `campsite_payments`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `booking` | `CampsiteBooking` | `@OneToOne(fetch=LAZY) @JoinColumn(unique=true)` | One payment per booking |
| `amount` | `BigDecimal` | `@NotNull @DecimalMin("0.01") precision=10,scale=2` | Payment amount |
| `transactionId` | `String` | — | Optional gateway ref |
| `referenceCode` | `String` | — | Optional reference |
| `method` | `CampsitePaymentMethod` | `@Enumerated(STRING)` | CARD/PAYPAL/BANK_TRANSFER |
| `status` | `CampsitePaymentStatus` | `@Enumerated(STRING)` | Default `PAID` |
| `paidAt` | `LocalDateTime` | — | Set at pay time |

---

### Module 1 Backend Enums

All files in: `back/src/main/java/tn/esprit/projetpidev/domain/enums/`

| File | Values |
|------|--------|
| `CampsiteType.java` | `OFFICIAL`, `OUTDOOR` |
| `CampsiteStatus.java` | `ACTIVE`, `SUSPENDED`, `DELETED` |
| `CampsiteBookingStatus.java` | `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| `CampsitePaymentMethod.java` | `CARD`, `PAYPAL`, `BANK_TRANSFER` |
| `CampsitePaymentStatus.java` | `PAID`, `REFUNDED`, `FAILED` |

---

### Module 1 Backend DTOs

#### Package: `dto/campsite/`

##### `CampsiteRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsite/CampsiteRequest.java`
**Annotations:** `@Data`

| Field | Type | Validation |
|-------|------|-----------|
| `name` | `String` | `@NotBlank` |
| `description` | `String` | — |
| `country` | `String` | `@NotBlank` |
| `city` | `String` | `@NotBlank` |
| `address` | `String` | — |
| `latitude` | `Double` | — |
| `longitude` | `Double` | — |
| `capacity` | `Integer` | `@Min(1)` |
| `type` | `CampsiteType` | `@NotNull` |
| `pricePerNight` | `BigDecimal` | `@NotNull @DecimalMin("0.0")` |
| `pictures` | `String` | — CSV string |
| `amenities` | `String` | — CSV string |
| `rules` | `String` | — |

##### `CampsiteResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsite/CampsiteResponse.java`
**Annotations:** `@Data`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | — |
| `name` | `String` | — |
| `description` | `String` | — |
| `country` | `String` | — |
| `city` | `String` | — |
| `address` | `String` | — |
| `latitude` | `Double` | — |
| `longitude` | `Double` | — |
| `capacity` | `Integer` | — |
| `type` | `CampsiteType` | — |
| `pricePerNight` | `BigDecimal` | — |
| `pictures` | `List<String>` | Split from CSV |
| `amenities` | `List<String>` | Split from CSV |
| `rules` | `String` | — |
| `status` | `CampsiteStatus` | — |
| `ownerId` | `Long` | — |
| `ownerName` | `String` | Full name of owner |
| `createdAt` | `LocalDateTime` | — |
| `updatedAt` | `LocalDateTime` | — |

---

#### Package: `dto/availability/`

##### `AvailabilityRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/availability/AvailabilityRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `campsiteId` | `Long` | `@NotNull` |
| `startDate` | `LocalDate` | `@NotNull` |
| `endDate` | `LocalDate` | `@NotNull` |
| `numberOfPlaces` | `Integer` | `@Min(0)` |
| `weatherCondition` | `String` | — |
| `isBlocked` | `boolean` | Default `false` |

##### `AvailabilityResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/availability/AvailabilityResponse.java`

| Field | Type |
|-------|------|
| `id` | `Long` |
| `campsiteId` | `Long` |
| `campsiteName` | `String` |
| `startDate` | `LocalDate` |
| `endDate` | `LocalDate` |
| `numberOfPlaces` | `Integer` |
| `weatherCondition` | `String` |
| `isBlocked` | `boolean` |
| `createdAt` | `LocalDateTime` |

---

#### Package: `dto/campsitebooking/`

##### `CampsiteBookingRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsitebooking/CampsiteBookingRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `campsiteId` | `Long` | `@NotNull` |
| `checkInDate` | `LocalDate` | `@NotNull` |
| `checkOutDate` | `LocalDate` | `@NotNull` |
| `numberOfGuests` | `Integer` | `@Min(1)` |

##### `CampsiteBookingResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsitebooking/CampsiteBookingResponse.java`

| Field | Type |
|-------|------|
| `id` | `Long` |
| `campsiteId` | `Long` |
| `campsiteName` | `String` |
| `campsiteCountry` | `String` |
| `campsiteCity` | `String` |
| `camperId` | `Long` |
| `camperFullName` | `String` |
| `checkInDate` | `LocalDate` |
| `checkOutDate` | `LocalDate` |
| `numberOfGuests` | `Integer` |
| `totalPrice` | `BigDecimal` |
| `status` | `CampsiteBookingStatus` |
| `cancellationReason` | `String` |
| `createdAt` | `LocalDateTime` |
| `updatedAt` | `LocalDateTime` |

##### `CancelBookingRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsitebooking/CancelBookingRequest.java`

| Field | Type |
|-------|------|
| `reason` | `String` |

---

#### Package: `dto/campsitepayment/`

##### `CampsitePaymentRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsitepayment/CampsitePaymentRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `bookingId` | `Long` | `@NotNull` |
| `amount` | `BigDecimal` | `@NotNull @DecimalMin("0.01")` |
| `method` | `CampsitePaymentMethod` | `@NotNull` |
| `transactionId` | `String` | — |
| `referenceCode` | `String` | — |

##### `CampsitePaymentResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/campsitepayment/CampsitePaymentResponse.java`

| Field | Type |
|-------|------|
| `id` | `Long` |
| `bookingId` | `Long` |
| `amount` | `BigDecimal` |
| `transactionId` | `String` |
| `referenceCode` | `String` |
| `method` | `CampsitePaymentMethod` |
| `status` | `CampsitePaymentStatus` |
| `paidAt` | `LocalDateTime` |

---

### Module 1 Backend Repositories

All files in: `back/src/main/java/tn/esprit/projetpidev/repositories/`

| Repository Interface | Extends | Notable Custom Queries |
|---------------------|---------|----------------------|
| `CampsiteRepository.java` | `JpaRepository<Campsite, Long>` | `findByOwnerIdAndStatusNot(ownerId, DELETED)`, `searchWithFilters(...)` |
| `AvailabilityRepository.java` | `JpaRepository<Availability, Long>` | `findByCampsiteId(campsiteId)` |
| `CampsiteBookingRepository.java` | `JpaRepository<CampsiteBooking, Long>` | `findByCamperId(camperId, pageable)`, `sumGuestsOverlapping(campsiteId, checkIn, checkOut, statusList)` |
| `CampsitePaymentRepository.java` | `JpaRepository<CampsitePayment, Long>` | `findByBookingId(bookingId)` |

---

### Module 1 Backend Services

#### `ICampsiteService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/ICampsiteService.java`

```java
CampsiteResponse create(CampsiteRequest request, Long ownerId);
CampsiteResponse update(Long id, CampsiteRequest request, Long requesterId);
void delete(Long id, Long requesterId);
CampsiteResponse getById(Long id);
Page<CampsiteResponse> search(String country, String city, CampsiteType type,
                               BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
Page<CampsiteResponse> getByOwner(Long ownerId, Pageable pageable);
Page<CampsiteResponse> getAll(Pageable pageable);
CampsiteResponse suspend(Long id);
CampsiteResponse activate(Long id);
```

#### `ICampsiteServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/ICampsiteServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| Soft delete | Sets `status = DELETED`, never removes the row |
| Owner authorization | `create` enforces `COMPSITEOWNERS` or `ADMIN` role; `update`/`delete` check ownership or ADMIN |
| CSV split | `pictures` and `amenities` split on `","` in `mapToResponse()` |
| Logging | Logs all create / update / suspend / activate operations |

---

#### `IAvailabilityService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/IAvailabilityService.java`

```java
AvailabilityResponse create(AvailabilityRequest request);
AvailabilityResponse update(Long id, AvailabilityRequest request);
void delete(Long id);
List<AvailabilityResponse> getByCampsite(Long campsiteId);
```

#### `IAvailabilityServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/IAvailabilityServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

---

#### `ICampsiteBookingService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/ICampsiteBookingService.java`

```java
CampsiteBookingResponse create(CampsiteBookingRequest request, Long camperId);
CampsiteBookingResponse getById(Long id);
Page<CampsiteBookingResponse> getMyCamperBookings(Long camperId, Pageable pageable);
Page<CampsiteBookingResponse> getByCampsite(Long campsiteId, Pageable pageable);
Page<CampsiteBookingResponse> getAll(Pageable pageable);
CampsiteBookingResponse cancel(Long id, Long requesterId, String reason);
CampsiteBookingResponse confirm(Long id);
CampsiteBookingResponse complete(Long id);
```

#### `ICampsiteBookingServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/ICampsiteBookingServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| Date validation | `checkInDate` must be before `checkOutDate` and not in the past |
| Capacity check | Uses `sumGuestsOverlapping()` query — `numberOfGuests + existing <= campsite.capacity` |
| Price calculation | `pricePerNight × ChronoUnit.DAYS.between(checkIn, checkOut)` |
| Cancellation rule | `checkInDate` must be at least 2 days from today |
| Email on confirm | Calls `emailService.sendBookingConfirmationEmail(...)` |
| Auto-confirm via payment | `ICampsitePaymentServiceImpl.pay()` calls `confirm(bookingId)` after recording payment |

---

#### `ICampsitePaymentService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/ICampsitePaymentService.java`

```java
CampsitePaymentResponse pay(CampsitePaymentRequest request);
CampsitePaymentResponse getByBooking(Long bookingId);
CampsitePaymentResponse getById(Long id);
CampsitePaymentResponse refund(Long id);
```

#### `ICampsitePaymentServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/ICampsitePaymentServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| Duplicate guard | Throws `ResponseStatusException(CONFLICT)` if payment already exists for booking |
| Auto-confirm | Calls `bookingService.confirm(bookingId)` after recording `PAID` status |
| Timestamp | Sets `paidAt = LocalDateTime.now()` |

---

### Module 1 Backend Controllers

#### `CampsiteController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/CampsiteController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/campsites") @RequiredArgsConstructor @Slf4j`
**Tag (Swagger):** `Campsites`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/campsites` | `COMPSITEOWNERS` or `ADMIN` | `@Valid @RequestBody CampsiteRequest` + `@AuthenticationPrincipal User` | `CampsiteResponse` | 201 |
| `PUT` | `/api/v1/campsites/{id}` | `COMPSITEOWNERS` or `ADMIN` | `@Valid @RequestBody CampsiteRequest` + `@AuthenticationPrincipal User` | `CampsiteResponse` | 200 |
| `DELETE` | `/api/v1/campsites/{id}` | `COMPSITEOWNERS` or `ADMIN` | `@AuthenticationPrincipal User` | `void` | 204 |
| `GET` | `/api/v1/campsites/{id}` | Public | `@PathVariable Long id` | `CampsiteResponse` | 200 |
| `GET` | `/api/v1/campsites` | Public | `?country &city &type &minPrice &maxPrice` + `Pageable` | `Page<CampsiteResponse>` | 200 |
| `GET` | `/api/v1/campsites/my` | `COMPSITEOWNERS` or `ADMIN` | `@AuthenticationPrincipal User` + `Pageable` | `Page<CampsiteResponse>` | 200 |
| `GET` | `/api/v1/campsites/all` | `ADMIN` | `Pageable` | `Page<CampsiteResponse>` | 200 |
| `PATCH` | `/api/v1/campsites/{id}/suspend` | `ADMIN` | `@PathVariable Long id` | `CampsiteResponse` | 200 |
| `PATCH` | `/api/v1/campsites/{id}/activate` | `ADMIN` | `@PathVariable Long id` | `CampsiteResponse` | 200 |

---

#### `AvailabilityController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/AvailabilityController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/availabilities") @RequiredArgsConstructor`
**Tag (Swagger):** `Availabilities`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/availabilities` | `COMPSITEOWNERS` or `ADMIN` | `@Valid @RequestBody AvailabilityRequest` | `AvailabilityResponse` | 201 |
| `PUT` | `/api/v1/availabilities/{id}` | `COMPSITEOWNERS` or `ADMIN` | `@Valid @RequestBody AvailabilityRequest` | `AvailabilityResponse` | 200 |
| `DELETE` | `/api/v1/availabilities/{id}` | `COMPSITEOWNERS` or `ADMIN` | `@PathVariable Long id` | `void` | 204 |
| `GET` | `/api/v1/availabilities/campsite/{campsiteId}` | Public | `@PathVariable Long campsiteId` | `List<AvailabilityResponse>` | 200 |

---

#### `CampsiteBookingController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/CampsiteBookingController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/campsite-bookings") @RequiredArgsConstructor`
**Tag (Swagger):** `Campsite Bookings`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/campsite-bookings` | `isAuthenticated()` | `@Valid @RequestBody CampsiteBookingRequest` + principal | `CampsiteBookingResponse` | 201 |
| `GET` | `/api/v1/campsite-bookings/{id}` | `isAuthenticated()` | `@PathVariable Long id` | `CampsiteBookingResponse` | 200 |
| `GET` | `/api/v1/campsite-bookings/my` | `isAuthenticated()` | principal + `Pageable` | `Page<CampsiteBookingResponse>` | 200 |
| `GET` | `/api/v1/campsite-bookings/campsite/{campsiteId}` | `COMPSITEOWNERS` or `ADMIN` | `@PathVariable` + `Pageable` | `Page<CampsiteBookingResponse>` | 200 |
| `GET` | `/api/v1/campsite-bookings` | `ADMIN` | `Pageable` | `Page<CampsiteBookingResponse>` | 200 |
| `PATCH` | `/api/v1/campsite-bookings/{id}/cancel` | `isAuthenticated()` | `@RequestBody(required=false) CancelBookingRequest` + principal | `CampsiteBookingResponse` | 200 |
| `PATCH` | `/api/v1/campsite-bookings/{id}/confirm` | `COMPSITEOWNERS` or `ADMIN` | `@PathVariable Long id` | `CampsiteBookingResponse` | 200 |
| `PATCH` | `/api/v1/campsite-bookings/{id}/complete` | `COMPSITEOWNERS` or `ADMIN` | `@PathVariable Long id` | `CampsiteBookingResponse` | 200 |

---

#### `CampsitePaymentController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/CampsitePaymentController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/campsite-payments") @RequiredArgsConstructor`
**Tag (Swagger):** `Campsite Payments`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/campsite-payments` | `isAuthenticated()` | `@Valid @RequestBody CampsitePaymentRequest` | `CampsitePaymentResponse` | 201 |
| `GET` | `/api/v1/campsite-payments/{id}` | `isAuthenticated()` | `@PathVariable Long id` | `CampsitePaymentResponse` | 200 |
| `GET` | `/api/v1/campsite-payments/booking/{bookingId}` | `isAuthenticated()` | `@PathVariable Long bookingId` | `CampsitePaymentResponse` | 200 |
| `PATCH` | `/api/v1/campsite-payments/{id}/refund` | `ADMIN` | `@PathVariable Long id` | `CampsitePaymentResponse` | 200 |

---

## Module 2 — Outdoor Campsite & Booking

### Module 2 Backend Entities

---

#### `OutdoorCampsite`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/OutdoorCampsite.java`
**Table:** `outdoor_campsites`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `name` | `String` | `@NotBlank` | Required |
| `description` | `String` | — | Optional |
| `country` | `String` | `@NotBlank` | Required |
| `city` | `String` | `@NotBlank` | Required |
| `latitude` | `Double` | — | Optional GPS |
| `longitude` | `Double` | — | Optional GPS |
| `pictures` | `String` | `@Column(columnDefinition="TEXT")` | CSV string |
| `naturalFeatures` | `String` | `@Column(columnDefinition="TEXT")` | CSV string |
| `accessDifficulty` | `AccessDifficulty` | `@Enumerated(STRING)` | EASY/MODERATE/HARD |
| `proposedBy` | `User` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `users` |
| `status` | `OutdoorCampsiteStatus` | `@Enumerated(STRING)` | Default `PENDING` |
| `adminNote` | `String` | — | Optional feedback |
| `approvedBy` | `User` | `@ManyToOne(fetch=LAZY)` | Null until moderated |
| `approvedAt` | `LocalDateTime` | — | Set on approval |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | Auto-set |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | Auto-set |

> **Note:** `pictures` and `naturalFeatures` stored as CSV, split into `List<String>` in the response DTO.

---

#### `OutdoorAvailability`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/OutdoorAvailability.java`
**Table:** `outdoor_availabilities`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `outdoorCampsite` | `OutdoorCampsite` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `outdoor_campsites` |
| `startDate` | `LocalDate` | `@NotNull` | Window start |
| `endDate` | `LocalDate` | `@NotNull` | Window end |
| `isAvailable` | `boolean` | — | Default `true` |
| `note` | `String` | — | Optional note |

---

#### `OutdoorBooking`
**File:** `back/src/main/java/tn/esprit/projetpidev/domain/OutdoorBooking.java`
**Table:** `outdoor_bookings`
**Annotations:** `@Entity @Table @Data @NoArgsConstructor @AllArgsConstructor @Builder`

| Field | Type | Column / Constraint | Notes |
|-------|------|---------------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | Primary key |
| `outdoorCampsite` | `OutdoorCampsite` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `outdoor_campsites` |
| `camper` | `User` | `@ManyToOne(fetch=LAZY) @NotNull` | FK to `users` |
| `checkInDate` | `LocalDate` | `@NotNull` | Arrival date |
| `checkOutDate` | `LocalDate` | `@NotNull` | Departure date |
| `numberOfGuests` | `Integer` | `@Min(1)` | Guest count |
| `status` | `OutdoorBookingStatus` | `@Enumerated(STRING)` | Default `PENDING` → auto-set `CONFIRMED` |
| `createdAt` | `LocalDateTime` | `@CreationTimestamp` | Auto-set |
| `updatedAt` | `LocalDateTime` | `@UpdateTimestamp` | Auto-set |

> **Note:** Outdoor bookings are **free** — no payment entity. Status is set to `CONFIRMED` immediately on creation.

---

### Module 2 Backend Enums

All files in: `back/src/main/java/tn/esprit/projetpidev/domain/enums/`

| File | Values |
|------|--------|
| `OutdoorCampsiteStatus.java` | `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` |
| `OutdoorBookingStatus.java` | `PENDING`, `CONFIRMED`, `CANCELLED` |
| `AccessDifficulty.java` | `EASY`, `MODERATE`, `HARD` |

---

### Module 2 Backend DTOs

#### Package: `dto/outdoorcampsite/`

##### `OutdoorCampsiteRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdoorcampsite/OutdoorCampsiteRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `name` | `String` | `@NotBlank` |
| `description` | `String` | — |
| `country` | `String` | `@NotBlank` |
| `city` | `String` | `@NotBlank` |
| `latitude` | `Double` | — |
| `longitude` | `Double` | — |
| `pictures` | `String` | — CSV string |
| `naturalFeatures` | `String` | — CSV string |
| `accessDifficulty` | `AccessDifficulty` | — |

##### `OutdoorCampsiteResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdoorcampsite/OutdoorCampsiteResponse.java`

| Field | Type |
|-------|------|
| `id` | `Long` |
| `name` | `String` |
| `description` | `String` |
| `country` | `String` |
| `city` | `String` |
| `latitude` | `Double` |
| `longitude` | `Double` |
| `pictures` | `List<String>` |
| `naturalFeatures` | `List<String>` |
| `accessDifficulty` | `AccessDifficulty` |
| `proposedById` | `Long` |
| `proposedByName` | `String` |
| `status` | `OutdoorCampsiteStatus` |
| `adminNote` | `String` |
| `approvedById` | `Long` |
| `approvedByName` | `String` |
| `approvedAt` | `LocalDateTime` |
| `createdAt` | `LocalDateTime` |
| `updatedAt` | `LocalDateTime` |

##### `ModerationRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdoorcampsite/ModerationRequest.java`

| Field | Type | Values |
|-------|------|--------|
| `action` | `String` | `"APPROVE"` / `"REJECT"` / `"SUSPEND"` |
| `adminNote` | `String` | Optional |

---

#### Package: `dto/outdooravailability/`

##### `OutdoorAvailabilityRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdooravailability/OutdoorAvailabilityRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `outdoorCampsiteId` | `Long` | `@NotNull` |
| `startDate` | `LocalDate` | `@NotNull` |
| `endDate` | `LocalDate` | `@NotNull` |
| `isAvailable` | `boolean` | Default `true` |
| `note` | `String` | — |

##### `OutdoorAvailabilityResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdooravailability/OutdoorAvailabilityResponse.java`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | — |
| `outdoorCampsiteId` | `Long` | — |
| `outdoorCampsiteName` | `String` | — |
| `startDate` | `LocalDate` | — |
| `endDate` | `LocalDate` | — |
| `isAvailable` | `boolean` | — |
| `isFullyBooked` | `boolean` | Computed: overlapping CONFIRMED bookings exist |
| `note` | `String` | — |

---

#### Package: `dto/outdoorbooking/`

##### `OutdoorBookingRequest.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdoorbooking/OutdoorBookingRequest.java`

| Field | Type | Validation |
|-------|------|-----------|
| `outdoorCampsiteId` | `Long` | `@NotNull` |
| `checkInDate` | `LocalDate` | `@NotNull` |
| `checkOutDate` | `LocalDate` | `@NotNull` |
| `numberOfGuests` | `Integer` | `@Min(1)` |

##### `OutdoorBookingResponse.java`
**File:** `back/src/main/java/tn/esprit/projetpidev/dto/outdoorbooking/OutdoorBookingResponse.java`

| Field | Type |
|-------|------|
| `id` | `Long` |
| `outdoorCampsiteId` | `Long` |
| `outdoorCampsiteName` | `String` |
| `outdoorCampsiteCountry` | `String` |
| `outdoorCampsiteCity` | `String` |
| `camperId` | `Long` |
| `camperFullName` | `String` |
| `checkInDate` | `LocalDate` |
| `checkOutDate` | `LocalDate` |
| `numberOfGuests` | `Integer` |
| `status` | `OutdoorBookingStatus` |
| `createdAt` | `LocalDateTime` |
| `updatedAt` | `LocalDateTime` |

---

### Module 2 Backend Repositories

All files in: `back/src/main/java/tn/esprit/projetpidev/repositories/`

| Repository Interface | Extends | Notable Custom Queries |
|---------------------|---------|----------------------|
| `OutdoorCampsiteRepository.java` | `JpaRepository<OutdoorCampsite, Long>` | `findByStatus(APPROVED, pageable)`, `findByStatus(PENDING, pageable)`, `findByProposedById(userId, pageable)` |
| `OutdoorAvailabilityRepository.java` | `JpaRepository<OutdoorAvailability, Long>` | `findByOutdoorCampsiteId(siteId)` |
| `OutdoorBookingRepository.java` | `JpaRepository<OutdoorBooking, Long>` | `findByCamperId(camperId, pageable)`, `findByOutdoorCampsiteId(siteId, pageable)`, overlap check query |

---

### Module 2 Backend Services

#### `IOutdoorCampsiteService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/IOutdoorCampsiteService.java`

```java
OutdoorCampsiteResponse propose(OutdoorCampsiteRequest request, Long proposerId);
OutdoorCampsiteResponse update(Long id, OutdoorCampsiteRequest request, Long requesterId);
OutdoorCampsiteResponse moderate(Long id, ModerationRequest request, Long adminId);
OutdoorCampsiteResponse getById(Long id);
Page<OutdoorCampsiteResponse> getApproved(Pageable pageable);
Page<OutdoorCampsiteResponse> getPending(Pageable pageable);
Page<OutdoorCampsiteResponse> getMyProposals(Long userId, Pageable pageable);
```

#### `IOutdoorCampsiteServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/IOutdoorCampsiteServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| Default status | `propose()` sets `status = PENDING` |
| Edit restriction | `update()` only allowed when `status == PENDING` and requester is the proposer |
| Moderation: APPROVE | Sets `status = APPROVED`, `approvedBy`, `approvedAt = now()`, sends approval email |
| Moderation: REJECT | Sets `status = REJECTED`, stores `adminNote`, sends rejection email |
| Moderation: SUSPEND | Sets `status = SUSPENDED` |
| CSV split | `pictures` and `naturalFeatures` split on `","` in `mapToResponse()` |

---

#### `IOutdoorAvailabilityService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/IOutdoorAvailabilityService.java`

```java
OutdoorAvailabilityResponse create(OutdoorAvailabilityRequest request);
OutdoorAvailabilityResponse update(Long id, OutdoorAvailabilityRequest request);
void delete(Long id);
List<OutdoorAvailabilityResponse> getBySite(Long outdoorCampsiteId);
```

#### `IOutdoorAvailabilityServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/IOutdoorAvailabilityServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| `isFullyBooked` flag | Computed during mapping: checks if any `CONFIRMED` booking overlaps the window |

---

#### `IOutdoorBookingService` interface
**File:** `back/src/main/java/tn/esprit/projetpidev/services/IOutdoorBookingService.java`

```java
OutdoorBookingResponse create(OutdoorBookingRequest request, Long camperId);
OutdoorBookingResponse getById(Long id);
Page<OutdoorBookingResponse> getMyBookings(Long camperId, Pageable pageable);
Page<OutdoorBookingResponse> getBySite(Long siteId, Pageable pageable);
OutdoorBookingResponse cancel(Long id, Long requesterId);
```

#### `IOutdoorBookingServiceImpl` implementation
**File:** `back/src/main/java/tn/esprit/projetpidev/services/impl/IOutdoorBookingServiceImpl.java`
**Annotations:** `@Service @Transactional @RequiredArgsConstructor @Slf4j`

| Behaviour | Detail |
|-----------|--------|
| APPROVED-only | `create()` throws `BAD_REQUEST` if `outdoorCampsite.status != APPROVED` |
| Date validation | `checkInDate` must be before `checkOutDate` |
| Overlap prevention | Checks existing `CONFIRMED` bookings for date overlap |
| Auto-confirm | Sets `status = CONFIRMED` immediately (free, no payment) |
| Cancellation | Requester must be the camper or an ADMIN |

---

### Module 2 Backend Controllers

#### `OutdoorCampsiteController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/OutdoorCampsiteController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/outdoor-campsites") @RequiredArgsConstructor`
**Tag (Swagger):** `Outdoor Campsites`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/outdoor-campsites` | `isAuthenticated()` | `@Valid @RequestBody OutdoorCampsiteRequest` + principal | `OutdoorCampsiteResponse` | 201 |
| `PUT` | `/api/v1/outdoor-campsites/{id}` | `isAuthenticated()` | `@Valid @RequestBody OutdoorCampsiteRequest` + principal | `OutdoorCampsiteResponse` | 200 |
| `PATCH` | `/api/v1/outdoor-campsites/{id}/moderate` | `ADMIN` | `@RequestBody ModerationRequest` + principal | `OutdoorCampsiteResponse` | 200 |
| `GET` | `/api/v1/outdoor-campsites/{id}` | Public | `@PathVariable Long id` | `OutdoorCampsiteResponse` | 200 |
| `GET` | `/api/v1/outdoor-campsites` | Public | `Pageable` | `Page<OutdoorCampsiteResponse>` | 200 |
| `GET` | `/api/v1/outdoor-campsites/pending` | `ADMIN` | `Pageable` | `Page<OutdoorCampsiteResponse>` | 200 |
| `GET` | `/api/v1/outdoor-campsites/my` | `isAuthenticated()` | principal + `Pageable` | `Page<OutdoorCampsiteResponse>` | 200 |

---

#### `OutdoorAvailabilityController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/OutdoorAvailabilityController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/outdoor-availabilities") @RequiredArgsConstructor`
**Tag (Swagger):** `Outdoor Availabilities`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/outdoor-availabilities` | `ADMIN` | `@Valid @RequestBody OutdoorAvailabilityRequest` | `OutdoorAvailabilityResponse` | 201 |
| `PUT` | `/api/v1/outdoor-availabilities/{id}` | `ADMIN` | `@Valid @RequestBody OutdoorAvailabilityRequest` | `OutdoorAvailabilityResponse` | 200 |
| `DELETE` | `/api/v1/outdoor-availabilities/{id}` | `ADMIN` | `@PathVariable Long id` | `void` | 204 |
| `GET` | `/api/v1/outdoor-availabilities/site/{siteId}` | Public | `@PathVariable Long siteId` | `List<OutdoorAvailabilityResponse>` | 200 |

---

#### `OutdoorBookingController`
**File:** `back/src/main/java/tn/esprit/projetpidev/controllers/OutdoorBookingController.java`
**Annotations:** `@RestController @RequestMapping("/api/v1/outdoor-bookings") @RequiredArgsConstructor`
**Tag (Swagger):** `Outdoor Bookings`

| HTTP | Path | `@PreAuthorize` | Request | Response | Status |
|------|------|----------------|---------|----------|--------|
| `POST` | `/api/v1/outdoor-bookings` | `isAuthenticated()` | `@Valid @RequestBody OutdoorBookingRequest` + principal | `OutdoorBookingResponse` | 201 |
| `GET` | `/api/v1/outdoor-bookings/{id}` | `isAuthenticated()` | `@PathVariable Long id` | `OutdoorBookingResponse` | 200 |
| `GET` | `/api/v1/outdoor-bookings/my` | `isAuthenticated()` | principal + `Pageable` | `Page<OutdoorBookingResponse>` | 200 |
| `GET` | `/api/v1/outdoor-bookings/site/{siteId}` | `ADMIN` | `@PathVariable` + `Pageable` | `Page<OutdoorBookingResponse>` | 200 |
| `PATCH` | `/api/v1/outdoor-bookings/{id}/cancel` | `isAuthenticated()` | `@PathVariable Long id` + principal | `OutdoorBookingResponse` | 200 |

---

## Security Configuration

**File:** `back/src/main/java/tn/esprit/projetpidev/jwt/SecurityConfig.java`

The rules below were **inserted** into the existing `securityFilterChain` before `anyRequest().authenticated()`. The full filter chain also has JWT validation via `JwtAuthFilter`.

```java
// ── Campsites (Module 1) ──────────────────────────────────
.requestMatchers(HttpMethod.GET,    "/api/v1/campsites", "/api/v1/campsites/**").permitAll()
.requestMatchers(HttpMethod.POST,   "/api/v1/campsites").hasAnyRole("COMPSITEOWNERS", "ADMIN")
.requestMatchers(HttpMethod.PUT,    "/api/v1/campsites/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/v1/campsites/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
.requestMatchers(HttpMethod.PATCH,  "/api/v1/campsites/**").hasRole("ADMIN")

// ── Availabilities ────────────────────────────────────────
.requestMatchers(HttpMethod.GET,    "/api/v1/availabilities/**").permitAll()
.requestMatchers(HttpMethod.POST,   "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
.requestMatchers(HttpMethod.PUT,    "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")

// ── Campsite Bookings ─────────────────────────────────────
.requestMatchers("/api/v1/campsite-bookings/**").authenticated()

// ── Campsite Payments ─────────────────────────────────────
.requestMatchers("/api/v1/campsite-payments/**").authenticated()

// ── Outdoor Campsites (Module 2) ──────────────────────────
.requestMatchers(HttpMethod.GET,    "/api/v1/outdoor-campsites", "/api/v1/outdoor-campsites/**").permitAll()
.requestMatchers(HttpMethod.POST,   "/api/v1/outdoor-campsites").authenticated()
.requestMatchers(HttpMethod.PUT,    "/api/v1/outdoor-campsites/**").authenticated()
.requestMatchers(HttpMethod.PATCH,  "/api/v1/outdoor-campsites/*/moderate").hasRole("ADMIN")

// ── Outdoor Availabilities ────────────────────────────────
.requestMatchers(HttpMethod.GET,    "/api/v1/outdoor-availabilities/**").permitAll()
.requestMatchers(           "/api/v1/outdoor-availabilities/**").hasRole("ADMIN")

// ── Outdoor Bookings ──────────────────────────────────────
.requestMatchers("/api/v1/outdoor-bookings/**").authenticated()
```

---

## Email Notifications

**File:** `back/src/main/java/tn/esprit/projetpidev/services/EmailService.java`
**New methods added** (existing class was extended, not replaced):

### Module 1 — Booking Confirmation

```java
public void sendBookingConfirmationEmail(
    String toEmail,
    String camperName,
    String campsiteName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    BigDecimal totalPrice
)
```

**Trigger:** Called by `ICampsiteBookingServiceImpl.confirm()`
**Content:** HTML email showing campsite name, dates, and total price

---

### Module 2 — Proposal Moderation

```java
public void sendOutdoorCampsiteApprovalEmail(
    String toEmail,
    String proposerName,
    String campsiteName,
    boolean approved,
    String adminNote
)
```

**Trigger:** Called by `IOutdoorCampsiteServiceImpl.moderate()` for APPROVE and REJECT actions
**Content:** HTML email stating approved or rejected, including any admin note

---

## Backend Tests

### Controller Tests

Both tests use `@WebMvcTest` with `@Import({SecurityConfig.class, PasswordConfig.class})` to load the real security configuration, plus `@MockBean` for `JwtService` and `CustomUserDetailsService`, plus `excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class` to avoid bean conflicts.

#### `CampsiteControllerTest`
**File:** `back/src/test/java/tn/esprit/projetpidev/controllers/CampsiteControllerTest.java`
**Annotations:**
```java
@WebMvcTest(
    value = CampsiteController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, PasswordConfig.class})
```

| Test Method | What it verifies |
|-------------|-----------------|
| `getById_publicEndpoint_returns200` | GET `/api/v1/campsites/1` — no auth needed → 200 |
| `search_publicEndpoint_returns200` | GET `/api/v1/campsites` — no auth needed → 200 |
| `create_ownerRole_returns201` | POST with `COMPSITEOWNERS` User → 201 |
| `create_camperRole_returns403` | POST with `COMPERS` User → 403 |
| `suspend_adminRole_returns200` | PATCH `/suspend` with `ADMIN` User → 200 |
| `create_unauthenticated_returns403` | POST with no auth → 403 |

**Key:** Authenticated tests use `SecurityMockMvcRequestPostProcessors.user(User.builder()...build())` with a real `tn.esprit.projetpidev.domain.User` object (not `@WithMockUser`) to satisfy `@AuthenticationPrincipal User`.

---

#### `CampsiteBookingControllerTest`
**File:** `back/src/test/java/tn/esprit/projetpidev/controllers/CampsiteBookingControllerTest.java`
**Annotations:**
```java
@WebMvcTest(
    value = CampsiteBookingController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, PasswordConfig.class})
```

| Test Method | What it verifies |
|-------------|-----------------|
| `create_authenticated_returns201` | POST with authenticated User → 201 |
| `create_unauthenticated_returns401or403` | POST without auth → 401 or 403 |
| `getMyBookings_authenticated_returns200` | GET `/my` with authenticated User → 200 |
| `cancel_authenticated_returns200` | PATCH `/cancel` with authenticated User → 200 |

---

### Service Tests

All service test files in: `back/src/test/java/tn/esprit/projetpidev/services/`

| Test File | Tests |
|-----------|-------|
| `ICampsiteServiceImplTest.java` | 11 tests — CRUD, soft-delete, owner check |
| `ICampsiteBookingServiceImplTest.java` | 9 tests — create, cancel, confirm, date/capacity validation |
| `IOutdoorCampsiteServiceImplTest.java` | 8 tests — propose, moderate (approve/reject/suspend) |

---

## Frontend — Services

### `CampsiteService`
**File:** `front/src/app/services/campsite.service.ts`
**Injectable:** `{ providedIn: 'root' }`
**Base URL:** `http://localhost:9099/api/v1/campsites`

| Method | HTTP | Endpoint | Returns |
|--------|------|----------|---------|
| `search(filters)` | GET | `/api/v1/campsites?country=&city=&type=&minPrice=&maxPrice=&page=&size=` | `Observable<CampsitePage>` |
| `getCampsiteById(id)` | GET | `/api/v1/campsites/{id}` | `Observable<CampsiteApiResponse>` |
| `getMyCampsites(page, size)` | GET | `/api/v1/campsites/my` | `Observable<CampsitePage>` |
| `getAllAdmin(page, size)` | GET | `/api/v1/campsites/all` | `Observable<CampsitePage>` |
| `create(request)` | POST | `/api/v1/campsites` | `Observable<CampsiteApiResponse>` |
| `update(id, request)` | PUT | `/api/v1/campsites/{id}` | `Observable<CampsiteApiResponse>` |
| `delete(id)` | DELETE | `/api/v1/campsites/{id}` | `Observable<void>` |
| `suspend(id)` | PATCH | `/api/v1/campsites/{id}/suspend` | `Observable<CampsiteApiResponse>` |
| `activate(id)` | PATCH | `/api/v1/campsites/{id}/activate` | `Observable<CampsiteApiResponse>` |
| `getAvailabilities(campsiteId)` | GET | `/api/v1/availabilities/campsite/{id}` | `Observable<AvailabilityResponse[]>` |
| `createAvailability(req)` | POST | `/api/v1/availabilities` | `Observable<AvailabilityResponse>` |
| `deleteAvailability(id)` | DELETE | `/api/v1/availabilities/{id}` | `Observable<void>` |

---

### `CampsiteBookingService`
**File:** `front/src/app/services/campsite-booking.service.ts`
**Injectable:** `{ providedIn: 'root' }`
**Base URL:** `http://localhost:9099/api/v1/campsite-bookings`

| Method | HTTP | Endpoint | Returns |
|--------|------|----------|---------|
| `create(request)` | POST | `/api/v1/campsite-bookings` | `Observable<CampsiteBookingResponse>` |
| `getById(id)` | GET | `/api/v1/campsite-bookings/{id}` | `Observable<CampsiteBookingResponse>` |
| `getMyBookings(page, size)` | GET | `/api/v1/campsite-bookings/my` | `Observable<BookingPage>` |
| `getByCampsite(campsiteId, page, size)` | GET | `/api/v1/campsite-bookings/campsite/{id}` | `Observable<BookingPage>` |
| `getAll(page, size)` | GET | `/api/v1/campsite-bookings` | `Observable<BookingPage>` |
| `cancel(id, reason?)` | PATCH | `/api/v1/campsite-bookings/{id}/cancel` | `Observable<CampsiteBookingResponse>` |
| `confirm(id)` | PATCH | `/api/v1/campsite-bookings/{id}/confirm` | `Observable<CampsiteBookingResponse>` |
| `pay(request)` | POST | `/api/v1/campsite-payments` | `Observable<CampsitePaymentResponse>` |
| `getPaymentByBooking(bookingId)` | GET | `/api/v1/campsite-payments/booking/{id}` | `Observable<CampsitePaymentResponse>` |

---

### `OutdoorCampsiteService`
**File:** `front/src/app/services/outdoor-campsite.service.ts`
**Injectable:** `{ providedIn: 'root' }`
**Base URLs:** `/api/v1/outdoor-campsites` and `/api/v1/outdoor-bookings`

| Method | HTTP | Endpoint | Returns |
|--------|------|----------|---------|
| `getApproved(page, size)` | GET | `/api/v1/outdoor-campsites` | `Observable<OutdoorCampsitePage>` |
| `getById(id)` | GET | `/api/v1/outdoor-campsites/{id}` | `Observable<OutdoorCampsiteResponse>` |
| `propose(request)` | POST | `/api/v1/outdoor-campsites` | `Observable<OutdoorCampsiteResponse>` |
| `update(id, request)` | PUT | `/api/v1/outdoor-campsites/{id}` | `Observable<OutdoorCampsiteResponse>` |
| `getPending(page, size)` | GET | `/api/v1/outdoor-campsites/pending` | `Observable<OutdoorCampsitePage>` |
| `moderate(id, request)` | PATCH | `/api/v1/outdoor-campsites/{id}/moderate` | `Observable<OutdoorCampsiteResponse>` |
| `getMyProposals(page, size)` | GET | `/api/v1/outdoor-campsites/my` | `Observable<OutdoorCampsitePage>` |
| `getAvailabilities(siteId)` | GET | `/api/v1/outdoor-availabilities/site/{id}` | `Observable<OutdoorAvailabilityResponse[]>` |
| `book(request)` | POST | `/api/v1/outdoor-bookings` | `Observable<OutdoorBookingResponse>` |
| `getBookingById(id)` | GET | `/api/v1/outdoor-bookings/{id}` | `Observable<OutdoorBookingResponse>` |
| `getMyOutdoorBookings(page, size)` | GET | `/api/v1/outdoor-bookings/my` | `Observable<OutdoorBookingPage>` |
| `cancelBooking(id)` | PATCH | `/api/v1/outdoor-bookings/{id}/cancel` | `Observable<OutdoorBookingResponse>` |

---

## Frontend — Components

All components are:
- Declared in `AppModule` (`front/src/app/app.module.ts`)
- Traditional `NgModule` style (not standalone)
- Routed in `AppRoutingModule` (`front/src/app/app-routing.module.ts`)

---

### Module 1 Frontend Components

---

#### `CampsitesComponent`
**Selector:** `app-campsites`
**Files:**
- `front/src/app/frontoffice/pages/campsites/campsites.component.ts`
- `front/src/app/frontoffice/pages/campsites/campsites.component.html`
- `front/src/app/frontoffice/pages/campsites/campsites.component.css`

**Purpose:** Browse and filter the official campsite catalogue with pagination.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `campsites` | `CampsiteApiResponse[]` | Current page results |
| `currentPage` | `number` | Active page index |
| `totalPages` | `number` | Total pages from API |
| `totalElements` | `number` | Total result count |
| `searchCountry` | `string` | Filter by country |
| `searchCity` | `string` | Filter by city |
| `filterType` | `string` | Filter by type (OFFICIAL/OUTDOOR) |
| `minPrice` | `number` | Min price filter |
| `maxPrice` | `number` | Max price filter |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads first page on init |
| `loadCampsites(page)` | Calls `CampsiteService.search()` with current filters |
| `applyFilters()` | Resets to page 0 and reloads |
| `goToPage(page)` | Navigates to a specific page |
| `viewDetail(id)` | Navigates to `/campsites/:id` |

---

#### `CampsiteDetailComponent`
**Selector:** `app-campsite-detail`
**Files:**
- `front/src/app/frontoffice/pages/campsite-detail/campsite-detail.component.ts`
- `front/src/app/frontoffice/pages/campsite-detail/campsite-detail.component.html`
- `front/src/app/frontoffice/pages/campsite-detail/campsite-detail.component.css`

**Purpose:** Full detail page for a single campsite — image gallery, amenities chips, availability table, and Book Now button.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `campsite` | `CampsiteApiResponse` | Loaded campsite data |
| `availabilities` | `AvailabilityResponse[]` | Availability windows from API |
| `activeImageIndex` | `number` | Current gallery image |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Reads `:id` from route, loads campsite and availabilities |
| `loadAvailability(campsiteId)` | Fetches availability windows |
| `setActiveImage(i)` | Switches gallery image |
| `bookNow()` | Navigates to `/campsites/:id/book` (guard redirects to login if not authenticated) |

---

#### `CampsiteBookingComponent`
**Selector:** `app-campsite-booking`
**Files:**
- `front/src/app/frontoffice/pages/campsite-booking/campsite-booking.component.ts`
- `front/src/app/frontoffice/pages/campsite-booking/campsite-booking.component.html`
- `front/src/app/frontoffice/pages/campsite-booking/campsite-booking.component.css`

**Purpose:** Booking form — date pickers, guest count, live price calculation, and form submission.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `bookingForm` | `FormGroup` | `checkInDate`, `checkOutDate`, `numberOfGuests` |
| `campsite` | `CampsiteApiResponse` | Loaded campsite |
| `totalPrice` | `number` | Computed: nights × pricePerNight |
| `nights` | `number` | Computed from dates |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads campsite from route param; subscribes to form changes to update price |
| `updatePrice()` | Recomputes `nights` and `totalPrice` from form values |
| `submit()` | Calls `CampsiteBookingService.create()`, on success navigates to `/campsite-payment?bookingId=&amount=` |

---

#### `CampsitePaymentComponent`
**Selector:** `app-campsite-payment`
**Files:**
- `front/src/app/frontoffice/pages/campsite-payment/campsite-payment.component.ts`
- `front/src/app/frontoffice/pages/campsite-payment/campsite-payment.component.html`
- `front/src/app/frontoffice/pages/campsite-payment/campsite-payment.component.css`

**Purpose:** Payment form with three methods — card (with live formatting), PayPal (redirect mock), bank transfer (reference display).

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `bookingId` | `number` | From query param |
| `amount` | `number` | From query param |
| `selectedMethod` | `'CARD' \| 'PAYPAL' \| 'BANK_TRANSFER'` | Active payment tab |
| `cardNumber` | `string` | Formatted "XXXX XXXX XXXX XXXX" |
| `cardName` | `string` | Cardholder name |
| `cardExpiry` | `string` | Formatted "MM/YY" |
| `cardCVV` | `string` | Digits only |
| `expiryExpired` | `boolean` | Validation flag |
| `paid` | `boolean` | Shows success screen |
| `loading` | `boolean` | Submit spinner |

**Key methods:**
| Method | Description |
|--------|-------------|
| `selectMethod(method)` | Switches payment method tab |
| `formatCardNumber(event)` | Inserts spaces every 4 digits |
| `formatExpiry(event)` | Formats as MM/YY, validates expiry not past |
| `sanitizeCVV(event)` | Strips non-digits |
| `pay()` | Calls `CampsiteBookingService.pay()`, on success sets `paid = true`, launches confetti |
| `launchConfetti()` | Fires canvas-confetti celebration animation |

---

#### `MyBookingsComponent`
**Selector:** `app-my-bookings`
**Files:**
- `front/src/app/frontoffice/pages/my-bookings/my-bookings.component.ts`
- `front/src/app/frontoffice/pages/my-bookings/my-bookings.component.html`
- `front/src/app/frontoffice/pages/my-bookings/my-bookings.component.css`

**Purpose:** Tabbed page showing the user's official campsite bookings and outdoor bookings.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `officialBookings` | `CampsiteBookingResponse[]` | Official bookings list |
| `outdoorBookings` | `OutdoorBookingResponse[]` | Outdoor bookings list |
| `activeTab` | `'official' \| 'outdoor'` | Active tab |
| `showCancelModal` | `boolean` | Cancel confirmation modal visibility |
| `cancelTargetId` | `number` | Booking ID pending cancellation |
| `cancelReason` | `string` | Optional reason input |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads both booking types |
| `loadBookings()` | Calls both services |
| `openCancelModal(id)` | Shows confirmation modal, stores `cancelTargetId` |
| `confirmCancel()` | Calls appropriate cancel service method; reloads list |
| `canCancel(checkInDate)` | Returns true if checkIn > today + 2 days (official only) |
| `badgeClass(status)` | Returns Bootstrap badge CSS class based on status |

---

### Module 2 Frontend Components

---

#### `OutdoorTripsComponent`
**Selector:** `app-outdoor-trips`
**Files:**
- `front/src/app/frontoffice/pages/outdoor-trips/outdoor-trips.component.ts`
- `front/src/app/frontoffice/pages/outdoor-trips/outdoor-trips.component.html`
- `front/src/app/frontoffice/pages/outdoor-trips/outdoor-trips.component.css`

**Purpose:** Paginated grid of approved outdoor campsites with FREE badge and difficulty badges.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `sites` | `OutdoorCampsiteResponse[]` | Current page |
| `currentPage` | `number` | Active page index |
| `totalPages` | `number` | Total pages from API |
| `totalElements` | `number` | Total count |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads page 0 |
| `loadSites(page)` | Calls `OutdoorCampsiteService.getApproved()` |
| `goToPage(page)` | Navigates to a page |
| `viewDetail(id)` | Navigates to `/outdoor-campsites/:id` |
| `proposeNew()` | Navigates to `/propose-outdoor` |

---

#### `OutdoorCampsiteDetailComponent`
**Selector:** `app-outdoor-campsite-detail`
**Files:**
- `front/src/app/frontoffice/pages/outdoor-campsite-detail/outdoor-campsite-detail.component.ts`
- `front/src/app/frontoffice/pages/outdoor-campsite-detail/outdoor-campsite-detail.component.html`
- `front/src/app/frontoffice/pages/outdoor-campsite-detail/outdoor-campsite-detail.component.css`

**Purpose:** Detail page for an outdoor campsite — image gallery, natural features, difficulty badge, availability windows (only open windows: `isAvailable && !isFullyBooked`), FREE badge, and Book Now.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `site` | `OutdoorCampsiteResponse` | Loaded outdoor campsite |
| `availabilities` | `OutdoorAvailabilityResponse[]` | Open availability windows |
| `activeImageIndex` | `number` | Current gallery image |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Reads `:id` from route, loads site and filters availabilities |
| `loadAvailabilities(siteId)` | Fetches all availabilities, keeps only `isAvailable && !isFullyBooked` |
| `setActiveImage(i)` | Switches gallery image |
| `bookNow()` | Navigates to `/outdoor-campsites/:id/book` |
| `difficultyBadge(diff)` | Returns CSS class: EASY=success, MODERATE=warning, HARD=danger |

---

#### `OutdoorBookingComponent`
**Selector:** `app-outdoor-booking`
**Files:**
- `front/src/app/frontoffice/pages/outdoor-booking/outdoor-booking.component.ts`
- `front/src/app/frontoffice/pages/outdoor-booking/outdoor-booking.component.html`
- `front/src/app/frontoffice/pages/outdoor-booking/outdoor-booking.component.css`

**Purpose:** Free booking form for an outdoor campsite — date pickers, guest count, nights preview.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `bookingForm` | `FormGroup` | `checkInDate`, `checkOutDate`, `numberOfGuests` |
| `site` | `OutdoorCampsiteResponse` | Loaded outdoor campsite |
| `nights` | `number` | Computed from dates |
| `loading` | `boolean` | Submit spinner |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads outdoor campsite from route param; subscribes to form changes to update nights |
| `updateNights()` | Computes nights count from date fields |
| `submit()` | Calls `OutdoorCampsiteService.book()`, on success navigates to `/my-bookings` |

---

#### `ProposeOutdoorComponent`
**Selector:** `app-propose-outdoor`
**Files:**
- `front/src/app/frontoffice/pages/propose-outdoor/propose-outdoor.component.ts`
- `front/src/app/frontoffice/pages/propose-outdoor/propose-outdoor.component.html`
- `front/src/app/frontoffice/pages/propose-outdoor/propose-outdoor.component.css`

**Purpose:** ReactiveForm to propose a new outdoor campsite (submitted in PENDING status for admin review).

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `form` | `FormGroup` | `name`, `description`, `country`, `city`, `latitude`, `longitude`, `pictures`, `naturalFeatures`, `accessDifficulty` |
| `difficulties` | `string[]` | `['EASY', 'MODERATE', 'HARD']` |
| `loading` | `boolean` | Submit spinner |
| `success` | `boolean` | Success state |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Builds `FormGroup` with validators |
| `submit()` | Calls `OutdoorCampsiteService.propose()`, on success sets `success = true`, navigates to `/my-proposals` after 2 seconds |
| `hasError(field)` | Returns true if field is invalid and touched |

---

#### `MyProposalsComponent`
**Selector:** `app-my-proposals`
**Files:**
- `front/src/app/frontoffice/pages/my-proposals/my-proposals.component.ts`
- `front/src/app/frontoffice/pages/my-proposals/my-proposals.component.html`
- `front/src/app/frontoffice/pages/my-proposals/my-proposals.component.css`

**Purpose:** Lists the authenticated user's outdoor campsite proposals with their current moderation status.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `proposals` | `OutdoorCampsiteResponse[]` | User's proposals |
| `loading` | `boolean` | Loading spinner |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads proposals via `OutdoorCampsiteService.getMyProposals()` |
| `statusBadgeClass(status)` | Returns Bootstrap badge class: PENDING=warning, APPROVED=success, REJECTED=danger, SUSPENDED=secondary |

---

### Dashboard / Admin Components

---

#### `CampsiteOwnerComponent`
**Selector:** `app-campsite-owner`
**Files:**
- `front/src/app/dashboards/campsite-owner/campsite-owner.component.ts`
- `front/src/app/dashboards/campsite-owner/campsite-owner.component.html`
- `front/src/app/dashboards/campsite-owner/campsite-owner.component.css`

**Purpose:** Full owner dashboard — campsite CRUD, availability management, booking list with confirm action.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `campsites` | `CampsiteApiResponse[]` | Owner's campsites |
| `bookings` | `CampsiteBookingResponse[]` | Bookings for selected campsite |
| `availabilities` | `AvailabilityResponse[]` | Availability windows for selected campsite |
| `selectedCampsite` | `CampsiteApiResponse \| null` | Currently focused campsite |
| `campsiteForm` | `FormGroup` | Create/Edit campsite form |
| `availForm` | `FormGroup` | Create availability form |
| `showCampsiteForm` | `boolean` | Form modal visibility |
| `isEditMode` | `boolean` | Create vs Edit mode flag |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads owner's campsites |
| `loadMyCampsites()` | Calls `CampsiteService.getMyCampsites()` |
| `selectCampsite(campsite)` | Sets `selectedCampsite`, loads bookings and availabilities for it |
| `openCreateForm()` | Resets form, sets `isEditMode = false`, shows modal |
| `openEditForm(campsite)` | Patches form with campsite data, sets `isEditMode = true` |
| `saveCampsite()` | Calls create or update based on `isEditMode` |
| `deleteCampsite(id)` | Calls `CampsiteService.delete()` with confirm dialog |
| `addAvailability()` | Calls `CampsiteService.createAvailability()` |
| `deleteAvailability(id)` | Calls `CampsiteService.deleteAvailability()` |
| `confirmBooking(id)` | Calls `CampsiteBookingService.confirm()` |

---

#### `AdminOutdoorModerationComponent`
**Selector:** `app-admin-outdoor-moderation`
**Files:**
- `front/src/app/dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component.ts`
- `front/src/app/dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component.html`
- `front/src/app/dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component.css`

**Purpose:** Admin dashboard listing PENDING outdoor campsite proposals with approve / reject actions. Shows site details inline with admin note input on reject.

**Key properties:**
| Property | Type | Description |
|----------|------|-------------|
| `pendingSites` | `OutdoorCampsiteResponse[]` | PENDING proposals list |
| `processingId` | `number \| null` | ID currently being actioned (spinner) |
| `showRejectModal` | `boolean` | Reject confirmation modal visibility |
| `selectedId` | `number \| null` | ID selected for rejection |
| `rejectNote` | `string` | Admin note for rejection email |

**Key methods:**
| Method | Description |
|--------|-------------|
| `ngOnInit()` | Loads pending proposals |
| `loadPending()` | Calls `OutdoorCampsiteService.getPending()` |
| `approve(id)` | Calls `moderate(id, { action: 'APPROVE' })`, reloads list |
| `openRejectModal(id)` | Stores `selectedId`, shows modal |
| `confirmReject()` | Calls `moderate(selectedId, { action: 'REJECT', adminNote: rejectNote })`, reloads list |

---

## Frontend — Routes & Guards

### Routes
**File:** `front/src/app/app-routing.module.ts`

All routes under the `FrontofficeLayout` (`''` parent path):

| Route path | Component | Guard | Module |
|-----------|-----------|-------|--------|
| `campsites` | `CampsitesComponent` | — | M1 |
| `campsites/:id` | `CampsiteDetailComponent` | — | M1 |
| `campsites/:id/book` | `CampsiteBookingComponent` | `requireAuthGuard` | M1 |
| `my-bookings` | `MyBookingsComponent` | `requireAuthGuard` | M1 + M2 |
| `campsite-payment` | `CampsitePaymentComponent` | `requireAuthGuard` | M1 |
| `outdoor-campsites` | `OutdoorTripsComponent` | — | M2 |
| `outdoor-campsites/:id` | `OutdoorCampsiteDetailComponent` | — | M2 |
| `outdoor-campsites/:id/book` | `OutdoorBookingComponent` | `requireAuthGuard` | M2 |
| `propose-outdoor` | `ProposeOutdoorComponent` | `requireAuthGuard` | M2 |
| `my-proposals` | `MyProposalsComponent` | `requireAuthGuard` | M2 |

Admin routes under `admin` (all protected by `adminGuard`):

| Route path | Component | Module |
|-----------|-----------|--------|
| `admin/campsites` | `CampsiteOwnerComponent` | M1 |
| `admin/outdoor-moderation` | `AdminOutdoorModerationComponent` | M2 |

---

### Guards

#### `requireAuthGuard`
**File:** `front/src/app/guards/require-auth.guard.ts`
**Type:** `CanActivateFn`
**Behaviour:** If `AuthService.isLoggedIn()` returns `false`, redirects to `/login` with `returnUrl` query param. Otherwise allows navigation.

---

## Module Comparison

| Aspect | Module 1 — Official | Module 2 — Outdoor |
|--------|--------------------|--------------------|
| **Campsite creation** | Owner creates directly (ACTIVE immediately) | Any user proposes; requires admin approval |
| **Booking cost** | Paid — `pricePerNight × nights` | Free — no payment |
| **Booking confirmation** | Owner or admin confirms | Auto-confirmed immediately on creation |
| **Cancellation rule** | Must be ≥ 2 days before check-in | Any time |
| **Availability management** | Owner manages windows | Admin manages windows |
| **Admin role** | Suspend/activate campsites, view all bookings | Approve/reject proposals, manage availability |
| **Email sent** | Booking confirmation (to camper) | Proposal approval/rejection (to proposer) |
| **Payment entity** | `CampsitePayment` | None |
| **Status lifecycle** | `ACTIVE` / `SUSPENDED` / `DELETED` | `PENDING` → `APPROVED` / `REJECTED` / `SUSPENDED` |
| **Booking statuses** | PENDING → CONFIRMED → COMPLETED / CANCELLED | CONFIRMED immediately / CANCELLED |

---

## Full File Tree

```
back/src/main/java/tn/esprit/projetpidev/
│
├── domain/
│   ├── enums/
│   │   ├── AccessDifficulty.java              (M2)
│   │   ├── CampsiteBookingStatus.java         (M1)
│   │   ├── CampsitePaymentMethod.java         (M1)
│   │   ├── CampsitePaymentStatus.java         (M1)
│   │   ├── CampsiteStatus.java                (M1)
│   │   ├── CampsiteType.java                  (M1)
│   │   ├── OutdoorBookingStatus.java          (M2)
│   │   └── OutdoorCampsiteStatus.java         (M2)
│   ├── Availability.java                      (M1)
│   ├── Campsite.java                          (M1)
│   ├── CampsiteBooking.java                   (M1)
│   ├── CampsitePayment.java                   (M1)
│   ├── OutdoorAvailability.java               (M2)
│   ├── OutdoorBooking.java                    (M2)
│   └── OutdoorCampsite.java                   (M2)
│
├── dto/
│   ├── availability/
│   │   ├── AvailabilityRequest.java           (M1)
│   │   └── AvailabilityResponse.java          (M1)
│   ├── campsite/
│   │   ├── CampsiteRequest.java               (M1)
│   │   └── CampsiteResponse.java              (M1)
│   ├── campsitebooking/
│   │   ├── CampsiteBookingRequest.java        (M1)
│   │   ├── CampsiteBookingResponse.java       (M1)
│   │   └── CancelBookingRequest.java          (M1)
│   ├── campsitepayment/
│   │   ├── CampsitePaymentRequest.java        (M1)
│   │   └── CampsitePaymentResponse.java       (M1)
│   ├── outdooravailability/
│   │   ├── OutdoorAvailabilityRequest.java    (M2)
│   │   └── OutdoorAvailabilityResponse.java   (M2)
│   ├── outdoorbooking/
│   │   ├── OutdoorBookingRequest.java         (M2)
│   │   └── OutdoorBookingResponse.java        (M2)
│   └── outdoorcampsite/
│       ├── ModerationRequest.java             (M2)
│       ├── OutdoorCampsiteRequest.java        (M2)
│       └── OutdoorCampsiteResponse.java       (M2)
│
├── repositories/
│   ├── AvailabilityRepository.java            (M1)
│   ├── CampsiteBookingRepository.java         (M1)
│   ├── CampsitePaymentRepository.java         (M1)
│   ├── CampsiteRepository.java                (M1)
│   ├── OutdoorAvailabilityRepository.java     (M2)
│   ├── OutdoorBookingRepository.java          (M2)
│   └── OutdoorCampsiteRepository.java         (M2)
│
├── services/
│   ├── IAvailabilityService.java              (M1)
│   ├── ICampsiteBookingService.java           (M1)
│   ├── ICampsitePaymentService.java           (M1)
│   ├── ICampsiteService.java                  (M1)
│   ├── IOutdoorAvailabilityService.java       (M2)
│   ├── IOutdoorBookingService.java            (M2)
│   ├── IOutdoorCampsiteService.java           (M2)
│   └── impl/
│       ├── IAvailabilityServiceImpl.java      (M1)
│       ├── ICampsiteBookingServiceImpl.java   (M1)
│       ├── ICampsitePaymentServiceImpl.java   (M1)
│       ├── ICampsiteServiceImpl.java          (M1)
│       ├── IOutdoorAvailabilityServiceImpl.java (M2)
│       ├── IOutdoorBookingServiceImpl.java    (M2)
│       └── IOutdoorCampsiteServiceImpl.java   (M2)
│
├── controllers/
│   ├── AvailabilityController.java            (M1)
│   ├── CampsiteBookingController.java         (M1)
│   ├── CampsiteController.java                (M1)
│   ├── CampsitePaymentController.java         (M1)
│   ├── OutdoorAvailabilityController.java     (M2)
│   ├── OutdoorBookingController.java          (M2)
│   └── OutdoorCampsiteController.java         (M2)
│
└── jwt/
    └── SecurityConfig.java   (extended — not new)

back/src/test/java/tn/esprit/projetpidev/
├── controllers/
│   ├── CampsiteBookingControllerTest.java     (M1)
│   └── CampsiteControllerTest.java            (M1)
└── services/
    ├── ICampsiteBookingServiceImplTest.java   (M1)
    ├── ICampsiteServiceImplTest.java          (M1)
    └── IOutdoorCampsiteServiceImplTest.java   (M2)

front/src/app/
├── services/
│   ├── campsite.service.ts                    (M1)
│   ├── campsite-booking.service.ts            (M1)
│   └── outdoor-campsite.service.ts            (M2)
│
├── frontoffice/pages/
│   ├── campsites/
│   │   ├── campsites.component.ts             (M1)
│   │   ├── campsites.component.html           (M1)
│   │   └── campsites.component.css            (M1)
│   ├── campsite-detail/
│   │   ├── campsite-detail.component.ts       (M1)
│   │   ├── campsite-detail.component.html     (M1)
│   │   └── campsite-detail.component.css      (M1)
│   ├── campsite-booking/
│   │   ├── campsite-booking.component.ts      (M1)
│   │   ├── campsite-booking.component.html    (M1)
│   │   └── campsite-booking.component.css     (M1)
│   ├── campsite-payment/
│   │   ├── campsite-payment.component.ts      (M1)
│   │   ├── campsite-payment.component.html    (M1)
│   │   └── campsite-payment.component.css     (M1)
│   ├── my-bookings/
│   │   ├── my-bookings.component.ts           (M1+M2)
│   │   ├── my-bookings.component.html         (M1+M2)
│   │   └── my-bookings.component.css          (M1+M2)
│   ├── outdoor-trips/
│   │   ├── outdoor-trips.component.ts         (M2)
│   │   ├── outdoor-trips.component.html       (M2)
│   │   └── outdoor-trips.component.css        (M2)
│   ├── outdoor-campsite-detail/
│   │   ├── outdoor-campsite-detail.component.ts   (M2)
│   │   ├── outdoor-campsite-detail.component.html (M2)
│   │   └── outdoor-campsite-detail.component.css  (M2)
│   ├── outdoor-booking/
│   │   ├── outdoor-booking.component.ts       (M2)
│   │   ├── outdoor-booking.component.html     (M2)
│   │   └── outdoor-booking.component.css      (M2)
│   ├── propose-outdoor/
│   │   ├── propose-outdoor.component.ts       (M2)
│   │   ├── propose-outdoor.component.html     (M2)
│   │   └── propose-outdoor.component.css      (M2)
│   └── my-proposals/
│       ├── my-proposals.component.ts          (M2)
│       ├── my-proposals.component.html        (M2)
│       └── my-proposals.component.css         (M2)
│
├── dashboards/
│   ├── campsite-owner/
│   │   ├── campsite-owner.component.ts        (M1)
│   │   ├── campsite-owner.component.html      (M1)
│   │   └── campsite-owner.component.css       (M1)
│   └── admin-outdoor-moderation/
│       ├── admin-outdoor-moderation.component.ts   (M2)
│       ├── admin-outdoor-moderation.component.html (M2)
│       └── admin-outdoor-moderation.component.css  (M2)
│
└── guards/
    └── require-auth.guard.ts                  (M1+M2)
```

---

## Email System — MailDev & HTML Templates

### Overview

The email system was upgraded from plain-text SMTP to **HTML emails via Thymeleaf templates**, backed by **MailDev** as the local development mail server.

| Concern | Before | After |
|---------|--------|-------|
| Mail server | Gmail SMTP (requires real credentials) | MailDev on `localhost:1025` (no auth) |
| Email format | Plain text | HTML (Thymeleaf templates) |
| `JavaMailSender` | Built manually per-send via `JavaMailSenderImpl` | Spring-managed bean (auto-configured) |
| Template engine | None | Thymeleaf `TemplateEngine` |
| Dev visibility | Emails sent to real inboxes | All emails captured in MailDev web UI at `http://localhost:1080` |

---

### Infrastructure — MailDev in Docker Compose

**File:** `docker-compose.yml`

```yaml
maildev:
  image: maildev/maildev
  container_name: campway-maildev
  ports:
    - "1025:1025"   # SMTP — used by Spring Boot backend
    - "1080:1080"   # Web UI — http://localhost:1080
  restart: unless-stopped
```

Start with:
```bash
docker compose up -d
```

Open **http://localhost:1080** to inspect all sent emails in a browser.

---

### application.properties — Mail Config

**File:** `back/src/main/resources/application.properties`

```properties
# MailDev (local dev email server)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=noreply@campway.dev
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false

app.frontend.url=http://localhost:4200
```

Removed:
- `spring.mail.username=${MAIL_USERNAME:}` / `spring.mail.password=${MAIL_PASSWORD:}` (no credentials needed for MailDev)
- `spring.autoconfigure.exclude=...MailSenderAutoConfiguration` (Spring now auto-configures `JavaMailSender`)

---

### pom.xml — New Dependency

**File:** `back/pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

Provides `TemplateEngine` bean used by `EmailService` to render HTML templates.

---

### EmailService — Rewritten

**File:** `back/src/main/java/tn/esprit/projetpidev/services/EmailService.java`

#### Key changes

| Aspect | Before | After |
|--------|--------|-------|
| Mail sender | `JavaMailSenderImpl` built per-call | `@Autowired JavaMailSender` (Spring bean) |
| Message type | `SimpleMailMessage` (plain text) | `MimeMessage` + `MimeMessageHelper` (HTML) |
| Template rendering | String concatenation | `TemplateEngine.process(templateName, ctx)` |
| SMTP profile logic | Custom auto-detect map (9 providers) | Removed — config is in `application.properties` |

#### Class structure

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;        // Spring-managed
    private final TemplateEngine templateEngine;    // Thymeleaf

    @Value("${spring.mail.username:noreply@campway.dev}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // Public methods — unchanged signatures, same callers
    public void sendBookingConfirmationEmail(String toEmail, String camperName,
                                              String campsiteName, LocalDate checkIn,
                                              LocalDate checkOut, BigDecimal totalPrice)

    public void sendOutdoorCampsiteApprovalEmail(String toEmail, String proposerName,
                                                  String campsiteName, boolean approved,
                                                  String adminNote)

    public void sendPasswordResetEmail(String toEmail, String token)

    // Private helper
    private void sendHtmlEmail(String to, String subject, String template, Context ctx)
}
```

All three public methods **signatures are unchanged** — existing callers in `ICampsiteBookingServiceImpl`, `IOutdoorCampsiteServiceImpl`, and `IAuthServiceImpl` required no modification.

---

### HTML Email Templates (Thymeleaf)

**Location:** `back/src/main/resources/templates/`

#### 1. `booking-confirmation.html`

Triggered by: `ICampsiteBookingServiceImpl.confirm()` → `emailService.sendBookingConfirmationEmail()`

| Thymeleaf variable | Source | Example |
|--------------------|--------|---------|
| `${camperName}` | `booking.getCamper().getFirstName()` | `Ranim` |
| `${campsiteName}` | `booking.getCampsite().getName()` | `Forest Camp Ain Draham` |
| `${checkIn}` | `booking.getCheckInDate()` | `2025-07-10` |
| `${checkOut}` | `booking.getCheckOutDate()` | `2025-07-14` |
| `${totalPrice}` | `booking.getTotalPrice() + " TND"` | `200.00 TND` |
| `${frontendUrl}` | `app.frontend.url` property | `http://localhost:4200` |

CTA button links to: `${frontendUrl}/my-bookings`

#### 2. `outdoor-approval.html`

Triggered by: `IOutdoorCampsiteServiceImpl.moderate()` → `emailService.sendOutdoorCampsiteApprovalEmail()`

| Thymeleaf variable | Source | Example |
|--------------------|--------|---------|
| `${proposerName}` | `campsite.getProposer().getFirstName()` | `Mohamed` |
| `${campsiteName}` | `campsite.getName()` | `Hidden Valley Mateur` |
| `${approved}` | `action == APPROVE` | `true` / `false` |
| `${status}` | `"APPROVED"` or `"REJECTED"` | `APPROVED` |
| `${adminNote}` | `request.getAdminNote()` | `Great location, approved!` |
| `${frontendUrl}` | `app.frontend.url` property | `http://localhost:4200` |

- Header is **green** when approved, **red** when rejected
- CTA links to `/outdoor-campsites` (approved) or `/propose-outdoor` (rejected)
- Admin note box only shown if `adminNote` is non-blank

#### 3. `password-reset.html`

Triggered by: `IAuthServiceImpl.forgotPassword()` → `emailService.sendPasswordResetEmail()`

| Thymeleaf variable | Source | Example |
|--------------------|--------|---------|
| `${resetLink}` | `frontendUrl + "/reset-password?token=" + token` | `http://localhost:4200/reset-password?token=uuid` |
| `${frontendUrl}` | `app.frontend.url` property | `http://localhost:4200` |

- Token valid for **15 minutes** (enforced in `IAuthServiceImpl.resetPassword()`)
- Fallback plain-text link shown below the button

---

### Email Flow Diagrams

#### Booking Confirmation Flow
```
POST /api/v1/campsite-bookings/{id}/confirm
  └─► ICampsiteBookingServiceImpl.confirm()
        ├─► booking.setStatus(CONFIRMED)
        ├─► bookingRepo.save(booking)
        └─► emailService.sendBookingConfirmationEmail(
                camper.email, camper.firstName,
                campsite.name, checkIn, checkOut, totalPrice)
              └─► templateEngine.process("booking-confirmation", ctx)
              └─► mailSender.send(mimeMessage)
                    └─► MailDev captures → http://localhost:1080
```

#### Outdoor Moderation Flow
```
PATCH /api/v1/outdoor-campsites/{id}/moderate
  └─► IOutdoorCampsiteServiceImpl.moderate()
        ├─► campsite.setStatus(APPROVED | REJECTED)
        ├─► outdoorCampsiteRepo.save(campsite)
        └─► emailService.sendOutdoorCampsiteApprovalEmail(
                proposer.email, proposer.firstName,
                campsite.name, approved, adminNote)
              └─► templateEngine.process("outdoor-approval", ctx)
              └─► mailSender.send(mimeMessage)
                    └─► MailDev captures → http://localhost:1080
```

#### Password Reset Flow
```
POST /auth/forgot-password  { email }
  └─► IAuthServiceImpl.forgotPassword()
        ├─► user.setPasswordResetToken(UUID)
        ├─► user.setPasswordResetExpiry(now + 15min)
        ├─► userRepo.save(user)
        └─► emailService.sendPasswordResetEmail(user.email, token)
              └─► templateEngine.process("password-reset", ctx)
              └─► mailSender.send(mimeMessage)
                    └─► MailDev captures → http://localhost:1080

POST /auth/reset-password  { token, newPassword }
  └─► IAuthServiceImpl.resetPassword()
        ├─► validate token + expiry
        ├─► user.setPassword(bcrypt(newPassword))
        ├─► user.setPasswordResetToken(null)
        └─► userRepo.save(user)
```

---

## Frontend Navigation Updates

### File Changed

**`front/src/app/frontoffice/layout/header/header.component.html`**

---

### What Was Added

#### Logged-in users nav — Outdoor dropdown

Replaced the absence of outdoor links with a **Bootstrap dropdown** containing all three outdoor-related routes:

```
Outdoor ▾
  ├── 🏔 Outdoor Trips       → /outdoor-campsites
  ├── 📍 Propose a Spot      → /propose-outdoor   (requireAuthGuard)
  └── 📋 My Proposals        → /my-proposals      (requireAuthGuard)
```

The dropdown uses `routerLinkActive` with `[routerLinkActiveOptions]="{exact:false}"` so the parent "Outdoor" label is highlighted when any child route is active.

Also changed "My Bookings" link target from `/bookings` (generic) to `/my-bookings` (module-specific campsite bookings page).

#### Guest nav — Outdoor Trips link

Added a direct "Outdoor Trips" link pointing to `/outdoor-campsites` (public, no auth required):

```
Home | Campsites | Outdoor Trips | Shop | Events
```

---

### Route Table (all outdoor-related routes)

| Route | Component | Guard | Visible in nav |
|-------|-----------|-------|----------------|
| `/outdoor-campsites` | `OutdoorTripsComponent` | none | Logged-in + Guest |
| `/outdoor-campsites/:id` | `OutdoorCampsiteDetailComponent` | none | — (linked from list) |
| `/outdoor-campsites/:id/book` | `OutdoorBookingComponent` | `requireAuthGuard` | — (linked from detail) |
| `/propose-outdoor` | `ProposeOutdoorComponent` | `requireAuthGuard` | Logged-in only (Outdoor dropdown) |
| `/my-proposals` | `MyProposalsComponent` | `requireAuthGuard` | Logged-in only (Outdoor dropdown) |
| `/admin/outdoor-moderation` | `AdminOutdoorModerationComponent` | `adminGuard` | Admin sidebar |

---

### Before / After Nav Structure

#### Before (logged-in)
```
Home | Campsites | My Bookings | Shop | Events | Forum | Messages
```

#### After (logged-in)
```
Home | Campsites | Outdoor ▾ | My Bookings | Shop | Events | Forum | Messages
                   ├── Outdoor Trips
                   ├── Propose a Spot
                   └── My Proposals
```

#### Before (guest)
```
Home | Campsites | Shop | Events
```

#### After (guest)
```
Home | Campsites | Outdoor Trips | Shop | Events
```
