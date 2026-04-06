package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.booking.BookingRequest;
import tn.esprit.projetpidev.dto.booking.BookingResponse;
import tn.esprit.projetpidev.services.IBookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Event Bookings", description = "Book and cancel event participation")
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest req) {
        return ResponseEntity.ok(bookingService.createBooking(req));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    /** GET /bookings?userId=45  — all bookings for a user */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    /** GET /bookings/event/{eventId}  — all bookings for an event */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(bookingService.getBookingsByEvent(eventId));
    }

    /** PATCH /bookings/{id}/status?status=CONFIRMED  — organizer/admin confirms or rejects */
    @PatchMapping("/{bookingId}/status")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long bookingId,
            @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateStatus(bookingId, status));
    }

    /** DELETE /bookings/{id}  — user cancels their own booking */
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> cancel(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled successfully.");
    }
}
