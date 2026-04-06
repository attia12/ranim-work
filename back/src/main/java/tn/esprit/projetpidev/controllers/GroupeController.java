package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.Groupe;
import tn.esprit.projetpidev.dto.groupe.GroupeRequest;
import tn.esprit.projetpidev.dto.groupe.GroupeResponse;
import tn.esprit.projetpidev.services.IGroupeService;

import java.util.List;

@RestController
@RequestMapping("/groupes")
@RequiredArgsConstructor
public class GroupeController {

    private final IGroupeService groupeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<GroupeResponse> createGroupe(
            @Valid @RequestBody GroupeRequest request) {
        return ResponseEntity.ok(groupeService.createGroupe(request));
    }

    @GetMapping("/{groupeId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<GroupeResponse> getGroupeById(@PathVariable Long groupeId) {
        return ResponseEntity.ok(groupeService.getGroupeById(groupeId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<List<GroupeResponse>> getAllGroupes() {
        return ResponseEntity.ok(groupeService.getAllGroupes());
    }

    @GetMapping("/public")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupeResponse>> getPublicGroupes() {
        return ResponseEntity.ok(groupeService.getPublicGroupes());
    }

    @PutMapping("/{groupeId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<GroupeResponse> updateGroupe(
            @PathVariable Long groupeId,
            @Valid @RequestBody GroupeRequest request) {
        return ResponseEntity.ok(groupeService.updateGroupe(groupeId, request));
    }

    @DeleteMapping("/{groupeId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<String> deleteGroupe(@PathVariable Long groupeId) {
        groupeService.deleteGroupe(groupeId);
        return ResponseEntity.ok("Group deleted successfully !");
    }
    // Affectation user à groupe
    @PutMapping("/{groupeId}/add-user/{userId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<Groupe> addUserToGroupe(
            @PathVariable Long groupeId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(groupeService.addUserToGroupe(groupeId, userId));
    }

    // Désaffectation user du groupe
    @PutMapping("/{groupeId}/remove-user/{userId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<Groupe> removeUserFromGroupe(
            @PathVariable Long groupeId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(groupeService.removeUserFromGroupe(groupeId, userId));
    }
}