package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.vehicle.VehicleRequest;
import tn.esprit.projetpidev.dto.vehicle.VehicleResponse;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;
import tn.esprit.projetpidev.domain.User;

import java.util.List;

public interface IVehicleService {
    VehicleResponse registerVehicle(VehicleRequest request, User loggedInUser);
    VehicleResponse getVehicleById(Long id);
    List<VehicleResponse> getAllVehicles();
    List<VehicleResponse> getVehiclesByOwner(Long ownerId);
    List<VehicleResponse> getAvailableVehicles();
    VehicleResponse updateVehicle(Long id, VehicleRequest request, User loggedInUser);
    VehicleResponse verifyVehicle(Long id);
    VehicleResponse updateAvailability(Long id, AvailabilityStatus status, User loggedInUser);
    void deleteVehicle(Long id, User loggedInUser);
}

