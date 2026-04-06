package tn.esprit.projetpidev.domain.userprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class PartnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organizationName;

    @Column(columnDefinition = "TEXT")
    private String partnershipDescription;

    private String websiteUrl;

    private String partnerType; // e.g. "Event Organizer", "Wild Campsite", "Forum Moderator"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
