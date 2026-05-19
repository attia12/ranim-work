// Module: Official Campsite & Booking | Layer: Controller Integration Test
package tn.esprit.projetpidev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import tn.esprit.projetpidev.jwt.PasswordConfig;
import tn.esprit.projetpidev.jwt.SecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.dto.campsite.CampsiteRequest;
import tn.esprit.projetpidev.dto.campsite.CampsiteResponse;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.jwt.CustomUserDetailsService;
import tn.esprit.projetpidev.jwt.JwtService;
import tn.esprit.projetpidev.services.ICampsiteService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CampsiteController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, PasswordConfig.class})
class CampsiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private ICampsiteService campsiteService;

    @Test
    void getById_publicEndpoint_returns200() throws Exception {
        CampsiteResponse response = buildResponse();
        when(campsiteService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/campsites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Campsite"));
    }

    @Test
    void search_publicEndpoint_returns200() throws Exception {
        when(campsiteService.search(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildResponse())));

        mockMvc.perform(get("/api/v1/campsites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void create_ownerRole_returns201() throws Exception {
        CampsiteRequest request = buildRequest();
        CampsiteResponse response = buildResponse();
        when(campsiteService.create(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/campsites")
                        .with(user(buildMockUser(Role.COMPSITEOWNERS)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_camperRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/campsites")
                        .with(user(buildMockUser(Role.COMPERS)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspend_adminRole_returns200() throws Exception {
        when(campsiteService.suspend(1L)).thenReturn(buildResponse());

        mockMvc.perform(patch("/api/v1/campsites/1/suspend")
                        .with(user(buildMockUser(Role.ADMIN)))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/campsites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildMockUser(Role role) {
        return User.builder().id(1L).email("test@test.com").password("pw").role(role).build();
    }

    private CampsiteRequest buildRequest() {
        CampsiteRequest r = new CampsiteRequest();
        r.setName("Test Campsite");
        r.setCountry("Tunisia");
        r.setCity("Tunis");
        r.setCapacity(20);
        r.setType(CampsiteType.OFFICIAL);
        r.setPricePerNight(new BigDecimal("50.00"));
        r.setLatitude(36.8);
        r.setLongitude(10.1);
        return r;
    }

    private CampsiteResponse buildResponse() {
        CampsiteResponse r = new CampsiteResponse();
        r.setId(1L);
        r.setName("Test Campsite");
        r.setCountry("Tunisia");
        r.setCity("Tunis");
        r.setType(CampsiteType.OFFICIAL);
        r.setStatus(CampsiteStatus.ACTIVE);
        r.setPricePerNight(new BigDecimal("50.00"));
        return r;
    }
}
