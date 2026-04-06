package tn.esprit.projetpidev.dto.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    /** ID of the User whose role is GUIDE */
    @NotNull(message = "Guide user ID is required")
    private Long guideUserId;

    @NotBlank(message = "Role description is required")
    private String roleDescription;

    private String status;
}
