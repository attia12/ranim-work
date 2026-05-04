package tn.esprit.projetpidev.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class RecommendedCampsiteDTO {

    @JsonAlias("campsite_id")
    private Long campsiteId;

    @JsonAlias("campsite_name")
    private String campsiteName;

    private Double score;

    @JsonAlias("score_breakdown")
    private ScoreBreakdownDTO scoreBreakdown;

    @JsonAlias("weather_description")
    private String weatherDescription;

    @JsonAlias("natural_features")
    private List<String> naturalFeatures;

    @JsonAlias("price_per_night")
    private Double pricePerNight;

    @JsonAlias("image_url")
    private String imageUrl;
}
