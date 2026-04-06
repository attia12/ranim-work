// Module: Outdoor Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.enums.OutdoorBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "outdoor_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"outdoorCampsite", "camper"})
@EqualsAndHashCode(exclude = {"outdoorCampsite", "camper"})
public class OutdoorBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outdoor_campsite_id", nullable = false)
    private OutdoorCampsite outdoorCampsite;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camper_id", nullable = false)
    private User camper;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    @Min(1)
    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutdoorBookingStatus status = OutdoorBookingStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
