// Module: Official Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campsite_availabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id", nullable = false)
    private Campsite campsite;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Min(0)
    private Integer numberOfPlaces;

    private String weatherCondition;

    @Builder.Default
    private boolean isBlocked = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
