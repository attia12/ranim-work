package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.like.LikeRequest;
import tn.esprit.projetpidev.dto.like.LikeResponse;
import tn.esprit.projetpidev.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ILikeServiceImpl implements ILikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepo;
    private final BlogPostRepository blogPostRepository;

    // Like a post
    @Override
    public LikeResponse likePost(LikeRequest request) {
        // Check if user already liked this post
        if (likeRepository.existsByUserIdAndBlogPostPostId(
                request.getUserId(), request.getPostId())) {
            throw new RuntimeException("User already liked this post");
        }

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BlogPost post = blogPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Like like = Like.builder()
                .user(user)
                .blogPost(post)
                .build();

        return mapToResponse(likeRepository.save(like));
    }


    @Override
    public void unlikePost(Long userId, Long postId) {
        Like like = likeRepository.findByUserIdAndBlogPostPostId(userId, postId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        likeRepository.delete(like);
    }


    @Override
    public List<LikeResponse> getLikesByPost(Long postId) {
        return likeRepository.findByBlogPostPostId(postId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<LikeResponse> getLikesByUser(Long userId) {
        return likeRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).toList();
    }



    private LikeResponse mapToResponse(Like like) {
        LikeResponse response = new LikeResponse();
        response.setLikeId(like.getLikeId());
        response.setUsername(like.getUser().getFullname());
        response.setPostId(like.getBlogPost().getPostId());
        response.setPostTitle(like.getBlogPost().getTitle());
        return response;
    }

}