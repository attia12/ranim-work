package tn.esprit.projetpidev.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(Boolean.TRUE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        // ── Swagger / OpenAPI ─────────────────────────────────────────────
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/api-docs.yaml",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ── Authentication ────────────────────────────────────────────────
                        .requestMatchers("/auth/register", "/auth/login", "/auth/me",
                                "/auth/users/search", "/auth/forgot-password",
                                "/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/auth/profile/agents").hasRole("ADMIN")
                        .requestMatchers("/auth/profile/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/camper").hasRole("COMPERS")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/campsite-owner").hasRole("COMPSITEOWNERS")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/campsite-owner/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/guide").hasRole("GUIDE")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/guide/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/coach").hasRole("COACH")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/coach/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/sponsor").hasRole("SPONSORS")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/partner").hasRole("PARTENERS")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/campsite-manager").hasRole("CAMPSITEMANAGER")
                        .requestMatchers(HttpMethod.GET,    "/auth/profile/provider/warehouses").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/auth/profile/provider/*/warehouses").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/auth/profile/provider/warehouses").hasRole("EQUIPEMENTPROVIEDERS")
                        .requestMatchers(HttpMethod.PUT,    "/auth/profile/provider/warehouses/*").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/auth/profile/provider/warehouses/*").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")

                        // ── Bookings ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/bookings", "/bookings/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/bookings").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/bookings/**").authenticated()

                        // ── Equipment ─────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/marketplace/equipment",
                                "/api/marketplace/equipment/{id}",
                                "/api/marketplace/equipment/search",
                                "/api/marketplace/equipment/category/{id}",
                                "/api/marketplace/equipment/owner/{id}",
                                "/api/marketplace/equipment/{eqId}/unavailable-periods",
                                "/api/marketplace/equipment/{eqId}/blocked-periods"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/equipment").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/equipment/{id}").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/marketplace/equipment/{id}").hasAnyRole("EQUIPEMENTPROVIEDERS", "ADMIN")

                        // ── Equipment Categories ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/marketplace/categories",
                                "/api/marketplace/categories/{id}"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/categories/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/marketplace/categories/{id}").hasRole("ADMIN")

                        // ── Reviews ───────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,
                                "/api/marketplace/reviews/equipment/{id}",
                                "/api/marketplace/reviews/user/{id}"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/reviews").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/reviews/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/marketplace/reviews/{id}").authenticated()

                        // ── Orders ────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/orders/my").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/orders/status/{status}").hasAnyRole("ADMIN", "EQUIPEMENTPROVIEDERS")
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/orders/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/orders").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/orders/{id}/coupon").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/orders/{id}/status").hasAnyRole("ADMIN", "EQUIPEMENTPROVIEDERS")
                        .requestMatchers(HttpMethod.DELETE, "/api/marketplace/orders/{id}").authenticated()

                        // ── Order Items ───────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/order-items/{itemId}").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/order-items/order/{orderId}").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/order-items/equipment/{equipmentId}").hasAnyRole("ADMIN", "EQUIPEMENTPROVIEDERS")
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/order-items/order/{orderId}").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/order-items/{itemId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/marketplace/order-items/{itemId}").authenticated()

                        // ── Payments ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/payments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/marketplace/payments/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/marketplace/payments").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/payments/{id}/complete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/marketplace/payments/{id}/refund").hasRole("ADMIN")

                        // ── Coupons ───────────────────────────────────────────────────────
                        .requestMatchers("/api/marketplace/coupons", "/api/marketplace/coupons/**").hasRole("ADMIN")

                        // ── Vehicles ──────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/vehicles/available").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/vehicles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/vehicles/my").hasRole("DELIVERYAGENT")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/vehicles/owner/{id}").hasAnyRole("DELIVERYAGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/vehicles/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/delivery/vehicles").hasRole("DELIVERYAGENT")
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/vehicles/{id}").hasAnyRole("DELIVERYAGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/vehicles/{id}/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/vehicles/{id}/availability").hasAnyRole("DELIVERYAGENT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery/vehicles/{id}").hasAnyRole("DELIVERYAGENT", "ADMIN")

                        // ── Deliveries ────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/my").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/available").hasRole("DELIVERYAGENT")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/agent").hasRole("DELIVERYAGENT")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/status/{status}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/vehicle/{vehicleId}").hasAnyRole("DELIVERYAGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/by-order/{orderId}").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/deliveries/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/delivery/deliveries").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/deliveries/{id}/claim").hasRole("DELIVERYAGENT")
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/deliveries/{id}/assign").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/deliveries/{id}/status").hasAnyRole("DELIVERYAGENT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery/deliveries/{id}").hasRole("ADMIN")

                        // ── Delivery Items ────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/delivery-items/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/delivery/delivery-items/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/delivery-items/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery/delivery-items/**").authenticated()

                        // ── Delivery Ratings ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/delivery/ratings/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/delivery/ratings/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/delivery/ratings/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/delivery/ratings/**").authenticated()

                        // ── Categories / Posts (public read) ──────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/categories", "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/posts", "/posts/**").permitAll()

                        // ── Events ────────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/events", "/events/**").hasAnyRole("ADMIN", "EVENT_ORGANIZER", "COMPERS")
                        .requestMatchers(HttpMethod.POST,   "/events", "/events/**").hasAnyRole("ADMIN", "EVENT_ORGANIZER")
                        .requestMatchers(HttpMethod.PUT,    "/events/**").hasAnyRole("ADMIN", "EVENT_ORGANIZER")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasAnyRole("ADMIN", "EVENT_ORGANIZER")

                        // ── Campsites (Module 1) ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/v1/campsites", "/api/v1/campsites/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/campsites").hasAnyRole("COMPSITEOWNERS", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/campsites/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/campsites/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/campsites/**").hasRole("ADMIN")

                        // ── Availabilities ────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/v1/availabilities/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/availabilities/**").hasAnyRole("COMPSITEOWNERS", "ADMIN")

                        // ── Campsite Bookings ─────────────────────────────────────────────
                        .requestMatchers("/api/v1/campsite-bookings/**").authenticated()

                        // ── Campsite Payments ─────────────────────────────────────────────
                        .requestMatchers("/api/v1/campsite-payments/**").authenticated()

                        // ── Outdoor Campsites (Module 2) ──────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/v1/outdoor-campsites", "/api/v1/outdoor-campsites/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/outdoor-campsites").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/outdoor-campsites/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/outdoor-campsites/*/moderate").hasRole("ADMIN") // ← FIXED: ** → *

                        // ── Outdoor Availabilities ────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/v1/outdoor-availabilities/**").permitAll()
                        .requestMatchers("/api/v1/outdoor-availabilities/**").hasRole("ADMIN")

                        // ── Outdoor Bookings ──────────────────────────────────────────────
                        .requestMatchers("/api/v1/outdoor-bookings/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}