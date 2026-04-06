package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.userprofile.*;
import tn.esprit.projetpidev.dto.auth.AuthResponse;
import tn.esprit.projetpidev.dto.auth.LoginRequest;
import tn.esprit.projetpidev.dto.auth.RegisterRequest;
import tn.esprit.projetpidev.jwt.JwtService;
import tn.esprit.projetpidev.repositories.UserRepository;
import tn.esprit.projetpidev.repositories.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IAuthServiceImpl implements IAuthService {

        private final UserRepository userRepo;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtSevice;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;

        private final ProviderProfileRepository providerProfileRepo;
        private final AgentProfileRepository agentProfileRepo;
        private final CamperProfileRepository camperProfileRepo;
        private final CampsiteOwnerProfileRepository campsiteOwnerProfileRepo;
        private final GuideProfileRepository guideProfileRepo;
        private final CoachProfileRepository coachProfileRepo;
        private final SponsorProfileRepository sponsorProfileRepo;
        private final PartnerProfileRepository partnerProfileRepo;
        private final CampsiteManagerProfileRepository campsiteManagerProfileRepo;
        private final EventOrganizerProfileRepository eventOrganizerProfileRepo;

        @Override
        public String register(RegisterRequest request) {
                if (userRepo.findByEmail(request.getEmail()).isPresent()) {
                        throw new IllegalArgumentException("An account with this email already exists.");
                }

                // ── Create and save the base user ─────────────────────────────────────
                User user = User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .phoneNumber(request.getPhoneNumber())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .role(request.getRole())
                        .avatar(request.getAvatar())
                        .address(request.getAddress())
                        .city(request.getCity())
                        .build();
                userRepo.save(user);

                // ── Save role-specific profile if fields are present ──────────────────

                if (request.getRole() == Role.EQUIPEMENTPROVIEDERS) {
                        if (hasText(request.getCompanyName()) || hasText(request.getShopDescription())) {
                                ProviderProfile p = ProviderProfile.builder().user(user).build();
                                if (hasText(request.getCompanyName()))
                                        p.setCompanyName(request.getCompanyName());
                                if (hasText(request.getShopDescription()))
                                        p.setShopDescription(request.getShopDescription());
                                providerProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.DELIVERYAGENT) {
                        if (hasText(request.getLicenseNumber())) {
                                AgentProfile p = AgentProfile.builder().user(user).licenseNumber(request.getLicenseNumber())
                                        .build();
                                agentProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.COMPERS) {
                        if (hasText(request.getExperienceLevel()) || hasText(request.getPreferredTerrain())
                                || hasText(request.getBio())) {
                                CamperProfile p = CamperProfile.builder().user(user).build();
                                if (hasText(request.getExperienceLevel()))
                                        p.setExperienceLevel(request.getExperienceLevel());
                                if (hasText(request.getPreferredTerrain()))
                                        p.setPreferredTerrain(request.getPreferredTerrain());
                                if (hasText(request.getBio()))
                                        p.setBio(request.getBio());
                                camperProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.COMPSITEOWNERS) {
                        if (hasText(request.getBusinessName()) || hasText(request.getBusinessDescription())) {
                                CampsiteOwnerProfile p = CampsiteOwnerProfile.builder().user(user).build();
                                if (hasText(request.getBusinessName()))
                                        p.setBusinessName(request.getBusinessName());
                                if (hasText(request.getBusinessDescription()))
                                        p.setBusinessDescription(request.getBusinessDescription());
                                if (hasText(request.getWebsiteUrl()))
                                        p.setWebsiteUrl(request.getWebsiteUrl());
                                if (hasText(request.getRegion()))
                                        p.setRegion(request.getRegion());
                                campsiteOwnerProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.GUIDE) {
                        if (hasText(request.getSpecialization()) || hasText(request.getCertificationNumber())) {
                                GuideProfile p = GuideProfile.builder().user(user).build();
                                if (hasText(request.getSpecialization()))
                                        p.setSpecialization(request.getSpecialization());
                                if (hasText(request.getLanguages()))
                                        p.setLanguages(request.getLanguages());
                                if (hasText(request.getCertificationNumber()))
                                        p.setCertificationNumber(request.getCertificationNumber());
                                if (hasText(request.getBio()))
                                        p.setBio(request.getBio());
                                if (request.getYearsExperience() != null)
                                        p.setYearsExperience(request.getYearsExperience());
                                guideProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.COACH) {
                        if (hasText(request.getCoachingType()) || hasText(request.getCertifications())) {
                                CoachProfile p = CoachProfile.builder().user(user).build();
                                if (hasText(request.getCoachingType()))
                                        p.setCoachingType(request.getCoachingType());
                                if (hasText(request.getCertifications()))
                                        p.setCertifications(request.getCertifications());
                                if (hasText(request.getBio()))
                                        p.setBio(request.getBio());
                                if (request.getYearsExperience() != null)
                                        p.setYearsExperience(request.getYearsExperience());
                                coachProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.SPONSORS) {
                        if (hasText(request.getCompanyName()) || hasText(request.getSponsorshipDescription())) {
                                SponsorProfile p = SponsorProfile.builder().user(user).build();
                                if (hasText(request.getCompanyName()))
                                        p.setCompanyName(request.getCompanyName());
                                if (hasText(request.getSponsorshipDescription()))
                                        p.setSponsorshipDescription(request.getSponsorshipDescription());
                                if (hasText(request.getWebsiteUrl()))
                                        p.setWebsiteUrl(request.getWebsiteUrl());
                                if (hasText(request.getLogoUrl()))
                                        p.setLogoUrl(request.getLogoUrl());
                                if (hasText(request.getIndustry()))
                                        p.setIndustry(request.getIndustry());
                                sponsorProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.PARTENERS) {
                        if (hasText(request.getOrganizationName()) || hasText(request.getPartnershipDescription())) {
                                PartnerProfile p = PartnerProfile.builder().user(user).build();
                                if (hasText(request.getOrganizationName()))
                                        p.setOrganizationName(request.getOrganizationName());
                                if (hasText(request.getPartnershipDescription()))
                                        p.setPartnershipDescription(request.getPartnershipDescription());
                                if (hasText(request.getWebsiteUrl()))
                                        p.setWebsiteUrl(request.getWebsiteUrl());
                                if (hasText(request.getPartnerType()))
                                        p.setPartnerType(request.getPartnerType());
                                partnerProfileRepo.save(p);
                        }
                }

                if (request.getRole() == Role.CAMPSITEMANAGER) {
                        CampsiteManagerProfile p = CampsiteManagerProfile.builder().user(user).build();
                        if (hasText(request.getManagedRegion()))
                                p.setManagedRegion(request.getManagedRegion());
                        if (hasText(request.getManagerNotes()))
                                p.setManagerNotes(request.getManagerNotes());
                        campsiteManagerProfileRepo.save(p);
                }

                // ── EVENT_ORGANIZER ───────────────────────────────────────────────────
                if (request.getRole() == Role.EVENT_ORGANIZER) {
                        EventOrganizerProfile p = EventOrganizerProfile.builder().user(user).build();
                        if (hasText(request.getOrganizationName()))
                                p.setOrganizationName(request.getOrganizationName());
                        if (hasText(request.getBio()))
                                p.setBio(request.getBio());
                        if (hasText(request.getSpecialization()))
                                p.setSpecialization(request.getSpecialization());
                        if (hasText(request.getWebsiteUrl()))
                                p.setWebsiteUrl(request.getWebsiteUrl());
                        if (hasText(request.getPhoneNumber()))
                                p.setPhoneContact(request.getPhoneNumber());
                        if (request.getYearsExperience() != null)
                                p.setYearsExperience(request.getYearsExperience());
                        eventOrganizerProfileRepo.save(p);
                }

                return "User created: " + user.getEmail();
        }

        /** Null-safe non-blank check */
        private boolean hasText(String s) {
                return s != null && !s.isBlank();
        }

        @Override
        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()));
                User user = userRepo.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                String token = jwtSevice.generateToken(user);
                return new AuthResponse(token, user.getRole().name(), user.getId(),
                        user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
                        user.getAvatar(), user.getAddress(), user.getCity());
        }

        @Override
        public AuthResponse updateProfile(Long userId, RegisterRequest request) {
                User user = userRepo.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setPhoneNumber(request.getPhoneNumber());
                userRepo.save(user);
                String token = jwtSevice.generateToken(user);
                return new AuthResponse(token, user.getRole().name(), user.getId(),
                        user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
                        user.getAvatar(), user.getAddress(), user.getCity());
        }

        @Override
        public void forgotPassword(String email) {
                userRepo.findByEmail(email).ifPresent(user -> {
                        String token = UUID.randomUUID().toString();
                        user.setPasswordResetToken(token);
                        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
                        userRepo.save(user);
                        try {
                                emailService.sendPasswordResetEmail(user.getEmail(), token);
                        } catch (org.springframework.mail.MailException e) {
                                throw new RuntimeException("Failed to send reset email: " + e.getMessage());
                        }
                });
        }

        @Override
        public void resetPassword(String token, String newPassword) {
                User user = userRepo.findByPasswordResetToken(token)
                        .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

                if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        throw new RuntimeException("Invalid or expired token");
                }

                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiry(null);
                userRepo.save(user);
        }

        @Override
        public List<User> searchUsers(String query) {
                String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query)
                        .stream()
                        .filter(u -> !u.getEmail().equals(currentEmail))
                        .toList();
        }
}