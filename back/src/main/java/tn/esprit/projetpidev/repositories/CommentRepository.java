package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Get all comments for a post
    List<Comment> findByBlogPostPostId(Long postId);
    // No findByUserId since unidirectional
}