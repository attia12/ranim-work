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
     * WMO codes considered truly severe for camping:
     *   65-67  Heavy rain / freezing rain
     *   73-75  Moderate to heavy snow
     *   77     Snow grains
     *   82     Violent rain showers
     *   95-99  Thunderstorm (with or without hail)
     *
     * NOT severe (campsite stays ACTIVE):
     *   51-63  Light/moderate drizzle or rain
     *   71     Light snow
     *   80-81  Slight/moderate rain showers
     */
    private static final java.util.Set<Integer> SEVERE_CODES = java.util.Set.of(
            61, 63,          // moderate / heavy rain
            65, 66, 67,      // heavy rain / freezing rain
            71, 73, 75, 77,  // snow (all intensities)
            80, 81, 82,      // rain showers (slight → violent)
            85, 86,          // snow showers
            95, 96, 99       // thunderstorm
    );

    /**
     * Returns true if any day in the daily forecast is severe enough to suspend the campsite.
     * Wind > 60 km/h is always considered severe regardless of weather code.
     */
    public boolean isForecastSevere() {
        if (daily == null || daily.getWeathercode() == null) return false;
        java.util.List<Integer> codes = daily.getWeathercode();
        java.util.List<Double> winds = daily.getWindspeed10mMax();
        for (int i = 0; i < codes.size(); i++) {
            Integer codeVal = codes.get(i);
            if (codeVal == null) continue;
            Double windVal = (winds != null && i < winds.size()) ? winds.get(i) : null;
            double wind = (windVal != null) ? windVal : 0.0;
            if (SEVERE_CODES.contains(codeVal) || wind > 60.0) return true;
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
            Integer codeVal = codes.get(i);
            if (codeVal == null) continue;
            Double windVal = (winds != null && i < winds.size()) ? winds.get(i) : null;
            double wind = (windVal != null) ? windVal : 0.0;
            if (SEVERE_CODES.contains(codeVal) || wind > 60.0) {
                String date = (times != null && i < times.size()) ? times.get(i) : "unknown";
                return "date=" + date + ", code=" + codeVal + ", wind=" + wind + " km/h";
            }
        }
        return null;
    }

    /** Legacy: still used for current_weather checks if needed. */
    public boolean isSevere() {
        if (currentWeather == null) return false;
        return SEVERE_CODES.contains(currentWeather.getWeathercode())
                || currentWeather.getWindspeed() > 60.0;
    }
}
