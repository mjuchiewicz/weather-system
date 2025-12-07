package com.weather.provider.controller;

import com.weather.provider.model.WeatherAlert;
import com.weather.provider.service.WeatherAlertService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/alert-rules")
@CrossOrigin(origins = "*")
public class WeatherAlertController {

    @Autowired
    private WeatherAlertService service;

    // CREATE
    @PostMapping
    public ResponseEntity<EntityModel<WeatherAlert>> createAlert(
            @Valid @RequestBody WeatherAlert alert) {

        WeatherAlert created = service.create(alert);

        EntityModel<WeatherAlert> model = EntityModel.of(created);
        model.add(linkTo(methodOn(WeatherAlertController.class)
                .getAlertById(created.getId())).withSelfRel());

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    // READ - wszystkie
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<WeatherAlert> alertPage = service.getAll(pageable);

        List<EntityModel<WeatherAlert>> alertModels = alertPage.getContent().stream()
                .map(alert -> {
                    EntityModel<WeatherAlert> model = EntityModel.of(alert);
                    model.add(linkTo(methodOn(WeatherAlertController.class)
                            .getAlertById(alert.getId())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", alertModels);
        response.put("currentPage", alertPage.getNumber());
        response.put("totalItems", alertPage.getTotalElements());
        response.put("totalPages", alertPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // READ - po ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<WeatherAlert>> getAlertById(@PathVariable Long id) {
        WeatherAlert alert = service.getById(id);

        EntityModel<WeatherAlert> model = EntityModel.of(alert);
        model.add(linkTo(methodOn(WeatherAlertController.class)
                .getAlertById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    // READ - aktywne
    @GetMapping("/active")
    public ResponseEntity<List<WeatherAlert>> getActiveAlerts() {
        List<WeatherAlert> alerts = service.getActive();
        return ResponseEntity.ok(alerts);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<WeatherAlert>> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody WeatherAlert alert) {

        WeatherAlert updated = service.update(id, alert);

        EntityModel<WeatherAlert> model = EntityModel.of(updated);
        model.add(linkTo(methodOn(WeatherAlertController.class)
                .getAlertById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    // TOGGLE active/inactive
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<WeatherAlert> toggleAlert(@PathVariable Long id) {
        WeatherAlert toggled = service.toggleActive(id);
        return ResponseEntity.ok(toggled);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAlert(@PathVariable Long id) {
        service.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Alert deleted successfully");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }
}