// Module: Official Campsite & Booking | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingRequest;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingResponse;
import tn.esprit.projetpidev.dto.campsitebooking.CancelBookingRequest;
import tn.esprit.projetpidev.services.ICampsiteBookingService;

@RestController
@RequestMapping("/api/v1/campsite-bookings")
@RequiredArgsConstructor
@Tag(name = "Campsite Bookings", description = "Book and manage campsite reservations")
public class CampsiteBookingController {

    private final ICampsiteBookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new campsite booking")
    public ResponseEntity<CampsiteBookingResponse> create(
            @Valid @RequestBody CampsiteBookingRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request, principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<CampsiteBookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my bookings (camper view)")
    public ResponseEntity<Page<CampsiteBookingResponse>> getMyBookings(
            @AuthenticationPrincipal User principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyCamperBookings(principal.getId(), pageable));
    }

    @GetMapping("/campsite/{campsiteId}")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Get all bookings for a specific campsite")
    public ResponseEntity<Page<CampsiteBookingResponse>> getByCampsite(
            @PathVariable Long campsiteId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getByCampsite(campsiteId, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get all bookings")
    public ResponseEntity<Page<CampsiteBookingResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAll(pageable));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<CampsiteBookingResponse> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest cancelRequest,
            @AuthenticationPrincipal User principal) {
        String reason = cancelRequest != null ? cancelRequest.getReason() : null;
        return ResponseEntity.ok(bookingService.cancel(id, principal.getId(), reason));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Confirm a booking (owner/admin)")
    public ResponseEntity<CampsiteBookingResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirm(id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Mark a booking as completed")
    public ResponseEntity<CampsiteBookingResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.complete(id));
    }
}
