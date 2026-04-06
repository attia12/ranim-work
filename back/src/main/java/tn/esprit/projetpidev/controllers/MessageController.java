package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.message.MessageRequest;
import tn.esprit.projetpidev.dto.message.MessageResponse;
import tn.esprit.projetpidev.services.IMessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @GetMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping("/conversation/{senderId}/{receiverId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(messageService.getConversation(senderId, receiverId));
    }

    @GetMapping("/received/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getReceivedMessages(
            @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getReceivedMessages(userId));
    }

    @GetMapping("/sent/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getSentMessages(
            @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getSentMessages(userId));
    }

    @GetMapping("/unread/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages(
            @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getUnreadMessages(userId));
    }

    @PutMapping("/read/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ResponseEntity.ok("Message marked as read !");
    }

    // ✅ PATCH pour éviter conflit avec PUT /read/{messageId}
    @PatchMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body) {
        String newContent = body.get("content");
        return ResponseEntity.ok(messageService.updateMessage(messageId, newContent));
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok("Message deleted successfully !");
    }
}