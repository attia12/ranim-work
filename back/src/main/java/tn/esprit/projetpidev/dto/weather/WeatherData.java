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

    @JsonProperty("daily")
    private DailyForecast daily;

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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyForecast {
        @JsonProperty("time")
        private java.util.List<String> time;
        @JsonProperty("weathercode")
        private java.util.List<Integer> weathercode;
        @JsonProperty("windspeed_10m_max")
        private java.util.List<Double> windspeed10mMax;
    }

    /**
     * Returns true if any day in the daily forecast is severe.
     * WMO codes: 65+ = heavy rain, 71+ = snow, 95+ = thunderstorm. Wind > 60 km/h also counts.
     */
    public boolean isForecastSevere() {
        if (daily == null || daily.getWeathercode() == null) return false;
        java.util.List<Integer> codes = daily.getWeathercode();
        java.util.List<Double> winds = daily.getWindspeed10mMax();
        for (int i = 0; i < codes.size(); i++) {
            int code = codes.get(i);
            double wind = (winds != null && i < winds.size()) ? winds.get(i) : 0.0;
            if (code >= 65 || wind > 60.0) return true;
        }
        return false;
    }

    /** Returns a description of the first severe day found in the forecast. */
    public String getFirstSevereDay() {
        if (daily == null || daily.getWeathercode() == null) return null;
        java.util.List<String> times = daily.getTime();
        java.util.List<Integer> codes = daily.getWeathercode();
        java.util.List<Double> winds = daily.getWindspeed10mMax();
        for (int i = 0; i < codes.size(); i++) {
            int code = codes.get(i);
            double wind = (winds != null && i < winds.size()) ? winds.get(i) : 0.0;
            if (code >= 65 || wind > 60.0) {
                String date = (times != null && i < times.size()) ? times.get(i) : "unknown";
                return "date=" + date + ", code=" + code + ", wind=" + wind + " km/h";
            }
        }
        return null;
    }

    /** Legacy: still used for current_weather checks if needed. */
    public boolean isSevere() {
        if (currentWeather == null) return false;
        return currentWeather.getWeathercode() >= 65 || currentWeather.getWindspeed() > 60.0;
    }
}
