package tn.esprit.projetpidev.dto.recommendation;

import lombok.Data;

@Data
public class ScoreBreakdownDTO {
    private Double terrain;
    private Double budget;
    private Double season;
    private Double rating;
    private Double weather;
    private Double total;
}
