package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.enums.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"items", "rating", "vehicle", "order", "camper"})
@EqualsAndHashCode(exclude = {"items", "rating", "vehicle", "order", "camper"})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING_ASSIGNMENT;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    private Float actualCost;

    private Float earningAmount;

    @Column(nullable = false)
    private LocalDateTime scheduledPickupTime;

    @Column(nullable = false)
    private LocalDateTime scheduledDeliveryTime;

    private LocalDateTime actualPickupTime;

    private LocalDateTime actualDeliveryTime;

    private String proofOfDelivery;

    @Column(columnDefinition = "TEXT")
    private String deliveryNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camper_id", nullable = false)
    private User camper;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryItem> items;

    @OneToOne(mappedBy = "delivery", cascade = CascadeType.ALL, optional = true)
    private DeliveryRating rating;
}

