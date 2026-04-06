package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "guide_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"event", "guide"})
@EqualsAndHashCode(exclude = {"event", "guide"})
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** The guide — a User whose role is GUIDE. Same pattern as Delivery.camper */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private User guide;

    @Column(nullable = false)
    private String roleDescription;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ASSIGNED"; // ASSIGNED | COMPLETED | CANCELLED

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
