// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsitepayment;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentMethod;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CampsitePaymentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String transactionId;
    private String referenceCode;
    private CampsitePaymentMethod method;
    private CampsitePaymentStatus status;
    private LocalDateTime paidAt;
}
