// Module: Outdoor Campsite & Booking | Layer: DTO
package tn.esprit.projetpidev.dto.outdoorcampsite;

import lombok.Data;

@Data
public class ModerationRequest {
    /** APPROVE or REJECT */
    private String action;
    private String adminNote;
}
