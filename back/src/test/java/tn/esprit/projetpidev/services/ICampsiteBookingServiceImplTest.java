// Module: Official Campsite & Booking | Layer: Service Unit Test
package tn.esprit.projetpidev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingRequest;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICampsiteBookingServiceImplTest {

    @Mock private CampsiteBookingRepository bookingRepository;
    @Mock private CampsiteRepository campsiteRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private ICampsiteBookingServiceImpl bookingService;

    private User camper;
    private User admin;
    private Campsite campsite;

    @BeforeEach
    void setUp() {
        camper = User.builder().id(1L).firstName("Alice").lastName("C")
                .email("alice@test.com").role(Role.COMPERS).enabled(true).build();

        admin = User.builder().id(2L).firstName("Bob").lastName("A")
                .email("bob@test.com").role(Role.ADMIN).enabled(true).build();

        campsite = Campsite.builder()
                .id(10L).name("Test Camp").country("TN").city("Tunis")
                .capacity(50).type(CampsiteType.OFFICIAL)
                .pricePerNight(new BigDecimal("30.00"))
                .status(CampsiteStatus.ACTIVE).owner(admin)
                .build();
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_validRequest_success() {
        CampsiteBookingRequest req = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 2);

        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));
        when(bookingRepository.sumGuestsOverlapping(any(), any(), any())).thenReturn(0);

        CampsiteBooking savedBooking = buildBooking(CampsiteBookingStatus.PENDING);
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        CampsiteBookingResponse response = bookingService.create(req, 1L);

        assertThat(response.getCampsiteId()).isEqualTo(10L);
        verify(bookingRepository).save(any(CampsiteBooking.class));
    }

    @Test
    void create_capacityExceeded_throwsException() {
        CampsiteBookingRequest req = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 10);

        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));
        // 45 guests already booked, requesting 10 more → exceeds capacity of 50
        when(bookingRepository.sumGuestsOverlapping(any(), any(), any())).thenReturn(45);

        assertThatThrownBy(() -> bookingService.create(req, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    void create_checkInNotBeforeCheckOut_throwsException() {
        CampsiteBookingRequest req = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(5), 2);

        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));

        assertThatThrownBy(() -> bookingService.create(req, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_pastCheckIn_throwsException() {
        CampsiteBookingRequest req = buildRequest(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), 2);

        when(campsiteRepository.findById(10L)).thenReturn(Optional.of(campsite));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));

        assertThatThrownBy(() -> bookingService.create(req, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Test
    void cancel_byCamperWithin2Days_throwsException() {
        CampsiteBooking booking = buildBooking(CampsiteBookingStatus.CONFIRMED);
        booking.setCheckInDate(LocalDate.now().plusDays(1)); // within 2 days
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));

        assertThatThrownBy(() -> bookingService.cancel(1L, 1L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 days");
    }

    @Test
    void cancel_byCamperAfter2Days_success() {
        CampsiteBooking booking = buildBooking(CampsiteBookingStatus.CONFIRMED);
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(camper));
        when(bookingRepository.save(any())).thenReturn(booking);

        CampsiteBookingResponse response = bookingService.cancel(1L, 1L, "Changed plans");

        assertThat(response.getStatus()).isEqualTo(CampsiteBookingStatus.CANCELLED);
    }

    @Test
    void cancel_byOtherUser_throwsException() {
        CampsiteBooking booking = buildBooking(CampsiteBookingStatus.CONFIRMED);
        booking.setCheckInDate(LocalDate.now().plusDays(10));

        User stranger = User.builder().id(99L).role(Role.COMPERS).enabled(true)
                .firstName("X").lastName("Y").email("x@y.com").build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(99L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> bookingService.cancel(1L, 99L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the booking camper");
    }

    // ── confirm ──────────────────────────────────────────────────────────────

    @Test
    void confirm_pendingBooking_statusBecomesConfirmed() {
        CampsiteBooking booking = buildBooking(CampsiteBookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        doNothing().when(emailService).sendBookingConfirmationEmail(
                any(), any(), any(), any(), any(), any());

        CampsiteBookingResponse response = bookingService.confirm(1L);

        assertThat(response.getStatus()).isEqualTo(CampsiteBookingStatus.CONFIRMED);
    }

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CampsiteBookingRequest buildRequest(LocalDate in, LocalDate out, int guests) {
        CampsiteBookingRequest r = new CampsiteBookingRequest();
        r.setCampsiteId(10L);
        r.setCheckInDate(in);
        r.setCheckOutDate(out);
        r.setNumberOfGuests(guests);
        return r;
    }

    private CampsiteBooking buildBooking(CampsiteBookingStatus status) {
        return CampsiteBooking.builder()
                .id(1L).campsite(campsite).camper(camper)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(8))
                .numberOfGuests(2)
                .totalPrice(new BigDecimal("90.00"))
                .status(status)
                .build();
    }
}
