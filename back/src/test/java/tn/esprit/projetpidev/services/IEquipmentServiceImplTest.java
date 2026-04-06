package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.*;
import tn.esprit.projetpidev.dto.equipment.EquipmentRequest;
import tn.esprit.projetpidev.dto.equipment.EquipmentResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.EquipmentCategoryRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.WarehouseRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IEquipmentServiceImplTest {

    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentCategoryRepository categoryRepository;
    @Mock private WarehouseRepository warehouseRepository;

    @InjectMocks
    private IEquipmentServiceImpl equipmentService;

    private User provider;
    private User normalUser;
    private EquipmentCategory category;
    private EquipmentRequest request;
    private Equipment savedEquipment;

    @BeforeEach
    void setUp() {
        provider = new User();
        provider.setId(1L);
        provider.setRole(Role.EQUIPEMENTPROVIEDERS);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole(Role.COMPERS);

        category = EquipmentCategory.builder().id(10L).name("Tents").build();

        request = new EquipmentRequest();
        request.setName("Camping Tent");
        request.setCategoryId(10L);
        request.setPricePerDay(15.0f);
        request.setAvailableForRent(true);

        savedEquipment = Equipment.builder()
                .id(100L)
                .name(request.getName())
                .pricePerDay(request.getPricePerDay())
                .category(category)
                .owner(provider)
                .build();
    }

    @Test
    void testCreateEquipment_Success() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(savedEquipment);

        EquipmentResponse response = equipmentService.createEquipment(request, provider);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Camping Tent", response.getName());
        assertEquals(10L, response.getCategoryId());
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    void testCreateEquipment_ThrowsWhenNotProvider() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            equipmentService.createEquipment(request, normalUser);
        });
        
        assertEquals("Only equipment providers can list equipment", exception.getMessage());
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void testCreateEquipment_ThrowsWhenCategoryNotFound() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            equipmentService.createEquipment(request, provider);
        });
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }
}

