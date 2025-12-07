package com.weather.xmlrpc;

import org.springframework.web.bind.annotation.*;

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

        List<Map<String, Object>> history = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("city", city);
            dayData.put("date", date.format(DateTimeFormatter.ISO_DATE));
            dayData.put("temperature", 15.0 + (Math.random() * 15)); // 15-30°C
            dayData.put("description", getRandomDescription());
            dayData.put("humidity", 50 + (int)(Math.random() * 30)); // 50-80%
            dayData.put("windSpeed", 5.0 + (Math.random() * 15)); // 5-20 km/h

            history.add(dayData);
        }

        return history;
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