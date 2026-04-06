package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.message.MessageRequest;
import tn.esprit.projetpidev.dto.message.MessageResponse;
import tn.esprit.projetpidev.repositories.*;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IMessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepo;
    private final LikeRepository likeRepository;
    private final BlogPostRepository blogPostRepository;

    @Override
    public MessageResponse sendMessage(MessageRequest request) {
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new RuntimeException("Cannot send message to yourself");
        }
        User sender = userRepo.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepo.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Message message = Message.builder()
                .content(request.getContent())
                .sentAt(new Date())
                .isRead(false)
                .sender(sender)
                .receiver(receiver)
                .build();
        return mapToResponse(messageRepository.save(message));
    }

    @Override
    public MessageResponse getMessageById(Long messageId) {
        return mapToResponse(messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found")));
    }

    @Override
    public List<MessageResponse> getConversation(Long senderId, Long receiverId) {
        List<Message> messages = messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        messages.addAll(messageRepository.findBySenderIdAndReceiverId(receiverId, senderId));
        messages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
        return messages.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MessageResponse> getReceivedMessages(Long userId) {
        return messageRepository.findByReceiverId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MessageResponse> getSentMessages(Long userId) {
        return messageRepository.findBySenderId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<MessageResponse> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsRead(userId, false)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setRead(true);
        messageRepository.save(message);
    }

    @Override
    public void deleteMessage(Long messageId) {
        try {
            messageRepository.deleteById(messageId);
        } catch (Exception e) {
            // ✅ Log pour voir l'erreur exacte
            System.out.println("❌ Error deleting message: " + e.getMessage());
            throw e;
        }
    }

    // ✅ Update message content
    @Override
    public MessageResponse updateMessage(Long messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setContent(newContent);
        return mapToResponse(messageRepository.save(message));
    }

    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setMessageId(message.getMessageId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setRead(message.isRead());
        response.setSenderId(message.getSender().getUserId());
        response.setReceiverId(message.getReceiver().getUserId());
        response.setSenderName(message.getSender().getFullname());
        response.setReceiverName(message.getReceiver().getFullname());
        return response;
    }
}