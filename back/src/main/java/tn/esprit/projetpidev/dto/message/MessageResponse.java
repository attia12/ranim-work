package tn.esprit.projetpidev.dto.message;

import lombok.Data;
import java.util.Date;

@Data
public class MessageResponse {
    private Long messageId;
    private String content;
    private Date sentAt;
    private boolean isRead;
    private Long senderId;      // ✅ Ajouté
    private Long receiverId;    // ✅ Ajouté
    private String senderName;
    private String receiverName;
}