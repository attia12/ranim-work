package tn.esprit.projetpidev.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReviewHistoryDTO {

    @JsonProperty("campsite_id")
    private Long campsiteId;

    private Double score;
}
