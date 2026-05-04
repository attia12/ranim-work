package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.dto.recommendation.AiRequestDTO;
import tn.esprit.projetpidev.dto.recommendation.BookingHistoryDTO;
import tn.esprit.projetpidev.dto.recommendation.CampsiteAiDTO;
import tn.esprit.projetpidev.dto.recommendation.RecommendedCampsiteDTO;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CampsiteBookingRepository bookingRepository;
    private final CampsiteRepository campsiteRepository;
    private final AiServiceClient aiServiceClient;

    @Cacheable(value = "recommendations", key = "#user.id")
    @Transactional(readOnly = true)
    public List<RecommendedCampsiteDTO> getRecommendations(User user) {
        Long userId = user.getId();
        log.info("Building recommendations for user {}", userId);

        // Last 24 months of confirmed bookings (max 50)
        LocalDate since = LocalDate.now().minusMonths(24);
        List<CampsiteBooking> recentBookings =
                bookingRepository.findRecentConfirmedByCamperId(userId, since);

        // Campsites already booked by user (to exclude from candidates)
        Set<Long> bookedCampsiteIds = recentBookings.stream()
                .map(b -> b.getCampsite().getId())
                .collect(Collectors.toSet());

        // All ACTIVE campsites not already booked by user
        List<Campsite> activeCampsites = campsiteRepository
                .findAllByStatusIn(List.of(CampsiteStatus.ACTIVE))
                .stream()
                .filter(c -> !bookedCampsiteIds.contains(c.getId()))
                .limit(200)
                .collect(Collectors.toList());

        if (activeCampsites.isEmpty()) {
            log.info("No candidate campsites for user {}", userId);
            return List.of();
        }

        AiRequestDTO aiRequest = new AiRequestDTO();
        aiRequest.setUserId(userId);
        aiRequest.setBookings(mapBookings(recentBookings));
        aiRequest.setReviews(Collections.emptyList()); // campsite reviews not yet implemented
        aiRequest.setAllCampsites(mapCampsites(activeCampsites));

        List<RecommendedCampsiteDTO> result = aiServiceClient.fetchRecommendations(aiRequest);
        log.info("Received {} recommendations for user {}", result.size(), userId);
        return result;
    }

    private List<BookingHistoryDTO> mapBookings(List<CampsiteBooking> bookings) {
        return bookings.stream().map(b -> {
            BookingHistoryDTO dto = new BookingHistoryDTO();
            dto.setCampsiteId(b.getCampsite().getId());
            dto.setCampsiteName(b.getCampsite().getName());
            dto.setNaturalFeatures(parseFeatures(b.getCampsite().getNaturalFeatures()));
            dto.setPricePerNight(
                    b.getCampsite().getPricePerNight() != null
                            ? b.getCampsite().getPricePerNight().doubleValue()
                            : 0.0);
            dto.setCheckIn(b.getCheckInDate().toString());
            dto.setCheckOut(b.getCheckOutDate().toString());
            dto.setLatitude(b.getCampsite().getLatitude() != null ? b.getCampsite().getLatitude() : 0.0);
            dto.setLongitude(b.getCampsite().getLongitude() != null ? b.getCampsite().getLongitude() : 0.0);
            return dto;
        }).collect(Collectors.toList());
    }

    private List<CampsiteAiDTO> mapCampsites(List<Campsite> campsites) {
        return campsites.stream().map(c -> {
            CampsiteAiDTO dto = new CampsiteAiDTO();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setNaturalFeatures(parseFeatures(c.getNaturalFeatures()));
            dto.setPricePerNight(
                    c.getPricePerNight() != null ? c.getPricePerNight().doubleValue() : 0.0);
            dto.setLatitude(c.getLatitude() != null ? c.getLatitude() : 0.0);
            dto.setLongitude(c.getLongitude() != null ? c.getLongitude() : 0.0);
            dto.setGlobalAvgRating(0.0);
            dto.setImageUrl(firstPicture(c.getPictures()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<String> parseFeatures(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String firstPicture(String pictures) {
        if (pictures == null || pictures.isBlank()) return null;
        String first = pictures.split(",")[0].trim();
        return first.isEmpty() ? null : first;
    }
}
