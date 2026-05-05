// Module: Campsite Status | Layer: Service
package tn.esprit.projetpidev.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projetpidev.dto.weather.WeatherData;

import java.util.Optional;

@Slf4j
@Service
public class WeatherService {

    private static final String CURRENT_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true";

    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
            "&daily=weathercode,windspeed_10m_max&timezone=auto&start_date={start}&end_date={end}";

    private final RestTemplate restTemplate = new RestTemplate();

    /** Fetches current weather (legacy). */
    public Optional<WeatherData> getCurrentWeather(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return Optional.empty();
        try {
            WeatherData data = restTemplate.getForObject(
                    CURRENT_URL, WeatherData.class, latitude, longitude);
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.warn("Weather API call failed for ({},{}): {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fetches daily forecast for the given coordinates and date range.
     * Returns empty if coordinates are null or the API call fails.
     */
    public Optional<WeatherData> getForecastWeather(Double latitude, Double longitude,
                                                     java.time.LocalDate startDate,
                                                     java.time.LocalDate endDate) {
        if (latitude == null || longitude == null) {
            log.warn("Skipping weather check — campsite has null coordinates (lat={}, lon={})", latitude, longitude);
            return Optional.empty();
        }
        log.info("Calling Open-Meteo forecast for ({},{}) from {} to {}", latitude, longitude, startDate, endDate);
        try {
            WeatherData data = restTemplate.getForObject(
                    FORECAST_URL, WeatherData.class,
                    latitude, longitude, startDate.toString(), endDate.toString());
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.warn("Forecast API call failed for ({},{}) [{} to {}]: {}",
                    latitude, longitude, startDate, endDate, e.getMessage());
            return Optional.empty();
        }
    }
}
