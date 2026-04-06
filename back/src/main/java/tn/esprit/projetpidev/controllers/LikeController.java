package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.like.LikeRequest;
import tn.esprit.projetpidev.dto.like.LikeResponse;
import tn.esprit.projetpidev.services.ILikeService;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;

    // Like a post
    @PostMapping
    public ResponseEntity<LikeResponse> likePost(
            @Valid @RequestBody LikeRequest request) {
        return ResponseEntity.ok(likeService.likePost(request));
    }

    // Unlike a post
    @DeleteMapping("/{userId}/{postId}")
    public ResponseEntity<String> unlikePost(
            @PathVariable Long userId,
            @PathVariable Long postId) {
        likeService.unlikePost(userId, postId);
        return ResponseEntity.ok("Post unliked successfully !");
    }

    // Get all likes for a post
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<LikeResponse>> getLikesByPost(
            @PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikesByPost(postId));
    }

    // Get all likes by a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikeResponse>> getLikesByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikesByUser(userId));
    }


}