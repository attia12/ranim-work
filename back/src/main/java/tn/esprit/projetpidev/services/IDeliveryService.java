package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.delivery.DeliveryRequest;
import tn.esprit.projetpidev.dto.delivery.DeliveryResponse;
import tn.esprit.projetpidev.dto.delivery.DeliveryStatusUpdateRequest;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.domain.User;

import java.util.List;
import java.util.Optional;

public interface IDeliveryService {
    DeliveryResponse createDelivery(DeliveryRequest request, User loggedInUser);
    DeliveryResponse getDeliveryById(Long id, User requester);
    List<DeliveryResponse> getAllDeliveries();
    List<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status);
    List<DeliveryResponse> getAvailableDeliveries();
    List<DeliveryResponse> getDeliveriesByCamper(Long camperId);
    List<DeliveryResponse> getDeliveriesByVehicle(Long vehicleId);
    List<DeliveryResponse> getDeliveriesByAgent(Long agentId);
    DeliveryResponse assignVehicle(Long deliveryId, Long vehicleId, User loggedInUser);
    DeliveryResponse claimDelivery(Long deliveryId, User agent);
    DeliveryResponse updateStatus(Long deliveryId, DeliveryStatusUpdateRequest request, User loggedInUser);
    void deleteDelivery(Long id);
    Optional<DeliveryResponse> getDeliveryByOrderId(Long orderId);
}

