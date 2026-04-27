package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.dto.notification.NotificationPayload;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to a specific user identified by their numeric ID.
     * The message is broadcast to /topic/notif-{userId} which only that user subscribes to.
     */
    public void sendToUser(Long userId, NotificationPayload payload) {
        try {
            messagingTemplate.convertAndSend("/topic/notif-" + userId, payload);
            log.info("WS notification sent to userId={}: type={}", userId, payload.getType());
        } catch (Exception e) {
            log.warn("Failed to send WS notification to userId={}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast a notification to all subscribers of a topic.
     * The message is delivered to /topic/{topic}.
     */
    public void sendBroadcast(String topic, NotificationPayload payload) {
        try {
            messagingTemplate.convertAndSend("/topic/" + topic, payload);
            log.debug("WS broadcast sent to /topic/{}: type={}", topic, payload.getType());
        } catch (Exception e) {
            log.warn("Failed to send WS broadcast to /topic/{}: {}", topic, e.getMessage());
        }
    }
}
