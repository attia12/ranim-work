// Module: Official Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.campsite;

import lombok.Data;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;

import java.time.LocalDateTime;

@Data
public class CampsiteStatusHistoryResponse {
    private Long id;
    private CampsiteStatus previousStatus;
    private CampsiteStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;
}
