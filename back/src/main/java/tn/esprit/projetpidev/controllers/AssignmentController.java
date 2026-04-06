package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.assignment.AssignmentRequest;
import tn.esprit.projetpidev.dto.assignment.AssignmentResponse;
import tn.esprit.projetpidev.services.IAssignmentService;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assign guides to events")
public class AssignmentController {

    private final IAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<AssignmentResponse> create(
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.createAssignment(request));
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AssignmentResponse> getById(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssignmentResponse>> getAll() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssignmentResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByEvent(eventId));
    }

    @GetMapping("/guide/{guideUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssignmentResponse>> getByGuide(@PathVariable Long guideUserId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByGuide(guideUserId));
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<AssignmentResponse> update(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, request));
    }

    /** Patch just the status: ASSIGNED | COMPLETED | CANCELLED */
    @PatchMapping("/{assignmentId}/status")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<AssignmentResponse> updateStatus(
            @PathVariable Long assignmentId,
            @RequestParam String status) {
        return ResponseEntity.ok(assignmentService.updateStatus(assignmentId, status));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('EVENT_ORGANIZER', 'ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long assignmentId) {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.ok("Assignment deleted successfully.");
    }
}
