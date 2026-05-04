package tn.esprit.projetpidev.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AiRequestDTO {

    @JsonProperty("user_id")
    private Long userId;

    private List<BookingHistoryDTO> bookings;
    private List<ReviewHistoryDTO> reviews;

    @JsonProperty("all_campsites")
    private List<CampsiteAiDTO> allCampsites;
}
