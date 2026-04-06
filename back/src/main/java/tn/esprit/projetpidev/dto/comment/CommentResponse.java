package tn.esprit.projetpidev.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long commentId;
    private String content;
    private LocalDateTime timeComment;
    private String authorName;
    private Long postId;
}