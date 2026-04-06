package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.orderItem.OrderItemRequest;
import tn.esprit.projetpidev.dto.orderItem.OrderItemResponse;
import tn.esprit.projetpidev.services.IOrderItemService;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/order-items")
@RequiredArgsConstructor
@Tag(name = " Order Items", description = "Add, update and remove items from orders")
public class OrderItemController {

    private final IOrderItemService orderItemService;

    @GetMapping("/{itemId}")
    public ResponseEntity<OrderItemResponse> getById(@PathVariable Long itemId) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(itemId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrder(orderId));
    }

    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','EQUIPEMENTPROVIEDERS')")
    public ResponseEntity<List<OrderItemResponse>> getByEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(orderItemService.getItemsByEquipment(equipmentId));
    }

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('COMPERS')")
    public ResponseEntity<OrderItemResponse> addToOrder(@PathVariable Long orderId,
                                                         @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderItemService.addItemToOrder(orderId, request));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('COMPERS')")
    public ResponseEntity<OrderItemResponse> update(@PathVariable Long itemId,
                                                     @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderItemService.updateOrderItem(itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('COMPERS','ADMIN')")
    public ResponseEntity<String> remove(@PathVariable Long itemId) {
        orderItemService.removeItemFromOrder(itemId);
        return ResponseEntity.ok("Order item removed successfully!");
    }
}

