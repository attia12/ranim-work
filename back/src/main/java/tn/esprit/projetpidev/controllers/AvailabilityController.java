// Module: Official Campsite & Booking | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.availability.AvailabilityRequest;
import tn.esprit.projetpidev.dto.availability.AvailabilityResponse;
import tn.esprit.projetpidev.services.IAvailabilityService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
@Tag(name = "Availabilities", description = "Campsite availability windows management")
public class AvailabilityController {

    private final IAvailabilityService availabilityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Create availability window")
    public ResponseEntity<AvailabilityResponse> create(@Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Update availability window")
    public ResponseEntity<AvailabilityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Delete availability window")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        availabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campsite/{campsiteId}")
    @Operation(summary = "Get all availability windows for a campsite")
    public ResponseEntity<List<AvailabilityResponse>> getByCampsite(@PathVariable Long campsiteId) {
        return ResponseEntity.ok(availabilityService.getByCampsite(campsiteId));
    }
}
