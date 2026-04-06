package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Message;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Get all messages between two users
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    // Get all messages received by a user
    List<Message> findByReceiverId(Long receiverId);
    // Get all messages sent by a user
    List<Message> findBySenderId(Long senderId);
    // Get unread messages for a user
    List<Message> findByReceiverIdAndIsRead(Long receiverId, boolean isRead);
}