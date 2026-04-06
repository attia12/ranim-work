package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.coupon.CouponRequest;
import tn.esprit.projetpidev.dto.coupon.CouponResponse;
import tn.esprit.projetpidev.services.ICouponService;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/coupons")
@RequiredArgsConstructor
@Tag(name = " Coupons", description = "Manage discount coupons (ADMIN only)")
public class CouponController {

    private final ICouponService couponService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.updateCoupon(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("Coupon deleted successfully!");
    }
}

