// Module: Campsite Status | Layer: Controller
package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.dto.campsite.CampsiteStatusHistoryResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.CampsiteStatusHistoryRepository;
import tn.esprit.projetpidev.services.CampsiteStatusEvaluator;
import tn.esprit.projetpidev.services.CampsiteStatusScheduler;
import tn.esprit.projetpidev.services.CampsiteStatusUpdater;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/campsite-status")
@RequiredArgsConstructor
@Tag(name = "Campsite Status", description = "Automatic campsite status management")
public class CampsiteStatusController {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteStatusHistoryRepository historyRepository;
    private final CampsiteStatusEvaluator evaluator;
    private final CampsiteStatusUpdater updater;
    private final CampsiteStatusScheduler scheduler;

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: manually trigger a full status refresh for all campsites")
    public ResponseEntity<Map<String, String>> refresh() {
        scheduler.refreshStatuses();
        return ResponseEntity.ok(Map.of("message", "Status refresh triggered."));
    }

    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPSITEOWNERS')")
    @Operation(summary = "Admin/Owner: evaluate and apply status for a single campsite")
    public ResponseEntity<Map<String, Object>> refreshOne(
            @PathVariable Long id,
            @AuthenticationPrincipal User principal) {
        Campsite campsite = campsiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", id));
        CampsiteStatusEvaluator.Evaluation eval = evaluator.evaluate(campsite);
        boolean changed = updater.applyIfChanged(campsite, eval.status(), eval.reason(),
                "ADMIN:" + principal.getEmail());
        return ResponseEntity.ok(Map.of(
                "newStatus", eval.status().name(),
                "reason", eval.reason(),
                "changed", changed
        ));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPSITEOWNERS')")
    @Operation(summary = "Get status history for a campsite")
    public ResponseEntity<List<CampsiteStatusHistoryResponse>> getHistory(@PathVariable Long id) {
        if (!campsiteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Campsite", id);
        }
        List<CampsiteStatusHistoryResponse> history = historyRepository
                .findByCampsite_IdOrderByChangedAtDesc(id)
                .stream()
                .map(h -> {
                    CampsiteStatusHistoryResponse r = new CampsiteStatusHistoryResponse();
                    r.setId(h.getId());
                    r.setPreviousStatus(h.getPreviousStatus());
                    r.setNewStatus(h.getNewStatus());
                    r.setReason(h.getReason());
                    r.setChangedBy(h.getChangedBy());
                    r.setChangedAt(h.getChangedAt());
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/current/{id}")
    @Operation(summary = "Preview what status the campsite would get without applying it")
    public ResponseEntity<Map<String, Object>> preview(@PathVariable Long id) {
        Campsite campsite = campsiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campsite", id));
        CampsiteStatusEvaluator.Evaluation eval = evaluator.evaluate(campsite);
        return ResponseEntity.ok(Map.of(
                "currentStatus", campsite.getStatus().name(),
                "evaluatedStatus", eval.status().name(),
                "reason", eval.reason()
        ));
    }
}
