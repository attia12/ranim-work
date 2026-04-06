package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryRequest;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryResponse;
import tn.esprit.projetpidev.services.IEquipmentCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/categories")
@RequiredArgsConstructor
@Tag(name = " Equipment Categories", description = "Manage equipment categories and subcategories")
public class EquipmentCategoryController {

    private final IEquipmentCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<EquipmentCategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentCategoryResponse> create(@Valid @RequestBody EquipmentCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentCategoryResponse> update(@PathVariable Long id,
            @Valid @RequestBody EquipmentCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully!");
    }
}
