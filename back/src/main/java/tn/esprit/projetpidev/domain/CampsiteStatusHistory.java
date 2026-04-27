// Module: Official Campsite & Booking | Layer: Domain Entity
package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "campsite_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampsiteStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id", nullable = false)
    private Campsite campsite;

    @Enumerated(EnumType.STRING)
    private CampsiteStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampsiteStatus newStatus;

    @Column(length = 500)
    private String reason;

    /** "SCHEDULER", "ADMIN:{email}", etc. */
    @Column(length = 100)
    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
