package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.Comment;
import tn.esprit.projetpidev.dto.comment.CommentRequest;
import tn.esprit.projetpidev.dto.comment.CommentResponse;
import tn.esprit.projetpidev.services.ICommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<CommentResponse> addComment(
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(request));
    }

    @GetMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted");
    }

    @PutMapping("/{commentId}/assign-post/{postId}")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPERS', 'ROLE_ADMIN')")
    public ResponseEntity<Comment> assignCommentToPost(
            @PathVariable Long commentId,
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.assignCommentToPost(commentId, postId));
    }

    @PutMapping("/{commentId}/remove-post")
    @PreAuthorize("hasAnyAuthority('ROLE_COMPERS', 'ROLE_ADMIN')")
    public ResponseEntity<Comment> removeCommentFromPost(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.removeCommentFromPost(commentId));
    }
}