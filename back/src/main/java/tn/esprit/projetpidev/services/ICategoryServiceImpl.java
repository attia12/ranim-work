package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.category.CategoryRequest;
import tn.esprit.projetpidev.dto.category.CategoryResponse;
import tn.esprit.projetpidev.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ICategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    // Create a new category
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    // Get category by ID
    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
        return mapToResponse(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found")));
    }

    // Get all categories
    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    // Update a category
    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return mapToResponse(categoryRepository.save(category));
    }

    // Delete a category
    @Override
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    // Map Category to CategoryResponse
    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setTotalPosts(
                category.getBlogPosts() != null ? category.getBlogPosts().size() : 0
        );
        return response;
    }
}