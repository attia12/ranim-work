package tn.esprit.projetpidev.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tn.esprit.projetpidev.domain.enums.Role;

@Data
public class RegisterRequest {
    // ── Base user fields ─────────────────────────────────────────────────
    @NotBlank
    @Size(min=2, max=50) @Pattern(regexp="^[A-Za-zÀ-ÿ\\s'\\-]+$")
    private String firstName;
    @NotBlank
    @Size(min=2, max=50) @Pattern(regexp="^[A-Za-zÀ-ÿ\\s'\\-]+$")
    private String lastName;
    @Pattern(regexp="^[+]?[0-9\\s\\-]{7,20}$")

    private String phoneNumber;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min=8)
    @Pattern(regexp="^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
            message="Must have uppercase, number, and special character")
    private String password;
    private Role role;
    private String avatar;
    private String address;
    private String city;

    // ── EQUIPEMENTPROVIEDERS ─────────────────────────────────────────────
    private String companyName;
    private String shopDescription;

    // ── DELIVERYAGENT ────────────────────────────────────────────────────
    private String licenseNumber;

    // ── COMPERS (Camper) ─────────────────────────────────────────────────
    private String experienceLevel;
    private String preferredTerrain;
    private String bio;

    // ── COMPSITEOWNERS ───────────────────────────────────────────────────
    private String businessName;
    private String businessDescription;
    private String websiteUrl;
    private String region;

    // ── GUIDE ────────────────────────────────────────────────────────────
    private String specialization;
    private Integer yearsExperience;
    private String languages;
    private String certificationNumber;

    // ── COACH ────────────────────────────────────────────────────────────
    private String coachingType;
    private String certifications;

    // ── SPONSORS ─────────────────────────────────────────────────────────
    private String sponsorshipDescription;
    private String logoUrl;
    private String industry;

    // ── PARTENERS ────────────────────────────────────────────────────────
    private String organizationName;
    private String partnershipDescription;
    private String partnerType;

    // ── CAMPSITEMANAGER ──────────────────────────────────────────────────
    private String managedRegion;
    private String managerNotes;
}