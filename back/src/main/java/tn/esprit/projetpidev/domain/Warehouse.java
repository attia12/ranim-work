package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.userprofile.ProviderProfile;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warehouses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"providerProfile", "equipment"})
@EqualsAndHashCode(exclude = {"providerProfile", "equipment"})
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    private String phone;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    private List<Equipment> equipment;
}
