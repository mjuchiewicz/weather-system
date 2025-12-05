package com.weather.frontend;

import com.weather.frontend.model.WeatherHistory;
import com.weather.frontend.service.WeatherHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/weather-history")
@CrossOrigin(origins = "*")
public class WeatherHistoryController {

    @Autowired
    private WeatherHistoryService service;

    // ========== CREATE ==========

    /**
     * POST /api/weather-history
     * Tworzy nowy wpis historii pogody
     */
    @PostMapping
    public ResponseEntity<EntityModel<WeatherHistory>> createWeather(
            @Valid @RequestBody WeatherHistory weatherHistory) {

        WeatherHistory created = service.create(weatherHistory);

        EntityModel<WeatherHistory> model = EntityModel.of(created);
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getWeatherById(created.getId())).withSelfRel());
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getAllWeather(0, 10, "timestamp")).withRel("all"));

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    // ========== READ ==========

    /**
     * GET /api/weather-history
     * Pobiera wszystkie wpisy (z paginacją)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWeather(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<WeatherHistory> weatherPage = service.getAll(pageable);

        // Konwersja do EntityModel z linkami
        List<EntityModel<WeatherHistory>> weatherModels = weatherPage.getContent().stream()
                .map(weather -> {
                    EntityModel<WeatherHistory> model = EntityModel.of(weather);
                    model.add(linkTo(methodOn(WeatherHistoryController.class)
                            .getWeatherById(weather.getId())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", weatherModels);
        response.put("currentPage", weatherPage.getNumber());
        response.put("totalItems", weatherPage.getTotalElements());
        response.put("totalPages", weatherPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/weather-history/{id}
     * Pobiera wpis po ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<WeatherHistory>> getWeatherById(@PathVariable Long id) {
        WeatherHistory weather = service.getById(id);

        EntityModel<WeatherHistory> model = EntityModel.of(weather);
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getWeatherById(id)).withSelfRel());
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getAllWeather(0, 10, "timestamp")).withRel("all"));
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getWeatherByCity(weather.getCity())).withRel("city-history"));

        return ResponseEntity.ok(model);
    }

    /**
     * GET /api/weather-history/city/{city}
     * Pobiera historię dla danego miasta
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<EntityModel<WeatherHistory>>> getWeatherByCity(
            @PathVariable String city) {

        List<WeatherHistory> weatherList = service.getByCity(city);

        List<EntityModel<WeatherHistory>> models = weatherList.stream()
                .map(weather -> {
                    EntityModel<WeatherHistory> model = EntityModel.of(weather);
                    model.add(linkTo(methodOn(WeatherHistoryController.class)
                            .getWeatherById(weather.getId())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(models);
    }

    /**
     * GET /api/weather-history/hot?temp=30
     * Pobiera gorące dni (temperatura > temp)
     */
    @GetMapping("/hot")
    public ResponseEntity<List<WeatherHistory>> getHotDays(
            @RequestParam(defaultValue = "30.0") Double temp) {

        List<WeatherHistory> hotDays = service.getHotDays(temp);
        return ResponseEntity.ok(hotDays);
    }

    /**
     * GET /api/weather-history/alerts
     * Pobiera wszystkie alerty
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<WeatherHistory>> getAlerts() {
        List<WeatherHistory> alerts = service.getAlerts();
        return ResponseEntity.ok(alerts);
    }

    // ========== UPDATE ==========

    /**
     * PUT /api/weather-history/{id}
     * Aktualizuje istniejący wpis
     */
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<WeatherHistory>> updateWeather(
            @PathVariable Long id,
            @Valid @RequestBody WeatherHistory weatherHistory) {

        WeatherHistory updated = service.update(id, weatherHistory);

        EntityModel<WeatherHistory> model = EntityModel.of(updated);
        model.add(linkTo(methodOn(WeatherHistoryController.class)
                .getWeatherById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    // ========== DELETE ==========

    /**
     * DELETE /api/weather-history/{id}
     * Usuwa wpis po ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWeather(@PathVariable Long id) {
        service.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Weather history deleted successfully");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/weather-history/city/{city}
     * Usuwa wszystkie wpisy dla miasta
     */
    @DeleteMapping("/city/{city}")
    public ResponseEntity<Map<String, String>> deleteByCity(@PathVariable String city) {
        service.deleteByCity(city);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All weather history for city deleted successfully");
        response.put("city", city);

        return ResponseEntity.ok(response);
    }
}