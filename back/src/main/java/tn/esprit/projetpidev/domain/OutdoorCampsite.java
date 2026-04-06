// Module: Outdoor Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.enums.AccessDifficulty;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "outdoor_campsites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutdoorCampsite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private Double latitude;
    private Double longitude;

    // Comma-separated picture URLs
    @Column(columnDefinition = "TEXT")
    private String pictures;

    // Comma-separated natural features
    @Column(columnDefinition = "TEXT")
    private String naturalFeatures;

    @Enumerated(EnumType.STRING)
    private AccessDifficulty accessDifficulty;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by_id", nullable = false)
    private User proposedBy;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutdoorCampsiteStatus status = OutdoorCampsiteStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User approvedBy;

    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
