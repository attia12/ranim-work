package tn.esprit.projetpidev.services;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.comment.CommentRequest;
import tn.esprit.projetpidev.dto.comment.CommentResponse;
import tn.esprit.projetpidev.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ICommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepo;

    @Override
    public CommentResponse addComment(CommentRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BlogPost post = blogPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .timeComment(LocalDateTime.now())
                .blogPost(post)
                .build();

        Comment savedComment = commentRepository.save(comment);
        user.getComments().add(savedComment);
        userRepo.save(user);

        return mapToResponse(savedComment, user.getFullname());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        // ✅ Chercher le vrai auteur
        String authorName = findAuthorName(commentId);
        return mapToResponse(comment, authorName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByBlogPostPostId(postId)
                .stream()
                .map(c -> {
                    // ✅ Chercher le vrai auteur pour chaque commentaire
                    String authorName = findAuthorName(c.getCommentId());
                    return mapToResponse(c, authorName);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getComments()
                .stream()
                .map(c -> mapToResponse(c, user.getFullname()))
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setContent(request.getContent());
        comment.setTimeComment(LocalDateTime.now());
        String authorName = findAuthorName(commentId);
        return mapToResponse(commentRepository.save(comment), authorName);
    }

    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // ✅ Chercher le vrai nom de l'auteur via la relation User → Comments
    private String findAuthorName(Long commentId) {
        return userRepo.findAll().stream()
                .filter(u -> u.getComments() != null &&
                        u.getComments().stream()
                                .anyMatch(c -> c.getCommentId().equals(commentId)))
                .map(User::getFullname)
                .findFirst()
                .orElse("Unknown");
    }

    private CommentResponse mapToResponse(Comment comment, String authorName) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setContent(comment.getContent());
        response.setTimeComment(comment.getTimeComment());
        response.setAuthorName(authorName);
        response.setPostId(comment.getBlogPost().getPostId());
        return response;
    }

    @Override
    public Comment assignCommentToPost(Long commentId, Long postId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        BlogPost post = blogPostRepository.findById(postId).orElse(null);
        if (comment == null || post == null) return null;
        comment.setBlogPost(post);
        return commentRepository.save(comment);
    }

    @Override
    public Comment removeCommentFromPost(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return null;
        comment.setBlogPost(null);
        return commentRepository.save(comment);
    }
}