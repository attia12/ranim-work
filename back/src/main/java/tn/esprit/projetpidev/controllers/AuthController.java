package tn.esprit.projetpidev.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.userprofile.*;
import tn.esprit.projetpidev.dto.auth.AuthResponse;
import tn.esprit.projetpidev.dto.auth.LoginRequest;
import tn.esprit.projetpidev.dto.auth.RegisterRequest;
import tn.esprit.projetpidev.repositories.*;
import tn.esprit.projetpidev.domain.userprofile.EventOrganizerProfile;
import tn.esprit.projetpidev.services.IAuthService;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = " Auth", description = "Register and login — returns JWT token")
public class AuthController {

    private final IAuthService authService;
    private final UserRepository userRepo;
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
    private final WarehouseRepository warehouseRepo;

    // ✅ DTO simple pour la recherche — évite d'exposer le mot de passe

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            authService.forgotPassword(email);
            return ResponseEntity.ok(Map.of("message", "If this email is registered, a reset link has been sent."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");
            String newPassword = body.get("newPassword");
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid or expired token"));
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<AuthResponse> updateProfile(@PathVariable Long id,
                                                      @RequestBody RegisterRequest request,
                                                      Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(null);
        }
        User requester = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isOwner = requester.getId().equals(id);
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body(null);
        }
        // Update base profile via existing service
        AuthResponse updatedAuth = authService.updateProfile(id, request);

        // Also update new User fields directly
        User target = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getAvatar()   != null) target.setAvatar(request.getAvatar());
        if (request.getAddress()  != null) target.setAddress(request.getAddress());
        if (request.getCity()     != null) target.setCity(request.getCity());
        userRepo.save(target);

