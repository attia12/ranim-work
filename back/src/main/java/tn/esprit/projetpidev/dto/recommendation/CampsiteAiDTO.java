package tn.esprit.projetpidev.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CampsiteAiDTO {

    private Long id;
    private String name;

    @JsonProperty("natural_features")
    private List<String> naturalFeatures;

    @JsonProperty("price_per_night")
    private Double pricePerNight;

    private Double latitude;
    private Double longitude;

    @JsonProperty("global_avg_rating")
    private Double globalAvgRating;

    @JsonProperty("image_url")
    private String imageUrl;
}
