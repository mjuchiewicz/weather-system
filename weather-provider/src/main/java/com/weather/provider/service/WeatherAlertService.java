package com.weather.provider.service;

import com.weather.provider.model.WeatherAlert;
import com.weather.provider.repository.WeatherAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WeatherAlertService {

    @Autowired
    private WeatherAlertRepository repository;

    // CREATE
    public WeatherAlert create(WeatherAlert alert) {
        return repository.save(alert);
    }

    // READ - po ID
    public WeatherAlert getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
    }

    // READ - wszystkie (z paginacją)
    public Page<WeatherAlert> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // READ - tylko aktywne
    public List<WeatherAlert> getActive() {
        return repository.findByActiveTrue();
    }

    // READ - po typie
    public List<WeatherAlert> getByType(String alertType) {
        return repository.findByAlertType(alertType);
    }

    // READ - po severity
    public List<WeatherAlert> getBySeverity(String severity) {
        return repository.findBySeverity(severity);
    }

    // UPDATE
    public WeatherAlert update(Long id, WeatherAlert updatedData) {
        WeatherAlert existing = getById(id);

        existing.setAlertType(updatedData.getAlertType());
        existing.setThreshold(updatedData.getThreshold());
        existing.setSeverity(updatedData.getSeverity());
        existing.setColor(updatedData.getColor());
        existing.setMessage(updatedData.getMessage());
        existing.setImageUrl(updatedData.getImageUrl());
        existing.setActive(updatedData.getActive());

        return repository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Alert not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // Toggle active/inactive
    public WeatherAlert toggleActive(Long id) {
        WeatherAlert alert = getById(id);
        alert.setActive(!alert.getActive());
        return repository.save(alert);
    }
}