package com.weather.frontend.service;

import com.weather.frontend.model.WeatherHistory;
import com.weather.frontend.repository.WeatherHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WeatherHistoryService {

    @Autowired
    private WeatherHistoryRepository repository;

    // CREATE - Dodaj nowy wpis
    public WeatherHistory create(WeatherHistory weatherHistory) {
        return repository.save(weatherHistory);
    }

    // READ - Pobierz po ID
    public WeatherHistory getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weather history not found with id: " + id));
    }

    // READ - Pobierz wszystkie (z paginacją)
    public Page<WeatherHistory> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // READ - Pobierz po mieście
    public List<WeatherHistory> getByCity(String city) {
        return repository.findByCity(city);
    }

    // READ - Pobierz gorące dni
    public List<WeatherHistory> getHotDays(Double temperature) {
        return repository.findHotDays(temperature);
    }

    // READ - Pobierz alerty
    public List<WeatherHistory> getAlerts() {
        return repository.findByStatus("ALERT");
    }

    // UPDATE - Aktualizuj wpis
    public WeatherHistory update(Long id, WeatherHistory updatedData) {
        WeatherHistory existing = getById(id);

        existing.setCity(updatedData.getCity());
        existing.setTemperature(updatedData.getTemperature());
        existing.setDescription(updatedData.getDescription());
        existing.setStatus(updatedData.getStatus());

        return repository.save(existing);
    }

    // DELETE - Usuń wpis
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Weather history not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // DELETE - Usuń wszystkie dla miasta
    public void deleteByCity(String city) {
        List<WeatherHistory> records = repository.findByCity(city);
        repository.deleteAll(records);
    }
}