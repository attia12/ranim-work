package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.userprofile.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String password;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private boolean enabled = true; // ← @Builder.Default ajouté

    @Column(columnDefinition = "LONGTEXT")
    private String avatar;

    private String address;

    private String city;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProviderProfile providerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AgentProfile agentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CamperProfile camperProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CampsiteOwnerProfile campsiteOwnerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EventOrganizerProfile eventOrganizerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private GuideProfile guideProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CoachProfile coachProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SponsorProfile sponsorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PartnerProfile partnerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CampsiteManagerProfile campsiteManagerProfile;

    // OneToMany unidirectional - User is the parent
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Comment> comments;

    // OneToMany bidirectional with BlogPost
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BlogPost> blogPosts;

    // OneToMany bidirectional with Like
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Like> likes;

    // OneToMany bidirectional with Message (sender)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> sentMessages;

    // OneToMany bidirectional with Message (receiver)
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<Message> receivedMessages;

    // ManyToMany with Groupe
    @ManyToMany(mappedBy = "users")
    private List<Groupe> groupes;

    public User() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getFullname() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public Long getUserId() {
        return id;

    }

    public void setUserId(long l) {
    }
}