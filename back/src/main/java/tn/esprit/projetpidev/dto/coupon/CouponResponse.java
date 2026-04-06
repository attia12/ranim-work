package tn.esprit.projetpidev.dto.coupon;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.DiscountType;

import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private Float discountValue;
    private Float minOrderAmount;
    private Integer maxUsageCount;
    private Integer currentUsageCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean isActive;
}

