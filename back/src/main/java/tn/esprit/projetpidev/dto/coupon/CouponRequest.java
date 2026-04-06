package tn.esprit.projetpidev.dto.coupon;

import jakarta.validation.constraints.*;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.DiscountType;

import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private Float discountValue;

    private Float minOrderAmount;

    @Min(value = 1, message = "Max usage count must be at least 1")
    private Integer maxUsageCount;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;

    private Boolean isActive = true;

    @AssertTrue(message = "validUntil must be after validFrom")
    public boolean isDateRangeValid() {
        return validFrom == null || validUntil == null || validUntil.isAfter(validFrom);
    }
}

