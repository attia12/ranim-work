// Module: Outdoor Campsite & Booking | Layer: Service Unit Test
package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.OutdoorCampsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.AccessDifficulty;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.outdoorcampsite.ModerationRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.OutdoorCampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IOutdoorCampsiteServiceImplTest {

    @Mock private OutdoorCampsiteRepository outdoorCampsiteRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private WsNotificationService wsNotificationService;

    @InjectMocks
    private IOutdoorCampsiteServiceImpl service;

    private User camper;
    private User admin;
    private OutdoorCampsite site;

    @BeforeEach
    void setUp() {
        camper = User.builder().id(1L).firstName("Alice").lastName("C")
                .email("alice@test.com").role(Role.COMPERS).enabled(true).build();

        admin = User.builder().id(2L).firstName("Bob").lastName("A")
                .email("bob@test.com").role(Role.ADMIN).enabled(true).build();

        site = OutdoorCampsite.builder()
                .id(10L).name("Forest Trail")
                .country("TN").city("Bizerte")
                .accessDifficulty(AccessDifficulty.EASY)
                .status(OutdoorCampsiteStatus.PENDING)
                .proposedBy(camper)
                .build();
    }

    @Test
    void propose_validRequest_returnsPending() {
        OutdoorCampsiteRequest req = buildRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));
        when(outdoorCampsiteRepository.save(any())).thenReturn(site);

        OutdoorCampsiteResponse response = service.propose(req, 1L);

        assertThat(response.getStatus()).isEqualTo(OutdoorCampsiteStatus.PENDING);
    }

    @Test
    void propose_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.propose(buildRequest(), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void moderate_approve_changesStatusAndSendsEmail() {
        when(outdoorCampsiteRepository.findById(10L)).thenReturn(Optional.of(site));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(outdoorCampsiteRepository.save(any())).thenReturn(site);
        doNothing().when(emailService).sendOutdoorCampsiteApprovalEmail(
                any(), any(), any(), anyBoolean(), any());
        doNothing().when(wsNotificationService).sendToUser(any(), any());

        ModerationRequest req = new ModerationRequest();
        req.setAction("APPROVE");
        req.setAdminNote("Looks good!");

        OutdoorCampsiteResponse response = service.moderate(10L, req, 2L);

        assertThat(response.getStatus()).isEqualTo(OutdoorCampsiteStatus.APPROVED);
        verify(emailService).sendOutdoorCampsiteApprovalEmail(any(), any(), any(), eq(true), any());
        verify(wsNotificationService).sendToUser(any(), any());
    }

    @Test
    void moderate_reject_changesStatusAndSendsEmail() {
        when(outdoorCampsiteRepository.findById(10L)).thenReturn(Optional.of(site));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(outdoorCampsiteRepository.save(any())).thenReturn(site);
        doNothing().when(emailService).sendOutdoorCampsiteApprovalEmail(
                any(), any(), any(), anyBoolean(), any());
        doNothing().when(wsNotificationService).sendToUser(any(), any());

        ModerationRequest req = new ModerationRequest();
        req.setAction("REJECT");
        req.setAdminNote("Not suitable.");

        OutdoorCampsiteResponse response = service.moderate(10L, req, 2L);

        assertThat(response.getStatus()).isEqualTo(OutdoorCampsiteStatus.REJECTED);
        verify(emailService).sendOutdoorCampsiteApprovalEmail(any(), any(), any(), eq(false), any());
        verify(wsNotificationService).sendToUser(any(), any());
    }

    @Test
    void moderate_unknownAction_throwsException() {
        when(outdoorCampsiteRepository.findById(10L)).thenReturn(Optional.of(site));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        ModerationRequest req = new ModerationRequest();
        req.setAction("UNKNOWN");

        assertThatThrownBy(() -> service.moderate(10L, req, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown moderation action");
    }

    @Test
    void update_byProposer_pendingStatus_success() {
        when(outdoorCampsiteRepository.findById(10L)).thenReturn(Optional.of(site));
        when(outdoorCampsiteRepository.save(any())).thenReturn(site);

        OutdoorCampsiteResponse response = service.update(10L, buildRequest(), 1L);

        assertThat(response).isNotNull();
    }

    @Test
    void update_byNonProposer_throwsException() {
        when(outdoorCampsiteRepository.findById(10L)).thenReturn(Optional.of(site));

        assertThatThrownBy(() -> service.update(10L, buildRequest(), 99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the proposer");
    }

    @Test
    void getById_notFound_throwsException() {
        when(outdoorCampsiteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private OutdoorCampsiteRequest buildRequest() {
        OutdoorCampsiteRequest r = new OutdoorCampsiteRequest();
        r.setName("Forest Trail");
        r.setCountry("TN");
        r.setCity("Bizerte");
        r.setAccessDifficulty(AccessDifficulty.EASY);
        return r;
    }
}
