package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.BlogPost;
import tn.esprit.projetpidev.domain.Like;
import tn.esprit.projetpidev.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    // Get all likes for a post
    List<Like> findByBlogPostPostId(Long postId);
    // Get all likes by a user
    List<Like> findByUserId(Long userId);
    // Check if user already liked a post
    boolean existsByUserIdAndBlogPostPostId(Long userId, Long postId);
    // Find like by user and post
    Optional<Like> findByUserIdAndBlogPostPostId(Long userId, Long postId);
    // Count likes for a post
    int countByBlogPostPostId(Long postId);

    boolean existsByUserAndBlogPost(User user, BlogPost post);
}