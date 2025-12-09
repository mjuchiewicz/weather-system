package com.weather.provider.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenWeatherMapService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherData getWeather(String city) {
        try {
            String url = String.format("%s?q=%s&appid=%s&units=metric",
                    apiUrl, city, apiKey);

            System.out.println("Calling OpenWeatherMap API for: " + city);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                return getFallbackWeather(city);
            }

            // Parse response
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            Map<String, Object> weather = ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);

            double temp = ((Number) main.get("temp")).doubleValue();
            String description = (String) weather.get("description");
            int humidity = ((Number) main.get("humidity")).intValue();

            System.out.println("API Response: " + temp + "°C, " + description);

            return new WeatherData(temp, description, humidity);

        } catch (Exception e) {
            System.err.println("API call failed: " + e.getMessage());
            return getFallbackWeather(city);
        }
    }

    private WeatherData getFallbackWeather(String city) {
        System.out.println("Using fallback data for: " + city);
        double temp = 15.0 + (Math.random() * 20);
        return new WeatherData(temp, "Data unavailable (fallback)", 65);
    }

    // Inner class for weather data
    public static class WeatherData {
        private final double temperature;
        private final String description;
        private final int humidity;

        public WeatherData(double temperature, String description, int humidity) {
            this.temperature = temperature;
            this.description = description;
            this.humidity = humidity;
        }

        public double getTemperature() { return temperature; }
        public String getDescription() { return description; }
        public int getHumidity() { return humidity; }
    }
}