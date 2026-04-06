package tn.esprit.projetpidev.domain.userprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "sponsor_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class SponsorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String sponsorshipDescription;

    private String websiteUrl;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    private String industry; // e.g. "Outdoor gear", "Energy drinks", "Tourism"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}