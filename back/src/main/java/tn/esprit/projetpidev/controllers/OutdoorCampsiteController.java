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
import tn.esprit.projetpidev.dto.outdoorcampsite.ModerationRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteResponse;
import tn.esprit.projetpidev.services.IOutdoorCampsiteService;

@RestController
@RequestMapping("/api/v1/outdoor-campsites")
@RequiredArgsConstructor
@Tag(name = "Outdoor Campsites", description = "Camper-proposed outdoor campsite management")
public class OutdoorCampsiteController {

    private final IOutdoorCampsiteService outdoorCampsiteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Propose a new outdoor campsite")
    public ResponseEntity<OutdoorCampsiteResponse> propose(
            @Valid @RequestBody OutdoorCampsiteRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(outdoorCampsiteService.propose(request, principal.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a PENDING proposal")
    public ResponseEntity<OutdoorCampsiteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OutdoorCampsiteRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(outdoorCampsiteService.update(id, request, principal.getId()));
    }

    @PatchMapping("/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: approve or reject a proposal")
    public ResponseEntity<OutdoorCampsiteResponse> moderate(
            @PathVariable Long id,
            @RequestBody ModerationRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(outdoorCampsiteService.moderate(id, request, principal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get outdoor campsite by ID")
    public ResponseEntity<OutdoorCampsiteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(outdoorCampsiteService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List approved outdoor campsites")
    public ResponseEntity<Page<OutdoorCampsiteResponse>> getApproved(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(outdoorCampsiteService.getApproved(pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list pending proposals")
    public ResponseEntity<Page<OutdoorCampsiteResponse>> getPending(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(outdoorCampsiteService.getPending(pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my proposals")
    public ResponseEntity<Page<OutdoorCampsiteResponse>> getMyProposals(
            @AuthenticationPrincipal User principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(outdoorCampsiteService.getMyProposals(principal.getId(), pageable));
    }
}
