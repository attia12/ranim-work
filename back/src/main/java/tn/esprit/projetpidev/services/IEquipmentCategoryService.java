package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryRequest;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryResponse;

import java.util.List;

public interface IEquipmentCategoryService {
    EquipmentCategoryResponse createCategory(EquipmentCategoryRequest request);
    EquipmentCategoryResponse getCategoryById(Long id);
    List<EquipmentCategoryResponse> getAllCategories();
    EquipmentCategoryResponse updateCategory(Long id, EquipmentCategoryRequest request);
    void deleteCategory(Long id);
}

