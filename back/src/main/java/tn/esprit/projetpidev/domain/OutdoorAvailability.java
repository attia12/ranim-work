// Module: Outdoor Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "outdoor_availabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutdoorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outdoor_campsite_id", nullable = false)
    private OutdoorCampsite outdoorCampsite;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Builder.Default
    private boolean isAvailable = true;

    private String note;
}
