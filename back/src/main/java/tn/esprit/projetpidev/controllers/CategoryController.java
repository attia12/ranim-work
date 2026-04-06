package tn.esprit.projetpidev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.category.CategoryRequest;
import tn.esprit.projetpidev.dto.category.CategoryResponse;
import tn.esprit.projetpidev.services.ICategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('COMPERS', 'ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully !");
    }
}