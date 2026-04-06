package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.blogpost.BlogPostRequest;
import tn.esprit.projetpidev.dto.blogpost.BlogPostResponse;
import tn.esprit.projetpidev.repositories.*;

import java.util.List;

@Service
@Transactional          // ← ajouter
@RequiredArgsConstructor
public class IBlogPostServiceImpl implements IBlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepo;

    @Override
    public BlogPostResponse createPost(BlogPostRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(category)
                .user(user)
                .build();

        return mapToResponse(blogPostRepository.save(post));
    }

    @Override
    public BlogPostResponse getPostById(Long postId) {
        return mapToResponse(blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found")));
    }

    @Override
    public List<BlogPostResponse> getAllPosts() {
        return blogPostRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BlogPostResponse> getPostsByUser(Long userId) {
        return blogPostRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BlogPostResponse> getPostsByCategory(Long categoryId) {
        return blogPostRepository.findByCategoryCategoryId(categoryId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BlogPostResponse> searchPosts(String keyword) {
        return blogPostRepository.findByTitleContaining(keyword)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public BlogPostResponse updatePost(Long postId, BlogPostRequest request) {
        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(category);

        return mapToResponse(blogPostRepository.save(post));
    }

    @Override
    public void deletePost(Long postId) {
        blogPostRepository.deleteById(postId);
    }

    private BlogPostResponse mapToResponse(BlogPost post) {
        BlogPostResponse response = new BlogPostResponse();
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCategoryName(post.getCategory().getName());
        response.setAuthorName(post.getUser().getFullname());
        response.setTotalComments(post.getComments() != null ? post.getComments().size() : 0);
        response.setTotalLikes(post.getLikes() != null ? post.getLikes().size() : 0);
        return response;
    }
    @Override
    public BlogPost assignCategoryToPost(Long postId, Long categoryId) {
        BlogPost post = blogPostRepository.findById(postId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (post == null || category == null) return null;
        post.setCategory(category);
        return blogPostRepository.save(post);
    }

    @Override
    public BlogPost removeCategoryFromPost(Long postId) {
        BlogPost post = blogPostRepository.findById(postId).orElse(null);
        if (post == null) return null;
        post.setCategory(null);
        return blogPostRepository.save(post);
    }
}