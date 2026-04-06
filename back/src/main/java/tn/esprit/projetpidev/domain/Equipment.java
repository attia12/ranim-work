package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"category", "owner", "warehouse"})
@EqualsAndHashCode(exclude = {"category", "owner", "warehouse"})
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private Float pricePerDay;

    private Float purchasePrice;

    private Boolean availableForRent;

    private Boolean availableForSale;

    private Integer stock;

    @OneToMany(
            mappedBy = "equipment",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER        // ← EAGER so photos always load with the entity
    )
    private List<EquipmentPhoto> photos;

    private String specifications;

    @Column(name = "equipment_condition")
    private String condition;

    private Float weight;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EquipmentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = true)
    private Warehouse warehouse;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;
}

