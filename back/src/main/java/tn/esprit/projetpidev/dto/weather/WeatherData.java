// Module: Campsite Status | Layer: DTO (Open-Meteo response wrapper)
package tn.esprit.projetpidev.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {

    @JsonProperty("current_weather")
    private CurrentWeather currentWeather;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        /** Air temperature in Celsius */
        private double temperature;
        /** Wind speed in km/h */
        private double windspeed;
        /** WMO Weather interpretation code */
        private int weathercode;
    }

    /**
     * Returns true if weather conditions are severe enough to warrant suspension.
     * WMO codes: 65+ = moderate/heavy rain, 71+ = snow, 80+ = heavy showers,
     * 85+ = snow showers, 95+ = thunderstorm. Wind > 60 km/h also counts.
     */
    public boolean isSevere() {
        if (currentWeather == null) return false;
        int code = currentWeather.getWeathercode();
        double wind = currentWeather.getWindspeed();
        return code >= 65 || wind > 60.0;
    }
}
