package com.weather.xmlrpc;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/xmlrpc")
@CrossOrigin(origins = "*")
public class HistoricalWeatherHandler {

    @PostMapping
    public Map<String, Object> handleXmlRpcRequest(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");

        System.out.println("XML-RPC style request received: " + method);

        if ("Weather.getHistoricalWeather".equals(method)) {
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            String city = (String) params.get("city");
            String date = (String) params.get("date");

            return getHistoricalWeather(city, date);
        } else if ("Weather.getServiceInfo".equals(method)) {
            Map<String, Object> result = new HashMap<>();
            result.put("info", getServiceInfo());
            return result;
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "Unknown method: " + method);
        return error;
    }

    @GetMapping("/historical")
    public List<Map<String, Object>> getHistoricalWeatherRest(
            @RequestParam String city,
            @RequestParam(defaultValue = "5") int days) {

        System.out.println("XML-RPC request: city=" + city + ", days=" + days);

        try {
            // weather-provider API
            String weatherProviderUrl = System.getenv().getOrDefault("WEATHER_PROVIDER_URL", "http://localhost:8084");
            String url = weatherProviderUrl + "/api/history/" + city + "?limit=" + days;
            RestTemplate restTemplate = new RestTemplate();
            List<Map<String, Object>> history = restTemplate.getForObject(url, List.class);

            System.out.println("Fetched " + (history != null ? history.size() : 0) + " records from weather-provider");

            return history != null ? history : new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Failed to fetch history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getRandomDescription() {
        String[] descriptions = {"Sunny", "Cloudy", "Partly Cloudy", "Rainy", "Windy"};
        return descriptions[(int)(Math.random() * descriptions.length)];
    }

    private Map<String, Object> getHistoricalWeather(String city, String date) {
        System.out.println("XML-RPC Request: city=" + city + ", date=" + date);

        Map<String, Object> result = new HashMap<>();
        result.put("city", city);
        result.put("date", date);
        result.put("temperature", 20.0 + (Math.random() * 10));
        result.put("description", "Partly Cloudy");
        result.put("humidity", 65);
        result.put("windSpeed", 12.5);
        result.put("retrievedAt", LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        System.out.println("XML-RPC Response sent for: " + city);

        return result;
    }

    private String getServiceInfo() {
        return "Historical Weather XML-RPC Service v1.0";
    }
}