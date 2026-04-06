package tn.esprit.projetpidev.dto.groupe;

import lombok.Data;
import java.util.Date;

@Data
public class GroupeResponse {
    private Long groupeId;
    private String name;
    private boolean isPrivate;
    private Date createAt;
    private int totalMembers;
}