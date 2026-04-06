package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.dto.vehicle.VehicleRequest;
import tn.esprit.projetpidev.dto.vehicle.VehicleResponse;
import tn.esprit.projetpidev.domain.Vehicle;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;
import tn.esprit.projetpidev.repositories.VehicleRepository;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IVehicleServiceImpl implements IVehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public VehicleResponse registerVehicle(VehicleRequest request, User loggedInUser) {
        if (loggedInUser.getRole() != Role.DELIVERYAGENT) {
            throw new IllegalArgumentException("Only delivery agents (DELIVERYAGENT) can register vehicles");
        }
        if (vehicleRepository.existsByPlate(request.getPlate())) {
            throw new IllegalStateException("A vehicle with plate '" + request.getPlate() + "' already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .type(request.getType())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .plate(request.getPlate().toUpperCase())
                .serviceRadius(request.getServiceRadius())
                .maxConcurrentDeliveries(request.getMaxConcurrentDeliveries() != null ? request.getMaxConcurrentDeliveries() : 1)
                .currentDeliveriesCount(0)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .isVerified(false)
                .insuranceExpiryDate(request.getInsuranceExpiryDate())
                .owner(loggedInUser)
                .build();

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return mapToResponse(vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByOwner(Long ownerId) {
        return vehicleRepository.findByOwnerId(ownerId).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableVehicles() {
        return vehicleRepository.findByIsVerifiedTrueAndAvailabilityStatus(AvailabilityStatus.AVAILABLE)
                .stream()
                .filter(v -> v.getCurrentDeliveriesCount() < v.getMaxConcurrentDeliveries())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request, User loggedInUser) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        boolean isOwner = vehicle.getOwner().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can update this vehicle");
        }

        vehicle.setType(request.getType());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setPlate(request.getPlate().toUpperCase());
        vehicle.setServiceRadius(request.getServiceRadius());
        vehicle.setMaxConcurrentDeliveries(request.getMaxConcurrentDeliveries());
        vehicle.setInsuranceExpiryDate(request.getInsuranceExpiryDate());

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse verifyVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        vehicle.setIsVerified(true);
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse updateAvailability(Long id, AvailabilityStatus status, User loggedInUser) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        boolean isOwner = vehicle.getOwner().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can update availability");
        }

        vehicle.setAvailabilityStatus(status);
        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long id, User loggedInUser) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        if (vehicle.getCurrentDeliveriesCount() > 0) {
            throw new IllegalStateException("Cannot delete vehicle with active deliveries");
        }

        boolean isOwner = vehicle.getOwner().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can delete this vehicle");
        }

        vehicleRepository.deleteById(id);
    }

    private VehicleResponse mapToResponse(Vehicle v) {
        VehicleResponse r = new VehicleResponse();
        r.setId(v.getId());
        r.setType(v.getType());
        r.setBrand(v.getBrand());
        r.setModel(v.getModel());
        r.setYear(v.getYear());
        r.setColor(v.getColor());
        r.setPlate(v.getPlate());
        r.setServiceRadius(v.getServiceRadius());
        r.setMaxConcurrentDeliveries(v.getMaxConcurrentDeliveries());
        r.setCurrentDeliveriesCount(v.getCurrentDeliveriesCount());
        r.setAvailabilityStatus(v.getAvailabilityStatus());
        r.setIsVerified(v.getIsVerified());
        r.setInsuranceExpiryDate(v.getInsuranceExpiryDate());
        r.setOwnerId(v.getOwner().getId());
        r.setOwnerFullName(v.getOwner().getFullname());
        return r;
    }
}

