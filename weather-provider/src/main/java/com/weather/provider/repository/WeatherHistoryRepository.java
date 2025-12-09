package com.weather.provider.repository;

import com.weather.provider.model.WeatherHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherHistoryRepository extends JpaRepository<WeatherHistory, Long> {

    // Znajdź po mieście
    List<WeatherHistory> findByCity(String city);

    // Znajdź ostatnie N rekordów dla miasta
    List<WeatherHistory> findByCityOrderByTimestampDesc(String city);

    // Znajdź rekordy z ostatnich N dni
    @Query("SELECT h FROM WeatherHistory h WHERE h.city = :city AND h.timestamp > :since ORDER BY h.timestamp DESC")
    List<WeatherHistory> findRecentByCity(String city, LocalDateTime since);

    // Statystyki dla miasta (dla SOAP)
    @Query("SELECT AVG(h.temperature) FROM WeatherHistory h WHERE h.city = :city")
    Double getAverageTemperature(String city);

    @Query("SELECT MIN(h.temperature) FROM WeatherHistory h WHERE h.city = :city")
    Double getMinTemperature(String city);

    @Query("SELECT MAX(h.temperature) FROM WeatherHistory h WHERE h.city = :city")
    Double getMaxTemperature(String city);
}