// Module: Official Campsite & Booking | Layer: Service Unit Test
package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.campsite.CampsiteRequest;
import tn.esprit.projetpidev.dto.campsite.CampsiteResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICampsiteServiceImplTest {

    @Mock
    private CampsiteRepository campsiteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ICampsiteServiceImpl campsiteService;

    private User ownerUser;
    private User adminUser;
    private User camperUser;
    private Campsite campsite;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id(1L).firstName("Alice").lastName("Owner")
                .email("alice@test.com").role(Role.COMPSITEOWNERS).enabled(true).build();

        adminUser = User.builder().id(2L).firstName("Bob").lastName("Admin")
                .email("bob@test.com").role(Role.ADMIN).enabled(true).build();

        camperUser = User.builder().id(3L).firstName("Charlie").lastName("Camper")
                .email("charlie@test.com").role(Role.COMPERS).enabled(true).build();

        campsite = Campsite.builder()
                .id(10L).name("Test Campsite").country("Tunisia").city("Tunis")
                .capacity(20).type(CampsiteType.OFFICIAL)
                .pricePerNight(new BigDecimal("50.00"))
                .status(CampsiteStatus.ACTIVE).owner(ownerUser)
                .build();
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_ownerRole_success() {
        CampsiteRequest request = buildRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(campsiteRepository.save(any())).thenReturn(campsite);

        CampsiteResponse response = campsiteService.create(request, 1L);

        assertThat(response.getId()).isEqualTo(10L);
        verify(campsiteRepository).save(any(Campsite.class));
    }

    @Test
    void create_camperRole_throwsException() {
        CampsiteRequest request = buildRequest();
        when(userRepository.findById(3L)).thenReturn(Optional.of(camperUser));

        assertThatThrownBy(() -> campsiteService.create(request, 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only OWNER or ADMIN");
    }

    @Test
    void create_userNotFound_throwsException() {
        CampsiteRequest request = buildRequest();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campsiteService.create(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_byOwner_success() {
        CampsiteRequest request = buildRequest();
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(campsiteRepository.save(any())).thenReturn(campsite);

        CampsiteResponse response = campsiteService.update(10L, request, 1L);

        assertThat(response).isNotNull();
    }

    @Test
    void update_byWrongOwner_throwsException() {
        User anotherOwner = User.builder().id(5L).role(Role.COMPSITEOWNERS).enabled(true)
                .firstName("X").lastName("Y").email("x@y.com").build();
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(5L)).thenReturn(Optional.of(anotherOwner));

        assertThatThrownBy(() -> campsiteService.update(10L, buildRequest(), 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("do not own");
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_byAdmin_softDeletes() {
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(campsiteRepository.save(any())).thenReturn(campsite);

        campsiteService.delete(10L, 2L);

        assertThat(campsite.getStatus()).isEqualTo(CampsiteStatus.DELETED);
        verify(campsiteRepository).save(campsite);
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));

        CampsiteResponse response = campsiteService.getById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Test Campsite");
    }

    @Test
    void getById_notFound_throwsException() {
        when(campsiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campsiteService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── suspend / activate ───────────────────────────────────────────────────

    @Test
    void suspend_changesStatusToSuspended() {
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(campsiteRepository.save(any())).thenReturn(campsite);

        campsiteService.suspend(10L);

        assertThat(campsite.getStatus()).isEqualTo(CampsiteStatus.SUSPENDED);
    }

    @Test
    void activate_changesStatusToActive() {
        campsite.setStatus(CampsiteStatus.SUSPENDED);
        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(campsiteRepository.save(any())).thenReturn(campsite);

        campsiteService.activate(10L);

        assertThat(campsite.getStatus()).isEqualTo(CampsiteStatus.ACTIVE);
    }

    // ── getByOwner ────────────────────────────────────────────────────────────

    @Test
    void getByOwner_returnsPaginatedPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(campsiteRepository.findByOwner_Id(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(campsite)));

        Page<CampsiteResponse> page = campsiteService.getByOwner(1L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CampsiteRequest buildRequest() {
        CampsiteRequest r = new CampsiteRequest();
        r.setName("Test Campsite");
        r.setCountry("Tunisia");
        r.setCity("Tunis");
        r.setCapacity(20);
        r.setType(CampsiteType.OFFICIAL);
        r.setPricePerNight(new BigDecimal("50.00"));
        return r;
    }
}
