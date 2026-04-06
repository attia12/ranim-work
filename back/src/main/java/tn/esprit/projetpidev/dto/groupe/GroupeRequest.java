package tn.esprit.projetpidev.dto.groupe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupeRequest {

    // Group name - required
    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    // Is the group private ?
    @NotNull(message = "Privacy status is required")
    private boolean isPrivate;
}