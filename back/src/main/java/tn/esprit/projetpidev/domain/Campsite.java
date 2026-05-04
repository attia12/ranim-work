// Module: Official Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "campsites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campsite {

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

    private String address;

    private Double latitude;
    private Double longitude;

    @Min(1)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampsiteType type;

    @DecimalMin("0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    // Stored as comma-separated URLs; split on read
    @Column(columnDefinition = "TEXT")
    private String pictures;

    // Stored as comma-separated values
    @Column(columnDefinition = "TEXT")
    private String amenities;

    @Column(columnDefinition = "TEXT")
    private String rules;

    // Stored as comma-separated enum values: FOREST,LAKE,MOUNTAIN,BEACH,RIVER,PLAIN
    // Used by the AI recommendation engine for terrain preference scoring
    @Column(columnDefinition = "TEXT")
    private String naturalFeatures;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CampsiteStatus status = CampsiteStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    /** Optional: date from which the campsite is open for bookings */
    private LocalDate startDate;

    /** Optional: date after which the campsite is considered expired */
    private LocalDate endDate;

    /** Set by the scheduler when it auto-changes status */
    private LocalDateTime lastStatusUpdate;

    /** Human-readable reason for the last automated status change */
    private String lastStatusReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
