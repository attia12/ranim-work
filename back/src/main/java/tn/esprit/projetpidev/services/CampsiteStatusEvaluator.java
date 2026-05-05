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

        // 2. Check weather forecast across the campsite's own date range
        //    startDate → endDate. Fallback: today if no startDate, today+16 if no endDate (API max).
        LocalDate forecastStart = (campsite.getStartDate() != null) ? campsite.getStartDate() : today;
        LocalDate forecastEnd   = (campsite.getEndDate()   != null) ? campsite.getEndDate()   : today.plusDays(15);
        // Open-Meteo free tier: 16-day window means indices 0-15 → max date = today+15
        if (forecastEnd.isAfter(today.plusDays(15))) forecastEnd = today.plusDays(15);
        // If the start is in the past, clamp to today (can't fetch past forecasts)
        if (forecastStart.isBefore(today)) forecastStart = today;

        // If campsite opens beyond the forecast window, skip weather check entirely
        if (forecastStart.isAfter(forecastEnd)) {
            log.info("Campsite {} starts {} which is beyond forecast window ({}), skipping weather check",
                    campsite.getId(), campsite.getStartDate(), forecastEnd);
            return new Evaluation(CampsiteStatus.ACTIVE, "Campsite period is beyond the 16-day forecast window.");
        }

        Optional<WeatherData> forecast = weatherService.getForecastWeather(
                campsite.getLatitude(), campsite.getLongitude(), forecastStart, forecastEnd);
        if (forecast.isPresent() && forecast.get().isForecastSevere()) {
            String detail = forecast.get().getFirstSevereDay();
            return new Evaluation(CampsiteStatus.SUSPENDED,
                    "Severe weather forecast (" + forecastStart + " → " + forecastEnd + "): " + detail);
        }

        // 3. Check capacity (fully booked today)
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
