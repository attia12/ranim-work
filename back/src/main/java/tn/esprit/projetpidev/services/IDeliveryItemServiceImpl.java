package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemRequest;
import tn.esprit.projetpidev.dto.deliveryItem.DeliveryItemResponse;
import tn.esprit.projetpidev.domain.Delivery;
import tn.esprit.projetpidev.domain.DeliveryItem;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;
import tn.esprit.projetpidev.repositories.DeliveryItemRepository;
import tn.esprit.projetpidev.repositories.DeliveryRepository;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.User;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IDeliveryItemServiceImpl implements IDeliveryItemService {

    private final DeliveryItemRepository deliveryItemRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public DeliveryItemResponse addItemToDelivery(DeliveryItemRequest request, User requester) {
        if (request.getDeliveryId() == null) {
            throw new IllegalArgumentException("Delivery ID is required when adding an item");
        }
        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", request.getDeliveryId()));

        boolean isOwner = delivery.getCamper().getId().equals(requester.getId());
        boolean isAdmin  = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the camper who created this delivery or an admin can add items");
        }

        if (delivery.getStatus() != DeliveryStatus.PENDING_ASSIGNMENT) {
            throw new IllegalStateException("Items can only be added to PENDING_ASSIGNMENT deliveries");
        }

        DeliveryItem item = DeliveryItem.builder()
                .delivery(delivery)
                .quantity(request.getQuantity())
                .transactionType(request.getTransactionType())
                .build();

        return mapToResponse(deliveryItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryItemResponse getDeliveryItemById(Long itemId) {
        return mapToResponse(deliveryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryItem", itemId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryItemResponse> getItemsByDelivery(Long deliveryId) {
        return deliveryItemRepository.findByDeliveryId(deliveryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public DeliveryItemResponse updateDeliveryItem(Long itemId, DeliveryItemRequest request, User requester) {
        DeliveryItem item = deliveryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryItem", itemId));

        boolean isOwner = item.getDelivery().getCamper().getId().equals(requester.getId());
        boolean isAdmin  = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the camper who created this delivery or an admin can modify items");
        }

        if (item.getDelivery().getStatus() != DeliveryStatus.PENDING_ASSIGNMENT) {
            throw new IllegalStateException("Items can only be modified on PENDING_ASSIGNMENT deliveries");
        }

        item.setQuantity(request.getQuantity());
        item.setTransactionType(request.getTransactionType());

        return mapToResponse(deliveryItemRepository.save(item));
    }

    @Override
    public void removeDeliveryItem(Long itemId, User requester) {
        DeliveryItem item = deliveryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryItem", itemId));

        boolean isOwner = item.getDelivery().getCamper().getId().equals(requester.getId());
        boolean isAdmin  = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the camper who created this delivery or an admin can remove items");
        }

        if (item.getDelivery().getStatus() != DeliveryStatus.PENDING_ASSIGNMENT) {
            throw new IllegalStateException("Items can only be removed from PENDING_ASSIGNMENT deliveries");
        }

        deliveryItemRepository.deleteById(itemId);
    }

    private DeliveryItemResponse mapToResponse(DeliveryItem item) {
        DeliveryItemResponse r = new DeliveryItemResponse();
        r.setId(item.getId());
        r.setQuantity(item.getQuantity());
        r.setTransactionType(item.getTransactionType());
        r.setDeliveryId(item.getDelivery().getId());
        r.setCreatedAt(item.getCreatedAt());
        return r;
    }
}

