package tn.esprit.projetpidev.domain.userprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "guide_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class GuideProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String specialization; // e.g. "Desert treks", "Mountain climbing"

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Integer yearsExperience;

    private String languages; // e.g. "Arabic, French, English"

    @Column(unique = true)
    private String certificationNumber;

    @Builder.Default
    private Boolean isVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
