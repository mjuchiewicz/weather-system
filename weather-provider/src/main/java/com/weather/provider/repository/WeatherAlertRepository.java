package com.weather.provider.repository;

import com.weather.provider.model.WeatherAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    // Znajdź po typie alertu
    List<WeatherAlert> findByAlertType(String alertType);

    // Znajdź aktywne alerty
    List<WeatherAlert> findByActiveTrue();

    // Znajdź po severity
    List<WeatherAlert> findBySeverity(String severity);

    // Znajdź alerty powyżej progu
    @Query("SELECT a FROM WeatherAlert a WHERE a.threshold > :value AND a.active = true")
    List<WeatherAlert> findAlertsAboveThreshold(Double value);

    // Znajdź z paginacją
    Page<WeatherAlert> findByActiveTrue(Pageable pageable);
}