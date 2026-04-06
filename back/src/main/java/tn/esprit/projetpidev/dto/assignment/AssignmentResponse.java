package tn.esprit.projetpidev.dto.assignment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentResponse {

    private Long assignmentId;

    // Event info
    private Long eventId;
    private String eventTitle;
    private String eventDate;
    private String eventLocation;

    // Guide user info (denormalized — same pattern as camperFullName in DeliveryResponse)
    private Long guideUserId;
    private String guideFullName;
    private String guideEmail;

    private String roleDescription;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
