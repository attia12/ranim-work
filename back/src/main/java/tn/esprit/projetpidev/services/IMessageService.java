package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.message.MessageRequest;
import tn.esprit.projetpidev.dto.message.MessageResponse;

import java.util.List;

public interface IMessageService {
    MessageResponse sendMessage(MessageRequest request);
    MessageResponse getMessageById(Long messageId);
    List<MessageResponse> getConversation(Long senderId, Long receiverId);
    List<MessageResponse> getReceivedMessages(Long userId);
    List<MessageResponse> getSentMessages(Long userId);
    List<MessageResponse> getUnreadMessages(Long userId);
    void markAsRead(Long messageId);
    void deleteMessage(Long messageId);
    MessageResponse updateMessage(Long messageId, String newContent); // ✅ Ajouté
}