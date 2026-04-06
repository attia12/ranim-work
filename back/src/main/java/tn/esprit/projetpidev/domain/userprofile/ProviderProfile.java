package tn.esprit.projetpidev.domain.userprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.Warehouse;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "provider_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String shopDescription;

    @OneToMany(mappedBy = "providerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Warehouse> warehouses;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
