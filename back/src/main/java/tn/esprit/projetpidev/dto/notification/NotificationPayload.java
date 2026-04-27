package tn.esprit.projetpidev.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private String type;
    private String message;
    private Long referenceId;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
