package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.enums.VehicleType;
import tn.esprit.projetpidev.dto.vehicle.VehicleRequest;
import tn.esprit.projetpidev.dto.vehicle.VehicleResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.VehicleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IVehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;

    @InjectMocks
    private IVehicleServiceImpl vehicleService;

    private User agent;
    private VehicleRequest request;

    @BeforeEach
    void setUp() {
        agent = new User();
        agent.setId(1L);
        agent.setRole(Role.DELIVERYAGENT);

        request = new VehicleRequest();
        request.setPlate("123-TN-4567");
        request.setType(VehicleType.VAN);
        request.setBrand("Ford");
        request.setModel("Transit");
        request.setYear(2020);
        request.setServiceRadius(50f);
        request.setMaxConcurrentDeliveries(3);
    }

    @Test
    void testRegisterVehicle_Success() {
        when(vehicleRepository.existsByPlate("123-TN-4567")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(0);
            v.setId(10L);
            return v;
        });

        VehicleResponse response = vehicleService.registerVehicle(request, agent);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("123-TN-4567", response.getPlate());
        assertEquals(VehicleType.VAN, response.getType());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testRegisterVehicle_ThrowsWhenNotAgent() {
        User normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole(Role.COMPERS);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vehicleService.registerVehicle(request, normalUser);
        });

        assertTrue(exception.getMessage().contains("Only delivery agents (DELIVERYAGENT) can register vehicles"));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testRegisterVehicle_ThrowsWhenPlateExists() {
        when(vehicleRepository.existsByPlate("123-TN-4567")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            vehicleService.registerVehicle(request, agent);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}
