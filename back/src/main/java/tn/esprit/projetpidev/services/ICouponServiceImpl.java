package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.coupon.CouponRequest;
import tn.esprit.projetpidev.dto.coupon.CouponResponse;
import tn.esprit.projetpidev.domain.Coupon;
import tn.esprit.projetpidev.repositories.CouponRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ICouponServiceImpl implements ICouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalStateException("Coupon code '" + request.getCode() + "' already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxUsageCount(request.getMaxUsageCount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();

        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        return mapToResponse(couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUsageCount(request.getMaxUsageCount());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);
        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        couponRepository.deleteById(coupon.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon validateCoupon(String code, Float orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Coupon code '" + code + "' not found"));

        if (!coupon.getIsActive()) {
            throw new IllegalStateException("Coupon is not active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new IllegalStateException("Coupon is expired or not yet valid");
        }
        if (coupon.getCurrentUsageCount() >= coupon.getMaxUsageCount()) {
            throw new IllegalStateException("Coupon usage limit has been reached");
        }
        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            throw new IllegalStateException("Order amount does not meet the minimum required for this coupon");
        }
        return coupon;
    }

    private CouponResponse mapToResponse(Coupon c) {
        CouponResponse r = new CouponResponse();
        r.setId(c.getId());
        r.setCode(c.getCode());
        r.setDiscountType(c.getDiscountType());
        r.setDiscountValue(c.getDiscountValue());
        r.setMinOrderAmount(c.getMinOrderAmount());
        r.setMaxUsageCount(c.getMaxUsageCount());
        r.setCurrentUsageCount(c.getCurrentUsageCount());
        r.setValidFrom(c.getValidFrom());
        r.setValidUntil(c.getValidUntil());
        r.setIsActive(c.getIsActive());
        return r;
    }
}

