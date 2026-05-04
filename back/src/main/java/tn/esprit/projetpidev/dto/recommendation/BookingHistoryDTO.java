package tn.esprit.projetpidev.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BookingHistoryDTO {

    @JsonProperty("campsite_id")
    private Long campsiteId;

    @JsonProperty("campsite_name")
    private String campsiteName;

    @JsonProperty("natural_features")
    private List<String> naturalFeatures;

    @JsonProperty("price_per_night")
    private Double pricePerNight;

    // Sent as ISO string "YYYY-MM-DD" — avoids WebClient serializing LocalDate as [2024,7,5]
    @JsonProperty("check_in")
    private String checkIn;

    @JsonProperty("check_out")
    private String checkOut;

    private Double latitude;
    private Double longitude;
}
