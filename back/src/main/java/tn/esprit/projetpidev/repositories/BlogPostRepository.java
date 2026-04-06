package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.BlogPost;
import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    // Get all posts by user
    List<BlogPost> findByUserId(Long userId);
    // Get all posts by category
    List<BlogPost> findByCategoryCategoryId(Long categoryId);
    // Search posts by title
    List<BlogPost> findByTitleContaining(String keyword);
}