package com.weather.provider.controller;

import com.weather.provider.model.WeatherHistory;
import com.weather.provider.repository.WeatherHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class WeatherHistoryController {

    @Autowired
    private WeatherHistoryRepository repository;

    // Pobierz ostatnich N rekordów dla miasta
    @GetMapping("/{city}")
    public List<WeatherHistory> getHistory(
            @PathVariable String city,
            @RequestParam(defaultValue = "5") int limit) {

        System.out.println("History request: city=" + city + ", limit=" + limit);

        List<WeatherHistory> history = repository.findByCityOrderByTimestampDesc(city);

        if (history.size() > limit) {
            return history.subList(0, limit);
        }

        return history;
    }

    // Statystyki dla miasta (dla SOAP)
    @GetMapping("/{city}/stats")
    public Map<String, Object> getStats(@PathVariable String city) {

        System.out.println("Stats request for: " + city);

        Double avg = repository.getAverageTemperature(city);
        Double min = repository.getMinTemperature(city);
        Double max = repository.getMaxTemperature(city);

        long count = repository.findByCity(city).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("city", city);
        stats.put("avgTemp", avg != null ? avg : 0.0);
        stats.put("minTemp", min != null ? min : 0.0);
        stats.put("maxTemp", max != null ? max : 0.0);
        stats.put("recordCount", count);

        System.out.println("Stats: AVG=" + avg + ", MIN=" + min + ", MAX=" + max);

        return stats;
    }

    // Wszystkie rekordy (debug)
    @GetMapping("/all")
    public List<WeatherHistory> getAllHistory() {
        return repository.findAll();
    }
}