package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.category.CategoryRequest;
import tn.esprit.projetpidev.dto.category.CategoryResponse;
import java.util.List;

public interface ICategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long categoryId);
    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(Long categoryId, CategoryRequest request);
    void deleteCategory(Long categoryId);
}