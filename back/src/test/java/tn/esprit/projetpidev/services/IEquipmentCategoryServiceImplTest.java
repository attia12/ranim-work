package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.EquipmentCategory;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryRequest;
import tn.esprit.projetpidev.dto.equipmentcategory.EquipmentCategoryResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EquipmentCategoryRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IEquipmentCategoryServiceImplTest {

    @Mock
    private EquipmentCategoryRepository categoryRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private IEquipmentCategoryServiceImpl categoryService;

    private EquipmentCategoryRequest request;
    private EquipmentCategory category;

    @BeforeEach
    void setUp() {
        request = new EquipmentCategoryRequest();
        request.setName("Tents");
        request.setDescription("All kinds of tents");

        category = EquipmentCategory.builder()
                .id(1L)
                .name("Tents")
                .description("All kinds of tents")
                .build();
    }

    @Test
    void testCreateCategory_Success() {
        when(categoryRepository.existsByName(request.getName())).thenReturn(false);
        when(categoryRepository.save(any(EquipmentCategory.class))).thenReturn(category);

        EquipmentCategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Tents", response.getName());
        verify(categoryRepository, times(1)).save(any(EquipmentCategory.class));
    }

    @Test
    void testCreateCategory_ThrowsWhenExists() {
        when(categoryRepository.existsByName(request.getName())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            categoryService.createCategory(request);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(categoryRepository, never()).save(any(EquipmentCategory.class));
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(equipmentRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCategory_ThrowsWhenHasEquipments() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(equipmentRepository.existsByCategoryId(1L)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory(1L);
        });

        assertTrue(exception.getMessage().contains("has associated equipment"));
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}

