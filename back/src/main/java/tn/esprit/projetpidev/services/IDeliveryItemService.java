package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemRequest;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemResponse;

import java.util.List;

public interface IDeliveryItemService {
    DeliveryItemResponse addItemToDelivery(DeliveryItemRequest request, tn.esprit.projetpidev.domain.User requester);
    DeliveryItemResponse getDeliveryItemById(Long itemId);
    List<DeliveryItemResponse> getItemsByDelivery(Long deliveryId);
    DeliveryItemResponse updateDeliveryItem(Long itemId, DeliveryItemRequest request, tn.esprit.projetpidev.domain.User requester);
    void removeDeliveryItem(Long itemId, tn.esprit.projetpidev.domain.User requester);
}

