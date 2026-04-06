package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.equipment.EquipmentRequest;
import tn.esprit.projetpidev.dto.equipment.EquipmentResponse;
import tn.esprit.projetpidev.services.IEquipmentService;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "CRUD operations for camping equipment listings")
public class EquipmentController {

    private final IEquipmentService equipmentService;
    private final UserRepository    userRepo;

    // ── Public read endpoints ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAll() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EquipmentResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(equipmentService.searchEquipment(q));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<EquipmentResponse>> getByCategory(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentByCategory(id));
    }

    @GetMapping("/owner/{id}")
    public ResponseEntity<List<EquipmentResponse>> getByOwner(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentByOwner(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ── NEW: Availability endpoints (public — no auth needed) ────────────────
    // These must come BEFORE /{id} catch-all routes to avoid routing conflicts.
    // SecurityConfig already permits GET on these paths — no @PreAuthorize needed.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/marketplace/equipment/{id}/unavailable-periods
     * Returns periods when this equipment is already rented (active orders).
     * Used by the product detail page to disable taken dates in the date picker.
     */
    @GetMapping("/{id}/unavailable-periods")
    public ResponseEntity<List<Map<String, String>>> getUnavailablePeriods(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getUnavailablePeriods(id));
    }

    /**
     * GET /api/marketplace/equipment/{id}/blocked-periods
     * Returns periods manually blocked by the equipment owner.
     * Used by the gear-provider dashboard to show / manage blockouts.
     */
    @GetMapping("/{id}/blocked-periods")
    public ResponseEntity<List<Map<String, String>>> getBlockedPeriods(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getBlockedPeriods(id));
    }

    // ── Protected write endpoints ─────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request,
                                                    Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(equipmentService.createEquipment(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<EquipmentResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody EquipmentRequest request,
                                                    Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(equipmentService.updateEquipment(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        equipmentService.deleteEquipment(id, user);
        return ResponseEntity.ok("Equipment deleted successfully!");
    }
}