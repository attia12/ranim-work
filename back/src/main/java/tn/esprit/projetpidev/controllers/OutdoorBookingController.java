// Module: Outdoor Campsite & Booking | Layer: Controller
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
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingRequest;
import tn.esprit.projetpidev.dto.outdoorbooking.OutdoorBookingResponse;
import tn.esprit.projetpidev.services.IOutdoorBookingService;

@RestController
@RequestMapping("/api/v1/outdoor-bookings")
@RequiredArgsConstructor
@Tag(name = "Outdoor Bookings", description = "Free outdoor campsite booking (no payment)")
public class OutdoorBookingController {

    private final IOutdoorBookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Book an outdoor campsite (free)")
    public ResponseEntity<OutdoorBookingResponse> create(
            @Valid @RequestBody OutdoorBookingRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request, principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get outdoor booking by ID")
    public ResponseEntity<OutdoorBookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my outdoor bookings")
    public ResponseEntity<Page<OutdoorBookingResponse>> getMyBookings(
            @AuthenticationPrincipal User principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyBookings(principal.getId(), pageable));
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get all bookings for an outdoor campsite")
    public ResponseEntity<Page<OutdoorBookingResponse>> getBySite(
            @PathVariable Long siteId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBySite(siteId, pageable));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel an outdoor booking")
    public ResponseEntity<OutdoorBookingResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(bookingService.cancel(id, principal.getId()));
    }
}
