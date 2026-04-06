// Module: Official Campsite & Booking | Layer: Controller Integration Test
package tn.esprit.projetpidev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingRequest;
import tn.esprit.projetpidev.dto.campsitebooking.CampsiteBookingResponse;
import tn.esprit.projetpidev.services.ICampsiteBookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampsiteBookingController.class)
class CampsiteBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICampsiteBookingService bookingService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(username = "user@test.com")
    void create_authenticated_returns201() throws Exception {
        CampsiteBookingRequest request = buildRequest();
        CampsiteBookingResponse response = buildResponse();
        when(bookingService.create(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/campsite-bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_unauthenticated_returns401or403() throws Exception {
        mockMvc.perform(post("/api/v1/campsite-bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(result ->
                        org.assertj.core.api.Assertions.assertThat(
                                result.getResponse().getStatus()).isIn(401, 403));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getMyBookings_authenticated_returns200() throws Exception {
        when(bookingService.getMyCamperBookings(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildResponse())));

        mockMvc.perform(get("/api/v1/campsite-bookings/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void cancel_authenticated_returns200() throws Exception {
        when(bookingService.cancel(any(), any(), any())).thenReturn(buildResponse());

        mockMvc.perform(patch("/api/v1/campsite-bookings/1/cancel").with(csrf()))
                .andExpect(status().isOk());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CampsiteBookingRequest buildRequest() {
        CampsiteBookingRequest r = new CampsiteBookingRequest();
        r.setCampsiteId(10L);
        r.setCheckInDate(LocalDate.now().plusDays(5));
        r.setCheckOutDate(LocalDate.now().plusDays(8));
        r.setNumberOfGuests(2);
        return r;
    }

    private CampsiteBookingResponse buildResponse() {
        CampsiteBookingResponse r = new CampsiteBookingResponse();
        r.setId(1L);
        r.setCampsiteId(10L);
        r.setCampsiteName("Test Camp");
        r.setCheckInDate(LocalDate.now().plusDays(5));
        r.setCheckOutDate(LocalDate.now().plusDays(8));
        r.setNumberOfGuests(2);
        r.setTotalPrice(new BigDecimal("90.00"));
        r.setStatus(CampsiteBookingStatus.PENDING);
        return r;
    }
}
