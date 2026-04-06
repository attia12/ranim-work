package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.vehicle.VehicleRequest;
import tn.esprit.projetpidev.dto.vehicle.VehicleResponse;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;
import tn.esprit.projetpidev.services.IVehicleService;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/vehicles")
@RequiredArgsConstructor
@Tag(name = " Vehicles", description = "Register and manage delivery vehicles")
public class VehicleController {

    private final IVehicleService vehicleService;
    private final UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VehicleResponse>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<VehicleResponse>> getAvailable() {
        return ResponseEntity.ok(vehicleService.getAvailableVehicles());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<List<VehicleResponse>> getMy(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vehicleService.getVehiclesByOwner(user.getId()));
    }

    @GetMapping("/owner/{id}")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<List<VehicleResponse>> getByOwner(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehiclesByOwner(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<VehicleResponse> register(@Valid @RequestBody VehicleRequest request,
                                                     Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vehicleService.registerVehicle(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody VehicleRequest request,
                                                   Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request, user));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.verifyVehicle(id));
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<VehicleResponse> updateAvailability(@PathVariable Long id,
                                                               @RequestParam AvailabilityStatus status,
                                                               Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(vehicleService.updateAvailability(id, status, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DELIVERYAGENT','ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        vehicleService.deleteVehicle(id, user);
        return ResponseEntity.ok("Vehicle deleted successfully!");
    }
}

