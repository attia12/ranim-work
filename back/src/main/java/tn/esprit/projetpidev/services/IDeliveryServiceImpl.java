package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.dto.delivery.DeliveryRequest;
import tn.esprit.projetpidev.dto.delivery.DeliveryResponse;
import tn.esprit.projetpidev.dto.delivery.DeliveryStatusUpdateRequest;
import tn.esprit.projetpidev.domain.Delivery;
import tn.esprit.projetpidev.domain.Vehicle;
import tn.esprit.projetpidev.domain.enums.AvailabilityStatus;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.repositories.VehicleRepository;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.domain.Order;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.repositories.OrderRepository;
import tn.esprit.projetpidev.repositories.AgentProfileRepository;

import tn.esprit.projetpidev.domain.userprofile.AgentProfile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class IDeliveryServiceImpl implements IDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final AgentProfileRepository agentProfileRepo;

    @Override
    public DeliveryResponse createDelivery(DeliveryRequest request, User loggedInUser) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException(
                    "Delivery can only be requested for PAID orders. Current status: " + order.getStatus()
            );
        }

        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalStateException("A delivery already exists for this order");
        }

        Delivery delivery = Delivery.builder()
                .status(DeliveryStatus.PENDING_ASSIGNMENT)
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .scheduledPickupTime(request.getScheduledPickupTime())
                .scheduledDeliveryTime(request.getScheduledDeliveryTime())
                .deliveryNotes(request.getDeliveryNotes())
                .actualCost(request.getActualCost())
                .camper(loggedInUser)
                .order(order)
                .build();

        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(Long id, User requester) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
        boolean isOwner = delivery.getCamper().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isAgent = delivery.getVehicle() != null &&
                delivery.getVehicle().getOwner().getId().equals(requester.getId());
        if (!isOwner && !isAdmin && !isAgent) {
            throw new IllegalArgumentException("You are not authorized to view this delivery");
        }
        return mapToResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getAvailableDeliveries() {
        return deliveryRepository.findByStatus(DeliveryStatus.PENDING_ASSIGNMENT)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByCamper(Long camperId) {
        return deliveryRepository.findByCamperIdOrderByCreatedAtDesc(camperId).stream()
                .map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByVehicle(Long vehicleId) {
        return deliveryRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByAgent(Long agentId) {
        return deliveryRepository.findByVehicleOwnerId(agentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DeliveryResponse assignVehicle(Long deliveryId, Long vehicleId, User loggedInUser) {
        if (loggedInUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can assign vehicles to deliveries");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", deliveryId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

        if (!vehicle.getIsVerified()) {
            throw new IllegalStateException("Vehicle is not verified");
        }
        if (vehicle.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is not available");
        }
        if (vehicle.getCurrentDeliveriesCount() >= vehicle.getMaxConcurrentDeliveries()) {
            throw new IllegalStateException("Vehicle has reached max concurrent deliveries");
        }

        delivery.setVehicle(vehicle);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        vehicle.setCurrentDeliveriesCount(vehicle.getCurrentDeliveriesCount() + 1);
        if (vehicle.getCurrentDeliveriesCount() >= vehicle.getMaxConcurrentDeliveries()) {
            vehicle.setAvailabilityStatus(AvailabilityStatus.BUSY);
        }
        vehicleRepository.save(vehicle);

        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryResponse claimDelivery(Long deliveryId, User agent) {
        // Block unverified agents — also blocks agents with no profile at all
        AgentProfile agentProfile = agentProfileRepo.findByUserId(agent.getId())
            .orElseThrow(() -> new IllegalStateException(
                "You must submit your license number in your profile before claiming deliveries."
            ));
        if (!Boolean.TRUE.equals(agentProfile.getIsVerified())) {
            throw new IllegalStateException(
                "Your account is not yet verified. Submit your license number in your profile " +
                "and wait for admin approval before claiming deliveries."
            );
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", deliveryId));

        if (delivery.getStatus() != DeliveryStatus.PENDING_ASSIGNMENT) {
            throw new IllegalStateException(
                "Only PENDING_ASSIGNMENT deliveries can be claimed. Current status: " + delivery.getStatus()
            );
        }

        // Find the agent's available vehicle
        Vehicle vehicle =
            vehicleRepository.findAll().stream()
                .filter(v -> v.getOwner().getId().equals(agent.getId()))
                .filter(v -> v.getAvailabilityStatus() ==
                             AvailabilityStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "You have no available vehicle. Register or mark a vehicle as available first."
                ));

        delivery.setVehicle(vehicle);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        vehicle.setCurrentDeliveriesCount(vehicle.getCurrentDeliveriesCount() + 1);
        if (vehicle.getCurrentDeliveriesCount() >= vehicle.getMaxConcurrentDeliveries()) {
            vehicle.setAvailabilityStatus(AvailabilityStatus.BUSY);
        }
        vehicleRepository.save(vehicle);

        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryResponse updateStatus(Long deliveryId, DeliveryStatusUpdateRequest request, User loggedInUser) {
        if (loggedInUser.getRole() != Role.DELIVERYAGENT && loggedInUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only delivery agents or admins can update delivery status");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", deliveryId));

        if (loggedInUser.getRole() == Role.DELIVERYAGENT) {
            if (delivery.getVehicle() == null ||
                !delivery.getVehicle().getOwner().getId().equals(loggedInUser.getId())) {
                throw new IllegalArgumentException("You are not authorized to update the status of this delivery");
            }
        }

        validateStatusTransition(delivery.getStatus(), request.getStatus());

        delivery.setStatus(request.getStatus());

        if (request.getActualPickupTime() != null) {
            delivery.setActualPickupTime(request.getActualPickupTime());
        }
        if (request.getActualDeliveryTime() != null) {
            delivery.setActualDeliveryTime(request.getActualDeliveryTime());
        }
        if (request.getProofOfDelivery() != null) {
            delivery.setProofOfDelivery(request.getProofOfDelivery());
        }

        if (request.getStatus() == DeliveryStatus.DELIVERED) {
            if (delivery.getActualDeliveryTime() == null) {
                delivery.setActualDeliveryTime(LocalDateTime.now());
            }
            Vehicle vehicle = delivery.getVehicle();
            if (vehicle != null) {
                vehicle.setCurrentDeliveriesCount(Math.max(0, vehicle.getCurrentDeliveriesCount() - 1));
                if (vehicle.getCurrentDeliveriesCount() == 0) {
                    vehicle.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
                }
                vehicleRepository.save(vehicle);
            }
        }

        return mapToResponse(deliveryRepository.save(delivery));
    }

    @Override
    public void deleteDelivery(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));

        if (delivery.getStatus() != DeliveryStatus.PENDING_ASSIGNMENT) {
            throw new IllegalStateException("Only PENDING_ASSIGNMENT deliveries can be deleted");
        }

        deliveryRepository.deleteById(id);
    }

    private void validateStatusTransition(DeliveryStatus current, DeliveryStatus next) {
        boolean valid = switch (current) {
            case PENDING_ASSIGNMENT -> next == DeliveryStatus.ASSIGNED;
            case ASSIGNED           -> next == DeliveryStatus.AT_PICKUP;
            case AT_PICKUP          -> next == DeliveryStatus.PICKED_UP;
            case PICKED_UP          -> next == DeliveryStatus.IN_TRANSIT;
            case IN_TRANSIT         -> next == DeliveryStatus.DELIVERED
                                    || next == DeliveryStatus.FAILED
                                    || next == DeliveryStatus.RETURNED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + next);
        }
    }

    @Override
    public Optional<DeliveryResponse> getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .map(this::mapToResponse);
    }

    private DeliveryResponse mapToResponse(Delivery d) {
        DeliveryResponse r = new DeliveryResponse();
        r.setId(d.getId());
        r.setStatus(d.getStatus());
        r.setPickupAddress(d.getPickupAddress());
        r.setDeliveryAddress(d.getDeliveryAddress());
        r.setActualCost(d.getActualCost());
        r.setEarningAmount(d.getEarningAmount());
        r.setScheduledPickupTime(d.getScheduledPickupTime());
        r.setScheduledDeliveryTime(d.getScheduledDeliveryTime());
        r.setActualPickupTime(d.getActualPickupTime());
        r.setActualDeliveryTime(d.getActualDeliveryTime());
        r.setProofOfDelivery(d.getProofOfDelivery());
        r.setDeliveryNotes(d.getDeliveryNotes());
        if (d.getVehicle() != null) {
            r.setVehicleId(d.getVehicle().getId());
            r.setVehiclePlate(d.getVehicle().getPlate());
        }
        r.setOrderId(d.getOrder().getId());
        r.setOrderNumber(d.getOrder().getOrderNumber());
        r.setCamperId(d.getCamper().getId());
        r.setCamperFullName(d.getCamper().getFullname());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}

