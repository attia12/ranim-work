package tn.esprit.projetpidev.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "groupes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupeId;

    private String name;
    private boolean isPrivate;
    private Date createAt;

    @ManyToMany
    @JoinTable(
            name = "groupe_users",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}