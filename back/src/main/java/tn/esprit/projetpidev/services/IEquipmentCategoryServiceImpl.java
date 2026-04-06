package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryRequest;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryResponse;
import tn.esprit.projetpidev.domain.EquipmentCategory;
import tn.esprit.projetpidev.repositories.EquipmentCategoryRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IEquipmentCategoryServiceImpl implements IEquipmentCategoryService {

    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public EquipmentCategoryResponse createCategory(EquipmentCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Category with name '" + request.getName() + "' already exists");
        }

        EquipmentCategory category = EquipmentCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .build();

        if (request.getParentId() != null) {
            EquipmentCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", request.getParentId()));
            category.setParent(parent);
        }

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentCategoryResponse getCategoryById(Long id) {
        return mapToResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EquipmentCategoryResponse updateCategory(Long id, EquipmentCategoryRequest request) {
        EquipmentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());

        if (request.getParentId() != null) {
            EquipmentCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        EquipmentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (equipmentRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Category has associated equipment and cannot be deleted");
        }
        categoryRepository.deleteById(id);
    }

    private EquipmentCategoryResponse mapToResponse(EquipmentCategory category) {
        EquipmentCategoryResponse response = new EquipmentCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIcon(category.getIcon());
        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }
        response.setChildCount(category.getChildren() != null ? category.getChildren().size() : 0);
        return response;
    }
}

