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

    private static final String OPEN_METEO_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches current weather for the given coordinates.
     * Returns empty if coordinates are null or the API call fails.
     */
    public Optional<WeatherData> getCurrentWeather(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return Optional.empty();
        try {
            WeatherData data = restTemplate.getForObject(
                    OPEN_METEO_URL, WeatherData.class, latitude, longitude);
            return Optional.ofNullable(data);
        } catch (Exception e) {
            log.warn("Weather API call failed for ({},{}): {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }
}
