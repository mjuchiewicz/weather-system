package com.weather.statistics.controller;

import com.weather.statistics.model.WeatherStatistics;
import com.weather.statistics.service.StatisticsCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsCalculator statisticsCalculator;

    @GetMapping("/{city}")
    public ResponseEntity<?> getStatistics(@PathVariable String city) {
        System.out.println("📊 Statistics request for: " + city);

        try {
            WeatherStatistics stats = statisticsCalculator.calculateAdvancedStatistics(city);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ Error calculating statistics: " + e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("city", city);
            error.put("hint", "Make sure there is historical weather data for this city");

            return ResponseEntity.status(400).body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Statistics Service");
        response.put("description", "Advanced weather statistics with Feign Client");
        return ResponseEntity.ok(response);
    }
}

