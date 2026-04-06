package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.BlogPost;
import tn.esprit.projetpidev.dto.blogpost.BlogPostRequest;
import tn.esprit.projetpidev.dto.blogpost.BlogPostResponse;
import tn.esprit.projetpidev.services.IBlogPostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class BlogPostController {

    private final IBlogPostService blogPostService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<BlogPostResponse> createPost(
            @Valid @RequestBody BlogPostRequest request) {
        return ResponseEntity.ok(blogPostService.createPost(request));
    }

    @GetMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlogPostResponse> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(blogPostService.getPostById(postId));
    }

    @GetMapping
    public ResponseEntity<List<BlogPostResponse>> getAllPosts() {
        return ResponseEntity.ok(blogPostService.getAllPosts());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BlogPostResponse>> getPostsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(blogPostService.getPostsByUser(userId));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BlogPostResponse>> getPostsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(blogPostService.getPostsByCategory(categoryId));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BlogPostResponse>> searchPosts(@RequestParam String keyword) {
        return ResponseEntity.ok(blogPostService.searchPosts(keyword));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<BlogPostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody BlogPostRequest request) {
        return ResponseEntity.ok(blogPostService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        blogPostService.deletePost(postId);
        return ResponseEntity.ok("Post deleted successfully !");
    }

    @PutMapping("/{postId}/assign-category/{categoryId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<BlogPost> assignCategoryToPost(
            @PathVariable Long postId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(blogPostService.assignCategoryToPost(postId, categoryId));
    }

    @PutMapping("/{postId}/remove-category")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<BlogPost> removeCategoryFromPost(@PathVariable Long postId) {
        return ResponseEntity.ok(blogPostService.removeCategoryFromPost(postId));
    }
}