        return ResponseEntity.ok(updatedAuth);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Not authenticated. Provide a Bearer token."));
        }
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("id",          user.getId());
        response.put("email",       user.getEmail() != null ? user.getEmail() : "");
        response.put("firstName",   user.getFirstName() != null ? user.getFirstName() : "");
        response.put("lastName",    user.getLastName() != null ? user.getLastName() : "");
        response.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        response.put("avatar",      user.getAvatar() != null ? user.getAvatar() : "");
        response.put("address",     user.getAddress() != null ? user.getAddress() : "");
        response.put("city",        user.getCity() != null ? user.getCity() : "");
        response.put("role",        user.getRole().name());

        // Attach role-specific profile if it exists
        if (user.getRole() == Role.EQUIPEMENTPROVIEDERS) {
            providerProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("companyName",     p.getCompanyName() != null ? p.getCompanyName() : "");
                response.put("shopDescription", p.getShopDescription() != null ? p.getShopDescription() : "");
            });
        }
        if (user.getRole() == Role.DELIVERYAGENT) {
            agentProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("licenseNumber", p.getLicenseNumber() != null ? p.getLicenseNumber() : "");
                response.put("isVerified",    p.getIsVerified() != null ? p.getIsVerified() : false);
            });
        }
        if (user.getRole() == Role.COMPERS) {
            camperProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("experienceLevel",  p.getExperienceLevel()  != null ? p.getExperienceLevel()  : "");
                response.put("preferredTerrain", p.getPreferredTerrain() != null ? p.getPreferredTerrain() : "");
                response.put("bio",              p.getBio()              != null ? p.getBio()              : "");
                response.put("totalTrips",       p.getTotalTrips()       != null ? p.getTotalTrips()       : 0);
            });
        }
        if (user.getRole() == Role.COMPSITEOWNERS) {
            campsiteOwnerProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("businessName",        p.getBusinessName()        != null ? p.getBusinessName()        : "");
                response.put("businessDescription", p.getBusinessDescription() != null ? p.getBusinessDescription() : "");
                response.put("websiteUrl",          p.getWebsiteUrl()          != null ? p.getWebsiteUrl()          : "");
                response.put("region",              p.getRegion()              != null ? p.getRegion()              : "");
                response.put("isVerified",          p.getIsVerified()          != null ? p.getIsVerified()          : false);
            });
        }
        if (user.getRole() == Role.GUIDE) {
            guideProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("specialization",     p.getSpecialization()     != null ? p.getSpecialization()     : "");
                response.put("bio",                p.getBio()                != null ? p.getBio()                : "");
                response.put("yearsExperience",    p.getYearsExperience()    != null ? p.getYearsExperience()    : 0);
                response.put("languages",          p.getLanguages()          != null ? p.getLanguages()          : "");
                response.put("certificationNumber",p.getCertificationNumber()!= null ? p.getCertificationNumber(): "");
                response.put("isVerified",         p.getIsVerified()         != null ? p.getIsVerified()         : false);
            });
        }
        if (user.getRole() == Role.COACH) {
            coachProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("coachingType",    p.getCoachingType()    != null ? p.getCoachingType()    : "");
                response.put("bio",             p.getBio()             != null ? p.getBio()             : "");
                response.put("yearsExperience", p.getYearsExperience() != null ? p.getYearsExperience() : 0);
                response.put("certifications",  p.getCertifications()  != null ? p.getCertifications()  : "");
                response.put("isVerified",      p.getIsVerified()      != null ? p.getIsVerified()      : false);
            });
        }
        if (user.getRole() == Role.SPONSORS) {
            sponsorProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("companyName",             p.getCompanyName()             != null ? p.getCompanyName()             : "");
                response.put("sponsorshipDescription",  p.getSponsorshipDescription()  != null ? p.getSponsorshipDescription()  : "");
                response.put("websiteUrl",              p.getWebsiteUrl()              != null ? p.getWebsiteUrl()              : "");
                response.put("logoUrl",                 p.getLogoUrl()                 != null ? p.getLogoUrl()                 : "");
                response.put("industry",                p.getIndustry()                != null ? p.getIndustry()                : "");
            });
        }
        if (user.getRole() == Role.PARTENERS) {
            partnerProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("organizationName",        p.getOrganizationName()        != null ? p.getOrganizationName()        : "");
                response.put("partnershipDescription",  p.getPartnershipDescription()  != null ? p.getPartnershipDescription()  : "");
                response.put("websiteUrl",              p.getWebsiteUrl()              != null ? p.getWebsiteUrl()              : "");
                response.put("partnerType",             p.getPartnerType()             != null ? p.getPartnerType()             : "");
            });
        }
        if (user.getRole() == Role.CAMPSITEMANAGER) {
            campsiteManagerProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("managedRegion", p.getManagedRegion() != null ? p.getManagedRegion() : "");
                response.put("managerNotes",  p.getManagerNotes()  != null ? p.getManagerNotes()  : "");
            });
        }
        if (user.getRole() == Role.EVENT_ORGANIZER) {
            eventOrganizerProfileRepo.findByUserId(user.getId()).ifPresent(p -> {
                response.put("organizationName", p.getOrganizationName() != null ? p.getOrganizationName() : "");
                response.put("bio",              p.getBio()              != null ? p.getBio()              : "");
                response.put("specialization",   p.getSpecialization()   != null ? p.getSpecialization()   : "");
                response.put("websiteUrl",       p.getWebsiteUrl()       != null ? p.getWebsiteUrl()       : "");
                response.put("phoneContact",     p.getPhoneContact()     != null ? p.getPhoneContact()     : "");
                response.put("yearsExperience",  p.getYearsExperience()  != null ? p.getYearsExperience()  : 0);
                response.put("isVerified",       p.getIsVerified()       != null ? p.getIsVerified()       : false);
            });
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Not authenticated."));
        }
        // Get the current user so we exclude ourselves from results
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String query = q.trim().toLowerCase();
        if (query.length() < 2) {
            return ResponseEntity.ok(java.util.List.of());
        }

        List<java.util.Map<String, Object>> results = userRepo.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .filter(u -> {
                    String fullName = ((u.getFirstName() != null ? u.getFirstName() : "") + " " +
                            (u.getLastName()  != null ? u.getLastName()  : "")).toLowerCase();
                    String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    return fullName.contains(query) || email.contains(query);
                })
                .limit(10)
                .map(u -> java.util.Map.<String, Object>of(
                        "id",        u.getId(),
                        "fullName",  (u.getFirstName() != null ? u.getFirstName() : "") + " " +
                                (u.getLastName()  != null ? u.getLastName()  : ""),
                        "email",     u.getEmail() != null ? u.getEmail() : "",
                        "role",      u.getRole().name()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }

    // ── ADMIN: List all users ──────────────────────────────────────────────────
    @GetMapping("/users")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        java.util.List<java.util.Map<String, Object>> users = userRepo.findAll().stream()
                .map(u -> {
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("id",          u.getId());
                    map.put("firstName",   u.getFirstName()   != null ? u.getFirstName()   : "");
                    map.put("lastName",    u.getLastName()    != null ? u.getLastName()    : "");
                    map.put("email",       u.getEmail()       != null ? u.getEmail()       : "");
                    map.put("phoneNumber", u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
                    map.put("role",        u.getRole().name());
                    map.put("enabled",     u.isEnabled());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(users);
    }

    // ── ADMIN: Update user role / details ─────────────────────────────────────
    @PutMapping("/users/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminUpdateUser(@PathVariable Long id,
                                             @RequestBody java.util.Map<String, String> body) {
        tn.esprit.projetpidev.domain.User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.containsKey("firstName"))   user.setFirstName(body.get("firstName"));
        if (body.containsKey("lastName"))    user.setLastName(body.get("lastName"));
        if (body.containsKey("phoneNumber")) user.setPhoneNumber(body.get("phoneNumber"));
        if (body.containsKey("role") && body.get("role") != null) {
            try {
                user.setRole(Role.valueOf(body.get("role")));
            } catch (IllegalArgumentException ignored) { }
        }
        if (body.containsKey("enabled")) {
            user.setEnabled(Boolean.parseBoolean(body.get("enabled")));
        }

        userRepo.save(user);
        return ResponseEntity.ok(java.util.Map.of("message", "User updated successfully"));
    }

    // ── ADMIN: Delete user ─────────────────────────────────────────────────────
    @DeleteMapping("/users/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok(java.util.Map.of("message", "User deleted successfully"));
    }

    // ── Provider profile: get ─────────────────────────────────────────────────
    @GetMapping("/profile/provider/{userId}")
    public ResponseEntity<?> getProviderProfile(@PathVariable Long userId) {
        return providerProfileRepo.findByUserId(userId)
                .map(p -> ResponseEntity.ok((Object) java.util.Map.of(
                        "id",              p.getId(),
                        "userId",          userId,
                        "companyName",     p.getCompanyName() != null ? p.getCompanyName() : "",
                        "shopDescription", p.getShopDescription() != null ? p.getShopDescription() : ""
                )))
                .orElse(ResponseEntity.ok(java.util.Map.of(
                        "id", 0, "userId", userId, "companyName", "", "shopDescription", ""
                )));
    }

    // ── Provider profile: save ────────────────────────────────────────────────
    @PutMapping("/profile/provider")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('EQUIPEMENTPROVIEDERS')")
    public ResponseEntity<?> saveProviderProfile(@RequestBody java.util.Map<String, String> body,
                                                 Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProviderProfile profile = providerProfileRepo.findByUserId(user.getId())
                .orElse(ProviderProfile.builder().user(user).build());
        if (body.containsKey("companyName"))     profile.setCompanyName(body.get("companyName"));
        if (body.containsKey("shopDescription")) profile.setShopDescription(body.get("shopDescription"));
        providerProfileRepo.save(profile);
        return ResponseEntity.ok(java.util.Map.of("message", "Provider profile saved"));
    }

    // ── Agent profile: get ────────────────────────────────────────────────────
    @GetMapping("/profile/agent/{userId}")
    public ResponseEntity<?> getAgentProfile(@PathVariable Long userId) {
        return agentProfileRepo.findByUserId(userId)
                .map(p -> ResponseEntity.ok((Object) java.util.Map.of(
                        "id",            p.getId(),
                        "userId",        userId,
                        "licenseNumber", p.getLicenseNumber() != null ? p.getLicenseNumber() : "",
                        "isVerified",    p.getIsVerified() != null ? p.getIsVerified() : false
                )))
                .orElse(ResponseEntity.ok(java.util.Map.of(
                        "id", 0, "userId", userId, "licenseNumber", "", "isVerified", false
                )));
    }

    // ── Agent profile: save ───────────────────────────────────────────────────
    @PutMapping("/profile/agent")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DELIVERYAGENT')")
    public ResponseEntity<?> saveAgentProfile(@RequestBody java.util.Map<String, String> body,
                                              Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        AgentProfile profile = agentProfileRepo.findByUserId(user.getId())
                .orElse(AgentProfile.builder().user(user).build());
        if (body.containsKey("licenseNumber")) profile.setLicenseNumber(body.get("licenseNumber"));
        agentProfileRepo.save(profile);
        return ResponseEntity.ok(java.util.Map.of("message", "Agent profile saved"));
    }

    // ── Admin: verify delivery agent ──────────────────────────────────────────
    @PutMapping("/profile/agent/{userId}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyAgent(@PathVariable Long userId) {
        AgentProfile profile = agentProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent profile not found for user " + userId));
        profile.setIsVerified(true);
        agentProfileRepo.save(profile);
        return ResponseEntity.ok(java.util.Map.of("message", "Agent verified successfully"));
    }

    // ── Camper profile: save ──────────────────────────────────────────────────
    @PutMapping("/profile/camper")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('COMPERS')")
    public ResponseEntity<?> saveCamperProfile(@RequestBody java.util.Map<String, Object> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        CamperProfile p = camperProfileRepo.findByUserId(user.getId()).orElse(CamperProfile.builder().user(user).build());
        if (body.containsKey("experienceLevel"))  p.setExperienceLevel((String)  body.get("experienceLevel"));
        if (body.containsKey("preferredTerrain")) p.setPreferredTerrain((String) body.get("preferredTerrain"));
        if (body.containsKey("bio"))              p.setBio((String)              body.get("bio"));
        if (body.containsKey("totalTrips") && body.get("totalTrips") instanceof Number)
            p.setTotalTrips(((Number) body.get("totalTrips")).intValue());
        camperProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Camper profile saved"));
    }

    // ── Campsite owner profile: save ──────────────────────────────────────────
    @PutMapping("/profile/campsite-owner")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('COMPSITEOWNERS')")
    public ResponseEntity<?> saveCampsiteOwnerProfile(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        CampsiteOwnerProfile p = campsiteOwnerProfileRepo.findByUserId(user.getId()).orElse(CampsiteOwnerProfile.builder().user(user).build());
        if (body.containsKey("businessName"))        p.setBusinessName(body.get("businessName"));
        if (body.containsKey("businessDescription")) p.setBusinessDescription(body.get("businessDescription"));
        if (body.containsKey("websiteUrl"))          p.setWebsiteUrl(body.get("websiteUrl"));
        if (body.containsKey("region"))              p.setRegion(body.get("region"));
        campsiteOwnerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Campsite owner profile saved"));
    }

    // ── Guide profile: save ───────────────────────────────────────────────────
    @PutMapping("/profile/guide")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GUIDE')")
    public ResponseEntity<?> saveGuideProfile(@RequestBody java.util.Map<String, Object> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        GuideProfile p = guideProfileRepo.findByUserId(user.getId()).orElse(GuideProfile.builder().user(user).build());
        if (body.containsKey("specialization"))      p.setSpecialization((String)      body.get("specialization"));
        if (body.containsKey("bio"))                 p.setBio((String)                 body.get("bio"));
        if (body.containsKey("languages"))           p.setLanguages((String)           body.get("languages"));
        if (body.containsKey("certificationNumber")) p.setCertificationNumber((String) body.get("certificationNumber"));
        if (body.containsKey("yearsExperience") && body.get("yearsExperience") instanceof Number)
            p.setYearsExperience(((Number) body.get("yearsExperience")).intValue());
        guideProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Guide profile saved"));
    }

    // ── Coach profile: save ───────────────────────────────────────────────────
    @PutMapping("/profile/coach")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('COACH')")
    public ResponseEntity<?> saveCoachProfile(@RequestBody java.util.Map<String, Object> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        CoachProfile p = coachProfileRepo.findByUserId(user.getId()).orElse(CoachProfile.builder().user(user).build());
        if (body.containsKey("coachingType"))    p.setCoachingType((String)    body.get("coachingType"));
        if (body.containsKey("bio"))             p.setBio((String)             body.get("bio"));
        if (body.containsKey("certifications"))  p.setCertifications((String)  body.get("certifications"));
        if (body.containsKey("yearsExperience") && body.get("yearsExperience") instanceof Number)
            p.setYearsExperience(((Number) body.get("yearsExperience")).intValue());
        coachProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Coach profile saved"));
    }

    // ── Sponsor profile: save ─────────────────────────────────────────────────
    @PutMapping("/profile/sponsor")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SPONSORS')")
    public ResponseEntity<?> saveSponsorProfile(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        SponsorProfile p = sponsorProfileRepo.findByUserId(user.getId()).orElse(SponsorProfile.builder().user(user).build());
        if (body.containsKey("companyName"))            p.setCompanyName(body.get("companyName"));
        if (body.containsKey("sponsorshipDescription")) p.setSponsorshipDescription(body.get("sponsorshipDescription"));
        if (body.containsKey("websiteUrl"))             p.setWebsiteUrl(body.get("websiteUrl"));
        if (body.containsKey("logoUrl"))                p.setLogoUrl(body.get("logoUrl"));
        if (body.containsKey("industry"))               p.setIndustry(body.get("industry"));
        sponsorProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Sponsor profile saved"));
    }

    // ── Partner profile: save ─────────────────────────────────────────────────
    @PutMapping("/profile/partner")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('PARTENERS')")
    public ResponseEntity<?> savePartnerProfile(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        PartnerProfile p = partnerProfileRepo.findByUserId(user.getId()).orElse(PartnerProfile.builder().user(user).build());
        if (body.containsKey("organizationName"))       p.setOrganizationName(body.get("organizationName"));
        if (body.containsKey("partnershipDescription")) p.setPartnershipDescription(body.get("partnershipDescription"));
        if (body.containsKey("websiteUrl"))             p.setWebsiteUrl(body.get("websiteUrl"));
        if (body.containsKey("partnerType"))            p.setPartnerType(body.get("partnerType"));
        partnerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Partner profile saved"));
    }

    @PutMapping("/profile/campsite-manager")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CAMPSITEMANAGER')")
    public ResponseEntity<?> saveCampsiteManagerProfile(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        CampsiteManagerProfile p = campsiteManagerProfileRepo.findByUserId(user.getId())
                .orElse(CampsiteManagerProfile.builder().user(user).build());
        if (body.containsKey("managedRegion")) p.setManagedRegion(body.get("managedRegion"));
        if (body.containsKey("managerNotes"))  p.setManagerNotes(body.get("managerNotes"));
        campsiteManagerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Campsite manager profile saved"));
    }

    // ── Admin: verify campsite owner ──────────────────────────────────────────
    @PutMapping("/profile/campsite-owner/{userId}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyCampsiteOwner(@PathVariable Long userId) {
        CampsiteOwnerProfile p = campsiteOwnerProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Campsite owner profile not found for user " + userId));
        p.setIsVerified(true);
        campsiteOwnerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Campsite owner verified"));
    }

    // ── Admin: verify guide ───────────────────────────────────────────────────
    @PutMapping("/profile/guide/{userId}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyGuide(@PathVariable Long userId) {
        GuideProfile p = guideProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Guide profile not found for user " + userId));
        p.setIsVerified(true);
        guideProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Guide verified"));
    }

    // ── Admin: verify coach ───────────────────────────────────────────────────
    @PutMapping("/profile/coach/{userId}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyCoach(@PathVariable Long userId) {
        CoachProfile p = coachProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Coach profile not found for user " + userId));
        p.setIsVerified(true);
        coachProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Coach verified"));
    }

    // ── Event organizer profile: get ──────────────────────────────────────────
    @GetMapping("/profile/event-organizer/{userId}")
    public ResponseEntity<?> getEventOrganizerProfile(@PathVariable Long userId) {
        return eventOrganizerProfileRepo.findByUserId(userId)
                .map(p -> ResponseEntity.ok((Object) java.util.Map.of(
                        "id",               p.getId(),
                        "userId",           userId,
                        "organizationName", p.getOrganizationName() != null ? p.getOrganizationName() : "",
                        "bio",              p.getBio()              != null ? p.getBio()              : "",
                        "specialization",   p.getSpecialization()   != null ? p.getSpecialization()   : "",
                        "websiteUrl",       p.getWebsiteUrl()       != null ? p.getWebsiteUrl()       : "",
                        "phoneContact",     p.getPhoneContact()     != null ? p.getPhoneContact()     : "",
                        "yearsExperience",  p.getYearsExperience()  != null ? p.getYearsExperience()  : 0,
                        "isVerified",       p.getIsVerified()       != null ? p.getIsVerified()       : false
                )))
                .orElse(ResponseEntity.ok(java.util.Map.of(
                        "id", 0, "userId", userId,
                        "organizationName", "", "bio", "", "specialization", "",
                        "websiteUrl", "", "phoneContact", "", "yearsExperience", 0, "isVerified", false
                )));
    }

    // ── Event organizer profile: save ─────────────────────────────────────────
    @PutMapping("/profile/event-organizer")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('EVENT_ORGANIZER')")
    public ResponseEntity<?> saveEventOrganizerProfile(@RequestBody java.util.Map<String, Object> body,
                                                       Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        EventOrganizerProfile p = eventOrganizerProfileRepo.findByUserId(user.getId())
                .orElse(EventOrganizerProfile.builder().user(user).build());
        if (body.containsKey("organizationName")) p.setOrganizationName((String) body.get("organizationName"));
        if (body.containsKey("bio"))              p.setBio((String)              body.get("bio"));
        if (body.containsKey("specialization"))   p.setSpecialization((String)   body.get("specialization"));
        if (body.containsKey("websiteUrl"))       p.setWebsiteUrl((String)       body.get("websiteUrl"));
        if (body.containsKey("phoneContact"))     p.setPhoneContact((String)     body.get("phoneContact"));
        if (body.containsKey("yearsExperience") && body.get("yearsExperience") instanceof Number)
            p.setYearsExperience(((Number) body.get("yearsExperience")).intValue());
        eventOrganizerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Event organizer profile saved"));
    }

    // ── Admin: verify event organizer ─────────────────────────────────────────
    @PutMapping("/profile/event-organizer/{userId}/verify")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyEventOrganizer(@PathVariable Long userId) {
        EventOrganizerProfile p = eventOrganizerProfileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Event organizer profile not found for user " + userId));
        p.setIsVerified(true);
        eventOrganizerProfileRepo.save(p);
        return ResponseEntity.ok(java.util.Map.of("message", "Event organizer verified"));
    }

    // ── Warehouses: get all for logged-in provider ────────────────────────────
    @GetMapping("/profile/provider/warehouses")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<?> getMyWarehouses(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(
                warehouseRepo.findByProviderProfileUserId(user.getId()).stream()
                        .map(w -> java.util.Map.of(
                                "id",      w.getId(),
                                "name",    w.getName()    != null ? w.getName()    : "",
                                "address", w.getAddress() != null ? w.getAddress() : "",
                                "city",    w.getCity()    != null ? w.getCity()    : "",
                                "phone",   w.getPhone()   != null ? w.getPhone()   : ""
                        ))
                        .toList()
        );
    }

    // ── Warehouses: get by userId ─────────────────────────────────────────────
    @GetMapping("/profile/provider/{userId}/warehouses")
    public ResponseEntity<?> getWarehousesByUser(@PathVariable Long userId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Not authenticated"));
        return ResponseEntity.ok(
                warehouseRepo.findByProviderProfileUserId(userId).stream()
                        .map(w -> java.util.Map.of(
                                "id",      w.getId(),
                                "name",    w.getName()    != null ? w.getName()    : "",
                                "address", w.getAddress() != null ? w.getAddress() : "",
                                "city",    w.getCity()    != null ? w.getCity()    : "",
                                "phone",   w.getPhone()   != null ? w.getPhone()   : ""
                        ))
                        .toList()
        );
    }

    // ── Warehouses: create ────────────────────────────────────────────────────
    @PostMapping("/profile/provider/warehouses")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('EQUIPEMENTPROVIEDERS')")
    public ResponseEntity<?> createWarehouse(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        ProviderProfile profile = providerProfileRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Set up your provider profile before creating a warehouse."));
        Warehouse w = Warehouse.builder()
                .name(body.get("name"))
                .address(body.get("address"))
                .city(body.get("city"))
                .phone(body.get("phone"))
                .providerProfile(profile)
                .build();
        Warehouse saved = warehouseRepo.save(w);
        return ResponseEntity.ok(java.util.Map.of(
                "id", saved.getId(), "name", saved.getName() != null ? saved.getName() : "",
                "address", saved.getAddress() != null ? saved.getAddress() : "",
                "city", saved.getCity() != null ? saved.getCity() : "",
                "phone", saved.getPhone() != null ? saved.getPhone() : ""
        ));
    }

    // ── Warehouses: update ────────────────────────────────────────────────────
    @PutMapping("/profile/provider/warehouses/{warehouseId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long warehouseId,
                                             @RequestBody java.util.Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        Warehouse w = warehouseRepo.findById(warehouseId).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        boolean isOwner = w.getProviderProfile().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) return ResponseEntity.status(403).body(java.util.Map.of("error", "Access denied"));
        if (body.containsKey("name"))    w.setName(body.get("name"));
        if (body.containsKey("address")) w.setAddress(body.get("address"));
        if (body.containsKey("city"))    w.setCity(body.get("city"));
        if (body.containsKey("phone"))   w.setPhone(body.get("phone"));
        warehouseRepo.save(w);
        return ResponseEntity.ok(java.util.Map.of("message", "Warehouse updated"));
    }

    // ── Warehouses: delete ────────────────────────────────────────────────────
    @DeleteMapping("/profile/provider/warehouses/{warehouseId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('EQUIPEMENTPROVIEDERS','ADMIN')")
    public ResponseEntity<?> deleteWarehouse(@PathVariable Long warehouseId, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        Warehouse w = warehouseRepo.findById(warehouseId).orElseThrow(() -> new RuntimeException("Warehouse not found"));
        boolean isOwner = w.getProviderProfile().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) return ResponseEntity.status(403).body(java.util.Map.of("error", "Access denied"));
        warehouseRepo.deleteById(warehouseId);
        return ResponseEntity.ok(java.util.Map.of("message", "Warehouse deleted"));
    }

    // ── Admin: list all delivery agents with their profile data ───────────────
    @GetMapping("/profile/agents")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAgents() {
        java.util.List<java.util.Map<String, Object>> result = userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.DELIVERYAGENT)
                .map(u -> {
                    java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("userId",        u.getId());
                    row.put("userFullName",  u.getFullname());
                    row.put("email",         u.getEmail() != null ? u.getEmail() : "");
                    row.put("phoneNumber",   u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
                    // Attach agent profile if it exists
                    agentProfileRepo.findByUserId(u.getId()).ifPresentOrElse(
                            p -> {
                                row.put("id",            p.getId());
                                row.put("licenseNumber", p.getLicenseNumber() != null ? p.getLicenseNumber() : "");
                                row.put("isVerified",    Boolean.TRUE.equals(p.getIsVerified()));
                                row.put("createdAt",     p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
                            },
                            () -> {
                                row.put("id",            null);
                                row.put("licenseNumber", "");
                                row.put("isVerified",    false);
                                row.put("createdAt",     "");
                            }
                    );
                    return row;
                })
                .toList();
        return ResponseEntity.ok(result);
    }
}