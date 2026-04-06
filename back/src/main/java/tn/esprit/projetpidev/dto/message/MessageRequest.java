package tn.esprit.projetpidev.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageRequest {


    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String content;


    @NotNull(message = "Sender ID is required")
    private Long senderId;


    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
}