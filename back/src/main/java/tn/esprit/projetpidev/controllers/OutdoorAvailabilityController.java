// Module: Outdoor Campsite & Booking | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityRequest;
import tn.esprit.projetpidev.dto.outdooravailability.OutdoorAvailabilityResponse;
import tn.esprit.projetpidev.services.IOutdoorAvailabilityService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/outdoor-availabilities")
@RequiredArgsConstructor
@Tag(name = "Outdoor Availabilities", description = "Outdoor campsite availability management")
public class OutdoorAvailabilityController {

    private final IOutdoorAvailabilityService availabilityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create outdoor availability window")
    public ResponseEntity<OutdoorAvailabilityResponse> create(@Valid @RequestBody OutdoorAvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update outdoor availability window")
    public ResponseEntity<OutdoorAvailabilityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OutdoorAvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete outdoor availability window")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        availabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get availability windows for an outdoor campsite")
    public ResponseEntity<List<OutdoorAvailabilityResponse>> getBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(availabilityService.getBySite(siteId));
    }
}
