// Module: Campsite Status | Layer: Service
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.dto.weather.WeatherData;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampsiteStatusEvaluator {

    private final CampsiteBookingRepository bookingRepository;
    private final WeatherService weatherService;

    public record Evaluation(CampsiteStatus status, String reason) {}

    /**
     * Determines the correct status for the campsite based on:
     * 1. Date range (PENDING / EXPIRED)
     * 2. Weather (SUSPENDED if severe)
     * 3. Capacity (FULL if no capacity for today)
     * 4. Otherwise ACTIVE
     *
     * Never changes SUSPENDED or DELETED campsites — those are manual states.
     */
    public Evaluation evaluate(Campsite campsite) {
        LocalDate today = LocalDate.now();

        // 1. Check expiry
        if (campsite.getEndDate() != null && campsite.getEndDate().isBefore(today)) {
            return new Evaluation(CampsiteStatus.EXPIRED, "End date " + campsite.getEndDate() + " has passed.");
        }

        // 2. Check start date (not yet open)
        if (campsite.getStartDate() != null && campsite.getStartDate().isAfter(today)) {
            return new Evaluation(CampsiteStatus.PENDING, "Start date " + campsite.getStartDate() + " not reached yet.");
        }

        // 3. Check weather
        Optional<WeatherData> weather = weatherService.getCurrentWeather(campsite.getLatitude(), campsite.getLongitude());
        if (weather.isPresent() && weather.get().isSevere()) {
            WeatherData.CurrentWeather cw = weather.get().getCurrentWeather();
            return new Evaluation(CampsiteStatus.SUSPENDED,
                    "Severe weather: code=" + cw.getWeathercode() + ", wind=" + cw.getWindspeed() + " km/h.");
        }

        // 4. Check capacity (fully booked today)
        if (campsite.getCapacity() != null && campsite.getCapacity() > 0) {
            int guestsToday = bookingRepository.sumGuestsOverlapping(
                    campsite.getId(), today, today.plusDays(1));
            if (guestsToday >= campsite.getCapacity()) {
                return new Evaluation(CampsiteStatus.FULL,
                        "At full capacity: " + guestsToday + "/" + campsite.getCapacity() + " guests booked today.");
            }
        }

        return new Evaluation(CampsiteStatus.ACTIVE, "All checks passed.");
    }
}
