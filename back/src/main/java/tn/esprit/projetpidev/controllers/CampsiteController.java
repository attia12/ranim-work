// Module: Official Campsite & Booking | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.dto.campsite.CampsiteRequest;
import tn.esprit.projetpidev.dto.campsite.CampsiteResponse;
import tn.esprit.projetpidev.services.ICampsiteService;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/campsites")
@RequiredArgsConstructor
@Tag(name = "Campsites", description = "Official campsite management")
public class CampsiteController {

    private final ICampsiteService campsiteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Create a new campsite")
    public ResponseEntity<CampsiteResponse> create(
            @Valid @RequestBody CampsiteRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campsiteService.create(request, principal.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Update a campsite")
    public ResponseEntity<CampsiteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CampsiteRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(campsiteService.update(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Soft-delete a campsite")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User principal) {
        campsiteService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campsite by ID")
    public ResponseEntity<CampsiteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campsiteService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search/list campsites with optional filters")
    public ResponseEntity<Page<CampsiteResponse>> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) CampsiteType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(campsiteService.search(country, city, type, minPrice, maxPrice, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('COMPSITEOWNERS', 'ADMIN')")
    @Operation(summary = "Get campsites owned by the authenticated user")
    public ResponseEntity<Page<CampsiteResponse>> getMyCampsites(
            @AuthenticationPrincipal User principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(campsiteService.getByOwner(principal.getId(), pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get all campsites including suspended")
    public ResponseEntity<Page<CampsiteResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(campsiteService.getAll(pageable));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: suspend a campsite")
    public ResponseEntity<CampsiteResponse> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(campsiteService.suspend(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: activate a campsite")
    public ResponseEntity<CampsiteResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(campsiteService.activate(id));
    }
}
