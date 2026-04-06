package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.coupon.CouponRequest;
import tn.esprit.projetpidev.dto.coupon.CouponResponse;
import tn.esprit.projetpidev.domain.Coupon;

import java.util.List;

public interface ICouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse getCouponById(Long id);
    List<CouponResponse> getAllCoupons();
    CouponResponse updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
    Coupon validateCoupon(String code, Float orderAmount);
}

