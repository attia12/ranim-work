package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.order.OrderRequest;
import tn.esprit.projetpidev.dto.order.OrderResponse;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.services.IOrderService;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/orders")
@RequiredArgsConstructor
@Tag(name = " Orders", description = "Create and manage marketplace orders")
public class OrderController {

    private final IOrderService orderService;
    private final UserRepository userRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderService.getOrderById(id, user));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderService.getOrdersByUser(user.getId()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','EQUIPEMENTPROVIEDERS')")
    public ResponseEntity<List<OrderResponse>> getByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request,
                                                 Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderService.createOrder(request, user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','EQUIPEMENTPROVIEDERS')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @RequestParam OrderStatus status,
                                                       Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, user));
    }

    @PostMapping("/{id}/coupon")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> applyCoupon(@PathVariable Long id,
                                                      @RequestParam String couponCode,
                                                      Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(orderService.applyCoupon(id, couponCode, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        orderService.deleteOrder(id, user);
        return ResponseEntity.ok("Order deleted successfully!");
    }
}

