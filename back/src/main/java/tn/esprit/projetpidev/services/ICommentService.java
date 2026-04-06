package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.Comment;
import tn.esprit.projetpidev.dto.comment.CommentRequest;
import tn.esprit.projetpidev.dto.comment.CommentResponse;
import java.util.List;

public interface ICommentService {
    CommentResponse addComment(CommentRequest request);
    CommentResponse getCommentById(Long commentId);
    List<CommentResponse> getCommentsByPost(Long postId);
    List<CommentResponse> getCommentsByUser(Long userId);
    CommentResponse updateComment(Long commentId, CommentRequest request);
    void deleteComment(Long commentId);
    Comment assignCommentToPost(Long commentId, Long postId);
    Comment removeCommentFromPost(Long commentId);
}