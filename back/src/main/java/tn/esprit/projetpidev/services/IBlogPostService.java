package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.BlogPost;
import tn.esprit.projetpidev.dto.blogpost.BlogPostRequest;
import tn.esprit.projetpidev.dto.blogpost.BlogPostResponse;
import java.util.List;

public interface IBlogPostService {
    BlogPostResponse createPost(BlogPostRequest request);
    BlogPostResponse getPostById(Long postId);
    List<BlogPostResponse> getAllPosts();
    List<BlogPostResponse> getPostsByUser(Long userId);
    List<BlogPostResponse> getPostsByCategory(Long categoryId);
    List<BlogPostResponse> searchPosts(String keyword);
    BlogPostResponse updatePost(Long postId, BlogPostRequest request);
    void deletePost(Long postId);
    BlogPost assignCategoryToPost(Long postId, Long categoryId);
    BlogPost removeCategoryFromPost(Long postId);
}